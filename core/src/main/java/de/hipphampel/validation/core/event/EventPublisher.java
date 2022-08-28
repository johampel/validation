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

/**
 * Publishes {@link Event Events}.
 * <p>
 * An {@code EventPublisher} is intended to publish {@code Events} that might be relevant for
 * the application. The counterpart of this interface is the {@link EventSubscriber} that allows
 * to add listeners for the {@code Events}.
 *
 * @see EventListener
 * @see EventSubscriber
 */
public interface EventPublisher {

  /**
   * Publishs an event.
   *
   * @param source  The source of the event, might be {@code null}
   * @param payload The event payload, must not be {@code null}
   * @param <T>     The Payload type
   * @return The published event
   */
  <T> Event<T> publish(Object source, T payload);
}
