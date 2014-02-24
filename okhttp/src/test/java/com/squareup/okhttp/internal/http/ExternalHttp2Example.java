/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.squareup.okhttp.internal.http;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.PushObserver;
import com.squareup.okhttp.internal.spdy.ErrorCode;
import com.squareup.okhttp.internal.spdy.Header;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import okio.BufferedSource;

import static com.squareup.okhttp.internal.http.OkHeaders.SELECTED_PROTOCOL;

public final class ExternalHttp2Example {
  public static void main(String[] args) throws Exception {
    OkHttpClient client = new OkHttpClient()
        .setProtocols(Protocol.HTTP2_AND_HTTP_11)
        .setPushObserver(PUSH_TO_CONSOLE);

    URL url = new URL("https://http2.iijplus.jp/push/test1");
    HttpsURLConnection connection = (HttpsURLConnection) client.open(url);

    connection.setHostnameVerifier(new HostnameVerifier() {
      @Override public boolean verify(String s, SSLSession sslSession) {
        System.out.println("VERIFYING " + s);
        return true;
      }
    });

    int responseCode = connection.getResponseCode();
    System.out.println(responseCode);
    List<String> protocolValues = connection.getHeaderFields().get(SELECTED_PROTOCOL);
    // If null, probably you didn't add jetty's npn jar to your boot classpath!
    if (protocolValues != null && !protocolValues.isEmpty()) {
      System.out.println("PROTOCOL " + protocolValues.get(0));
    }

    BufferedReader reader =
        new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
    String line;
    while ((line = reader.readLine()) != null) {
      System.out.println(line);
    }
    connection.disconnect();
  }

  private static final PushObserver PUSH_TO_CONSOLE = new PushObserver() {
    @Override public boolean onRequest(int streamId, List<Header> requestHeaders) {
      System.out.printf("PUSH PROMISE[%s] >> %s%n", streamId, requestHeaders);
      return false;
    }

    @Override
    public boolean onHeaders(int streamId, List<Header> responseHeaders, boolean last) {
      System.out.printf("PUSH PROMISE[%s] << %s%n", streamId, responseHeaders);
      return false;
    }

    @Override
    public boolean onData(int streamId, BufferedSource source, int length, boolean last)
        throws IOException {
      System.out.printf("PUSH PROMISE[%s] << %s%n", streamId, source.readByteString(length));
      return false;
    }

    @Override public void onReset(int streamId, ErrorCode errorCode) {
      System.out.printf("PUSH PROMISE[%s] << CANCELLED%n", streamId);
    }
  };
}
