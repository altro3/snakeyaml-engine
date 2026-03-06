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
package org.snakeyaml.engine.usecases.recursive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP_SETTINGS;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

@Tag("fast")
class RecursiveMapTest {

    @Test
    @DisplayName("Load map with recursive values")
    void loadRecursiveMap() {
        @SuppressWarnings("unchecked")
        var map = (Map<String, String>) DEFAULT_LOAD.loadFromString("""
            First occurrence: &anchor Foo
            Second occurrence: *anchor
            Override anchor: &anchor Bar
            Reuse anchor: *anchor
            """);
        assertEquals(Map.of(
            "First occurrence", "Foo",
            "Second occurrence", "Foo",
            "Override anchor", "Bar",
            "Reuse anchor", "Bar"
        ), map);
    }

    @Test
    @DisplayName("Dump and Load map with recursive values")
    void loadRecursiveMap2() {
        var map1 = new LinkedHashMap<String, Object>();
        map1.put("name", "first");
        var map2 = new LinkedHashMap<String, Object>();
        map2.put("name", "second");
        map1.put("next", map2);
        map2.put("next", map1);
        String output1 = new Dump(DumpSettings.builder().build()).dumpToString(map1);
        assertEquals("""
            &id001
            name: first
            next:
              name: second
              next: *id001
            """, output1);

        @SuppressWarnings("unchecked")
        var parsed1 = (Map<String, Object>) DEFAULT_LOAD.loadFromString(output1);
        assertEquals(2, parsed1.size());
        assertEquals("first", parsed1.get("name"));
        @SuppressWarnings("unchecked")
        var parsed2 = (Map<String, Object>) parsed1.get("next");
        assertEquals("second", parsed2.get("name"));
    }

    @Test
    @DisplayName("Fail to load map with recursive keys")
    void failToLoadRecursiveMapByDefault() {
        var load = new Load(LoadSettings.builder()
            .setAllowNonScalarKeys(true)
            .build());
        // fail to load map which has only one key - reference to itself
        var e = assertThrows(YamlEngineException.class, () -> load.loadFromString("&id002\n" + "*id002 : foo"));
        assertEquals("Recursive key for mapping is detected but it is not configured to be allowed.", e.getMessage());
    }

    @Test
    @DisplayName("Load map with recursive keys if it is explicitly allowed")
    void loadRecursiveMapIfAllowed() {
        var load = new Load(LoadSettings.builder()
            .setAllowRecursiveKeys(true)
            .setAllowNonScalarKeys(true)
            .build());
        // load map which has only one key - reference to itself
        @SuppressWarnings("unchecked")
        var recursive = (Map<Object, Object>) load.loadFromString("&id002\n" + "*id002 : foo");
        assertEquals(1, recursive.size());
    }
}
