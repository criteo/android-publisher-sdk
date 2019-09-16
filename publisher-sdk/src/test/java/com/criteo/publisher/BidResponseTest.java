package com.criteo.publisher;

import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public class BidResponseTest {

    private static final double PRICE = 1.0d;
    private BidToken token1;
    private static final boolean VALID = true;

    private BidResponse bidResponse1;

    @Test
    public void testBidResponse() {
        UUID uuid1 = UUID.nameUUIDFromBytes("TEST_STRING1".getBytes());

        token1 = new BidToken(uuid1, new BannerAdUnit("AdUnitId1", new AdSize(320, 50)));

        bidResponse1 = new BidResponse(PRICE, token1, VALID);
        Assert.assertEquals(PRICE, bidResponse1.getPrice(), 0);
        Assert.assertEquals(VALID, bidResponse1.isBidSuccess());
    }

    @Test
    public void testBidResponseFromNullUUID() {
        UUID uuid1 = UUID.nameUUIDFromBytes("TEST_STRING1".getBytes());

        token1 = new BidToken(uuid1, new BannerAdUnit("AdUnitId1", new AdSize(320, 50)));

        bidResponse1 = new BidResponse(PRICE, token1, VALID);
        Assert.assertEquals(PRICE, bidResponse1.getPrice(), 0);
        Assert.assertEquals(VALID, bidResponse1.isBidSuccess());
    }

}
