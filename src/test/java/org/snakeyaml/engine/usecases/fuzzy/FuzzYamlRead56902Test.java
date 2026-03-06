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
import org.snakeyaml.engine.v2.exceptions.ScannerException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

/**
 * <a href="https://github.com/FasterXML/jackson-dataformats-text/issues/406">link</a>
 * <a href="https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=56902">link</a>
 */
@org.junit.jupiter.api.Tag("fast")
class FuzzYamlRead56902Test {

    @Test
    void testHugeMinorValue() {
        var e = assertThrows(ScannerException.class, () -> DEFAULT_LOAD.loadFromString("%YAML 1.9224775801"));
        assertTrue(e.getMessage().contains("found a number which cannot represent a valid version: 9224775801"), e.getMessage());
    }

    @Test
    void testHugeMajorValue() {
        var e = assertThrows(ScannerException.class, () -> DEFAULT_LOAD.loadFromString("%YAML 100651234565.1"));
        assertTrue(e.getMessage().contains("found a number which cannot represent a valid version: 100651234565"), e.getMessage());
    }
}
