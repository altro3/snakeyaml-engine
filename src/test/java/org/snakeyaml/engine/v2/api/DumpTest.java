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
import org.snakeyaml.engine.v2.util.StreamToStringWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

@Tag("fast")
class DumpTest {

    @Test
    @DisplayName("Dump string")
    void dumpString() {
        var settings = DumpSettings.builder().build();
        var dump = new Dump(settings);
        String str = dump.dumpToString("a");
        assertEquals("a\n", str);
    }

    @Test
    @DisplayName("Dump int")
    void dumpInteger() {
        String str = DEFAULT_DUMP.dumpToString(1);
        assertEquals("1\n", str);
    }

    @Test
    @DisplayName("Dump boolean")
    void dumpBoolean() {
        String str = DEFAULT_DUMP.dumpToString(Boolean.TRUE);
        assertEquals("true\n", str);
    }

    @Test
    @DisplayName("Dump seq")
    void dumpSequence() {
        String str = DEFAULT_DUMP.dumpToString(List.of(2, "a", Boolean.TRUE));
        assertEquals("[2, a, true]\n", str);
    }

    @Test
    @DisplayName("Dump map")
    void dumpMapping() {
        var map = new LinkedHashMap<String, Object>() {{
            put("x", 1);
            put("y", 2);
            put("z", 3);
        }};
        String output = DEFAULT_DUMP.dumpToString(map);
        assertEquals("{x: 1, y: 2, z: 3}\n", output);
    }

    @Test
    @DisplayName("Dump all instances")
    void dumpAll() {
        var list = new ArrayList<>() {{
            add("a");
            add(null);
            add(Boolean.TRUE);
        }};
        var streamToStringWriter = new StreamToStringWriter();
        DEFAULT_DUMP.dumpAll(list.iterator(), streamToStringWriter);
        assertEquals("""
            a
            --- null
            --- true
            """, streamToStringWriter.toString());
        // load back
        for (Object obj : DEFAULT_LOAD.loadAllFromString(streamToStringWriter.toString())) {
            assertEquals(list.remove(0), obj);
        }
    }

    @Test
    @DisplayName("Dump all instances")
    void dumpAllToString() {
        var list = new ArrayList<>() {{
            add("a");
            add(null);
            add(Boolean.TRUE);
        }};
        String output = DEFAULT_DUMP.dumpAllToString(list.iterator());
        assertEquals("""
            a
            --- null
            --- true
            """, output);
        // load back
        for (Object obj : DEFAULT_LOAD.loadAllFromString(output)) {
            assertEquals(list.remove(0), obj);
        }
    }

    @Test
    @DisplayName("Dump to File")
    void dumpToFile() throws IOException {
        File file = null;
        try {
            file = Files.createTempFile("snakeyaml-test", "dump-to-file").toFile().getCanonicalFile();
        } catch (IOException e) {
            fail("Unable to create temporary file for output");
        }
        file.deleteOnExit();
        var writer = new YamlOutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8) {
            @Override
            public void processIOException(IOException e) {
                throw new RuntimeException(e);
            }
        };
        DEFAULT_DUMP.dump(Map.of("x", 1, "y", 2, "z", 3), writer);
        assertTrue(file.exists());
    }
}
