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
package org.snakeyaml.engine.issues.issue149;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.lowlevel.Parse;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.util.TestUtils;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD_SETTINGS;

@org.junit.jupiter.api.Tag("fast")
class GlobalDirectivesTest {

    @Test
    @DisplayName("Use tag directive")
    void testOneDocument() {
        Iterable<Event> events = yamlToEvents("issues/issue149-one-document.yaml");
        final var counter = new AtomicInteger(0);
        events.forEach(event -> counter.incrementAndGet());

        assertEquals(10, counter.get());
    }

    @Test
    @DisplayName("Fail to parse because directive does not stay for the second document")
    void testDirectives() {
        Iterable<Event> events = yamlToEvents("issues/issue149-losing-directives.yaml");
        final var counter = new AtomicInteger(0);
        try {
            events.forEach(event -> counter.incrementAndGet());
        } catch (ParserException e) {
            assertTrue(e.getMessage().contains("found undefined tag handle !u!"), e.getMessage());
        }
    }

    @Test
    @DisplayName("Parse both tag directives")
    void testDirectives2() {
        Iterable<Event> events = yamlToEvents("issues/issue149-losing-directives-2.yaml");
        final var counter = new AtomicInteger(0);
        events.forEach(event -> counter.incrementAndGet());

        assertEquals(18, counter.get());
    }

    private Iterable<Event> yamlToEvents(final String resourceName) {
        InputStream input = TestUtils.getResourceAsStream(resourceName);
        var parser = new Parse(DEFAULT_LOAD_SETTINGS);
        return parser.parseInputStream(input);
    }
}
