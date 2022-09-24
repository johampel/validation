package de.hipphampel.validation.core.path;

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

import de.hipphampel.validation.core.utils.Pair;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link BeanAccessor} using reflection to access the objects properties.
 * <p>
 * This is also the default implementation used by the {@link BeanPathResolver}. It is abke to
 * recognize Java record components and Java bean properties.
 * <p>
 * It depends on the <i>white list</i> specified at contstruction time, for which classes accessible
 * properties are recognized. If the white list is empty, if recognozes for any class the accessors;
 * otherwise only for those classes where the full qualified class name matches at least one entry
 * of the white list (which is a list of regular expresssions).
 */
public class ReflectionBeanAccessor implements BeanAccessor {

  private final List<Pattern> whiteList;
  private final Map<Class<?>, AccessorMap> accessors;


  /**
   * Default constructor,
   * <p>
   * Creates an instance with an empty whitelist, which does not restrict the beans known by the
   * accessor.
   */
  public ReflectionBeanAccessor() {
    this(List.of());
  }

  /**
   * Constructor.
   * <p>
   * Creates an instance with a whitelist, that lets the accessor know only those classes having a
   * full qualified class name matching at least one entry in the white list.
   *
   * @param whiteList List of regular expressions.
   */
  public ReflectionBeanAccessor(List<String> whiteList) {
    this.accessors = new ConcurrentHashMap<>();
    this.whiteList = whiteList.stream()
        .map(Pattern::compile)
        .toList();
  }

  /**
   * Get the white list.
   * <p>
   * The white list is a list of regular expression for the full qualified class names of the beans
   * that are recognized by this instance. For example if the list contains only
   * {@code de\.hipphampel\..*}, only classes in the package {@code de.hipphampel} are recognized as
   * classes providing properties, If no pattern is given, all classes are recognized.
   *
   * @return The white list
   */
  public List<Pattern> getWhiteList() {
    return whiteList;
  }

  @Override
  public boolean knowsProperty(Object bean, String name) {
    if (bean == null) {
      return false;
    }
    return getAccessorMap(bean.getClass()).getAccessorMethod(name).isPresent();
  }

  @Override
  public List<String> getPropertyNames(Object bean) {
    if (bean == null) {
      return List.of();
    }
    return getAccessorMap(bean.getClass()).getPropertyNames();
  }

  @Override
  public Resolved<Object> getProperty(Object bean, String name) {
    if (bean == null) {
      return Resolved.empty();
    }
    return getAccessorMap(bean.getClass()).getAccessorMethod(name)
        .map(method -> Resolved.of(invoke(bean, method)))
        .orElse(Resolved.empty());
  }

  private Object invoke(Object bean, Method method) {
    try {
      return method.invoke(bean);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private AccessorMap getAccessorMap(Class<?> beanType) {
    return accessors.computeIfAbsent(beanType, this::createAccessorMap);
  }

  private AccessorMap createAccessorMap(Class<?> beanType) {
    if (!inWhiteList(beanType)) {
      return new AccessorMap(Map.of());
    }
    if (beanType.isRecord()) {
      return new AccessorMap(Stream.of(beanType.getRecordComponents())
          .collect(
              Collectors.toMap(RecordComponent::getName, RecordComponent::getAccessor, (a, b) -> a,
                  TreeMap::new)));
    } else {
      return new AccessorMap(Stream.of(beanType.getMethods())
          .flatMap(m -> getPropertyName(m).map(n -> Pair.of(n, m)).stream())
          .collect(Collectors.toMap(Pair::first, Pair::second, (a, b) -> a, TreeMap::new)));
    }
  }

  private boolean inWhiteList(Class<?> beanType) {
    if (whiteList.isEmpty()) {
      return true;
    }
    String className = beanType.getName();
    return whiteList.stream().anyMatch(p -> p.matcher(className).matches());
  }

  private Optional<String> getPropertyName(Method method) {
    String methodName = method.getName();
    if (method.getParameterTypes().length > 0 ||
        method.getReturnType() == void.class || method.getReturnType() == Void.class ||
        Modifier.isStatic(method.getModifiers()) ||
        "getClass".equals(methodName)) {
      return Optional.empty();
    }

    String propertyName;
    if (methodName.startsWith("get") && !methodName.equals("get")) {
      propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
    } else if (methodName.startsWith("is") && !methodName.equals("is") &&
        (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
      propertyName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
    } else {
      return Optional.empty();
    }

    return Optional.of(propertyName);
  }

  private static class AccessorMap {

    private final List<String> propertyNames;
    private final Map<String, Method> accessors;

    private AccessorMap(Map<String, Method> accessors) {
      this.propertyNames = new ArrayList<>(accessors.keySet());
      this.accessors = accessors;
    }

    public List<String> getPropertyNames() {
      return propertyNames;
    }

    public Optional<Method> getAccessorMethod(String name) {
      return Optional.ofNullable(accessors.get(name));
    }
  }
}
