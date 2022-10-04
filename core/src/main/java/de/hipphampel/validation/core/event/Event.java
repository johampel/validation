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

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An event sent by an {@link EventPublisher}.
 * <p>
 * This is basically an en envelope around the event payload provided by some event sender
 *
 * @param payload     The event payload
 * @param publishTime The publish time of the event
 * @param source      The object being the originator of this event
 * @param <T>         The payload type
 */
public record Event<T>(T payload, LocalDateTime publishTime, Object source) {

  /**
   * Constructor
   *
   * @param payload     The event payload
   * @param publishTime The publish time of the event
   * @param source      The object being the originator of this event
   */
  public Event {
    Objects.requireNonNull(payload);
    Objects.requireNonNull(publishTime);
  }
}
