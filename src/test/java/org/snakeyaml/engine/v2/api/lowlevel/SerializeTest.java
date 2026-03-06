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
package org.snakeyaml.engine.v2.api.lowlevel;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.events.DocumentEndEvent;
import org.snakeyaml.engine.v2.events.DocumentStartEvent;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.ImplicitTuple;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.StreamEndEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.util.TestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP_SETTINGS;

@org.junit.jupiter.api.Tag("fast")
class SerializeTest {

    @Test
    void serializeOneScalar() {
        var serialize = new Serialize(DEFAULT_DUMP_SETTINGS);
        Iterable<Event> events = serialize.serializeOne(new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN));
        var list = new ArrayList<Event>();
        events.forEach(list::add);
        assertEquals(5, list.size());
        TestUtils.compareEvents(List.of(
            new StreamStartEvent(),
            new DocumentStartEvent(false, SpecVersion.EMPTY, new HashMap<>()),
            new ScalarEvent(null, null, ImplicitTuple.FALSE_FALSE, "a", ScalarStyle.PLAIN),
            new DocumentEndEvent(false), new StreamEndEvent()
        ), list);
    }
}
