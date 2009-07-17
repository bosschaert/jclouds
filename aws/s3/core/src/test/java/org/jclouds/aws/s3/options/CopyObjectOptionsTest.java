/**
 *
 * Copyright (C) 2009 Global Cloud Specialists, Inc. <info@globalcloudspecialists.com>
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 */
package org.jclouds.aws.s3.options;

import static org.jclouds.aws.s3.options.CopyObjectOptions.Builder.ifSourceETagDoesntMatch;
import static org.jclouds.aws.s3.options.CopyObjectOptions.Builder.ifSourceETagMatches;
import static org.jclouds.aws.s3.options.CopyObjectOptions.Builder.ifSourceModifiedSince;
import static org.jclouds.aws.s3.options.CopyObjectOptions.Builder.ifSourceUnmodifiedSince;
import static org.jclouds.aws.s3.options.CopyObjectOptions.Builder.overrideAcl;
import static org.jclouds.aws.s3.options.CopyObjectOptions.Builder.overrideMetadataWith;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.jclouds.aws.s3.domain.CannedAccessPolicy;
import org.jclouds.aws.s3.options.CopyObjectOptions;
import org.jclouds.aws.s3.reference.S3Headers;
import org.jclouds.http.HttpUtils;
import org.jclouds.util.DateService;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Tests possible uses of CopyObjectOptions and CopyObjectOptions.Builder.*
 * 
 * @author Adrian Cole
 */
@Test(groups = "unit", testName = "s3.CopyObjectOptionsTest")
public class CopyObjectOptionsTest {

   private byte[] testBytes;
   private DateTime now;
   private String nowExpected;
   private Multimap<String, String> goodMeta;
   private Multimap<String, String> badMeta;

   @BeforeMethod
   void setUp() {
      goodMeta = HashMultimap.create();
      goodMeta.put("x-amz-meta-adrian", "foo");
      badMeta = HashMultimap.create();
      badMeta.put("x-google-meta-adrian", "foo");

      now = new DateTime();
      nowExpected = new DateService().rfc822DateFormat(now);
      testBytes = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };
   }

   @Test
   void testGoodMetaStatic() {
      CopyObjectOptions options = overrideMetadataWith(goodMeta);
      assertGoodMeta(options);
   }

   @Test(expectedExceptions = NullPointerException.class)
   public void testMetaNPE() {
      overrideMetadataWith(null);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testBadMeta() {
      overrideMetadataWith(badMeta);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testBadMetaStatic() {
      overrideMetadataWith(badMeta);
   }

   private void assertGoodMeta(CopyObjectOptions options) {
      assert options != null;
      assert options.getMetadata() != null;
      Multimap<String, String> headers = options.buildRequestHeaders();
      assertEquals(headers.size(), 2);
      assertEquals(headers.get("x-amz-metadata-directive").iterator().next(), "REPLACE");
      assertEquals(options.getMetadata().size(), 1);
      assertEquals(headers.get("x-amz-meta-adrian").iterator().next(), "foo");
      assertEquals(options.getMetadata().get("x-amz-meta-adrian").iterator().next(), "foo");
   }

   @Test
   void testGoodMeta() {
      CopyObjectOptions options = new CopyObjectOptions();
      options.overrideMetadataWith(goodMeta);
      assertGoodMeta(options);
   }

   @Test
   public void testIfModifiedSince() {
      CopyObjectOptions options = new CopyObjectOptions();
      options.ifSourceModifiedSince(now);
      assertEquals(options.getIfModifiedSince(), nowExpected);
   }

   @Test
   public void testNullIfModifiedSince() {
      CopyObjectOptions options = new CopyObjectOptions();
      assertNull(options.getIfModifiedSince());
   }

   @Test
   public void testIfModifiedSinceStatic() {
      CopyObjectOptions options = ifSourceModifiedSince(now);
      assertEquals(options.getIfModifiedSince(), nowExpected);
   }

   @Test(expectedExceptions = NullPointerException.class)
   public void testIfModifiedSinceNPE() {
      ifSourceModifiedSince(null);
   }

   @Test
   public void testIfUnmodifiedSince() {
      CopyObjectOptions options = new CopyObjectOptions();
      options.ifSourceUnmodifiedSince(now);
      isNowExpected(options);
   }

   @Test
   public void testNullIfUnmodifiedSince() {
      CopyObjectOptions options = new CopyObjectOptions();
      assertNull(options.getIfUnmodifiedSince());
   }

   @Test
   public void testIfUnmodifiedSinceStatic() {
      CopyObjectOptions options = ifSourceUnmodifiedSince(now);
      isNowExpected(options);
   }

   private void isNowExpected(CopyObjectOptions options) {
      assertEquals(options.getIfUnmodifiedSince(), nowExpected);
   }

   @Test(expectedExceptions = NullPointerException.class)
   public void testIfUnmodifiedSinceNPE() {
      ifSourceUnmodifiedSince(null);
   }

   @Test
   public void testIfETagMatches() throws UnsupportedEncodingException {
      CopyObjectOptions options = new CopyObjectOptions();
      options.ifSourceETagMatches(testBytes);
      matchesHex(options.getIfMatch());
   }

   @Test
   public void testNullIfETagMatches() {
      CopyObjectOptions options = new CopyObjectOptions();
      assertNull(options.getIfMatch());
   }

   @Test
   public void testIfETagMatchesStatic() throws UnsupportedEncodingException {
      CopyObjectOptions options = ifSourceETagMatches(testBytes);
      matchesHex(options.getIfMatch());
   }

   @Test(expectedExceptions = NullPointerException.class)
   public void testIfETagMatchesNPE() throws UnsupportedEncodingException {
      ifSourceETagMatches(null);
   }

   @Test
   public void testIfETagDoesntMatch() throws UnsupportedEncodingException {
      CopyObjectOptions options = new CopyObjectOptions();
      options.ifSourceETagDoesntMatch(testBytes);
      matchesHex(options.getIfNoneMatch());
   }

   @Test
   public void testNullIfETagDoesntMatch() {
      CopyObjectOptions options = new CopyObjectOptions();
      assertNull(options.getIfNoneMatch());
   }

   @Test
   public void testIfETagDoesntMatchStatic() throws UnsupportedEncodingException {
      CopyObjectOptions options = ifSourceETagDoesntMatch(testBytes);
      matchesHex(options.getIfNoneMatch());
   }

   @Test(expectedExceptions = NullPointerException.class)
   public void testIfETagDoesntMatchNPE() throws UnsupportedEncodingException {
      ifSourceETagDoesntMatch(null);
   }

   private void matchesHex(String match) throws UnsupportedEncodingException {
      String expected = "\"" + HttpUtils.toHexString(testBytes) + "\"";
      assertEquals(match, expected);
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testIfUnmodifiedAfterModified() {
      ifSourceModifiedSince(now).ifSourceUnmodifiedSince(now);

   }

   public void testIfUnmodifiedAfterETagMatches() throws UnsupportedEncodingException {
      ifSourceETagMatches(testBytes).ifSourceUnmodifiedSince(now);

   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testIfUnmodifiedAfterETagDoesntMatch() throws UnsupportedEncodingException {
      ifSourceETagDoesntMatch(testBytes).ifSourceUnmodifiedSince(now);
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testIfModifiedAfterUnmodified() {
      ifSourceUnmodifiedSince(now).ifSourceModifiedSince(now);

   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testIfModifiedAfterETagMatches() throws UnsupportedEncodingException {
      ifSourceETagMatches(testBytes).ifSourceModifiedSince(now);

   }

   public void testIfModifiedAfterETagDoesntMatch() throws UnsupportedEncodingException {
      ifSourceETagDoesntMatch(testBytes).ifSourceModifiedSince(now);
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testETagMatchesAfterIfModified() throws UnsupportedEncodingException {
      ifSourceModifiedSince(now).ifSourceETagMatches(testBytes);

   }

   public void testETagMatchesAfterIfUnmodified() throws UnsupportedEncodingException {
      ifSourceUnmodifiedSince(now).ifSourceETagMatches(testBytes);

   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testETagMatchesAfterETagDoesntMatch() throws UnsupportedEncodingException {
      ifSourceETagDoesntMatch(testBytes).ifSourceETagMatches(testBytes);
   }

   public void testETagDoesntMatchAfterIfModified() throws UnsupportedEncodingException {
      ifSourceModifiedSince(now).ifSourceETagDoesntMatch(testBytes);

   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testETagDoesntMatchAfterIfUnmodified() throws UnsupportedEncodingException {
      ifSourceUnmodifiedSince(now).ifSourceETagDoesntMatch(testBytes);

   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testETagDoesntMatchAfterETagMatches() throws UnsupportedEncodingException {
      ifSourceETagMatches(testBytes).ifSourceETagDoesntMatch(testBytes);
   }

   @Test
   void testBuildRequestHeadersWhenMetadataNull() throws UnsupportedEncodingException {
      assert new CopyObjectOptions().buildRequestHeaders() != null;
   }

   @Test
   void testBuildRequestHeaders() throws UnsupportedEncodingException {

      Multimap<String, String> headers = ifSourceModifiedSince(now).ifSourceETagDoesntMatch(
               testBytes).overrideMetadataWith(goodMeta).buildRequestHeaders();
      assertEquals(headers.get("x-amz-copy-source-if-modified-since").iterator().next(),
               new DateService().rfc822DateFormat(now));
      assertEquals(headers.get("x-amz-copy-source-if-none-match").iterator().next(), "\""
               + HttpUtils.toHexString(testBytes) + "\"");
      for (String value : goodMeta.values())
         assertTrue(headers.containsValue(value));

   }

   @Test
   public void testAclDefault() {
      CopyObjectOptions options = new CopyObjectOptions();
      assertEquals(options.getAcl(), CannedAccessPolicy.PRIVATE);
   }

   @Test
   public void testAclStatic() {
      CopyObjectOptions options = overrideAcl(CannedAccessPolicy.AUTHENTICATED_READ);
      assertEquals(options.getAcl(), CannedAccessPolicy.AUTHENTICATED_READ);
   }

   @Test
   void testBuildRequestHeadersACL() throws UnsupportedEncodingException {

      Multimap<String, String> headers = overrideAcl(CannedAccessPolicy.AUTHENTICATED_READ)
               .buildRequestHeaders();
      assertEquals(headers.get(S3Headers.CANNED_ACL).iterator().next(),
               CannedAccessPolicy.AUTHENTICATED_READ.toString());
   }
}