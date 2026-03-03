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
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.Mark;

public final class ScalarToken extends Token {

    private final String value;
    private final boolean plain;
    private final ScalarStyle style;

    public ScalarToken(@NonNull String value, boolean plain, @NonNull Mark startMark, @NonNull Mark endMark) {
        this(value, plain, ScalarStyle.PLAIN, startMark, endMark);
    }

    public ScalarToken(@NonNull String value, boolean plain, @NonNull ScalarStyle style, @NonNull Mark startMark, @NonNull Mark endMark) {
        super(startMark, endMark);
        this.value = value;
        this.plain = plain;
        this.style = style;
    }

    public boolean isPlain() {
        return this.plain;
    }

    public @NonNull String getValue() {
        return this.value;
    }

    public @NonNull ScalarStyle getStyle() {
        return this.style;
    }

    @Override
    public @NonNull Id getTokenId() {
        return Id.Scalar;
    }

    @Override
    public String toString() {
        return getTokenId().toString() + " plain=" + plain + " style=" + style + " value=" + value;
    }
}
