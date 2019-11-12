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
