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
import org.snakeyaml.engine.v2.exceptions.Mark;

/**
 * Basic unit of output from a {@link org.snakeyaml.engine.v2.parser.Parser} or input of a
 * {@link org.snakeyaml.engine.v2.emitter.Emitter}.
 */
public abstract class Event {

    private final Mark startMark;
    private final Mark endMark;

    public Event(@Nullable Mark startMark, @Nullable Mark endMark) {
        if ((startMark != null && endMark == null) || (startMark == null && endMark != null)) {
            throw new NullPointerException("Both marks must be either present or absent.");
        }
        this.startMark = startMark;
        this.endMark = endMark;
    }

    /*
     * Create Node for emitter
     */
    public Event() {
        this(null, null);
    }

    public @Nullable Mark getStartMark() {
        return startMark;
    }

    public @Nullable Mark getEndMark() {
        return endMark;
    }

    /**
     * Get the type (kind) if this Event
     *
     * @return the ID of this Event
     */
    public abstract @NonNull Id getEventId();

    /**
     * ID of a non-abstract Event
     */
    public enum Id {
        Alias,
        Comment,
        DocumentEnd,
        DocumentStart,
        MappingEnd,
        MappingStart,
        Scalar,
        SequenceEnd,
        SequenceStart,
        StreamEnd,
        StreamStart, // NOSONAR
    }
}
