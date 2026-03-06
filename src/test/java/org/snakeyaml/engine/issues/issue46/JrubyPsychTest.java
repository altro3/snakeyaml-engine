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
package org.snakeyaml.engine.issues.issue46;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

/**
 * <a href="https://github.com/jruby/jruby/issues/7698">link</a>
 */
class JrubyPsychTest {

    @Test
    @DisplayName("Issue 46: parse different values")
    void parseDifferentValues() {
        parse("\u2029", "\n \u2029");
        parse("\u2029", "\n\u2029");
        parse("\u2028", "\n \u2028");
        parse("\u2028", "\n\u2028");
        parse("\u2029 1", "\n\u2029 1");

        parse("\u2029*", "\n\u2029* "); // empty alias
        parse("\u2029*", "\n\u2029*"); // empty alias
        parse("\u2029* 1", "\n\u2029* 1");
    }

    @Test
    @DisplayName("Issue 46: parse document where 2028 is used as leading space (3rd)")
    void parseValid() {
        var iter = (Iterable<?>) DEFAULT_LOAD.loadAllFromString("--- |2-\n\n\u2028  * C\n");
        assertNotNull(iter);
        Object doc = iter.iterator().next();
        assertNotNull(doc);
    }

    @Test
    @DisplayName("Issue 46: parse document")
    void parseInvalid2() {
        var iter = (Iterable<?>) DEFAULT_LOAD.loadAllFromString("--- |2-\n\n  \u2028* C\n");
        assertNotNull(iter);
        Object doc = iter.iterator().next();
        assertEquals("\n\u2028* C", doc);
    }


    private void parse(Object expected, String data) {
        Object obj = DEFAULT_LOAD.loadFromString(data);
        assertEquals(expected, obj);
    }

    @Test
    @DisplayName("Issue 46: * is not alias after 2028")
    void failToParseInvalid() {
        var iter = (Iterable<?>) DEFAULT_LOAD.loadAllFromString("\n\u2028* C");
        for (Object o : iter) {
            assertEquals("\u2028* C", o);
        }
    }

    @Test
    @DisplayName("Issue 46: use anchor instead of alias")
    void parse2028_1() {
        var iter = (Iterable<?>) DEFAULT_LOAD.loadAllFromString("\n\u2028&C");
        for (Object o : iter) {
            assertEquals("\u2028&C", o);
        }
    }
}
