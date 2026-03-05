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
package org.snakeyaml.engine.v2.events;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.common.CharConstants;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.Mark;

import java.util.stream.Collectors;

/**
 * Marks a scalar value.
 */
public final class ScalarEvent extends NodeEvent {

    private final String tag;
    // style flag of a scalar event indicates the style of the scalar.
    private final ScalarStyle style;
    private final String value;
    // The implicit flag of a scalar event is a pair of boolean values that
    // indicate if the tag may be omitted when the scalar is emitted in a plain
    // and non-plain style correspondingly.
    private final ImplicitTuple implicit;

    public ScalarEvent(@Nullable Anchor anchor, @Nullable String tag, @NonNull ImplicitTuple implicit, @NonNull String value,
                       @NonNull ScalarStyle style, @Nullable Mark startMark, @Nullable Mark endMark) {
        super(anchor, startMark, endMark);
        this.tag = tag;
        this.implicit = implicit;
        this.value = value;
        this.style = style;
    }

    public ScalarEvent(@Nullable Anchor anchor, @Nullable String tag, @NonNull ImplicitTuple implicit, @NonNull String value, @NonNull ScalarStyle style) {
        this(anchor, tag, implicit, value, style, null, null);
    }

    /**
     * Tag of this scalar.
     *
     * @return The tag of this scalar, or <code>null</code> if no explicit tag is available.
     */
    public @Nullable String getTag() {
        return tag;
    }

    /**
     * Style of the scalar.
     * <dl>
     * <dt>null</dt>
     * <dd>Flow Style - Plain</dd>
     * <dt>'\''</dt>
     * <dd>Flow Style - Single-Quoted</dd>
     * <dt>'"'</dt>
     * <dd>Flow Style - Double-Quoted</dd>
     * <dt>'|'</dt>
     * <dd>Block Style - Literal</dd>
     * <dt>'&gt;'</dt>
     * <dd>Block Style - Folded</dd>
     * </dl>
     *
     * @return Style of the scalar.
     */
    public @NonNull ScalarStyle getScalarStyle() {
        return style;
    }

    /**
     * String representation of the value.
     * <p>
     * Without quotes and escaping.
     * </p>
     *
     * @return Value as Unicode string.
     */
    public @NonNull String getValue() {
        return value;
    }

    public @NonNull ImplicitTuple getImplicit() {
        return implicit;
    }

    @Override
    public @NonNull Id getEventId() {
        return Id.Scalar;
    }

    public boolean isPlain() {
        return style == ScalarStyle.PLAIN;
    }

    public boolean isLiteral() {
        return style == ScalarStyle.LITERAL;
    }

    public boolean isSQuoted() {
        return style == ScalarStyle.SINGLE_QUOTED;
    }

    public boolean isDQuoted() {
        return style == ScalarStyle.DOUBLE_QUOTED;
    }

    public boolean isFolded() {
        return style == ScalarStyle.FOLDED;
    }

    public boolean isJson() {
        return style == ScalarStyle.JSON_SCALAR_STYLE;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder("=VAL");
        if (anchor != null) {
            builder.append(" &").append(anchor);
        }
        if (implicit.bothFalse() && tag != null) {
            builder.append(" <").append(tag).append('>');
        }
        return builder.append(' ').append(style.toString()).append(escapedValue())
            .toString();
    }

    // escape
    public String escapedValue() {
        return value.codePoints()
            .filter(i -> i < Character.MAX_VALUE)
            .mapToObj(ch -> CharConstants.escapeChar(new String(Character.toChars(ch))))
            .collect(Collectors.joining(""));
    }
}
