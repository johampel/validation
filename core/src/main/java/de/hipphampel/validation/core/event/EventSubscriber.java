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

/**
 * Subscribes to {@link Event Events}.
 * <p>
 * An {@code EventSubscriber} is intended to subscribe to {@code Events} that might be relevant for
 * the application. The counterpart of this interface is the {@link EventPublisher} that allows to
 * publish the {@code Events}.
 *
 * @see EventPublisher
 * @see EventListener
 */
public interface EventSubscriber {

  /**
   * Subscribes to {@link Event Events}.
   * <p>
   * This ensures that the {@code listener} is invoked each time a event occurs. The result of this
   * method is a {@link Subscription} that can be used to close the the subscription afterwards.
   *
   * @param listener The event listener
   * @return The {@code Subscription}
   * @see Subscription
   */
  Subscription subscribe(EventListener listener);
}
