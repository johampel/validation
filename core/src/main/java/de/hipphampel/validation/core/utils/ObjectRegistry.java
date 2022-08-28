package de.hipphampel.validation.core.utils;

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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Supplemental object to manage objects of different types.
 * <p>
 * This basic idea of this class is that one can register an object, optionally by specifying
 * additional types, with the intention to retrieve the object afterwards by using the type it was
 * registered for. So technically, this is basically map whereas the keys are types and the values
 * the registered objects.
 * <p>
 * Since in the scope of one instance of a {@code ObjectRegistry} only one object can be registered
 * for a specific type, all these registered objects are conceptually singletons in the scope of the
 * {@code ObjectRegistry}
 */
public class ObjectRegistry {

  private final Map<Class<?>, Object> registrations = new ConcurrentHashMap<>();

  /**
   * Registers {@code object} for its own type and optionally for the given {@code types}.
   * <p>
   * Registration means, that the object is made known to this instance for these types, so that it
   * can be looked up via the {@link #get(Class)} method.
   * <p>
   * If for one of the type another object is found, the binding for this type is removed first, but
   * the other object is still available in case it is registered for other types. This is a
   * difference to the {@link #addAndRemovePrevious(Object, Class[])} method.
   *
   * @param object The object to register. The object is at least registered for its type returned
   *               by {@code getClass()}
   * @param types  Optionally, a list of further type to register the object for. These types must
   *               be either super classes or implemented interfaces of the {@code object}
   * @return This instance
   * @see #addAndRemovePrevious(Object, Class[])
   * @see #removeType(Class)
   * @see #removeObject(Object)
   */
  public ObjectRegistry add(Object object, Class<?>... types) {
    getTypesToRegister(object, types).forEach(type -> registrations.put(type, object));
    return this;
  }

  /**
   * Registers {@code object} for its own type and optionally for the given {@code types} and
   * removes the objects that were previously registered for them.
   * <p>
   * Registration means, that the object is made known to this instance for these types, so that it
   * can be looked up via the {@link #get(Class)} method.
   * <p>
   * If for one of the type another object is found, the other object is removed from the registry,
   * so that the other object has no registrations anymore. This is a difference to the
   * {@link #add(Object, Class[])} method.
   *
   * @param object The object to register. The object is at least registered for its type returned
   *               by {@code getClass()}
   * @param types  Optionally, a list of further type to register the object for. These types must
   *               be either super classes or implemented interfaces of the {@code object}
   * @return This instance
   * @see #add(Object, Class[])
   * @see #removeType(Class)
   * @see #removeObject(Object)
   */
  public ObjectRegistry addAndRemovePrevious(Object object, Class<?>... types) {
    Set<Class<?>> typesToRegister = getTypesToRegister(object, types);

    typesToRegister.stream().filter(this::knowsType).map(this::get).forEach(this::removeObject);
    typesToRegister.forEach(type -> registrations.put(type, object));
    return this;
  }

  private Set<Class<?>> getTypesToRegister(Object object, Class<?>... types) {
    Objects.requireNonNull(object);
    Class<?> objType = object.getClass();

    Set<Class<?>> typesToRegister = new HashSet<>();
    typesToRegister.add(objType);
    for (Class<?> type : types) {
      if (!type.isAssignableFrom(objType)) {
        throw new IllegalArgumentException(objType + " is not assignable to " + type);
      }
      typesToRegister.add(type);
    }
    return typesToRegister;
  }

  /**
   * Unregisters the given {@code type}.
   * <p>
   * Removes exactly the given type. If it is bound to an object that is also registered for an
   * other type, the registration of the other type is left unchanged
   *
   * @param type The type to remove
   * @return This instance
   * @see #removeObject(Object)
   * @see #add(Object, Class[])
   * @see #addAndRemovePrevious(Object, Class[])
   */
  public ObjectRegistry removeType(Class<?> type) {
    registrations.remove(type);
    return this;
  }

  /**
   * Unregisters the given {@code object}.
   * <p>
   * This method removes all registrations for this object from the registry
   *
   * @param object The object to remove
   * @return This instance
   * @see #removeType(Class)
   * @see #add(Object, Class[])
   * @see #addAndRemovePrevious(Object, Class[])
   */
  public ObjectRegistry removeObject(Object object) {
    getTypesOf(object).forEach(this::removeType);
    return this;
  }

  /**
   * Checks, whether {@code type} is known.
   *
   * @param type The type to check
   * @return {@code true}, if known.
   */
  public boolean knowsType(Class<?> type) {
    return registrations.containsKey(type);
  }

  /**
   * Checks, whether {@code object} is known.
   *
   * @param object The object to check
   * @return {@code true}, if known.
   */
  public boolean knowsObject(Object object) {
    return registrations.containsValue(object);
  }

  /**
   * Gets all types known to this instance.
   *
   * @return {@link Set} of all known types.
   */
  public Set<Class<?>> getTypes() {
    return new HashSet<>(registrations.keySet());
  }

  /**
   * Gets the types, the given {@code object} is registered for.
   *
   * @param object The object to examine.
   * @return {@link Set} with the types {@code object} is registered for
   */
  public Set<Class<?>> getTypesOf(Object object) {
    return registrations.entrySet().stream().filter(e -> Objects.equals(object, e.getValue()))
        .map(Entry::getKey).collect(Collectors.toSet());
  }

  /**
   * Gets a set with all objects being registered
   *
   * @return Set of objects
   */
  public Set<Object> getObjects() {
    return new HashSet<>(registrations.values());
  }

  /**
   * Gets the object bound to the given {@code type}.
   * <p>
   * The method throws an exception in case that no object is registered for the given type.
   *
   * @param type The type of the request object.
   * @param <T>  Type type of the requested object
   * @return The object
   * @throws NoSuchElementException If no object is registered for the given type
   */
  public <T> T get(Class<T> type) {
    Object obj = registrations.get(type);
    if (obj == null) {
      throw new NoSuchElementException("Object of type " + type + " not known");
    }
    return type.cast(obj);
  }

  /**
   * Gets the object bound to the given {@code type} or creates a new one using the {@code creator}
   *
   * @param type    The type of the object
   * @param creator The function that creates the object in case it does not exist yet
   * @param <T>     The type of the object
   * @return The object
   */
  @SuppressWarnings("unchecked")
  public <T> T getOrRegister(Class<T> type, Function<Class<T>, T> creator) {
    return type.cast(registrations.computeIfAbsent(type, t -> creator.apply((Class<T>) t)));
  }

  /**
   * Gets an {@link Optional} for the given given {@code type}.
   * <p>
   * If an object is bound for the specified {@code type}, an according {@code Optional} with
   * present value is returned, otherwise an empty.
   *
   * @param type The type of the request object.
   * @param <T>  Type type of the requested object
   * @return The object
   */
  public <T> Optional<T> getOpt(Class<T> type) {
    return Optional.ofNullable(type.cast(registrations.get(type)));
  }

  /**
   * Returns a map with all registrations.
   *
   * @return The map
   */
  public Map<Class<?>, Object> toMap() {
    return Collections.unmodifiableMap(registrations);
  }
}
