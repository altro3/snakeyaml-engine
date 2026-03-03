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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.scanner.Scanner;
import org.snakeyaml.engine.v2.scanner.ScannerImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.snakeyaml.engine.v2.tokens.CommentToken;
import org.snakeyaml.engine.v2.tokens.ScalarToken;
import org.snakeyaml.engine.v2.tokens.Token;
import org.snakeyaml.engine.v2.tokens.Token.Id;

public class ScannerWithCommentEnabledTest {

    private final boolean DEBUG = false;

    private void assertTokensEqual(List<Id> expected, Scanner sut) {
        assertTokensEqual(expected, null, sut);
    }

    private void printToken(Token token) {
        String value;
        switch (token.getTokenId()) {
            case Scalar:
                value = "(value='" + ((ScalarToken) token).getValue() + "')";
                break;
            case Comment:
                CommentToken commentToken = (CommentToken) token;
                value = "(type='" + commentToken.getCommentType() + ", value='" + commentToken.getValue()
                    + "')";
                break;
            default:
                value = "";
                break;
        }
        if (DEBUG) {
            System.out.println(token.getTokenId().name() + value);
        }
    }

    private void assertTokenEquals(Iterator<Id> expectedIdIterator,
                                   Iterator<String> expectedScalarValueIterator, Token token) {
        printToken(token);
        assertTrue(expectedIdIterator.hasNext());
        Id expectedValue = expectedIdIterator.next();
        Id id = token.getTokenId();
        assertSame(expectedValue, id);
        if (expectedScalarValueIterator != null && token.getTokenId() == Id.Scalar) {
            assertEquals(expectedScalarValueIterator.next(), ((ScalarToken) token).getValue());
        }
    }

    private void assertTokensEqual(List<Id> expectedList, List<String> expectedScalarValueList,
                                   Scanner sut) {
        Iterator<Id> expectedIterator = expectedList.iterator();
        Iterator<String> expectedScalarValueIterator =
            expectedScalarValueList == null ? null : expectedScalarValueList.iterator();
        while (!sut.checkToken(Id.StreamEnd)) {
            Token token = sut.next();
            assertTokenEquals(expectedIterator, expectedScalarValueIterator, token);
        }
        Token token = sut.peekToken();
        assertTokenEquals(expectedIterator, expectedScalarValueIterator, token);
        assertFalse(expectedIterator.hasNext());
    }

    private Scanner constructScanner(String input) {
        LoadSettings settings = LoadSettings.builder().setParseComments(true).build();
        return new ScannerImpl(settings, new StreamReader(settings, new StringReader(input)));
    }

    @Test
    public void testEmpty() {
        List<Id> expected = Arrays.asList(Id.StreamStart, Id.StreamEnd);

        Scanner sut = constructScanner("");

        assertTokensEqual(expected, sut);
    }

    @Test
    public void testOnlyCommentLines() {
        List<Id> expected = Arrays.asList(Id.StreamStart, //
            Id.Comment, //
            Id.Comment, //
            Id.StreamEnd);

        Scanner sut = constructScanner("" + //
            "# This stream contains no\n" + //
            "# documents, only comments.");

        assertTokensEqual(expected, sut);
    }

    @Test
    public void testCommentEndingALine() {
        List<Id> expected = Arrays.asList(Id.StreamStart, //
            Id.BlockMappingStart, //
            Id.Key, Id.Scalar, Id.Value, Id.Comment, //
            Id.Scalar, //
            Id.BlockEnd, //
            Id.StreamEnd);
        List<String> expectedScalarValue = Arrays.asList(//
            "key", "value");

        Scanner sut = constructScanner("" + //
            "key: # Comment\n" + //
            "  value\n");

        assertTokensEqual(expected, expectedScalarValue, sut);
    }

    @Test
    public void testMultiLineComment() {
        List<Id> expected = Arrays.asList(Id.StreamStart, //
            Id.BlockMappingStart, //
            Id.Key, Id.Scalar, Id.Value, Id.Comment, Id.Comment, //
            Id.Scalar, //
            Id.Comment, //
            Id.BlockEnd, //
            Id.StreamEnd);
        List<String> expectedScalarValue = Arrays.asList(//
            "key", "value");

        Scanner sut = constructScanner("" + //
            "key: # Comment\n" + //
            "     # lines\n" + //
            "  value\n" + //
            "\n");

        assertTokensEqual(expected, expectedScalarValue, sut);
    }

    @Test
    public void testBlankLine() {
        List<Id> expected = Arrays.asList(Id.StreamStart, //
            Id.Comment, //
            Id.StreamEnd);

        Scanner sut = constructScanner("" + //
            "\n");

        assertTokensEqual(expected, sut);
    }

    @Test
    public void testBlankLineComments() {
        List<Id> expected = Arrays.asList(Id.StreamStart, //
            Id.Comment, //
            Id.BlockMappingStart, //
            Id.Key, Id.Scalar, Id.Value, Id.Scalar, Id.Comment, //
            Id.Comment, //
            Id.Comment, //
            Id.BlockEnd, //
            Id.StreamEnd);

        Scanner sut = constructScanner("" + //
            "\n" + //
            "abc: def # commment\n" + //
            "\n" + //
            "\n");

        assertTokensEqual(expected, sut);
    }

    @Test
    public void test_blockScalar_replaceNLwithSpaces_singleNLatEnd() {
        List<Id> expected = Arrays.asList(//
            Id.StreamStart, //
            Id.BlockMappingStart, //
            Id.Key, Id.Scalar, Id.Value, Id.Comment, //
            Id.Scalar, //
            Id.BlockEnd, //
            Id.StreamEnd //
        );
        List<String> expectedScalarValue = Arrays.asList(//
            "abc", "def hij\n");

        Scanner sut = constructScanner("abc: > # Comment\n    def\n    hij\n\n");

        // printTokens(sut);
        assertTokensEqual(expected, expectedScalarValue, sut);
    }

    @Test
    public void test_blockScalar_replaceNLwithSpaces_noNLatEnd() {
        List<Id> expected = Arrays.asList(//
            Id.StreamStart, //
            Id.BlockMappingStart, //
            Id.Key, Id.Scalar, Id.Value, Id.Comment, Id.Scalar, //
            Id.BlockEnd, //
            Id.StreamEnd //
        );
        List<String> expectedScalarValue = Arrays.asList(//
            "abc", "def hij");

        Scanner sut = constructScanner("abc: >- # Comment\n    def\n    hij\n\n");

        assertTokensEqual(expected, expectedScalarValue, sut);
    }

    @Test
    public void test_blockScalar_replaceNLwithSpaces_allNLatEnd() {
        List<Id> expected = Arrays.asList(//
            Id.StreamStart, //
            Id.BlockMappingStart, //
            Id.Key, Id.Scalar, Id.Value, Id.Comment, Id.Scalar, //
            Id.BlockEnd, //
            Id.StreamEnd //
        );
        List<String> expectedScalarValue = Arrays.asList(//
            "abc", "def hij\n\n");

        Scanner sut = constructScanner("abc: >+ # Comment\n    def\n    hij\n\n");

        assertTokensEqual(expected, expectedScalarValue, sut);
    }

    @Test
    public void test_blockScalar_keepNL_singleNLatEnd() {
        List<Id> expected = Arrays.asList(//
            Id.StreamStart, //
            Id.BlockMappingStart, //
            Id.Key, Id.Scalar, Id.Value, Id.Comment, Id.Scalar, //
            Id.BlockEnd, //
            Id.StreamEnd //
        );
        List<String> expectedScalarValue = Arrays.asList(//
            "abc", "def\nhij\n");

        Scanner sut = constructScanner("abc: | # Comment\n    def\n    hij\n\n");

        assertTokensEqual(expected, expectedScalarValue, sut);
    }

    @Test
    public void test_blockScalar_keepNL_noNLatEnd() {
        List<Id> expected = Arrays.asList(//
            Id.StreamStart, //
            Id.BlockMappingStart, //
            Id.Key, Id.Scalar, Id.Value, Id.Comment, Id.Scalar, //
            Id.BlockEnd, //
            Id.StreamEnd //
        );
        List<String> expectedScalarValue = Arrays.asList(//
            "abc", "def\nhij");

        Scanner sut = constructScanner("abc: |- # Comment\n    def\n    hij\n\n");

        assertTokensEqual(expected, expectedScalarValue, sut);
    }

    @Test
    public void test_blockScalar_keepNL_allNLatEnd() {
        List<Id> expected = Arrays.asList(//
            Id.StreamStart, //
            Id.BlockMappingStart, //
            Id.Key, Id.Scalar, Id.Value, Id.Comment, Id.Scalar, //
            Id.BlockEnd, //
            Id.StreamEnd //
        );
        List<String> expectedScalarValue = Arrays.asList(//
            "abc", "def\nhij\n\n");

        Scanner sut = constructScanner("abc: |+ # Comment\n    def\n    hij\n\n");

        assertTokensEqual(expected, expectedScalarValue, sut);
    }

    @Test
    public void testDirectiveLineEndComment() {
        List<Id> expected = Arrays.asList(//
            Id.StreamStart, //
            Id.Directive, //
            Id.Comment, //
            Id.StreamEnd //
        );

        Scanner sut = constructScanner("%YAML 1.1 #Comment\n");

        assertTokensEqual(expected, sut);
    }
}
