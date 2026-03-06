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
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.events.CommentEvent;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.Event.Id;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD_SETTINGS;

class ProblematicYamlTest {

    private static final boolean DEBUG = false;
    private static final LoadSettings LOAD_OPTIONS = LoadSettings.builder()
        .setParseComments(true)
        .build();

    @Test
    void testParseProblematicYaml1() {
        final String yamlString1 = "key: value\n" +
            "  # Comment 1\n" + // s.b BLOCK, classified as INLINE
            "\n" +
            "  # Comment 2\n";

        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, //
            Id.Scalar, //
            Id.Comment, //
            Id.Comment, //
            Id.Comment, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );
        List<CommentType> expectedCommentTypeList = List.of(//
            CommentType.BLOCK, CommentType.BLANK_LINE, CommentType.BLOCK);
        ParserImpl parser =
            new ParserImpl(LOAD_OPTIONS, new StreamReader(LOAD_OPTIONS, new StringReader(yamlString1)));
        assertEventListEquals(expectedEventIdList, expectedCommentTypeList, parser);
    }

    @Test
    void testParseProblematicYaml2() {
        final String yamlString2 = "key: value\n" +
            "\n" +
            "  # Comment 1\n" + // s.b BLOCK, classified as INLINE
            "\n" +
            "  # Comment 2\n";
        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, //
            Id.Scalar, //
            Id.Comment, //
            Id.Comment, //
            Id.Comment, //
            Id.Comment, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );
        var expectedCommentTypeList = List.of(CommentType.BLANK_LINE, CommentType.BLOCK, CommentType.BLANK_LINE, CommentType.BLOCK);
        var parser = new ParserImpl(LOAD_OPTIONS, new StreamReader(LOAD_OPTIONS, new StringReader(yamlString2)));
        assertEventListEquals(expectedEventIdList, expectedCommentTypeList, parser);
    }

    @Test
    void testParseProblematicYaml3() {
        final String yamlString3 = """
            key: value
            
            key: value
            """;
        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, //
            Id.Scalar, //
            Id.Comment, //
            Id.Scalar, //
            Id.Scalar, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd //
        );
        var expectedCommentTypeList = Collections.singletonList(CommentType.BLANK_LINE);
        var parser = new ParserImpl(LOAD_OPTIONS, new StreamReader(LOAD_OPTIONS, new StringReader(yamlString3)));
        assertEventListEquals(expectedEventIdList, expectedCommentTypeList, parser);
    }

    @Test
    void testParseProblematicYaml4() {
        String yamlString4 = """
            ---
            in the block context:
                indentation should be kept: {\s
                but in the flow context: [
            it may be violated]
            }
            ---
            the parser does not require scalars
            to be indented with at least one space
            ...
            ---
            "the parser does not require scalars
            to be indented with at least one space"
            ---
            foo:
                bar: 'quoted scalars
            may not adhere indentation'
            """;
        var expectedEventIdList = List.of(//
            Id.StreamStart, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, //
            Id.MappingStart, //
            Id.Scalar, //
            Id.MappingStart, //
            Id.Scalar, //
            Id.SequenceStart, //
            Id.Scalar, //
            Id.SequenceEnd, //
            Id.MappingEnd, //
            Id.MappingEnd, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.DocumentStart, //
            Id.Scalar, //
            Id.DocumentEnd, //
            Id.DocumentStart, //
            Id.Scalar, //
            Id.DocumentEnd, //
            Id.DocumentStart, //
            Id.MappingStart, //
            Id.Scalar, //
            Id.MappingStart, //
            Id.Scalar, //
            Id.Scalar, //
            Id.MappingEnd, //
            Id.MappingEnd, //
            Id.DocumentEnd, //
            Id.StreamEnd//
        );
        var parser = new ParserImpl(DEFAULT_LOAD_SETTINGS, new StreamReader(DEFAULT_LOAD_SETTINGS, new StringReader(yamlString4)));
        assertEventListEquals(expectedEventIdList, new ArrayList<>(), parser);
    }

    private void assertEventListEquals(List<Id> expectedEventIdList, List<CommentType> expectedCommentTypeList, Parser parser) {
        Iterator<CommentType> commentTypeIterator = expectedCommentTypeList.iterator();
        for (Id expectedEventId : expectedEventIdList) {
            parser.checkEvent(expectedEventId);
            Event event = parser.next();
            println("Expected: " + expectedEventId);
            if (event == null) {
                fail("Missing event: " + expectedEventId);
            }
            println("Got: " + event + (event.getEventId() == Id.Comment ? " " + ((CommentEvent) event).getCommentType() : ""));
            println();
            if (event.getEventId() == Id.Comment) {
                assertEquals(commentTypeIterator.next(), ((CommentEvent) event).getCommentType());
            }
            assertEquals(expectedEventId, event.getEventId());
        }
    }

    @SuppressWarnings("unused")
    private void printEventList(Parser parser) {
        for (Event event = parser.next(); event != null; event = parser.next()) {
            println("Got: " + event + (event.getEventId() == Id.Comment ? " " + ((CommentEvent) event).getCommentType() : ""));
            println();
        }
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
}
