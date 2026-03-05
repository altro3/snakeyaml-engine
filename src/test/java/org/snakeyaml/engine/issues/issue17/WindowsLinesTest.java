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
package org.snakeyaml.engine.issues.issue17;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

@org.junit.jupiter.api.Tag("fast")
public class WindowsLinesTest {

    @Test
    void parseWindowsNewLine() {
        @SuppressWarnings("unchecked")
        var list = (Map<String, String>) DEFAULT_LOAD.loadFromString("parent:\r\n  key: value");
        assertEquals(1, list.size());
        assertNotNull(list.get("parent"));
    }
}
