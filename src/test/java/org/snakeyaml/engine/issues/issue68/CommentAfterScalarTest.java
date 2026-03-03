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
package org.snakeyaml.engine.issues.issue68;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.nodes.Node;

@org.junit.jupiter.api.Tag("fast")
public class CommentAfterScalarTest {

    private final LoadSettings loadSettings = LoadSettings.builder().setParseComments(true).build();

    @Test
    @DisplayName("Respect inline comment for '!!str # comment'")
    void testInLineCommentForScalarNode() {
        var compose = new Compose(loadSettings);
        Node node = compose.composeString("!!str # comment");
        assertNotNull(node);
        assertEquals(1, node.getInLineComments().size());
        assertEquals(" comment", node.getInLineComments().stream().findFirst().get().value());
    }

    @Test
    @DisplayName("Respect inline and block comments for '!!str # comment\n# block comment1'")
    void testInLineCommentForScalarNode2() {
        var compose = new Compose(loadSettings);
        Node node = compose.composeString("!!str # comment\n# block comment1\n# block comment2");
        assertNotNull(node);
        assertEquals(1, node.getInLineComments().size());
        assertEquals(" comment", node.getInLineComments().stream().findFirst().get().value());
        assertEquals(2, node.getBlockComments().size());
        assertEquals(" block comment1", node.getBlockComments().stream().findFirst().get().value());
        assertEquals(" block comment2",
            node.getBlockComments().stream().skip(1).findFirst().get().value());
    }
}
