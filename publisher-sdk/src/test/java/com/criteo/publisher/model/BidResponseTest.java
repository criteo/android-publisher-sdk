package com.criteo.publisher.model;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BidResponseTest {

    private static final double PRICE = 1.0d;
    private static final String TOKEN = "asdasdasds123ADSD23";
    private static final boolean SUCCESS = true;

    private BidResponse bidResponse;

    @Before
    public void prepare(){
        bidResponse = new BidResponse(PRICE , TOKEN , SUCCESS);
    }

    @Test
    public void testBidResponse(){
        Assert.assertEquals(bidResponse.getPrice() , PRICE , 0);
        Assert.assertEquals(bidResponse.getToken() , TOKEN);
        Assert.assertEquals(bidResponse.isSuccess() , SUCCESS);

    }

}