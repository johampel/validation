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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ComponentPathTest {

  private final CollectionPathResolver resolver = new CollectionPathResolver();

  @ParameterizedTest
  @CsvSource({
      "'',                     false",
      "'abc',                  false",
      "'*',                    true",
      "'**',                   true",
      "'abc',                  false",
      "'abc/def',              false",
      "'abc/*',                true",
      "'abc/**',               true",
  })
  public void isPattern(String str, boolean isPattern) {
    Path path = resolver.parse(str);
    assertThat(path.isPattern()).isEqualTo(isPattern);
  }

  @ParameterizedTest
  @CsvSource({
      "'abc',                  'abc',             true",
      "'abc',                  'def',             false",
      "'abc',                  '*',               true",
      "'abc',                  '**',              true",
      "'',                     '**',              true",
      "'abc/def',              'abc/ghi',         false",
      "'abc/def',              'abc/def',         true",
      "'abc/def/ghi',          '*/def/ghi',       true",
      "'abc/def/ghi',          'abc/*/ghi',       true",
      "'abc/def/ghi',          '**',              true",
      "'abc/def/ghi',          '**/ghi',          true",
      "'abc/def/ghi',          '**/def/ghi',      true",
      "'abc/def/def/ghi',      '**/def/ghi',      true",
      "'abc/def/ghi',          '**/jkl',          false",
      "'a/b/c',                'a/**/b/c',        true",
      "'a/b/b/c',              'a/**/b/c',        true",
      "'a/b/a/b/c',            'a/**/b/c/**/c',   false",
      "'a/b/a/b/c/e/c',        'a/**/b/c/**/c',   true"
  })
  public void isMatchedBy(String str, String patternStr, boolean match) {
    Path path = resolver.parse(str);
    Path pattern = resolver.parse(patternStr);

    assertThat(path.isMatchedBy(pattern)).isEqualTo(match);
  }

  @ParameterizedTest
  @CsvSource({
      "'',    '',    ''",
      "'a/b', '',    'a/b'",
      "'',    'c/d', 'c/d'",
      "'a/b', 'c/d', 'a/b/c/d'",
  })
  public void concat(String parent, String child, String result) {
    ComponentPath parentPath = (ComponentPath) resolver.parse(parent);
    ComponentPath childPath = (ComponentPath) resolver.parse(child);
    ComponentPath resultPath = (ComponentPath) resolver.parse(result);

    ComponentPath actualPath = parentPath.concat(childPath);
    assertThat(actualPath).isEqualTo(resultPath);
  }

  @ParameterizedTest
  @CsvSource({
      "'a/b/c/d',   0, 'a/b/c/d'",
      "'a/b/c/d',   1, 'b/c/d'",
      "'a/b/c/d',   2, 'c/d'",
      "'a/b/c/d',   3, 'd'",
      "'a/b/c/d',   4, ''",
      "'a/b/c/d',   5, ",
  })
  public void subPath(String source, int level, String result) {
    ComponentPath sourcePath = (ComponentPath) resolver.parse(source);
    ComponentPath resultPath = result == null ? null : (ComponentPath) resolver.parse(result);

    if (resultPath != null) {
      ComponentPath actualPath = sourcePath.subPath(level);
      assertThat(actualPath).isEqualTo(resultPath);
    } else {
      assertThatThrownBy(() -> sourcePath.subPath(level)).isInstanceOf(
          IllegalArgumentException.class);
    }
  }
}
