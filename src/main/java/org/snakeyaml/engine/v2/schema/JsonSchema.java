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
package org.snakeyaml.engine.v2.schema;

import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.constructor.ConstructYamlNull;
import org.snakeyaml.engine.v2.constructor.json.ConstructOptionalClass;
import org.snakeyaml.engine.v2.constructor.json.ConstructUuidClass;
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlBinary;
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonBool;
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonFloat;
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonInt;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Default schema
 */
public class JsonSchema implements Schema {
    // No need to extend Failsafe schema because it is empty

    private final ScalarResolver scalarResolver = new JsonScalarResolver();
    private final Map<Tag, ConstructNode> tagConstructors = new HashMap<>() {{
        put(Tag.NULL, new ConstructYamlNull());
        put(Tag.BOOL, new ConstructYamlJsonBool());
        put(Tag.INT, new ConstructYamlJsonInt());
        put(Tag.FLOAT, new ConstructYamlJsonFloat());
        put(Tag.BINARY, new ConstructYamlBinary());
        put(new Tag(UUID.class), new ConstructUuidClass());
        put(new Tag(Optional.class), new ConstructOptionalClass(scalarResolver));
    }};

    /**
     * Create ScalarResolver
     *
     * @return JsonScalarResolver
     */
    @Override
    public ScalarResolver getScalarResolver() {
        return scalarResolver;
    }

    /**
     * Basic constructs
     *
     * @return map with constructs
     */
    @Override
    public Map<Tag, ConstructNode> getSchemaTagConstructors() {
        return tagConstructors;
    }
}
