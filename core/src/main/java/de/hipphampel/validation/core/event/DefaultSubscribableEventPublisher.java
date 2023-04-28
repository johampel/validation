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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the {@link SubscribableEventPublisher}{@link EventPublisher}.
 * <p>
 * This implementation informs {@linkplain #subscribe(EventListener) subscribed} listeners about any
 * event that is {@linkplain #publish(Object, Object) published}.
 *
 * @see SubscribableEventPublisher
 */
public class DefaultSubscribableEventPublisher implements SubscribableEventPublisher {

  private final Set<SubscriptionImpl> subscriptions = ConcurrentHashMap.newKeySet();

  @Override
  public <T> void publish(Object source, T payload) {
    Event<T> event = new Event<>(payload, LocalDateTime.now(), source);
    subscriptions.forEach(subscription -> subscription.eventListener.accept(event));
  }

  @Override
  public Subscription subscribe(EventListener listener) {
    SubscriptionImpl subscription = new SubscriptionImpl(this, Objects.requireNonNull(listener));
    listener.subscribed(subscription);
    subscriptions.add(subscription);
    return subscription;
  }

  private record SubscriptionImpl(DefaultSubscribableEventPublisher publisher, EventListener eventListener) implements Subscription {

    @Override
    public void unsubscribe() {
      if (publisher.subscriptions.remove(this)) {
        eventListener.unsubscribed(this);
      }
    }
  }
}
