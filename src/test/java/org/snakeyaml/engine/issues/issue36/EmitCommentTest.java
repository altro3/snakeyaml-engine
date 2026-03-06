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
package org.snakeyaml.engine.issues.issue36;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.events.CommentEvent;
import org.snakeyaml.engine.v2.events.DocumentEndEvent;
import org.snakeyaml.engine.v2.events.DocumentStartEvent;
import org.snakeyaml.engine.v2.events.ImplicitTuple;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.StreamEndEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.util.StreamToStringWriter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
class EmitCommentTest {

    @Test
    @DisplayName("Issue 36: comment with scalar should not be ignored")
    void emitCommentWithEvent() {
        var settings = DumpSettings.builder()
            .setDumpComments(true)
            .build();
        var writer = new StreamToStringWriter();
        new Emitter(settings, writer)
            .emit(new StreamStartEvent())
            .emit(new DocumentStartEvent(false, SpecVersion.EMPTY, Map.of()))
            .emit(new CommentEvent(CommentType.BLOCK, "Hello world!", null, null))
            .emit(new ScalarEvent(null, null, ImplicitTuple.TRUE_TRUE, "This is the scalar", ScalarStyle.DOUBLE_QUOTED))
            .emit(new DocumentEndEvent(false))
            .emit(new StreamEndEvent());

        assertEquals("""
            #Hello world!
            "This is the scalar"
            """, writer.toString());
    }

    @Test
    @DisplayName("Issue 36: only comment should not be ignored")
    void emitComment() {
        var settings = DumpSettings.builder()
            .setDumpComments(true)
            .build();
        var writer = new StreamToStringWriter();
        new Emitter(settings, writer)
            .emit(new StreamStartEvent())
            .emit(new DocumentStartEvent(false, SpecVersion.EMPTY, new HashMap<>()))
            .emit(new CommentEvent(CommentType.BLOCK, "Hello world!", null, null))
            .emit(new DocumentEndEvent(false))
            .emit(new StreamEndEvent());

        assertEquals("#Hello world!\n", writer.toString());
    }
}
