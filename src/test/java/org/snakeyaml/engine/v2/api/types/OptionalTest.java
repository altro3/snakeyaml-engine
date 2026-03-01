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
package org.snakeyaml.engine.v2.api.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@org.junit.jupiter.api.Tag("fast")
class OptionalTest {

  @Test
  @DisplayName("Represent Optional as value")
  void representOptional() {
    var standardRepresenter = new StandardRepresenter(DumpSettings.builder().build());
    Node node = standardRepresenter.represent("a");
    assertEquals("tag:yaml.org,2002:java.util.Optional", node.getTag().getValue());
  }

  @Test
  @DisplayName("Represent Optional.empty as null")
  void representEmptyOptional() {
    var standardRepresenter = new StandardRepresenter(DumpSettings.builder().build());
    Node node = standardRepresenter.represent(null);
    assertEquals("tag:yaml.org,2002:null", node.getTag().getValue());
  }

  @Test
  @DisplayName("Dump Optional as its value")
  void dumpOptional() {
    var settings = DumpSettings.builder().build();
    var dump = new Dump(settings);
    String str = dump.dumpToString("a");
    assertEquals("!!java.util.Optional 'a'\n", str);
  }

  @Test
  @DisplayName("Dump empty Optional as null")
  void dumpEmptyOptional() {
    var settings = DumpSettings.builder().build();
    var dump = new Dump(settings);
    String str = dump.dumpToString(null);
    assertEquals("null\n", str);
  }

  @Test
  @DisplayName("Dump Optionals")
  void dumpListOfOptional() {
    var settings = DumpSettings.builder().build();
    var dump = new Dump(settings);
    String str = dump.dumpToString(Arrays.asList(Optional.of(2), null, Optional.of("a")));
    assertEquals("[!!java.util.Optional '2', null, !!java.util.Optional 'a']\n", str);
  }

  @Test
  @DisplayName("Dump Optionals")
  void dumpListOfOptional2() {
    var settings = DumpSettings.builder().build();
    var dump = new Dump(settings);
    String str = dump.dumpToString(Optional.of(List.of(1, 2)));
    assertEquals("!!java.util.Optional [1, 2]\n", str);
  }

  // parse
  @Test
  @DisplayName("Optional 'a' is parsed")
  void parseOptional() {
    var settings = LoadSettings.builder().build();
    var load = new Load(settings);
    var str = (Optional<String>) load.loadFromString("!!java.util.Optional a");
    assertEquals(Optional.of("a"), str);
  }

  @Test
  @DisplayName("Empty Optional parsed")
  void parseEmptyOptional() {
    var settings = LoadSettings.builder().build();
    var load = new Load(settings);
    var str = (Optional<String>) load.loadFromString("!!java.util.Optional null");
    assertNull(str);
  }

  @Test
  @DisplayName("Empty Optional parsed")
  void parseEmptyOptional2() {
    var settings = LoadSettings.builder().build();
    var load = new Load(settings);
    var str = (Optional<String>) load.loadFromString("!!java.util.Optional ");
    assertNull(str);
  }
}
