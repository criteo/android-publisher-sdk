package com.criteo.publisher;

import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.model.TokenValue;

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
        TokenValue tokenForBanner1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForBanner2 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForInterstitial1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);
        TokenValue tokenForInterstitial2 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);

        BidToken bidTokenForBanner1 = tokenCache.add(tokenForBanner1);
        BidToken bidTokenForBanner2 = tokenCache.add(tokenForBanner2);
        BidToken bidTokenForInterstitial1 = tokenCache.add(tokenForInterstitial1);
        BidToken bidTokenForInterstitial2 = tokenCache.add(tokenForInterstitial2);

        Assert.assertEquals(tokenForBanner1, tokenCache.getTokenValue(bidTokenForBanner1, AdUnitType.CRITEO_BANNER));
        Assert.assertEquals(tokenForBanner2, tokenCache.getTokenValue(bidTokenForBanner2, AdUnitType.CRITEO_BANNER));
        Assert.assertEquals(tokenForInterstitial1,
                tokenCache.getTokenValue(bidTokenForInterstitial1, AdUnitType.CRITEO_INTERSTITIAL));
        Assert.assertEquals(tokenForInterstitial2,
                tokenCache.getTokenValue(bidTokenForInterstitial2, AdUnitType.CRITEO_INTERSTITIAL));
    }

    @Test
    public void testReturnNullWhenTokenExpired() {
        TokenValue tokenForBanner1 = new TokenValue(System.currentTimeMillis(), -1, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForInterstitial1 = new TokenValue(System.currentTimeMillis(), -1, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);

        BidToken bidTokenForBanner1 = tokenCache.add(tokenForBanner1);
        BidToken bidTokenForInterstitial1 = tokenCache.add(tokenForInterstitial1);

        Assert.assertNull(tokenCache.getTokenValue(bidTokenForBanner1, AdUnitType.CRITEO_BANNER));
        Assert.assertNull(tokenCache.getTokenValue(bidTokenForInterstitial1, AdUnitType.CRITEO_INTERSTITIAL));
    }

    @Test
    public void testReturnNullWhenWrongAdUnitType() {
        TokenValue tokenForBanner1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForInterstitial1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);

        BidToken bidTokenForBanner1 = tokenCache.add(tokenForBanner1);
        BidToken bidTokenForInterstitial1 = tokenCache.add(tokenForInterstitial1);

        Assert.assertNull(tokenCache.getTokenValue(bidTokenForBanner1, AdUnitType.CRITEO_INTERSTITIAL));
        Assert.assertNull(tokenCache.getTokenValue(bidTokenForInterstitial1, AdUnitType.CRITEO_BANNER));
    }

    @Test
    public void testGetTokenForBidAndGetValueForTokenNotEquals() {
        TokenValue tokenForBanner1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForBanner2 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForInterstitial1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);
        TokenValue tokenForInterstitial2 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);

        BidToken bidTokenForBanner2 = tokenCache.add(tokenForBanner2);
        BidToken bidTokenForInterstitial2 = tokenCache.add(tokenForInterstitial2);
        Assert.assertNotEquals(tokenForBanner1, tokenCache.getTokenValue(bidTokenForBanner2, AdUnitType.CRITEO_BANNER));
        Assert.assertNotEquals(tokenForInterstitial1,
                tokenCache.getTokenValue(bidTokenForInterstitial2, AdUnitType.CRITEO_INTERSTITIAL));
    }

    //Checks TokenCache removes token after call getTokenValue
    @Test
    public void testGetTokenForBidAndGetValueForTokenCheckNull() {
        TokenValue tokenForBanner1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_BANNER);
        TokenValue tokenForInterstitial1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE, AdUnitType.CRITEO_INTERSTITIAL);

        BidToken bidTokenForBanner1 = tokenCache.add(tokenForBanner1);
        BidToken bidTokenForInterstitial1 = tokenCache.add(tokenForInterstitial1);
        tokenCache.getTokenValue(bidTokenForBanner1, AdUnitType.CRITEO_BANNER);
        tokenCache.getTokenValue(bidTokenForInterstitial1, AdUnitType.CRITEO_INTERSTITIAL);
        Assert.assertNull(tokenCache.getTokenValue(bidTokenForBanner1, AdUnitType.CRITEO_BANNER));
        Assert.assertNull(tokenCache.getTokenValue(bidTokenForInterstitial1, AdUnitType.CRITEO_INTERSTITIAL));
    }

    @Test
    public void testGetUncachedTokenAndNullToken() {
        BidToken unChachedToken = new BidToken(UUID.randomUUID());
        BidToken nullToken = null;
        Assert.assertNull(tokenCache.getTokenValue(unChachedToken, AdUnitType.CRITEO_BANNER));
        Assert.assertNull(tokenCache.getTokenValue(nullToken, AdUnitType.CRITEO_BANNER));
    }

}