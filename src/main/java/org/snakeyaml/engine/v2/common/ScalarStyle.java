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
package org.snakeyaml.engine.v2.common;

/**
 * YAML provides a rich set of scalar styles. Block scalar styles include the literal style and the
 * folded style; flow scalar styles include the plain style and two quoted styles, the single-quoted
 * style and the double-quoted style. These styles offer a range of trade-offs between expressive
 * power and readability.
 */
public enum ScalarStyle {
  /**
   * Double quoted scalar
   */
  DOUBLE_QUOTED('"'),
  /**
   * Single quoted scalar
   */
  SINGLE_QUOTED('\''),
  /**
   * Literal scalar
   */
  LITERAL('|'),
  /**
   * Folded scalar
   */
  FOLDED('>'),
  /**
   * Mixture of scalar styles to dump JSON format. Double-quoted style for !!str, !!binary,
   * !!timestamp. Plain style - for !!bool, !!float, !!int, !!null
   *
   * These are never dumped - !!merge, !!value, !!yaml
   */
  JSON_SCALAR_STYLE('J'),
  /**
   * Plain scalar
   */
  PLAIN(null);

  private final Character styleOpt;

  ScalarStyle(Character style) {
    this.styleOpt = style;
  }

  @Override
  public String toString() {
    return styleOpt != null ? styleOpt.toString() : ":";
  }
}
