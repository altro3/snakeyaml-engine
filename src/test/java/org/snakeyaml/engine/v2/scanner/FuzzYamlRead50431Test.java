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
package org.snakeyaml.engine.v2.scanner;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.exceptions.ScannerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

/**
 * <a href="https://github.com/FasterXML/jackson-dataformats-text/issues/400">Issue 400</a>
 * <a href="https://github.com/FasterXML/jackson-dataformats-text/pull/401">Issue 401</a>
 */
@org.junit.jupiter.api.Tag("fast")
class FuzzYamlRead50431Test {

    @Test
    void testIncompleteValue() {
        var e = assertThrows(ScannerException.class, () -> DEFAULT_LOAD.loadFromString("\"\\UE30EEE"));
        assertTrue(e.getMessage().contains("found unknown escape character E30EEE"), e.getMessage());
    }

    @Test
    void testProperValue() {
        var parsed = (String) DEFAULT_LOAD.loadFromString("\"\\U0000003B\"");
        assertEquals(1, parsed.length());
        assertEquals("\u003B", parsed);
    }

    @Test
    void testNotQuoted() {
        var parsed = (String) DEFAULT_LOAD.loadFromString("\\UE30EEE");
        assertEquals(8, parsed.length());
        assertEquals("\\UE30EEE", parsed);
    }
}
