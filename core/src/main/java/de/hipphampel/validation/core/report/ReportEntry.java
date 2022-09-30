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
package de.hipphampel.validation.core.report;

import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

/**
 * An entry within a {@link Report} describing the result of a {@link Rule}
 *
 * @param path   The {@link Path}, might be {@code null}
 * @param rule   The {@code Rule}
 * @param result The {@link Result}
 */
public record ReportEntry(Path path, Rule rule, Result result) {

  /**
   * Enum identifying a field of the {@link ReportEntry} class.
   */
  public enum Field {
    RULE(Comparator.comparing(e-> e.rule.getId()), e-> e.rule.getId()),
    PATH(Comparator.comparing(e -> String.valueOf(e.path())), ReportEntry::path),
    CODE(Comparator.comparing(e -> e.result().code().ordinal()), e -> e.result().code()),
    REASON(Comparator.comparing(e -> String.valueOf(e.result().reason())),
        e -> e.result().reason());

    private final Comparator<ReportEntry> comparator;
    private final Function<ReportEntry, Object> getter;

    Field(Comparator<ReportEntry> comparator, Function<ReportEntry, Object> getter) {
      this.comparator = Objects.requireNonNull(comparator);
      this.getter = Objects.requireNonNull(getter);
    }

    /**
     * Get the {@link Comparator} for the field.
     *
     * @return The {@code Comparator}
     */
    public Comparator<ReportEntry> getComparator() {
      return comparator;
    }

    /**
     * Get the getter for the field.
     *
     * @return The getter
     */
    public Function<ReportEntry, Object> getGetter() {
      return getter;
    }

    /**
     * Creates a {@link Sort} for this field with ascending ordering.
     *
     * @return The {@code Sort}
     */
    public Sort asc() {
      return new Sort(this, false);
    }

    /**
     * Creates a {@link Sort} for this field with descending ordering.
     *
     * @return The {@code Sort}
     */
    public Sort desc() {
      return new Sort(this, true);
    }
  }

  /**
   * Describes  a sorting.
   *
   * @param field      The {@link Field} to sort
   * @param descending The sort direction, {@code true} if descending
   */
  public record Sort(Field field, boolean descending) {

    public Sort {
      Objects.requireNonNull(field);
    }
  }
}
