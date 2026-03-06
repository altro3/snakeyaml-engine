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
package org.snakeyaml.engine.v2.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

@Tag("fast")
class LoadTest {

    @Test
    @DisplayName("String 'a' is parsed")
    void parseString() {
        var str = (String) DEFAULT_LOAD.loadFromString("a");
        assertEquals("a", str);
    }

    @Test
    @DisplayName("Integer 1 is parsed")
    void parseInteger() {
        var integer = (Integer) DEFAULT_LOAD.loadFromString("1");
        assertEquals(Integer.valueOf(1), integer);
    }

    @Test
    @DisplayName("Boolean true is parsed")
    void parseBoolean() {
        assertTrue((Boolean) DEFAULT_LOAD.loadFromString("true"));
    }

    @Test
    @DisplayName("null is parsed")
    void parseNull() {
        assertNull(DEFAULT_LOAD.loadFromString(""));
    }

    @Test
    @DisplayName("null tag is parsed")
    void parseNullTag() {
        assertNull(DEFAULT_LOAD.loadFromString("!!null"));
    }

    @Test
    @DisplayName("Float is parsed")
    void parseFloat() {
        var doubleValue = (Double) DEFAULT_LOAD.loadFromString("1.01");
        assertEquals(1.01, doubleValue);
    }

    @Test
    @DisplayName("Load from InputStream")
    void loadFromInputStream() {
        var v = (String) DEFAULT_LOAD.loadFromInputStream(new ByteArrayInputStream("aaa".getBytes()));
        assertEquals("aaa", v);
    }

    @Test
    @DisplayName("Load from Reader")
    void loadFromReader() {
        var v = (String) DEFAULT_LOAD.loadFromReader(new StringReader("bbb"));
        assertEquals("bbb", v);
    }

    @Test
    @DisplayName("Load all from String")
    void loadAllFromString() {
        var input = new ByteArrayInputStream("bbb\n---\nccc\n---\nddd".getBytes());
        Iterable<Object> v = DEFAULT_LOAD.loadAllFromInputStream(input);
        Iterator<Object> iter = v.iterator();
        assertTrue(iter.hasNext());
        Object o1 = iter.next();
        assertEquals("bbb", o1);

        assertTrue(iter.hasNext());
        Object o2 = iter.next();
        assertEquals("ccc", o2);

        assertTrue(iter.hasNext());
        Object o3 = iter.next();
        assertEquals("ddd", o3);

        assertFalse(iter.hasNext());
    }

    @Test
    @DisplayName("Load all from String")
    void loadIterableFromString() {
        Iterable<Object> v = DEFAULT_LOAD.loadAllFromString("1\n---\n2\n---\n3");
        int counter = 1;
        for (Object o : v) {
            // System.out.println("O: " + o);
            assertEquals(counter, o);
            counter++;
        }
        assertEquals(4, counter);
    }

    @Test
    @DisplayName("Load all from String which has only 1 document")
    void loadIterableFromString2() {

        Iterable<Object> iterable = DEFAULT_LOAD.loadAllFromString("1\n");
        int counter = 1;
        for (Object o : iterable) {
            // System.out.println("O: " + o);
            assertEquals(counter, o);
            counter++;
        }
        assertEquals(2, counter);

        Iterator<Object> iter = DEFAULT_LOAD.loadAllFromString("1\n").iterator();
        iter.hasNext();
        Object o1 = iter.next();
        assertEquals(1, o1);
        assertFalse(iter.hasNext());
    }

    @Test
    @DisplayName("Load all from Reader")
    void loadAllFromReader() {
        Iterable<Object> v = DEFAULT_LOAD.loadAllFromReader(new StringReader("bbb"));
        Iterator<Object> iter = v.iterator();
        assertTrue(iter.hasNext());
        Object o1 = iter.next();
        assertEquals("bbb", o1);
        assertFalse(iter.hasNext());
    }

    @Test
    @DisplayName("Load a lot of documents from the same Load instance (not recommended)")
    void loadManyFromTheSameInstance() {
        for (int i = 0; i < 100000; i++) {
            Iterable<Object> v = DEFAULT_LOAD.loadAllFromReader(new StringReader("{foo: bar, list: [1, 2, 3]}"));
            Iterator<Object> iter = v.iterator();
            assertTrue(iter.hasNext());
            Object o1 = iter.next();
            assertNotNull(o1);
            assertFalse(iter.hasNext());
        }
    }

    @Test
    @DisplayName("Throw UnsupportedOperationException if try to remove from iterator")
    void loadAllFromStringWithUnsupportedOperationException() {
        Iterable<Object> v = DEFAULT_LOAD.loadAllFromString("bbb");
        var exception = assertThrows(UnsupportedOperationException.class, () -> v.iterator().remove());
        assertEquals("Removing is not supported.", exception.getMessage());
    }
}
