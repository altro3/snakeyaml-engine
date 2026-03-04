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
package org.snakeyaml.engine.v2.serializer;

import org.jspecify.annotations.NonNull;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.emitter.Emitable;
import org.snakeyaml.engine.v2.events.AliasEvent;
import org.snakeyaml.engine.v2.events.CommentEvent;
import org.snakeyaml.engine.v2.events.DocumentEndEvent;
import org.snakeyaml.engine.v2.events.DocumentStartEvent;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.ImplicitTuple;
import org.snakeyaml.engine.v2.events.MappingEndEvent;
import org.snakeyaml.engine.v2.events.MappingStartEvent;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.SequenceEndEvent;
import org.snakeyaml.engine.v2.events.SequenceStartEvent;
import org.snakeyaml.engine.v2.events.StreamEndEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.AnchorNode;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.NodeType;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.util.MergeUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transform a Node Graph to Event stream and allow provided {@link Emitable} to present the
 * {@link Event}s into the output stream
 */
public class Serializer {

    private final DumpSettings settings;
    private final Emitable emitable;
    private final Set<Node> serializedNodes;
    private final Map<Node, Anchor> anchors;
    private final boolean dereferenceAliases;
    private final Set<Node> recursive;
    private final MergeUtils mergeUtils;

    /**
     * Create Serializer
     *
     * @param settings - dump configuration
     * @param emitable - destination for the event stream
     */
    public Serializer(DumpSettings settings, Emitable emitable) {
        this.settings = settings;
        this.emitable = emitable;
        this.serializedNodes = new HashSet<>();
        this.anchors = new HashMap<>();
        this.dereferenceAliases = settings.dereferenceAliases();
        this.recursive = Collections.newSetFromMap(new IdentityHashMap<>());
        this.mergeUtils = new MergeUtils() {
            @Override
            public @NonNull MappingNode asMappingNode(@NonNull Node node) {
                if (node instanceof MappingNode mappingNode) {
                    return mappingNode;
                }
                // TODO: This need to be explored more to understand if only MappingNode possible.
                // Or at least the error message needs to be improved.
                throw new YamlEngineException("expecting MappingNode while processing merge.");
            }
        };
    }

    /**
     * Serialize document
     *
     * @param node - the document root
     */
    public void serializeDocument(Node node) {
        this.emitable.emit(new DocumentStartEvent(settings.explicitStart(), settings.yamlDirective(), settings.tagDirective()));
        anchorNode(node);
        if (settings.explicitRootTag() != null) {
            node.setTag(settings.explicitRootTag());
        }
        serializeNode(node);
        this.emitable.emit(new DocumentEndEvent(settings.explicitEnd()));
        this.serializedNodes.clear();
        this.anchors.clear();
        this.recursive.clear();
    }

    /**
     * Emit {@link StreamStartEvent}
     */
    public void emitStreamStart() {
        this.emitable.emit(new StreamStartEvent());
    }

    /**
     * Emit {@link StreamEndEvent}
     */
    public void emitStreamEnd() {
        this.emitable.emit(new StreamEndEvent());
    }

    private void anchorNode(Node node) {
        final Node realNode;
        if (node.getNodeType() == NodeType.ANCHOR) {
            realNode = ((AnchorNode) node).getRealNode();
        } else {
            realNode = node;
        }
        if (this.anchors.containsKey(realNode)) {
            // it looks weird, anchor does contain the key node, but we call computeIfAbsent()
            // this is because the value is null (HashMap permits values to be null)
            this.anchors.computeIfAbsent(realNode, a -> settings.anchorGenerator().nextAnchor(realNode));
        } else {
            this.anchors.put(realNode, realNode.getAnchor() != null ? settings.anchorGenerator().nextAnchor(realNode) : null);
            switch (realNode.getNodeType()) {
                case SEQUENCE:
                    var seqNode = (SequenceNode) realNode;
                    List<Node> nodes = seqNode.getValue();
                    for (var nodeItem: nodes) {
                        anchorNode(nodeItem);
                    }
                    break;
                case MAPPING:
                    var mappingNode = (MappingNode) realNode;
                    List<NodeTuple> nodeTuples = mappingNode.getValue();
                    for (var nodeTuple : nodeTuples) {
                        anchorNode(nodeTuple.keyNode());
                        anchorNode(nodeTuple.valueNode());
                    }
                    break;
                default: // no further action required for non-collections
            }
        }
    }

    /**
     * Recursive serialization of a {@link Node}
     *
     * @param node - content
     */
    private void serializeNode(Node node) {
        if (node.getNodeType() == NodeType.ANCHOR) {
            node = ((AnchorNode) node).getRealNode();
        }
        if (dereferenceAliases && recursive.contains(node)) {
            throw new YamlEngineException("Cannot dereferenceAliases for recursive structures.");
        }
        recursive.add(node);
        Anchor tAlias = null;
        if (!dereferenceAliases) {
            tAlias = this.anchors.get(node);
        }
        if (!dereferenceAliases && this.serializedNodes.contains(node)) {
            this.emitable.emit(new AliasEvent(tAlias));
        } else {
            this.serializedNodes.add(node);
            switch (node.getNodeType()) {
                case SCALAR:
                    var scalarNode = (ScalarNode) node;
                    serializeComments(node.getBlockComments());
                    Tag detectedTag = settings.schema().getScalarResolver().resolve(scalarNode.getValue(), true);
                    Tag defaultTag = settings.schema().getScalarResolver().resolve(scalarNode.getValue(), false);
                    var tuple = ImplicitTuple.byValues(node.getTag().equals(detectedTag), node.getTag().equals(defaultTag));
                    var event = new ScalarEvent(tAlias, node.getTag().getValue(), tuple, scalarNode.getValue(), scalarNode.getScalarStyle());
                    this.emitable.emit(event);
                    serializeComments(node.getInLineComments());
                    serializeComments(node.getEndComments());
                    break;
                case SEQUENCE:
                    var seqNode = (SequenceNode) node;
                    serializeComments(node.getBlockComments());
                    boolean implicitS = node.getTag().equals(Tag.SEQ);
                    this.emitable.emit(new SequenceStartEvent(tAlias, node.getTag().getValue(), implicitS, seqNode.getFlowStyle()));
                    List<Node> nodes = seqNode.getValue();
                    for (Node nodeItem : nodes) {
                        serializeNode(nodeItem);
                    }
                    this.emitable.emit(new SequenceEndEvent());
                    serializeComments(node.getInLineComments());
                    serializeComments(node.getEndComments());
                    break;
                default:// instance of MappingNode
                    serializeComments(node.getBlockComments());
                    if (node.getTag() != Tag.COMMENT) {
                        boolean implicitM = node.getTag().equals(Tag.MAP);
                        var mappingNode = (MappingNode) node;
                        List<NodeTuple> nodeTuples = mappingNode.getValue();
                        if (this.dereferenceAliases && mappingNode.hasMergeTag()) {
                            nodeTuples = mergeUtils.flatten(mappingNode);
                        }
                        this.emitable.emit(new MappingStartEvent(tAlias, mappingNode.getTag().getValue(), implicitM, mappingNode.getFlowStyle(), null, null));
                        for (var nodeTuple : nodeTuples) {
                            serializeNode(nodeTuple.keyNode());
                            serializeNode(nodeTuple.valueNode());
                        }
                        this.emitable.emit(new MappingEndEvent());
                        serializeComments(node.getInLineComments());
                        serializeComments(node.getEndComments());
                    }
            }
        }
        recursive.remove(node);
    }

    private void serializeComments(List<CommentLine> comments) {
        if (settings.dumpComments() && comments != null) {
            for (CommentLine line : comments) {
                var commentEvent = new CommentEvent(line.commentType(), line.value(), line.startMark(), line.endMark());
                this.emitable.emit(commentEvent);
            }
        }
    }
}
