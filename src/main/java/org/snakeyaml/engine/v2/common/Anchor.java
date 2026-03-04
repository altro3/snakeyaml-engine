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
package org.snakeyaml.engine.v2.common;

import org.jspecify.annotations.NonNull;
import org.snakeyaml.engine.v2.exceptions.EmitterException;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Value inside Anchor and Alias
 *
 * @param value - the anchor value
 */
public record Anchor(
    @NonNull
    String value
) {

    private static final Set<Character> INVALID_ANCHOR = Set.of('[', ']', '{', '}', ',', '*', '&');
    private static final Pattern SPACES_PATTERN = Pattern.compile("\\s");

    public Anchor {
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Empty anchor.");
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (INVALID_ANCHOR.contains(ch)) {
                throw new EmitterException("Invalid character '" + ch + "' in the anchor: " + value);
            }
        }
        Matcher matcher = SPACES_PATTERN.matcher(value);
        if (matcher.find()) {
            throw new EmitterException("Anchor may not contain spaces: " + value);
        }
    }
}
