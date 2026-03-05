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
package org.snakeyaml.engine.v2.api.lowlevel;

import org.jspecify.annotations.NonNull;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.util.StreamToStringWriter;

import java.util.Iterator;

/**
 * Emit the events into a data stream (opposite for Parse).
 */
public class Present {

    private final DumpSettings settings;

    /**
     * Create Present (emitter)
     *
     * @param settings - configuration
     */
    public Present(@NonNull DumpSettings settings) {
        this.settings = settings;
    }

    /**
     * Serialize the provided Events
     *
     * @param events - the data to serialize
     * @return - the YAML document
     */
    public String emitToString(@NonNull Iterator<Event> events) {
        var writer = new StreamToStringWriter();
        final var emitter = new Emitter(settings, writer);
        events.forEachRemaining(emitter::emit);
        return writer.toString();
    }
}

