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
package org.snakeyaml.engine.v2.api.types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.nodes.Node;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_REPRESENTER;

@org.junit.jupiter.api.Tag("fast")
class OptionalTest {

    @Test
    @DisplayName("Represent Optional as value")
    void representOptional() {
        Node node = DEFAULT_REPRESENTER.represent(Optional.of("a"));
        assertEquals("tag:yaml.org,2002:java.util.Optional", node.getTag().getValue());
    }

    @Test
    @DisplayName("Represent Optional.empty as null")
    void representEmptyOptional() {
        Node node = DEFAULT_REPRESENTER.represent(Optional.empty());
        assertEquals("tag:yaml.org,2002:null", node.getTag().getValue());
    }

    @Test
    @DisplayName("Dump Optional as its value")
    void dumpOptional() {
        String str = DEFAULT_DUMP.dumpToString(Optional.of("a"));
        assertEquals("!!java.util.Optional 'a'\n", str);
    }

    @Test
    @DisplayName("Dump empty Optional as null")
    void dumpEmptyOptional() {
        String str = DEFAULT_DUMP.dumpToString(Optional.empty());
        assertEquals("null\n", str);
    }

    @Test
    @DisplayName("Dump Optionals")
    void dumpListOfOptional() {
        String str = DEFAULT_DUMP.dumpToString(List.of(Optional.of(2), Optional.empty(), Optional.of("a")));
        assertEquals("[!!java.util.Optional '2', null, !!java.util.Optional 'a']\n", str);
    }

    @Test
    @DisplayName("Dump Optionals")
    void dumpListOfOptional2() {
        String str = DEFAULT_DUMP.dumpToString(Optional.of(List.of(1, 2)));
        assertEquals("!!java.util.Optional [1, 2]\n", str);
    }

    // parse
    @Test
    @DisplayName("Optional 'a' is parsed")
    void parseOptional() {
        var str = (Optional<String>) DEFAULT_LOAD.loadFromString("!!java.util.Optional a");
        assertEquals(Optional.of("a"), str);
    }

    @Test
    @DisplayName("Empty Optional parsed")
    void parseEmptyOptional() {
        var str = (Optional<String>) DEFAULT_LOAD.loadFromString("!!java.util.Optional null");
        assertEquals(Optional.empty(), str);
    }

    @Test
    @DisplayName("Empty Optional parsed")
    void parseEmptyOptional2() {
        var str = (Optional<String>) DEFAULT_LOAD.loadFromString("!!java.util.Optional ");
        assertEquals(Optional.empty(), str);
    }
}
