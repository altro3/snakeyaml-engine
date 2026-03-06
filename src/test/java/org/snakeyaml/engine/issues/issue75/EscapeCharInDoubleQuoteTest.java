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
package org.snakeyaml.engine.issues.issue75;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.ScannerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

/**
 * Test for <a href="https://bitbucket.org/snakeyaml/snakeyaml-engine/issues/75/fails-to-parse-valid-json-0x7f-in-quoted">Issue75</a>
 */
class EscapeCharInDoubleQuoteTest {

    @Test
    void testSpecialCharInDoubleQuote() {
        String str = "\"\u007F\""; // "\DEL"
        assertEquals("\u007F", DEFAULT_LOAD.loadFromString(str));
    }

    @Test
    void testDELAllowedInDoubleQuoted() {
        String str = "\"\u007F\"";
        assertEquals("\u007F", DEFAULT_LOAD.loadFromString(str)); // Should pass - nb-json allows 0x7F
    }

    @Test
    void testDELAllowedInSingleQuoted() {
        String str = "'\u007F'";
        assertEquals("\u007F", DEFAULT_LOAD.loadFromString(str)); // Should pass - nb-json allows 0x7F
    }

    @Test
    void testDELRejectedInPlainScalar() {
        String str = "key: \u007F"; // DEL in plain scalar value
        assertThrows(ScannerException.class, () -> DEFAULT_LOAD.loadFromString(str));
    }

    @Test
    void testDELRejectedInPlainScalarKey() {
        String str = "ke\u007Fy: value"; // DEL in plain scalar key
        assertThrows(ScannerException.class, () -> DEFAULT_LOAD.loadFromString(str));
    }

    @Test
    void testDELRejectedInComment() {
        LoadSettings settings = LoadSettings.builder().setParseComments(true).build();
        Load loadWithComments = new Load(settings);
        String str = "key: value # comment with \u007F";
        assertThrows(ScannerException.class, () -> loadWithComments.loadFromString(str));
    }

    @Test
    void testDELRejectedInBlockScalar() {
        String str = "|\n  text with \u007F"; // DEL in literal block scalar
        assertThrows(ScannerException.class, () -> DEFAULT_LOAD.loadFromString(str));
    }

    @Test
    void testDELRejectedInFoldedBlockScalar() {
        String str = ">\n  text with \u007F"; // DEL in folded block scalar
        assertThrows(ScannerException.class, () -> DEFAULT_LOAD.loadFromString(str));
    }
}
