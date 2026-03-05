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
package org.snakeyaml.engine.issues.issue54;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP;

/**
 * Issue 54: add a space after anchor (when it is a simple key)
 */
@org.junit.jupiter.api.Tag("fast")
public class DumpWithoutSpaceTest {

    @Test
    @DisplayName("The document does not have a space after the *1 alias")
    void failToParseWithoutSpaceAfterAlias() {
        var e = assertThrows(Exception.class, () -> parse("--- &1\nhash:\n  :one: true\n  :two: true\n  *1: true"));
        assertTrue(e.getMessage().contains("could not find expected ':'"));
    }

    @Test
    @DisplayName("The output does include a space after the *1 alias")
    void parseWithSpaceAfterAlias() {
        Object obj = parse("--- &1\nhash:\n  :one: true\n  :two: true\n  *1 : true");
        assertNotNull(obj);
    }

    @Test
    @DisplayName("Dump and load an alias")
    void parseOwnOutput() {
        var map = new HashMap<Object, Boolean>();
        map.put(":one", true);
        map.put(map, true);
        String output = DEFAULT_DUMP.dumpToString(map);
        assertEquals("""
            &id001
            :one: true
            *id001 : true
            """, output);
        Object recursive = parse(output);
        assertNotNull(recursive);
    }

    private Object parse(String data) {
        var load = new Load(LoadSettings.builder()
            .setAllowRecursiveKeys(true)
            .setAllowNonScalarKeys(true)
            .build());
        return load.loadFromString(data);
    }
}
