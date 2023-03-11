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

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link EventListener} wrapper that automatically unsubscribes the wrapped listener when it is garbage collected.
 *
 * It is essential, that the wrapped {@link EventListener} is weakly reachable in order to receive events. Once it is not,
 * it is automatically unsubscribed.
 *
 * @see EventListener
 */
public class WeakEventListener implements EventListener {

  private final WeakReference<EventListener> delegate;
  private final Set<Subscription> subscriptions;

  /**
   * Constructor.
   *
   * @param delegate The wrapped listener
   */
  public WeakEventListener(EventListener delegate) {
    this.delegate = new WeakReference<>(Objects.requireNonNull(delegate));
    this.subscriptions = ConcurrentHashMap.newKeySet();
  }

  @Override
  public void accept(Event<?> event) {
    EventListener realListener = realListener();
    if (realListener != null) {
      realListener.accept(event);
    }
  }

  @Override
  public void subscribed(Subscription subscription) {
    EventListener realListener = realListener();
    if (realListener != null) {
      realListener.subscribed(subscription);
    }
    this.subscriptions.add(subscription);
  }

  @Override
  public void unsubscribed(Subscription subscription) {
    EventListener realListener = realListener();
    if (realListener != null) {
      realListener.unsubscribed(subscription);
    }
    this.subscriptions.remove(subscription);
  }

  EventListener realListener() {
    EventListener realListener = delegate.get();
    if (realListener == null) {
      subscriptions.forEach(Subscription::unsubscribe);
    }
    return realListener;
  }
}
