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
package org.snakeyaml.engine.v2.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.exceptions.DuplicateKeyException;
import org.snakeyaml.engine.v2.schema.JsonSchema;

import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD_SETTINGS;

@Tag("fast")
class LoadSettingsTest {

    @Test
    @DisplayName("Accept only YAML 1.2")
    void acceptOnly12() {
        UnaryOperator<SpecVersion> strict12 = t -> {
            if (t.getMajor() != 1 || t.getMinor() != 2) {
                throw new IllegalArgumentException("Only 1.2 is supported.");
            }
            return t;
        };

        var load = new Load(LoadSettings.builder()
            .setVersionFunction(strict12)
            .build());

        var e = assertThrows(IllegalArgumentException.class, () -> load.loadFromString("%YAML 1.1\n...\nfoo"));
        assertEquals("Only 1.2 is supported.", e.getMessage());
    }

    @Test
    @DisplayName("Do not allow duplicate keys")
    void doNotAllowDuplicateKeys() {
        var load = new Load(LoadSettings.builder()
            .setAllowDuplicateKeys(false)
            .build());
        var e = assertThrows(DuplicateKeyException.class, () -> load.loadFromString("{a: 1, a: 2}"));
        assertTrue(e.getMessage().contains("found duplicate key a"));
    }

    @Test
    @DisplayName("Do not allow duplicate keys by default")
    void doNotAllowDuplicateKeysByDefault() {
        var e = assertThrows(DuplicateKeyException.class, () -> DEFAULT_LOAD.loadFromString("{a: 1, a: 2}"));
        assertTrue(e.getMessage().contains("found duplicate key a"));
    }

    @Test
    @DisplayName("Allow duplicate keys")
    void allowDuplicateKeysWhenSpecified() {
        var load = new Load(LoadSettings.builder()
            .setAllowDuplicateKeys(true)
            .build());
        @SuppressWarnings("unchecked")
        var map = (Map<String, Integer>) load.loadFromString("{a: 1, a: 2}");
        assertEquals(2, map.get("a"));
    }

    @Test
    @DisplayName("Set and get custom property")
    void customProperty() {
        var key = new SomeKey();
        var settings = LoadSettings.builder()
            .setCustomProperty(key, "foo")
            .setCustomProperty(SomeStatus.DELIVERED, "bar")
            .build();
        assertEquals("foo", settings.getCustomProperty(key));
        assertEquals("bar", settings.getCustomProperty(SomeStatus.DELIVERED));
    }

    @Test
    @DisplayName("Set and get custom I/O buffer size")
    void bufferSize() {
        var settings = LoadSettings.builder()
            .setBufferSize(4096)
            .build();
        assertEquals(4096, settings.bufferSize());
    }

    @Test
    @DisplayName("Use JSON schema by default")
    void defaultSchema() {
        assertEquals(JsonSchema.class, DEFAULT_LOAD_SETTINGS.schema().getClass());
    }

    public enum SomeStatus implements SettingKey {
        ORDERED,
        DELIVERED,
    }

    public static final class SomeKey implements SettingKey {

    }
}
