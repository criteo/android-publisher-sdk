package com.criteo.publisher.model;

import com.criteo.publisher.Util.AdUnitType;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TokenCacheTest {

    private static final String TEST_CREATIVE = "https://rdi.us.criteo.com/delivery/r/ajs.php?did=5c87fcdb7cc0d71b24ee2ee6454eb810&u=%7CvsLBMQ0Ek4IxXQb0B5n7RyCAQymjqwh29YhNM9EzK9Q%3D%7C&c1=fYGSyyN4O4mkT2ynhzfwbdpiG7v0SMGpms6Tk24GWc957HzbzgL1jw-HVL5D0BjRx5ef3wBVfDXXmh9StLy8pf5kDJtrQLTLQrexjq5CZt9tEDx9mY8Y-eTV19PWOQoNjXkJ4_mhKqV0IfwHDIfLVDBWmsizVCoAtU1brQ2weeEkUU5-mDfn3qzTX3jPXszef5bC3pbiLJAK3QamQlglD1dkWYOkUwLAXxMjr2MXeBQk2YK-_qYz0fMVJG0xWJ-jVmsqdOw9A9rkGIgToRoUewB0VAu5eSkjSBoGs4yEbsnJ5Ssq5fquJMNvm6T77b8fzQI-eXgwoEfKkdAuCbj3gNrPBgzGZAJPGO-TYvJgs22Bljy-hNCk1E0E030zLtKo-XvAVRvZ5PswtwoccPSl6u1wiV8fMCXHx9QW9-fdXaVxzZe9AZB6w7pHxKUwiRK9";

    private TokenCache tokenCache;

    @Before
    public void preapre() {
        tokenCache = new TokenCache();
    }

    @Test
    public void testGetTokenForBidAndGetValueForToken() {
        TokenValue tokenForBanner1 = new TokenValue(100, 10, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForBanner2 = new TokenValue(200, 5, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForInterstitial1 = new TokenValue(100, 10, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);
        TokenValue tokenForInterstitial2 = new TokenValue(100, 10, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);

        BidToken bidTokenForBanner1 = tokenCache.add(tokenForBanner1);
        BidToken bidTokenForBanner2 = tokenCache.add(tokenForBanner2);
        BidToken bidTokenForInterstitial1 = tokenCache.add(tokenForInterstitial1);
        BidToken bidTokenForInterstitial2 = tokenCache.add(tokenForInterstitial2);

        Assert.assertEquals(tokenForBanner1, tokenCache.getTokenValue(bidTokenForBanner1));
        Assert.assertEquals(tokenForBanner2, tokenCache.getTokenValue(bidTokenForBanner2));
        Assert.assertEquals(tokenForInterstitial1, tokenCache.getTokenValue(bidTokenForInterstitial1));
        Assert.assertEquals(tokenForInterstitial2, tokenCache.getTokenValue(bidTokenForInterstitial2));
    }

    @Test
    public void testGetTokenForBidAndGetValueForTokenNotEquals() {
        TokenValue tokenForBanner1 = new TokenValue(100, 10, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForBanner2 = new TokenValue(200, 5, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForInterstitial1 = new TokenValue(100, 10, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);
        TokenValue tokenForInterstitial2 = new TokenValue(100, 10, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);

        BidToken bidTokenForBanner2 = tokenCache.add(tokenForBanner2);
        BidToken bidTokenForInterstitial2 = tokenCache.add(tokenForInterstitial2);
        Assert.assertNotEquals(tokenForBanner1, tokenCache.getTokenValue(bidTokenForBanner2));
        Assert.assertNotEquals(tokenForInterstitial1, tokenCache.getTokenValue(bidTokenForInterstitial2));
    }

    //Checks TokenCache removes token after call getTokenValue
    @Test
    public void testGetTokenForBidAndGetValueForTokenCheckNull() {
        TokenValue tokenForBanner1 = new TokenValue(100, 10, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForInterstitial1 = new TokenValue(100, 10, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);

        BidToken bidTokenForBanner1 = tokenCache.add(tokenForBanner1);
        BidToken bidTokenForInterstitial1 = tokenCache.add(tokenForInterstitial1);
        tokenCache.getTokenValue(bidTokenForBanner1);
        tokenCache.getTokenValue(bidTokenForInterstitial1);
        Assert.assertNull(tokenCache.getTokenValue(bidTokenForBanner1));
        Assert.assertNull(tokenCache.getTokenValue(bidTokenForInterstitial1));
    }

    @Test
    public void testGetUncachedTokenAndNullToken() {
        BidToken unChachedToken = new BidToken(UUID.randomUUID());
        BidToken nullToken = null;
        Assert.assertNull(tokenCache.getTokenValue(unChachedToken));
        Assert.assertNull(tokenCache.getTokenValue(nullToken));
    }

}