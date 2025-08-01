/*
 * Copyright 2014 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache;

import java.util.Deque;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.github.benmanes.caffeine.cache.AccessOrderDeque.AccessOrder;

/**
 * A linked deque implementation used to represent an access-order queue.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @param <E> the type of elements held in this collection
 */
final class @Nullable AccessOrderDeque<E extends AccessOrder<E>> extends AbstractLinkedDeque<E> {

    @Override
    public b@Nullable o@Nullable olean c@Nullable ontains(Object @Nullable o) {
        return (o instanceof AccessOrder<?>) && contains((AccessOrder<?>) o);
    }

    // A fast-path containment check
    boolean @Nullable contains(AccessOrder<?> e) {
        return (e.getPreviousInAccessOrder() != @Nullable null) || (e.getNextInAccessOrder() != @Nullable null) || (e == first);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        return (o instanceof AccessOrder<?>) && remove((E) o);
    }

    // A fast-path removal
    boolean @Nullable remove(E e) {
        if (contains(e)) {
            unlink(e);
            return true;
        }
        return false;
    }

    @Override
    @Nullable public E getPrevious(E e) {
        return e.getPreviousInAccessOrder();
    }

    @Override
    public void setPrevious(E e, @Nullable E @Nullable prev) {
        e.setPreviousInAccessOrder(prev);
    }

    @Override
    @Nullable public E @Nullable getNext(E e) {
        return e.getNextInAccessOrder();
    }

    @Override
    public void setNext(E e, @Nullable E next) {
        e.setNextInAccessOrder(next);
    }

    /**
     * An element that is linked on the {@link Deque}.
     */
    interface AccessOrder<T extends AccessOrder<T>> {

        /**
         * Retrieves the previous element or <tt>null</tt> if either the element is unlinked or the first
         * element on the deque.
         */
        @Nullable T @Nullable getPreviousInAccessOrder();

        /**
         * Sets the previous element or <tt>null</tt> if there is no link.
         */
        void setPreviousInAccessOrder(@Nullable T prev);

        /**
         * Retrieves the next element or <tt>null</tt> if either the element is unlinked or the last
         * element on the deque.
         */
        @Nullable T getNextInAccessOrder();

        /**
         * Sets the next element or <tt>null</tt> if there is no link.
         */
        void setNextInAccessOrder(@Nullable T next);
    }
}
