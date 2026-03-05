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
package org.snakeyaml.engine.issues.issue25;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@org.junit.jupiter.api.Tag("fast")
class DumpToStringTest {

    @Test
    @DisplayName("If Dump instance is called more then once then the results are not predictable.")
    void dumpToStringTwice() {
        var dump = new Dump(DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .build());

        class Something {

            final int doesntMatter = 0;
        }

        var something = new Something();
        var data = new LinkedHashMap<String, Object>();
        data.put("before", "bla");
        data.put("nested", something);

        var e = assertThrows(YamlEngineException.class, () -> dump.dumpToString(data));
        assertEquals("Representer is not defined for class org.snakeyaml.engine.issues.issue25.DumpToStringTest$1Something", e.getMessage());
        String output = dump.dumpToString(data);
        // System.out.print("actual " + output);
        assertEquals("before: bla\n", output);
    }
}
