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
package org.snakeyaml.engine.v2.representer;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.serializer.Serializer;
import org.snakeyaml.engine.v2.util.StreamToStringWriter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("fast")
class RepresentEntryTest {

    private final DumpSettings settings = DumpSettings.builder()
        .setDefaultScalarStyle(ScalarStyle.PLAIN)
        .setDefaultFlowStyle(FlowStyle.BLOCK)
        .setDumpComments(true)
        .build();
    private final CommentedEntryRepresenter commentedEntryRepresenter = new CommentedEntryRepresenter(settings);

    @Test
    @DisplayName("Represent and dump mapping nodes using the new method")
    void representMapping() {
        var stringOutputStream = new StreamToStringWriter();

        var serializer = new Serializer(settings, new Emitter(settings, stringOutputStream));
        serializer.emitStreamStart();
        serializer.serializeDocument(commentedEntryRepresenter.represent(createMap()));
        serializer.emitStreamEnd();

        assertEquals("""
                #Key node block comment
                a: val1 #Value node inline comment
                """,
            stringOutputStream.toString());
    }

    private Map<String, String> createMap() {
        var map = new LinkedHashMap<String, String>();
        map.put("a", "val1");
        return map;
    }

    private static class CommentedEntryRepresenter extends StandardRepresenter {

        public CommentedEntryRepresenter(DumpSettings settings) {
            super(settings);
        }

        @Override
        protected @NonNull NodeTuple representMappingEntry(Map.Entry<?, ?> entry) {
            NodeTuple tuple = super.representMappingEntry(entry);
            var keyBlockComments = List.of(new CommentLine(null, null, "Key node block comment", CommentType.BLOCK));
            tuple.keyNode().setBlockComments(keyBlockComments);

            var valueEndComments = List.of(new CommentLine(null, null, "Value node inline comment", CommentType.IN_LINE));
            tuple.valueNode().setEndComments(valueEndComments);

            return tuple;
        }
    }
}
