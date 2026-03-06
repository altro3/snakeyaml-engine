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
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ComposerWithCommentEnabledTest {

    private static final boolean DEBUG = false;

    @Test
    void testEmpty() {
        String data = "";
        String[] expected = new String[] { //
            "" //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testParseWithOnlyComment() {
        String data = "# Comment";
        var expected = new String[] { //
            "Block Comment", //
            "MappingNode", //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testCommentEndingALine() {
        String data = """
            key: # Comment
              value
            """;

        var expected = new String[] { //
            "MappingNode", //
            "    Tuple", //
            "        ScalarNode: key", //
            "            InLine Comment", //
            "        ScalarNode: value" //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testMultiLineComment() {
        var data = """
            key: # Comment
                 # lines
              value
            
            """;

        var expected = new String[] { //
            "MappingNode", //
            "    Tuple", //
            "        ScalarNode: key", //
            "            InLine Comment", //
            "            InLine Comment", //
            "        ScalarNode: value", //
            "End Comment" //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testBlankLine() {
        var data = "\n";

        var expected = new String[] { //
            "Block Comment", //
            "MappingNode", //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testBlankLineComments() {
        var data = """
            
            abc: def # commment
            
            
            """;

        var expected = new String[] { //
            "MappingNode", //
            "    Tuple", //
            "        Block Comment", //
            "        ScalarNode: abc", //
            "        ScalarNode: def", //
            "            InLine Comment", //
            "End Comment", //
            "End Comment", //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void test_blockScalar() {
        var data = """
            abc: > # Comment
                def
                hij
            
            """;

        var expected = new String[] { //
            "MappingNode", //
            "    Tuple", //
            "        ScalarNode: abc", //
            "            InLine Comment", //
            "        ScalarNode: def hij" //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testDirectiveLineEndComment() {
        var data = "%YAML 1.1 #Comment\n---\n";

        var expected = new String[] {
            "ScalarNode: "
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testSequence() {
        var data = """
            # Comment
            list: # InlineComment1
            # Block Comment
            - item # InlineComment2
            # Comment
            """;

        var expected = new String[] { //
            "MappingNode", //
            "    Tuple", //
            "        Block Comment", //
            "        ScalarNode: list", //
            "            InLine Comment", //
            "        SequenceNode", //
            "            Block Comment", //
            "            ScalarNode: item", //
            "                InLine Comment", //
            "End Comment" //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testAllComments1() {
        var data = """
            # Block Comment1
            # Block Comment2
            key: # Inline Comment1a
                 # Inline Comment1b
              # Block Comment3a
              # Block Comment3b
              value # Inline Comment2
            # Block Comment4
            list: # InlineComment3a
                  # InlineComment3b
            # Block Comment5
            - item1 # InlineComment4
            - item2: [ value2a, value2b ] # InlineComment5
            - item3: { key3a: [ value3a1, value3a2 ], key3b: value3b } # InlineComment6
            # Block Comment6
            ---
            # Block Comment7
            """;

        var expected = new String[] { //
            "MappingNode", //
            "    Tuple", //
            "        Block Comment", //
            "        Block Comment", //
            "        ScalarNode: key", //
            "            InLine Comment", //
            "            InLine Comment", //
            "        Block Comment", //
            "        Block Comment", //
            "        ScalarNode: value", //
            "            InLine Comment", //
            "    Tuple", //
            "        Block Comment", //
            "        ScalarNode: list", //
            "            InLine Comment", //
            "            InLine Comment", //
            "        SequenceNode", //
            "            Block Comment", //
            "            ScalarNode: item1", //
            "                InLine Comment", //
            "            MappingNode", //
            "                Tuple", //
            "                    ScalarNode: item2", //
            "                    SequenceNode", //
            "                        ScalarNode: value2a", //
            "                        ScalarNode: value2b", //
            "                        InLine Comment", //
            "            MappingNode", //
            "                Tuple", //
            "                    ScalarNode: item3", //
            "                    MappingNode", //
            "                        Tuple", //
            "                            ScalarNode: key3a", //
            "                            SequenceNode", //
            "                                ScalarNode: value3a1", //
            "                                ScalarNode: value3a2", //
            "                        Tuple", //
            "                            ScalarNode: key3b", //
            "                            ScalarNode: value3b", //
            "                        InLine Comment", //
            "End Comment", //
            "---", //
            "Block Comment", //
            "ScalarNode: ", // This is an empty scalar created as this is an empty document
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testAllComments2() {
        var data = """
            # Block Comment1
            # Block Comment2
            - item1 # Inline Comment1a
                    # Inline Comment1b
            # Block Comment3a
            # Block Comment3b
            - item2: value # Inline Comment2
            # Block Comment4
            """;

        var expected = new String[] { //
            "SequenceNode", //
            "    Block Comment", //
            "    Block Comment", //
            "    ScalarNode: item1", //
            "        InLine Comment", //
            "        InLine Comment", //
            "    MappingNode", //
            "        Tuple", //
            "            Block Comment", //
            "            Block Comment", //
            "            ScalarNode: item2", //
            "            ScalarNode: value", //
            "                InLine Comment", //
            "End Comment", //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testAllComments3() {
        var data = """
            # Block Comment1
            [ item1, item2: value2, {item3: value3} ] # Inline Comment1
            # Block Comment2
            """;

        var expected = new String[] { //
            "Block Comment", //
            "SequenceNode", //
            "    ScalarNode: item1", //
            "    MappingNode", //
            "        Tuple", //
            "            ScalarNode: item2", //
            "            ScalarNode: value2", //
            "    MappingNode", //
            "        Tuple", //
            "            ScalarNode: item3", //
            "            ScalarNode: value3", //
            "    InLine Comment", //
            "End Comment", //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = getNodeList(sut);

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testGetSingleNode() {
        var data = """
            
            abc: def # commment
            
            
            """;
        var expected = new String[] { //
            "MappingNode", //
            "    Tuple", //
            "        Block Comment", "        ScalarNode: abc", //
            "        ScalarNode: def", //
            "            InLine Comment", //
            "End Comment", //
            "End Comment", //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = Collections.singletonList(sut.getSingleNode());

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testGetSingleNodeHeaderComment() {
        var data = """
            
            # Block Comment1
            # Block Comment2
            abc: def # commment
            
            
            """;
        var expected = new String[] { //
            "MappingNode", //
            "    Tuple", //
            "        Block Comment", //
            "        Block Comment", //
            "        Block Comment", //
            "        ScalarNode: abc", //
            "        ScalarNode: def", //
            "            InLine Comment", //
            "End Comment", //
            "End Comment", //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = Collections.singletonList(sut.getSingleNode());

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    @Test
    void testBaseConstructorGetData() {
        var data = """
            
            abc: def # commment
            
            
            """;

        var sut = new TestConstructor(LoadSettings.builder().build());
        Composer composer = newComposerWithCommentsEnabled(data);
        Object result = sut.constructSingleDocument(composer.getSingleNode());
        assertInstanceOf(LinkedHashMap.class, result);
        @SuppressWarnings("unchecked")
        var map = (LinkedHashMap<String, Object>) result;
        assertEquals(1, map.size());
        assertEquals("def", map.get("abc"));
    }

    @Test
    void testEmptyEntryInMap() {
        var data = """
            userProps:
            #password
            pass: mySecret
            """;
        var expected = new String[] { //
            "MappingNode", //
            "    Tuple", //
            "        ScalarNode: userProps", //
            "        ScalarNode: ", //
            "    Tuple", //
            "        Block Comment", //
            "        ScalarNode: pass", //
            "        ScalarNode: mySecret", //
        };

        Composer sut = newComposerWithCommentsEnabled(data);
        List<Node> result = Collections.singletonList(sut.getSingleNode());

        printNodeList(result);
        assertNodesEqual(expected, result);
    }

    private void printBlockComment(Node node, int level, PrintStream out) {
        if (node.getBlockComments() != null) {
            List<CommentLine> blockComments = node.getBlockComments();
            for (int i = 0; i < blockComments.size(); i++) {
                printWithIndent("Block Comment", level, out);
            }
        }
    }

    private void printEndComment(Node node, int level, PrintStream out) {
        if (node.getEndComments() != null) {
            List<CommentLine> endComments = node.getEndComments();
            for (int i = 0; i < endComments.size(); i++) {
                printWithIndent("End Comment", level, out);
            }
        }
    }

    private void printInLineComment(Node node, int level, PrintStream out) {
        if (node.getInLineComments() != null) {
            List<CommentLine> inLineComments = node.getInLineComments();
            for (int i = 0; i < inLineComments.size(); i++) {
                printWithIndent("InLine Comment", level + 1, out);
            }
        }
    }

    private void printWithIndent(String line, int level, PrintStream out) {
        for (int ix = 0; ix < level; ix++) {
            out.print("    ");
        }
        out.print(line);
        out.print("\n");
    }

    private void printNodeInternal(Node node, int level, PrintStream out) {

        if (node instanceof MappingNode mappingNode) {
            printBlockComment(mappingNode, level, out);
            printWithIndent(mappingNode.getClass().getSimpleName(), level, out);
            for (NodeTuple childNodeTuple : mappingNode.getValue()) {
                printWithIndent("Tuple", level + 1, out);
                printNodeInternal(childNodeTuple.keyNode(), level + 2, out);
                printNodeInternal(childNodeTuple.valueNode(), level + 2, out);
            }
            printInLineComment(mappingNode, level, out);
            printEndComment(mappingNode, level, out);

        } else if (node instanceof SequenceNode sequenceNode) {
            printBlockComment(sequenceNode, level, out);
            printWithIndent(sequenceNode.getClass().getSimpleName(), level, out);
            for (Node childNode : sequenceNode.getValue()) {
                printNodeInternal(childNode, level + 1, out);
            }
            printInLineComment(sequenceNode, level, out);
            printEndComment(sequenceNode, level, out);

        } else if (node instanceof ScalarNode scalarNode) {
            printBlockComment(scalarNode, level, out);
            printWithIndent(scalarNode.getClass().getSimpleName() + ": " + scalarNode.getValue(), level, out);
            printInLineComment(scalarNode, level, out);
            printEndComment(scalarNode, level, out);

        } else {
            printBlockComment(node, level, out);
            printWithIndent(node.getClass().getSimpleName(), level, out);
            printInLineComment(node, level, out);
            printEndComment(node, level, out);
        }
    }

    private void printNodeList(List<Node> nodeList) {
        if (DEBUG) {
            System.out.println("BEGIN");
            boolean first = true;
            for (Node node : nodeList) {
                if (first) {
                    first = false;
                } else {
                    System.out.println("---");
                }
                printNodeInternal(node, 1, System.out);
            }
            System.out.println("DONE\n");
        }
    }

    private List<Node> getNodeList(Composer composer) {
        var nodeList = new ArrayList<Node>();
        while (composer.hasNext()) {
            nodeList.add(composer.next());
        }
        return nodeList;
    }

    private void assertNodesEqual(String[] expected, List<Node> nodeList) {
        var baos = new ByteArrayOutputStream();
        boolean first = true;
        try (var out = new PrintStream(baos)) {
            for (Node node : nodeList) {
                if (first) {
                    first = false;
                } else {
                    out.print("---\n");
                }
                printNodeInternal(node, 0, out);
            }
        }
        String actualString = baos.toString();
        String[] actuals = actualString.split("\n");
        for (int ix = 0; ix < Math.min(expected.length, actuals.length); ix++) {
            assertEquals(expected[ix], actuals[ix]);
        }
        assertEquals(expected.length, actuals.length);
    }

    private Composer newComposerWithCommentsEnabled(String data) {
        var settings = LoadSettings.builder()
            .setParseComments(true)
            .build();
        return new Composer(settings, new ParserImpl(settings, new StreamReader(settings, data)));
    }

    private static class TestConstructor extends StandardConstructor {

        public TestConstructor(LoadSettings settings) {
            super(settings);
        }
    }
}
