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
package org.snakeyaml.engine.v2.api;

import org.jspecify.annotations.NonNull;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.NonPrintableStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.exceptions.EmitterException;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.schema.JsonSchema;
import org.snakeyaml.engine.v2.schema.Schema;
import org.snakeyaml.engine.v2.serializer.AnchorGenerator;
import org.snakeyaml.engine.v2.serializer.NumberAnchorGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable configuration for serialization. Description for all the fields can be found in the
 * builder
 */
public record DumpSettings(

    boolean explicitStart,
    boolean explicitEnd,
    NonPrintableStyle nonPrintableStyle,
    Tag explicitRootTag,
    AnchorGenerator anchorGenerator,
    SpecVersion yamlDirective,
    Map<String, String> tagDirective,
    FlowStyle defaultFlowStyle,
    ScalarStyle defaultScalarStyle,
    Boolean dereferenceAliases,
    // emitter
    boolean canonical,
    boolean multiLineFlow,
    boolean useUnicodeEncoding,
    int indent,
    int indicatorIndent,
    int width,
    String bestLineBreak,
    boolean splitLines,
    int maxSimpleKeyLength,
    boolean indentWithIndicator,
    boolean dumpComments,
    Schema schema,
    // general
    Map<SettingKey, Object> customProperties
) {

    public static Builder builder() {
        return new Builder();
    }

    public Object getCustomProperty(SettingKey key) {
        return customProperties.get(key);
    }

    /**
     * Builder pattern implementation for DumpSettings
     */
    public static final class Builder {

        private boolean explicitStart;
        private boolean explicitEnd;
        private NonPrintableStyle nonPrintableStyle = NonPrintableStyle.ESCAPE;
        private Tag explicitRootTag;
        private AnchorGenerator anchorGenerator = new NumberAnchorGenerator(0);
        private SpecVersion yamlDirective;
        private Map<String, String> tagDirective = new HashMap<>();
        private FlowStyle defaultFlowStyle = FlowStyle.AUTO;
        private ScalarStyle defaultScalarStyle = ScalarStyle.PLAIN;
        private boolean dereferenceAliases;
        // emitter
        private boolean canonical;
        private boolean multiLineFlow;
        private boolean useUnicodeEncoding = true;
        private int indent = 2;
        private int indicatorIndent;
        private int width = 80;
        private String bestLineBreak = "\n";
        private boolean splitLines = true;
        private int maxSimpleKeyLength = 128;
        private boolean indentWithIndicator;
        private boolean dumpComments;
        private Schema schema = new JsonSchema();
        Map<SettingKey, Object> customProperties = new HashMap<>();

        /**
         * Define flow style
         *
         * @param defaultFlowStyle - specify the style
         * @return the builder with the provided value
         */
        public Builder setDefaultFlowStyle(FlowStyle defaultFlowStyle) {
            this.defaultFlowStyle = defaultFlowStyle;
            return this;
        }

        /**
         * Define default scalar style
         *
         * @param defaultScalarStyle - specify the scalar style
         * @return the builder with the provided value
         */
        public Builder setDefaultScalarStyle(ScalarStyle defaultScalarStyle) {
            this.defaultScalarStyle = defaultScalarStyle;
            return this;
        }

        /**
         * Add '---' in the beginning of the document
         *
         * @param explicitStart - true if the document start must be explicitly indicated
         * @return the builder with the provided value
         */
        public Builder setExplicitStart(boolean explicitStart) {
            this.explicitStart = explicitStart;
            return this;
        }

        /**
         * Define anchor name generator (by default 'id' + number)
         *
         * @param anchorGenerator - specified function to create anchor names
         * @return the builder with the provided value
         */
        public Builder setAnchorGenerator(@NonNull AnchorGenerator anchorGenerator) {
            this.anchorGenerator = anchorGenerator;
            return this;
        }

        /**
         * Define root {@link Tag} or let the tag be detected automatically
         *
         * @param explicitRootTag - specify the root tag
         * @return the builder with the provided value
         */
        public Builder setExplicitRootTag(@NonNull Tag explicitRootTag) {
            this.explicitRootTag = explicitRootTag;
            return this;
        }

        /**
         * Add '...' in the end of the document
         *
         * @param explicitEnd - true if the document end must be explicitly indicated
         * @return the builder with the provided value
         */
        public Builder setExplicitEnd(boolean explicitEnd) {
            this.explicitEnd = explicitEnd;
            return this;
        }

        /**
         * Add YAML <a href="http://yaml.org/spec/1.2/spec.html#id2782090">directive</a>
         *
         * @param yamlDirective - the version to be used in the directive
         * @return the builder with the provided value
         */
        public Builder setYamlDirective(@NonNull SpecVersion yamlDirective) {
            this.yamlDirective = yamlDirective;
            return this;
        }

        /**
         * Add TAG <a href="http://yaml.org/spec/1.2/spec.html#id2782090">directive</a>
         *
         * @param tagDirective - the data to create TAG directive
         * @return the builder with the provided value
         */
        public Builder setTagDirective(@NonNull Map<String, String> tagDirective) {
            this.tagDirective = tagDirective;
            return this;
        }

        /**
         * Enforce canonical representation
         *
         * @param canonical - specify if the canonical representation must be used
         * @return the builder with the provided value
         */
        public Builder setCanonical(boolean canonical) {
            this.canonical = canonical;
            return this;
        }

        /**
         * Use pretty flow style when every value in the flow context gets a separate line.
         *
         * @param multiLineFlow - set false to output all values in a single line.
         * @return the builder with the provided value
         */
        public Builder setMultiLineFlow(boolean multiLineFlow) {
            this.multiLineFlow = multiLineFlow;
            return this;
        }

        /**
         * Specify whether to emit non-ASCII printable Unicode characters (emit Unicode char or escape
         * sequence starting with '\\u') The default value is true. When set to false then printable
         * non-ASCII characters (Cyrillic, Chinese etc) will be not printed but escaped (to support ASCII
         * terminals)
         *
         * @param useUnicodeEncoding - true to use Unicode for "Я", false to use "\u0427" for the same
         *     char (if useUnicodeEncoding is false then all non-ASCII characters are escaped)
         * @return the builder with the provided value
         */
        public Builder setUseUnicodeEncoding(boolean useUnicodeEncoding) {
            this.useUnicodeEncoding = useUnicodeEncoding;
            return this;
        }

        /**
         * Define the amount of the spaces for the indent in the block flow style. Default is 2.
         *
         * @param indent - the number of spaces. Must be within the range
         *     org.snakeyaml.engine.v2.emitter.Emitter.MIN_INDENT and
         *     org.snakeyaml.engine.v2.emitter.Emitter.MAX_INDENT
         * @return the builder with the provided value
         */
        public Builder setIndent(int indent) {
            if (indent < Emitter.MIN_INDENT) {
                throw new EmitterException("Indent must be at least " + Emitter.MIN_INDENT);
            }
            if (indent > Emitter.MAX_INDENT) {
                throw new EmitterException("Indent must be at most " + Emitter.MAX_INDENT);
            }
            this.indent = indent;
            return this;
        }

        /**
         * It adds the specified indent for sequence indicator in the block flow. Default is 0. For better
         * visual results it should be by 2 less than the indent (which is 2 by default) It is 2 chars
         * less because the first char is '-' and the second char is the space after it.
         *
         * @param indicatorIndent - must be non-negative and less than
         *     org.snakeyaml.engine.v2.emitter.Emitter.MAX_INDENT - 1
         * @return the builder with the provided value
         */
        public Builder setIndicatorIndent(int indicatorIndent) {
            if (indicatorIndent < 0) {
                throw new EmitterException("Indicator indent must be non-negative");
            }
            if (indicatorIndent > Emitter.MAX_INDENT - 1) {
                throw new EmitterException("Indicator indent must be at most Emitter.MAX_INDENT-1: " + (Emitter.MAX_INDENT - 1));
            }
            this.indicatorIndent = indicatorIndent;
            return this;
        }

        /**
         * Set max width for literal scalars. When the scalar representation takes more then the preferred
         * with the scalar will be split into a few lines. The default is 80.
         *
         * @param width - the width
         * @return the builder with the provided value
         */
        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        /**
         * If the YAML is created for another platform (for instance on Windows to be consumed under
         * Linux) than this setting is used to define the line ending. The platform line end is used by
         * default.
         *
         * @param bestLineBreak - "\r\n" or "\n"
         * @return the builder with the provided value
         */
        public Builder setBestLineBreak(@NonNull String bestLineBreak) {
            this.bestLineBreak = bestLineBreak;
            return this;
        }

        /**
         * Define whether to split long lines
         *
         * @param splitLines - true to split long lines
         * @return the builder with the provided value
         */
        public Builder setSplitLines(boolean splitLines) {
            this.splitLines = splitLines;
            return this;
        }

        /**
         * Define max key length to use simple key (without '?').
         * <a href="https://yaml.org/spec/1.2/spec.html#id2798057">More info</a>
         *
         * @param maxSimpleKeyLength - the limit after which the key gets explicit key indicator '?'
         * @return the builder with the provided value
         */
        public Builder setMaxSimpleKeyLength(int maxSimpleKeyLength) {
            if (maxSimpleKeyLength > 1024) {
                throw new YamlEngineException("The simple key must not span more than 1024 stream characters. See https://yaml.org/spec/1.2/spec.html#id2798057");
            }
            this.maxSimpleKeyLength = maxSimpleKeyLength;
            return this;
        }

        /**
         * When String object contains non-printable characters, they are escaped with \\u or \\x
         * notation. Sometimes it is better to transform this data to binary (with the !!binary tag).
         * String objects with printable data are non affected by this setting.
         *
         * @param nonPrintableStyle - set this to BINARY to force non-printable String to represent as
         *     binary (byte array)
         * @return the builder with the provided value
         */
        public Builder setNonPrintableStyle(NonPrintableStyle nonPrintableStyle) {
            this.nonPrintableStyle = nonPrintableStyle;
            return this;
        }

        /**
         * Custom property is the way to give some runtime parameters to be used during dumping
         *
         * @param key - the key
         * @param value - the value behind the key
         * @return the builder with the provided value
         */
        public Builder setCustomProperty(SettingKey key, Object value) {
            customProperties.put(key, value);
            return this;
        }

        /**
         * Set to true to add the indent for sequences to the general indent
         *
         * @param indentWithIndicator - true when indent for sequences is added to general
         * @return the builder with the provided value
         */
        public Builder setIndentWithIndicator(boolean indentWithIndicator) {
            this.indentWithIndicator = indentWithIndicator;
            return this;
        }

        /**
         * Set to true to add comments from Nodes to
         *
         * @param dumpComments - true when comments should be dumped (serialised)
         * @return the builder with the provided value
         */
        public Builder setDumpComments(boolean dumpComments) {
            this.dumpComments = dumpComments;
            return this;
        }

        /**
         * Provide either recommended or custom
         * <a href="https://yaml.org/spec/1.2.2/#chapter-10-recommended-schemas">schema</a> instead of
         * default {@link org.snakeyaml.engine.v2.schema.JsonSchema}. These 3 are available
         * {@link org.snakeyaml.engine.v2.schema.FailsafeSchema},
         * {@link org.snakeyaml.engine.v2.schema.JsonSchema},
         * {@link org.snakeyaml.engine.v2.schema.CoreSchema}.
         *
         * @param schema - the tag schema
         * @return the builder with the provided value
         */
        public Builder setSchema(Schema schema) {
            this.schema = schema;
            return this;
        }

        /**
         * Disable usage of anchors and aliases while serialising an instance. Recursive objects will not
         * work when they are disabled. (Forces Serializer to skip emitting anchors names, emit Node
         * content instead of Alias, fail with SerializationException if serialized structure is
         * recursive.)
         *
         * @param dereferenceAliases - true to use copies of objects instead of references to the same
         *     instance
         * @return the builder with the provided value
         */
        public Builder setDereferenceAliases(Boolean dereferenceAliases) {
            this.dereferenceAliases = dereferenceAliases;
            return this;
        }

        /**
         * Create immutable DumpSettings
         *
         * @return DumpSettings with the provided values
         */
        public DumpSettings build() {
            return new DumpSettings(
                explicitStart,
                explicitEnd,
                nonPrintableStyle,
                explicitRootTag,
                anchorGenerator,
                yamlDirective,
                tagDirective,
                defaultFlowStyle,
                defaultScalarStyle,
                dereferenceAliases,
                canonical,
                multiLineFlow,
                useUnicodeEncoding,
                indent,
                indicatorIndent,
                width,
                bestLineBreak,
                splitLines,
                maxSimpleKeyLength,
                indentWithIndicator,
                dumpComments,
                schema,
                customProperties
            );
        }
    }
}
