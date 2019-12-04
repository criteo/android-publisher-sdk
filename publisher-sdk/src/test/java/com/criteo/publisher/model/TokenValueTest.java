package com.criteo.publisher.model;

import com.criteo.publisher.Clock;
import com.criteo.publisher.Util.AdUnitType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TokenValueTest {

    private static final long BID_TIME = 1000;
    private static final int BID_TTLE = 10;
    private static final int SECOND_TO_MILLI = 1000;

    private static final String DISPLAY_URL = "http://www.criteo.com";

    private TokenValue tokenValue;

    @Mock
    private Clock clock;

    @Before
    public void prepare() {
        MockitoAnnotations.initMocks(this);
        tokenValue = new TokenValue(BID_TIME, BID_TTLE, DISPLAY_URL, AdUnitType.CRITEO_BANNER, clock);
    }

    @Test
    public void testTokenValueCreated() {
        Assert.assertEquals(tokenValue.getAdUnitType(), AdUnitType.CRITEO_BANNER);
        Assert.assertEquals(tokenValue.getDisplayUrl(), DISPLAY_URL);
        Assert.assertEquals(tokenValue.gettokenExpirationTime(), BID_TIME + BID_TTLE * SECOND_TO_MILLI);
    }
}
