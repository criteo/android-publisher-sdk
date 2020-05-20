package com.criteo.publisher.advancednative;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.view.LayoutInflater;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.tests.R;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;

public class CriteoMediaViewTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  @Test
  public void inflatedFromXml_GivenNoAttribute_SetNoDrawable() throws Exception {
    LayoutInflater inflater = LayoutInflater.from(context);

    CriteoMediaView mediaView = (CriteoMediaView) inflater.inflate(R.layout.test_media_view_empty, null);

    assertNull(mediaView.getImageView().getDrawable());
  }

  @Test
  public void inflatedFromXml_GivenSrcAttribute_SetDrawable() throws Exception {
    LayoutInflater inflater = LayoutInflater.from(context);

    CriteoMediaView mediaView = (CriteoMediaView) inflater.inflate(R.layout.test_media_view_placeholder, null);

    assertNotNull(mediaView.getImageView().getDrawable());
  }

}