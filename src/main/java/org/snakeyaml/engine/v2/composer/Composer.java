/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.snakeyaml.engine.v2.composer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.comments.CommentEventsCollector;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.events.AliasEvent;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.MappingStartEvent;
import org.snakeyaml.engine.v2.events.NodeEvent;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.SequenceStartEvent;
import org.snakeyaml.engine.v2.exceptions.ComposerException;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.NodeType;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;
import org.snakeyaml.engine.v2.util.MergeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a node graph from parser events.
 * <p>
 * Corresponds to the 'Composer' step as described in chapter 3.1.2 of the
 * <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">YAML Specification</a>.
 * </p>
 * It implements {@link Iterator} to get the stream of {@link Node}s from the input.
 */
public class Composer implements Iterator<Node> {

    /**
     * Event parser
     */
    protected final Parser parser;
    private final ScalarResolver scalarResolver;
    private final Map<Anchor, Node> anchors = new HashMap<>();
    private final Set<Node> recursiveNodes = new HashSet<>();
    private final LoadSettings settings;
    private final CommentEventsCollector blockCommentsCollector;
    private final CommentEventsCollector inlineCommentsCollector;
    private int nonScalarAliasesCount = 0;
    private final MergeUtils mergeUtils;

    /**
     * Create
     *
     * @param settings - configuration options
     * @param parser - the input
     */
    public Composer(@NonNull LoadSettings settings, @NonNull Parser parser) {
        this.parser = parser;
        this.scalarResolver = settings.schema().getScalarResolver();
        this.settings = settings;
        this.blockCommentsCollector = new CommentEventsCollector(parser, CommentType.BLANK_LINE, CommentType.BLOCK);
        this.inlineCommentsCollector = new CommentEventsCollector(parser, CommentType.IN_LINE);
        this.mergeUtils = new MergeUtils() {
            @Override
            public @NonNull MappingNode asMappingNode(@NonNull Node node) {
                return Composer.this.asMappingNode(node);
            }
        };
    }

    /**
     * Checks if further documents are available.
     *
     * @return <code>true</code> if there is at least one more document.
     */
    @Override
    public boolean hasNext() {
        // Drop the STREAM-START event.
        if (parser.checkEvent(Event.Id.StreamStart)) {
            parser.next();
        }
        // If there are more documents available?
        return !parser.checkEvent(Event.Id.StreamEnd);
    }

    /**
     * Reads a document from a source that contains only one document.
     * <p>
     * If the stream contains more than one document, an exception is thrown.
     * </p>
     *
     * @return The root node of the document or <code>null</code> if no document is available.
     */
    public Node getSingleNode() {
        // Drop the STREAM-START event.
        parser.next();
        // Compose a document if the stream is not empty.
        Node document = null;
        if (!parser.checkEvent(Event.Id.StreamEnd)) {
            document = next();
        }
        if (document != null) {
            // is there a better place for this code? Should it be in the Node?
            document.setInLineComments(inlineCommentsCollector.collectEvents().consume());
            document.setBlockComments(blockCommentsCollector.collectEvents().consume());
        }
        // Ensure that the stream contains no more documents.
        if (!parser.checkEvent(Event.Id.StreamEnd)) {
            Event event = parser.next();
            Mark previousDocMark = document != null ? document.getStartMark() : null;
            throw new ComposerException("Expected a single document in the stream", previousDocMark, "but found another document", event.getStartMark());
        }
        // Drop the STREAM-END event.
        parser.next();
        return document;
    }

    /**
     * Reads and composes the next document.
     *
     * @return The root node of the document or <code>null</code> if no more documents are available.
     */
    @Override
    public @NonNull Node next() {
        // Collect inter-document start comments
        blockCommentsCollector.collectEvents();
        if (parser.checkEvent(Event.Id.StreamEnd)) {
            List<CommentLine> commentLines = blockCommentsCollector.consume();
            Mark startMark = commentLines.get(0).startMark();
            Node node = new MappingNode(Tag.COMMENT, false, List.of(), FlowStyle.BLOCK, startMark, null);
            node.setBlockComments(commentLines);
            return node;
        }
        // Drop the DOCUMENT-START event.
        parser.next();
        // Compose the root node.
        Node node = composeNode(null);
        // Drop the DOCUMENT-END event.
        blockCommentsCollector.collectEvents();
        if (!blockCommentsCollector.isEmpty()) {
            node.setEndComments(blockCommentsCollector.consume());
        }
        parser.next();
        this.anchors.clear();
        this.recursiveNodes.clear();
        this.nonScalarAliasesCount = 0;
        return node;
    }

    private Node composeNode(Node parent) {
        blockCommentsCollector.collectEvents();
        if (parent != null) {
            recursiveNodes.add(parent);
        }
        final Node node;
        if (parser.checkEvent(Event.Id.Alias)) {
            var event = (AliasEvent) parser.next();
            Anchor anchor = event.getAlias();
            if (!anchors.containsKey(anchor)) {
                throw new ComposerException("found undefined alias " + anchor, event.getStartMark());
            }
            node = anchors.get(anchor);
            if (node.getNodeType() != NodeType.SCALAR) {
                this.nonScalarAliasesCount++;
                if (this.nonScalarAliasesCount > settings.maxAliasesForCollections()) {
                    throw new YamlEngineException("Number of aliases for non-scalar nodes exceeds the specified max=" + settings.maxAliasesForCollections());
                }
            }
            if (recursiveNodes.remove(node)) {
                node.setRecursive(true);
            }
            // drop comments, they cannot be supported here
            blockCommentsCollector.consume();
            inlineCommentsCollector.collectEvents().consume();
        } else {
            var event = (NodeEvent) parser.peekEvent();
            Anchor anchor = event.getAnchor();
            // the check for duplicate anchors has been removed (issue 174)
            if (parser.checkEvent(Event.Id.Scalar)) {
                node = composeScalarNode(anchor, blockCommentsCollector.consume());
            } else if (parser.checkEvent(Event.Id.SequenceStart)) {
                node = composeSequenceNode(anchor);
            } else {
                node = composeMappingNode(anchor);
            }
        }
        if (parent != null) {
            recursiveNodes.remove(parent);
        }
        return node;
    }

    private void registerAnchor(Anchor anchor, Node node) {
        anchors.put(anchor, node);
        node.setAnchor(anchor);
    }

    /**
     * Create ScalarNode
     *
     * @param anchor - anchor if present
     * @param blockComments - comments before the Node
     * @return Node
     */
    protected Node composeScalarNode(Anchor anchor, List<CommentLine> blockComments) {
        var ev = (ScalarEvent) parser.next();
        String tag = ev.getTag();
        boolean resolved = false;
        Tag nodeTag;
        if (tag == null || tag.equals("!")) {
            nodeTag = scalarResolver.resolve(ev.getValue(), ev.getImplicit().isCanOmitTagInPlainScalar());
            resolved = true;
        } else {
            nodeTag = new Tag(tag);
        }
        var node = new ScalarNode(nodeTag, resolved, ev.getValue(), ev.getScalarStyle(), ev.getStartMark(), ev.getEndMark());
        if (anchor != null) {
            registerAnchor(anchor, node);
        }
        node.setBlockComments(blockComments);
        node.setInLineComments(inlineCommentsCollector.collectEvents().consume());
        return node;
    }

    /**
     * Compose a sequence Node from the input starting with SequenceStartEvent
     *
     * @param anchor - anchor if present
     * @return parsed Node
     */
    protected SequenceNode composeSequenceNode(Anchor anchor) {
        var startEvent = (SequenceStartEvent) parser.next();
        String tag = startEvent.getTag();
        Tag nodeTag;
        boolean resolved = false;
        if (tag == null || tag.equals("!")) {
            nodeTag = Tag.SEQ;
            resolved = true;
        } else {
            nodeTag = new Tag(tag);
        }
        final var children = new ArrayList<Node>();
        var node = new SequenceNode(nodeTag, resolved, children, startEvent.getFlowStyle(),
            startEvent.getStartMark(), null);
        if (startEvent.isFlow()) {
            node.setBlockComments(blockCommentsCollector.consume());
        }
        if (anchor != null) {
            registerAnchor(anchor, node);
        }
        while (!parser.checkEvent(Event.Id.SequenceEnd)) {
            blockCommentsCollector.collectEvents();
            if (parser.checkEvent(Event.Id.SequenceEnd)) {
                break;
            }
            children.add(composeNode(node));
        }
        if (startEvent.isFlow()) {
            node.setInLineComments(inlineCommentsCollector.collectEvents().consume());
        }
        Event endEvent = parser.next();
        node.setEndMark(endEvent.getEndMark());
        inlineCommentsCollector.collectEvents();
        if (!inlineCommentsCollector.isEmpty()) {
            node.setInLineComments(inlineCommentsCollector.consume());
        }
        return node;
    }

    /**
     * Create mapping Node
     *
     * @param anchor - anchor if present
     * @return Node
     */
    protected Node composeMappingNode(Anchor anchor) {
        var startEvent = (MappingStartEvent) parser.next();
        String tag = startEvent.getTag();
        Tag nodeTag;
        boolean resolved = false;
        if (tag == null || tag.equals("!")) {
            nodeTag = Tag.MAP;
            resolved = true;
        } else {
            nodeTag = new Tag(tag);
        }

        final var children = new ArrayList<NodeTuple>();
        var node = new MappingNode(nodeTag, resolved, children, startEvent.getFlowStyle(), startEvent.getStartMark(), null);
        if (startEvent.isFlow()) {
            node.setBlockComments(blockCommentsCollector.consume());
        }
        if (anchor != null) {
            registerAnchor(anchor, node);
        }
        while (!parser.checkEvent(Event.Id.MappingEnd)) {
            blockCommentsCollector.collectEvents();
            if (parser.checkEvent(Event.Id.MappingEnd)) {
                break;
            }
            composeMappingChildren(children, node);
        }
        if (startEvent.isFlow()) {
            node.setInLineComments(inlineCommentsCollector.collectEvents().consume());
        }
        Event endEvent = parser.next();
        node.setEndMark(endEvent.getEndMark());
        inlineCommentsCollector.collectEvents();
        if (!inlineCommentsCollector.isEmpty()) {
            node.setInLineComments(inlineCommentsCollector.consume());
        }
        if (node.hasMergeTag()) {
            List<NodeTuple> updatedValue = mergeUtils.flatten(node);
            node.setValue(updatedValue);
            node.setHasMergeTag(false);
        }
        return node;
    }

    /**
     * Add the provided Node to the children as the last child
     *
     * @param children - the list to be extended
     * @param node - the child to the children
     */
    protected void composeMappingChildren(List<NodeTuple> children, MappingNode node) {
        Node itemKey = composeKeyNode(node);
        if (itemKey.getNodeType() != NodeType.SCALAR && !settings.allowNonScalarKeys()) {
            throw new YamlEngineException("Non scalar key is detected but it is not configured to be allowed.");
        }
        if (itemKey.getTag().equals(Tag.MERGE)) {
            node.setHasMergeTag(true);
        }
        Node itemValue = composeValueNode(node);
        children.add(new NodeTuple(itemKey, itemValue));
    }

    protected @NonNull MappingNode asMappingNode(@NonNull Node node) {
        if (node instanceof MappingNode mappingNode) {
            return mappingNode;
        }
        Anchor anchor = node.getAnchor();
        if (anchor != null) {
            Node ref = anchors.get(anchor);
            if (ref instanceof MappingNode mappingNode) {
                return mappingNode;
            }
        }
        Event ev = parser.peekEvent();
        throw new ComposerException("Expected mapping node or an anchor referencing mapping", ev.getStartMark());
    }

    /**
     * To be able to override composeNode(node) which is a key
     *
     * @param node - the source
     * @return node
     */
    protected @NonNull Node composeKeyNode(@Nullable MappingNode node) {
        return composeNode(node);
    }

    /**
     * To be able to override composeNode(node) which is a value
     *
     * @param node - the source
     * @return node
     */
    protected @NonNull Node composeValueNode(@Nullable MappingNode node) {
        return composeNode(node);
    }
}
