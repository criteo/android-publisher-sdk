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

package com.criteo.publisher.model;

import static org.junit.Assert.assertEquals;

import android.os.Parcel;
import org.junit.Before;
import org.junit.Test;

public class AdSizeTest {

  private static final int HEIGHT = 10;
  private static final int WIDTH = 350;
  private AdSize adSize;

  @Before
  public void initialize() {
    adSize = new AdSize(WIDTH, HEIGHT);
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
