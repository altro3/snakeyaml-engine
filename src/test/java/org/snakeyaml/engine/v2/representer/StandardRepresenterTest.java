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

import com.google.common.collect.TreeRangeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.SequenceNode;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP_SETTINGS;

@Tag("fast")
class StandardRepresenterTest {

    private final StandardRepresenter standardRepresenter = new StandardRepresenter(DEFAULT_DUMP_SETTINGS);

    @Test
    @DisplayName("Represent unknown class")
    void representUnknownClass() {
        var e = assertThrows(YamlEngineException.class, () -> standardRepresenter.represent(TreeRangeSet.create()));
        assertEquals("Representer is not defined for class com.google.common.collect.TreeRangeSet", e.getMessage());
    }

    @Test
    @DisplayName("Represent Enum as node with global tag")
    void representEnum() {
        Node node = standardRepresenter.represent(FormatEnum.JSON);
        assertEquals("tag:yaml.org,2002:org.snakeyaml.engine.v2.representer.FormatEnum", node.getTag().getValue());
    }

    @Test
    @DisplayName("Represent Iterator as node with global tag")
    void representIterator() {
        var listOfStrings = List.of("hello", "world");
        Iterator<String> iterator = listOfStrings.iterator();
        Node node = standardRepresenter.represent(iterator);
        assertEquals("tag:yaml.org,2002:seq", node.getTag().getValue());
        var seq = (SequenceNode) node;
        assertEquals(2, seq.getValue().size());
        seq.getValue().forEach(n -> assertEquals("tag:yaml.org,2002:str", n.getTag().getValue()));
        // dump
        assertEquals("[hello, world]\n", DEFAULT_DUMP.dumpToString(listOfStrings.iterator()));
    }

    @Test
    @DisplayName("Represent Set as node")
    void representSet() {
        var setOfStrings = new TreeSet<>(Set.of("bbb", "aaa"));
        Node node = standardRepresenter.represent(setOfStrings);
        assertEquals("tag:yaml.org,2002:set", node.getTag().getValue());
        var seq = (MappingNode) node;
        assertEquals(2, seq.getValue().size());
        // dump
        assertEquals("[aaa, bbb]\n", DEFAULT_DUMP.dumpToString(setOfStrings.iterator()));
    }
}
