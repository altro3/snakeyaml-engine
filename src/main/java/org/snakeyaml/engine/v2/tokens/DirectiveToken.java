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
package org.snakeyaml.engine.v2.tokens;

import org.jspecify.annotations.NonNull;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

import java.util.List;

public final class DirectiveToken<T> extends Token {

    public static final String YAML_DIRECTIVE = "YAML";
    public static final String TAG_DIRECTIVE = "TAG";

    private final String name;
    private final List<T> value;

    public DirectiveToken(@NonNull String name, @NonNull List<T> value, Mark startMark, Mark endMark) {
        super(startMark, endMark);
        if (value.size() != 2) {
            throw new YamlEngineException("Two strings/integers must be provided instead of " + value.size());
        }
        this.name = name;
        this.value = value;
    }

    public @NonNull String getName() {
        return this.name;
    }

    public @NonNull List<T> getValue() {
        return this.value;
    }

    @Override
    public @NonNull Id getTokenId() {
        return Id.Directive;
    }
}
