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
package org.snakeyaml.engine.v2.comments;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.emitter.Emitable;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.Event.Id;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.snakeyaml.engine.v2.serializer.Serializer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerializerWithCommentEnabledTest {

    private final boolean DEBUG = false;

    @Test
    void testEmpty() {
        String data = "";
        var expectedEventIdList = List.of(
            Id.StreamStart,
            Id.StreamEnd
        );

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    @Test
    void testParseWithOnlyComment() {
        String data = "# Comment";

        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.Comment, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    @Test
    void testCommentEndingALine() {
        String data = "key: # Comment\n" + //
            "  value\n";

        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, Id.Comment, Id.Scalar, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd);

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    @Test
    void testMultiLineComment() {
        String data = "key: # Comment\n" + //
            "     # lines\n" + //
            "  value\n" + //
            "\n";

        var expectedEventIdList = List.of(Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, Id.Comment, Id.Comment, Id.Scalar, //
            Id.MappingEnd, //
            Id.Comment, //
            Id.DocumentEnd, //
            Id.StreamEnd);

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    @Test
    void testBlankLine() {
        String data = "\n";

        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.Comment, //
            Id.DocumentEnd, //
            Id.StreamEnd);

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    @Test
    void testBlankLineComments() {
        String data = "\n" + //
            "abc: def # comment\n" + //
            "\n" + //
            "\n";

        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Comment, //
            Id.Scalar, Id.Scalar, Id.Comment, //
            Id.MappingEnd, //
            Id.Comment, //
            Id.Comment, //
            Id.DocumentEnd, //
            Id.StreamEnd);

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    @Test
    void test_blockScalar() {
        String data = "abc: > # Comment\n" + //
            "    def\n" + //
            "    hij\n" + //
            "\n";

        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, Id.Comment, //
            Id.Scalar, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    @Test
    void testDirectiveLineEndComment() {
        String data = "%YAML 1.1 #Comment\n---";

        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.Scalar, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    @Test
    void testSequence() {
        String data = "# Comment\n" + //
            "list: # InlineComment1\n" + //
            "# Block Comment\n" + //
            "- item # InlineComment2\n" + //
            "# Comment\n";

        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Comment, //
            Id.Scalar, Id.Comment, //
            Id.SequenceStart, //
            Id.Comment, //
            Id.Scalar, Id.Comment, //
            Id.SequenceEnd, //
            Id.MappingEnd, //
            Id.Comment, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    @Test
    void testAllComments1() {
        String data = "# Block Comment1\n" + //
            "# Block Comment2\n" + //
            "key: # Inline Comment1a\n" + //
            "     # Inline Comment1b\n" + //
            "  # Block Comment3a\n" + //
            "  # Block Comment3b\n" + //
            "  value # Inline Comment2\n" + //
            "# Block Comment4\n" + //
            "list: # InlineComment3a\n" + //
            "      # InlineComment3b\n" + //
            "# Block Comment5\n" + //
            "- item1 # InlineComment4\n" + //
            "- item2: [ value2a, value2b ] # InlineComment5\n" + //
            "- item3: { key3a: [ value3a1, value3a2 ], key3b: value3b } # InlineComment6\n" + //
            "# Block Comment6\n" + //
            "---\n" + //
            "# Block Comment7\n";

        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Comment, //
            Id.Comment, //
            Id.Scalar, Id.Comment, Id.Comment, //

            Id.Comment, Id.Comment, //
            Id.Scalar, Id.Comment, //

            Id.Comment, //
            Id.Scalar, Id.Comment, Id.Comment, //

            Id.SequenceStart, //
            Id.Comment, //
            Id.Scalar, //
            Id.Comment, //

            Id.MappingStart, //
            Id.Scalar, Id.SequenceStart, Id.Scalar, Id.Scalar, Id.SequenceEnd, Id.Comment, //
            Id.MappingEnd,

            Id.MappingStart, //
            Id.Scalar, // value=item3
            Id.MappingStart, //
            Id.Scalar, // value=key3a
            Id.SequenceStart, //
            Id.Scalar, // value=value3a
            Id.Scalar, // value=value3a2
            Id.SequenceEnd, //
            Id.Scalar, // value=key3b
            Id.Scalar, // value=value3b
            Id.MappingEnd, //
            Id.Comment, // type=IN_LINE, value= InlineComment6
            Id.MappingEnd, //
            Id.SequenceEnd, //
            Id.MappingEnd, //
            Id.Comment, //
            Id.DocumentEnd, //

            Id.DocumentStart, //
            Id.Comment, //
            Id.Scalar, // Empty
            Id.DocumentEnd, //
            Id.StreamEnd //
        );

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    @Test
    void testAllComments2() {
        String data = "# Block Comment1\n" + //
            "# Block Comment2\n" + //
            "- item1 # Inline Comment1a\n" + //
            "        # Inline Comment1b\n" + //
            "# Block Comment3a\n" + //
            "# Block Comment3b\n" + //
            "- item2: value # Inline Comment2\n" + //
            "# Block Comment4\n";

        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.SequenceStart, //
            Id.Comment, //
            Id.Comment, //
            Id.Scalar, Id.Comment, Id.Comment, //
            Id.MappingStart, //
            Id.Comment, //
            Id.Comment, //
            Id.Scalar, Id.Scalar, Id.Comment, //
            Id.MappingEnd, //
            Id.SequenceEnd, //
            Id.Comment, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    @Test
    void testAllComments3() {
        String data = "# Block Comment1\n" + //
            "[ item1, item2: value2, {item3: value3} ] # Inline Comment1\n" + //
            "# Block Comment2\n";

        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.Comment, //
            Id.SequenceStart, //
            Id.Scalar, Id.MappingStart, //
            Id.Scalar, Id.Scalar, //
            Id.MappingEnd, //
            Id.MappingStart, //
            Id.Scalar, Id.Scalar, //
            Id.MappingEnd, //
            Id.SequenceEnd, //
            Id.Comment, //
            Id.Comment, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );

        List<Event> result = serializeWithCommentsEnabled(data);

        assertEventListEquals(expectedEventIdList, result);
    }

    private void println(String s) {
        if (DEBUG) {
            System.out.println(s);
        }
    }

    private void println() {
        if (DEBUG) {
            System.out.println();
        }
    }

    private void assertEventListEquals(List<Id> expectedEventIdList, List<Event> actualEvents) {
        Iterator<Event> iterator = actualEvents.iterator();
        for (Id expectedEventId : expectedEventIdList) {
            println("Expected: " + expectedEventId);
            assertTrue(iterator.hasNext());
            Event event = iterator.next();
            println("Got: " + event);
            println();
            assertEquals(expectedEventId, event.getEventId());
        }
    }

    private List<Event> serializeWithCommentsEnabled(String data) {
        var emitter = new TestEmitter();
        var dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setDumpComments(true)
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .build();
        var serializer = new Serializer(dumpSettings, emitter);
        serializer.emitStreamStart();
        var settings = LoadSettings.builder()
            .setParseComments(true)
            .build();
        var composer = new Composer(settings, new ParserImpl(settings, new StreamReader(settings, data)));
        while (composer.hasNext()) {
            serializer.serializeDocument(composer.next());
        }
        serializer.emitStreamEnd();
        List<Event> events = emitter.getEventList();
        println("RESULT: ");
        for (Event event : events) {
            println(event.toString());
        }
        println();
        return events;
    }

    private static class TestEmitter implements Emitable {

        private final List<Event> eventList = new ArrayList<>();

        @Override
        public Emitable emit(Event event) {
            eventList.add(event);
            return this;
        }

        public List<Event> getEventList() {
            return eventList;
        }
    }
}
