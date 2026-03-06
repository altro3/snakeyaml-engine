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
package org.snakeyaml.engine.usecases.references;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

@org.junit.jupiter.api.Tag("fast")
class NonAsciiAnchorTest {

    private final String NON_ANCHORS = ",[]{}*&./";

    @Test
    @DisplayName("Non ASCII anchor name must be accepted")
    void testNonAsciiAnchor() {
        var floatValue = (String) DEFAULT_LOAD.loadFromString("&something_タスク タスク");
        assertEquals("タスク", floatValue);
    }

    @Test
    void testUnderscore() {
        Object value = DEFAULT_LOAD.loadFromString("&_ タスク");
        assertEquals("タスク", value);
    }

    @Test
    void testSmile() {
        Object value = DEFAULT_LOAD.loadFromString("&\uD83D\uDE01 v1");
        // System.out.println("&\uD83D\uDE01 v1");
        assertEquals("v1", value);
    }

    @Test
    void testAlpha() {
        Object value = DEFAULT_LOAD.loadFromString("&kääk v1");
        assertEquals("v1", value);
    }

    @Test
    @DisplayName("Reject invalid anchors which contain one of " + NON_ANCHORS)
    void testNonAllowedAnchor() {
        for (var c : NON_ANCHORS.toCharArray()) {
            var e = assertThrows(Exception.class, () -> loadWith(c));
            assertTrue(e.getMessage().contains("while scanning an anchor"), e.getMessage());
            assertTrue(e.getMessage().contains("unexpected character found"), e.getMessage());
        }
    }

    private void loadWith(char c) {
        DEFAULT_LOAD.loadFromString("&" + c + " value");
    }
}
