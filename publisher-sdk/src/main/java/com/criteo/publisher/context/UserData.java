/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.context;

import androidx.annotation.Keep;

@Keep
public class UserData extends AbstractContextData<UserData> {

  /**
   * <h1>Definition</h1>
   * Hashed email of the user
   *
   * <h2>Hashing Format</h2>
   * The hashing should be the users’ email address:
   * <ul>
   *   <li>Encoded in UTF-8</li>
   *   <li>Trimmed of any white space (eg: “test@criteo.com “ should become “test@criteo.com”)</li>
   *   <li>Converted to lower case</li>
   *   <li>Hashed with MD5 & output as ASCII text</li>
   *   <li>Hashed with SHA256 and output as ASCII text</li>
   * </ul>
   *
   * <h2>Example</h2>
   * <dl>
   *   <dt>Type</dt>
   *   <dd>String</dd>
   *
   *   <dt>Original Email</dt>
   *   <dd>john.doe@gmail.com</dd>
   *
   *   <dt>MD5</dt>
   *   <dd>e13743a7f1db7f4246badd6fd6ff54ff</dd>
   *
   *   <dt>SHA256 of MD5</dt>
   *   <dd>000e3171a5110c35c69d060112bd0ba55d9631c7c2ec93f1840e4570095b263a</dd>
   * </dl>
   *
   * <h1>Usage</h1>
   * The {@link EmailHasher} class is a helper to hash an email accordingly to the format above.
   * <p>
   * It can be used like:
   * <pre><code>
   *   UserData userData = new UserData()
   *       .set(UserData.HASHED_EMAIL, EmailHasher.hash("john.doe@gmail.com"));
   * </code></pre>
   */
  public static final String HASHED_EMAIL = "data.hashedEmail";

  /**
   * <h1>Definition</h1>
   * A developer's own persistent unique user identifier. In case the publisher support it.
   *
   * <h1>Type</h1>
   * String, example: "abcd12399"
   */
  public static final String DEV_USER_ID = "data.devUserId";

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UserData)) {
      return false;
    }
    return super.equals(o);
  }
}
