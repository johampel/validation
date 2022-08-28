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
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.provider.SimpleRuleSelector;
import de.hipphampel.validation.core.rule.DispatchingRule.DispatchEntry;
import de.hipphampel.validation.core.utils.TypeInfo;
import de.hipphampel.validation.core.utils.TypeReference;
import de.hipphampel.validation.core.value.Value;
import de.hipphampel.validation.core.value.Values;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Allows to build {@link Rule Rules}.
 * <p>
 * This is just an abstract class that acts a base for concrete builders. The concrete builders in
 * turn are constructed via according static factory methods of this class. For example
 * {@link #conditionRule(String, Class) conditionRule} creates a {@link ConditionRuleBuilder} to
 * build a {@link ConditionRule}
 *
 * @param <T> The type of the object begin validated
 * @param <B> The type pf tje builder.
 */
public abstract class RuleBuilder<T, B extends RuleBuilder<T, B>> {

  protected final String id;
  protected final Class<? super T> factsType;
  protected final Map<String, Object> metadata;
  protected final List<Condition> preconditions;

  /**
   * Constructor.
   *
   * @param id        The id of the {@link Rule} to create
   * @param factsType The {@link Class} of the type being validated by the {@code Rule}
   */
  protected RuleBuilder(String id, Class<? super T> factsType) {
    this.id = Objects.requireNonNull(id);
    this.factsType = factsType;
    this.metadata = new HashMap<>();
    this.preconditions = new ArrayList<>();
  }

  /**
   * Creates a new {@link ConditionRuleBuilder}.
   *
   * @param id       Id of the {@link ConditionRule} to build
   * @param factsType The type of the object being validated
   * @param <T>      The type of the object being validated
   * @return The {@link ConditionRuleBuilder}
   */
  @SuppressWarnings("unchecked")
  public static <T> ConditionRuleBuilder<T> conditionRule(String id, TypeReference<T> factsType) {
    return new ConditionRuleBuilder<>(id, (Class<T>) TypeInfo.forTypeReference(factsType).getRawClass());
  }

  /**
   * Creates a new {@link ConditionRuleBuilder}.
   *
   * @param id        Id of the {@link ConditionRule} to build
   * @param factsType The type of the object being validated
   * @param <T>       The type of the object being validated
   * @return The {@link ConditionRuleBuilder}
   */
  public static <T> ConditionRuleBuilder<T> conditionRule(String id, Class<? super T> factsType) {
    return new ConditionRuleBuilder<>(id, factsType);
  }

  /**
   * Creates a new {@link FunctionRuleBuilder}.
   *
   * @param id        Id of the {@link FunctionRule} to build
   * @param factsType The type of the object being validated
   * @param <T>       The type of the object being validated
   * @return The {@link FunctionRuleBuilder}
   */
  @SuppressWarnings("unchecked")
  public static <T> FunctionRuleBuilder<T> functionRule(String id, TypeReference<T> factsType) {
    return new FunctionRuleBuilder<>(id,
        (Class<T>) TypeInfo.forTypeReference(factsType).resolve());
  }

  /**
   * Creates a new {@link FunctionRuleBuilder}.
   *
   * @param id        Id of the {@link FunctionRule} to build
   * @param factsType The type of the object being validated
   * @param <T>       The type of the object being validated
   * @return The {@link FunctionRuleBuilder}
   */
  public static <T> FunctionRuleBuilder<T> functionRule(String id, Class<? super T> factsType) {
    return new FunctionRuleBuilder<>(id, factsType);
  }

  /**
   * Creates a new {@link DispatchingRuleBuilder}.
   *
   * @param id        Id of the {@link DispatchingRule} to build
   * @param factsType The type of the object being validated
   * @param <T>       The type of the object being validated
   * @return The {@link DispatchingRuleBuilder}
   */
  @SuppressWarnings("unchecked")
  public static <T> DispatchingRuleBuilder<T> dispatchingRule(String id,
      TypeReference<T> factsType) {
    return new DispatchingRuleBuilder<>(id,
        (Class<T>) TypeInfo.forTypeReference(factsType).resolve());
  }

  /**
   * Creates a new {@link DispatchingRuleBuilder}.
   *
   * @param id        Id of the {@link DispatchingRule} to build
   * @param factsType The type of the object being validated
   * @param <T>       The type of the object being validated
   * @return The {@link DispatchingRuleBuilder}
   */
  public static <T> DispatchingRuleBuilder<T> dispatchingRule(String id,
      Class<? super T> factsType) {
    return new DispatchingRuleBuilder<>(id, factsType);
  }

  /**
   * Adds the given {@code key} - {@code value} pair to the {@link Rule#getMetadata() metadata} of
   * the {@link Rule} to built.
   *
   * @param key   The key of the metdata
   * @param value The value of the metadata
   * @return This builder
   */
  public B withMetadata(String key, Object value) {
    this.metadata.put(key, value);
    return self();
  }

  /**
   * Adds the given {@code metadata} to the
   * {@link Rule#getMetadata() metadata of the {@link Rule} to built.
   *
   * @param metadata The addditional metadata
   * @return This builder
   */
  public B withMetadata(Map<String, Object> metadata) {
    this.metadata.putAll(metadata);
    return self();
  }

  /**
   * Adds {@code condition} to the {@link Rule#getPreconditions() preconditions} of the {@link Rule}
   * to built.
   *
   * @param condition The {@link Condition}
   * @return This builder
   */
  public B withPrecondition(Condition condition) {
    this.preconditions.add(Objects.requireNonNull(condition));
    return self();
  }

  /**
   * Adds {@code conditions} to the {@link Rule#getPreconditions() preconditions} of the
   * {@link Rule} to built.
   *
   * @param conditions The {@link Condition Conditions}
   * @return This builder
   */
  public B withPreconditions(Collection<? extends Condition> conditions) {
    this.preconditions.addAll(conditions);
    return self();
  }

  /**
   * Adds {@code conditions} to the {@link Rule#getPreconditions() preconditions} of the
   * {@link Rule} to built.
   *
   * @param conditions The {@link Condition Conditions}
   * @return This builder
   */
  public B withPreconditions(Condition... conditions) {
    this.preconditions.addAll(Arrays.asList(conditions));
    return self();
  }

  /**
   * Returns this instance casted to the builder type
   *
   * @return This instance
   */
  @SuppressWarnings("unchecked")
  protected B self() {
    return (B) this;
  }

  /**
   * {@link RuleBuilder} for a {@link ConditionRule}.
   *
   * @param <T> Type of the object being validated
   */
  public static class ConditionRuleBuilder<T> extends RuleBuilder<T, ConditionRuleBuilder<T>> {

    private Condition condition;
    private Value<ResultReason> failReason;


    /**
     * Constructor.
     *
     * @param id        Id of the rule to be built
     * @param factsType {@link Class} of the object being validated
     */
    protected ConditionRuleBuilder(String id, Class<? super T> factsType) {
      super(id, factsType);
    }

    /**
     * Specifies the {@link Condition} used for validation.
     *
     * @param condition The {@code Condition}
     * @return This builder.
     */
    public ConditionRuleBuilder<T> validateWith(Condition condition) {
      this.condition = Objects.requireNonNull(condition);
      return self();
    }

    /**
     * Specifies the fail {@link ResultReason} to return, if the rule fails.
     *
     * @param failReason The Fail reason
     * @return This builder
     */
    public ConditionRuleBuilder<T> withFailReason(Value<ResultReason> failReason) {
      this.failReason = failReason;
      return self();
    }

    /**
     * Specifies the fail {@link ResultReason} to return, if the rule fails.
     *
     * @param failReason The Fail reason
     * @return This builder
     */
    public ConditionRuleBuilder<T> withFailReason(ResultReason failReason) {
      return withFailReason(failReason == null ? null : Values.val(failReason));
    }

    /**
     * Specifies the fail message to return, if the rule fails.
     *
     * @param failMessage The Fail message
     * @return This builder
     */
    public ConditionRuleBuilder<T> withFailReason(String failMessage) {
      return withFailReason(
          failMessage == null ? null : Values.val(new StringResultReason(failMessage))
      );
    }

    /**
     * Builds the final {@link ConditionRule}
     *
     * @return The {@code ConditionRule}
     */
    public ConditionRule<T> build() {
      return new ConditionRule<>(
          id,
          factsType,
          Collections.unmodifiableMap(metadata),
          Collections.unmodifiableList(preconditions),
          Objects.requireNonNull(condition),
          failReason);
    }
  }

  /**
   * {@link RuleBuilder} for a {@link FunctionRule}.
   *
   * @param <T> Type of the object being validated
   */
  public static class FunctionRuleBuilder<T> extends RuleBuilder<T, FunctionRuleBuilder<T>> {

    private BiFunction<ValidationContext, T, Result> function;

    /**
     * Constructor.
     *
     * @param id        Id of the rule to be built
     * @param factsType {@link Class} of the object being validated
     */
    protected FunctionRuleBuilder(String id, Class<? super T> factsType) {
      super(id, factsType);
    }

    /**
     * Specifies the {@link BiFunction} used for validation.
     *
     * @param function The {@code BiFunction}
     * @return This builder.
     */
    public FunctionRuleBuilder<T> validateWith(BiFunction<ValidationContext, T, Result> function) {
      this.function = Objects.requireNonNull(function);
      return self();
    }

    /**
     * Builds the final {@link FunctionRule}
     *
     * @return The {@code FunctionRule}
     */
    public FunctionRule<T> build() {
      return new FunctionRule<>(
          id,
          factsType,
          Collections.unmodifiableMap(metadata),
          Collections.unmodifiableList(preconditions),
          Objects.requireNonNull(function));
    }
  }

  /**
   * {@link RuleBuilder} for a {@link DispatchingRule}.
   *
   * @param <T> Type of the object being validated
   */
  public static class DispatchingRuleBuilder<T> extends RuleBuilder<T, DispatchingRuleBuilder<T>> {

    private final List<DispatchingRule.DispatchEntry> dispatchList;

    /**
     * Constructor.
     *
     * @param id        Id of the rule to be built
     * @param factsType {@link Class} of the object being validated
     */
    protected DispatchingRuleBuilder(String id, Class<? super T> factsType) {
      super(id, factsType);
      this.dispatchList = new ArrayList<>();
    }

    /**
     * Starts the production of a {@link DispatchEntry}
     *
     * @param paths The {@link Path Paths} of the entry
     * @return A {@link DispatchEntryBuilder}
     */
    public DispatchEntryBuilder forPaths(Value<Set<String>> paths) {
      return new DispatchEntryBuilder(paths);
    }

    /**
     * Starts the production of a {@link DispatchEntry}
     *
     * @param paths The {@link Path Paths} of the entry
     * @return A {@link DispatchEntryBuilder}
     */
    public DispatchEntryBuilder forPaths(Set<String> paths) {
      return new DispatchEntryBuilder(Values.val(paths));
    }

    /**
     * Starts the production of a {@link DispatchEntry}
     *
     * @param paths The {@link Path Paths} of the entry
     * @return A {@link DispatchEntryBuilder}
     */
    public DispatchEntryBuilder forPaths(String... paths) {
      return new DispatchEntryBuilder(Values.val(new HashSet<>(Arrays.asList(paths))));
    }

    private DispatchingRuleBuilder<T> addEntry(DispatchEntry entry) {
      dispatchList.add(entry);
      return this;
    }

    /**
     * Builds the final {@link DispatchingRule}
     *
     * @return The {@code ConditionRule}
     */
    public DispatchingRule<T> build() {
      return new DispatchingRule<>(
          id,
          factsType,
          Collections.unmodifiableMap(metadata),
          Collections.unmodifiableList(preconditions),
          Collections.unmodifiableList(dispatchList));
    }

    /**
     * Builder for a single {@link DispatchEntry}
     * <p>
     * This is a sub builder of the {@link DispatchingRuleBuilder}
     *
     * @see DispatchingRuleBuilder
     * @see DispatchEntry
     */
    public class DispatchEntryBuilder {

      private final Value<Set<String>> paths;

      /**
       * Constructor.
       *
       * @param paths The {@link Path Paths} the {@link DispatchEntry} has.
       */
      public DispatchEntryBuilder(Value<Set<String>> paths) {
        this.paths = Objects.requireNonNull(paths);
      }

      /**
       * Finalizes the production of a {@link DispatchEntry} with the given {@code rules} and adds
       * it to the {@link DispatchingRuleBuilder}
       *
       * @param rules The {@link Rule} ids
       * @return The {@code DispatchingRuleBuilder}
       */
      public DispatchingRuleBuilder<T> validateWith(Value<Set<String>> rules) {
        return validateWith(SimpleRuleSelector.of(rules));
      }

      /**
       * Finalizes the production of a {@link DispatchEntry} with the given {@code rules} and adds
       * it to the {@link DispatchingRuleBuilder}
       *
       * @param rules The {@link Rule} ids
       * @return The {@code DispatchingRuleBuilder}
       */
      public DispatchingRuleBuilder<T> validateWith(Collection<String> rules) {
        return validateWith(RuleSelector.of(rules));
      }

      /**
       * Finalizes the production of a {@link DispatchEntry} with the given {@code rules} and adds
       * it to the {@link DispatchingRuleBuilder}
       *
       * @param rules The {@link Rule} ids
       * @return The {@code DispatchingRuleBuilder}
       */
      public DispatchingRuleBuilder<T> validateWith(String... rules) {
        return validateWith(RuleSelector.of(rules));
      }

      /**
       * Finalizes the production of a {@link DispatchEntry} with the given {@code rules} and adds
       * it to the {@link DispatchingRuleBuilder}
       *
       * @param rules The {@link RuleSelector}
       * @return The {@code DispatchingRuleBuilder}
       */
      public DispatchingRuleBuilder<T> validateWith(RuleSelector rules) {
        return DispatchingRuleBuilder.this.addEntry(new DispatchEntry(paths, rules));
      }
    }
  }
}
