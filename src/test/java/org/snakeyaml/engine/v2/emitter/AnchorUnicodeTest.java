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
package org.snakeyaml.engine.v2.emitter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.serializer.AnchorGenerator;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("fast")
class AnchorUnicodeTest {

    private static final Set<Character> INVALID_ANCHOR = Set.of(
        '[',
        ']',
        '{',
        '}',
        ',',
        '*',
        '&'
    );

    @Test
    void testUnicodeAnchor() {
        var settings = DumpSettings.builder()
            .setAnchorGenerator(new AnchorGenerator() {
                int id = 0;

                @Override
                public Anchor nextAnchor(Node node) {
                    return new Anchor("タスク" + id++);
                }
            })
            .build();
        var dump = new Dump(settings);
        var list = List.of("abc");
        var toExport = List.of(list, list);
        String output = dump.dumpToString(toExport);
        assertEquals("- &タスク0 [abc]\n- *タスク0\n", output);
    }

    @Test
    void testInvalidAnchor() {
        for (Character ch : INVALID_ANCHOR) {
            var dump = new Dump(createSettings(ch));
            var list = List.of("abc");
            var toExport = List.of(list, list);
            var e = assertThrows(Exception.class, () -> dump.dumpToString(toExport));
            assertEquals("Invalid character '" + ch + "' in the anchor: anchor" + ch, e.getMessage());
        }
    }

    @Test
    void testAnchors() {
        assertEquals("a", new Anchor("a").toString());
        assertEquals("Anchor may not contain spaces: a ", checkAnchor("a "));
        assertEquals("Anchor may not contain spaces: a \t", checkAnchor("a \t"));
        assertEquals("Invalid character '[' in the anchor: a[", checkAnchor("a["));
        assertEquals("Invalid character ']' in the anchor: a]", checkAnchor("a]"));
        assertEquals("Invalid character '{' in the anchor: {a", checkAnchor("{a"));
        assertEquals("Invalid character '}' in the anchor: }a", checkAnchor("}a"));
        assertEquals("Invalid character ',' in the anchor: a,b", checkAnchor("a,b"));
        assertEquals("Invalid character '*' in the anchor: a*b", checkAnchor("a*b"));
        assertEquals("Invalid character '&' in the anchor: a&b", checkAnchor("a&b"));
    }

    private DumpSettings createSettings(final Character invalid) {
        return DumpSettings.builder()
            .setAnchorGenerator(node -> new Anchor("anchor" + invalid))
            .build();
    }

    private String checkAnchor(String a) {
        try {
            new Anchor(a).toString();
            throw new IllegalStateException("Invalid must not be accepted: " + a);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
