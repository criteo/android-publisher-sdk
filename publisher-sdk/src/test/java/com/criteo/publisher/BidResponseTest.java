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

import static org.assertj.core.api.Assertions.assertThat;

import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import java.util.UUID;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Test;

public class BidResponseTest {

  private static final double PRICE = 1.0d;
  private static final boolean VALID = true;

  @Test
  public void testBidResponse() {
    UUID uuid = UUID.nameUUIDFromBytes("TEST_STRING1".getBytes());
    AdSize size = new AdSize(320, 50);
    BannerAdUnit adUnitId = new BannerAdUnit("AdUnitId1", size);

    BidToken token = new BidToken(uuid, adUnitId);

    BidResponse bidResponse = new BidResponse(PRICE, token, VALID);
    Assert.assertEquals(PRICE, bidResponse.getPrice(), 0);
    Assert.assertEquals(VALID, bidResponse.isBidSuccess());
  }

  @Test
  public void equalsContract() throws Exception {
    EqualsVerifier.forClass(BidResponse.class)
        .usingGetClass()
        .verify();
  }

  @Test
  public void create_GivenNoArgument_CreateANoBidResponse() throws Exception {
    BidResponse bidResponse = new BidResponse();

    assertThat(bidResponse.isBidSuccess()).isFalse();
    assertThat(bidResponse.getPrice()).isEqualTo(0.0);
    assertThat(bidResponse.getBidToken()).isNull();
  }

}
