package com.criteo.publisher;

import com.criteo.publisher.model.NativeAssets;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

public class StubConstants {

  /**
   * Display URL that is always returned by CDB stub.
   * <p>
   * Domain name is a wildcard to avoid issues if the CDB stub server change its name. We only want
   * to check that the URL looks like a CDB stub URL.
   */
  public static final Pattern STUB_DISPLAY_URL = Pattern.compile(
      "https://(.+)/delivery/ajs.php\\?width=[0-9]+(&|&amp;)height=[0-9]+");

  /**
   * Image that is shown in the AJS creative (see {@link #STUB_DISPLAY_URL}) returned by CDB stub.
   * This is also the image of the single product in case of native response from CDB.
   */
  public static final String STUB_CREATIVE_IMAGE = "https://publisherdirect.criteo.com/publishertag/preprodtest/creative_cas.png";

  /**
   * Native assets that are always returned by CDB stub. See {@link #STUB_NATIVE_JSON}.
   */
  public static final NativeAssets STUB_NATIVE_ASSETS;

  private static final String STUB_NATIVE_JSON = "{\n"
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

  static {
    try {
      STUB_NATIVE_ASSETS = new NativeAssets(new JSONObject(STUB_NATIVE_JSON));
    } catch (JSONException e) {
      throw new IllegalStateException(e);
    }
  }

}
