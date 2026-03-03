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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;
import org.snakeyaml.engine.v2.scanner.Scanner;
import org.snakeyaml.engine.v2.scanner.ScannerImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.snakeyaml.engine.v2.tokens.StreamEndToken;
import org.snakeyaml.engine.v2.tokens.StreamStartToken;
import org.snakeyaml.engine.v2.tokens.Token;

@org.junit.jupiter.api.Tag("fast")
public class InheritedTokensTest extends InheritedImportTest {

    @Test
    @DisplayName("Tokens are correct")
    public void testTokens() throws FileNotFoundException {
        Map<Token.Id, String> replaces = new HashMap<Token.Id, String>();
        replaces.put(Token.Id.Directive, "%");
        replaces.put(Token.Id.DocumentStart, "---");
        replaces.put(Token.Id.DocumentEnd, "...");
        replaces.put(Token.Id.Alias, "*");
        replaces.put(Token.Id.Anchor, "&");
        replaces.put(Token.Id.Tag, "!");
        replaces.put(Token.Id.Scalar, "_");
        replaces.put(Token.Id.BlockSequenceStart, "[[");
        replaces.put(Token.Id.BlockMappingStart, "{{");
        replaces.put(Token.Id.BlockEnd, "]}");
        replaces.put(Token.Id.FlowSequenceStart, "[");
        replaces.put(Token.Id.FlowSequenceEnd, "]");
        replaces.put(Token.Id.FlowMappingStart, "{");
        replaces.put(Token.Id.FlowMappingEnd, "}");
        replaces.put(Token.Id.BlockEntry, ",");
        replaces.put(Token.Id.FlowEntry, ",");
        replaces.put(Token.Id.Key, "?");
        replaces.put(Token.Id.Value, ":");
        //
        File[] tokensFiles = getStreamsByExtension(".tokens");
        assertTrue(tokensFiles.length > 0, "No test files found.");
        for (int i = 0; i < tokensFiles.length; i++) {
            String name = tokensFiles[i].getName();
            int position = name.lastIndexOf('.');
            String dataName = name.substring(0, position) + ".data";
            //
            String tokenFileData = getResource(name);
            String[] split = tokenFileData.split("\\s+");
            List<String> tokens2 = new ArrayList<String>();
            Collections.addAll(tokens2, split);
            //
            List<String> tokens1 = new ArrayList<String>();
            LoadSettings settings = LoadSettings.builder().build();
            StreamReader reader = new StreamReader(settings,
                new YamlUnicodeReader(new FileInputStream(getFileByName(dataName))));
            Scanner scanner = new ScannerImpl(settings, reader);
            try {
                while (scanner.checkToken()) {
                    Token token = scanner.next();
                    if (!(token instanceof StreamStartToken || token instanceof StreamEndToken)) {
                        String replacement = replaces.get(token.getTokenId());
                        tokens1.add(replacement);
                    }
                }
                assertEquals(tokens1.size(), tokens2.size(), tokenFileData);
                assertEquals(tokens1, tokens2);
            } catch (RuntimeException e) {
                System.out.println("File name: \n" + tokensFiles[i].getName());
                String data = getResource(tokensFiles[i].getName());
                System.out.println("Data: \n" + data);
                System.out.println("Tokens:");
                for (String token : tokens1) {
                    System.out.println(token);
                }
                fail("Cannot scan: " + tokensFiles[i]);
            }
        }
    }

    @Test
    @DisplayName("Tokens are correct in data files")
    public void testScanner() throws IOException {
        File[] files = getStreamsByExtension(".data", true);
        assertTrue(files.length > 0, "No test files found.");
        for (File file : files) {
            List<String> tokens = new ArrayList<String>();
            InputStream input = new FileInputStream(file);
            LoadSettings settings = LoadSettings.builder().build();
            StreamReader reader = new StreamReader(settings, new YamlUnicodeReader(input));
            Scanner scanner = new ScannerImpl(settings, reader);
            try {
                while (scanner.checkToken()) {
                    Token token = scanner.next();
                    tokens.add(token.getClass().getName());
                }
            } catch (RuntimeException e) {
                System.out.println("File name: \n" + file.getName());
                String data = getResource(file.getName());
                System.out.println("Data: \n" + data);
                System.out.println("Tokens:");
                for (String token : tokens) {
                    System.out.println(token);
                }
                fail("Cannot scan: " + file + "; " + e.getMessage());
            } finally {
                input.close();
            }
        }
    }
}
