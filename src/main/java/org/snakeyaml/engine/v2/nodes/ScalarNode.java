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
package org.snakeyaml.engine.v2.nodes;

import org.jspecify.annotations.NonNull;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.Mark;

/**
 * Represents a scalar node.
 * <p>
 * Scalar nodes form the leaves in the node graph.
 * </p>
 */
public class ScalarNode extends Node {

    private final ScalarStyle style;
    private final String value;

    public ScalarNode(Tag tag, boolean resolved, @NonNull String value, @NonNull ScalarStyle style, Mark startMark, Mark endMark) {
        super(tag, startMark, endMark);
        this.value = value;
        this.style = style;
        this.resolved = resolved;
    }

    public ScalarNode(Tag tag, String value, ScalarStyle style) {
        this(tag, true, value, style, null, null);
    }

    /**
     * Get scalar style of this node.
     *
     * @return style of this scalar node
     * @see org.snakeyaml.engine.v2.events.ScalarEvent Flow styles -
     *     https://yaml.org/spec/1.2/spec.html#id2786942 Block styles -
     *     https://yaml.org/spec/1.2/spec.html#id2793652
     */
    public @NonNull ScalarStyle getScalarStyle() {
        return style;
    }

    @Override
    public @NonNull NodeType getNodeType() {
        return NodeType.SCALAR;
    }

    /**
     * Value of this scalar.
     *
     * @return Scalar's value.
     */
    public @NonNull String getValue() {
        return value;
    }

    public boolean isPlain() {
        return style == ScalarStyle.PLAIN;
    }

    @Override
    public String toString() {
        return "<" + this.getClass().getName() +" (tag=" + getTag() + ", value=" + getValue() + ")>";
    }
}
