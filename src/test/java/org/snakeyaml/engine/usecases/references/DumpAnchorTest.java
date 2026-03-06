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

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.util.StreamToStringWriter;
import org.snakeyaml.engine.v2.util.TestUtils;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
class DumpAnchorTest {

    @Test
    void test_anchor_test() {
        var str = TestUtils.getResource("anchor/issue481.yaml");
        var compose = new Compose(LoadSettings.builder().build());
        Node node = compose.composeReader(new StringReader(str));

        var yaml = new Dump(DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setAnchorGenerator(Node::getAnchor)
            .build());

        var writer = new StreamToStringWriter();
        yaml.dumpNode(node, writer);
        assertEquals(str, writer.toString());
    }
}
