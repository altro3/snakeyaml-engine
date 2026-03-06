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
package org.snakeyaml.engine.issues.issue23;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.events.DocumentStartEvent;
import org.snakeyaml.engine.v2.events.ImplicitTuple;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.util.StreamToStringWriter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_DUMP_SETTINGS;

@org.junit.jupiter.api.Tag("fast")
class EmptyStringOutputTest {

    @Test
    void outputEmptyString() {
        String output = DEFAULT_DUMP.dumpToString("");
        assertEquals("''\n", output, "The output must NOT contain ---");
    }

    @Test
    void outputEmptyStringWithExplicitStart() {
        var dumper = new Dump(DumpSettings.builder()
            .setExplicitStart(true)
            .build());
        String output = dumper.dumpToString("");
        assertEquals("--- ''\n", output, "The output must contain ---");
    }

    @Test
    void outputEmptyStringWithEmitter() {
        assertEquals("---", dump(""), "The output must contain ---");
    }

    @Test
    void outputStringWithEmitter() {
        assertEquals("v1234512345", dump("v1234512345"), "The output must NOT contain ---");
    }

    private String dump(String value) {
        var writer = new StreamToStringWriter();
        new Emitter(DEFAULT_DUMP_SETTINGS, writer)
            .emit(new StreamStartEvent())
            .emit(new DocumentStartEvent(false, SpecVersion.EMPTY, Map.of()))
            .emit(new ScalarEvent(null, null, ImplicitTuple.TRUE_FALSE, value, ScalarStyle.PLAIN, null, null));
        return writer.toString();
    }
}
