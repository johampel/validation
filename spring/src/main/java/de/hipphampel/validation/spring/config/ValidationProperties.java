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
package de.hipphampel.validation.spring.config;

import de.hipphampel.validation.core.path.AbstractComponentPathResolver;
import de.hipphampel.validation.core.path.BeanPathResolver;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.Resolved;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable properties of the {@link ValidationAutoConfiguration}.
 * <p>
 * All properties are bound under the prfix {@code validation}.
 */
@ConfigurationProperties(prefix = "validation")
public class ValidationProperties {

  private PathResolverProperties pathResolver = new PathResolverProperties();

  /**
   * Gets the properties to configure the {@link BeanPathResolver}.
   *
   * @return The configuration properties,
   */
  public PathResolverProperties getPathResolver() {
    return pathResolver;
  }

  /**
   * Sets the properties to configure the {@link BeanPathResolver}
   *
   * @param pathResolver The configuration properties
   */
  public void setPathResolver(PathResolverProperties pathResolver) {
    this.pathResolver = pathResolver;
  }

  /**
   * Properties to configure a {@link BeanPathResolver}.
   * <p>
   * Please also refer to the {@link AbstractComponentPathResolver} for a detailed description of the settings
   *
   * @see AbstractComponentPathResolver
   */
  public static class PathResolverProperties {

    private String separator = "/";
    private String allInLevel = "*";
    private String manyLevels = "**";

    private boolean mapUnresolvableToNull = false;
    private List<String> whiteList = List.of();

    /**
     * Gets the separator string to separate components of a path.
     *
     * @return Separator string
     */
    public String getSeparator() {
      return separator;
    }

    /**
     * Sets the separator string to separate components of a path.
     *
     * @param separator Separator string
     */
    public void setSeparator(String separator) {
      this.separator = separator;
    }

    /**
     * Gets the string representing all possible components in one level in a path.
     *
     * @return String for all in level.
     */
    public String getAllInLevel() {
      return allInLevel;
    }

    /**
     * Sets the string representing all possible components in one level in a path.
     *
     * @param allInLevel String for all in level.
     */
    public void setAllInLevel(String allInLevel) {
      this.allInLevel = allInLevel;
    }

    /**
     * Gets the string representing zero or more levels in a path.
     *
     * @return String for many lavels.
     */
    public String getManyLevels() {
      return manyLevels;
    }

    /**
     * Sets the string representing zero or more levels in a path.
     *
     * @param manyLevels String for many lavels.
     */
    public void setManyLevels(String manyLevels) {
      this.manyLevels = manyLevels;
    }

    /**
     * Gets the regular expressions for the types recognized by the path resolver.
     *
     * @return List of regular expressions
     */
    public List<String> getWhiteList() {
      return whiteList;
    }

    /**
     * sets the regular expressions for the types recognized by the path resolver.
     *
     * @param whiteList List of regular expressions
     */
    public void setWhiteList(List<String> whiteList) {
      this.whiteList = whiteList;
    }

    /**
     * Checks, whether unresolvable concrete {@link Path Paths} are mapped to {@code null}.
     *
     * @return If {@code true}, then not existing concrete {@link Path Paths} resolve to a {@link Resolved} with value {@code null}. If
     * {@code false} it resolves to an {@link Resolved#empty() empty} {@code Resolved}
     */
    public boolean isMapUnresolvableToNull() {
      return mapUnresolvableToNull;
    }

    /**
     * Sets, whether unresolvable concrete {@link Path Paths} are mapped to {@code null}.
     *
     * @param mapUnresolvableToNull If {@code true}, then not existing concrete {@link Path Paths} resolve to a {@link Resolved} with value
     *                              {@code null}. If {@code false} it resolves to an {@link Resolved#empty() empty} {@code Resolved}
     */
    public void setMapUnresolvableToNull(boolean mapUnresolvableToNull) {
      this.mapUnresolvableToNull = mapUnresolvableToNull;
    }
  }

}
