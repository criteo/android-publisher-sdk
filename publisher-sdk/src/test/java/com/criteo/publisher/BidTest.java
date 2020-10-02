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

import static com.criteo.publisher.util.AdUnitType.CRITEO_BANNER;
import static com.criteo.publisher.util.AdUnitType.CRITEO_CUSTOM_NATIVE;
import static com.criteo.publisher.util.AdUnitType.CRITEO_INTERSTITIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.criteo.publisher.model.CdbResponseSlot;
import com.criteo.publisher.model.nativeads.NativeAssets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BidTest {

  @Mock
  private CdbResponseSlot slot;

  @Mock
  private Clock clock;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getPrice_GivenSlot_ReturnItsPrice() {
    when(slot.getCpmAsNumber()).thenReturn(42.0d);

    Bid bid = new Bid(CRITEO_BANNER, clock, slot);

    assertThat(bid.getPrice()).isEqualTo(42.0);
  }

  @Test
  public void getPrice_GivenSlotAndConsumedBid_ReturnItsOriginalPrice() {
    when(slot.getCpmAsNumber()).thenReturn(42.0d);

    Bid bid = new Bid(CRITEO_BANNER, clock, slot);
    bid.consumeNativeAssets();

    assertThat(bid.getPrice()).isEqualTo(42.0);
  }

  @Test
  public void consumeDisplayUrlFor_GivenValidBannerSlot_ReturnDisplayUrl() {
    when(slot.getDisplayUrl()).thenReturn("display.url");
    when(slot.isExpired(clock)).thenReturn(false);

    Bid bid = new Bid(CRITEO_BANNER, clock, slot);
    String displayUrl = bid.consumeDisplayUrlFor(CRITEO_BANNER);

    assertThat(displayUrl).isEqualTo("display.url");
  }

  @Test
  public void consumeDisplayUrlFor_GivenValidInterstitialSlot_ReturnDisplayUrl() {
    when(slot.getDisplayUrl()).thenReturn("display.url");
    when(slot.isExpired(clock)).thenReturn(false);

    Bid bid = new Bid(CRITEO_INTERSTITIAL, clock, slot);
    String displayUrl = bid.consumeDisplayUrlFor(CRITEO_INTERSTITIAL);

    assertThat(displayUrl).isEqualTo("display.url");
  }

  @Test
  public void consumeDisplayUrlFor_GivenAfterConsumingOnce_ReturnNull() {
    when(slot.getDisplayUrl()).thenReturn("display.url");
    when(slot.isExpired(clock)).thenReturn(false);

    Bid bid = new Bid(CRITEO_INTERSTITIAL, clock, slot);
    bid.consumeDisplayUrlFor(CRITEO_INTERSTITIAL);
    String displayUrl = bid.consumeDisplayUrlFor(CRITEO_INTERSTITIAL);

    assertThat(displayUrl).isNull();
  }

  @Test
  public void consumeDisplayUrlFor_GivenValidBannerSlotButBidIsForAnotherType_ReturnNull() {
    when(slot.getDisplayUrl()).thenReturn("display.url");
    when(slot.isExpired(clock)).thenReturn(false);

    Bid bid = new Bid(CRITEO_INTERSTITIAL, clock, slot);
    String displayUrl = bid.consumeDisplayUrlFor(CRITEO_BANNER);

    assertThat(displayUrl).isNull();
  }

  @Test
  public void consumeDisplayUrlFor_GivenExpiredSlot_ReturnNull() {
    when(slot.getDisplayUrl()).thenReturn("display.url");
    when(slot.isExpired(clock)).thenReturn(true);

    Bid bid = new Bid(CRITEO_BANNER, clock, slot);
    String displayUrl = bid.consumeDisplayUrlFor(CRITEO_BANNER);

    assertThat(displayUrl).isNull();
  }

  @Test
  public void consumeNativeAssets_GivenValidNativeSlot_ReturnAssets() {
    NativeAssets expected = mock(NativeAssets.class);
    when(slot.getNativeAssets()).thenReturn(expected);
    when(slot.isExpired(clock)).thenReturn(false);

    Bid bid = new Bid(CRITEO_CUSTOM_NATIVE, clock, slot);
    NativeAssets nativeAssets = bid.consumeNativeAssets();

    assertThat(nativeAssets).isEqualTo(expected);
  }

  @Test
  public void consumeNativeAssets_GivenAfterConsumingOnce_ReturnNull() {
    when(slot.getNativeAssets()).thenReturn(mock(NativeAssets.class));
    when(slot.isExpired(clock)).thenReturn(false);

    Bid bid = new Bid(CRITEO_CUSTOM_NATIVE, clock, slot);
    bid.consumeNativeAssets();
    NativeAssets nativeAssets = bid.consumeNativeAssets();

    assertThat(nativeAssets).isNull();
  }

  @Test
  public void consumeNativeAssets_GivenExpiredSlot_ReturnNull() {
    when(slot.getNativeAssets()).thenReturn(mock(NativeAssets.class));
    when(slot.isExpired(clock)).thenReturn(true);

    Bid bid = new Bid(CRITEO_CUSTOM_NATIVE, clock, slot);
    NativeAssets nativeAssets = bid.consumeNativeAssets();

    assertThat(nativeAssets).isNull();
  }

}
