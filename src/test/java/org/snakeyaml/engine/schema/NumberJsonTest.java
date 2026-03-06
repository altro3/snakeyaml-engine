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
package org.snakeyaml.engine.schema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.snakeyaml.engine.util.TestUtil.DEFAULT_LOAD;

@org.junit.jupiter.api.Tag("fast")
public class NumberJsonTest {

    @Test
    @DisplayName("Test all integers which are define in the core schema & JSON")
    void parseInteger() {
        assertEquals(1, DEFAULT_LOAD.loadFromString("1"));
        assertEquals(-1, DEFAULT_LOAD.loadFromString("-1"));
        assertEquals(0, DEFAULT_LOAD.loadFromString("0"));
        assertEquals(0, DEFAULT_LOAD.loadFromString("-0"));
        assertEquals("012", DEFAULT_LOAD.loadFromString("012"), "Leading zeros are not allowed.");
        assertEquals(1234567890, DEFAULT_LOAD.loadFromString("1234567890"));
        assertEquals(12345678901L, DEFAULT_LOAD.loadFromString("12345678901"));
        assertEquals(new BigInteger("1234567890123456789123"), DEFAULT_LOAD.loadFromString("1234567890123456789123"));
    }

    @Test
    @DisplayName("Test all strings which WERE integers or doubles in YAML 1.1")
    void parseString() {
        assertEquals("12:10:02", DEFAULT_LOAD.loadFromString("12:10:02"));
        assertEquals("0b1010", DEFAULT_LOAD.loadFromString("0b1010"));
        assertEquals("0xFF", DEFAULT_LOAD.loadFromString("0xFF"));
        assertEquals("1_000", DEFAULT_LOAD.loadFromString("1_000"));

        assertEquals("1_000.5", DEFAULT_LOAD.loadFromString("1_000.5"));
        assertEquals("+.inf", DEFAULT_LOAD.loadFromString("+.inf"));

        // start with +
        assertEquals("+1", DEFAULT_LOAD.loadFromString("+1"));
        assertEquals("+1223344", DEFAULT_LOAD.loadFromString("+1223344"));
        assertEquals("+12.23344", DEFAULT_LOAD.loadFromString("+12.23344"));
        assertEquals("+0.23344", DEFAULT_LOAD.loadFromString("+0.23344"));
        assertEquals("+0", DEFAULT_LOAD.loadFromString("+0"));

        // leading zero
        assertEquals("03", DEFAULT_LOAD.loadFromString("03"));
        assertEquals("03.67", DEFAULT_LOAD.loadFromString("03.67"));
        assertEquals("3.6", DEFAULT_LOAD.loadFromString("! 3.6"));
        assertEquals("3", DEFAULT_LOAD.loadFromString("! 3"));
    }

    @Test
    @DisplayName("Test all doubles which are define in the core schema & JSON")
    void parseDouble() {
        assertEquals(-1.345, DEFAULT_LOAD.loadFromString("-1.345"));
        assertEquals(0D, DEFAULT_LOAD.loadFromString("0.0"));
        assertEquals(0D, DEFAULT_LOAD.loadFromString("0.0"));
        assertEquals(0D, DEFAULT_LOAD.loadFromString("0.0"));
        assertEquals(+0D, DEFAULT_LOAD.loadFromString("0.0"));
        assertEquals(-0D, DEFAULT_LOAD.loadFromString("-0.0"));
        assertEquals(0.123, DEFAULT_LOAD.loadFromString("0.123"));
        assertEquals(1.23E-6, DEFAULT_LOAD.loadFromString("1.23e-6"));
        assertEquals(1.23E6, DEFAULT_LOAD.loadFromString("1.23e+6"));
        assertEquals(1.23E6, DEFAULT_LOAD.loadFromString("1.23e6"));
        assertEquals(1.23, DEFAULT_LOAD.loadFromString("1.23E0"));
        assertEquals(-1.23E6, DEFAULT_LOAD.loadFromString("-1.23e6"));
        assertEquals(1000.25, DEFAULT_LOAD.loadFromString("1000.25"));
        assertEquals(9000D, DEFAULT_LOAD.loadFromString("9000.00"));
        assertEquals(1D, DEFAULT_LOAD.loadFromString("1."));
    }

    @Test
    @DisplayName("Parse special doubles which are defined in the JSON schema, but not in JSON")
    void parseDoubleSpecial() {
        assertEquals(Double.POSITIVE_INFINITY, DEFAULT_LOAD.loadFromString(".inf"));
        assertEquals(Double.NEGATIVE_INFINITY, DEFAULT_LOAD.loadFromString("-.inf"));
        assertEquals(Double.NaN, DEFAULT_LOAD.loadFromString(".nan"));

        assertEquals(".INF", DEFAULT_LOAD.loadFromString(".INF"));
        assertEquals(".NAN", DEFAULT_LOAD.loadFromString(".NAN"));
    }

    @Test
    @DisplayName("Dump special doubles which are defined in the JSON schema, but not in JSON")
    void dumpDoubleSpecial() {
        Dump dumper = new Dump(DumpSettings.builder().build());
        assertEquals(".inf\n", dumper.dumpToString(Double.POSITIVE_INFINITY));
        assertEquals(".inf\n", dumper.dumpToString(Float.POSITIVE_INFINITY));

        assertEquals("-.inf\n", dumper.dumpToString(Double.NEGATIVE_INFINITY));
        assertEquals("-.inf\n", dumper.dumpToString(Float.NEGATIVE_INFINITY));

        assertEquals(".nan\n", dumper.dumpToString(Double.NaN));
        assertEquals(".nan\n", dumper.dumpToString(Float.NaN));
    }
}
