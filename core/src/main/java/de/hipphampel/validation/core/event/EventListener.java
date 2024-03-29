/*
 * The MIT License
 * Copyright © 2022 Johannes Hampel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.hipphampel.validation.core.event;

import java.util.function.Consumer;

/**
 * A listener for {@link Event Events}.
 *
 * Can be used to subscribe for events at the {@link EventSubscriber}
 *
 * @see Event
 * @see EventSubscriber
 */
@FunctionalInterface
public interface EventListener extends Consumer<Event<?>> {

  /**
   * Called by the {@link EventSubscriber} when this instance has been {@link EventSubscriber#subscribe(EventListener) subscribed}.
   *
   * @param subscription The {@link Subscription}
   */
  default void subscribed(Subscription subscription) {
  }

  /**
   * Called by the {@link EventSubscriber} when {@link Subscription#unsubscribe() unsubscribe} has been called.
   * @param subscription The {@link Subscription}
   */
  default void unsubscribed(Subscription subscription) {
  }
}
