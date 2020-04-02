package com.criteo.publisher.privacy.gdpr;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.concurrent.ThreadingUtil.waitForAllThreads;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.ResultCaptor;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.InterstitialAdUnit;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class GdprTcfEndToEndWithCdbTests {

  private static final String TCF2_CONSENT_NOT_GIVEN = "COwJDpQOwJDpQIAAAAENAPCgAAAAAAAAAAAAAxQAgAsABiAAAAAA";
  private static final String TCF2_CONSENT_GIVEN = "COwJDpQOwJDpQIAAAAENAPCgAAAAAAAAAAAAAxQAQAtgAAAA";
  private static final String TCF1_CONSENT_NOT_GIVEN = "BOnz82JOnz82JABABBFRCPgAAAAFuABABAA";
  private static final String TCF1_CONSENT_GIVEN = "BOnz814Onz814ABABBFRCP4AAAAFuABAC2A";

  private final InterstitialAdUnit validInterstitialAdUnit = TestAdUnits.INTERSTITIAL;

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  private SharedPreferences sharedPreferences;

  @Before
  public void setUp() {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  @After
  public void tearDown() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.remove("IABTCF_TCString");
    editor.remove("IABTCF_gdprApplies");
    editor.remove("IABConsent_ConsentString");
    editor.remove("IABConsent_SubjectToGDPR");
    editor.apply();
  }

  @Test
  public void whenCdbCall_givenTcf2ConsentNotGiven_ShouldNotBid() throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf2NegativeConsent();

    // when
    whenBidding();

    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertEquals(0, lastCaptureValue.getSlots().size());
  }

  @Test
  public void whenCdbCall_givenTcf2ConsentGiven_ShouldBid() throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf2PositiveConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertTrue(lastCaptureValue.getSlots().size() > 0);
  }

  @Test
  public void whenCdbCall_givenTcf1ConsentGiven_ShouldBid() throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf1PositiveConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertTrue(lastCaptureValue.getSlots().size() > 0);
  }

  @Test
  public void whenCdbCall_givenTcf1ConsentNotGiven_ShouldNotBid() throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf1NegativeConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertEquals(0, lastCaptureValue.getSlots().size());
  }

  @Test
  public void whenCdbCall_givenTcf2ConsentGiven_andTcf1ConsentNotGiven_ShouldBid()
      throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf2PositiveConsent();
    givenTcf1NegativeConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertTrue(lastCaptureValue.getSlots().size() > 0);
  }

  @Test
  public void whenCdbCall_givenTcf1ConsentGiven_andTcf2ConsentGiven_ShouldBid()
      throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf1PositiveConsent();
    givenTcf2PositiveConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertTrue(lastCaptureValue.getSlots().size() > 0);
  }

  @Test
  public void whenCdbCall_givenTcf1ConsentNotGiven_andTcf2ConsentNotGiven_ShouldBid()
      throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf1NegativeConsent();
    givenTcf2NegativeConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertEquals(0, lastCaptureValue.getSlots().size());
  }

  @Test
  public void whenCdbCall_givenTcf2ConsentNotGiven_andTcf1ConsentGiven_ShouldNotBid()
      throws Exception {
    ResultCaptor<CdbResponse> cdbResultCaptor = mockedDependenciesRule.captorCdbResult();

    // given
    givenInitializedSdk();
    givenTcf2NegativeConsent();
    givenTcf1PositiveConsent();

    // when
    whenBidding();

    // then
    CdbResponse lastCaptureValue = cdbResultCaptor.getLastCaptureValue();
    assertEquals(0, lastCaptureValue.getSlots().size());
  }

  private void givenInitializedSdk(AdUnit... preloadedAdUnits) throws Exception {
    givenInitializedCriteo(preloadedAdUnits);
    waitForBids();
  }

  private void waitForBids() {
    waitForAllThreads(mockedDependenciesRule.getTrackingCommandsExecutor());
  }

  private void givenTcf1PositiveConsent() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("IABConsent_ConsentString", TCF1_CONSENT_GIVEN);
    editor.putString("IABConsent_SubjectToGDPR", "1");
    editor.apply();
  }

  private void givenTcf1NegativeConsent() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("IABConsent_ConsentString", TCF1_CONSENT_NOT_GIVEN);
    editor.putString("IABConsent_SubjectToGDPR", "1");
    editor.apply();
  }

  private void givenTcf2PositiveConsent() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("IABTCF_TCString", TCF2_CONSENT_GIVEN);
    editor.putInt("IABTCF_gdprApplies", 1);
    editor.apply();
  }

  private void givenTcf2NegativeConsent() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("IABTCF_TCString", TCF2_CONSENT_NOT_GIVEN);
    editor.putInt("IABTCF_gdprApplies", 1);
    editor.apply();
  }

  private void whenBidding() {
    Criteo.getInstance().getBidResponse(validInterstitialAdUnit);
    waitForBids();
  }
}
