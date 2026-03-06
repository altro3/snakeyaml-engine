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
package org.snakeyaml.engine.usecases.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.util.TestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

@org.junit.jupiter.api.Tag("fast")
class ParseJsonTest {

    /**
     * <a href="https://bitbucket.org/snakeyaml/snakeyaml/issues/1110/exception-during-parse-of-tab-idented-json">cf-app</a>
     */
    @Test
    @DisplayName("Parse JSON with TABs")
    void testJsonWithTabs() {
        String str = TestUtils.getResource("json/mtad.yaml");
        @SuppressWarnings("unchecked")
        var obj = (Map<String, Object>) DEFAULT_LOAD.loadFromString(str);
        assertEquals(4, obj.size());
        assertTrue(obj.containsKey("_schema-version"));
    }

    @Test
    @DisplayName("Parse JSON with TABs, small")
    void testJsonWithTabsSmall() {
        String str = TestUtils.getResource("json/leading-tab.yaml");
        @SuppressWarnings("unchecked")
        Map<String, Object> obj = (Map<String, Object>) DEFAULT_LOAD.loadFromString(str);
        assertEquals(3, obj.size());
        assertTrue(obj.containsKey("modules"));
    }
}
