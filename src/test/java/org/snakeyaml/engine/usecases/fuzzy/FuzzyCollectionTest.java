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
package org.snakeyaml.engine.usecases.fuzzy;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FuzzyCollectionTest {

    /**
     * <a href="https://bitbucket.org/snakeyaml/snakeyaml/issues/1064">link</a> This is different from SnakeYAML - the YAML looks valid.
     */
    @Test
    void testFuzzyInput() {
        String datastring = " ? - - ? - - ? ? - - ? ? ? - - ? ? - - ? ? ? - - ? ? - ? ? - - ? - - ? ? ? - - ? ? - ?  -? - ? ? - - ? - - ? ? ? - - ? ? - ?  -? - ? ? - - ? - ";
        var dataStream = new ByteArrayInputStream(datastring.getBytes(StandardCharsets.UTF_8));
        var reader = new InputStreamReader(dataStream, StandardCharsets.UTF_8);

        var yamlProcessor = new Load(LoadSettings.builder()
            .setAllowRecursiveKeys(true)
            .setMaxAliasesForCollections(1000)
            .setAllowDuplicateKeys(true)
            .setAllowNonScalarKeys(true)
            .build());
        Object fuzzy = yamlProcessor.loadFromReader(reader);
        assertTrue(fuzzy.toString().startsWith("{[[{[[{{[[{{{[[{{[[{{{[[{{[{{[[{[[{{{[[{{[{-?"));
    }
}
