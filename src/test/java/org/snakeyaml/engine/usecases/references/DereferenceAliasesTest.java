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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.util.TestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

@Tag("fast")
class DereferenceAliasesTest {

    @Test
    void testNoAliases() {
        var map = (Map<?, ?>) DEFAULT_LOAD.loadFromString(TestUtils.getResource("issues/issue1086-1-input.yaml"));
        var dump = new Dump(DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setDereferenceAliases(true)
            .build());
        String node = dump.dumpToString(map);
        String expected = TestUtils.getResource("issues/issue1086-1-expected.yaml");
        assertEquals(expected, node);
    }

    @Test
    void testNoAliasesRecursive() {
        var map = (Map<?, ?>) DEFAULT_LOAD.loadFromString(TestUtils.getResource("issues/issue1086-2-input.yaml"));
        var dump = new Dump(DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setDereferenceAliases(true)
            .build());

        var e = assertThrows(YamlEngineException.class, () -> dump.dumpToString(map));
        assertEquals("Cannot dereferenceAliases for recursive structures.", e.getMessage());
    }
}

