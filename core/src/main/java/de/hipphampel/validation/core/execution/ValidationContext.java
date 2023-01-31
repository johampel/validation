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
package de.hipphampel.validation.core.execution;

import de.hipphampel.validation.core.Validator;
import de.hipphampel.validation.core.event.DefaultSubscribableEventPublisher;
import de.hipphampel.validation.core.event.EventPublisher;
import de.hipphampel.validation.core.exception.ValidationException;
import de.hipphampel.validation.core.path.BeanPathResolver;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.path.Resolvable;
import de.hipphampel.validation.core.provider.InMemoryRuleRepository;
import de.hipphampel.validation.core.provider.RuleRepository;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.report.ReportReporter;
import de.hipphampel.validation.core.report.Reporter;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.utils.ObjectRegistry;
import de.hipphampel.validation.core.utils.Pair;
import de.hipphampel.validation.core.utils.Stacked;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Contextual information passed to a {@link Rule} when executing it.
 * <p>
 * First of all, the {@code ValidationContext} provides information about the {@code Rule} and object currently being validated. For this
 * purpose, it provides several callback methods that are called by the {@link RuleExecutor}. For example
 * {@link #enterRule(Rule, Object) enterRule} and {@link #leaveRule() leaveRule} are called by the {@code RuleExecutor}, when a {@code Rule}
 * is entered or left, it influences then the {@link #getRuleStack() ruleStack} and the {@link #getCurrentRule() currentRule}.
 * <p>
 * Secondly, the {@code ValidationContext} provides access to infrastructural/shared objects, like the {@link PathResolver},
 * {@link RuleExecutor}, {@link EventPublisher}, {@link Reporter}, and {@link RuleRepository}, or more in general to so called <i>shared
 * objects</i>, which can be registered at any time you want in the {@code ValidationContext}, but stay always the same.
 * <p>
 * Thirdly, the {@code Validator} might pass validation parameters to the validation. These parameters might be used to influence the
 * validation behaviour.
 * <p>
 * It is guaranteed that an instance of the {@code ValidationContext} is only accessed by one single thread at any point of time. But since
 * {@code Rules} might be executed asynchronously, there is a need to create a {@link #copy() copy} of the {@code ValidationContext} from
 * time to time. The contract is that the copied instance must return exactly the same objects for the infrastructural or shared objects,
 * for all other objects a deep copy is sufficient.
 * <p>
 * Normally, an <b>initial</b> {@code ValidationContext} is created by the {@link Validator} when calling a {@code validate*} method, so in
 * general, the lifetime of the initial {@code ValidationContext} is limited to the execution of all rules for a single object. During rule
 * execution, especially when it is done in an asynchronous way - the validation might create deep copies of the the initial context.
 *
 * @see RuleExecutor
 * @see Validator
 */
public class ValidationContext {

  private final ObjectRegistry sharedExtensions;
  private final ObjectRegistry localExtensions;
  private final Map<String, Object> parameters;
  private Stacked<Pair<Rule<?>, Object>> ruleStack;
  private Stacked<Resolvable> pathStack;
  private Object rootFacts;

  /**
   * Default constructor. Makes only sense for testing purposes.
   */
  public ValidationContext() {
    this(
        new ReportReporter(null),
        Map.of(),
        new DefaultRuleExecutor(),
        new InMemoryRuleRepository(),
        new BeanPathResolver(),
        new DefaultSubscribableEventPublisher());
  }

  /**
   * Constructor.
   *
   * @param reporter       The {@link Reporter}
   * @param ruleExecutor   The {@link RuleExecutor}
   * @param ruleRepository The {@link RuleRepository}
   * @param pathResolver   The {@link PathResolver}
   * @param eventPublisher The {@link EventPublisher}
   * @param parameters     Additional paramters passed to the validation
   */
  public ValidationContext(
      Reporter<?> reporter,
      Map<String, Object> parameters,
      RuleExecutor ruleExecutor,
      RuleRepository ruleRepository,
      PathResolver pathResolver,
      EventPublisher eventPublisher) {
    this.ruleStack = Stacked.empty();
    this.pathStack = Stacked.empty();
    this.parameters = Collections.unmodifiableMap(parameters);
    this.sharedExtensions = new ObjectRegistry();
    this.localExtensions = new ObjectRegistry();
    sharedExtensions.add(Objects.requireNonNull(reporter), Reporter.class);
    sharedExtensions.add(Objects.requireNonNull(eventPublisher), EventPublisher.class);
    sharedExtensions.add(Objects.requireNonNull(pathResolver), PathResolver.class);
    sharedExtensions.add(Objects.requireNonNull(ruleExecutor), RuleExecutor.class);
    sharedExtensions.add(Objects.requireNonNull(ruleRepository), RuleRepository.class);
  }

  private ValidationContext(ValidationContext source) {
    this.sharedExtensions = source.sharedExtensions;
    this.localExtensions = new ObjectRegistry();
    this.ruleStack = source.ruleStack;
    this.pathStack = source.pathStack;
    this.parameters = source.parameters;
    this.rootFacts = source.rootFacts;
  }

  /**
   * Creates a deep copy of the {@link ValidationContext}.
   * <p>
   * This method id called - for example - by the {@link RuleExecutor} when executing the rules asynchronously.
   *
   * @return The copy.
   * @see RuleSelector
   */
  public ValidationContext copy() {
    return new ValidationContext(this);
  }

  /**
   * Gets the stack of the {@link Rule Rules} being executed.
   * <p>
   * When a {@code Rule} is {@linkplain #enterRule(Rule, Object) entered}, an entry is pushed on to this stack, when it is
   * {@linkplain #leaveRule() left}, it is popped.
   * <p>
   * Each entry is a {@link Pair} of of {@code Rule} and the object being validated. There is a special check, when pushing entries on the
   * stack: it is not possible to push the same entry twice on to the stack in order to prevent recursive {@code Rule} execution.
   *
   * @return A {@link Stacked} with the top of the stack.
   * @see #enterRule(Rule, Object)
   * @see #leaveRule()
   * @see #getCurrentRule()
   */
  public Stacked<Pair<Rule<?>, Object>> getRuleStack() {
    return ruleStack;
  }

  /**
   * Called by the {@link RuleExecutor} just before a {@link Rule} is executed.
   * <p>
   * It basically tries to push an according entry on to the {@link #getRuleStack()}. If the same combination of parameters is already on
   * the stack, nothing happens
   *
   * @param rule  The {@link Rule}
   * @param facts The object being executed
   * @return {@code true}, when the {@code Rule} has been entered, {@code false} if not. In case the method returns {@code false} this is an
   * indicator, that a rule recursively calls itself on the same object, which is not allowed
   * @see RuleExecutor
   * @see #getRuleStack()
   * @see #leaveRule()
   * @see #getCurrentRule()
   */
  public boolean enterRule(Rule<?> rule, Object facts) {
    Pair<Rule<?>, Object> entry = new Pair<>(rule, facts);
    if (ruleStack.exists(v -> Objects.equals(v, entry))) {
      return false;
    }

    if (ruleStack.isEmpty()) {
      rootFacts = facts;
    }

    ruleStack = ruleStack.push(entry);
    return true;
  }

  /**
   * Called by the {@link RuleExecutor} just after a {@link Rule} is executed.
   * <p>
   * This is the counterpart to {@link #enterRule(Rule, Object) enterRule}. It is called by thw {@code RuleExecutor} directly after the rule
   * execution
   *
   * @see RuleExecutor
   * @see #enterRule(Rule, Object)
   * @see #getRuleStack()
   */
  public void leaveRule() {
    if (ruleStack.isEmpty()) {
      throw new ValidationException("No active rule");
    }

    ruleStack = ruleStack.pop();

    if (ruleStack.isEmpty()) {
      rootFacts = null;
    }
  }

  /**
   * Gets the currently active {@link Rule}.
   * <p>
   * Note that this might have a different value for different threads, since rules might be executed in an asynchronous fashion.
   *
   * @return The current {@code Rule} or {@code null}
   */
  public Rule<?> getCurrentRule() {
    return ruleStack.isEmpty() ? null : ruleStack.getValue().first();
  }

  /**
   * Gets the stack of the {@link Path Paths} and facts.
   * <p>
   * When a {@code Path} is {@linkplain #enterPath(Object, Path) entered} then an entry is pushed on to this stack, when it is
   * {@linkplain  #leavePath() left}, the entry is popped.
   * <p>
   * Entering and leaving is done by the {@link RuleExecutor} when a {@code validateForPath} method is called
   *
   * @return The stack
   * @see #enterPath(Object, Path)
   * @see #leavePath()
   */
  public Stacked<Resolvable> getPathStack() {
    return pathStack;
  }

  /**
   * Gets the current absolute {@link Path} used for validation.
   * <p>
   * If the validation is currently executed for a {@code Path}, this is returned, otherwise {@link PathResolver#selfPath() the self path}.
   * <p>
   * Note that this might have a different value for different threads, since rules might be executed in an asynchronous fashion.
   *
   * @return The  {@code Path}
   */
  public Path getCurrentPath() {
    return pathStack.isEmpty() ? getPathResolver().selfPath() : pathStack.getValue().path();
  }

  /**
   * Gets the root object being validated.
   * <p>
   * The root object is the object being originally validated, this might be different from the {@link #getCurrentFacts() currentFacts},
   * e.g. if the current rule execution has been triggered by another rule via
   * {@link RuleExecutor#validateForPath(ValidationContext, Rule, Object, Path) validateForPath}.
   *
   * @param type The requested type
   * @param <T>  The requested type
   * @return The root facts
   * @see #getCurrentFacts()
   * @see #getParentFacts()
   */
  public <T> T getRootFacts(Class<T> type) {
    return type.cast(getRootFacts());
  }

  /**
   * Gets the root object being validated.
   * <p>
   * The root object is the object being originally validated, this might be different from the {@link #getCurrentFacts() currentFacts},
   * e.g. if the current rule execution has been triggered by another rule via
   * {@link RuleExecutor#validateForPath(ValidationContext, Rule, Object, Path) validateForPath}. The root object is the objects originally
   * passed to the {@link Rule#validate(ValidationContext, Object) validate} method and stays constant during all further rule executions
   * triggered from within this {@code validate} call.
   *
   * @return The root facts
   * @see #getCurrentFacts()
   * @see #getParentFacts()
   */
  public Object getRootFacts() {
    return rootFacts;
  }

  /**
   * Gets the object currently being validated.
   * <p>
   * The object returned by this method is exactly the same that is passed as {@code facts} parameter to the current
   * {@link Rule#validate(ValidationContext, Object) validate} method
   *
   * @param type The requested type
   * @param <T>  The requested type
   * @return The current facts
   * @see #getRootFacts()
   * @see #getParentFacts()
   */
  public <T> T getCurrentFacts(Class<T> type) {
    return type.cast(getCurrentFacts());
  }

  /**
   * Gets the object currently being validated.
   * <p>
   * The object returned by this method is exactly the same that is passed as {@code facts} parameter to the current
   * {@link Rule#validate(ValidationContext, Object) validate} method
   *
   * @return The current facts
   * @see #getRootFacts()
   * @see #getParentFacts()
   */
  public Object getCurrentFacts() {
    return ruleStack.isEmpty() ? null : ruleStack.getValue().second();
  }

  /**
   * Gets the parent  object being validated.
   * <p>
   * The object returned by this method is the one that was the {@link #getCurrentFacts() current object}, when this
   * {@link Rule#validate(ValidationContext, Object) validation} has been triggered.
   *
   * @param type The requested type
   * @param <T>  The requested type
   * @return The parent facts
   * @see #getRootFacts()
   * @see #getCurrentFacts()
   */
  public <T> T getParentFacts(Class<T> type) {
    return type.cast(getParentFacts());
  }

  /**
   * Gets the parent  object being validated.
   * <p>
   * The object returned by this method is the one that was the {@link #getCurrentFacts() current object}, when this
   * {@link Rule#validate(ValidationContext, Object) validation} has been triggered.
   *
   * @return The parent facts
   * @see #getRootFacts()
   * @see #getCurrentFacts()
   */
  public Object getParentFacts() {
    return ruleStack.getParent() == null || ruleStack.getParent().isEmpty() ? null : ruleStack.getParent().getValue().second();
  }

  /**
   * Called by the {@link RuleExecutor} before an object if validated using a {@code validateForPath} method.
   * <p>
   * For example, when {@link RuleExecutor#validateForPath(ValidationContext, Rule, Object, Path)} is called, this method id called; after
   * the execution of the rule, the {@link #leavePath()} is called.
   *
   * @param parent The parent object being used, {@code path} is resolved on this object to calculate the object being validated
   * @param path   The {@link Path}
   * @see #enterPath(Object, Path)
   */
  public void enterPath(Object parent, Path path) {
    Path absolutePath = pathStack.isEmpty() ? path : pathStack.getValue().path().concat(path);
    pathStack = pathStack.push(new Resolvable(parent, absolutePath));
  }

  /**
   * Called by the {@link RuleExecutor} after an object is validated using {@code validateForPath} methods.
   *
   * @see #enterPath(Object, Path)
   */
  public void leavePath() {
    if (this.pathStack.isEmpty()) {
      throw new ValidationException("No active parent");
    }
    pathStack = pathStack.pop();
  }

  /**
   * Gets the context parameters.
   *
   * @return The parameters.
   */
  public Map<String, Object> getParameters() {
    return parameters;
  }

  /**
   * Gets the associated {@link Reporter}
   *
   * @return The {@code Reporter}
   */
  public Reporter<?> getReporter() {
    return getSharedExtension(Reporter.class);
  }

  /**
   * Gets the associated {@link RuleRepository}
   *
   * @return The {@code RuleRepository}
   */
  public RuleRepository getRuleProvider() {
    return getSharedExtension(RuleRepository.class);
  }

  /**
   * Gets the associated {@link RuleExecutor}
   *
   * @return The {@code RuleExecutor }
   */
  public RuleExecutor getRuleExecutor() {
    return getSharedExtension(RuleExecutor.class);
  }

  /**
   * Gets the associated {@link PathResolver}.
   *
   * @return The {@code PathResolver}
   */
  public PathResolver getPathResolver() {
    return getSharedExtension(PathResolver.class);
  }

  /**
   * Gets the associated {@link EventPublisher}.
   *
   * @return The {@code EventPublisher}.
   */
  public EventPublisher getEventPublisher() {
    return getSharedExtension(EventPublisher.class);
  }

  /**
   * Gets the shared extension of the given {@code type} from this context.
   * <p>
   * Shared extensions are shared between the different copies of this {@code ValidationContext}, meaning that if calling this function with
   * the same parameters on a context created by {@link #copy() copy}, it returns the same instance. This effectively means that the objects
   * are available during the complete validation of an object, not only for a single rule.
   *
   * @param type The type of the requested object
   * @param <T>  The type of the requested object
   * @return The object
   * @throws java.util.NoSuchElementException If there is nu such object
   * @see #getLocalExtension(Class)
   */
  public <T> T getSharedExtension(Class<T> type) {
    return sharedExtensions.get(type);
  }

  /**
   * Checks, whether this instance knows a shared extension of the given type. See {@link #getSharedExtension(Class)} for details.
   *
   * @param type The type of the requested object
   * @return {@code true}, if object is known
   */
  public boolean knowsSharedExtension(Class<?> type) {
    return sharedExtensions.knowsType(type);
  }

  /**
   * Gets the shared extension of the {@code type} or creates it using the {@code create} is not exists yet. See
   * {@link #getSharedExtension(Class)} for details.
   *
   * @param type    The type of the requested object
   * @param creator The function to create the object
   * @param <T>     The type of the requested object
   * @return The object
   */
  public <T> T getOrCreateSharedExtension(Class<T> type, Function<Class<T>, T> creator) {
    return sharedExtensions.getOrRegister(type, creator);
  }

  /**
   * Gets the local extension of the given {@code type} from this context.
   * <p>
   * Local extensions are only valid for this {@code ValidationContext} and not shared between different instances - so they are only
   * available during the validation of a single {@code Rule}.
   *
   * @param type The type of the requested object
   * @param <T>  The type of the requested object
   * @return The object
   * @throws java.util.NoSuchElementException If there is nu such object
   * @see #getSharedExtension(Class)
   */
  public <T> T getLocalExtension(Class<T> type) {
    return localExtensions.get(type);
  }

  /**
   * Checks, whether this instance knows a local extension of the given type. See {@link #getLocalExtension(Class)} for details.
   *
   * @param type The type of the requested object
   * @return {@code true}, if object is known
   */
  public boolean knowsLocalExtension(Class<?> type) {
    return localExtensions.knowsType(type);
  }

  /**
   * Gets the local extension of the {@code type} or creates it using the {@code create} is not exists yet. See
   * {@link #getLocalExtension(Class)} for details.
   *
   * @param type    The type of the requested object
   * @param creator The function to create the object
   * @param <T>     The type of the requested object
   * @return The object
   */
  public <T> T getOrCreateLocalExtension(Class<T> type, Function<Class<T>, T> creator) {
    return localExtensions.getOrRegister(type, creator);
  }
}
