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
package org.snakeyaml.engine.usecases.tags;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;
import org.snakeyaml.engine.v2.schema.JsonSchema;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Example of parsing a !!timestamp tag
 */
@org.junit.jupiter.api.Tag("fast")
class TimestampTagTest {

    // this is an example of the tag from YAML 1.1 spec. It can be anything else
    static final Tag MY_TIME_TAG = new Tag(Tag.PREFIX + "timestamp");

    @Test
    void testExplicitTag() {
        var loader = new Load(LoadSettings.builder()
            .setTagConstructors(Map.of(MY_TIME_TAG, new TimestampConstructor()))
            .build());
        var obj = (LocalDateTime) loader.loadFromString("!!timestamp 2020-03-24T12:34:00.333");
        assertEquals(LocalDateTime.of(2020, 3, 24, 12, 34, 0, 333000000), obj);
    }

    @Test
    void testImplicitTag() {
        var loader = new Load(LoadSettings.builder()
            .setSchema(new TimestampSchema())
            .build());
        var obj = (LocalDateTime) loader.loadFromString("2020-03-24T12:34:00.333");
        assertEquals(LocalDateTime.of(2020, 3, 24, 12, 34, 0, 333000000), obj);
    }

    @Test
    void testImplicitTagInMap() {
        var loader = new Load(LoadSettings.builder()
            .setSchema(new TimestampSchema())
            .build());
        @SuppressWarnings("unchecked")
        var map = (Map<String, LocalDateTime>) loader.loadFromString("time: 2020-03-24T13:44:10.333");
        LocalDateTime time = map.get("time");
        assertEquals(LocalDateTime.of(2020, 3, 24, 13, 44, 10, 333000000), time);
    }

    static final class TimestampConstructor implements ConstructNode {

        @Override
        public Object construct(Node node) {
            var scalar = (ScalarNode) node;
            // the parsing depends on what should be parsed and to which object
            // examples can be found in SnakeYAML tests for the YAML 1.1 types format
            return LocalDateTime.parse(scalar.getValue());
        }
    }

    /**
     * This is required to support implicit tags
     */
    static final class MyScalarResolver extends JsonScalarResolver {

        // this is taken from YAML 1.1 types
        // it can be changed to represent the business case
        public static final Pattern TIMESTAMP = Pattern.compile("^(?:[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]|[0-9][0-9][0-9][0-9]-[0-9][0-9]?-[0-9][0-9]?(?:[Tt]|[ \t]+)[0-9][0-9]?:[0-9][0-9]:[0-9][0-9](?:\\.[0-9]*)?(?:[ \t]*(?:Z|[-+][0-9][0-9]?(?::[0-9][0-9])?))?)$");

        @Override
        public Tag resolve(String value, Boolean implicit) {
            if (TIMESTAMP.matcher(value).matches()) {
                return MY_TIME_TAG;
            }
            return super.resolve(value, implicit);
        }
    }

    public static final class TimestampSchema extends JsonSchema {

        @Override
        public ScalarResolver getScalarResolver() {
            return new MyScalarResolver();
        }

        @Override
        public Map<Tag, ConstructNode> getSchemaTagConstructors() {
            Map<Tag, ConstructNode> parent = super.getSchemaTagConstructors();
            parent.put(MY_TIME_TAG, new TimestampConstructor());
            return parent;
        }
    }
}
