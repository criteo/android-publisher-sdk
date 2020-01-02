package com.criteo.publisher.model;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CdbRequestTest {

  @Mock
  private Publisher publisher;

  @Mock
  private User user;

  private String sdkVersion;

  private int profileId;

  private JSONObject gdprConsent;

  private List<CacheAdUnit> adUnits;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    sdkVersion = "1.2.3";
    profileId = 42;
    adUnits = new ArrayList<>();
  }

  @Test
  public void toJson_GivenAllInformation_MapThemToJson() throws Exception {
    JSONObject publisherJson = mock(JSONObject.class);
    when(publisher.toJson()).thenReturn(publisherJson);

    JSONObject userJson = mock(JSONObject.class);
    when(user.toJson()).thenReturn(userJson);

    gdprConsent = mock(JSONObject.class);

    adUnits.add(new CacheAdUnit(new AdSize(1, 2), "myAdUnit", CRITEO_BANNER));

    String adUnitJson = "{\n"
        + "  \"sizes\": [\"1x2\"],\n"
        + "  \"placementId\": \"myAdUnit\"\n"
        + "}";

    CdbRequest request = createRequest();
    JSONObject json = request.toJson();

    assertThat(json.get("user")).isEqualTo(userJson);
    assertThat(json.get("publisher")).isEqualTo(publisherJson);
    assertThat(json.get("sdkVersion")).isEqualTo("1.2.3");
    assertThat(json.get("profileId")).isEqualTo(42);
    assertThat(json.get("gdprConsent")).isEqualTo(gdprConsent);
    assertThat(json.getJSONArray("slots").length()).isEqualTo(1);
    assertThat(json.getJSONArray("slots").getJSONObject(0).toString())
        .isEqualToIgnoringWhitespace(adUnitJson);
  }

  @Test
  public void toJson_GivenNoGdpr_DoesNotMapIt() throws Exception {
    gdprConsent = null;

    CdbRequest request = createRequest();
    JSONObject json = request.toJson();

    assertThat(json.has("gdprConsent")).isFalse();
  }

  private CdbRequest createRequest() {
    return new CdbRequest(publisher, user, sdkVersion, profileId, gdprConsent, adUnits);
  }

}