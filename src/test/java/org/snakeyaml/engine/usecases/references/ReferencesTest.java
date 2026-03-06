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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP;

@Tag("fast")
class ReferencesTest {

    @Test
    void referencesWithRecursiveKeysNotAllowedByDefault() {
        String output = createDump(30);
        // System.out.println(output);
        long time1 = System.currentTimeMillis();
        // Load
        var load = new Load(LoadSettings.builder()
            .setMaxAliasesForCollections(150)
            .setAllowNonScalarKeys(true)
            .build());

        var e = assertThrows(Exception.class, () -> load.loadFromString(output));
        assertEquals("Recursive key for mapping is detected but it is not configured to be allowed.", e.getMessage());
        long time2 = System.currentTimeMillis();
        var duration = (time2 - time1) / 1000F;
        assertTrue(duration < 1, "It should fail quickly. Time was " + duration + " seconds.");
    }

    @Test
    @DisplayName("Parsing with aliases may take a lot of time, CPU and memory")
    void parseManyAliasesForCollections() {
        String output = createDump(25);
        // Load
        long time1 = System.currentTimeMillis();
        var load = new Load(LoadSettings.builder()
            .setAllowRecursiveKeys(true)
            .setMaxAliasesForCollections(50)
            .setAllowNonScalarKeys(true)
            .build());
        load.loadFromString(output);
        long time2 = System.currentTimeMillis();
        var duration = (time2 - time1) / 1000D;
        int cores = Runtime.getRuntime().availableProcessors();
        double minDuration = 0.7;
        if (cores > 4) {
            minDuration = 0.4;
        }
        assertTrue(duration > minDuration, "It should take time. Time was " + duration + " seconds.");
        assertTrue(duration < 9, "Time was " + duration + " seconds.");
    }

    @Test
    @DisplayName("Prevent DoS attack by failing early")
    void referencesWithRestrictedAliases() {
        // without alias restriction this size should occupy tons of CPU, memory and time to parse
        String bigYaml = createDump(35);
        // Load
        long time1 = System.currentTimeMillis();
        var load = new Load(LoadSettings.builder()
            .setAllowRecursiveKeys(true)
            .setMaxAliasesForCollections(40)
            .setAllowNonScalarKeys(true)
            .build());
        var e = assertThrows(Exception.class, () -> load.loadFromString(bigYaml));
        assertEquals("Number of aliases for non-scalar nodes exceeds the specified max=40", e.getMessage());
        long time2 = System.currentTimeMillis();
        var duration = (time2 - time1) / 1000F;
        assertTrue(duration < 1, "It should fail quickly. Time was " + duration + " seconds.");
    }

    /**
     * Create data which is difficult to parse.
     *
     * @param size size of the map, defines the complexity
     * @return YAML to parse
     */
    private String createDump(int size) {
        var root = new LinkedHashMap<>();
        Map<Object, Object> s1;
        Map<Object, Object> s2;
        Map<Object, Object> t1;
        Map<Object, Object> t2;
        s1 = root;
        s2 = new LinkedHashMap<>();
        /*
         * the time to parse grows very quickly SIZE -> time to parse in seconds 25 -> 1 26 -> 2 27 -> 3
         * 28 -> 8 29 -> 13 30 -> 28 31 -> 52 32 -> 113 33 -> 245 34 -> 500
         */
        for (int i = 0; i < size; i++) {

            t1 = new LinkedHashMap<>();
            t2 = new LinkedHashMap<>();
            t1.put("foo", "1");
            t2.put("bar", "2");

            s1.put("a", t1);
            s1.put("b", t2);
            s2.put("a", t1);
            s2.put("b", t2);

            s1 = t1;
            s2 = t2;
        }

        // this is VERY BAD code
        // the map has itself as a key (no idea why it may be used except of a DoS attack)
        var f = new LinkedHashMap<>();
        f.put(f, "a");
        f.put("g", root);

        String output = DEFAULT_DUMP.dumpToString(f);
        // TODO no replace should be needed
        return output.replace("001: ", "001 : ");
    }
}
