/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.okhttp;

import com.squareup.okhttp.internal.spdy.ErrorCode;
import com.squareup.okhttp.internal.spdy.Header;
import java.io.IOException;
import java.util.List;
import okio.BufferedSource;

/**
 * {@link Protocol#spdyVariant SPDY variants} only. Processes server-initiated
 * HTTP requests.
 */
public interface PushObserver {
  /**
   * Describes the request that the server intends to push a response for.
   *
   * <p>Return true to cancel delivery of this stream's response.
   *
   * <p>Use the stream ID to correlate response headers and data.
   *
   * @param streamId server-initiated stream ID: an even number.
   * @param requestHeaders minimally includes {@code :method}, {@code :scheme},
   * {@code :authority}, and (@code :path}.
   */
  boolean onRequest(int streamId, List<Header> requestHeaders);

  /**
   * The response headers corresponding to a pushed request.  When {@code last}
   * is true, there are no data events to follow.
   *
   * <p>Return true to cancel delivery of further data on this stream ID.
   *
   * <p>Use the stream ID to correlate request headers and data.
   *
   * @param streamId server-initiated stream ID: an even number.
   * @param responseHeaders minimally includes {@code :status}.
   * @param last when true, there is no response data.
   */
  boolean onHeaders(int streamId, List<Header> responseHeaders, boolean last);

  /**
   * A chunk of response data corresponding to a pushed request.  This data
   * must either be read or skipped.
   *
   * <p>Return true to cancel delivery of further data on this stream ID.
   *
   * <p>Use the stream ID to correlate request headers and data.
   *
   * @param streamId server-initiated stream ID: an even number.
   * @param source location of data corresponding with this stream ID.
   * @param length number of bytes to read or skip from the source.
   * @param last when true, there are no data events to follow.
   */
  boolean onData(int streamId, BufferedSource source, int length, boolean last) throws IOException;

  /** Indicates the reason why this stream was cancelled. */
  void onReset(int streamId, ErrorCode errorCode);

  PushObserver CANCEL = new PushObserver() {

    @Override public boolean onRequest(int streamId, List<Header> requestHeaders) {
      return true;
    }

    @Override public boolean onHeaders(int streamId, List<Header> responseHeaders, boolean last) {
      return true;
    }

    @Override public boolean onData(int streamId, BufferedSource source, int length, boolean last)
        throws IOException {
      source.skip(length);
      return true;
    }

    @Override public void onReset(int streamId, ErrorCode errorCode) {
    }
  };
}

