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
package org.snakeyaml.engine.usecases.indentation;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.util.TestUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
class IndentWithIndicatorTest {

    @Test
    void testIndentWithIndicator1() {
        var dumper = new Dump(DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndentWithIndicator(true)
            .setIndent(2)
            .setIndicatorIndent(1)
            .build());
        String output = dumper.dumpToString(createData());

        String doc = TestUtils.getResource("indentation/issue416-1.yaml");

        assertEquals(doc, output);
    }

    @Test
    void testIndentWithIndicator2() {
        var dumper = new Dump(DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndentWithIndicator(true)
            .setIndent(2)
            .setIndicatorIndent(2)
            .build());
        String output = dumper.dumpToString(createData());

        String doc = TestUtils.getResource("indentation/issue416-2.yaml");

        assertEquals(doc, output);
    }

    @Test
    void testIndentWithIndicator3() {
        DumpSettings settings = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndentWithIndicator(false)
            .setIndent(4)
            .setIndicatorIndent(2)
            .build();

        Dump dumper = new Dump(settings);
        String output = dumper.dumpToString(createData());

        String doc = TestUtils.getResource("indentation/issue416_3.yaml");

        assertEquals(doc, output);
    }

    private Map<String, Object> createData() {
        var fred = new LinkedHashMap<String, String>();
        fred.put("name", "Fred");
        fred.put("role", "creator");

        var john = new LinkedHashMap<String, String>();
        john.put("name", "John");
        john.put("role", "committer");

        var developers = new ArrayList<Map<String, String>>();
        developers.add(fred);
        developers.add(john);

        var company = new LinkedHashMap<String, Object>();
        company.put("developers", developers);
        company.put("name", "Yet Another Company");
        company.put("location", "Maastricht");

        var data = new LinkedHashMap<String, Object>();
        data.put("company", company);

        return data;
    }
}
