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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_REPRESENTER;

@org.junit.jupiter.api.Tag("fast")
class UuidTest {

    private static final UUID THE_UUID = UUID.fromString("37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc");

    @Test
    @DisplayName("Represent UUID as node with global tag")
    void representUUID() {
        Node node = DEFAULT_REPRESENTER.represent(THE_UUID);
        assertEquals("tag:yaml.org,2002:java.util.UUID", node.getTag().getValue());
    }

    @Test
    @DisplayName("Dump UUID as string")
    void dumpUuid() {
        String output = DEFAULT_DUMP.dumpToString(THE_UUID);
        assertEquals("!!java.util.UUID '37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc'\n", output);
    }

    @Test
    @DisplayName("Parse UUID")
    void parseUuid() {
        var uuid = (UUID) DEFAULT_LOAD.loadFromString("!!java.util.UUID '37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc'\n");
        assertEquals(THE_UUID, uuid);
    }

    @Test
    @DisplayName("Parse UUID as root")
    void parseUuidAsRoot() {
        var uuid = (UUID) DEFAULT_LOAD.loadFromString("!!java.util.UUID '37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc'\n");
        assertEquals(THE_UUID, uuid);
    }
}
