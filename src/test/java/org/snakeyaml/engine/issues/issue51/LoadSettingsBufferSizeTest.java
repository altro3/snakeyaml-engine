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
package org.snakeyaml.engine.issues.issue51;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@org.junit.jupiter.api.Tag("fast")
class LoadSettingsBufferSizeTest {

    private final String yaml = """
         - foo: bar
           if: 'aaa' == 'bbb'
        """;

    @DisplayName("Issue 51 - exact buffer size")
    @Test
    void setBufferSizeCutsError() {
        var e = assertThrows(ParserException.class, () -> parse(yaml));
        assertEquals("""
            while parsing a block mapping
             in reader, line 1, column 4:
                 - foo: bar
                   ^
            expected <block end>, but found '<scalar>'
             in reader, line 2, column 14:
                   if: 'aaa' == 'bbb'
                             ^
            """, e.getMessage());
    }

    private void parse(String yaml) {
        var settings = LoadSettings.builder()
            .setBufferSize(yaml.length())
            .build();
        new Composer(settings, new ParserImpl(settings, new StreamReader(settings, yaml)))
            .getSingleNode();
    }
}
