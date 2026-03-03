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
package org.snakeyaml.engine.usecases.inherited;

import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.events.AliasEvent;
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
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.tokens.AliasToken;
import org.snakeyaml.engine.v2.tokens.AnchorToken;
import org.snakeyaml.engine.v2.tokens.ScalarToken;
import org.snakeyaml.engine.v2.tokens.TagToken;
import org.snakeyaml.engine.v2.tokens.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CanonicalParser implements Parser {

    private final String label;
    private final List<Event> events;
    private final CanonicalScanner scanner;
    private boolean parsed;

    public CanonicalParser(String data, String label) {
        this.label = label;
        events = new ArrayList<>();
        parsed = false;
        scanner = new CanonicalScanner(data, label);
    }

    // stream: STREAM-START document* STREAM-END
    private void parseStream() {
        scanner.getToken(Token.Id.StreamStart);
        events.add(new StreamStartEvent(null, null));
        while (!scanner.checkToken(Token.Id.StreamEnd)) {
            if (scanner.checkToken(Token.Id.Directive, Token.Id.DocumentStart)) {
                parseDocument();
            } else {
                throw new CanonicalException("Document is expected, got " + scanner.tokens.get(0) + " in " + label);
            }
        }
        scanner.getToken(Token.Id.StreamEnd);
        events.add(new StreamEndEvent(null, null));
    }

    // document: DIRECTIVE? DOCUMENT-START node
    private void parseDocument() {
        if (scanner.checkToken(Token.Id.Directive)) {
            scanner.getToken(Token.Id.Directive);
        }
        scanner.getToken(Token.Id.DocumentStart);
        events.add(new DocumentStartEvent(true, SpecVersion.V_1_2, Collections.emptyMap(), null, null));
        parseNode();
        if (scanner.checkToken(Token.Id.DocumentEnd)) {
            scanner.getToken(Token.Id.DocumentEnd);
        }
        events.add(new DocumentEndEvent(true, null, null));
    }

    // node: ALIAS | ANCHOR? TAG? (SCALAR|sequence|mapping)
    private void parseNode() {
        if (scanner.checkToken(Token.Id.Alias)) {
            var token = (AliasToken) scanner.next();
            events.add(new AliasEvent(token.getValue(), null, null));
        } else {
            Anchor anchor = null;
            if (scanner.checkToken(Token.Id.Anchor)) {
                var token = (AnchorToken) scanner.next();
                anchor = token.getValue();
            }
            String tag = null;
            if (scanner.checkToken(Token.Id.Tag)) {
                var token = (TagToken) scanner.next();
                tag = token.getValue().handle() + token.getValue().suffix();
            }
            if (scanner.checkToken(Token.Id.Scalar)) {
                var token = (ScalarToken) scanner.next();
                events.add(new ScalarEvent(anchor, tag, new ImplicitTuple(false, false), token.getValue(), ScalarStyle.PLAIN, null, null));
            } else if (scanner.checkToken(Token.Id.FlowSequenceStart)) {
                events.add(new SequenceStartEvent(anchor, Tag.SEQ.getValue(), false, FlowStyle.AUTO, null, null));
                parseSequence();
            } else if (scanner.checkToken(Token.Id.FlowMappingStart)) {
                events.add(new MappingStartEvent(anchor, Tag.MAP.getValue(), false, FlowStyle.AUTO, null, null));
                parseMapping();
            } else {
                throw new CanonicalException("SCALAR, '[', or '{' is expected, got " + scanner.tokens.get(0));
            }
        }
    }

    // sequence: SEQUENCE-START (node (ENTRY node)*)? ENTRY? SEQUENCE-END
    private void parseSequence() {
        scanner.getToken(Token.Id.FlowSequenceStart);
        if (!scanner.checkToken(Token.Id.FlowSequenceEnd)) {
            parseNode();
            while (!scanner.checkToken(Token.Id.FlowSequenceEnd)) {
                scanner.getToken(Token.Id.FlowEntry);
                if (!scanner.checkToken(Token.Id.FlowSequenceEnd)) {
                    parseNode();
                }
            }
        }
        scanner.getToken(Token.Id.FlowSequenceEnd);
        events.add(new SequenceEndEvent(null, null));
    }

    // mapping: MAPPING-START (map_entry (ENTRY map_entry)*)? ENTRY? MAPPING-END
    private void parseMapping() {
        scanner.getToken(Token.Id.FlowMappingStart);
        if (!scanner.checkToken(Token.Id.FlowMappingEnd)) {
            parseMapEntry();
            while (!scanner.checkToken(Token.Id.FlowMappingEnd)) {
                scanner.getToken(Token.Id.FlowEntry);
                if (!scanner.checkToken(Token.Id.FlowMappingEnd)) {
                    parseMapEntry();
                }
            }
        }
        scanner.getToken(Token.Id.FlowMappingEnd);
        events.add(new MappingEndEvent(null, null));
    }

    // map_entry: KEY node VALUE node
    private void parseMapEntry() {
        scanner.getToken(Token.Id.Key);
        parseNode();
        scanner.getToken(Token.Id.Value);
        parseNode();
    }

    public void parse() {
        parseStream();
        parsed = true;
    }

    @Override
    public Event next() {
        if (!parsed) {
            parse();
        }
        return events.remove(0);
    }

    /**
     * Check the type of the next event.
     */
    @Override
    public boolean checkEvent(Event.ID choice) {
        if (!parsed) {
            parse();
        }
        if (!events.isEmpty()) {
            return events.get(0).getEventId() == choice;
        }
        return false;
    }

    /**
     * Get the next event.
     */
    @Override
    public Event peekEvent() {
        if (!parsed) {
            parse();
        }
        if (events.isEmpty()) {
            return null;
        } else {
            return events.get(0);
        }
    }

    @Override
    public boolean hasNext() {
        return peekEvent() != null;
    }
}
