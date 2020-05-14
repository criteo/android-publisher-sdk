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
import static com.criteo.publisher.activity.TestNativeActivity.TITLE_TAG;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
  public void loadStandaloneAd_GivenValidBid_DisplayAllInformationInViews() throws Exception {
    NativeAssets expectedAssets = STUB_NATIVE_ASSETS;

    givenInitializedCriteo(TestAdUnits.NATIVE);
    mockedDependenciesRule.waitForIdleState();

    TestNativeActivity activity = activityRule.getActivity();
    activity.loadStandaloneAd();
    mockedDependenciesRule.waitForIdleState();

    ViewGroup adLayout = activity.getWindow().getDecorView().findViewWithTag(AD_LAYOUT_TAG);

    // Check there is one ad
    assertEquals(1, adLayout.getChildCount());

    // Check assets
    NativeProduct product = expectedAssets.getProduct();
    assertEquals(product.getTitle(), textInView(adLayout, TITLE_TAG));
    assertEquals(product.getDescription(), textInView(adLayout, DESCRIPTION_TAG));
    assertEquals(product.getPrice(), textInView(adLayout, PRICE_TAG));
    assertEquals(product.getCallToAction(), textInView(adLayout, CALL_TO_ACTION_TAG));
    assertEquals(expectedAssets.getAdvertiserDomain(), textInView(adLayout, ADVERTISER_DOMAIN_TAG));
    assertEquals(expectedAssets.getAdvertiserDescription(), textInView(adLayout, ADVERTISER_DESCRIPTION_TAG));

    // Check product image that should be replaced
    assertNotNull(drawableInView(adLayout, PRODUCT_IMAGE_TAG));
    // FIXME EE-1052
    // assertNotEquals(activity.getDefaultDrawable(), drawableInView(adLayout, PRODUCT_IMAGE_TAG));

    // Check logo image that should keep placeholder (there is no advertiser logo in stub assets)
    assertNotNull(drawableInView(adLayout, ADVERTISER_LOGO_TAG));
    assertEquals(activity.getDefaultDrawable(), drawableInView(adLayout, ADVERTISER_LOGO_TAG));

    // Check AdChoice
    View nativeAdView = adLayout.getChildAt(0);
    ImageView adChoiceView = adChoiceOverlay.getAdChoiceView(nativeAdView);
    assertNotNull(adChoiceView);
    // FIXME EE-1052
    // assertNotNull(adChoiceView.getDrawable());

    // Check click
    String clickUrl = product.getClickUrl().toString();
    checkClickOnViewRedirectTo(adLayout, TITLE_TAG, clickUrl);
    checkClickOnViewRedirectTo(adLayout, DESCRIPTION_TAG, clickUrl);
    checkClickOnViewRedirectTo(adLayout, PRICE_TAG, clickUrl);
    checkClickOnViewRedirectTo(adLayout, CALL_TO_ACTION_TAG, clickUrl);
    checkClickOnViewRedirectTo(adLayout, PRODUCT_IMAGE_TAG, clickUrl);
    checkClickOnViewRedirectTo(adLayout, ADVERTISER_DOMAIN_TAG, clickUrl);
    checkClickOnViewRedirectTo(adLayout, ADVERTISER_DESCRIPTION_TAG, clickUrl);
    checkClickOnViewRedirectTo(adLayout, ADVERTISER_LOGO_TAG, clickUrl);
    checkClickOnViewRedirectTo(nativeAdView, clickUrl);
    checkClickOnViewRedirectTo(adChoiceView, expectedAssets.getPrivacyOptOutClickUrl().toString());
  }

  private CharSequence textInView(@NonNull ViewGroup view, @NonNull Object tag) {
    TextView textView = view.findViewWithTag(tag);
    return textView.getText();
  }

  private Drawable drawableInView(@NonNull ViewGroup view, @NonNull Object tag) {
    ImageView imageView = view.findViewWithTag(tag);
    return imageView.getDrawable();
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
