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
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.exceptions.Mark;

/**
 * Marks the beginning of a sequence node.
 * <p>
 * This event is followed by the elements contained in the sequence, and a {@link SequenceEndEvent}.
 * </p>
 *
 * @see SequenceEndEvent
 */
public final class SequenceStartEvent extends CollectionStartEvent {

    public SequenceStartEvent(@NonNull Anchor anchor, @NonNull String tag, boolean implicit, @NonNull FlowStyle flowStyle, @Nullable Mark startMark, @Nullable Mark endMark) {
        super(anchor, tag, implicit, flowStyle, startMark, endMark);
    }

    public SequenceStartEvent(@NonNull Anchor anchor, @NonNull String tag, boolean implicit, @NonNull FlowStyle flowStyle) {
        this(anchor, tag, implicit, flowStyle, null, null);
    }

    @Override
    public @NonNull Id getEventId() {
        return Id.SequenceStart;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder("+SEQ");
        if (flowStyle == FlowStyle.FLOW) {
            builder.append(" []");
        }
        return builder.append(super.toString())
            .toString();
    }
}
