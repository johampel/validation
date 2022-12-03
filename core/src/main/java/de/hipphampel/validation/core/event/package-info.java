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

/**
 * Event related components.
 * <p>
 * During validation, a {@link de.hipphampel.validation.core.event.EventPublisher} is used to publish events to the client. Such events are
 * sent, when the execution of a {@link de.hipphampel.validation.core.rule.Rule Rule} starts or ends, but an application/{@code Rule}
 * implementation is also allowed to send its own events.
 * <p>
 * While the {@code EventPublisher} is for sending, the {@link de.hipphampel.validation.core.event.EventSubscriber} is the part that is
 * intended to add {@link de.hipphampel.validation.core.event.EventListener} to be called when an event occurs. Conceptually,
 * {@code EventPublishers} and {@code EvenSubscriber} might be different objects, but with the
 * {@link de.hipphampel.validation.core.event.DefaultSubscribableEventPublisher} there is also one implementation that unifies both.
 * <p>
 * {@code EventPublisher} and {@code EventSubscriber} are more infrastructural objects that should be provided by the
 * {@link de.hipphampel.validation.core.Validator Validator}.
 *
 * @see de.hipphampel.validation.core.event.EventSubscriber
 * @see de.hipphampel.validation.core.event.EventPublisher
 * @see de.hipphampel.validation.core.event.Event
 */
package de.hipphampel.validation.core.event;
