package com.criteo.publisher.model;

import androidx.annotation.NonNull;

public class ScreenSize implements Comparable<ScreenSize> {

  private int width;
  private int height;

  public ScreenSize(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  @Override
  public int compareTo(@NonNull ScreenSize second) {
    return width - second.width;
  }


}