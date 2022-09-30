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
package de.hipphampel.validation.core.utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides type information including the generic type information.
 * <p>
 * This is basically an adaption of Spring's {@çode org.springframework.core.ResolvableType} class.
 */
public class TypeInfo {

  public static final TypeInfo NONE = new TypeInfo(EmptyType.INSTANCE, null, 0);
  private static final TypeInfo[] EMPTY_TYPES_ARRAY = new TypeInfo[0];

  private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new IdentityHashMap<>(Map.of(
      boolean.class, Boolean.class,
      char.class, Character.class,
      byte.class, Byte.class,
      short.class, Short.class,
      int.class, Integer.class,
      long.class, Long.class,
      float.class, Float.class,
      double.class, Double.class,
      void.class, Void.class
  ));
  private static final Map<TypeInfo, TypeInfo> cache = new ConcurrentHashMap<>();

  private final Type type;
  private final VariableResolver variableResolver;
  private final TypeInfo componentType;
  private final Integer hash;
  private Class<?> resolved;
  private volatile TypeInfo superType;
  private volatile TypeInfo[] interfaces;
  private volatile TypeInfo[] generics;


  private TypeInfo(Type type, VariableResolver variableResolver) {
    this.type = type;
    this.variableResolver = variableResolver;
    this.componentType = null;
    this.hash = calculateHashCode();
    this.resolved = null;
  }

  private TypeInfo(Type type, VariableResolver variableResolver, Integer hash) {
    this.type = type;
    this.variableResolver = variableResolver;
    this.componentType = null;
    this.hash = hash;
    this.resolved = resolveClass();
  }

  private TypeInfo(Type type, VariableResolver variableResolver, TypeInfo componentType) {
    this.type = type;
    this.variableResolver = variableResolver;
    this.componentType = componentType;
    this.hash = null;
    this.resolved = resolveClass();
  }

  private TypeInfo(Class<?> clazz) {
    this.resolved = (clazz != null ? clazz : Object.class);
    this.type = this.resolved;
    this.variableResolver = null;
    this.componentType = null;
    this.hash = null;
  }

  /**
   * Gets the underlying {@link Type}
   *
   * @return The {@code Type}
   */
  public Type getType() {
    return this.type;
  }

  /**
   * Gets the underlying Java {@link Class} being managed.
   *
   * @return The {@code Class}, might be {@code null} if not available.
   */
  public Class<?> getRawClass() {
    if (this.type == this.resolved) {
      return this.resolved;
    }
    Type rawType = this.type;
    if (rawType instanceof ParameterizedType pt) {
      rawType = pt.getRawType();
    }
    return (rawType instanceof Class ? (Class<?>) rawType : null);
  }

  /**
   * Return this type as a resolved {@code Class}, falling back to {@link java.lang.Object} if no
   * specific class can be resolved.
   *
   * @return the resolved {@link Class} or the {@code Object} fallback
   * @see #getRawClass()
   * @see #resolve(Class)
   */
  public Class<?> toClass() {
    return resolve(Object.class);
  }

  /**
   * Determine whether the given object is an instance of this {@code TypeInfo}.
   *
   * @param obj the object to check
   * @see #isAssignableFrom(Class)
   */
  public boolean isInstance(Object obj) {
    return (obj != null && isAssignableFrom(obj.getClass()));
  }

  /**
   * Determine whether this {@code TypeInfo} is assignable from the specified other type.
   *
   * @param other the type to be checked against (as a {@code Class})
   * @see #isAssignableFrom(TypeInfo)
   */
  public boolean isAssignableFrom(Class<?> other) {
    return isAssignableFrom(forClass(other), null);
  }

  /**
   * Determine whether this {@code TypeInfo} is assignable from the specified other type.
   * <p>Attempts to follow the same rules as the Java compiler, considering
   * whether both the {@link #resolve() resolved} {@code Class} is
   * {@link Class#isAssignableFrom(Class) assignable from} the given type as well as whether all
   * {@link #getGenerics() generics} are assignable.
   *
   * @param other the type to be checked against (as a {@code TypeInfo})
   * @return {@code true} if the specified other type can be assigned to this {@code TypeInfo};
   * {@code false} otherwise
   */
  public boolean isAssignableFrom(TypeInfo other) {
    return isAssignableFrom(other, null);
  }

  private boolean isAssignableFrom(TypeInfo other, Map<Type, Type> matchedBefore) {
    if (this == NONE || other == NONE) {
      return false;
    }

    if (isArray()) {
      return (other.isArray() && getComponentType().isAssignableFrom(other.getComponentType()));
    }

    if (matchedBefore != null && matchedBefore.get(this.type) == other.type) {
      return true;
    }

    WildcardBounds ourBounds = WildcardBounds.get(this);
    WildcardBounds typeBounds = WildcardBounds.get(other);

    // In the form X is assignable to <? extends Number>
    if (typeBounds != null) {
      return (ourBounds != null && ourBounds.isSameKind(typeBounds) &&
          ourBounds.isAssignableFrom(typeBounds.getBounds()));
    }

    // In the form <? extends Number> is assignable to X...
    if (ourBounds != null) {
      return ourBounds.isAssignableFrom(other);
    }

    // Main assignability check about to follow
    boolean exactMatch = (matchedBefore != null);  // We're checking nested generic variables now...
    boolean checkGenerics = true;
    Class<?> ourResolved = null;
    if (this.type instanceof TypeVariable variable) {
      // Try default variable resolution
      if (this.variableResolver != null) {
        TypeInfo resolved = this.variableResolver.resolveVariable(variable);
        if (resolved != null) {
          ourResolved = resolved.resolve();
        }
      }
      if (ourResolved == null) {
        // Try variable resolution against target type
        if (other.variableResolver != null) {
          TypeInfo resolved = other.variableResolver.resolveVariable(variable);
          if (resolved != null) {
            ourResolved = resolved.resolve();
            checkGenerics = false;
          }
        }
      }
      if (ourResolved == null) {
        // Unresolved type variable, potentially nested -> never insist on exact match
        exactMatch = false;
      }
    }
    if (ourResolved == null) {
      ourResolved = resolve(Object.class);
    }
    Class<?> otherResolved = other.toClass();

    // We need an exact type match for generics
    // List<CharSequence> is not assignable from List<String>
    if (exactMatch ? !ourResolved.equals(otherResolved)
        : !isAssignable(ourResolved, otherResolved)) {
      return false;
    }

    if (checkGenerics) {
      // Recursively check each generic
      TypeInfo[] ourGenerics = getGenerics();
      TypeInfo[] typeGenerics = other.as(ourResolved).getGenerics();
      if (ourGenerics.length != typeGenerics.length) {
        return false;
      }
      if (matchedBefore == null) {
        matchedBefore = new IdentityHashMap<>(1);
      }
      matchedBefore.put(this.type, other.type);
      for (int i = 0; i < ourGenerics.length; i++) {
        if (!ourGenerics[i].isAssignableFrom(typeGenerics[i], matchedBefore)) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Return {@code true} if this type resolves to a Class that represents an array.
   *
   * @see #getComponentType()
   */
  public boolean isArray() {
    if (this == NONE) {
      return false;
    }
    return ((this.type instanceof Class && ((Class<?>) this.type).isArray()) ||
        this.type instanceof GenericArrayType || resolveType().isArray());
  }

  /**
   * Return the TypeInfo representing the component type of the array or {@link #NONE} if this type
   * does not represent an array.
   *
   * @see #isArray()
   */
  public TypeInfo getComponentType() {
    if (this == NONE) {
      return NONE;
    }
    if (this.componentType != null) {
      return this.componentType;
    }
    if (this.type instanceof Class) {
      Class<?> componentType = ((Class<?>) this.type).getComponentType();
      return forType(componentType, this.variableResolver);
    }
    if (this.type instanceof GenericArrayType) {
      return forType(((GenericArrayType) this.type).getGenericComponentType(),
          this.variableResolver);
    }
    return resolveType().getComponentType();
  }

  /**
   * Convenience method to return this type as a resolvable {@link Collection} type.
   * <p>Returns {@link #NONE} if this type does not implement or extend
   * {@link Collection}.
   *
   * @see #as(Class)
   * @see #asMap()
   */
  public TypeInfo asCollection() {
    return as(Collection.class);
  }

  /**
   * Convenience method to return this type as a resolvable {@link Map} type.
   * <p>Returns {@link #NONE} if this type does not implement or extend
   * {@link Map}.
   *
   * @see #as(Class)
   * @see #asCollection()
   */
  public TypeInfo asMap() {
    return as(Map.class);
  }

  /**
   * Return this type as a {@link TypeInfo} of the specified class. Searches
   * {@link #getSuperType() supertype} and {@link #getInterfaces() interface} hierarchies to find a
   * match, returning {@link #NONE} if this type does not implement or extend the specified class.
   *
   * @param type the required type (typically narrowed)
   * @return a {@link TypeInfo} representing this object as the specified type, or {@link #NONE} if
   * not resolvable as that type
   * @see #asCollection()
   * @see #asMap()
   * @see #getSuperType()
   * @see #getInterfaces()
   */
  public TypeInfo as(Class<?> type) {
    if (this == NONE) {
      return NONE;
    }
    Class<?> resolved = resolve();
    if (resolved == null || resolved == type) {
      return this;
    }
    for (TypeInfo interfaceType : getInterfaces()) {
      TypeInfo interfaceAsType = interfaceType.as(type);
      if (interfaceAsType != NONE) {
        return interfaceAsType;
      }
    }
    return getSuperType().as(type);
  }

  /**
   * Return a {@link TypeInfo} representing the direct supertype of this type.
   * <p>If no supertype is available this method returns {@link #NONE}.
   *
   * @see #getInterfaces()
   */
  public TypeInfo getSuperType() {
    Class<?> resolved = resolve();
    if (resolved == null) {
      return NONE;
    }
    try {
      Type superclass = resolved.getGenericSuperclass();
      if (superclass == null) {
        return NONE;
      }
      TypeInfo superType = this.superType;
      if (superType == null) {
        superType = forType(superclass, this);
        this.superType = superType;
      }
      return superType;
    } catch (TypeNotPresentException ex) {
      // Ignore non-present types in generic signature
      return NONE;
    }
  }

  /**
   * Return a {@link TypeInfo} array representing the direct interfaces implemented by this type. If
   * this type does not implement any interfaces an empty array is returned.
   *
   * @see #getSuperType()
   */
  public TypeInfo[] getInterfaces() {
    Class<?> resolved = resolve();
    if (resolved == null) {
      return EMPTY_TYPES_ARRAY;
    }
    TypeInfo[] interfaces = this.interfaces;
    if (interfaces == null) {
      Type[] genericIfcs = resolved.getGenericInterfaces();
      interfaces = new TypeInfo[genericIfcs.length];
      for (int i = 0; i < genericIfcs.length; i++) {
        interfaces[i] = forType(genericIfcs[i], this);
      }
      this.interfaces = interfaces;
    }
    return interfaces;
  }

  /**
   * Return {@code true} if this type contains generic parameters.
   *
   * @see #getGeneric(int...)
   * @see #getGenerics()
   */
  public boolean hasGenerics() {
    return (getGenerics().length > 0);
  }

  /**
   * Return {@code true} if this type contains unresolvable generics only, that is, no substitute
   * for any of its declared type variables.
   */
  boolean isEntirelyUnresolvable() {
    if (this == NONE) {
      return false;
    }
    TypeInfo[] generics = getGenerics();
    for (TypeInfo generic : generics) {
      if (!generic.isUnresolvableTypeVariable() && !generic.isWildcardWithoutBounds()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determine whether the underlying type has any unresolvable generics: either through an
   * unresolvable type variable on the type itself or through implementing a generic interface in a
   * raw fashion, i.e. without substituting that interface's type variables. The result will be
   * {@code true} only in those two scenarios.
   */
  public boolean hasUnresolvableGenerics() {
    if (this == NONE) {
      return false;
    }
    TypeInfo[] generics = getGenerics();
    for (TypeInfo generic : generics) {
      if (generic.isUnresolvableTypeVariable() || generic.isWildcardWithoutBounds()) {
        return true;
      }
    }
    Class<?> resolved = resolve();
    if (resolved != null) {
      try {
        for (Type genericInterface : resolved.getGenericInterfaces()) {
          if (genericInterface instanceof Class) {
            if (forClass((Class<?>) genericInterface).hasGenerics()) {
              return true;
            }
          }
        }
      } catch (TypeNotPresentException ex) {
        // Ignore non-present types in generic signature
      }
      return getSuperType().hasUnresolvableGenerics();
    }
    return false;
  }

  /**
   * Determine whether the underlying type is a type variable that cannot be resolved through the
   * associated variable resolver.
   */
  private boolean isUnresolvableTypeVariable() {
    if (this.type instanceof TypeVariable) {
      if (this.variableResolver == null) {
        return true;
      }
      TypeVariable<?> variable = (TypeVariable<?>) this.type;
      TypeInfo resolved = this.variableResolver.resolveVariable(variable);
      if (resolved == null || resolved.isUnresolvableTypeVariable()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determine whether the underlying type represents a wildcard without specific bounds (i.e.,
   * equal to {@code ? extends Object}).
   */
  private boolean isWildcardWithoutBounds() {
    if (this.type instanceof WildcardType wt) {
      if (wt.getLowerBounds().length == 0) {
        Type[] upperBounds = wt.getUpperBounds();
        if (upperBounds.length == 0 || (upperBounds.length == 1
            && Object.class == upperBounds[0])) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Return a {@link TypeInfo} for the specified nesting level.
   * <p>See {@link #getNested(int, Map)} for details.
   *
   * @param nestingLevel the nesting level
   * @return the {@link TypeInfo} type, or {@code #NONE}
   */
  public TypeInfo getNested(int nestingLevel) {
    return getNested(nestingLevel, null);
  }

  /**
   * Return a {@link TypeInfo} for the specified nesting level.
   * <p>The nesting level refers to the specific generic parameter that should be returned.
   * A nesting level of 1 indicates this type; 2 indicates the first nested generic; 3 the second;
   * and so on. For example, given {@code List<Set<Integer>>} level 1 refers to the {@code List},
   * level 2 the {@code Set}, and level 3 the {@code Integer}.
   * <p>The {@code typeIndexesPerLevel} map can be used to reference a specific generic
   * for the given level. For example, an index of 0 would refer to a {@code Map} key; whereas, 1
   * would refer to the value. If the map does not contain a value for a specific level the last
   * generic will be used (e.g. a {@code Map} value).
   * <p>Nesting levels may also apply to array types; for example given
   * {@code String[]}, a nesting level of 2 refers to {@code String}.
   * <p>If a type does not {@link #hasGenerics() contain} generics the
   * {@link #getSuperType() supertype} hierarchy will be considered.
   *
   * @param nestingLevel        the required nesting level, indexed from 1 for the current type, 2
   *                            for the first nested generic, 3 for the second and so on
   * @param typeIndexesPerLevel a map containing the generic index for a given nesting level (may be
   *                            {@code null})
   * @return a {@link TypeInfo} for the nested level, or {@link #NONE}
   */
  public TypeInfo getNested(int nestingLevel, Map<Integer, Integer> typeIndexesPerLevel) {
    TypeInfo result = this;
    for (int i = 2; i <= nestingLevel; i++) {
      if (result.isArray()) {
        result = result.getComponentType();
      } else {
        // Handle derived types
        while (result != TypeInfo.NONE && !result.hasGenerics()) {
          result = result.getSuperType();
        }
        Integer index = (typeIndexesPerLevel != null ? typeIndexesPerLevel.get(i) : null);
        index = (index == null ? result.getGenerics().length - 1 : index);
        result = result.getGeneric(index);
      }
    }
    return result;
  }

  /**
   * Return a {@link TypeInfo} representing the generic parameter for the given indexes. Indexes are
   * zero based; for example given the type {@code Map<Integer, List<String>>},
   * {@code getGeneric(0)} will access the {@code Integer}. Nested generics can be accessed by
   * specifying multiple indexes; for example {@code getGeneric(1, 0)} will access the
   * {@code String} from the nested {@code List}. For convenience, if no indexes are specified the
   * first generic is returned.
   * <p>If no generic is available at the specified indexes {@link #NONE} is returned.
   *
   * @param indexes the indexes that refer to the generic parameter (may be omitted to return the
   *                first generic)
   * @return a {@link TypeInfo} for the specified generic, or {@link #NONE}
   * @see #hasGenerics()
   * @see #getGenerics()
   * @see #resolveGeneric(int...)
   * @see #resolveGenerics()
   */
  public TypeInfo getGeneric(int... indexes) {
    TypeInfo[] generics = getGenerics();
    if (indexes == null || indexes.length == 0) {
      return (generics.length == 0 ? NONE : generics[0]);
    }
    TypeInfo generic = this;
    for (int index : indexes) {
      generics = generic.getGenerics();
      if (index < 0 || index >= generics.length) {
        return NONE;
      }
      generic = generics[index];
    }
    return generic;
  }

  /**
   * Return an array of {@link TypeInfo ResolvableTypes} representing the generic parameters of this
   * type. If no generics are available an empty array is returned. If you need to access a specific
   * generic consider using the {@link #getGeneric(int...)} method as it allows access to nested
   * generics and protects against {@code IndexOutOfBoundsExceptions}.
   *
   * @return an array of {@link TypeInfo ResolvableTypes} representing the generic parameters (never
   * {@code null})
   * @see #hasGenerics()
   * @see #getGeneric(int...)
   * @see #resolveGeneric(int...)
   * @see #resolveGenerics()
   */
  public TypeInfo[] getGenerics() {
    if (this == NONE) {
      return EMPTY_TYPES_ARRAY;
    }
    TypeInfo[] generics = this.generics;
    if (generics == null) {
      if (this.type instanceof Class) {
        Type[] typeParams = ((Class<?>) this.type).getTypeParameters();
        generics = new TypeInfo[typeParams.length];
        for (int i = 0; i < generics.length; i++) {
          generics[i] = TypeInfo.forType(typeParams[i], this);
        }
      } else if (this.type instanceof ParameterizedType) {
        Type[] actualTypeArguments = ((ParameterizedType) this.type).getActualTypeArguments();
        generics = new TypeInfo[actualTypeArguments.length];
        for (int i = 0; i < actualTypeArguments.length; i++) {
          generics[i] = forType(actualTypeArguments[i], this.variableResolver);
        }
      } else {
        generics = resolveType().getGenerics();
      }
      this.generics = generics;
    }
    return generics;
  }

  /**
   * Convenience method that will {@link #getGenerics() get} and {@link #resolve() resolve} generic
   * parameters.
   *
   * @return an array of resolved generic parameters (the resulting array will never be
   * {@code null}, but it may contain {@code null} elements})
   * @see #getGenerics()
   * @see #resolve()
   */
  public Class<?>[] resolveGenerics() {
    TypeInfo[] generics = getGenerics();
    Class<?>[] resolvedGenerics = new Class<?>[generics.length];
    for (int i = 0; i < generics.length; i++) {
      resolvedGenerics[i] = generics[i].resolve();
    }
    return resolvedGenerics;
  }

  /**
   * Convenience method that will {@link #getGenerics() get} and {@link #resolve() resolve} generic
   * parameters, using the specified {@code fallback} if any type cannot be resolved.
   *
   * @param fallback the fallback class to use if resolution fails
   * @return an array of resolved generic parameters
   * @see #getGenerics()
   * @see #resolve()
   */
  public Class<?>[] resolveGenerics(Class<?> fallback) {
    TypeInfo[] generics = getGenerics();
    Class<?>[] resolvedGenerics = new Class<?>[generics.length];
    for (int i = 0; i < generics.length; i++) {
      resolvedGenerics[i] = generics[i].resolve(fallback);
    }
    return resolvedGenerics;
  }

  /**
   * Convenience method that will {@link #getGeneric(int...) get} and {@link #resolve() resolve} a
   * specific generic parameters.
   *
   * @param indexes the indexes that refer to the generic parameter (may be omitted to return the
   *                first generic)
   * @return a resolved {@link Class} or {@code null}
   * @see #getGeneric(int...)
   * @see #resolve()
   */

  public Class<?> resolveGeneric(int... indexes) {
    return getGeneric(indexes).resolve();
  }

  /**
   * Resolve this type to a {@link java.lang.Class}, returning {@code null} if the type cannot be
   * resolved. This method will consider bounds of {@link TypeVariable TypeVariables} and
   * {@link WildcardType WildcardTypes} if direct resolution fails; however, bounds of
   * {@code Object.class} will be ignored.
   * <p>If this method returns a non-null {@code Class} and {@link #hasGenerics()}
   * returns {@code false}, the given type effectively wraps a plain {@code Class}, allowing for
   * plain {@code Class} processing if desirable.
   *
   * @return the resolved {@link Class}, or {@code null} if not resolvable
   * @see #resolve(Class)
   * @see #resolveGeneric(int...)
   * @see #resolveGenerics()
   */

  public Class<?> resolve() {
    return this.resolved;
  }

  /**
   * Resolve this type to a {@link java.lang.Class}, returning the specified {@code fallback} if the
   * type cannot be resolved. This method will consider bounds of {@link TypeVariable TypeVariables}
   * and {@link WildcardType WildcardTypes} if direct resolution fails; however, bounds of
   * {@code Object.class} will be ignored.
   *
   * @param fallback the fallback class to use if resolution fails
   * @return the resolved {@link Class} or the {@code fallback}
   * @see #resolve()
   * @see #resolveGeneric(int...)
   * @see #resolveGenerics()
   */
  public Class<?> resolve(Class<?> fallback) {
    return (this.resolved != null ? this.resolved : fallback);
  }


  private Class<?> resolveClass() {
    if (this.type == EmptyType.INSTANCE) {
      return null;
    }
    if (this.type instanceof Class) {
      return (Class<?>) this.type;
    }
    if (this.type instanceof GenericArrayType) {
      Class<?> resolvedComponent = getComponentType().resolve();
      return (resolvedComponent != null ? Array.newInstance(resolvedComponent, 0).getClass()
          : null);
    }
    return resolveType().resolve();
  }

  /**
   * Resolve this type by a single level, returning the resolved value or {@link #NONE}.
   * <p>Note: The returned {@link TypeInfo} should only be used as an intermediary
   * as it cannot be serialized.
   */
  TypeInfo resolveType() {
    if (this.type instanceof ParameterizedType pt) {
      return forType(pt.getRawType(), this.variableResolver);
    }
    if (this.type instanceof WildcardType wt) {
      Type resolved = resolveBounds(wt.getUpperBounds());
      if (resolved == null) {
        resolved = resolveBounds(wt.getLowerBounds());
      }
      return forType(resolved, this.variableResolver);
    }
    if (this.type instanceof TypeVariable variable) {
      // Try default variable resolution
      if (this.variableResolver != null) {
        TypeInfo resolved = this.variableResolver.resolveVariable(variable);
        if (resolved != null) {
          return resolved;
        }
      }
      // Fallback to bounds
      return forType(resolveBounds(variable.getBounds()), this.variableResolver);
    }
    return NONE;
  }


  private Type resolveBounds(Type[] bounds) {
    if (bounds.length == 0 || bounds[0] == Object.class) {
      return null;
    }
    return bounds[0];
  }


  private TypeInfo resolveVariable(TypeVariable<?> variable) {
    if (this.type instanceof TypeVariable) {
      return resolveType().resolveVariable(variable);
    }
    if (this.type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) this.type;
      Class<?> resolved = resolve();
      if (resolved == null) {
        return null;
      }
      TypeVariable<?>[] variables = resolved.getTypeParameters();
      for (int i = 0; i < variables.length; i++) {
        if (Objects.equals(variables[i].getName(), variable.getName())) {
          Type actualType = parameterizedType.getActualTypeArguments()[i];
          return forType(actualType, this.variableResolver);
        }
      }
      Type ownerType = parameterizedType.getOwnerType();
      if (ownerType != null) {
        return forType(ownerType, this.variableResolver).resolveVariable(variable);
      }
    }
    if (this.type instanceof WildcardType) {
      TypeInfo resolved = resolveType().resolveVariable(variable);
      if (resolved != null) {
        return resolved;
      }
    }
    if (this.variableResolver != null) {
      return this.variableResolver.resolveVariable(variable);
    }
    return null;
  }


  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof TypeInfo)) {
      return false;
    }

    TypeInfo otherType = (TypeInfo) other;
    if (!Objects.equals(this.type, otherType.type)) {
      return false;
    }
    if (this.variableResolver != otherType.variableResolver &&
        (this.variableResolver == null || otherType.variableResolver == null ||
            !Objects.equals(this.variableResolver.getSource(),
                otherType.variableResolver.getSource()))) {
      return false;
    }
    if (!Objects.equals(this.componentType, otherType.componentType)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return (this.hash != null ? this.hash : calculateHashCode());
  }

  private int calculateHashCode() {
    int hashCode = Objects.hashCode(this.type);
    if (this.variableResolver != null) {
      hashCode = 31 * hashCode + Objects.hashCode(this.variableResolver.getSource());
    }
    if (this.componentType != null) {
      hashCode = 31 * hashCode + Objects.hashCode(this.componentType);
    }
    return hashCode;
  }

  /**
   * Adapts this {@link TypeInfo} to a {@link VariableResolver}.
   */

  VariableResolver asVariableResolver() {
    if (this == NONE) {
      return null;
    }
    return new DefaultVariableResolver(this);
  }

  /**
   * Custom serialization support for {@link #NONE}.
   */
  private Object readResolve() {
    return (this.type == EmptyType.INSTANCE ? NONE : this);
  }

  /**
   * Return a String representation of this type in its fully resolved form (including any generic
   * parameters).
   */
  @Override
  public String toString() {
    if (isArray()) {
      return getComponentType() + "[]";
    }
    if (this.resolved == null) {
      return "?";
    }
    if (this.type instanceof TypeVariable<?> variable) {
      if (this.variableResolver == null
          || this.variableResolver.resolveVariable(variable) == null) {
        // Don't bother with variable boundaries for toString()...
        // Can cause infinite recursions in case of self-references
        return "?";
      }
    }
    if (hasGenerics()) {
      StringBuilder builder = new StringBuilder(this.resolved.getName()).append("<");
      TypeInfo[] gen = getGenerics();
      for (int i = 0; i < gen.length; i++) {
        if (i > 0) {
          builder.append(", ");
        }
        builder.append(gen[i]);
      }
      return builder.append(">").toString();
    }
    return this.resolved.getName();
  }

  // Factory methods

  /**
   * Return a {@link TypeInfo} for the specified {@link Class}, using the full generic type
   * information for assignability checks.
   * <p>For example: {@code TypeInfo.forClass(MyArrayList.class)}.
   *
   * @param clazz the class to introspect ({@code null} is semantically equivalent to
   *              {@code Object.class} for typical use cases here)
   * @return a {@link TypeInfo} for the specified class
   * @see #forClass(Class, Class)
   * @see #forClassWithGenerics(Class, Class...)
   */
  public static TypeInfo forClass(Class<?> clazz) {
    return new TypeInfo(clazz);
  }

  /**
   * Return a {@link TypeInfo} for the specified {@link Class}, doing assignability checks against
   * the raw class only (analogous to {@link Class#isAssignableFrom}, which this serves as a wrapper
   * for.
   * <p>For example: {@code TypeInfo.forRawClass(List.class)}.
   *
   * @param clazz the class to introspect ({@code null} is semantically equivalent to
   *              {@code Object.class} for typical use cases here)
   * @return a {@link TypeInfo} for the specified class
   * @see #forClass(Class)
   * @see #getRawClass()
   * @since 4.2
   */
  public static TypeInfo forRawClass(Class<?> clazz) {
    return new TypeInfo(clazz) {
      @Override
      public TypeInfo[] getGenerics() {
        return EMPTY_TYPES_ARRAY;
      }

      @Override
      public boolean isAssignableFrom(Class<?> other) {
        return (clazz == null || isAssignable(clazz, other));
      }

      @Override
      public boolean isAssignableFrom(TypeInfo other) {
        Class<?> otherClass = other.resolve();
        return (otherClass != null && (clazz == null || isAssignable(clazz, otherClass)));
      }
    };
  }

  /**
   * Return a {@link TypeInfo} for the specified base type (interface or base class) with a given
   * implementation class.
   * <p>For example: {@code TypeInfo.forClass(List.class, MyArrayList.class)}.
   *
   * @param baseType            the base type (must not be {@code null})
   * @param implementationClass the implementation class
   * @return a {@link TypeInfo} for the specified base type backed by the given implementation class
   * @see #forClass(Class)
   * @see #forClassWithGenerics(Class, Class...)
   */
  public static TypeInfo forClass(Class<?> baseType, Class<?> implementationClass) {
    TypeInfo asType = forType(implementationClass).as(baseType);
    return (asType == NONE ? forType(baseType) : asType);
  }

  /**
   * Return a {@link TypeInfo} for the specified {@link Class} with pre-declared generics.
   *
   * @param clazz    the class (or interface) to introspect
   * @param generics the generics of the class
   * @return a {@link TypeInfo} for the specific class and generics
   * @see #forClassWithGenerics(Class, TypeInfo...)
   */
  public static TypeInfo forClassWithGenerics(Class<?> clazz, Class<?>... generics) {
    TypeInfo[] resolvableGenerics = new TypeInfo[generics.length];
    for (int i = 0; i < generics.length; i++) {
      resolvableGenerics[i] = forClass(generics[i]);
    }
    return forClassWithGenerics(clazz, resolvableGenerics);
  }

  /**
   * Return a {@link TypeInfo} for the specified {@link Class} with pre-declared generics.
   *
   * @param clazz    the class (or interface) to introspect
   * @param generics the generics of the class
   * @return a {@link TypeInfo} for the specific class and generics
   * @see #forClassWithGenerics(Class, Class...)
   */
  public static TypeInfo forClassWithGenerics(Class<?> clazz, TypeInfo... generics) {
    TypeVariable<?>[] variables = clazz.getTypeParameters();
    if (variables.length != generics.length) {
      throw new IllegalArgumentException(
          "Mismatched number of generics specified for " + clazz.toGenericString());
    }
    Type[] arguments = new Type[generics.length];
    for (int i = 0; i < generics.length; i++) {
      TypeInfo generic = generics[i];
      Type argument = (generic != null ? generic.getType() : null);
      arguments[i] = (argument != null && !(argument instanceof TypeVariable) ? argument
          : variables[i]);
    }

    ParameterizedType syntheticType = new SyntheticParameterizedType(clazz, arguments);
    return forType(syntheticType, new TypeVariablesVariableResolver(variables, generics));
  }

  /**
   * Return a {@link TypeInfo} for the specified instance. The instance does not convey generic
   * information.
   *
   * @param instance the instance
   * @return a {@link TypeInfo} for the specified instance
   */
  public static TypeInfo forInstance(Object instance) {
    return TypeInfo.forClass(instance.getClass());
  }

  /**
   * Return a {@link TypeInfo} as a array of the specified {@code componentType}.
   *
   * @param componentType the component type
   * @return a {@link TypeInfo} as an array of the specified component type
   */
  public static TypeInfo forArrayComponent(TypeInfo componentType) {
    Class<?> arrayClass = Array.newInstance(componentType.resolve(), 0).getClass();
    return new TypeInfo(arrayClass, null, componentType);
  }

  /**
   * Return a {@link TypeInfo} for the specified {@link Type}.
   *
   * @param type the source type (potentially {@code null})
   * @return a {@link TypeInfo} for the specified {@link Type}
   * @see #forType(Type, TypeInfo)
   */
  public static TypeInfo forType(Type type) {
    return forType(type, (TypeInfo) null);
  }

  /**
   * Return a {@link TypeInfo} for the specified {@link Type} backed by the given owner type.
   *
   * @param type  the source type or {@code null}
   * @param owner the owner type used to resolve variables
   * @return a {@link TypeInfo} for the specified {@link Type} and owner
   * @see #forType(Type)
   */
  public static TypeInfo forType(Type type, TypeInfo owner) {
    VariableResolver variableResolver = null;
    if (owner != null) {
      variableResolver = owner.asVariableResolver();
    }
    return forType(type, variableResolver);
  }

  /**
   * Return a {@link TypeInfo} for the specified {@link Type} backed by a given
   * {@link VariableResolver}.
   *
   * @param type             the source type or {@code null}
   * @param variableResolver the variable resolver or {@code null}
   * @return a {@link TypeInfo} for the specified {@link Type} and {@link VariableResolver}
   */
  static TypeInfo forType(Type type, VariableResolver variableResolver) {
    if (type == null) {
      return NONE;
    }

    // For simple Class references, build the wrapper right away -
    // no expensive resolution necessary, so not worth caching...
    if (type instanceof Class) {
      return new TypeInfo(type, variableResolver, (TypeInfo) null);
    }

    // Check the cache - we may have a TypeInfo which has been resolved before...
    TypeInfo resultType = new TypeInfo(type, variableResolver);
    TypeInfo cachedType = cache.get(resultType);
    if (cachedType == null) {
      cachedType = new TypeInfo(type, variableResolver, resultType.hash);
      cache.put(cachedType, cachedType);
    }
    resultType.resolved = cachedType.resolved;
    return resultType;
  }

  /**
   * Gets the type wrapped by the given {@code typeRef}
   *
   * @param typeRef The {@link TypeReference}
   * @return The {@link TypeInfo}
   */
  public static <T> TypeInfo forTypeReference(TypeReference<T> typeRef) {
    Type[] interfaces = typeRef.getClass().getGenericInterfaces();
    for(Type interf: interfaces) {
      if (interf instanceof ParameterizedType pt && pt.getRawType() == TypeReference.class) {
        return forType(pt.getActualTypeArguments()[0]);
      }
    }
    return TypeInfo.NONE;
  }

  /**
   * Clear the internal {@code TypeInfo} cache.
   *
   * @since 4.2
   */
  public static void clearCache() {
    cache.clear();
  }

  private static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
    if (lhsType.isAssignableFrom(rhsType)) {
      return true;
    }
    if (lhsType.isPrimitive()) {
      Class<?> resolvedPrimitive = PRIMITIVE_WRAPPER_MAP.get(rhsType);
      return (lhsType == resolvedPrimitive);
    } else {
      Class<?> resolvedWrapper = PRIMITIVE_WRAPPER_MAP.get(rhsType);
      return (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper));
    }
  }

  /**
   * Strategy interface used to resolve {@link TypeVariable TypeVariables}.
   */
  interface VariableResolver {

    /**
     * Return the source of the resolver (used for hashCode and equals).
     */
    Object getSource();

    /**
     * Resolve the specified variable.
     *
     * @param variable the variable to resolve
     * @return the resolved variable, or {@code null} if not found
     */

    TypeInfo resolveVariable(TypeVariable<?> variable);
  }

  private static class DefaultVariableResolver implements VariableResolver {

    private final TypeInfo source;

    DefaultVariableResolver(TypeInfo typeInfo) {
      this.source = typeInfo;
    }

    @Override
    public TypeInfo resolveVariable(TypeVariable<?> variable) {
      return this.source.resolveVariable(variable);
    }

    @Override
    public Object getSource() {
      return this.source;
    }
  }

  private static class TypeVariablesVariableResolver implements VariableResolver {

    private final TypeVariable<?>[] variables;

    private final TypeInfo[] generics;

    public TypeVariablesVariableResolver(TypeVariable<?>[] variables, TypeInfo[] generics) {
      this.variables = variables;
      this.generics = generics;
    }

    @Override
    public TypeInfo resolveVariable(TypeVariable<?> variable) {
      for (int i = 0; i < this.variables.length; i++) {
        if (Objects.equals(this.variables[i], variable)) {
          return this.generics[i];
        }
      }
      return null;
    }

    @Override
    public Object getSource() {
      return this.generics;
    }
  }

  private static final class SyntheticParameterizedType implements ParameterizedType {

    private final Type rawType;

    private final Type[] typeArguments;

    public SyntheticParameterizedType(Type rawType, Type[] typeArguments) {
      this.rawType = rawType;
      this.typeArguments = typeArguments;
    }

    @Override
    public String getTypeName() {
      String typeName = this.rawType.getTypeName();
      if (this.typeArguments.length > 0) {
        StringJoiner stringJoiner = new StringJoiner(", ", "<", ">");
        for (Type argument : this.typeArguments) {
          stringJoiner.add(argument.getTypeName());
        }
        return typeName + stringJoiner;
      }
      return typeName;
    }

    @Override

    public Type getOwnerType() {
      return null;
    }

    @Override
    public Type getRawType() {
      return this.rawType;
    }

    @Override
    public Type[] getActualTypeArguments() {
      return this.typeArguments;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof ParameterizedType otherType)) {
        return false;
      }
      return (otherType.getOwnerType() == null && this.rawType.equals(otherType.getRawType()) &&
          Arrays.equals(this.typeArguments, otherType.getActualTypeArguments()));
    }

    @Override
    public int hashCode() {
      return (this.rawType.hashCode() * 31 + Arrays.hashCode(this.typeArguments));
    }

    @Override
    public String toString() {
      return getTypeName();
    }
  }

  /**
   * Internal helper to handle bounds from {@link WildcardType WildcardTypes}.
   */
  private static class WildcardBounds {

    private final Kind kind;

    private final TypeInfo[] bounds;

    /**
     * Internal constructor to create a new {@link WildcardBounds} instance.
     *
     * @param kind   the kind of bounds
     * @param bounds the bounds
     * @see #get(TypeInfo)
     */
    public WildcardBounds(Kind kind, TypeInfo[] bounds) {
      this.kind = kind;
      this.bounds = bounds;
    }

    /**
     * Return {@code true} if this bounds is the same kind as the specified bounds.
     */
    public boolean isSameKind(WildcardBounds bounds) {
      return this.kind == bounds.kind;
    }

    /**
     * Return {@code true} if this bounds is assignable to all the specified types.
     *
     * @param types the types to test against
     * @return {@code true} if this bounds is assignable to all types
     */
    public boolean isAssignableFrom(TypeInfo... types) {
      for (TypeInfo bound : this.bounds) {
        for (TypeInfo type : types) {
          if (!isAssignable(bound, type)) {
            return false;
          }
        }
      }
      return true;
    }

    private boolean isAssignable(TypeInfo source, TypeInfo from) {
      return (this.kind == Kind.UPPER ? source.isAssignableFrom(from)
          : from.isAssignableFrom(source));
    }

    /**
     * Return the underlying bounds.
     */
    public TypeInfo[] getBounds() {
      return this.bounds;
    }

    /**
     * Get a {@link WildcardBounds} instance for the specified type, returning {@code null} if the
     * specified type cannot be resolved to a {@link WildcardType}.
     *
     * @param type the source type
     * @return a {@link WildcardBounds} instance or {@code null}
     */

    public static WildcardBounds get(TypeInfo type) {
      TypeInfo resolveToWildcard = type;
      while (!(resolveToWildcard.getType() instanceof WildcardType)) {
        if (resolveToWildcard == NONE) {
          return null;
        }
        resolveToWildcard = resolveToWildcard.resolveType();
      }
      WildcardType wildcardType = (WildcardType) resolveToWildcard.type;
      Kind boundsType = (wildcardType.getLowerBounds().length > 0 ? Kind.LOWER : Kind.UPPER);
      Type[] bounds = (boundsType == Kind.UPPER ? wildcardType.getUpperBounds()
          : wildcardType.getLowerBounds());
      TypeInfo[] resolvableBounds = new TypeInfo[bounds.length];
      for (int i = 0; i < bounds.length; i++) {
        resolvableBounds[i] = TypeInfo.forType(bounds[i], type.variableResolver);
      }
      return new WildcardBounds(boundsType, resolvableBounds);
    }

    enum Kind {UPPER, LOWER}
  }

  private static class EmptyType implements Type {

    static final Type INSTANCE = new EmptyType();

    Object readResolve() {
      return INSTANCE;
    }
  }

}
