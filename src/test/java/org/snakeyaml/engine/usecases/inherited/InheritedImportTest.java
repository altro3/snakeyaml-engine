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

import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.snakeyaml.engine.v2.util.TestUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD_SETTINGS;

public abstract class InheritedImportTest {

    public static final String PATH = "inherited_yaml_1_1";

    protected String getResource(String theName) {
        return TestUtils.getResource(PATH + File.separator + theName);
    }

    protected File[] getStreamsByExtension(String extension) {
        return getStreamsByExtension(extension, false);
    }

    protected File[] getStreamsByExtension(String extension, boolean onlyIfCanonicalPresent) {
        var file = new File("src/test/resources/" + PATH);
        assertTrue(file.exists(), "Folder not found: " + file.getAbsolutePath());
        assertTrue(file.isDirectory());
        return file.listFiles(new InheritedFilenameFilter(extension, onlyIfCanonicalPresent));
    }

    protected File getFileByName(String name) {
        var file = new File("src/test/resources/" + PATH + "/" + name);
        assertTrue(file.exists(), "Folder not found: " + file.getAbsolutePath());
        assertTrue(file.isFile());
        return file;
    }

    protected List<Event> canonicalParse(InputStream input2, String label) throws IOException {
        LoadSettings setting = LoadSettings.builder()
            .setLabel(label)
            .build();
        var reader = new StreamReader(setting, new YamlUnicodeReader(input2));
        var buffer = new StringBuilder();
        while (reader.peek() != '\0') {
            buffer.appendCodePoint(reader.peek());
            reader.forward();
        }
        var parser = new CanonicalParser(buffer.toString().replace(System.lineSeparator(), "\n"), label);
        var result = new ArrayList<Event>();
        while (parser.hasNext()) {
            result.add(parser.next());
        }
        input2.close();
        return result;
    }

    protected List<Event> parse(InputStream input) throws IOException {
        var reader = new StreamReader(DEFAULT_LOAD_SETTINGS, new YamlUnicodeReader(input));
        var parser = new ParserImpl(DEFAULT_LOAD_SETTINGS, reader);
        var result = new ArrayList<Event>();
        while (parser.hasNext()) {
            result.add(parser.next());
        }
        input.close();
        return result;
    }

    private record InheritedFilenameFilter(
        String extension,
        boolean onlyIfCanonicalPresent
    ) implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            int position = name.lastIndexOf('.');
            String canonicalFileName = name.substring(0, position) + ".canonical";
            var canonicalFile = new File(dir, canonicalFileName);
            if (onlyIfCanonicalPresent && !canonicalFile.exists()) {
                return false;
            }
            return name.endsWith(extension);
        }
    }
}
