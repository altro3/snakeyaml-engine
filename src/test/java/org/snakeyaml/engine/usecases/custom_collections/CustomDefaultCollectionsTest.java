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
package org.snakeyaml.engine.usecases.custom_collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
class CustomDefaultCollectionsTest {

    @Test
    @DisplayName("Create LinkedList by default")
    void createLinkedListByDefault() {
        // init size is not used in LinkedList
        var load = new Load(LoadSettings.builder()
            .setDefaultList(initSize -> new LinkedList<>())
            .build());
        @SuppressWarnings("unchecked")
        var list = (LinkedList<String>) load.loadFromString("- a\n- b");
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("Create TreeMap by default")
    void createTreeMapByDefault() {
        // init size is not used in TreeMap
        var load = new Load(LoadSettings.builder()
            .setDefaultMap(initSize -> new TreeMap<>())
            .build());
        @SuppressWarnings("unchecked")
        var map = (TreeMap<String, String>) load.loadFromString("{k1: v1, k2: v2}");
        assertEquals(2, map.size());
    }

    @Test
    @DisplayName("Create TreeSet by default")
    void createTreeSetByDefault() {
        var load = new Load(LoadSettings.builder()
            .setDefaultSet(initSize -> new TreeSet<>())
            .build());
        @SuppressWarnings("unchecked")
        var set = (TreeSet<String>) load.loadFromString("!!set\n? foo\n? bar");
        assertEquals(2, set.size());
        // must be re-ordered
        assertEquals("bar", set.first());
        assertEquals("foo", set.last());
    }
}

