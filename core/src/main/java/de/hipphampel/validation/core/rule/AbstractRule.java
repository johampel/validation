package de.hipphampel.validation.core.rule;

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

import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.execution.ValidationContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract base implementation of the {@link Rule} interface.
 * <p>
 * Concrete implementations have to implement the
 * {@link #validate(ValidationContext, Object) valiate} method, all other methods are implemented
 * using the values specified at construction time.
 *
 * @param <T> Type of the object being validated
 */
public abstract class AbstractRule<T> implements Rule<T> {

  private final String id;
  private final Class<? super T> factsType;
  private final Map<String, Object> metadata;
  private final List<Condition> preconditions;

  /**
   * Creates an instance using the given parameters.
   *
   * @param id            The id of the {@link Rule}
   * @param factsType     {@link Class} of the objects being validated by this rule
   * @param metadata      The metadata
   * @param preconditions The list of {@linkplain Condition preconditions}
   */
  protected AbstractRule(String id, Class<? super T> factsType, Map<String, Object> metadata,
      List<Condition> preconditions) {
    this.id = Objects.requireNonNull(id);
    this.factsType = factsType == null ? RuleUtils.determineRuleFactsType(this) : factsType;
    this.metadata = Collections.unmodifiableMap(Objects.requireNonNull(metadata));
    this.preconditions = Collections.unmodifiableList(Objects.requireNonNull(preconditions));
  }

  /**
   * Creates an instance using the given parameters.
   *
   * @param id            The id of the {@link Rule}
   * @param metadata      The metadata
   * @param preconditions The list of {@linkplain Condition preconditions}
   */
  protected AbstractRule(String id, Map<String, Object> metadata, List<Condition> preconditions) {
    this(id, null, metadata, preconditions);
  }

  /**
   * Creates an instance using the given parameters.
   *
   * @param id The id of the {@link Rule}
   */
  protected AbstractRule(String id) {
    this(id, null, Map.of(), List.of());
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Class<? super T> getFactsType() {
    return this.factsType;
  }

  @Override
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @Override
  public List<Condition> getPreconditions() {
    return preconditions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractRule<?> that = (AbstractRule<?>) o;
    return Objects.equals(id, that.id) && Objects.equals(metadata, that.metadata) && Objects.equals(
        preconditions, that.preconditions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, metadata, preconditions);
  }

  @Override
  public String toString() {
    return "Rule{" + "id='" + id + "'}";
  }
}
