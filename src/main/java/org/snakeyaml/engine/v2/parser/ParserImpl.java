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
package org.snakeyaml.engine.v2.parser;

import org.jspecify.annotations.NonNull;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.common.ArrayStack;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
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
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.scanner.Scanner;
import org.snakeyaml.engine.v2.scanner.ScannerImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.snakeyaml.engine.v2.tokens.AliasToken;
import org.snakeyaml.engine.v2.tokens.AnchorToken;
import org.snakeyaml.engine.v2.tokens.BlockEntryToken;
import org.snakeyaml.engine.v2.tokens.CommentToken;
import org.snakeyaml.engine.v2.tokens.DirectiveToken;
import org.snakeyaml.engine.v2.tokens.ScalarToken;
import org.snakeyaml.engine.v2.tokens.StreamEndToken;
import org.snakeyaml.engine.v2.tokens.StreamStartToken;
import org.snakeyaml.engine.v2.tokens.TagToken;
import org.snakeyaml.engine.v2.tokens.TagTuple;
import org.snakeyaml.engine.v2.tokens.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * <pre>
 * # The following YAML grammar is LL(1) and is parsed by a recursive descent parser.
 *
 * stream            ::= STREAM-START implicit_document? explicit_document* STREAM-END
 * implicit_document ::= block_node DOCUMENT-END*
 * explicit_document ::= DIRECTIVE* DOCUMENT-START block_node? DOCUMENT-END*
 * block_node_or_indentless_sequence ::=
 *                       ALIAS
 *                       | properties (block_content | indentless_block_sequence)?
 *                       | block_content
 *                       | indentless_block_sequence
 * block_node        ::= ALIAS
 *                       | properties block_content?
 *                       | block_content
 * flow_node         ::= ALIAS
 *                       | properties flow_content?
 *                       | flow_content
 * properties        ::= TAG ANCHOR? | ANCHOR TAG?
 * block_content     ::= block_collection | flow_collection | SCALAR
 * flow_content      ::= flow_collection | SCALAR
 * block_collection  ::= block_sequence | block_mapping
 * flow_collection   ::= flow_sequence | flow_mapping
 * block_sequence    ::= BLOCK-SEQUENCE-START (BLOCK-ENTRY block_node?)* BLOCK-END
 * indentless_sequence   ::= (BLOCK-ENTRY block_node?)+
 * block_mapping     ::= BLOCK-MAPPING_START
 *                       ((KEY block_node_or_indentless_sequence?)?
 *                       (VALUE block_node_or_indentless_sequence?)?)*
 *                       BLOCK-END
 * flow_sequence     ::= FLOW-SEQUENCE-START
 *                       (flow_sequence_entry FLOW-ENTRY)*
 *                       flow_sequence_entry?
 *                       FLOW-SEQUENCE-END
 * flow_sequence_entry   ::= flow_node | KEY flow_node? (VALUE flow_node?)?
 * flow_mapping      ::= FLOW-MAPPING-START
 *                       (flow_mapping_entry FLOW-ENTRY)*
 *                       flow_mapping_entry?
 *                       FLOW-MAPPING-END
 * flow_mapping_entry    ::= flow_node | KEY flow_node? (VALUE flow_node?)?
 * #
 * FIRST sets:
 * #
 * stream: { STREAM-START }
 * explicit_document: { DIRECTIVE DOCUMENT-START }
 * implicit_document: FIRST(block_node)
 * block_node: { ALIAS TAG ANCHOR SCALAR BLOCK-SEQUENCE-START BLOCK-MAPPING-START FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * flow_node: { ALIAS ANCHOR TAG SCALAR FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * block_content: { BLOCK-SEQUENCE-START BLOCK-MAPPING-START FLOW-SEQUENCE-START FLOW-MAPPING-START SCALAR }
 * flow_content: { FLOW-SEQUENCE-START FLOW-MAPPING-START SCALAR }
 * block_collection: { BLOCK-SEQUENCE-START BLOCK-MAPPING-START }
 * flow_collection: { FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * block_sequence: { BLOCK-SEQUENCE-START }
 * block_mapping: { BLOCK-MAPPING-START }
 * block_node_or_indentless_sequence: { ALIAS ANCHOR TAG SCALAR BLOCK-SEQUENCE-START BLOCK-MAPPING-START FLOW-SEQUENCE-START FLOW-MAPPING-START BLOCK-ENTRY }
 * indentless_sequence: { ENTRY }
 * flow_collection: { FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * flow_sequence: { FLOW-SEQUENCE-START }
 * flow_mapping: { FLOW-MAPPING-START }
 * flow_sequence_entry: { ALIAS ANCHOR TAG SCALAR FLOW-SEQUENCE-START FLOW-MAPPING-START KEY }
 * flow_mapping_entry: { ALIAS ANCHOR TAG SCALAR FLOW-SEQUENCE-START FLOW-MAPPING-START KEY }
 * </pre>
 * <p>
 * Since writing a recursive-descendant parser is a straightforward task, we give few comments here.
 */
public class ParserImpl implements Parser {

    private static final Map<String, String> DEFAULT_TAGS = Map.of(
        "!", "!",
        "!!", Tag.PREFIX
    );

    /**
     * tokeniser
     */
    protected final Scanner scanner;
    private final LoadSettings settings;
    private final ArrayStack<Production> states;
    private final ArrayStack<Mark> marksStack;
    private Event currentEvent; // parsed event
    private Production state;
    private Map<String, String> directiveTags;

    /**
     * Create
     *
     * @param settings - configuration options
     * @param reader - the input
     */
    public ParserImpl(@NonNull LoadSettings settings, @NonNull StreamReader reader) {
        this(settings, new ScannerImpl(settings, reader));
    }

    /**
     * Create
     *
     * @param settings - configuration options
     * @param scanner - input
     */
    public ParserImpl(@NonNull LoadSettings settings, @NonNull Scanner scanner) {
        this.scanner = scanner;
        this.settings = settings;
        currentEvent = null;
        directiveTags = new HashMap<>(DEFAULT_TAGS);
        states = new ArrayStack<>(100);
        marksStack = new ArrayStack<>(10);
        state = new ParseStreamStart(); // prepare the next state
    }

    /**
     * Check the ID of the next event.
     */
    @Override
    public boolean checkEvent(Event.Id id) {
        peekEvent();
        return currentEvent != null && currentEvent.getEventId() == id;
    }

    /**
     * Get the next event (and keep it). Produce the event if not yet present.
     */
    @Override
    public Event peekEvent() {
        produce();
        if (currentEvent == null) {
            throw new NoSuchElementException("No more Events found.");
        }
        return currentEvent;
    }

    /**
     * Consume the event (get the next event and removed it).
     */
    @Override
    public Event next() {
        Event value = peekEvent();
        currentEvent = null;
        return value;
    }

    /**
     * Produce the event if not yet present.
     *
     * @return true if there is another event
     */
    @Override
    public boolean hasNext() {
        produce();
        return currentEvent != null;
    }

    private void produce() {
        if (currentEvent == null && state != null) {
            currentEvent = state.produce();
        }
    }

    private CommentEvent produceCommentEvent(CommentToken token) {
        String value = token.getValue();
        CommentType type = token.getCommentType();

        // state = state, that no change in state
        return new CommentEvent(type, value, token.getStartMark(), token.getEndMark());
    }

    @SuppressWarnings("unchecked")
    private VersionTagsTuple processDirectives() {
        SpecVersion yamlSpecVersion = null;
        var tagHandles = new HashMap<String, String>();
        while (scanner.checkToken(Token.Id.Directive)) {
            @SuppressWarnings("rawtypes")
            var token = (DirectiveToken) scanner.next();
            List<?> dirOption = token.getValue();
            if (dirOption != null) {
                // the value must be present
                if (token.getName().equals(DirectiveToken.YAML_DIRECTIVE)) {
                    if (yamlSpecVersion != null) {
                        throw new ParserException("Found duplicate YAML directive", token.getStartMark());
                    }
                    var value = (List<Integer>) dirOption;
                    yamlSpecVersion = settings.versionFunction().apply(SpecVersion.findVersion(value.get(0), value.get(1)));
                } else if (token.getName().equals(DirectiveToken.TAG_DIRECTIVE)) {
                    var value = (List<String>) dirOption;
                    String handle = value.get(0);
                    String prefix = value.get(1);
                    if (tagHandles.containsKey(handle)) {
                        throw new ParserException("Duplicate tag handle " + handle, token.getStartMark());
                    }
                    tagHandles.put(handle, prefix);
                }
            }
        }
        var detectedTagHandles = new HashMap<String, String>();
        if (!tagHandles.isEmpty()) {
            // copy from tagHandles
            detectedTagHandles.putAll(tagHandles);
        }
        for (var entry : DEFAULT_TAGS.entrySet()) {
            // do not overwrite re-defined tags
            if (!tagHandles.containsKey(entry.getKey())) {
                tagHandles.put(entry.getKey(), entry.getValue());
            }
        }
        directiveTags = tagHandles;
        // data for the event (no default tags added)
        return new VersionTagsTuple(yamlSpecVersion != null ? yamlSpecVersion : SpecVersion.V_1_2, detectedTagHandles);
    }

    private Event parseFlowNode() {
        return parseNode(false, false);
    }

    private Event parseBlockNodeOrIndentlessSequence() {
        return parseNode(true, true);
    }

    /**
     * Check if the next token is a content token that would start a node. Used to determine if
     * comments after anchor/tag should be emitted separately (content follows) or are inline comments
     * (no content follows, empty scalar).
     */
    private boolean hasNodeContent(boolean block, boolean indentlessSequence) {
        if (indentlessSequence && scanner.checkToken(Token.Id.BlockEntry)) {
            return true;
        }
        if (scanner.checkToken(Token.Id.Scalar, Token.Id.FlowSequenceStart,
            Token.Id.FlowMappingStart)) {
            return true;
        }
        return block && scanner.checkToken(Token.Id.BlockSequenceStart, Token.Id.BlockMappingStart);
    }

    private Event parseNode(boolean block, boolean indentlessSequence) {
        Event event;
        Mark startMark = null;
        Mark endMark = null;
        Mark tagMark = null;
        if (scanner.checkToken(Token.Id.Alias)) {
            var token = (AliasToken) scanner.next();
            event = new AliasEvent(token.getValue(), token.getStartMark(), token.getEndMark());
            state = states.pop();
        } else {
            Anchor anchor = null;
            TagTuple tagTupleValue = null;
            if (scanner.checkToken(Token.Id.Anchor)) {
                var token = (AnchorToken) scanner.next();
                startMark = token.getStartMark();
                endMark = token.getEndMark();
                anchor = token.getValue();
                if (scanner.checkToken(Token.Id.Tag)) {
                    var tagToken = (TagToken) scanner.next();
                    tagMark = tagToken.getStartMark();
                    endMark = tagToken.getEndMark();
                    tagTupleValue = tagToken.getValue();
                }
            } else if (scanner.checkToken(Token.Id.Tag)) {
                var tagToken = (TagToken) scanner.next();
                startMark = tagToken.getStartMark();
                tagMark = startMark;
                endMark = tagToken.getEndMark();
                tagTupleValue = tagToken.getValue();
                if (scanner.checkToken(Token.Id.Anchor)) {
                    var token = (AnchorToken) scanner.next();
                    endMark = token.getEndMark();
                    anchor = token.getValue();
                }
            }
            String tag = null;
            if (tagTupleValue != null) {
                String handle = tagTupleValue.handle();
                String suffix = tagTupleValue.suffix();
                if (!directiveTags.containsKey(handle)) {
                    throw new ParserException("while parsing a node", startMark, "found undefined tag handle " + handle, tagMark);
                }
                tag = directiveTags.get(handle) + suffix;
            }
            if (startMark == null) {
                startMark = scanner.peekToken().getStartMark();
                endMark = startMark;
            }
            // Handle comments that appear after properties (anchor/tag) but before node content.
            // Only consume and emit comments if actual content follows them; otherwise, they are
            // inline comments that should be handled by the existing flow.
            if ((anchor != null || tag != null) && scanner.checkToken(Token.Id.Comment)) {
                // Peek ahead to see if there's content after any comments
                var commentTokensAfterProperties = new ArrayList<CommentToken>();
                while (scanner.checkToken(Token.Id.Comment)) {
                    commentTokensAfterProperties.add((CommentToken) scanner.next());
                }
                // Check if there's actual content after the comments
                if (hasNodeContent(block, indentlessSequence)) {
                    // Content follows - emit comments first, then parse content
                    state = new ParseNodeWithPendingComments(block, indentlessSequence, anchor, tag, startMark, endMark, tagMark, commentTokensAfterProperties, states.pop());
                    return produceCommentEvent(commentTokensAfterProperties.remove(0));
                }
                // No content follows - this is an empty scalar case.
                // Create the scalar event and set up state to emit DocumentEnd, then the comments.
                boolean implicit = tag == null;
                var scalarEvent = new ScalarEvent(anchor, tag, ImplicitTuple.byValues(implicit, false), "", ScalarStyle.PLAIN, startMark, endMark);
                // Pop states to maintain stack consistency (normally ParseDocumentEnd would be popped)
                states.pop();
                // The next state should emit DocumentEnd, then the collected comments, then continue
                state = new ParseDocumentEndThenComments(commentTokensAfterProperties);
                return scalarEvent;
            }
            boolean implicit = tag == null;
            if (indentlessSequence && scanner.checkToken(Token.Id.BlockEntry)) {
                endMark = scanner.peekToken().getEndMark();
                event = new SequenceStartEvent(anchor, tag, implicit, FlowStyle.BLOCK, startMark, endMark);
                state = new ParseIndentlessSequenceEntryKey();
            } else {
                if (scanner.checkToken(Token.Id.Scalar)) {
                    var token = (ScalarToken) scanner.next();
                    endMark = token.getEndMark();
                    ImplicitTuple implicitValues;
                    if (token.isPlain() && tag == null) {
                        implicitValues = ImplicitTuple.TRUE_FALSE;
                    } else if (tag == null) {
                        implicitValues = ImplicitTuple.FALSE_TRUE;
                    } else {
                        implicitValues = ImplicitTuple.FALSE_FALSE;
                    }
                    event = new ScalarEvent(anchor, tag, implicitValues, token.getValue(), token.getStyle(), startMark, endMark);
                    state = states.pop();
                } else if (scanner.checkToken(Token.Id.FlowSequenceStart)) {
                    endMark = scanner.peekToken().getEndMark();
                    event = new SequenceStartEvent(anchor, tag, implicit, FlowStyle.FLOW, startMark, endMark);
                    state = new ParseFlowSequenceFirstEntry();
                } else if (scanner.checkToken(Token.Id.FlowMappingStart)) {
                    endMark = scanner.peekToken().getEndMark();
                    event = new MappingStartEvent(anchor, tag, implicit, FlowStyle.FLOW, startMark, endMark);
                    state = new ParseFlowMappingFirstKey();
                } else if (block && scanner.checkToken(Token.Id.BlockSequenceStart)) {
                    endMark = scanner.peekToken().getStartMark();
                    event = new SequenceStartEvent(anchor, tag, implicit, FlowStyle.BLOCK, startMark, endMark);
                    state = new ParseBlockSequenceFirstEntry();
                } else if (block && scanner.checkToken(Token.Id.BlockMappingStart)) {
                    endMark = scanner.peekToken().getStartMark();
                    event = new MappingStartEvent(anchor, tag, implicit, FlowStyle.BLOCK, startMark, endMark);
                    state = new ParseBlockMappingFirstKey();
                } else if (anchor != null || tag != null) {
                    // Empty scalars are allowed even if a tag or an anchor is specified.
                    event = new ScalarEvent(anchor, tag, ImplicitTuple.byValues(implicit, false), "", ScalarStyle.PLAIN, startMark, endMark);
                    state = states.pop();
                } else {
                    Token token = scanner.peekToken();
                    throw new ParserException("while parsing a " + (block ? "block" : "flow") + " node", startMark, "expected the node content, but found '" + token.getTokenId() + "'", token.getStartMark());
                }
            }
        }
        return event;
    }

    /**
     * <pre>
     * block_mapping     ::= BLOCK-MAPPING_START
     *           ((KEY block_node_or_indentless_sequence?)?
     *           (VALUE block_node_or_indentless_sequence?)?)*
     *           BLOCK-END
     * </pre>
     */
    private Event processEmptyScalar(Mark mark) {
        return new ScalarEvent(null, null, ImplicitTuple.TRUE_FALSE, "", ScalarStyle.PLAIN, mark, mark);
    }

    private Mark markPop() {
        return marksStack.pop();
    }

    private void markPush(Mark mark) {
        marksStack.push(mark);
    }

    private class ParseStreamStart implements Production {

        @Override
        public Event produce() {
            // Parse the stream start.
            StreamStartToken token = (StreamStartToken) scanner.next();
            Event event = new StreamStartEvent(token.getStartMark(), token.getEndMark());
            // Prepare the next state.
            state = new ParseImplicitDocumentStart();
            return event;
        }
    }

    private class ParseImplicitDocumentStart implements Production {

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Comment)) {
                state = new ParseImplicitDocumentStart();
                return produceCommentEvent((CommentToken) scanner.next());
            }
            if (scanner.checkToken(Token.Id.Directive, Token.Id.DocumentStart, Token.Id.StreamEnd)) {
                // explicit document detected
                return new ParseDocumentStart().produce();
            }
            // Parse an implicit document.
            Token token = scanner.peekToken();
            Mark startMark = token.getStartMark();
            Event event = new DocumentStartEvent(false, SpecVersion.V_1_2, Collections.emptyMap(), startMark, startMark);
            // Prepare the next state.
            states.push(new ParseDocumentEnd());
            state = new ParseBlockNode();
            return event;
        }
    }

    private class ParseDocumentStart implements Production {

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Comment)) {
                state = new ParseDocumentStart();
                return produceCommentEvent((CommentToken) scanner.next());
            }
            // Parse any extra document end indicators.
            while (scanner.checkToken(Token.Id.DocumentEnd)) {
                scanner.next();
            }
            if (scanner.checkToken(Token.Id.Comment)) {
                state = new ParseDocumentStart();
                return produceCommentEvent((CommentToken) scanner.next());
            }
            // Parse an explicit document.
            Event event;
            if (!scanner.checkToken(Token.Id.StreamEnd)) {
                scanner.resetDocumentIndex();
                Token token = scanner.peekToken();
                Mark startMark = token.getStartMark();
                VersionTagsTuple tuple = processDirectives();
                while (scanner.checkToken(Token.Id.Comment)) {
                    // the comments in the directive are ignored because they are not part of the Node tree
                    scanner.next();
                }
                if (!scanner.checkToken(Token.Id.StreamEnd)) {
                    if (!scanner.checkToken(Token.Id.DocumentStart)) {
                        throw new ParserException("expected '<document start>', but found '" + scanner.peekToken().getTokenId() + "'", scanner.peekToken().getStartMark());
                    }
                    token = scanner.next();
                    Mark endMark = token.getEndMark();
                    event = new DocumentStartEvent(true, tuple.specVersion(), tuple.tags(), startMark,
                        endMark);
                    states.push(new ParseDocumentEnd());
                    state = new ParseDocumentContent();
                    return event;
                } else {
                    throw new ParserException("expected '<document start>', but found '" + scanner.peekToken().getTokenId() + "'", scanner.peekToken().getStartMark());
                }
            }
            // Parse the end of the stream.
            var token = (StreamEndToken) scanner.next();
            event = new StreamEndEvent(token.getStartMark(), token.getEndMark());
            if (!states.isEmpty()) {
                throw new YamlEngineException("Unexpected end of stream. States left: " + states);
            }
            if (!markEmpty()) {
                throw new YamlEngineException("Unexpected end of stream. Marks left: " + marksStack);
            }
            state = null;
            return event;
        }

        private boolean markEmpty() {
            return marksStack.isEmpty();
        }
    }

    // block_sequence ::= BLOCK-SEQUENCE-START (BLOCK-ENTRY block_node?)*
    // BLOCK-END

    private class ParseDocumentEnd implements Production {

        @Override
        public Event produce() {
            // Parse the document end.
            Token token = scanner.peekToken();
            Mark startMark = token.getStartMark();
            Mark endMark = startMark;
            boolean explicit = false;
            if (scanner.checkToken(Token.Id.DocumentEnd)) {
                token = scanner.next();
                endMark = token.getEndMark();
                explicit = true;
            } else if (scanner.checkToken(Token.Id.Directive)) {
                throw new ParserException("expected '<document end>' before directives, but found '" + scanner.peekToken().getTokenId() + "'", scanner.peekToken().getStartMark());
            }
            directiveTags.clear(); // directive tags do not survive between the documents
            var event = new DocumentEndEvent(explicit, startMark, endMark);
            // Prepare the next state.
            state = new ParseDocumentStart();
            return event;
        }
    }

    private class ParseDocumentContent implements Production {

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Comment)) {
                state = new ParseDocumentContent();
                return produceCommentEvent((CommentToken) scanner.next());
            }
            if (scanner.checkToken(Token.Id.Directive, Token.Id.DocumentStart, Token.Id.DocumentEnd,
                Token.Id.StreamEnd)) {
                Event event = processEmptyScalar(scanner.peekToken().getStartMark());
                state = states.pop();
                return event;
            } else {
                return new ParseBlockNode().produce();
            }
        }
    }

    /**
     * <pre>
     *  block_node_or_indentless_sequence ::= ALIAS
     *                | properties (block_content | indentless_block_sequence)?
     *                | block_content
     *                | indentless_block_sequence
     *  block_node    ::= ALIAS
     *                    | properties block_content?
     *                    | block_content
     *  flow_node     ::= ALIAS
     *                    | properties flow_content?
     *                    | flow_content
     *  properties    ::= TAG ANCHOR? | ANCHOR TAG?
     *  block_content     ::= block_collection | flow_collection | SCALAR
     *  flow_content      ::= flow_collection | SCALAR
     *  block_collection  ::= block_sequence | block_mapping
     *  flow_collection   ::= flow_sequence | flow_mapping
     * </pre>
     */

    private class ParseBlockNode implements Production {

        @Override
        public Event produce() {
            return parseNode(true, false);
        }
    }

    // indentless_sequence ::= (BLOCK-ENTRY block_node?)+

    private class ParseBlockSequenceFirstEntry implements Production {

        @Override
        public Event produce() {
            Token token = scanner.next();
            markPush(token.getStartMark());
            return new ParseBlockSequenceEntryKey().produce();
        }
    }

    private class ParseBlockSequenceEntryKey implements Production {

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Comment)) {
                state = new ParseBlockSequenceEntryKey();
                return produceCommentEvent((CommentToken) scanner.next());
            }
            if (scanner.checkToken(Token.Id.BlockEntry)) {
                BlockEntryToken token = (BlockEntryToken) scanner.next();
                return new ParseBlockSequenceEntryValue(token).produce();
            }
            if (!scanner.checkToken(Token.Id.BlockEnd)) {
                Token token = scanner.peekToken();
                throw new ParserException("while parsing a block collection", markPop(),
                    "expected <block end>, but found '" + token.getTokenId() + "'", token.getStartMark());
            }
            Token token = scanner.next();
            Event event = new SequenceEndEvent(token.getStartMark(), token.getEndMark());
            state = states.pop();
            markPop();
            return event;
        }
    }

    private class ParseBlockSequenceEntryValue implements Production {

        BlockEntryToken token;

        public ParseBlockSequenceEntryValue(final BlockEntryToken token) {
            this.token = token;
        }

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Comment)) {
                state = new ParseBlockSequenceEntryValue(token);
                return produceCommentEvent((CommentToken) scanner.next());
            }
            if (!scanner.checkToken(Token.Id.BlockEntry, Token.Id.BlockEnd)) {
                states.push(new ParseBlockSequenceEntryKey());
                return new ParseBlockNode().produce();
            } else {
                state = new ParseBlockSequenceEntryKey();
                return processEmptyScalar(token.getEndMark());
            }
        }
    }

    private class ParseIndentlessSequenceEntryKey implements Production {

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Comment)) {
                state = new ParseIndentlessSequenceEntryKey();
                return produceCommentEvent((CommentToken) scanner.next());
            }
            if (scanner.checkToken(Token.Id.BlockEntry)) {
                BlockEntryToken token = (BlockEntryToken) scanner.next();
                return new ParseIndentlessSequenceEntryValue(token).produce();
            }
            Token token = scanner.peekToken();
            Event event = new SequenceEndEvent(token.getStartMark(), token.getEndMark());
            state = states.pop();
            return event;
        }
    }

    private class ParseIndentlessSequenceEntryValue implements Production {

        BlockEntryToken token;

        public ParseIndentlessSequenceEntryValue(final BlockEntryToken token) {
            this.token = token;
        }

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Comment)) {
                state = new ParseIndentlessSequenceEntryValue(token);
                return produceCommentEvent((CommentToken) scanner.next());
            }
            if (!scanner.checkToken(Token.Id.BlockEntry, Token.Id.Key, Token.Id.Value,
                Token.Id.BlockEnd)) {
                states.push(new ParseIndentlessSequenceEntryKey());
                return new ParseBlockNode().produce();
            } else {
                state = new ParseIndentlessSequenceEntryKey();
                return processEmptyScalar(token.getEndMark());
            }
        }
    }

    private class ParseBlockMappingFirstKey implements Production {

        @Override
        public Event produce() {
            Token token = scanner.next();
            markPush(token.getStartMark());
            return new ParseBlockMappingKey().produce();
        }
    }

    private class ParseBlockMappingKey implements Production {

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Comment)) {
                state = new ParseBlockMappingKey();
                return produceCommentEvent((CommentToken) scanner.next());
            }
            if (scanner.checkToken(Token.Id.Key)) {
                Token token = scanner.next();
                if (!scanner.checkToken(Token.Id.Key, Token.Id.Value, Token.Id.BlockEnd)) {
                    states.push(new ParseBlockMappingValue());
                    return parseBlockNodeOrIndentlessSequence();
                } else {
                    state = new ParseBlockMappingValue();
                    return processEmptyScalar(token.getEndMark());
                }
            }
            if (!scanner.checkToken(Token.Id.BlockEnd)) {
                Token token = scanner.peekToken();
                throw new ParserException("while parsing a block mapping", markPop(),
                    "expected <block end>, but found '" + token.getTokenId() + "'", token.getStartMark());
            }
            Token token = scanner.next();
            Event event = new MappingEndEvent(token.getStartMark(), token.getEndMark());
            state = states.pop();
            markPop();
            return event;
        }
    }

    private class ParseBlockMappingValue implements Production {

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Value)) {
                Token token = scanner.next();
                if (scanner.checkToken(Token.Id.Comment)) {
                    var p = new ParseBlockMappingValueComment();
                    state = p;
                    return p.produce();
                } else if (!scanner.checkToken(Token.Id.Key, Token.Id.Value, Token.Id.BlockEnd)) {
                    states.push(new ParseBlockMappingKey());
                    return parseBlockNodeOrIndentlessSequence();
                } else {
                    state = new ParseBlockMappingKey();
                    return processEmptyScalar(token.getEndMark());
                }
            } else if (scanner.checkToken(Token.Id.Scalar)) {
                states.push(new ParseBlockMappingKey());
                return parseBlockNodeOrIndentlessSequence();
            }
            state = new ParseBlockMappingKey();
            Token token = scanner.peekToken();
            return processEmptyScalar(token.getStartMark());
        }
    }

    private class ParseBlockMappingValueComment implements Production {

        List<CommentToken> tokens = new LinkedList<>();

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Comment)) {
                tokens.add((CommentToken) scanner.next());
                return produce();
            } else if (!scanner.checkToken(Token.Id.Key, Token.Id.Value, Token.Id.BlockEnd)) {
                if (!tokens.isEmpty()) {
                    return produceCommentEvent(tokens.remove(0));
                }
                states.push(new ParseBlockMappingKey());
                return parseBlockNodeOrIndentlessSequence();
            } else {
                state = new ParseBlockMappingValueCommentList(tokens);
                return processEmptyScalar(scanner.peekToken().getStartMark());
            }
        }
    }

    private class ParseBlockMappingValueCommentList implements Production {

        List<CommentToken> tokens;

        public ParseBlockMappingValueCommentList(final List<CommentToken> tokens) {
            this.tokens = tokens;
        }

        @Override
        public Event produce() {
            if (!tokens.isEmpty()) {
                return produceCommentEvent(tokens.remove(0));
            }
            return new ParseBlockMappingKey().produce();
        }
    }

    /**
     * <pre>
     * flow_sequence     ::= FLOW-SEQUENCE-START
     *                       (flow_sequence_entry FLOW-ENTRY)*
     *                       flow_sequence_entry?
     *                       FLOW-SEQUENCE-END
     * flow_sequence_entry   ::= flow_node | KEY flow_node? (VALUE flow_node?)?
     * Note that while production rules for both flow_sequence_entry and
     * flow_mapping_entry are equal, their interpretations are different.
     * For `flow_sequence_entry`, the part `KEY flow_node? (VALUE flow_node?)?`
     * generate an inline mapping (set syntax).
     * </pre>
     */
    private class ParseFlowSequenceFirstEntry implements Production {

        @Override
        public Event produce() {
            Token token = scanner.next();
            markPush(token.getStartMark());
            return new ParseFlowSequenceEntry(true).produce();
        }
    }

    private class ParseFlowSequenceEntry implements Production {

        private final boolean first;

        public ParseFlowSequenceEntry(boolean first) {
            this.first = first;
        }

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Comment)) {
                state = new ParseFlowSequenceEntry(first);
                return produceCommentEvent((CommentToken) scanner.next());
            }
            if (!scanner.checkToken(Token.Id.FlowSequenceEnd)) {
                if (!first) {
                    if (scanner.checkToken(Token.Id.FlowEntry)) {
                        scanner.next();
                        if (scanner.checkToken(Token.Id.Comment)) {
                            state = new ParseFlowSequenceEntry(true);
                            return produceCommentEvent((CommentToken) scanner.next());
                        }
                    } else {
                        Token token = scanner.peekToken();
                        throw new ParserException("while parsing a flow sequence", markPop(),
                            "expected ',' or ']', but got " + token.getTokenId(), token.getStartMark());
                    }
                }
                if (scanner.checkToken(Token.Id.Key)) {
                    Token token = scanner.peekToken();
                    var event = new MappingStartEvent(null, null, true, FlowStyle.FLOW, token.getStartMark(),
                        token.getEndMark());
                    state = new ParseFlowSequenceEntryMappingKey();
                    return event;
                } else if (!scanner.checkToken(Token.Id.FlowSequenceEnd)) {
                    states.push(new ParseFlowSequenceEntry(false));
                    return parseFlowNode();
                }
            }
            Token token = scanner.next();
            Event event = new SequenceEndEvent(token.getStartMark(), token.getEndMark());
            if (!scanner.checkToken(Token.Id.Comment)) {
                state = states.pop();
            } else {
                state = new ParseFlowEndComment();
            }
            markPop();
            return event;
        }
    }

    private class ParseFlowEndComment implements Production {

        @Override
        public Event produce() {
            Event event = produceCommentEvent((CommentToken) scanner.next());
            if (!scanner.checkToken(Token.Id.Comment)) {
                state = states.pop();
            }
            return event;
        }
    }

    private class ParseFlowSequenceEntryMappingKey implements Production {

        @Override
        public Event produce() {
            Token token = scanner.next();
            if (!scanner.checkToken(Token.Id.Value, Token.Id.FlowEntry, Token.Id.FlowSequenceEnd)) {
                states.push(new ParseFlowSequenceEntryMappingValue());
                return parseFlowNode();
            } else {
                state = new ParseFlowSequenceEntryMappingValue();
                return processEmptyScalar(token.getEndMark());
            }
        }
    }

    private class ParseFlowSequenceEntryMappingValue implements Production {

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Value)) {
                Token token = scanner.next();
                if (!scanner.checkToken(Token.Id.FlowEntry, Token.Id.FlowSequenceEnd)) {
                    states.push(new ParseFlowSequenceEntryMappingEnd());
                    return parseFlowNode();
                } else {
                    state = new ParseFlowSequenceEntryMappingEnd();
                    return processEmptyScalar(token.getEndMark());
                }
            } else {
                state = new ParseFlowSequenceEntryMappingEnd();
                Token token = scanner.peekToken();
                return processEmptyScalar(token.getStartMark());
            }
        }
    }

    private class ParseFlowSequenceEntryMappingEnd implements Production {

        @Override
        public Event produce() {
            state = new ParseFlowSequenceEntry(false);
            Token token = scanner.peekToken();
            return new MappingEndEvent(token.getStartMark(), token.getEndMark());
        }
    }

    /**
     * <pre>
     *   flow_mapping  ::= FLOW-MAPPING-START
     *          (flow_mapping_entry FLOW-ENTRY)*
     *          flow_mapping_entry?
     *          FLOW-MAPPING-END
     *   flow_mapping_entry    ::= flow_node | KEY flow_node? (VALUE flow_node?)?
     * </pre>
     */
    private class ParseFlowMappingFirstKey implements Production {

        @Override
        public Event produce() {
            Token token = scanner.next();
            markPush(token.getStartMark());
            return new ParseFlowMappingKey(true).produce();
        }
    }

    private class ParseFlowMappingKey implements Production {

        private final boolean first;

        public ParseFlowMappingKey(boolean first) {
            this.first = first;
        }

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Comment)) {
                state = new ParseFlowMappingKey(first);
                return produceCommentEvent((CommentToken) scanner.next());
            }
            if (!scanner.checkToken(Token.Id.FlowMappingEnd)) {
                if (!first) {
                    if (scanner.checkToken(Token.Id.FlowEntry)) {
                        scanner.next();
                        if (scanner.checkToken(Token.Id.Comment)) {
                            state = new ParseFlowMappingKey(true);
                            return produceCommentEvent((CommentToken) scanner.next());
                        }
                    } else {
                        Token token = scanner.peekToken();
                        throw new ParserException("while parsing a flow mapping", markPop(),
                            "expected ',' or '}', but got " + token.getTokenId(), token.getStartMark());
                    }
                }
                if (scanner.checkToken(Token.Id.Key)) {
                    Token token = scanner.next();
                    if (!scanner.checkToken(Token.Id.Value, Token.Id.FlowEntry, Token.Id.FlowMappingEnd)) {
                        states.push(new ParseFlowMappingValue());
                        return parseFlowNode();
                    } else {
                        state = new ParseFlowMappingValue();
                        return processEmptyScalar(token.getEndMark());
                    }
                } else if (!scanner.checkToken(Token.Id.FlowMappingEnd)) {
                    states.push(new ParseFlowMappingEmptyValue());
                    return parseFlowNode();
                }
            }
            Token token = scanner.next();
            Event event = new MappingEndEvent(token.getStartMark(), token.getEndMark());
            markPop();
            if (!scanner.checkToken(Token.Id.Comment)) {
                state = states.pop();
            } else {
                state = new ParseFlowEndComment();
            }
            return event;
        }
    }

    private class ParseFlowMappingValue implements Production {

        @Override
        public Event produce() {
            if (scanner.checkToken(Token.Id.Value)) {
                Token token = scanner.next();
                if (!scanner.checkToken(Token.Id.FlowEntry, Token.Id.FlowMappingEnd)) {
                    states.push(new ParseFlowMappingKey(false));
                    return parseFlowNode();
                } else {
                    state = new ParseFlowMappingKey(false);
                    return processEmptyScalar(token.getEndMark());
                }
            } else {
                state = new ParseFlowMappingKey(false);
                Token token = scanner.peekToken();
                return processEmptyScalar(token.getStartMark());
            }
        }
    }

    private class ParseFlowMappingEmptyValue implements Production {

        @Override
        public Event produce() {
            state = new ParseFlowMappingKey(false);
            return processEmptyScalar(scanner.peekToken().getStartMark());
        }
    }

    /**
     * Production that emits pending comment events collected after anchor/tag, then parses node
     * content.
     */
    private class ParseNodeWithPendingComments implements Production {

        private final boolean block;
        private final boolean indentlessSequence;
        private final Anchor anchor;
        private final String tag;
        private final Mark startMark;
        private final Mark endMark;
        private final Mark tagMark;
        private final List<CommentToken> pendingComments;
        private final Production nextState;

        public ParseNodeWithPendingComments(boolean block, boolean indentlessSequence, Anchor anchor,
                                            String tag, Mark startMark, Mark endMark, Mark tagMark, List<CommentToken> pendingComments,
                                            Production nextState) {
            this.block = block;
            this.indentlessSequence = indentlessSequence;
            this.anchor = anchor;
            this.tag = tag;
            this.startMark = startMark;
            this.endMark = endMark;
            this.tagMark = tagMark;
            this.pendingComments = pendingComments;
            this.nextState = nextState;
        }

        @Override
        public Event produce() {
            if (!pendingComments.isEmpty()) {
                state = this;
                return produceCommentEvent(pendingComments.remove(0));
            }
            // All comments emitted, now parse the actual node content
            state = new ParseNodeContent(block, indentlessSequence, anchor, tag, startMark, endMark,
                tagMark, nextState);
            return state.produce();
        }
    }

    /**
     * Production that parses node content after anchor/tag and any comments have been processed.
     */
    private class ParseNodeContent implements Production {

        private final boolean block;
        private final boolean indentlessSequence;
        private final Anchor anchor;
        private final String tag;
        private Mark startMark;
        private Mark endMark;
        private final Mark tagMark;
        private final Production nextState;

        public ParseNodeContent(boolean block, boolean indentlessSequence, Anchor anchor, String tag,
                                Mark startMark, Mark endMark, Mark tagMark, Production nextState) {
            this.block = block;
            this.indentlessSequence = indentlessSequence;
            this.anchor = anchor;
            this.tag = tag;
            this.startMark = startMark;
            this.endMark = endMark;
            this.tagMark = tagMark;
            this.nextState = nextState;
        }

        @Override
        public Event produce() {
            Event event;
            // Update marks if they weren't set (no anchor/tag was present)
            if (startMark == null) {
                startMark = scanner.peekToken().getStartMark();
                endMark = startMark;
            }
            boolean implicit = tag.isEmpty();
            if (indentlessSequence && scanner.checkToken(Token.Id.BlockEntry)) {
                endMark = scanner.peekToken().getEndMark();
                event = new SequenceStartEvent(anchor, tag, implicit, FlowStyle.BLOCK, startMark, endMark);
                states.push(nextState);
                state = new ParseIndentlessSequenceEntryKey();
            } else if (scanner.checkToken(Token.Id.Scalar)) {
                var token = (ScalarToken) scanner.next();
                endMark = token.getEndMark();
                ImplicitTuple implicitValues;
                if ((token.isPlain() && tag.isEmpty())) {
                    implicitValues = ImplicitTuple.TRUE_FALSE;
                } else if (tag.isEmpty()) {
                    implicitValues = ImplicitTuple.FALSE_TRUE;
                } else {
                    implicitValues = ImplicitTuple.FALSE_FALSE;
                }
                event = new ScalarEvent(anchor, tag, implicitValues, token.getValue(), token.getStyle(), startMark, endMark);
                state = nextState;
            } else if (scanner.checkToken(Token.Id.FlowSequenceStart)) {
                endMark = scanner.peekToken().getEndMark();
                event = new SequenceStartEvent(anchor, tag, implicit, FlowStyle.FLOW, startMark, endMark);
                states.push(nextState);
                state = new ParseFlowSequenceFirstEntry();
            } else if (scanner.checkToken(Token.Id.FlowMappingStart)) {
                endMark = scanner.peekToken().getEndMark();
                event = new MappingStartEvent(anchor, tag, implicit, FlowStyle.FLOW, startMark, endMark);
                states.push(nextState);
                state = new ParseFlowMappingFirstKey();
            } else if (block && scanner.checkToken(Token.Id.BlockSequenceStart)) {
                endMark = scanner.peekToken().getStartMark();
                event = new SequenceStartEvent(anchor, tag, implicit, FlowStyle.BLOCK, startMark, endMark);
                states.push(nextState);
                state = new ParseBlockSequenceFirstEntry();
            } else if (block && scanner.checkToken(Token.Id.BlockMappingStart)) {
                endMark = scanner.peekToken().getStartMark();
                event = new MappingStartEvent(anchor, tag, implicit, FlowStyle.BLOCK, startMark, endMark);
                states.push(nextState);
                state = new ParseBlockMappingFirstKey();
            } else if (anchor != null || tag != null) {
                // Empty scalars are allowed even if a tag or an anchor is specified.
                event = new ScalarEvent(anchor, tag, ImplicitTuple.byValues(implicit, false), "",
                    ScalarStyle.PLAIN, startMark, endMark);
                state = nextState;
            } else {
                Token token = scanner.peekToken();
                throw new ParserException("while parsing a " + (block ? "block" : "flow") + " node",
                    startMark, "expected the node content, but found '" + token.getTokenId() + "'",
                    token.getStartMark());
            }
            return event;
        }
    }

    /**
     * Production that emits DocumentEnd event, then emits any pending comments that were collected
     * after an empty scalar. This ensures comments appear after DocumentEnd in the event stream,
     * which is where the Composer expects to find inline comments for the root document node.
     */
    private class ParseDocumentEndThenComments implements Production {

        private final List<CommentToken> pendingComments;
        private boolean documentEndEmitted = false;

        public ParseDocumentEndThenComments(List<CommentToken> pendingComments) {
            this.pendingComments = pendingComments;
        }

        @Override
        public Event produce() {
            if (!documentEndEmitted) {
                // First, emit the DocumentEnd event (similar to ParseDocumentEnd.produce())
                documentEndEmitted = true;
                Token token = scanner.peekToken();
                Mark startMark = token.getStartMark();
                Mark endMark = startMark;
                boolean explicit = false;
                if (scanner.checkToken(Token.Id.DocumentEnd)) {
                    token = scanner.next();
                    endMark = token.getEndMark();
                    explicit = true;
                }
                directiveTags.clear();
                state = this;
                return new DocumentEndEvent(explicit, startMark, endMark);
            }
            // Then emit any pending comments
            if (!pendingComments.isEmpty()) {
                state = this;
                return produceCommentEvent(pendingComments.remove(0));
            }
            // Finally, continue with ParseDocumentStart
            return new ParseDocumentStart().produce();
        }
    }
}
