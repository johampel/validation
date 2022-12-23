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
package de.hipphampel.validation.core.rule;

import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.exception.RuleFailedException;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.path.Resolved;
import de.hipphampel.validation.core.utils.OneOfTwo;
import de.hipphampel.validation.core.utils.TypeInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * {@link Rule} implementation that runs a {@link Method} to drive a validation.
 * <p>
 * Actually, any method - with any number of parameters and any return type - can be bound to a {@code ReflectionRule}, since each method
 * parameter needs to be bound via a {@link ParameterBinding} to a value passed on the {@link ValidationContext} and object being validated;
 * the return value is mapped via a {@link ResultMapper} to a validation result then.
 * <p>
 * Please refer to the {@link ParameterBinding} class for predefined binding types for the parameters. For the {@code ResultMapper} there
 * exists just a {@linkplain DefaultResultMapper default implementation} with limited capabilities, if you need your own, you need to
 * implemnt it.
 *
 * @param <T> Type of the object being validated
 */
public class ReflectionRule<T> extends AbstractRule<T> {

  /**
   * Strategy to populate a method argument.
   * <p>
   * Instances of this class define, how to compute a method argument based on the {@link ValidationContext} and the object to be validated.
   * The result of the binder is a {@link Resolved} object, it is the task of the {@link ReflectionRule} to deal with unresolved objects.
   * <p>
   * There are several implementation available, nevertheless it is possible to define your own:
   * <p>
   * The following implementations might be useful:
   * <ul>
   *   <li>{@link FactsBinding} returns the object being validated</li>
   *   <li>{@link PathBinding} returns the resolved value of a {@link Path} applied on the object being validated</li>
   *   <li>{@link ContextBinding} returns the {@code ValidationContext}</li>
   *   <li>{@link ContextParameterBinding} returns the value of a parameter of the {@code ValidationContext}</li>
   *   <li>{@link MetadataBinding} returns the valie of a metadata entry of the current {@link Rule}</li>
   * </ul>
   */
  @FunctionalInterface
  public interface ParameterBinding extends BiFunction<ValidationContext, Object, Resolved<?>> {

  }

  /**
   * Strategy to map the return value of a method to a {@link Result}.
   * <p>
   * This is basically jsut a plain {@link Function} having {@code Result} as output.
   */
  @FunctionalInterface
  public interface ResultMapper extends Function<Object, Result> {

  }

  private final Object boundInstance;
  private final Method ruleMethod;
  private final List<ParameterBinding> bindings;
  private final ResultMapper resultMapper;

  /**
   * Creates an instance using the given parameters.
   * <p>
   * It uses the {@link DefaultResultMapper} for mapping the result.
   *
   * @param id            The id of the {@link Rule}
   * @param factsType     {@link Class} of the objects being validated by this rule
   * @param metadata      The metadata
   * @param preconditions The list of {@linkplain Condition preconditions}
   * @param boundInstance The object that is bound to the method. In case of static methods it must be  {@code null}, in case of instance
   *                      method not {@code null}
   * @param ruleMethod    The {@code Method} to execute
   * @param bindings      The {@linkplain ParameterBinding bindings} for the method parameters
   */
  public ReflectionRule(String id, Class<? super T> factsType, Map<String, Object> metadata, List<Condition> preconditions,
      Object boundInstance, Method ruleMethod, List<ParameterBinding> bindings) {
    this(id, factsType, metadata, preconditions, boundInstance, ruleMethod, bindings, DefaultResultMapper.INSTANCE);
  }

  /**
   * Creates an instance using the given parameters.
   *
   * @param id            The id of the {@link Rule}
   * @param factsType     {@link Class} of the objects being validated by this rule
   * @param metadata      The metadata
   * @param preconditions The list of {@linkplain Condition preconditions}
   * @param boundInstance The object that is bound to the method. In case of static methods it must be  {@code null}, in case of instance
   *                      method not {@code null}
   * @param ruleMethod    The {@code Method} to execute
   * @param bindings      The {@linkplain ParameterBinding bindings} for the method parameters
   * @param resultMapper  The {@link ResultMapper} to map the methods return type to a {@link Result}
   */
  public ReflectionRule(String id, Class<? super T> factsType, Map<String, Object> metadata, List<Condition> preconditions,
      Object boundInstance, Method ruleMethod, List<ParameterBinding> bindings, ResultMapper resultMapper) {
    super(id, factsType, metadata, preconditions);
    this.boundInstance = boundInstance;
    this.ruleMethod = Objects.requireNonNull(ruleMethod);
    this.bindings = new ArrayList<>(Objects.requireNonNull(bindings));
    this.resultMapper = Objects.requireNonNull(resultMapper);
    if ((boundInstance == null) != Modifier.isStatic(ruleMethod.getModifiers())) {
      throw new IllegalArgumentException(
          "boundInstance must be null for static methods and not null for non-static method. This is not the case.");
    }
    if (bindings.size() != ruleMethod.getParameterCount()) {
      throw new IllegalArgumentException(
          "ParameterBinding count (" + bindings.size() + ") mismatches method parameter count (" + ruleMethod.getParameterCount() + ")");
    }
  }

  @Override
  public Result validate(ValidationContext context, T facts) {
    Object[] args = getArguments(context, facts);
    Object result = invokeMethod(args);
    return resultMapper.apply(result);
  }

  /**
   * Prepares the argument list for the method call.
   * <p>
   * Calls {@link #getArgument(ValidationContext, Object, Parameter, ParameterBinding) getArgument} for each argument.
   *
   * @param context The current {@link ValidationContext}
   * @param facts   The object being validated
   * @return The argument list
   */
  protected Object[] getArguments(ValidationContext context, T facts) {
    Object[] args = new Object[bindings.size()];
    for (int i = 0; i < args.length; i++) {
      args[i] = getArgument(context, facts, ruleMethod.getParameters()[i], bindings.get(i));
    }
    return args;
  }

  /**
   * Gets the actual argument for the given {@code parameter} and {@code binding}.
   * <p>
   * The method calls the binding and calls - if the binding returns an empty {@link Resolved} -
   * {@link #onUnresolvableArgument(ValidationContext, Object, Parameter, ParameterBinding) onUnresolvedArgument} or - if the binding
   * returns an existing {@code Resolved} -
   * {@link #convertArgumentIfRequired(ValidationContext, Object, Parameter, Object) convertArgumentIfRequired} to ensure that the value
   * matches the method signature
   *
   * @param context   The current {@link ValidationContext}
   * @param facts     The object being validated
   * @param parameter The method parameter
   * @param binding   The {@link ParameterBinding}
   * @return The argument.
   */
  protected Object getArgument(ValidationContext context, T facts, Parameter parameter, ParameterBinding binding) {
    return binding.apply(context, facts)
        .map(value -> convertArgumentIfRequired(context, facts, parameter, value))
        .orElseGet(() -> onUnresolvableArgument(context, facts, parameter, binding));
  }

  /**
   * Called when preparing the arguments for the method call to convert the {@code value} to the type matching the {@code parameter}.
   * <p>
   * By default, this method returns the {@code value} as it is, if it is assignable to the type expected by the {@code parameter} and
   * throws a {@link RuleFailedException} in case it is not assignable. Derived classes might decide to overwrite this method in order to
   * apply some type conversion or  throw an exception with a different {@link ResultReason}.
   *
   * @param context   The current {@link ValidationContext}
   * @param facts     The object being validated
   * @param parameter The method parameter
   * @param value     The argument
   * @return The maybe converted value
   */
  protected Object convertArgumentIfRequired(ValidationContext context, T facts, Parameter parameter, Object value) {
    if (isAssignable(parameter.getType(), value)) {
      return value;
    }
    throw new RuleFailedException(String.format("Expected a %s for parameter '%s', but got a %s",
        parameter.getType().getName(),
        parameter.getName(),
        value == null ? "'null'" : value.getClass().getName()));
  }

  /**
   * Checks, whether {@code value} is assignable to a variable having the given {@code type}
   *
   * @param type  The type
   * @param value The value
   * @return {@code true}, is assignable
   */
  protected boolean isAssignable(Class<?> type, Object value) {
    if (type.isInstance(value)) {
      return true;
    }
    if (!type.isPrimitive()) {
      return false;
    }
    Class<?> wrapperType = TypeInfo.PRIMITIVE_WRAPPER_MAP.get(type);
    return wrapperType != null && wrapperType.isInstance(value);
  }

  /**
   * Called when a parameter cannot be resolved.
   * <p>
   * A parameter is not resolvable, if the {@link ParameterBinding} returns an {@linkplain Resolved#isEmpty() empty} instance. By default,
   * this method returns {@code null} in this case.
   *
   * @param context   The current {@link ValidationContext}
   * @param facts     The object being validated
   * @param parameter The method parameter
   * @param binding   The {@link ParameterBinding}
   * @return The object to be used instead of the unresolved value.
   */
  protected Object onUnresolvableArgument(ValidationContext context, T facts, Parameter parameter, ParameterBinding binding) {
    return null;
    //throw new RuleFailedException(String.format("Cannot resolve value for parameter '%s'", parameter.getName()));
  }

  /**
   * Invokes the method bound to this rule with the given {@code args}.
   * <p>
   * Any kind of exception is wrapped by a {@link RuleFailedException}
   *
   * @param args The method arguments
   * @return The native result of the method.
   */
  protected Object invokeMethod(Object[] args) {
    try {
      return ruleMethod.invoke(boundInstance, args);
    } catch (InvocationTargetException e) {
      throw new RuleFailedException("Rule execution failed", e.getCause());
    } catch (Exception e) {
      throw new RuleFailedException("Rule execution failed", e);
    }
  }

  /**
   * Default implementation of the {@link ResultMapper}.
   * <p>
   * The following mappings exist:
   *
   * <ul>
   *   <li>If the method result is a {@link Result} instance, it is returned as it is</li>
   *   <li>If the method result is a {@code boolean} or {@code Boolean}, it returns {@link Result#ok() ok} for {@code true} and
   *   {@link Result#failed() failed} for {@code false}</li>
   *   <li>For any other type, it returned {@code failed}</li>
   * </ul>
   */
  public static class DefaultResultMapper implements ResultMapper {

    public static final ResultMapper INSTANCE = new DefaultResultMapper();

    @Override
    public Result apply(Object nativeResult) {
      if (nativeResult instanceof Result result) {
        return result;
      }
      if (nativeResult instanceof Boolean bool) {
        return bool ? Result.ok() : Result.failed();
      }

      return Result.failed(String.format("Unexpected result: %s", nativeResult));
    }
  }

  /**
   * {@link ParameterBinding} that returns the object being validated.
   */
  public record FactsBinding() implements ParameterBinding {

    @Override
    public Resolved<?> apply(ValidationContext context, Object facts) {
      return Resolved.of(facts);
    }
  }

  /**
   * {@link ParameterBinding} that resolves the given {@code path} on the object being validate.
   *
   * @param path The path to resolve
   */
  public record PathBinding(OneOfTwo<String, Path> path) implements ParameterBinding {

    public PathBinding {
      Objects.requireNonNull(path);
    }

    /**
     * Constructor
     *
     * @param path The path to resolve
     */
    public PathBinding(String path) {
      this(OneOfTwo.ofFirst(path));
    }

    /**
     * Constructor
     *
     * @param path The path to resolve
     */
    public PathBinding(Path path) {
      this(OneOfTwo.ofSecond(path));
    }

    @Override
    public Resolved<?> apply(ValidationContext context, Object facts) {
      PathResolver pathResolver = context.getPathResolver();
      return pathResolver.resolve(
          facts,
          path.mapIfFirst(pathResolver::parse));
    }
  }

  /**
   * {@link ParameterBinding} that returns the current {@link ValidationContext}
   */
  public record ContextBinding() implements ParameterBinding {

    @Override
    public Resolved<?> apply(ValidationContext context, Object facts) {
      return Resolved.of(context);
    }
  }

  /**
   * {@link ParameterBinding} that returns the {@link ValidationContext} parameter having the given {@code name}.
   * <p>
   * If the parameter not exists, a {@code null} value is reported.
   *
   * @param name Name of the {@code ValidationContext} parameter
   */
  public record ContextParameterBinding(String name) implements ParameterBinding {

    public ContextParameterBinding {
      Objects.requireNonNull(name);
    }

    @Override
    public Resolved<?> apply(ValidationContext context, Object facts) {
      return Resolved.of(context.getParameters().get(name));
    }
  }

  /**
   * {@link ParameterBinding} that returns the {@link Rule} metadata entry having the given {@code name}.
   * <p>
   * If the entry not exists, a {@code null} value is reported.
   *
   * @param name Name of the {@code Rule} metadata entry
   */
  public record MetadataBinding(String name) implements ParameterBinding {

    public MetadataBinding {
      Objects.requireNonNull(name);
    }

    @Override
    public Resolved<?> apply(ValidationContext context, Object facts) {
      Rule<?> rule = context.getCurrentRule();
      return Resolved.of(rule.getMetadata().get(name));
    }
  }
}
