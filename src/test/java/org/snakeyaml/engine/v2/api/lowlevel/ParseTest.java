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
package org.snakeyaml.engine.v2.api.lowlevel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.StreamEndEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.util.TestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("fast")
class ParseTest {

  @Test
  void parseEmptyReader() throws IOException {
    var parse = new Parse(LoadSettings.builder().build());
    Iterable<Event> events = parse.parseReader(new StringReader(""));
    var list = new ArrayList<Event>();
    events.forEach(list::add);
    assertEquals(2, list.size());
    TestUtils.compareEvents(List.of(new StreamStartEvent(), new StreamEndEvent()), list);
  }

  @Test
  void parseEmptyInputStream() {
    var parse = new Parse(LoadSettings.builder().build());
    Iterable<Event> events = parse.parseInputStream(new ByteArrayInputStream("".getBytes()));
    var list = new ArrayList<Event>();
    events.forEach(list::add);
    assertEquals(2, list.size());
    TestUtils.compareEvents(List.of(new StreamStartEvent(), new StreamEndEvent()), list);
  }
}
