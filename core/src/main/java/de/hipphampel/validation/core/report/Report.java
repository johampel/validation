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

import de.hipphampel.validation.core.report.ReportEntry.Sort;
import de.hipphampel.validation.core.rule.ResultCode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a basic validation report.
 * <p>
 * A {@code Report} consists of {@link ReportEntry ReportEntries}, whereas each entry describes the
 * outcome of a single rule execution. The {@code Report} provides methods to categorize an query
 * these entries.
 *
 * @param entries The {@code ReportEntries}
 * @see ReportEntry
 */
public record Report(Set<ReportEntry> entries) {

  public Report {
    Objects.requireNonNull(entries);
  }

  /**
   * Checks, whether {@code Report} is empty.
   * <p>
   * The {@code Report} is empty, if it has no entries
   *
   * @return {@code true} if empty.
   */
  public boolean isEmpty() {
    return entries.isEmpty();
  }

  /**
   * Creates a new {@code Report} containing only entries that match the given {@code predicate}
   *
   * @param predicate The predicate
   * @return Anew {@code Report}
   */
  public Report filter(Predicate<ReportEntry> predicate) {
    return new Report(entries.stream().filter(predicate).collect(Collectors.toSet()));
  }

  /**
   * Creates a new {@code Report} containing only entries having at least the {@code minSeverity}.
   * <p>
   * This means that all {@link #entries()} () entries} are filtered so that their
   * {@link ResultCode} has at least the given {@code minSeverity}.
   *
   * @param minSeverity the minimum {@link ResultCode}
   * @return The filtered {@code Report}
   */
  public Report filter(ResultCode minSeverity) {
    return filter(e -> !e.result().code().hasLowerSeverityThan(minSeverity));
  }

  /**
   * Categorizes the {@link ReportEntry ReportEntries} according to the geiven
   * {@code discriminator}.
   * <p>
   * This produces a map of sub reports, each {@code Report} contains only those entries that match
   * the value returned by the {@code discriminator}
   *
   * @param discriminator The function to categorize the entries
   * @param <T>           The return type of the the function
   * @return The categorized reports.
   */
  public <T> Map<T, Report> categorize(Function<ReportEntry, T> discriminator) {
    return entries.stream()
        .collect(Collectors.groupingBy(
            discriminator,
            Collectors.collectingAndThen(Collectors.toSet(), Report::new)));
  }


  /**
   * Gets the {@link ResultCode} with the highest severity in this {@code Report}.
   *
   * @return The highest severity
   */
  public Optional<ResultCode> getResultCodeWithHighestSeverity() {
    return entries.stream()
        .map(entry -> entry.result().code())
        .reduce(ResultCode::getHighestSeverityOf);
  }

  /**
   * Gets the {@link ReportEntry ReportEntries} in a sorted fashion.
   *
   * @param sorts The sorting
   * @return The sorted entries
   */
  public List<ReportEntry> entriesSorted(Sort... sorts) {
    return entriesSorted(Arrays.asList(sorts));
  }

  /**
   * Gets the {@link ReportEntry ReportEntries} in a sorted fashion.
   *
   * @param sorts The sorting
   * @return The sorted entries
   */
  public List<ReportEntry> entriesSorted(List<Sort> sorts) {
    return entries.stream()
        .sorted(new ReportEntryComparator(sorts))
        .toList();
  }

  private record ReportEntryComparator(List<Sort> sorts) implements Comparator<ReportEntry> {

    @Override
    public int compare(ReportEntry e1, ReportEntry e2) {
      if (e1 == e2) {
        return 0;
      }

      for (Sort sort : sorts) {
        int res = sort.field().getComparator().compare(e1, e2);
        if (res != 0) {
          return sort.descending() ? -res : res;
        }
      }
      return Integer.compare(Objects.hashCode(e1), Objects.hashCode(e2));
    }
  }
}
