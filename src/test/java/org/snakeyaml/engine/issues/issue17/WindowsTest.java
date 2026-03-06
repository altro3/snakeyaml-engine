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
package org.snakeyaml.engine.issues.issue17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD_SETTINGS;

/**
 * <a href="https://yaml.org/spec/1.2/spec.html#id2774608">link</a>
 */
@org.junit.jupiter.api.Tag("fast")
class WindowsTest {

    @Test
    @DisplayName("Check that Windows style line endings handled the same as Unix style ones")
    void testGetLineNumberOnWindows() {
        var reader1 = new StreamReader(DEFAULT_LOAD_SETTINGS, "foo\r\nbar");
        var reader2 = new StreamReader(DEFAULT_LOAD_SETTINGS, "foo\nbar");
        reader1.forward(100);
        reader2.forward(100);
        assertEquals(reader1.getLine(), reader2.getLine());
    }

    @Test
    void countLinesCRLF() {
        var e = assertThrows(ParserException.class, () -> DEFAULT_LOAD.loadFromString("\r\n["));
        assertTrue(e.getMessage().contains("line 2,"), e.getMessage());
    }

    @Test
    void countLinesCRCR() {
        var e = assertThrows(ParserException.class, () -> DEFAULT_LOAD.loadFromString("\r\r["));
        assertTrue(e.getMessage().contains("line 3,"));
    }

    @Test
    void countLinesLFLF() {
        var e = assertThrows(ParserException.class, () -> DEFAULT_LOAD.loadFromString("\n\n["));
        assertTrue(e.getMessage().contains("line 3,"));
    }
}
