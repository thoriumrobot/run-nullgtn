/*
 * Copyright (C) 2018 Ludwig
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
package com.ludwig.keyvaluestore.types;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

/** Store a {@code java.util.List} of homogeneous values. */
public interface ListType<T> {
  /**
   * Retrieve the current {@code List} from this store using Rx. If this store has not had any
   * values written then an empty immutable {@code List} is returned by this {@link Single}.
   */
  Single<List<T>> get();

  /**
   * Retrieve the current {@code List} from this store in a blocking manner. This may take time. If
   * the store has not yet had any values written to it then this method returns an empty immutable
   * {@code List}.
   */
  List<T> blockingGet();

  /**
   * Write a {@code List} to this store and observe the operation. The {@code List} returned in the
   * {@link Single} is the {@code List} written to this store, making this useful for chaining.
   */
  Single<List<T>> observePut(final List<T> list);

  /**
   * Asynchronously write a {@code List} to this store. The write operation occurs on {@link
   * Schedulers#io()}. If you wish to specify the {@link Scheduler} then use {@link #put(List,
   * Scheduler)}.
   */
  void put(List<T> list);

  /** Write a {@code List} to this store on a specified {@link Scheduler}. */
  void put(List<T> list, Scheduler scheduler);

  /**
   * Observe changes to the {@code List} in this store. {@code onNext()} will be invoked immediately
   * with the current {@code List} upon subscription and subsequent changes thereafter.
   *
   * <p>When this store is empty the item delivered in {@code onNext()} is an empty immutable {@code
   * List}.
   */
  Observable<List<T>> observe();

  /**
   * Clear the {code List} in this store and observe the operation.
   *
   * <p>The {@code List} returned by the {@link Single} is the new, empty {@code List}, making this
   * useful for chaining.
   */
  Single<List<T>> observeClear();

  /**
   * Asynchronously clear the {@code List} in this store. The clear operation occurs on {@link
   * Schedulers#io()}. If you wish to specify the {@link Scheduler} then use {@link
   * #clear(Scheduler)}.
   */
  void clear();

  /** Clear the {@code List} from this store on a specified {@link Scheduler}. */
  void clear(Scheduler scheduler);

  /**
   * Add an item to the stored {@code List} and observe the operation. This will create a new {@code
   * List} if one does not currently exist.
   *
   * <p>The {@code List} returned by the {@link Single} is the {@code List} written to this store,
   * making this useful for chaining.
   */
  Single<List<T>> observeAdd(final T value);

  /**
   * Asynchronously add an item to the stored {@code List}. The write operation occurs on {@link
   * Schedulers#io()}. If you wish to specify the {@link Scheduler} then use {@link #add(Object,
   * Scheduler)}.
   */
  void add(T value);

  /** Add an item to the stored {@code List} on the specified {@link Scheduler}. */
  void add(T value, Scheduler scheduler);

  /**
   * Attempt to remove an item from the {@code List} and observe the operation. This method removes
   * the first item for which the predicate function returns true.
   *
   * <p>The {@code List} returned by the {@link Single} is the modified {@code List} written to this
   * store, making this useful for chaining.
   */
  Single<List<T>> observeRemove(final PredicateFunc<T> predicateFunc);

  /**
   * Asynchronously attempt to remove all items matching criteria from the {@code List}. This method
   * removes all items for which the predicate function returns true.
   *
   * <p>This operation occurs on {@link Schedulers#io()}. If you wish to specify the {@link
   * Scheduler} then use {@link #remove(Scheduler, PredicateFunc)}.
   */
  Single<List<T>> observeRemoveAll(PredicateFunc<T> predicateFunc);

  /**
   * Asynchronously attempt to remove an item from the {@code List}. This method removes the first
   * item for which the predicate function returns true.
   *
   * <p>This operation occurs on {@link Schedulers#io()}. If you wish to specify the {@link
   * Scheduler} then use {@link #remove(Scheduler, PredicateFunc)}.
   */
  void remove(PredicateFunc<T> predicateFunc);

  /**
   * Attempt to remove an item from the {@code List} on the specified {@link Scheduler}. This method
   * removes the first item for which the predicate function returns true.
   */
  void remove(Scheduler scheduler, PredicateFunc<T> predicateFunc);

  /**
   * Attempt to remove an item from the {@code List} and observe the operation. This method removes
   * the first item that {@code .equals()} the specified value.
   *
   * <p>The {@code List} returned by the {@link Single} is the modified {@code List} written to this
   * store, making this useful for chaining.
   */
  Single<List<T>> observeRemove(final T value);

  /**
   * Asynchronously attempt to remove an item from the {@code List}. This method removes the first
   * item that {@code .equals()} the specified value.
   *
   * <p>This operation occurs on {@link Schedulers#io()}. If you wish to specify the {@link
   * Scheduler} then use {@link #remove(Object, Scheduler)}.
   */
  void remove(final T value);

  /**
   * Attempt to remove an item from the {@code List} on the specified {@link Scheduler}. This method
   * removes the first item that {@code .equals()} the specified value.
   */
  void remove(final T value, Scheduler scheduler);

  /**
   * Remove the item from the {@code List} at the specified position and observe the operation.
   *
   * <p>The {@code List} returned by the {@link Single} is the modified {@code List} written to this
   * store, making this useful for chaining.
   */
  Single<List<T>> observeRemove(final int position);

  /**
   * Asynchronously remove the item from the {@code List} at the specified position.
   *
   * <p>This operation occurs on {@link Schedulers#io()}. If you wish to specify the {@link
   * Scheduler} then use {@link #remove(int, Scheduler)}.
   */
  void remove(int position);

  /** Remove an item from the {@code List} at the position on the specified {@link Scheduler}. */
  void remove(int position, Scheduler scheduler);

  /**
   * Attempt to replace an item from the {@code List} and observe the operation. This method
   * replaces the first item for which the predicate function returns true.
   *
   * <p>The {@code List} returned by the {@link Single} is the modified {@code List} written to this
   * store, making this useful for chaining.
   */
  Single<List<T>> observeReplace(final T value, final PredicateFunc<T> predicateFunc);

  /**
   * Asynchronously attempt to replace an item in the {@code List}. This method replaces the first
   * item for which the predicate function returns true.
   *
   * <p>This operation occurs on {@link Schedulers#io()}. If you wish to specify the {@link
   * Scheduler} then use {@link #replace(Object, Scheduler, PredicateFunc)}.
   */
  void replace(T value, PredicateFunc<T> predicateFunc);

  /**
   * Attempt to replace an item in the {@code List} on the specified {@link Scheduler}. This method
   * replaces the first item for which the predicate function returns true.
   */
  void replace(T value, Scheduler scheduler, PredicateFunc<T> predicateFunc);

  /**
   * Add or replace an item from the {@code List} and observe the operation. This method replaces
   * the first item in the {@code List} for which the predicate function returns true. If no items
   * qualify then the item is appended to the end of the {@code List}.
   *
   * <p>The {@code List} returned by the {@link Single} is the modified {@code List} written to this
   * store, making this useful for chaining.
   */
  Single<List<T>> observeAddOrReplace(final T value, final PredicateFunc<T> predicateFunc);

  /**
   * Asynchronously add or replace an item from the {@code List}. This method replaces the first
   * item in the {@code List} for which the predicate function returns true. If no items qualify
   * then the item is appended to the end of the {@code List}.
   *
   * <p>This operation occurs on {@link Schedulers#io()}. If you wish to specify the {@link
   * Scheduler} then use {@link #addOrReplace(Object, Scheduler, PredicateFunc)}.
   */
  void addOrReplace(T value, PredicateFunc<T> predicateFunc);

  /**
   * Add or replace an item from the {@code List} on the specified {@link Scheduler}. This method
   * replaces the first item in the {@code List} for which the predicate function returns true. If
   * no items qualify then the item is appended to the end of the {@code List}.
   */
  void addOrReplace(T value, Scheduler scheduler, PredicateFunc<T> predicateFunc);

  /** A callback to determine if a particular value qualifies for an operation. */
  interface PredicateFunc<T> {
    /** Determine if a value qualifies for a particular operation. */
    boolean test(T value);
  }
}
