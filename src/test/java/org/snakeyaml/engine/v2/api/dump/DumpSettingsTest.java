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
package org.snakeyaml.engine.v2.api.dump;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.SettingKey;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.NonPrintableStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.exceptions.EmitterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP_SETTINGS;

@Tag("fast")
class DumpSettingsTest {

    @Test
    @DisplayName("Check default values")
    void defaults() {
        var settings = DEFAULT_DUMP_SETTINGS;

        assertEquals("\n", settings.bestLineBreak());
        assertEquals(2, settings.indent());
        assertEquals(FlowStyle.AUTO, settings.defaultFlowStyle());
        assertEquals(ScalarStyle.PLAIN, settings.defaultScalarStyle());
        assertNull(settings.explicitRootTag());
        assertFalse(settings.indentWithIndicator());
        assertFalse(settings.explicitEnd());
        assertFalse(settings.explicitStart());
        assertFalse(settings.canonical());
        assertTrue(settings.splitLines());
        assertFalse(settings.multiLineFlow());
        assertTrue(settings.useUnicodeEncoding());
        assertEquals(0, settings.indicatorIndent());
        assertEquals(128, settings.maxSimpleKeyLength());
        assertEquals(NonPrintableStyle.ESCAPE, settings.nonPrintableStyle());
        assertEquals(80, settings.width());
        assertEquals(SpecVersion.EMPTY, settings.yamlDirective());
        assertEquals(Map.of(), settings.tagDirective());
        assertNotNull(settings.anchorGenerator());
    }

    @Test
    @DisplayName("Canonical output")
    void setCanonical() {
        var dump = new Dump(DumpSettings.builder()
            .setCanonical(true)
            .build());
        var data = new ArrayList<Integer>();
        for (int i = 0; i < 2; i++) {
            data.add(i);
        }
        String str = dump.dumpToString(data);
        assertEquals("""
            ---
            !!seq [
              !!int "0",
              !!int "1",
            ]
            """, str);
    }

    @Test
    @DisplayName("Use Windows line break")
    void setBestLineBreak() {
        var dump = new Dump(DumpSettings.builder()
            .setBestLineBreak("\r\n")
            .build());
        var data = new ArrayList<Integer>();
        for (int i = 0; i < 2; i++) {
            data.add(i);
        }
        String str = dump.dumpToString(data);
        assertEquals("[0, 1]\r\n", str);
    }

    @Test
    void setMultiLineFlow() {
        var dump = new Dump(DumpSettings.builder()
            .setMultiLineFlow(true)
            .build());
        var data = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            data.add(i);
        }
        String str = dump.dumpToString(data);
        assertEquals("""
            [
              0,
              1,
              2
            ]
            """, str);
    }

    @Test
    @DisplayName("Show tag directives")
    void setTagDirective() {
        var tagDirectives = new TreeMap<String, String>();
        tagDirectives.put("!yaml!", "tag:yaml.org,2002:");
        tagDirectives.put("!python!", "!python");
        var dump = new Dump(DumpSettings.builder()
            .setTagDirective(tagDirectives)
            .build());
        String str = dump.dumpToString("data");
        assertEquals("""
            %TAG !python! !python
            %TAG !yaml! tag:yaml.org,2002:
            --- data
            """, str);
    }

    @Test
    @DisplayName("Check corner cases for indent")
    void setIndent() {
        var exception1 = assertThrows(EmitterException.class, () -> DumpSettings.builder().setIndent(0));
        assertEquals("Indent must be at least 1", exception1.getMessage());

        var exception2 = assertThrows(EmitterException.class, () -> DumpSettings.builder().setIndent(12));
        assertEquals("Indent must be at most 10", exception2.getMessage());
    }

    @Test
    @DisplayName("Check corner cases for Indicator Indent")
    void setIndicatorIndent() {
        var exception1 = assertThrows(EmitterException.class, () -> DumpSettings.builder().setIndicatorIndent(-1));
        assertEquals("Indicator indent must be non-negative", exception1.getMessage());

        var exception2 = assertThrows(EmitterException.class, () -> DumpSettings.builder().setIndicatorIndent(10));
        assertEquals("Indicator indent must be at most Emitter.MAX_INDENT-1: 9", exception2.getMessage());
    }

    @Test
    @DisplayName("Dump explicit version")
    void dumpVersion() {
        var dump = new Dump(DumpSettings.builder()
            .setYamlDirective(SpecVersion.V_1_2)
            .build());
        String str = dump.dumpToString("a");
        assertEquals("""
            %YAML 1.2
            --- a
            """, str);
    }

    @Test
    void dumpCustomProperty() {
        var settings = DumpSettings.builder()
            .setCustomProperty(new KeyName("key"), "value")
            .build();
        assertEquals("value", settings.getCustomProperty(new KeyName("key")));
        assertNull(settings.getCustomProperty(new KeyName("None")));
    }

    static class KeyName implements SettingKey {

        private final String keyName;

        public KeyName(String name) {
            keyName = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            KeyName keyName1 = (KeyName) o;
            return Objects.equals(keyName, keyName1.keyName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyName);
        }
    }
}
