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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SubscribableEventPublisherTest {

  @Test
  void subscribeAndUnsubscribe() {
    SubscribableEventPublisher publisher = new SubscribableEventPublisher();
    List<Event<?>> events1 = new ArrayList<>();
    List<Event<?>> events2 = new ArrayList<>();

    Subscription subscription1 = publisher.subscribe(events1::add);
    Subscription subscription2 = publisher.subscribe(events2::add);

    LocalDateTime send1 = LocalDateTime.now();
    publisher.publish(this, "One");
    assertThat(events1).hasSize(1);
    assertThat(events1).isEqualTo(events2);
    assertThat(events1.get(0).payload()).isEqualTo("One");
    assertThat(events1.get(0).publishTime()).isAfterOrEqualTo(send1);
    assertThat(events1.get(0).publishTime()).isBeforeOrEqualTo(LocalDateTime.now());
    assertThat(events1.get(0).source()).isSameAs(this);

    subscription2.unsubscribe();
    LocalDateTime send2 = LocalDateTime.now();
    publisher.publish(this, "Two");
    assertThat(events2).isEqualTo(List.of(events1.get(0)));
    assertThat(events1).hasSize(2);
    assertThat(events1.get(1).payload()).isEqualTo("Two");
    assertThat(events1.get(1).publishTime()).isAfterOrEqualTo(send2);
    assertThat(events1.get(1).publishTime()).isBeforeOrEqualTo(LocalDateTime.now());
    assertThat(events1.get(1).source()).isSameAs(this);

    subscription1.unsubscribe();
    LocalDateTime send3 = LocalDateTime.now();
    publisher.publish(this, "Three");
    assertThat(events2).hasSize(1);
    assertThat(events1).hasSize(2);

  }
}
