/*
 * Copyright The Stargate Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.stargate.db;

import com.datastax.oss.driver.shaded.guava.common.collect.ImmutableMap;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
public interface AuthenticatedUser extends Serializable {

  String name();

  @Nullable
  String token();

  boolean isFromExternalAuth();

  Map<String, String> customProperties();

  static AuthenticatedUser of(String userName) {
    return ImmutableAuthenticatedUser.builder()
        .name(userName)
        .isFromExternalAuth(false)
        .customProperties(Collections.emptyMap())
        .build();
  }

  static AuthenticatedUser of(String userName, String token) {
    return ImmutableAuthenticatedUser.builder()
        .name(userName)
        .token(token)
        .isFromExternalAuth(false)
        .customProperties(Collections.emptyMap())
        .build();
  }

  static AuthenticatedUser of(
      String userName,
      String token,
      boolean useTransitionalAuth,
      Map<String, String> customProperties) {
    return ImmutableAuthenticatedUser.builder()
        .name(userName)
        .token(token)
        .isFromExternalAuth(useTransitionalAuth)
        .customProperties(customProperties)
        .build();
  }

  class Serializer {
    private static final ByteBuffer FROM_EXTERNAL_VALUE =
        ByteBuffer.wrap(new byte[] {1}).asReadOnlyBuffer();

    private static final String CUSTOM_PAYLOAD_NAME_PREFIX = "stargate.auth.subject.custom.";
    private static final String TOKEN = "stargate.auth.subject.token";
    private static final String ROLE = "stargate.auth.subject.role";
    private static final String EXTERNAL = "stargate.auth.subject.fromExternalAuth";

    private static void encode(
        ImmutableMap.Builder<String, ByteBuffer> map, String key, String value) {
      if (value == null) {
        return;
      }

      ByteBuffer buffer = StandardCharsets.UTF_8.encode(value).asReadOnlyBuffer();
      map.put(key, buffer);
    }

    public static Map<String, ByteBuffer> serialize(AuthenticatedUser user) {
      ImmutableMap.Builder<String, ByteBuffer> map = ImmutableMap.builder();
      encode(map, TOKEN, user.token());
      encode(map, ROLE, user.name());

      if (user.isFromExternalAuth()) {
        map.put(EXTERNAL, FROM_EXTERNAL_VALUE);
      }

      for (Entry<String, String> e : user.customProperties().entrySet()) {
        String key = CUSTOM_PAYLOAD_NAME_PREFIX + e.getKey();
        encode(map, key, e.getValue());
      }

      return map.build();
    }

    public static AuthenticatedUser load(Map<String, ByteBuffer> customPayload) {
      ByteBuffer token = customPayload.get(TOKEN);
      ByteBuffer roleName = customPayload.get(ROLE);
      ByteBuffer isFromExternalAuth = customPayload.get(EXTERNAL);

      if (token == null || roleName == null) {
        throw new IllegalStateException("token and roleName must be provided");
      }

      ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
      for (Entry<String, ByteBuffer> e : customPayload.entrySet()) {
        String key = e.getKey();
        if (key.startsWith(CUSTOM_PAYLOAD_NAME_PREFIX)) {
          String name = key.substring(CUSTOM_PAYLOAD_NAME_PREFIX.length());
          String value = StandardCharsets.UTF_8.decode(e.getValue()).toString();
          map.put(name, value);
        }
      }

      return AuthenticatedUser.of(
          StandardCharsets.UTF_8.decode(roleName).toString(),
          StandardCharsets.UTF_8.decode(token).toString(),
          (isFromExternalAuth != null),
          map.build());
    }
  }
}
