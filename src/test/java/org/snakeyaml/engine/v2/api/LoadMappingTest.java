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
package org.snakeyaml.engine.v2.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.util.TestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("fast")
class LoadMappingTest {

    @Test
    @DisplayName("Empty map {} is parsed")
    void parseEmptyMap() {
        var settings = LoadSettings.builder().build();
        var load = new Load(settings);
        var map = (Map<Integer, Integer>) load.loadFromString("{}");
        assertEquals(LoadSettings.builder().build().defaultMap().apply(0), map);
    }

    @Test
    @DisplayName("map {a: 1} is parsed")
    void parseMap1() {
        var settings = LoadSettings.builder().build();
        var load = new Load(settings);
        var map = (Map<String, Integer>) load.loadFromString("{a: 1}");
        var expected = Map.of("a", 1);
        assertEquals(expected, map);
    }

    @Test
    @DisplayName("map {a: 1, b: 2} is parsed")
    void parseMap2() {
        var settings = LoadSettings.builder().build();
        var load = new Load(settings);
        var map = (Map<String, Object>) load.loadFromString("a: 1\nb: 2\nc:\n  - aaa\n  - bbb");
        var expected = Map.of("a", 1, "b", 2, "c", List.of("aaa", "bbb"));
        assertEquals(expected, map);
        // assertEquals("{a=1, b=2, c=[aaa, bbb]}", map.toString());
    }

    @Test
    @DisplayName("map {x: 1, y: 2, z:3} is parsed")
    void parseMap3() {
        var settings = LoadSettings.builder().build();
        var load = new Load(settings);
        var map = (Map<String, Integer>) load.loadFromString(TestUtils.getResource("load/map1.yaml"));
        var expected = Map.of("x", 1, "y", 2, "z", 3);
        assertEquals(expected, map);
    }

}
