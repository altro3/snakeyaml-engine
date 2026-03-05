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
package org.snakeyaml.engine.v2.representer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for <a href="https://bitbucket.org/snakeyaml/snakeyaml-engine/issues/9/indentation-before-sequence">issue</a>
 */
@Tag("fast")
class IndentationTest {

    @Test
    @DisplayName("Dump block map seq with default indent settings")
    void dumpBlockMappingSequenceWithDefaultSettings() {
        Dump dump = createDump(0);
        String output = dump.dumpToString(createMap());
        assertEquals("""
                key1:
                - value1
                - value2
                key2:
                - value3
                - value4
                """,
            output);
    }

    @Test
    @DisplayName("Dump block seq map with default indent settings")
    void dumpBlockSequenceMappingWithDefaultSettings() {
        Dump dump = createDump(0);
        String output = dump.dumpToString(createSequence());
        assertEquals("""
                - key1: value1
                  key2: value2
                - key3: value3
                  key4: value4
                """,
            output);
    }

    @Test
    @DisplayName("Dump block seq map with specified indicator indent")
    void dumpBlockMappingSequence() {
        Dump dump = createDump(2);
        String output = dump.dumpToString(createMap());
        assertEquals(
            """
                key1:
                  - value1
                  - value2
                key2:
                  - value3
                  - value4
                """,
            output);
    }

    @Test
    @DisplayName("Dump block seq map with indicatorIndent=2")
    void dumpBlockSequenceMapping() {
        Dump dump = createDump(2);
        String output = dump.dumpToString(createSequence());
        assertEquals(
            """
                  - key1: value1
                    key2: value2
                  - key3: value3
                    key4: value4
                """,
            output);
    }

    private Map<Object, Object> createMap() {
        var mapping = new LinkedHashMap<>();
        mapping.put("key1", List.of("value1", "value2"));
        mapping.put("key2", List.of("value3", "value4"));
        return mapping;
    }

    private List<Object> createSequence() {
        var mapping1 = new LinkedHashMap<>();
        mapping1.put("key1", "value1");
        mapping1.put("key2", "value2");
        var mapping2 = new LinkedHashMap<>();
        mapping2.put("key3", "value3");
        mapping2.put("key4", "value4");
        return List.of(mapping1, mapping2);
    }

    private Dump createDump(int indicatorIndent) {
        return new Dump(DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndicatorIndent(indicatorIndent)
            .setIndent(indicatorIndent + 2)
            .build());
    }
}
