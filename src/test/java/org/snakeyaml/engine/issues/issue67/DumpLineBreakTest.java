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
package org.snakeyaml.engine.issues.issue67;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.ScannerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP_SETTINGS;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

/**
 * Issue 67: ScannerException on block scalar "\n" See
 * <a href="https://www.baeldung.com/yaml-multi-line#1-keep">Keep in Literal scalar</a>
 */
@org.junit.jupiter.api.Tag("fast")
public class DumpLineBreakTest {

    @Test
    @DisplayName("Dump default scalar style")
    void dumpDefaultScalaStyle() {
        assertEquals(ScalarStyle.PLAIN, DEFAULT_DUMP_SETTINGS.defaultScalarStyle());
    }

    @Test
    @DisplayName("Dump literal scalar style")
    void dumpLiteralScalaStyle() {
        check(ScalarStyle.LITERAL, "\n", "|2+\n\n");
        check(ScalarStyle.LITERAL, "", "\"\"\n");
        check(ScalarStyle.LITERAL, " ", "\" \"\n");
    }

    @Test
    @DisplayName("Dump JSON scalar style")
    void dumpJSONScalaStyle() {
        check(ScalarStyle.JSON_SCALAR_STYLE, "\n", "\"\\n\"\n");
        check(ScalarStyle.JSON_SCALAR_STYLE, "", "\"\"\n");
        check(ScalarStyle.JSON_SCALAR_STYLE, " ", "\" \"\n");
    }

    @Test
    @DisplayName("Dump PLAIN scalar style")
    void dumpPlainScalaStyle() {
        check(ScalarStyle.PLAIN, "\n", "|2+\n\n");
        check(ScalarStyle.PLAIN, "", "''\n");
        check(ScalarStyle.PLAIN, " ", "' '\n");
    }

    @Test
    @DisplayName("Dump FOLDED scalar style")
    void dumpFoldedScalaStyle() {
        check(ScalarStyle.FOLDED, "\n", ">2+\n\n");
        check(ScalarStyle.FOLDED, "", "\"\"\n");
        check(ScalarStyle.FOLDED, " ", "\" \"\n");
    }

    @Test
    @DisplayName("Dump SINGLE_QUOTED scalar style")
    void dumpSINGLE_QUOTEDScalaStyle() {
        check(ScalarStyle.SINGLE_QUOTED, "\n", "'\n\n  '\n");
        check(ScalarStyle.SINGLE_QUOTED, "", "''\n");
        check(ScalarStyle.SINGLE_QUOTED, " ", "' '\n");
    }

    @Test
    @DisplayName("Dump DOUBLE_QUOTED scalar style")
    void dumpDOUBLE_QUOTEDScalaStyle() {
        check(ScalarStyle.DOUBLE_QUOTED, "\n", "\"\\n\"\n");
        check(ScalarStyle.DOUBLE_QUOTED, "", "\"\"\n");
        check(ScalarStyle.DOUBLE_QUOTED, " ", "\" \"\n");
    }

    private void check(ScalarStyle style, String yaml, String expected) {
        DumpSettings dumpSettings = DumpSettings.builder().setDefaultScalarStyle(style).build();
        Dump dump = new Dump(dumpSettings);
        String dumpString = dump.dumpToString(yaml);
        assertEquals(expected, dumpString);
        assertEquals(yaml, DEFAULT_LOAD.loadFromString(dumpString));
    }

    @Test
    @DisplayName("Use Keep in Literal scalar")
    void parseLiteral() {
        String input = """
            ---
            top:
              foo:
              - problem: |2+
            
              bar: baz
            """;
        // System.out.println(input);
        Object obj = DEFAULT_LOAD.loadFromString(input);
        assertNotNull(obj);
    }

    /**
     * <a href="https://matrix.yaml.info/details/S98Z.html">YAML Test Matrix</a>
     * <a href="https://github.com/yaml/yaml-test-suite/issues/37">S98Z under YAML 1.2</a>
     * <a href="https://github.com/yaml/yaml-test-suite/blob/main/src/S98Z.yaml">yaml-test-suite</a>
     */
    @Test
    @DisplayName("Use Keep in Literal scalar: S98Z")
    void parseLiteralS98Z() {
        String input = """
            empty block scalar: >
            \s
             \s
              \s
             # comment""";
        // System.out.println(input);
        try {
            DEFAULT_LOAD.loadFromString(input);
            fail("S98Z");
        } catch (ScannerException e) {
            assertTrue(
                e.getMessage().contains(
                    "the leading empty lines contain more spaces (3) than the first non-empty line (1)."),
                e.getMessage());
        }
    }

    /**
     * <a href="https://matrix.yaml.info/details/T26H.html">YAML Test Matrix</a>
     * <a href="https://github.com/yaml/yaml-test-suite/blob/main/src/T26H.yaml">yaml-test-suite</a>
     */
    @Test
    @DisplayName("Use Keep in Literal scalar: T26H")
    void parseLiteralT26H() {
        String input = """
            --- |
            \s
             \s
              literal
              \s
             \s
              text
            
             # Comment
            """;
        // System.out.println(input);
        Object obj = DEFAULT_LOAD.loadFromString(input);
        assertEquals("""
            
            
            literal
            \s
            
            text
            """, obj);
    }
}
