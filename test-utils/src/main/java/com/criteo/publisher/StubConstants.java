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

package com.criteo.publisher;

import com.criteo.publisher.model.nativeads.NativeAssets;
import java.io.IOException;
import java.util.regex.Pattern;

public class StubConstants {

  /**
   * Display URL that is always returned by CDB stub.
   * <p>
   * Domain name is a wildcard to avoid issues if the CDB stub server change its name. We only want
   * to check that the URL looks like a CDB stub URL.
   */
  public static final Pattern STUB_DISPLAY_URL = Pattern.compile(
      "https?://(.+)/delivery/ajs.php\\?width=[0-9]+(&|&amp;)height=[0-9]+");

  public static final Pattern STUB_VAST_DISPLAY_URL = Pattern.compile(
      "https?://(.+)/delivery/vast.php");

  /**
   * Image that is shown in the AJS creative (see {@link #STUB_DISPLAY_URL}) returned by CDB stub.
   * This is also the image of the single product in case of native response from CDB.
   */
  public static final String STUB_CREATIVE_IMAGE = "https://publisherdirect.criteo.com/publishertag/preprodtest/creative_cas.png";

  public static final String STUB_NATIVE_JSON = "{\n"
      + "  \"products\": [\n"
      + "    {\n"
      + "      \"title\": \"Criteo native solution\",\n"
      + "      \"description\": \"A smart solution for your Native advertising\",\n"
      + "      \"price\": \"10$\",\n"
      + "      \"clickUrl\": \"https://www.criteo.com/products/\",\n"
      + "      \"callToAction\": \"Try it now!\",\n"
      + "      \"image\": {\n"
      + "        \"url\": \"https://publisherdirect.criteo.com/publishertag/preprodtest/creative.png\",\n"
      + "        \"height\": 300,\n"
      + "        \"width\": 300\n"
      + "      }\n"
      + "    }\n"
      + "  ],\n"
      + "  \"advertiser\": {\n"
      + "    \"description\": \"Our digital marketing solutions are trusted\",\n"
      + "    \"domain\": \"criteo.com\",\n"
      + "    \"logo\": {\n"
      + "      \"url\": \"https://www.criteo.com/images/criteo-logo.svg\",\n"
      + "      \"height\": 300,\n"
      + "      \"width\": 300\n"
      + "    },\n"
      + "    \"logoClickUrl\": \"https://www.criteo.com\"\n"
      + "  },\n"
      + "  \"privacy\": {\n"
      + "    \"optoutClickUrl\": \"https://info.criteo.com/privacy/informations\",\n"
      + "    \"optoutImageUrl\": \"https://static.criteo.net/flash/icon/nai_small.png\",\n"
      + "    \"longLegalText\": \"\"\n"
      + "  },\n"
      + "  \"impressionPixels\": [\n"
      + "    {\n"
      + "      \"url\": \"https://my-impression-pixel/test/impression\"\n"
      + "    },\n"
      + "    {\n"
      + "      \"url\": \"https://cas.com/lg.com\"\n"
      + "    }\n"
      + "  ]\n"
      + "}";

  /**
   * Native assets that are always returned by CDB stub. See {@link #STUB_NATIVE_JSON}.
   */
  public static final NativeAssets STUB_NATIVE_ASSETS;

  static {
    try {
      STUB_NATIVE_ASSETS = DependencyProvider.getInstance()
          .provideMoshi()
          .adapter(NativeAssets.class)
          .fromJson(STUB_NATIVE_JSON);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
