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
 * Marks the end of a document.
 * <p>
 * This event follows the document's content.
 * </p>
 */
public final class DocumentEndEvent extends Event {

    private final boolean explicit;

    public DocumentEndEvent(boolean explicit, @Nullable Mark startMark, @Nullable Mark endMark) {
        super(startMark, endMark);
        this.explicit = explicit;
    }

    public DocumentEndEvent(boolean explicit) {
        this(explicit, null, null);
    }

    public boolean isExplicit() {
        return explicit;
    }

    @Override
    public @NonNull Id getEventId() {
        return Id.DocumentEnd;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder("-DOC");
        if (explicit) {
            builder.append(" ...");
        }
        return builder.toString();
    }
}
