package com.criteo.publisher;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InHouseTest {

  @Mock
  private BidManager bidManager;

  @Mock
  private TokenCache tokenCache;

  @Mock
  private Clock clock;

  private InHouse inHouse;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    inHouse = new InHouse(bidManager, tokenCache, clock);
  }

  @Test
  public void getTokenValue_GivenToken_DelegateToCache() throws Exception {
    TokenValue expectedTokenValue = mock(TokenValue.class);
    BidToken token = new BidToken(UUID.randomUUID(), mock(AdUnit.class));

    when(tokenCache.getTokenValue(token, CRITEO_BANNER)).thenReturn(expectedTokenValue);

    TokenValue tokenValue = inHouse.getTokenValue(token, CRITEO_BANNER);

    assertThat(tokenValue).isEqualTo(expectedTokenValue);
  }

  @Test
  public void getBidResponse_GivenNullAdUnit_ReturnNoBid() throws Exception {
    BidResponse bidResponse = inHouse.getBidResponse(null);

    assertIsNoBid(bidResponse);
  }

  @Test
  public void getBidResponse_GivenBidManagerYieldNoBid_ReturnNoBid() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);

    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(null);

    BidResponse bidResponse = inHouse.getBidResponse(adUnit);

    assertIsNoBid(bidResponse);
  }

  @Test
  public void getBidResponse_WhenBidManagerYieldBid_ReturnBid() throws Exception {
    AdUnit adUnit = mock(AdUnit.class);
    Slot slot = mock(Slot.class);

    when(slot.getCpmAsNumber()).thenReturn(42.1337);
    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(slot);

    BidResponse bidResponse = inHouse.getBidResponse(adUnit);

    assertThat(bidResponse.isBidSuccess()).isTrue();
    assertThat(bidResponse.getPrice()).isEqualTo(42.1337);
  }

  private void assertIsNoBid(BidResponse bidResponse) {
    assertThat(bidResponse.isBidSuccess()).isFalse();
    assertThat(bidResponse.getBidToken()).isNull();
    assertThat(bidResponse.getPrice()).isZero();
  }

}