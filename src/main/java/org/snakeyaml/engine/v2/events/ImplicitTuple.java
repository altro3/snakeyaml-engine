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
package org.snakeyaml.engine.v2.events;

/**
 * The implicit flag of a scalar event is a pair of boolean values that indicate if the tag may be
 * omitted when the scalar is emitted in a plain and non-plain style correspondingly.
 */
public enum ImplicitTuple {

    TRUE_TRUE(true, true),
    TRUE_FALSE(true, false),
    FALSE_TRUE(false, true),
    FALSE_FALSE(false, false),
    ;

    private final boolean canOmitTagInPlainScalar;
    private final boolean canOmitTagInNonPlainScalar;

    ImplicitTuple(boolean canOmitTagInPlainScalar, boolean canOmitTagInNonPlainScalar) {
        this.canOmitTagInPlainScalar = canOmitTagInPlainScalar;
        this.canOmitTagInNonPlainScalar = canOmitTagInNonPlainScalar;
    }

    public boolean isCanOmitTagInPlainScalar() {
        return canOmitTagInPlainScalar;
    }

    public boolean isCanOmitTagInNonPlainScalar() {
        return canOmitTagInNonPlainScalar;
    }

    public boolean bothFalse() {
        return !canOmitTagInPlainScalar && !canOmitTagInNonPlainScalar;
    }

    public static ImplicitTuple byValues(boolean canOmitTagInPlainScalar, boolean canOmitTagInNonPlainScalar) {
        if (canOmitTagInPlainScalar && canOmitTagInNonPlainScalar) {
            return TRUE_TRUE;
        } else if (canOmitTagInPlainScalar) {
            return TRUE_FALSE;
        } else if (canOmitTagInNonPlainScalar) {
            return FALSE_TRUE;
        } else {
            return FALSE_FALSE;
        }
    }
}
