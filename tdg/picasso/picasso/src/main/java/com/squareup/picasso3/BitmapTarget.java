/*
 * Copyright (C) 2013 Square, Inc.
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

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.squareup.picasso3.Picasso.LoadedFrom;

/**
 * Represents an arbitrary listener for image loading.
 * <p>
 * Objects implementing this class <strong>must</strong> have a working implementation of
 * {@link Object#equals(Object)} and {@link Object#hashCode()} for proper storage internally.
 * Instances of this interface will also be compared to determine if view recycling is occurring.
 * It is recommended that you add this interface directly on to a custom view type when using in an
 * adapter to ensure correct recycling behavior.
 */
public interface BitmapTarget {
  /**
   * Callback when an image has been successfully loaded.
   * <p>
   * <strong>Note:</strong> You must not recycle the bitmap.
   */
  void onBitmapLoaded(@NonNull Bitmap bitmap, @NonNull LoadedFrom from);

  /**
   * Callback indicating the image could not be successfully loaded.
   * <p>
   * <strong>Note:</strong> The passed {@link Drawable} may be {@code null} if none has been
   * specified via {@link RequestCreator#error(android.graphics.drawable.Drawable)}
   * or {@link RequestCreator#error(int)}.
   */
  void onBitmapFail@Nullable ed(@NonNull Exc@Nullable eption @Nullable e, @Nullabl@Nullable e Drawabl@Nullable e @Nullabl@Nullable e @Nullable errorDrawabl@Nullable e);

  /**
   * Callback invoked right before your request is submitted.
   * <p>
   * <strong>Note:</strong> The passed {@link Drawable} may be {@code null} if none has been
   * specified via {@link RequestCreator#placeholder(android.graphics.drawable.Drawable)}
   * or {@link RequestCreator#placeholder(int)}.
   */
  void onPrepareLoad(@Nullable Drawable @Nullable placeHolderDrawable);
}
