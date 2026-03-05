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
package org.snakeyaml.engine.issues.issue47;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.events.DocumentEndEvent;
import org.snakeyaml.engine.v2.events.DocumentStartEvent;
import org.snakeyaml.engine.v2.events.ImplicitTuple;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.StreamEndEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.util.StreamToStringWriter;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

/**
 * Test issue 47 <a href="https://yaml.org/spec/1.2.2/#3231-node-styles">Node styles</a>
 */
class SetWidthTest {

    String stringToSerialize = "arn:aws:iam::12345678901234567890:foobarbaz:testing:testing2:role/github-actions-role/${{ github.token }}";

    @Test
    @DisplayName("Issue 47: emit plain and split")
    void emitPlainString() {
        var settings = DumpSettings.builder()
            .setWidth(80) // Intentionally limited.
            .build();
        var writer = new StreamToStringWriter();
        new Dump(settings)
            .dump(stringToSerialize, writer);
        String yaml = writer.toString();
        assertEquals("arn:aws:iam::12345678901234567890:foobarbaz:testing:testing2:role/github-actions-role/${{\n  github.token }}", yaml.trim());
        assertEquals(stringToSerialize, parseBack(yaml));
    }

    @Test
    @DisplayName("Issue 47: emit plain and split")
    void emitPlain() {
        var settings = DumpSettings.builder()
            .setWidth(80) // Intentionally limited.
            .build();
        var writer = new StreamToStringWriter();
        new Emitter(settings, writer)
            .emit(new StreamStartEvent())
            .emit(new DocumentStartEvent(false, SpecVersion.EMPTY, emptyMap()))
            .emit(new ScalarEvent(null, null, ImplicitTuple.TRUE_TRUE, stringToSerialize, ScalarStyle.PLAIN))
            .emit(new DocumentEndEvent(false))
            .emit(new StreamEndEvent());
        String yaml = writer.toString();
        assertEquals("arn:aws:iam::12345678901234567890:foobarbaz:testing:testing2:role/github-actions-role/${{\n  github.token }}", yaml.trim());
        assertEquals(stringToSerialize, parseBack(yaml));
    }

    @Test
    @DisplayName("Issue 47: emit plain and no split")
    void emitPlainNoSplit() {
        var settings = DumpSettings.builder()
            .setWidth(180) // Intentionally limited.
            .build();
        var writer = new StreamToStringWriter();
        new Emitter(settings, writer)
            .emit(new StreamStartEvent())
            .emit(new DocumentStartEvent(false, SpecVersion.EMPTY, emptyMap()))
            .emit(new ScalarEvent(null, null, ImplicitTuple.TRUE_TRUE, stringToSerialize, ScalarStyle.PLAIN))
            .emit(new DocumentEndEvent(false))
            .emit(new StreamEndEvent());
        String yaml = writer.toString();
        assertEquals("arn:aws:iam::12345678901234567890:foobarbaz:testing:testing2:role/github-actions-role/${{ github.token }}\n", yaml);
        assertEquals(stringToSerialize, parseBack(yaml));
    }

    @Test
    @DisplayName("Issue 47: emit folded")
    void emitFolded() {
        var settings = DumpSettings.builder()
            .setWidth(80) // Intentionally limited.
            .setDefaultScalarStyle(ScalarStyle.FOLDED)
            .build();
        String yaml = new Dump(settings)
            .dumpToString(stringToSerialize);
        assertEquals("""
            >-
              arn:aws:iam::12345678901234567890:foobarbaz:testing:testing2:role/github-actions-role/${{
              github.token }}
            """, yaml);
        assertEquals(stringToSerialize, parseBack(yaml));
    }

    private String parseBack(String yaml) {
        return DEFAULT_LOAD.loadFromString(yaml).toString();
    }
}
