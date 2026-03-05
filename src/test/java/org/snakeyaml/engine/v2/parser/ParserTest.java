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
package org.snakeyaml.engine.v2.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.scanner.ScannerImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD_SETTINGS;

@org.junit.jupiter.api.Tag("fast")
class ParserTest {

    @Test
    @DisplayName("Expected NoSuchElementException after all the events are finished.")
    void testToString() {
        var reader = new StreamReader(DEFAULT_LOAD_SETTINGS, "444333");
        var scanner = new ScannerImpl(DEFAULT_LOAD_SETTINGS, reader);
        var parser = new ParserImpl(DEFAULT_LOAD_SETTINGS, scanner);
        assertTrue(parser.hasNext());
        assertEquals(Event.Id.StreamStart, parser.next().getEventId());
        assertTrue(parser.hasNext());
        assertEquals(Event.Id.DocumentStart, parser.next().getEventId());
        assertTrue(parser.hasNext());
        assertEquals(Event.Id.Scalar, parser.next().getEventId());
        assertTrue(parser.hasNext());
        assertEquals(Event.Id.DocumentEnd, parser.next().getEventId());
        assertTrue(parser.hasNext());
        assertEquals(Event.Id.StreamEnd, parser.next().getEventId());
        assertFalse(parser.hasNext());

        var e = assertThrows(NoSuchElementException.class, parser::next);
        assertEquals("No more Events found.", e.getMessage());
    }
}

