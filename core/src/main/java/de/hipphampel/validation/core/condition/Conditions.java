package de.hipphampel.validation.core.condition;

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

import de.hipphampel.validation.core.condition.CompareCondition.Mode;
import de.hipphampel.validation.core.rule.ResultCode;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.utils.StreamProvider;
import de.hipphampel.validation.core.value.Value;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Collection of factory methods for {@link Condition Conditions}
 */
public interface Conditions {

  /**
   * Factory method for a condition that always evaluates to {@code true}.
   *
   * @return The condition
   */
  static Condition alwaysTrue() {
    return always(true);
  }

  /**
   * Factory method for a condition that always evaluates to {@code false}.
   *
   * @return The condition
   */
  static Condition alwaysFalse() {
    return always(false);
  }

  /**
   * Returns a {@link Condition} that always returns the given value
   *
   * @param value The result
   * @return the {@code Condition}
   */
  static Condition always(boolean value) {
    return new AlwaysCondition(value);
  }

  /**
   * Creates a {@link Condition}, if all the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code AND} of the given conditions.
   */
  static Condition and(Condition... conditions) {
    return and(StreamProvider.of(Arrays.asList(conditions)));
  }

  /**
   * Creates a {@link Condition}, if all the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code AND} of the given conditions.
   */
  static Condition and(Collection<Condition> conditions) {
    Objects.requireNonNull(conditions);
    return and(StreamProvider.of(conditions));
  }

  /**
   * Creates a {@link Condition}, if all the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code AND} of the given conditions.
   */
  static Condition and(StreamProvider<Condition> conditions) {
    return new AndCondition(conditions);
  }

  /**
   * Creates a {@link Condition}, if at none of the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code NOT} of the given conditions.
   */
  static Condition not(Condition... conditions) {
    return not(StreamProvider.of(Arrays.asList(conditions)));
  }

  /**
   * Creates a {@link Condition}, if at none of the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code NOT} of the given conditions.
   */
  static Condition not(Collection<Condition> conditions) {
    Objects.requireNonNull(conditions);
    return not(StreamProvider.of(conditions));
  }

  /**
   * Creates a {@link Condition}, if at none of the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code NOT} of the given conditions.
   */
  static Condition not(StreamProvider<Condition> conditions) {
    return new NotCondition(conditions);
  }

  /**
   * Creates a {@link Condition}, if at least one of the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code OR} of the given conditions.
   */
  static Condition or(Condition... conditions) {
    return or(StreamProvider.of(Arrays.asList(conditions)));
  }

  /**
   * Creates a {@link Condition}, if at least one of the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code OR} of the given conditions.
   */
  static Condition or(Collection<Condition> conditions) {
    Objects.requireNonNull(conditions);
    return or(StreamProvider.of(conditions));
  }

  /**
   * Creates a {@link Condition}, if at least one of the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code OR} of the given conditions.
   */
  static Condition or(StreamProvider<Condition> conditions) {
    return new OrCondition(conditions);
  }

  /**
   * Creates a {@link Condition}, if exactly one of the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code XOR} of the given conditions.
   */
  static Condition xor(Condition... conditions) {
    return xor(StreamProvider.of(Arrays.asList(conditions)));
  }

  /**
   * Creates a {@link Condition}, if exactly one of the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code XOR} of the given conditions.
   */
  static Condition xor(Collection<Condition> conditions) {
    Objects.requireNonNull(conditions);
    return xor(StreamProvider.of(conditions));
  }

  /**
   * Creates a {@link Condition}, if exactly one of the given {@code conditions} are {@code true}
   *
   * @param conditions Sub conditions
   * @return The logical {@code XOR} of the given conditions.
   */
  static Condition xor(StreamProvider<Condition> conditions) {
    return new XorCondition(conditions);
  }

  /**
   * Creates a {@link Condition} that checks, if {@code arg} is {@code null}
   *
   * @param arg The {@link Value} to check
   * @param <T> Type of the {@code arg}
   * @return A {@code Condition} that becomes {@code true}, if the argument evaluates to
   * {@code null}
   */
  static <T> Condition isNull(Value<T> arg) {
    return new IsNullCondition<>(arg, IsNullCondition.Mode.NULL);
  }

  /**
   * Creates a {@link Condition} that checks, if {@code arg} is not {@code null}
   *
   * @param arg The {@link Value} to check
   * @param <T> Type of the {@code arg}
   * @return A {@code Condition} that becomes {@code true}, if the argument not evaluates to
   * {@code null}
   */
  static <T> Condition isNotNull(Value<T> arg) {
    return new IsNullCondition<>(arg, IsNullCondition.Mode.NOT_NULL);
  }

  /**
   * Creates a {@link Condition} that checks, if {@code left} is  equal to {@code right}
   *
   * @param left  The left  side {@link Value} to check
   * @param right The right  side {@link Value} to check
   * @param <T>   Type of the arguments
   * @return A {@code Condition} that becomes {@code true}, if the arguments  evaluate to equal
   * values
   */
  static <T> Condition eq(Value<T> left, Value<T> right) {
    return new EqualsCondition<>(left, right, EqualsCondition.Mode.EQ);
  }

  /**
   * Creates a {@link Condition} that checks, if {@code left} is different to {@code right}
   *
   * @param left  The left  side {@link Value} to check
   * @param right The right  side {@link Value} to check
   * @param <T>   Type of the arguments
   * @return A {@code Condition} that becomes {@code true}, if the arguments  evaluate to different
   * values
   */
  static <T> Condition ne(Value<T> left, Value<T> right) {
    return new EqualsCondition<>(left, right, EqualsCondition.Mode.NE);
  }

  /**
   * Creates a {@link Condition} that checks, if {@code left} is less or equal to {@code right}
   *
   * @param left  The left  side {@link Value} to check
   * @param right The right  side {@link Value} to check
   * @param <T>   Type of the arguments
   * @return A {@code Condition} that becomes {@code true}, if  {@code left} evaluates to a value
   * that is less or equal to the value evaluated by {@code right}
   */
  static <T extends Comparable<T>> Condition le(Value<T> left, Value<T> right) {
    return new CompareCondition<>(left, right, Mode.LE);
  }

  /**
   * Creates a {@link Condition} that checks, if {@code left} is less than {@code right}
   *
   * @param left  The left  side {@link Value} to check
   * @param right The right  side {@link Value} to check
   * @param <T>   Type of the arguments
   * @return A {@code Condition} that becomes {@code true}, if  {@code left} evaluates to a value
   * that is less than the value evaluated by {@code right}
   */
  static <T extends Comparable<T>> Condition lt(Value<T> left, Value<T> right) {
    return new CompareCondition<>(left, right, Mode.LT);
  }

  /**
   * Creates a {@link Condition} that checks, if {@code left} is greater than {@code right}
   *
   * @param left  The left  side {@link Value} to check
   * @param right The right  side {@link Value} to check
   * @param <T>   Type of the arguments
   * @return A {@code Condition} that becomes {@code true}, if  {@code left} evaluates to a value
   * that is greater than the value evaluated by {@code right}
   */
  static <T extends Comparable<T>> Condition gt(Value<T> left, Value<T> right) {
    return new CompareCondition<>(left, right, Mode.GT);
  }

  /**
   * Creates a {@link Condition} that checks, if {@code left} is greater or equal to {@code right}
   *
   * @param left  The left  side {@link Value} to check
   * @param right The right  side {@link Value} to check
   * @param <T>   Type of the arguments
   * @return A {@code Condition} that becomes {@code true}, if  {@code left} evaluates to a value
   * that is greater or equal to the value evaluated by {@code right}
   */
  static <T extends Comparable<T>> Condition ge(Value<T> left, Value<T> right) {
    return new CompareCondition<>(left, right, Mode.GE);
  }

  /**
   * Creates a {@link Condition} that executes the {@link Rule} with the given {@code ruleId}.
   * <p>
   * The condition will evaluate to {@code true}, if the condition evaluates to
   * {@link ResultCode#OK}
   *
   * @param ruleId The id of the {@code Rule}
   * @return The {@code Condition}
   */
  static Condition rule(Value<String> ruleId) {
    return new RuleCondition(ruleId);
  }

  /**
   * Creates a {@link Condition} that wraps around the given {@code predicate}
   *
   * @param predicate The {@link Predicate}
   * @return The {@code Condition}
   */
  static Condition predicate(Predicate<?> predicate) {
    return new PredicateCondition(predicate);
  }
}
