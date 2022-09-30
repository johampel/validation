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

import de.hipphampel.validation.core.report.ReportEntry.Field;
import de.hipphampel.validation.core.report.ReportEntry.Sort;
import de.hipphampel.validation.core.rule.ResultCode;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Allows to format a {@link Report} to differebt kinds of output formats.
 */
public interface ReportFormatter {

  /**
   * Writes {@code report} to {@code out}
   *
   * @param report The {@link Report}
   * @param out    Output destination
   */
  default void format(Report report, OutputStream out) {
    format(report, new PrintWriter(out));
  }

  /**
   * Writes {@code report} to {@code out}
   *
   * @param report The {@link Report}
   * @param out    Output destination
   */
  default void format(Report report, PrintStream out) {
    format(report, new PrintWriter(out));
  }

  /**
   * Writes {@code report} to {@code out}
   *
   * @param report The {@link Report}
   * @param out    Output destination
   */
  default void format(Report report, Writer out) {
    format(report, new PrintWriter(out));
  }

  /**
   * Writes {@code report} to {@code out}
   *
   * @param report The {@link Report}
   * @param out    Output destination
   */
  void format(Report report, PrintWriter out);

  /**
   * Simple formatter.
   * <p>
   * Writes a simple float test representation of the report.
   */
  class Simple implements ReportFormatter {

    @Override
    public void format(Report report, PrintWriter out) {
      printReport(report, out);
      out.flush();
    }

    void printReport(Report report, PrintWriter out) {
      out.format("Result: %s\n",
          report.getResultCodeWithHighestSeverity().orElse(ResultCode.OK));
      report.entries().stream()
          .sorted(
              Comparator.comparing(e -> (e.path() == null ? "" : "/" + e.path()) + " " + e.rule()))
          .forEach(e -> printReportEntry(e, out));
    }

    void printReportEntry(ReportEntry entry, PrintWriter out) {
      if (entry.path() != null) {
        out.format("- at path '%s': %s - ", entry.path(), entry.rule().getId());
      } else {
        out.format("- at root: %s - ", entry.rule().getId());
      }
      if (entry.result().reason() != null) {
        out.format("%s (%s)\n",
            entry.result().code(),
            entry.result().reason());
      } else {
        out.format("%s\n",
            entry.result().code());
      }

    }
  }

  /**
   * A {@link ReportFormatter} producing CSV compatible output.
   * <p>
   * Upon construction, it is possible to specify most aspects of the CSV to generate
   */
  class Csv implements ReportFormatter {

    private final List<Field> fields;
    private final List<Sort> sorts;
    private final char quote;
    private final char separator;
    private final boolean withHeader;

    /**
     * Constructor.
     *
     * @param fields     List of {@link Field Fields} of the report
     * @param sorts      List of {@link Sort Sorts} for sorting the entries
     * @param quote      Character to use as quote.
     * @param separator  Character to use as field separator
     * @param withHeader Indicates, whether out with or without header
     */
    public Csv(List<Field> fields, List<Sort> sorts, char quote, char separator,
        boolean withHeader) {
      this.fields = fields;
      this.sorts = sorts;
      this.quote = quote;
      this.separator = separator;
      this.withHeader = withHeader;
    }

    /**
     * Default constructor.
     */
    public Csv() {
      this(
          List.of(Field.RULE, Field.PATH, Field.CODE, Field.REASON),
          List.of(Field.CODE.desc(), Field.RULE.asc(), Field.PATH.asc()),
          '"',
          ';',
          true
      );
    }

    @Override
    public void format(Report report, PrintWriter out) {
      if (withHeader) {
        printHeader(out);
      }

      for (ReportEntry entry : report.entriesSorted(sorts)) {
        printEntry(out, entry);
      }
      out.flush();
    }

    void printHeader(PrintWriter out) {
      out.println(
          fields.stream()
              .map(String::valueOf)
              .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase())
              .map(this::quoted)
              .collect(Collectors.joining("" + separator)));
    }

    void printEntry(PrintWriter out, ReportEntry entry) {
      out.println(
          fields.stream()
              .map(f -> f.getGetter().apply(entry))
              .map(this::quoted)
              .collect(Collectors.joining("" + separator)));
    }

    String quoted(Object obj) {
      StringBuilder buffer = new StringBuilder();
      buffer.append(quote);
      if (obj != null) {
        String str = String.valueOf(obj);
        for (int i = 0; i < str.length(); i++) {
          char ch = str.charAt(i);
          if (ch == quote) {
            buffer.append(quote).append(quote);
          } else {
            buffer.append(ch);
          }
        }
      }
      buffer.append(quote);
      return buffer.toString();
    }
  }
}
