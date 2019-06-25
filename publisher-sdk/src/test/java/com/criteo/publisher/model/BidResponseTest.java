package com.criteo.publisher.model;

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
        UUID uuid2 = UUID.nameUUIDFromBytes("TEST_STRING2".getBytes());

        token1 = new BidToken(uuid1);

        bidResponse1 = new BidResponse(PRICE, token1, VALID);
        Assert.assertEquals(PRICE, bidResponse1.getPrice(), 0);
        Assert.assertEquals(VALID, bidResponse1.isBidSuccess());
    }

    @Test
    public void testBidResponseFromNullUUID() {
        UUID uuid1 = UUID.nameUUIDFromBytes("TEST_STRING1".getBytes());
        UUID uuid2 = null;

        token1 = new BidToken(uuid1);

        bidResponse1 = new BidResponse(PRICE, token1, VALID);
        Assert.assertEquals(PRICE, bidResponse1.getPrice(), 0);
        Assert.assertEquals(VALID, bidResponse1.isBidSuccess());
    }

}
