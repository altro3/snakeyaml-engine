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
package org.snakeyaml.engine.usecases.inherited;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.tokens.Token;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@org.junit.jupiter.api.Tag("fast")
class InheritedCanonicalTest extends InheritedImportTest {

    @Test
    @DisplayName("Canonical scan")
    void testCanonicalScanner() throws IOException {
        File[] files = getStreamsByExtension(".canonical");
        assertTrue(files.length > 0, "No test files found.");
        for (var file : files) {
            try (var input = new FileInputStream(file)) {
                assertFalse(canonicalScan(input, file.getName()).isEmpty());
            }
        }
    }

    @Test
    @DisplayName("Canonical parse")
    void testCanonicalParser() throws IOException {
        File[] files = getStreamsByExtension(".canonical");
        assertTrue(files.length > 0, "No test files found.");
        for (var file : files) {
            try (var input = new FileInputStream(file)) {
                assertFalse(canonicalParse(input, file.getName()).isEmpty());
            }
        }
    }

    private List<Token> canonicalScan(InputStream input, String label) throws IOException {
        int ch = input.read();
        var buffer = new StringBuilder();
        while (ch != -1) {
            buffer.append((char) ch);
            ch = input.read();
        }
        var scanner = new CanonicalScanner(buffer.toString().replace(System.lineSeparator(), "\n"), label);
        var result = new ArrayList<Token>();
        while (scanner.hasNext()) {
            result.add(scanner.next());
        }
        return result;
    }
}
