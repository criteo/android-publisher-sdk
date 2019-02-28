package com.criteo.publisher.model;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AdSizeTest {

    private static final int HEIGHT = 10;
    private static final int WIDTH = 350;
    private AdSize adSize;

    @Before
    public void initialize() {
        adSize = new AdSize(HEIGHT, WIDTH);
    }

    @Test
    public void testFormattedSize() {
        assertEquals(WIDTH + "x" + HEIGHT, adSize.getFormattedSize());
        AdSize adSizeEmpty = new AdSize();
        assertEquals("0x0", adSizeEmpty.getFormattedSize());
    }

    @Test
    public void testAdSizeParcelable() {
        Parcel parcel = Parcel.obtain();
        adSize.writeToParcel(parcel, adSize.describeContents());
        parcel.setDataPosition(0);
        AdSize adSizeFromParcel = AdSize.CREATOR.createFromParcel(parcel);
        assertEquals(adSize, adSizeFromParcel);
    }

}
