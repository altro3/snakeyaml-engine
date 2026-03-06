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
package org.snakeyaml.engine.usecases.tags;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

/**
 * Example of parsing a local tag
 */
@org.junit.jupiter.api.Tag("fast")
class SetsTagTest {

    @Test
    @DisplayName("Test that !!set tag creates a Set")
    void testSetsTag() {
        final String yaml = """
            ---
            sets: !!set
                ? a
                ? b
            """;
        @SuppressWarnings("unchecked")
        var map = (Map<String, Set<String>>) DEFAULT_LOAD.loadFromString(yaml);
        Set<String> set = map.get("sets");
        assertEquals(2, set.size());
        var iter = set.iterator();
        assertEquals("a", iter.next());
        assertEquals("b", iter.next());
    }
}
