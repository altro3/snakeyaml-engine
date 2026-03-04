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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.Event.Id;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;


public class ParserWithCommentEnabledTest {

    private final boolean DEBUG = false;

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

    private void assertEventListEquals(List<Id> expectedEventIdList, Parser parser) {
        for (Id expectedEventId : expectedEventIdList) {
            parser.checkEvent(expectedEventId);
            Event event = parser.next();
            if (DEBUG) {
                println("Expected: " + expectedEventId);
                println("Got: " + event);
                println();
            }
            if (event == null) {
                fail("Missing event: " + expectedEventId);
            }
            assertEquals(expectedEventId, event.getEventId());
        }
    }

    @SuppressWarnings("unused")
    private void printEventList(Parser parser) {
        for (Event event = parser.next(); event != null; event = parser.next()) {
            println("Got: " + event);
            println();
        }
    }

    private Parser createParser(String data) {
        LoadSettings loadSettings = LoadSettings.builder().setParseComments(true).build();
        Parser sut = new ParserImpl(loadSettings, new StreamReader(loadSettings, data));
        return sut;
    }

    @Test
    public void testEmpty() {
        List<Id> expectedEventIdList = Arrays.asList(Id.StreamStart, Id.StreamEnd);
        String data = "";
        Parser sut = createParser(data);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void testParseWithOnlyComment() {
        String data = "# Comment";
        List<Id> expectedEventIdList = Arrays.asList(//
            Id.StreamStart, //
            Id.Comment, //
            Id.StreamEnd //
        );
        Parser sut = createParser(data);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void testCommentEndingALine() {
        String data = "" + //
            "key: # Comment\n" + //
            "  value\n";

        List<Id> expectedEventIdList = Arrays.asList(Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, Id.Comment, Id.Scalar, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd);
        Parser sut = createParser(data);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void testMultiLineComment() {
        String data = "" + //
            "key: # Comment\n" + //
            "     # lines\n" + //
            "  value\n" + //
            "\n";
        List<Id> expectedEventIdList = Arrays.asList(Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, Id.Comment, Id.Comment, Id.Scalar, //
            Id.Comment, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd);
        Parser sut = createParser(data);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void testBlankLine() {
        String data = "" + //
            "\n";
        List<Id> expectedEventIdList = Arrays.asList(Id.StreamStart, //
            Id.Comment, //
            Id.StreamEnd);
        Parser sut = createParser(data);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void testBlankLineComments() {
        String data = "" + //
            "\n" + //
            "abc: def # commment\n" + //
            "\n" + //
            "\n";

        List<Id> expectedEventIdList = Arrays.asList(Id.StreamStart, //
            Id.Comment, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, Id.Scalar, Id.Comment, //
            Id.Comment, //
            Id.Comment, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd);
        Parser sut = createParser(data);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void test_blockScalar() {
        String data = "" + //
            "abc: > # Comment\n" + //
            "    def\n" + //
            "    hij\n" + //
            "\n";

        List<Id> expectedEventIdList = Arrays.asList(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, Id.Comment, //
            Id.Scalar, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );
        Parser sut = createParser(data);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void testDirectiveLineEndComment() {
        String data = "%YAML 1.1 #Comment\n---";
        List<Id> expectedEventIdList = Arrays.asList(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.Scalar, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );
        Parser sut = createParser(data);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void testSequence() {
        String data = "" + //
            "# Comment\n" + //
            "list: # InlineComment1\n" + //
            "# Block Comment\n" + //
            "- item # InlineComment2\n" + //
            "# Comment\n";

        List<Id> expectedEventIdList = Arrays.asList(//
            Id.StreamStart, //
            Id.Comment, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, Id.Comment, Id.Comment, //
            Id.SequenceStart, //
            Id.Scalar, Id.Comment, //
            Id.Comment, //
            Id.SequenceEnd, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );
        Parser sut = createParser(data);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void testAllComments1() throws Exception {
        String data = "" + //
            "# Block Comment1\n" + //
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
            "# Block Comment7\n" + //
            "";

        List<Id> expectedEventIdList = Arrays.asList(//
            Id.StreamStart, //
            Id.Comment, //
            Id.Comment, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, Id.Comment, Id.Comment, //

            Id.Comment, Id.Comment, //
            Id.Scalar, Id.Comment, //

            Id.Comment, //
            Id.Scalar, Id.Comment, Id.Comment, //
            Id.Comment, //

            Id.SequenceStart, //
            Id.Scalar, Id.Comment, //
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
            Id.Comment, //
            Id.MappingEnd, //
            Id.SequenceEnd, //
            Id.MappingEnd, Id.DocumentEnd, //

            Id.DocumentStart, //
            Id.Comment, //
            Id.Scalar, // Empty
            Id.DocumentEnd, //
            Id.StreamEnd //
        );
        Parser sut = createParser(data);
        // printEventList(sut);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void testAllComments2() throws Exception {
        String data = "" + //
            "# Block Comment1\n" + //
            "# Block Comment2\n" + //
            "- item1 # Inline Comment1a\n" + //
            "        # Inline Comment1b\n" + //
            "# Block Comment3a\n" + //
            "# Block Comment3b\n" + //
            "- item2: value # Inline Comment2\n" + //
            "# Block Comment4\n" + //
            "";

        List<Id> expectedEventIdList = Arrays.asList(//
            Id.StreamStart, //
            Id.Comment, //
            Id.Comment, //
            Id.DocumentStart, //
            Id.SequenceStart, //
            Id.Scalar, Id.Comment, Id.Comment, //
            Id.Comment, //
            Id.Comment, //
            Id.MappingStart, //
            Id.Scalar, Id.Scalar, Id.Comment, //
            Id.Comment, //
            Id.MappingEnd, //
            Id.SequenceEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );
        Parser sut = createParser(data);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void testAllComments3() throws Exception {
        String data = "" + //
            "# Block Comment1\n" + //
            "[ item1, item2: value2, {item3: value3} ] # Inline Comment1\n" + //
            "# Block Comment2\n" + //
            "";
        List<Id> expectedEventIdList = Arrays.asList(//
            Id.StreamStart, //
            Id.Comment, //
            Id.DocumentStart, //
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
        Parser sut = createParser(data);
        // printEventList(sut);
        assertEventListEquals(expectedEventIdList, sut);
    }

    @Test
    public void testKeepingNewLineInsideSequence() throws Exception {
        String data = "" + "\n" + "key:\n" + "\n" + "- item1\n" + "\n" + // Per Spec this is part of
            // plain scalar above
            "- item2\n" + "\n" + // Per Spec this is part of plain scalar above
            "- item3\n" + "\n" + // Should be comment?
            "key2: value2\n" + "\n" + // Should be comment?
            "key3: value3\n" + "\n" + // Should be comment?
            "";

        List<Id> expectedEventIdList = Arrays.asList(//
            Id.StreamStart, //
            Id.Comment, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, //
            Id.Comment, //
            Id.SequenceStart, //
            Id.Scalar, //
            Id.Scalar, //
            Id.Scalar, //
            Id.Comment, //
            Id.SequenceEnd, //
            Id.Scalar, //
            Id.Scalar, //
            Id.Comment, //
            Id.Scalar, //
            Id.Scalar, //
            Id.Comment, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );
        Parser sut = createParser(data);
        // printEventList(sut);
        assertEventListEquals(expectedEventIdList, sut);
    }
}
