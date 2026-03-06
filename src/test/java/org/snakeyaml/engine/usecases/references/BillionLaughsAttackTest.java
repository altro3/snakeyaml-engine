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
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

/**
 * <a href="https://en.wikipedia.org/wiki/Billion_laughs_attack#Variations">link</a>
 */
@Tag("fast")
class BillionLaughsAttackTest {

    static final String data = """
        a: &a ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
        b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]
        c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]
        d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]
        e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]
        f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]
        g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]
        h: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]
        i: &i [*h,*h,*h,*h,*h,*h,*h,*h,*h]""";

    static final String scalarAliasesData = """
        a: &a foo
        b:  *a
        c:  *a
        d:  *a
        e:  *a
        f:  *a
        g:  *a
        """;

    @Test
    @DisplayName("Load many aliases if explicitly allowed")
    void billionLaughsAttackLoaded() {
        var load = new Load(LoadSettings.builder()
            .setMaxAliasesForCollections(72)
            .build());
        var map = (Map<?, ?>) load.loadFromString(data);
        assertNotNull(map);
    }

    @Test
    @DisplayName("Billion_laughs_attack if data expanded")
    void billionLaughsAttackExpanded() {
        var load = new Load(LoadSettings.builder()
            .setMaxAliasesForCollections(100)
            .build());
        var map = (Map<?, ?>) load.loadFromString(data);
        assertNotNull(map);
        var e = assertThrows(Throwable.class, map::toString);
        assertTrue(e.getMessage().contains("heap"));
    }

    @Test
    @DisplayName("Prevent Billion_laughs_attack by default")
    void billionLaughsAttackWithRestrictedAliases() {
        var e = assertThrows(YamlEngineException.class, () -> DEFAULT_LOAD.loadFromString(data));
        assertEquals("Number of aliases for non-scalar nodes exceeds the specified max=50", e.getMessage());
    }

    @Test
    @DisplayName("Number of aliases for scalar nodes is not restricted")
    void doNotRestrictScalarAliases() {
        // smaller than number of aliases for scalars
        var load = new Load(LoadSettings.builder()
            .setMaxAliasesForCollections(5)
            .build());
        load.loadFromString(scalarAliasesData);
    }
}
