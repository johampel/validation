package de.hipphampel.validation.core.event;

/*-
 * #%L
 * validation-core
 * %%
 * Copyright (C) 2022 Johannes Hampel
 * %%
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
 * #L%
 */

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link EventPublisher} that also implements {@link EventSubscriber}.
 * <p>
 * This implementation informs {@linkplain #subscribe(EventListener) subscribed} listeners about any
 * event that takes place
 *
 * @see EventSubscriber
 * @see EventPublisher
 */
public class SubscribableEventPublisher implements EventPublisher, EventSubscriber {

  private final Map<Subscription, EventListener> subscriptions = new ConcurrentHashMap<>();

  @Override
  public <T> Event<T> publish(Object source, T payload) {
    Event<T> event = new Event<>(payload, LocalDateTime.now(), source);
    subscriptions.values().forEach(c -> c.accept(event));
    return event;
  }

  @Override
  public Subscription subscribe(EventListener listener) {
    Subscription subscription = new SubscriptionImpl();
    subscriptions.put(subscription, Objects.requireNonNull(listener));
    return subscription;
  }

  private class SubscriptionImpl implements Subscription {

    private final UUID id = UUID.randomUUID();

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SubscriptionImpl that = (SubscriptionImpl) o;
      return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
    }

    @Override
    public void unsubscribe() {
      SubscribableEventPublisher.this.subscriptions.remove(this);
    }
  }
}
