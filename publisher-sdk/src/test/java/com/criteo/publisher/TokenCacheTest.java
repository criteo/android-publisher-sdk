package com.criteo.publisher;

import static org.mockito.Mockito.when;

import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.TokenValue;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TokenCacheTest {
    private static final String TEST_CREATIVE = "https://rdi.us.criteo.com/delivery/r/ajs.php?did=5c87fcdb7cc0d71b24ee2ee6454eb810&u=%7CvsLBMQ0Ek4IxXQb0B5n7RyCAQymjqwh29YhNM9EzK9Q%3D%7C&c1=fYGSyyN4O4mkT2ynhzfwbdpiG7v0SMGpms6Tk24GWc957HzbzgL1jw-HVL5D0BjRx5ef3wBVfDXXmh9StLy8pf5kDJtrQLTLQrexjq5CZt9tEDx9mY8Y-eTV19PWOQoNjXkJ4_mhKqV0IfwHDIfLVDBWmsizVCoAtU1brQ2weeEkUU5-mDfn3qzTX3jPXszef5bC3pbiLJAK3QamQlglD1dkWYOkUwLAXxMjr2MXeBQk2YK-_qYz0fMVJG0xWJ-jVmsqdOw9A9rkGIgToRoUewB0VAu5eSkjSBoGs4yEbsnJ5Ssq5fquJMNvm6T77b8fzQI-eXgwoEfKkdAuCbj3gNrPBgzGZAJPGO-TYvJgs22Bljy-hNCk1E0E030zLtKo-XvAVRvZ5PswtwoccPSl6u1wiV8fMCXHx9QW9-fdXaVxzZe9AZB6w7pHxKUwiRK9";

    private TokenCache tokenCache;
    private BannerAdUnit bannerAdUnit1;
    private BannerAdUnit bannerAdUnit2;
    private InterstitialAdUnit interstitialAdUnit1;
    private InterstitialAdUnit interstitialAdUnit2;

    @Mock
    private Clock clock;

    @Before
    public void prepare() {
        MockitoAnnotations.initMocks(this);
        tokenCache = new TokenCache();
        bannerAdUnit1 = new BannerAdUnit("banneradUnitId1", new AdSize(320, 50));
        interstitialAdUnit1 = new InterstitialAdUnit("interstitialadUnitId1");
    }

    @Test
    public void testGetTokenForBidAndGetValueForToken() {

        bannerAdUnit2 = new BannerAdUnit("banneradUnitId2", new AdSize(320, 50));
        interstitialAdUnit2 = new InterstitialAdUnit("interstitialadUnitId2");

        TokenValue tokenForBanner1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_BANNER, clock);
        TokenValue tokenForBanner2 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_BANNER, clock);
        TokenValue tokenForInterstitial1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_INTERSTITIAL, clock);
        TokenValue tokenForInterstitial2 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_INTERSTITIAL, clock);

        BidToken bidTokenForBanner1 = tokenCache.add(tokenForBanner1, bannerAdUnit1);
        BidToken bidTokenForBanner2 = tokenCache.add(tokenForBanner2, bannerAdUnit2);
        BidToken bidTokenForInterstitial1 = tokenCache.add(tokenForInterstitial1, interstitialAdUnit1);
        BidToken bidTokenForInterstitial2 = tokenCache.add(tokenForInterstitial2, interstitialAdUnit2);

        Assert.assertEquals(tokenForBanner1, tokenCache.getTokenValue(bidTokenForBanner1, AdUnitType.CRITEO_BANNER));
        Assert.assertEquals(tokenForBanner2, tokenCache.getTokenValue(bidTokenForBanner2, AdUnitType.CRITEO_BANNER));
        Assert.assertEquals(tokenForInterstitial1,
                tokenCache.getTokenValue(bidTokenForInterstitial1, AdUnitType.CRITEO_INTERSTITIAL));
        Assert.assertEquals(tokenForInterstitial2,
                tokenCache.getTokenValue(bidTokenForInterstitial2, AdUnitType.CRITEO_INTERSTITIAL));
    }

    @Test
    public void testReturnNullWhenTokenExpired() {
        long  bidTime = System.currentTimeMillis();
        int bidTtlInSeconds = 100;
        when(clock.getCurrentTimeInMillis()).thenReturn(bidTime + bidTtlInSeconds * 1000 + 1);

        TokenValue tokenForBanner1 = new TokenValue(bidTime, bidTtlInSeconds, TEST_CREATIVE,
                AdUnitType.CRITEO_BANNER, clock);

        TokenValue tokenForInterstitial1 = new TokenValue(bidTime, bidTtlInSeconds, TEST_CREATIVE,
                AdUnitType.CRITEO_INTERSTITIAL, clock);

        BidToken bidTokenForBanner1 = tokenCache.add(tokenForBanner1, bannerAdUnit1);
        BidToken bidTokenForInterstitial1 = tokenCache.add(tokenForInterstitial1, interstitialAdUnit1);

        Assert.assertNull(tokenCache.getTokenValue(bidTokenForBanner1, AdUnitType.CRITEO_BANNER));
        Assert.assertNull(tokenCache.getTokenValue(bidTokenForInterstitial1, AdUnitType.CRITEO_INTERSTITIAL));
    }

    @Test
    public void testReturnNullWhenWrongAdUnitType() {
        TokenValue tokenForBanner1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_BANNER, clock);
        TokenValue tokenForInterstitial1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_INTERSTITIAL, clock);

        BidToken bidTokenForBanner1 = tokenCache.add(tokenForBanner1, bannerAdUnit1);
        BidToken bidTokenForInterstitial1 = tokenCache.add(tokenForInterstitial1, bannerAdUnit1);

        Assert.assertNull(tokenCache.getTokenValue(bidTokenForBanner1, AdUnitType.CRITEO_INTERSTITIAL));
        Assert.assertNull(tokenCache.getTokenValue(bidTokenForInterstitial1, AdUnitType.CRITEO_BANNER));
    }

    @Test
    public void testGetTokenForBidAndGetValueForTokenNotEquals() {
        bannerAdUnit2 = new BannerAdUnit("banneradUnitId2", new AdSize(320, 50));
        interstitialAdUnit2 = new InterstitialAdUnit("interstitialadUnitId2");

        TokenValue tokenForBanner1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_BANNER, clock);
        TokenValue tokenForBanner2 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_BANNER, clock);
        TokenValue tokenForInterstitial1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_INTERSTITIAL, clock);
        TokenValue tokenForInterstitial2 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_INTERSTITIAL, clock);

        BidToken bidTokenForBanner2 = tokenCache.add(tokenForBanner2, bannerAdUnit2);
        BidToken bidTokenForInterstitial2 = tokenCache.add(tokenForInterstitial2, interstitialAdUnit2);
        Assert.assertNotEquals(tokenForBanner1, tokenCache.getTokenValue(bidTokenForBanner2, AdUnitType.CRITEO_BANNER));
        Assert.assertNotEquals(tokenForInterstitial1,
                tokenCache.getTokenValue(bidTokenForInterstitial2, AdUnitType.CRITEO_INTERSTITIAL));
    }

    //Checks TokenCache removes token after call getTokenValue
    @Test
    public void testGetTokenForBidAndGetValueForTokenCheckNull() {
        TokenValue tokenForBanner1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_BANNER, clock);
        TokenValue tokenForInterstitial1 = new TokenValue(System.currentTimeMillis(), 100, TEST_CREATIVE,
                AdUnitType.CRITEO_INTERSTITIAL, clock);

        BidToken bidTokenForBanner1 = tokenCache.add(tokenForBanner1, bannerAdUnit1);
        BidToken bidTokenForInterstitial1 = tokenCache.add(tokenForInterstitial1, interstitialAdUnit1);
        tokenCache.getTokenValue(bidTokenForBanner1, AdUnitType.CRITEO_BANNER);
        tokenCache.getTokenValue(bidTokenForInterstitial1, AdUnitType.CRITEO_INTERSTITIAL);
        Assert.assertNull(tokenCache.getTokenValue(bidTokenForBanner1, AdUnitType.CRITEO_BANNER));
        Assert.assertNull(tokenCache.getTokenValue(bidTokenForInterstitial1, AdUnitType.CRITEO_INTERSTITIAL));
    }

    @Test
    public void testGetUncachedTokenAndNullToken() {
        BidToken unChachedToken = new BidToken(UUID.randomUUID(), bannerAdUnit1);
        BidToken nullToken = null;
        Assert.assertNull(tokenCache.getTokenValue(unChachedToken, AdUnitType.CRITEO_BANNER));
        Assert.assertNull(tokenCache.getTokenValue(nullToken, AdUnitType.CRITEO_BANNER));
    }

}