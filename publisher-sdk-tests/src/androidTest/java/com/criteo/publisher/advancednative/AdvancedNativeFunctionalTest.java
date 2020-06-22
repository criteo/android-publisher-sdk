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
import static com.criteo.publisher.activity.TestNativeActivity.ADVERTISER_DESCRIPTION_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.ADVERTISER_DOMAIN_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.ADVERTISER_LOGO_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.AD_LAYOUT_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.CALL_TO_ACTION_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.DESCRIPTION_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.PRICE_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.PRODUCT_IMAGE_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.RECYCLER_VIEW_TAG;
import static com.criteo.publisher.activity.TestNativeActivity.TITLE_TAG;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.BidResponse;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.TestAdUnits;
import com.criteo.publisher.activity.TestNativeActivity;
import com.criteo.publisher.adview.Redirection;
import com.criteo.publisher.mock.MockBean;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.model.nativeads.NativeProduct;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;

public class AdvancedNativeFunctionalTest {

  @Rule
  public ActivityTestRule<TestNativeActivity> activityRule = new ActivityTestRule<>(TestNativeActivity.class);

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private AdChoiceOverlay adChoiceOverlay;

  @MockBean
  private Redirection redirection;

  @Test
  public void loadStandaloneAdInAdLayout_GivenValidBid_DisplayAllInformationInViews() throws Exception {
    givenInitializedCriteo(TestAdUnits.NATIVE);
    mockedDependenciesRule.waitForIdleState();

    TestNativeActivity activity = activityRule.getActivity();
    activity.loadStandaloneAdInAdLayout();
    mockedDependenciesRule.waitForIdleState();

    // Check there is one ad
    ViewGroup adLayout = getAdLayout();
    assertEquals(1, adLayout.getChildCount());

    checkAllInformationAreDisplayed((ViewGroup) adLayout.getChildAt(0));
  }

  @Test
  public void loadInHouseAdInAdLayout_GivenValidBid_DisplayAllInformationInViews() throws Exception {
    givenInitializedCriteo(TestAdUnits.NATIVE);
    mockedDependenciesRule.waitForIdleState();

    TestNativeActivity activity = activityRule.getActivity();
    BidResponse bidResponse = Criteo.getInstance().getBidResponse(TestAdUnits.NATIVE);
    activity.loadInHouseAdInAdLayout(bidResponse.getBidToken());
    mockedDependenciesRule.waitForIdleState();

    // Check there is one ad
    ViewGroup adLayout = getAdLayout();
    assertEquals(1, adLayout.getChildCount());

    checkAllInformationAreDisplayed((ViewGroup) adLayout.getChildAt(0));
  }

  @Test
  public void loadStandaloneAdInRecyclerView_GivenValidBid_DisplayAllInformationInViews() throws Exception {
    givenInitializedCriteo(TestAdUnits.NATIVE);
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
    givenInitializedCriteo(TestAdUnits.NATIVE);
    mockedDependenciesRule.waitForIdleState();

    TestNativeActivity activity = activityRule.getActivity();
    BidResponse bidResponse1 = Criteo.getInstance().getBidResponse(TestAdUnits.NATIVE);
    activity.loadInHouseAdInRecyclerView(bidResponse1.getBidToken());
    mockedDependenciesRule.waitForIdleState();

    BidResponse bidResponse2 = Criteo.getInstance().getBidResponse(TestAdUnits.NATIVE);
    activity.loadInHouseAdInRecyclerView(bidResponse2.getBidToken());
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
