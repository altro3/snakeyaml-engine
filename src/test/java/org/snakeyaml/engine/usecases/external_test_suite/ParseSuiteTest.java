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
package org.snakeyaml.engine.usecases.external_test_suite;

import com.google.common.collect.Streams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.usecases.external_test_suite.SuiteUtils.ParseResult;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Parse;
import org.snakeyaml.engine.v2.events.Event;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@org.junit.jupiter.api.Tag("fast")
class ParseSuiteTest {

    // TODO FIXME JEF9-02 is not according to the spec
    private final List<SuiteData> all = SuiteUtils.getAll().stream()
        .filter(data -> !data.getName().equals("JEF9-02"))
        .toList();

    /**
     * This test is used to debug one test (which is given explicitly)
     */
    @Test
    @DisplayName("Parse: Run one test")
    void runOne() {
        var data = SuiteUtils.getOne("Y79Y-002");
        var settings = LoadSettings.builder()
            .setLabel(data.getLabel())
            .build();
        Iterable<Event> iterable = new Parse(settings)
            .parseString(data.getInput());
        for (Event event : iterable) {
            assertNotNull(event);
            // System.out.println(event);
        }
    }

    @Test
    @DisplayName("Run comprehensive test suite")
    void runAll() {
        for (SuiteData data : all) {
            ParseResult result = SuiteUtils.parseData(data);
            boolean shouldFail = data.hasError();
            if (SuiteUtils.deviationsWithSuccess.contains(data.getName())
                || SuiteUtils.deviationsWithError.contains(data.getName())) {
                shouldFail = !shouldFail;
            }
            if (shouldFail) {
                assertNotNull(result.error(), "Expected error, but got none in file " + data.getName() + ", " + data.getLabel() + "\n" + result.events());
            } else {
                assertNull(result.error(), "Testcase: " + data.getName() + "; label: " + data.getLabel() + "\nExpected NO error, but got: " + result.error());
                List<ParsePair> pairs = Streams.zip(data.getEvents().stream(), result.events().stream(), ParsePair::new)
                    .toList();
                for (ParsePair pair : pairs) {
                    var representation = new EventRepresentation(pair.event());
                    assertEquals(pair.expected(), representation.getRepresentation(), "Failure in " + data.getName());
                }
            }
        }
    }

    record ParsePair(
        String expected,
        Event event
    ) {

    }
}
