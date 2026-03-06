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
package org.snakeyaml.engine.usecases.untrusted;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ByteLimitTest {

    @Test
    @DisplayName("Limit a single document")
    void testSetCodePointLimit() {
        var load = new Load(LoadSettings.builder()
            .setCodePointLimit(15)
            .build());
        var e = assertThrows(Exception.class, () -> load.loadFromString("12345678901234567890"));
        assertEquals("The incoming YAML document exceeds the limit: 15 code points.", e.getMessage());
    }

    @Test
    void testLoadAll553() {
        var load = new Load(LoadSettings.builder()
            .setCodePointLimit(15)
            .build());
        var e = assertThrows(Exception.class, () -> load.loadAllFromString("12345678901234567890").iterator().next());
        assertEquals("The incoming YAML document exceeds the limit: 15 code points.", e.getMessage());
    }

    @Test
    void testLoadManyDocuments() {
        var load = new Load(LoadSettings.builder()
            .setCodePointLimit(8)
            .build());
        var iter = load.loadAllFromString("---\nfoo\n---\nbar\n---\nyep").iterator();
        assertEquals("foo", iter.next());
        assertEquals("bar", iter.next());
        assertEquals("yep", iter.next());
        assertFalse(iter.hasNext());
    }
}
