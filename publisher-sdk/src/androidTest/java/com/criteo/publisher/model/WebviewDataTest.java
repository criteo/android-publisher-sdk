package com.criteo.publisher.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.text.TextUtils;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.Util.MockedDependenciesRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class WebviewDataTest {
    @Rule
    public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

    private String data;

    private WebViewData webviewData;

    private Config config;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();
        config = new Config(InstrumentationRegistry.getContext());
        when(dependencyProvider.provideConfig(any(Context.class))).thenReturn(config);
        webviewData = new WebViewData(config);
    }

    @Test
    public void testSetContentWithData() {
        data = "html";
        webviewData.setContent(data);

        assertTrue(!TextUtils.isEmpty(webviewData.getContent()));
        assertFalse(webviewData.getContent().contains(config.getAdTagDataMode()));
    }
}
