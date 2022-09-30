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
package de.hipphampel.validation.core;

import de.hipphampel.validation.core.event.DefaultSubscribableEventPublisher;
import de.hipphampel.validation.core.event.Event;
import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.event.Subscription;
import de.hipphampel.validation.core.event.payloads.RuleFinishedPayload;
import de.hipphampel.validation.core.execution.ValidationContext;
import java.time.LocalDateTime;
import java.util.List;

public class TestUtils {

  public static final LocalDateTime FIXED_DATE = LocalDateTime.now();
  public static final long FIXED_DURATION = 123L;

  public static Subscription collectEventsInto(ValidationContext context, List<Event<?>> sink) {
    SubscribableEventPublisher eventPublisher = context.getSharedObject(
        DefaultSubscribableEventPublisher.class);
    return eventPublisher.subscribe(e -> sink.add(fixify(e)));
  }

  private static Event<?> fixify(Event<?> evt) {
    Object payload = evt.payload();
    if (payload instanceof RuleFinishedPayload rfp) {
      payload = new RuleFinishedPayload(rfp.rule(), rfp.pathStack(), rfp.facts(), rfp.result(),
          FIXED_DURATION);
    }
    return new Event<>(payload, FIXED_DATE, evt.source());
  }

}
