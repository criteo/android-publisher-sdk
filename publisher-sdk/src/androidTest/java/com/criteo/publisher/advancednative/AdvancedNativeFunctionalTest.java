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

package com.criteo.publisher.advancednative;

import static com.criteo.publisher.CriteoUtil.givenInitializedCriteo;
import static com.criteo.publisher.StubConstants.STUB_NATIVE_ASSETS;
import static com.criteo.publisher.TestAdUnits.NATIVE;
import static com.criteo.publisher.activity.TestNativeActivity.ADVERTISER_DESCRIPTION_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.ADVERTISER_DOMAIN_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.ADVERTISER_LOGO_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.AD_LAYOUT_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.CALL_TO_ACTION_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.DESCRIPTION_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.PRICE_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.PRIVACY_LEGAL_TEXT_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.PRODUCT_IMAGE_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.RECYCLER_VIEW_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.TITLE_TAG;
import static com.criteo.publisher.advancednative.NativeLogMessage.onNativeClicked;
import static com.criteo.publisher.advancednative.NativeLogMessage.onNativeImpressionRegistered;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.activity.TestNativeActivity;
import com.criteo.publisher.adview.Redirection;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.integration.Integration;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.mock.MockBean;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.model.NativeAdUnit;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.model.nativeads.NativeProduct;
import com.criteo.publisher.network.PubSdkApi;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AdvancedNativeFunctionalTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule().withMockedLogger();

  @Rule
  public ActivityTestRule<TestNativeActivity> activityRule = new ActivityTestRule<>(
      TestNativeActivity.class, false, false);

  @Inject
  private AdChoiceOverlay adChoiceOverlay;

  @MockBean
  private Redirection redirection;

  @SpyBean
  private PubSdkApi api;

  private Logger logger;

  @Before
  public void setUp() {
    // Start activity only there so beans are properly injected
    activityRule.launchActivity(new Intent());

    logger = mockedDependenciesRule.getMockedLogger();
  }

  @Test
  public void loadStandaloneAdInAdLayout_GivenValidBid_DisplayAllInformationInViews()
      throws Exception {
    givenInitializedCriteo(NATIVE);
    mockedDependenciesRule.waitForIdleState();

    TestNativeActivity activity = activityRule.getActivity();
    activity.loadStandaloneAdInAdLayout();
    mockedDependenciesRule.waitForIdleState();

    // Check there is one ad
    ViewGroup adLayout = getAdLayout();
    assertEquals(1, adLayout.getChildCount());

    checkAllInformationAreDisplayed((ViewGroup) adLayout.getChildAt(0));

    verify(api, atLeastOnce()).loadCdb(
        argThat(request -> request.getProfileId() == Integration.STANDALONE.getProfileId()),
        any()
    );

    verify(logger).log(onNativeImpressionRegistered(NATIVE));
    verify(logger, atLeastOnce()).log(onNativeClicked(NATIVE));
  }

  @Test
  public void loadInHouseAdInAdLayout_GivenValidBid_DisplayAllInformationInViews() throws Exception {
    givenInitializedCriteo(NATIVE);
    mockedDependenciesRule.waitForIdleState();

    TestNativeActivity activity = activityRule.getActivity();
    Criteo.getInstance().loadBid(NATIVE, new ContextData(), activity::loadInHouseAdInAdLayout);
    mockedDependenciesRule.waitForIdleState();

    // Check there is one ad
    ViewGroup adLayout = getAdLayout();
    assertEquals(1, adLayout.getChildCount());

    checkAllInformationAreDisplayed((ViewGroup) adLayout.getChildAt(0));

    verify(api, atLeastOnce()).loadCdb(
        argThat(request -> request.getProfileId() == Integration.IN_HOUSE.getProfileId()),
        any()
    );

    verify(logger).log(onNativeImpressionRegistered((NativeAdUnit) null));
    verify(logger, atLeastOnce()).log(onNativeClicked((NativeAdUnit) null));
  }

  @Test
  public void loadStandaloneAdInRecyclerView_GivenValidBid_DisplayAllInformationInViews() throws Exception {
    givenInitializedCriteo(NATIVE);
    mockedDependenciesRule.waitForIdleState();

    TestNativeActivity activity = activityRule.getActivity();
    activity.loadStandaloneAdInRecyclerView();
    mockedDependenciesRule.waitForIdleState();

    activity.loadStandaloneAdInRecyclerView();
    mockedDependenciesRule.waitForIdleState();

    // Check there is two ads
    ViewGroup recyclerView = getRecyclerView();
    assertEquals(2, recyclerView.getChildCount());

    checkAllInformationAreDisplayed((ViewGroup) recyclerView.getChildAt(0));
    checkAllInformationAreDisplayed((ViewGroup) recyclerView.getChildAt(1));
  }

  @Test
  public void loadInHouseAdInRecyclerView_GivenValidBid_DisplayAllInformationInViews() throws Exception {
    givenInitializedCriteo(NATIVE);
    mockedDependenciesRule.waitForIdleState();

    TestNativeActivity activity = activityRule.getActivity();
    Criteo.getInstance().loadBid(NATIVE, new ContextData(), activity::loadInHouseAdInRecyclerView);
    mockedDependenciesRule.waitForIdleState();

    Criteo.getInstance().loadBid(NATIVE, new ContextData(), activity::loadInHouseAdInRecyclerView);
    mockedDependenciesRule.waitForIdleState();

    // Check there is two ads
    ViewGroup recyclerView = getRecyclerView();
    assertEquals(2, recyclerView.getChildCount());

    checkAllInformationAreDisplayed((ViewGroup) recyclerView.getChildAt(0));
    checkAllInformationAreDisplayed((ViewGroup) recyclerView.getChildAt(1));
  }

  private void checkAllInformationAreDisplayed(ViewGroup nativeAdView) {
    NativeAssets expectedAssets = STUB_NATIVE_ASSETS;

    // Check assets
    NativeProduct product = expectedAssets.getProduct();
    assertEquals(product.getTitle(), textInView(nativeAdView, TITLE_TAG));
    assertEquals(product.getDescription(), textInView(nativeAdView, DESCRIPTION_TAG));
    assertEquals(product.getPrice(), textInView(nativeAdView, PRICE_TAG));
    assertEquals(product.getCallToAction(), textInView(nativeAdView, CALL_TO_ACTION_TAG));
    assertEquals(expectedAssets.getAdvertiserDomain(), textInView(nativeAdView, ADVERTISER_DOMAIN_TAG));
    assertEquals(expectedAssets.getAdvertiserDescription(), textInView(nativeAdView, ADVERTISER_DESCRIPTION_TAG));
    assertEquals(expectedAssets.getPrivacyLongLegalText(), textInView(nativeAdView, PRIVACY_LEGAL_TEXT_TAG));

    // Check product image that should be replaced
    Drawable defaultDrawable = activityRule.getActivity().getDefaultDrawable();
    assertNotNull(drawableInView(nativeAdView, PRODUCT_IMAGE_TAG));
    assertNotEquals(defaultDrawable, drawableInView(nativeAdView, PRODUCT_IMAGE_TAG));
    // Check logo image that should keep placeholder (there is no advertiser logo in stub assets)
    assertNotNull(drawableInView(nativeAdView, ADVERTISER_LOGO_TAG));
    assertEquals(defaultDrawable, drawableInView(nativeAdView, ADVERTISER_LOGO_TAG));

    // Check AdChoice
    ImageView adChoiceView = adChoiceOverlay.getAdChoiceView(nativeAdView);
    assertNotNull(adChoiceView);
    assertNotNull(adChoiceView.getDrawable());

    // Check click
    String clickUrl = product.getClickUrl().toString();
    checkClickOnViewRedirectTo(nativeAdView, TITLE_TAG, clickUrl);
    checkClickOnViewRedirectTo(nativeAdView, DESCRIPTION_TAG, clickUrl);
    checkClickOnViewRedirectTo(nativeAdView, PRICE_TAG, clickUrl);
    checkClickOnViewRedirectTo(nativeAdView, CALL_TO_ACTION_TAG, clickUrl);
    checkClickOnViewRedirectTo(nativeAdView, PRODUCT_IMAGE_TAG, clickUrl);
    checkClickOnViewRedirectTo(nativeAdView, ADVERTISER_DOMAIN_TAG, clickUrl);
    checkClickOnViewRedirectTo(nativeAdView, ADVERTISER_DESCRIPTION_TAG, clickUrl);
    checkClickOnViewRedirectTo(nativeAdView, ADVERTISER_LOGO_TAG, clickUrl);
    checkClickOnViewRedirectTo(nativeAdView, clickUrl);
    checkClickOnViewRedirectTo(adChoiceView, expectedAssets.getPrivacyOptOutClickUrl().toString());
    checkClickOnViewRedirectTo(nativeAdView, PRIVACY_LEGAL_TEXT_TAG, clickUrl);
  }

  private ViewGroup getAdLayout() {
    TestNativeActivity activity = activityRule.getActivity();
    return activity.getWindow().getDecorView().findViewWithTag(AD_LAYOUT_TAG);
  }

  private ViewGroup getRecyclerView() {
    TestNativeActivity activity = activityRule.getActivity();
    return activity.getWindow().getDecorView().findViewWithTag(RECYCLER_VIEW_TAG);
  }

  private CharSequence textInView(@NonNull ViewGroup view, @NonNull Object tag) {
    TextView textView = view.findViewWithTag(tag);
    return textView.getText();
  }

  private Drawable drawableInView(@NonNull ViewGroup view, @NonNull Object tag) {
    CriteoMediaView mediaView = view.findViewWithTag(tag);
    return mediaView.getImageView().getDrawable();
  }

  private void checkClickOnViewRedirectTo(@NonNull ViewGroup view, @NonNull Object tag, @NonNull String expectedRedirectionUri) {
    checkClickOnViewRedirectTo(view.findViewWithTag(tag), expectedRedirectionUri);
  }

  private void checkClickOnViewRedirectTo(@NonNull View view, @NonNull String expectedRedirectionUri) {
    clearInvocations(redirection);
    runOnMainThreadAndWait(view::performClick);

    mockedDependenciesRule.waitForIdleState();

    verify(redirection).redirect(
        eq(expectedRedirectionUri),
        eq(activityRule.getActivity().getComponentName()),
        any()
    );
  }

}
