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
package org.snakeyaml.engine.v2.resolver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.schema.FailsafeSchema;
import org.snakeyaml.engine.v2.schema.Schema;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
class NoScalarResolverTest {

    @Test
    @DisplayName("Resolve with FailsafeSchema")
    void resolveIntegerAsStringWithFailsafeSchema() {
        var load = new Load(LoadSettings.builder()
            .setSchema(new FailsafeSchema())
            .build());
        Object str = load.loadFromString("5");
        assertEquals("5", str);
    }

    @Test
    @DisplayName("Resolve with custom ScalarResolver")
    void resolveIntegerAsStringWithCustomScalarResolver() {
        var schema = new Schema() {
            @Override
            public ScalarResolver getScalarResolver() {
                return new MyScalarResolver();
            }

            @Override
            public Map<Tag, ConstructNode> getSchemaTagConstructors() {
                return new HashMap<>();
            }
        };
        var load = new Load(LoadSettings.builder()
            .setSchema(schema)
            .build());
        Object ooo = load.loadFromString("5");
        assertEquals("5", ooo);
    }

    public static final class MyScalarResolver implements ScalarResolver {

        @Override
        public Tag resolve(String value, Boolean implicit) {
            return Tag.STR;
        }
    }
}
