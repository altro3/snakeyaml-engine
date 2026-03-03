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
import org.snakeyaml.engine.v2.constructor.core.ConstructYamlCoreBool;
import org.snakeyaml.engine.v2.constructor.core.ConstructYamlCoreFloat;
import org.snakeyaml.engine.v2.constructor.core.ConstructYamlCoreInt;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.CoreScalarResolver;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Core schema
 */
public class CoreSchema extends JsonSchema {

    private final Map<Tag, ConstructNode> fullSchemaTagConstructors;

    public CoreSchema() {
        var superSchemaTagConstructors = super.getSchemaTagConstructors();
        fullSchemaTagConstructors = new HashMap<>(superSchemaTagConstructors);
        fullSchemaTagConstructors.put(Tag.BOOL, new ConstructYamlCoreBool());
        fullSchemaTagConstructors.put(Tag.INT, new ConstructYamlCoreInt());
        fullSchemaTagConstructors.put(Tag.FLOAT, new ConstructYamlCoreFloat());
    }

    /**
     * Create ScalarResolver with the merge implemented
     *
     * @return CoreScalarResolver
     * @see <a href="https://ktomk.github.io/writing/yaml-anchor-alias-and-merge-key.html">Support of
     *     Merge Keys</a>
     */
    @Override
    public ScalarResolver getScalarResolver() {
        return new CoreScalarResolver(true);
    }

    /**
     * Provide constructs to support the schema (bool, int, float)
     *
     * @return map
     */
    @Override
    public Map<Tag, ConstructNode> getSchemaTagConstructors() {
        return fullSchemaTagConstructors;
    }
}
