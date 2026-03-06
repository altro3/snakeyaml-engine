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
package org.snakeyaml.engine.usecases.colon_in_flow_context;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

@org.junit.jupiter.api.Tag("fast")
class ColonInFlowContextInMapTest {

    @Test
    void withSeparation() {
        @SuppressWarnings("unchecked")
        var map = (Map<String, Integer>) DEFAULT_LOAD.loadFromString("{a: 1}");
        assertEquals(1, map.get("a"));
    }

    @Test
    void withoutEmptyValue() {
        @SuppressWarnings("unchecked")
        var map = (Map<String, Integer>) DEFAULT_LOAD.loadFromString("{a:}");
        assertTrue(map.containsKey("a"));
    }

    @Test
    void withoutSeparation() {
        @SuppressWarnings("unchecked")
        var map = (Map<String, Integer>) DEFAULT_LOAD.loadFromString("{a:1}");
        assertTrue(map.containsKey("a:1"));
    }
}


