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

import org.jspecify.annotations.NonNull;

import java.io.Serializable;

/**
 * YAML Version indicator
 */
public enum SpecVersion implements Serializable {

    EMPTY(0, 0),
    V_1_0(1, 0),
    V_1_1(1, 1),
    V_1_2(1, 2),
    ;

    /**
     * keep major
     */
    private final int major;
    /**
     * keep minor
     */
    private final int minor;
    /**
     * String representation like "1.2"
     */
    private final String representation;

    /**
     * Create
     *
     * @param major - major part ov version
     * @param minor - minor part of version
     */
    SpecVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
        representation = major + "." + minor;
    }

    /**
     * getter
     *
     * @return 1
     */
    public int getMajor() {
        return major;
    }

    /**
     * getter
     *
     * @return 0 or 1
     */
    public int getMinor() {
        return minor;
    }

    /**
     * create readable text
     *
     * @return text
     */
    public String getRepresentation() {
        return representation;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public static @NonNull SpecVersion findVersion(@NonNull Integer major, @NonNull Integer minor) {
        return switch (minor) {
            case 0 -> V_1_0;
            case 1 -> V_1_1;
            case 2 -> V_1_2;
            default -> throw new IllegalArgumentException("Unknown YAML specification version: " + major + "." + minor);
        };
    }

    @Override
    public String toString() {
        return "Version{" + "major=" + major + ", minor=" + minor + '}';
    }
}
