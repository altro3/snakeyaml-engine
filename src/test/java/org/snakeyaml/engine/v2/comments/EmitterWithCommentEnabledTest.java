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
package org.snakeyaml.engine.v2.comments;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.events.CommentEvent;
import org.snakeyaml.engine.v2.events.DocumentEndEvent;
import org.snakeyaml.engine.v2.events.DocumentStartEvent;
import org.snakeyaml.engine.v2.events.ImplicitTuple;
import org.snakeyaml.engine.v2.events.MappingEndEvent;
import org.snakeyaml.engine.v2.events.MappingStartEvent;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.SequenceEndEvent;
import org.snakeyaml.engine.v2.events.SequenceStartEvent;
import org.snakeyaml.engine.v2.events.StreamEndEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.snakeyaml.engine.v2.serializer.Serializer;
import org.snakeyaml.engine.v2.util.StreamToStringWriter;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmitterWithCommentEnabledTest {

    @Test
    void testEmpty() {
        String data = "";

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testWithOnlyComment() {
        String data = "# Comment\n\n";

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testCommentEndingALine() {
        String data = """
            key: # Comment
              value
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testMultiLineComment() {
        String data = """
            key: # Comment
                 # lines
              value
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testBlankLine() {
        String data = "\n";

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testBlankLineComments() {
        String data = """
            
            abc: def # comment
            
            
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testBlockScalar() {
        String data = """
            abc: | # Comment
              def
              hij
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testDirectiveLineEndComment() {
        String data = "%YAML 1.1 #Comment\n---\n";

        String result = runEmitterWithCommentsEnabled(data);
        // We currently strip Directive comments
        assertEquals("", result);
    }

    @Test
    void testSequence() {
        String data = """
            # Comment
            list: # InlineComment1
              - # Block Comment
                item # InlineComment2
            # Comment
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    /**
     * @see <a href="https://bitbucket.org/snakeyaml/snakeyaml-engine/issues/64/emitting-an-empty-string-as-a-list-element">issue 64</a>
     */
    @Test
    void testSequenceEmptyString() {
        String data = """
            # Comment
            list: # InlineComment1
              - # Block Comment
                '' # InlineComment2
            # Comment
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testAllComments1() {
        String data = """
            # Block Comment1
            # Block Comment2
            key: # Inline Comment1a
                 # Inline Comment1b
              # Block Comment3a
              # Block Comment3b
              value # Inline Comment2
            # Block Comment4
            list: # InlineComment3a
                  # InlineComment3b
              - # Block Comment5
                item1 # InlineComment4
              - item2: [value2a, value2b] # InlineComment5
              - item3: {key3a: [value3a1, value3a2], key3b: value3b} # InlineComment6
            # Block Comment6
            ---
            # Block Comment7
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testMultiDoc() {
        String data = """
            key: value
            # Block Comment
            ---
            # Block Comment
            key: value
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testAllComments2() {
        String data = """
            key:
              key:
                key:
                - # Block Comment1
                  item1a
                - # Block Comment2
                - item1b
                - # Block Comment3
                  MapKey_1: MapValue1
                  MapKey_2: MapValue2
            key2:
            - # Block Comment4
              # Block Comment5
              item1 # Inline Comment1a
                    # Inline Comment1b
            - # Block Comment6a
              # Block Comment6b
              item2: value # Inline Comment2
            # Block Comment7
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testAllComments3() {
        String data = """
            # Block Comment1
            [item1, {item2: value2}, {item3: value3}] # Inline Comment1
            # Block Comment2
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testKeepingNewLineInsideSequence() {
        String data = "\n" + "key:\n" +
            // " \n" + // only supported in a sequence right now
            "- item1\n" +
            // "\n" + // Per Spec this is part of plain scalar above
            "- item2\n" +
            // "\n" + // Per Spec this is part of plain scalar above
            "- item3\n" + "\n" + "key2: value2\n" + "\n" + "key3: value3\n" + "\n";

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testKeepingNewLineInsideSequence2() {
            /*
             Not supported right now " \n" + "#-
             https://github.intuit.com/dev-patterns/intuit-kustomize/intuit-service-appd-noingress-base?ref=v3.1.2\n"
             + "# Add the following base and HPA-patch.yaml, fill in correct minReplicas and
             maxReplcias in Hpa-patch.yaml\n" + "#-
             https://github.intuit.com/dev-patterns/intuit-kustomize//intuit-service-hpa-base?ref=v3.1.2\n"
             */
        // "\n" +
        String data = """
            apiVersion: kustomize.config.k8s.io/v1beta1
            kind: Kustomization
            
            namePrefix: acquisition-gateway-
            
            bases:
            - https://github.intuit.com/dev-patterns/intuit-kustomize//intuit-service-canary-appd-noingress-base?ref=v3.2.0
            - https://github.intuit.com/dev-patterns/intuit-kustomize//intuit-service-rollout-hpa-base?ref=v3.2.0
            # resources:
            # - Nginx-ConfigMap.yaml
            
            resources:
            - ConfigMap-v1-splunk-sidecar-config.yaml
            - CronJob-patch.yaml
            
            patchesStrategicMerge:
            - app-rollout-patch.yaml
            - Service-patch.yaml
            - Service-metrics-patch.yaml
            - Hpa-patch.yaml
            #- SignalSciences-patch.yaml
            
            # Uncomment HPA-patch when you need to enable HPA
            #- Hpa-patch.yaml
            # Uncomment SignalSciences-patch when you need to enable Signal Sciences
            #- SignalSciences-patch.yaml
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testCommentsIndentFirstLineBlank() {
        String data = """
            # Comment 1
            key1:
             \s
              # Comment 2
              # Comment 3
              key2: value1
            # "Fun" options
            key3:
              # Comment 4
              # Comment 5
              key4: value2
            key5:
              key6: value3
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testCommentsLineBlank() {
        String data = """
            # Comment 1
            key1:
             \s
              # Comment 2
            
              # Comment 3
            
              key2: value1
            # "Fun" options
            key3:
              # Comment 4
              # Comment 5
              key4: value2
            key5:
              key6: value3
            """;

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testMultiLineString() {
        String data = """
            # YAML load and save bug with keep block chomping indicator
            example:
              description: |+
                These lines have a carrage return after them.
                And the carrage return will be duplicated with each save if the
                block chomping indicator + is used. ("keep": keep the line feed, keep trailing blank lines.)
            
            successfully-loaded: test
            """;
        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void test100Comments() {
        var commentBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            commentBuilder.append("# Comment ").append(i).append('\n');
        }
        var data = commentBuilder + "simpleKey: simpleValue\n" + "\n";

        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(data, result);
    }

    @Test
    void testCommentsOnReference() {
        String data = """
            dummy: &a test
            conf:
            - # comment not ok here
              *a #comment not ok here
            """;
        String expected = """
            dummy: &a test
            conf:
            - *a
            """;
        String result = runEmitterWithCommentsEnabled(data);
        assertEquals(expected, result);
    }

    @Test
    void testCommentsAtDataWindowBreak() {
        String data = getComplexConfig();

        // final Yaml yaml = new Yaml(new SafeConstructor(), yamlRepresenter, yamlOptions,
        // loaderOptions);

        var load = new Load(LoadSettings.builder()
            .setMaxAliasesForCollections(Integer.MAX_VALUE)
            .build());
        load.loadAllFromString(data);
    }

    @Test
    void testCommentsInFlowMapping() {
        var output = new StreamToStringWriter();
        var allImplicit = ImplicitTuple.TRUE_TRUE;
        producePrettyFlowEmitter(output)
            .emit(new StreamStartEvent(null, null))
            .emit(new DocumentStartEvent(false, SpecVersion.EMPTY, new HashMap<>(), null, null))
            .emit(new MappingStartEvent(null, "yaml.org,2002:map", true, FlowStyle.FLOW))
            .emit(new CommentEvent(CommentType.BLOCK, " I'm first", null, null))
            .emit(new ScalarEvent(null, "yaml.org,2002:str", allImplicit, "a", ScalarStyle.PLAIN, null, null))
            .emit(new ScalarEvent(null, "yaml.org,2002:str", allImplicit, "Hello", ScalarStyle.PLAIN, null, null))
            .emit(new ScalarEvent(null, "yaml.org,2002:str", allImplicit, "b", ScalarStyle.PLAIN, null, null))
            .emit(new MappingStartEvent(null, "yaml.org,2002:map", true, FlowStyle.FLOW, null, null))
            .emit(new ScalarEvent(null, "yaml.org,2002:str", allImplicit, "one", ScalarStyle.PLAIN, null, null))
            .emit(new ScalarEvent(null, "yaml.org,2002:str", allImplicit, "World", ScalarStyle.PLAIN, null, null))
            .emit(new CommentEvent(CommentType.BLOCK, " also me", null, null))
            .emit(new ScalarEvent(null, "yaml.org,2002:str", allImplicit, "two", ScalarStyle.PLAIN, null, null))
            .emit(new ScalarEvent(null, "yaml.org,2002:str", allImplicit, "eee", ScalarStyle.PLAIN, null, null))
            .emit(new MappingEndEvent(null, null))
            .emit(new MappingEndEvent(null, null))
            .emit(new DocumentEndEvent(false, null, null))
            .emit(new StreamEndEvent(null, null));

        String result = output.toString();
        final String data = """
            {
              # I'm first
              a: Hello,
              b: {
                one: World,
                # also me
                two: eee
              }
            }
            """;

        assertEquals(data, result);
    }

    @Test
    void testCommentInEmptyFlowMapping() {
        var output = new StreamToStringWriter();
        producePrettyFlowEmitter(output)
            .emit(new StreamStartEvent(null, null))
            .emit(new DocumentStartEvent(false, SpecVersion.EMPTY, new HashMap<>(), null, null))
            .emit(new MappingStartEvent(null, "yaml.org,2002:map", true, FlowStyle.FLOW, null, null))
            .emit(new CommentEvent(CommentType.BLOCK, " nobody home", null, null))
            .emit(new MappingEndEvent(null, null))
            .emit(new DocumentEndEvent(false, null, null))
            .emit(new StreamEndEvent(null, null));

        String result = output.toString();
        final String data = """
            {
              # nobody home
            }
            """;

        assertEquals(data, result);
    }

    @Test
    void testCommentInFlowSequence() {
        var allImplicit = ImplicitTuple.TRUE_TRUE;
        var output = new StreamToStringWriter();
        producePrettyFlowEmitter(output)
            .emit(new StreamStartEvent(null, null))
            .emit(new DocumentStartEvent(false, SpecVersion.EMPTY, new HashMap<>(), null, null))
            .emit(new SequenceStartEvent(null, "yaml.org,2002:seq", true, FlowStyle.FLOW, null, null))
            .emit(new CommentEvent(CommentType.BLOCK, " red", null, null))
            .emit(new ScalarEvent(null, "yaml.org,2002:str", allImplicit, "one", ScalarStyle.PLAIN, null, null))
            .emit(new CommentEvent(CommentType.BLOCK, " blue", null, null))
            .emit(new ScalarEvent(null, "yaml.org,2002:str", allImplicit, "two", ScalarStyle.PLAIN, null, null))
            .emit(new SequenceEndEvent(null, null))
            .emit(new DocumentEndEvent(false, null, null))
            .emit(new StreamEndEvent(null, null));

        String result = output.toString();
        var data = """
            [
              # red
              one,
              # blue
              two
            ]
            """;

        assertEquals(data, result);
    }

    @Test
    void testCommentInEmptySequence() {
        var output = new StreamToStringWriter();
        producePrettyFlowEmitter(output)
            .emit(new StreamStartEvent(null, null))
            .emit(new DocumentStartEvent(false, SpecVersion.EMPTY, new HashMap<>(), null, null))
            .emit(new SequenceStartEvent(null, "yaml.org,2002:seq", true, FlowStyle.FLOW, null, null))
            .emit(new CommentEvent(CommentType.BLOCK, " nobody home", null, null))
            .emit(new SequenceEndEvent(null, null))
            .emit(new DocumentEndEvent(false, null, null))
            .emit(new StreamEndEvent(null, null));

        String result = output.toString();
        var data = """
            [
              # nobody home
            ]
            """;

        assertEquals(data, result);
    }

    private String getComplexConfig() {
        return """
            # Core configurable options for LWC
            core:
            
                # The language LWC will use, specified by the shortname. For example, English = en, French = fr, German = de,
                # and so on
                locale: en
            
                # How often updates are batched to the database (in seconds). If set to a higher value than 10, you may have
                # some unexpected results, especially if your server is prone to crashing.
                flushInterval: 10
            
                # LWC regularly caches protections locally to prevent the database from being queried as often. The default is 10000
                # and for most servers is OK. LWC will also fill up to <precache> when the server is started automatically.
                cacheSize: 10000
            
                # How many protections are precached on startup. If set to -1, it will use the cacheSize value instead and precache
                # as much as possible
                precache: -1
            
                # If true, players will be sent a notice in their chat box when they open a protection they have access to, but
                # not their own unless <showMyNotices> is set to true
                showNotices: true
            
                # If true, players will be sent a notice in their chat box when they open a protection they own.
                showMyNotices: false
            """;
    }

    private String runEmitterWithCommentsEnabled(String data) {
        var output = new StreamToStringWriter();

        var dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setDumpComments(true)
            .build();
        var serializer = new Serializer(dumpSettings, new Emitter(dumpSettings, output));

        serializer.emitStreamStart();
        var loadSettings = LoadSettings.builder()
            .setParseComments(true)
            .build();
        var composer = new Composer(loadSettings, new ParserImpl(loadSettings, new StreamReader(loadSettings, data)));
        while (composer.hasNext()) {
            Node node = composer.next();
            // System.out.println(node);
            serializer.serializeDocument(node);
        }
        serializer.emitStreamEnd();

        return output.toString();
    }

    private Emitter producePrettyFlowEmitter(StreamDataWriter output) {
        return new Emitter(DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setDefaultFlowStyle(FlowStyle.FLOW)
            .setDumpComments(true)
            .setMultiLineFlow(true)
            .build(), output);
    }
}
