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
package org.snakeyaml.engine.v2.nodes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.exceptions.Mark;

/**
 * Represents a sequence.
 * <p>
 * A sequence is a ordered collection of nodes.
 * </p>
 */
public class SequenceNode extends CollectionNode<Node> {

  private final List<Node> value;

  public SequenceNode(Tag tag, boolean resolved, List<Node> value, FlowStyle flowStyle,
      Mark startMark, Mark endMark) {
    super(tag, flowStyle, startMark, endMark);
    Objects.requireNonNull(value, "value in a Node is required.");
    this.value = value;
    this.resolved = resolved;
  }

  public SequenceNode(Tag tag, List<Node> value, FlowStyle flowStyle) {
    this(tag, true, value, flowStyle, null, null);
  }

  @Override
  public NodeType getNodeType() {
    return NodeType.SEQUENCE;
  }

  /**
   * Returns the elements in this sequence.
   *
   * @return Nodes in the specified order.
   */
  @Override
  public List<Node> getValue() {
    return value;
  }

  @Override
  public String toString() {
    var buf = new StringBuilder();
    var isFirst = true;
    for (Node node : value) {
      if (isFirst) {
        buf.append(',');
      }
      if (node instanceof CollectionNode) {
        // to avoid overflow in case of recursive structures
        buf.append(System.identityHashCode(node));
      } else {
        buf.append(node.toString());
      }
      isFirst = false;
    }
    return "<SequenceNode (tag=" + tag + ", value=[" + buf + "])>";
  }
}
