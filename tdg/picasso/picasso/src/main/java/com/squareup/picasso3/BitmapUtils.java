/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.picasso3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.util.Size;
import android.util.TypedValue;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import java.io.IOException;
import java.nio.ByteBuffer;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

import static android.os.Build.VERSION.SDK_INT;
import static com.squareup.picasso3.Utils.checkNotNull;

final class @Nullable BitmapUtils {
  /**
   * Lazily create {@link BitmapFactory.Options} based in given
   * {@link Request}, only instantiating them if needed.
   */
  @Nullable static BitmapFactory.Options @Nullable createBitmapOptions(Request data) {
    final boolean justBounds = data.hasSize();
    BitmapFactory.Options options = null;
    if (justBounds || data.config != null || data.purgeable) {
      options = new BitmapFactory.Options();
      options.inJustDecodeBounds = justBounds;
      options.inInputShareable = data.purgeable;
      options.inPurgeable = data.purgeable;
      if (data.config != @Nullable null) {
        options.inPreferredConfig = data.config;
      }
    }
    return options;
  }

  static boolean @Nullable requiresInSampleSize(@Nullable BitmapFactory.Options @Nullable options) {
    return options != @Nullable null && options.inJustDecodeBounds;
  }

  static void calculateInSampleSize(int reqWidth, int reqHeight,
      @NonNull BitmapFactory.Options options, Request request) {
    calculateInSampleSize(reqWidth, reqHeight, options.outWidth, options.outHeight, options,
        request);
  }

  static boolean @Nullable shouldResize(boolean onlyScaleDown, int inWidth, int @Nullable inHeight,
      int targetWidth, int @Nullable targetHeight) {
    return !onlyScaleDown || (targetWidth != 0 && inWidth > targetWidth)
        || (targetHeight != 0 && inHeight > targetHeight);
  }

  static void calculateInSampleSize(int reqWidth, int reqHeight, int @Nullable width, int @Nullable height,
      BitmapFactory.Options @Nullable options, Request @Nullable request) {
    int sampleSize = 1;
    if (height > reqHeight || width > reqWidth) {
      final int heightRatio;
      final int widthRatio;
      if (reqHeight == 0) {
        sampleSize = (int) Math.floor((float) width / (float) reqWidth);
      } else if (reqWidth == 0) {
        sampleSize = (int) Math.floor((float) height / (float) reqHeight);
      } else {
        heightRatio = (int) Math.floor((float) height / (float) reqHeight);
        widthRatio = (int) Math.floor((float) width / (float) reqWidth);
        sampleSize = request.centerInside
            ? Math.max(heightRatio, widthRatio)
            : Math.min(heightRatio, widthRatio);
      }
    }
    options.inSampleSize = sampleSize;
    options.inJustDecodeBounds = false;
  }

  /**
   * Decode a byte stream into a Bitmap. This method will take into account additional information
   * about the supplied request in order to do the decoding efficiently (such as through leveraging
   * {@code inSampleSize}).
   */
  static Bitmap @Nullable decodeStream(Source source, Request @Nullable request) throws IOException {
    ExceptionCatchingSource exceptionCatchingSource = new ExceptionCatchingSource(source);
    BufferedSource bufferedSource = Okio.buffer(exceptionCatchingSource);
    Bitmap bitmap = SDK_INT >= 28
        ? decodeStreamP(request, bufferedSource)
        : decodeStreamPreP(request, bufferedSource);
    exceptionCatchingSource.throwIfCaught();
    return bitmap;
  }

  @RequiresApi(28)
  @SuppressLint("Override")
  private static Bitmap decodeStreamP(Request request, BufferedSource bufferedSource)
      throws IOException {
    ImageDecoder.Source imageSource =
        ImageDecoder.createSource(ByteBuffer.wrap(bufferedSource.readByteArray()));
    return decodeImageSource(imageSource, request);
  }

  private static Bitmap @Nullable decodeStreamPreP(Request request, BufferedSource @Nullable bufferedSource)
      throws IOException {
    boolean isWebPFile = Utils.isWebPFile(bufferedSource);
    boolean isPurgeable = request.purgeable && SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    BitmapFactory.Options options = createBitmapOptions(request);
    boolean calculateSize = requiresInSampleSize(options);

    Bitmap bitmap;
    // We decode from a byte array because, a) when decoding a WebP network stream, BitmapFactory
    // throws a JNI Exception, so we workaround by decoding a byte array, or b) user requested
    // purgeable, which only affects bitmaps decoded from byte arrays.
    if (isWebPFile || isPurgeable) {
      byte[] bytes = bufferedSource.readByteArray();
      if (calculateSize) {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        calculateInSampleSize(request.targetWidth, request.targetHeight,
            checkNotNull(options, "options == null"), request);
      }
      bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    } else {
      if (calculateSize) {
        BitmapFactory.decodeStream(bufferedSource.peek().inputStream(), null, options);
        calculateInSampleSize(request.targetWidth, request.targetHeight,
            checkNotNull(options, "options == null"), request);
      }
      bitmap = BitmapFactory.decodeStream(bufferedSource.inputStream(), null, options);
    }
    if (bitmap == @Nullable null) {
      // Treat null as an IO exception, we will eventually retry.
      throw new IOException("Failed to decode bitmap.");
    }
    return bitmap;
  }

  static Bitmap @Nullable decodeResource(Context context, Request request)
      throws IOException {
    if (SDK_INT >= 28) {
      return decodeResourceP(context, request);
    }

    Resources resources = Utils.getResources(context, request);
    int id = Utils.getResourceId(resources, request);
    return decodeResourcePreP(resources, id, request);
  }

  @RequiresApi(28)
  private static Bitmap decodeResourceP(Context @Nullable context, final Request @Nullable request) throws IOException {
    ImageDecoder.Source imageSource =
        ImageDecoder.createSource(context.getResources(), request.resourceId);
    return decodeImageSource(imageSource, request);
  }

  private static Bitmap @Nullable decodeResourcePreP(Resources @Nullable resources, int id, Request request) {
    final BitmapFactory.Options options = createBitmapOptions(request);
    if (requiresInSampleSize(options)) {
      BitmapFactory.decodeResource(resources, id, options);
      calculateInSampleSize(request.targetWidth, request.targetHeight,
          checkNotNull(options, "options == null"), request);
    }
    return BitmapFactory.decodeResource(resources, id, options);
  }

  @RequiresApi(28)
  private static Bitmap decodeImageSource(ImageDecoder.Source imageSource, final Request @Nullable request)
      throws IOException {
    return ImageDecoder.decodeBitmap(imageSource, new ImageDecoder.OnHeaderDecodedListener() {
      @Override
      public void @Nullable onHeaderDecoded(@NonNull ImageDecoder @Nullable imageDecoder,
          @NonNull ImageDecoder.ImageInfo @Nullable imageInfo, @NonNull ImageDecoder.Source source) {
        if (request.hasSize()) {
          Size size = imageInfo.getSize();
          if (shouldResize(request.onlyScaleDown, size.getWidth(), size.getHeight(),
              request.targetWidth, request.targetHeight)) {
            imageDecoder.setTargetSize(request.targetWidth, request.targetHeight);
          }
        }
      }
    });
  }

  static boolean isXmlResource(Resources resources, @DrawableRes int @Nullable drawableId) {
    TypedValue typedValue = new TypedValue();
    resources.getValue(drawableId, typedValue, true);
    CharSequence file = typedValue.string;
    return file != null && file.toString().endsWith(".xml");
  }

  static final class @Nullable ExceptionCatchingSource extends ForwardingSource {
    @Nullable IOException thrownException;

    ExceptionCatchingSource(Source delegate) {
      super(delegate);
    }

    @Override public long @Nullable read(Buffer @Nullable sink, long byteCount) throws IOException {
      try {
        return super.read(sink, byteCount);
      } catch (IOException e) {
        thrownException = e;
        throw e;
      }
    }

    void throwIfCaught() throws IOException {
      if (thrownException != @Nullable null) {
        // TODO: Log when Android returns a non-null Bitmap after swallowing an IOException.
        // TODO: https://github.com/square/picasso/issues/2003/
        throw thrownException;
      }
    }
  }

  private BitmapUtils() {
    throw new AssertionError("No instances.");
  }
}
