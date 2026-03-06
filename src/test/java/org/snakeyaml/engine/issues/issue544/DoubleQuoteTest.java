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
package org.snakeyaml.engine.issues.issue544;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Present;
import org.snakeyaml.engine.v2.api.lowlevel.Serialize;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP_SETTINGS;

class DoubleQuoteTest {

    @Test
    void testSubstitution() {
        assertEquals("""
            double_quoted: "\\U0001f510This process is simple and secure."
            single_quoted: "\\U0001f510This process is simple and secure."
            """, emit(DumpSettings.builder()
            .setUseUnicodeEncoding(false)
            .build()));
    }

    @Test
    void testUnicode() {
        assertEquals("""
            double_quoted: "🔐This process is simple and secure."
            single_quoted: '🔐This process is simple and secure.'
            """, emit(DumpSettings.builder()
            .setUseUnicodeEncoding(true)
            .build()));
    }

    @Test
    void testDefault() {
        assertEquals("""
            double_quoted: "🔐This process is simple and secure."
            single_quoted: '🔐This process is simple and secure.'
            """, emit(DEFAULT_DUMP_SETTINGS));
    }

    private MappingNode create() {
        String content = "🔐This process is simple and secure.";

        var doubleQuotedKey = new ScalarNode(Tag.STR, "double_quoted", ScalarStyle.PLAIN);
        var doubleQuotedValue = new ScalarNode(Tag.STR, content, ScalarStyle.DOUBLE_QUOTED);
        var doubleQuotedTuple = new NodeTuple(doubleQuotedKey, doubleQuotedValue);

        var singleQuotedKey = new ScalarNode(Tag.STR, "single_quoted", ScalarStyle.PLAIN);
        var singleQuotedValue = new ScalarNode(Tag.STR, content, ScalarStyle.SINGLE_QUOTED);
        var singleQuotedTuple = new NodeTuple(singleQuotedKey, singleQuotedValue);

        var nodeTuples = List.of(doubleQuotedTuple, singleQuotedTuple);

        return new MappingNode(Tag.MAP, nodeTuples, FlowStyle.BLOCK);
    }

    private String emit(DumpSettings settings) {
        Iterable<Event> eventsIter = new Serialize(settings).serializeOne(create());
        return new Present(settings).emitToString(eventsIter.iterator());
    }
}
