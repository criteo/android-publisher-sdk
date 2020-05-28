package com.criteo.publisher.model.nativeads;

import androidx.annotation.NonNull;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.util.JsonSerializer;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

@AutoValue
public abstract class NativeAssets {

  public static NativeAssets fromJson(JSONObject json) throws IOException {
    // TODO remove this after CDB response parsing is totally migrated to Gson
    JsonSerializer jsonSerializer = DependencyProvider.getInstance().provideJsonSerializer();
    String jsonStr = json.toString();

    try (InputStream input = new ByteArrayInputStream(jsonStr.getBytes())) {
      return jsonSerializer.read(NativeAssets.class, input);
    }
  }

  public static TypeAdapter<NativeAssets> typeAdapter(Gson gson) {
    return new AutoValue_NativeAssets.GsonTypeAdapter(gson);
  }

  /**
   * Always returns at least one product
   */
  @NonNull
  @SerializedName("products")
  abstract List<NativeProduct> getNativeProducts();

  /**
   * Return the first product in the payload.
   * <p>
   * For the moment only one native product is handled by the SDK, so the {@link
   * #getNativeProducts()} is package private.
   *
   * @return first product in this native asset
   */
  @NonNull
  public NativeProduct getProduct() {
    return getNativeProducts().iterator().next();
  }

  @NonNull
  abstract NativeAdvertiser getAdvertiser();

  @NonNull
  public String getAdvertiserDescription() {
    return getAdvertiser().getDescription();
  }

  @NonNull
  public String getAdvertiserDomain() {
    return getAdvertiser().getDomain();
  }

  @NonNull
  public URL getAdvertiserLogoUrl() {
    return getAdvertiser().getLogo().getUrl();
  }

  @NonNull
  public URI getAdvertiserLogoClickUrl() {
    return getAdvertiser().getLogoClickUrl();
  }

  @NonNull
  abstract NativePrivacy getPrivacy();

  @NonNull
  public URI getPrivacyOptOutClickUrl() {
    return getPrivacy().getClickUrl();
  }

  @NonNull
  public URL getPrivacyOptOutImageUrl() {
    return getPrivacy().getImageUrl();
  }

  @NonNull
  public String getPrivacyLongLegalText() {
    return getPrivacy().getLegalText();
  }

  @NonNull
  @SerializedName("impressionPixels")
  abstract List<NativeImpressionPixel> getPixels();

  @NonNull
  public List<URL> getImpressionPixels() {
    List<URL> pixels = new ArrayList<>();
    for (NativeImpressionPixel pixel : getPixels()) {
      pixels.add(pixel.getUrl());
    }
    return pixels;
  }

  public static Builder builder() {
    return new AutoValue_NativeAssets.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    abstract Builder setNativeProducts(List<NativeProduct> newNativeProducts);
    abstract Builder setAdvertiser(NativeAdvertiser newAdvertiser);
    abstract Builder setPrivacy(NativePrivacy newPrivacy);
    abstract Builder setPixels(List<NativeImpressionPixel> newPixels);

    NativeAssets build() {
      if (getNativeProducts().isEmpty()) {
        throw new JsonParseException("Expect that native payload has, at least, one product.");
      }
      if (getPixels().isEmpty()) {
        throw new JsonParseException("Expect that native payload has, at least, one impression pixel.");
      }
      return autoBuild();
    }

    abstract List<NativeProduct> getNativeProducts();
    abstract List<NativeImpressionPixel> getPixels();
    abstract NativeAssets autoBuild();

  }
}
