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
package de.hipphampel.validation.core.path;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CollectionPathResolverTest {

  final Object SAMPLE = new TreeMap<>(Map.of(
      "value", "v1",
      "list", List.of("v1", "v3", "v4"),
      "set", new TreeSet<>(Set.of("v5", "v6", "v7")),
      "map", new TreeMap<>(Map.of("a", "v8", "b", "v9", "c", "v10")),
      "nested", new TreeMap<>(Map.of(
          "value", "v11",
          "list", List.of("v12", "v13", "v14"),
          "set", new TreeSet<>(Set.of("v15", "v16", "v17")),
          "map", new TreeMap<>(Map.of("a", "v18", "b", "v19", "c", "v20")),
          "nested", new TreeMap<>(Map.of(
              "value", "v21",
              "list", List.of("v22", "v23", "v24"),
              "set", new TreeSet<>(Set.of("v25", "v26", "v27")),
              "map", new TreeMap<>(Map.of("a", "v28", "b", "v29", "c", "v30"))
          ))
      ))));

  @Test
  public void ctor() {
    CollectionPathResolver resolver = new CollectionPathResolver();
    assertThat(resolver.getSeparator()).isEqualTo("/");
    assertThat(resolver.getAllInLevel()).isEqualTo("*");
    assertThat(resolver.getManyLevels()).isEqualTo("**");

    resolver = new CollectionPathResolver("a", "b", "c");
    assertThat(resolver.getSeparator()).isEqualTo("a");
    assertThat(resolver.getAllInLevel()).isEqualTo("b");
    assertThat(resolver.getManyLevels()).isEqualTo("c");
  }

  @ParameterizedTest
  @CsvSource({
      "'', '{list=[v1, v3, v4], map={a=v8, b=v9, c=v10}, nested={list=[v12, v13, v14], map={a=v18, b=v19, c=v20}, nested={list=[v22, v23, v24], map={a=v28, b=v29, c=v30}, set=[v25, v26, v27], value=v21}, set=[v15, v16, v17], value=v11}, set=[v5, v6, v7], value=v1}'",
      "'value', 'v1'",
      "'list', '[v1, v3, v4]'",
      "'list/1', 'v3'",
      "'list/a',",
      "'list/3',",
      "'list/-1',",
      "'set', '[v5, v6, v7]'",
      "'set/2', 'v7'",
      "'set/a',",
      "'set/3',",
      "'set/-1',",
      "'map', '{a=v8, b=v9, c=v10}'",
      "'map/c', 'v10'",
      "'map/d',",
      "'nested/nested/value', 'v21'",
      "'nested/nested/map/c', 'v30'",
      "'nested/*/map/c',",
      "'**',",
  })
  public void resolve(String pathStr, String expected) {
    CollectionPathResolver resolver = new CollectionPathResolver();
    Path path = resolver.parse(pathStr);
    Resolved<?> resolved = resolver.resolve(SAMPLE, path);
    assertThat(String.valueOf(resolved.orElse(null))).isEqualTo(String.valueOf(expected));
  }

  @ParameterizedTest
  @CsvSource({
      "'',            1,  '[]'",
      "'notFound',    0,  '[]'",
      "'value',       1,  '[value]'",
      "'*',           5,  '[list, map, nested, set, value]'",
      "'list/*',      3,  '[list/0, list/1, list/2]'",
      "'list/3',      0,  '[]'",
      "'list/2',      1,  '[list/2]'",
      "'set/*',       3,  '[set/0, set/1, set/2]'",
      "'set/3',       0,  '[]'",
      "'set/2',       1,  '[set/2]'",
      "'map/*',       3,  '[map/a, map/b, map/c]'",
      "'map/d',       0,  '[]'",
      "'map/a',       1,  '[map/a]'",
      "'**',          42, '[, list, list/0, list/1, list/2, map, map/a, map/b, map/c, nested, nested/list, nested/list/0, nested/list/1, nested/list/2, nested/map, nested/map/a, nested/map/b, nested/map/c, nested/nested, nested/nested/list, nested/nested/list/0, nested/nested/list/1, nested/nested/list/2, nested/nested/map, nested/nested/map/a, nested/nested/map/b, nested/nested/map/c, nested/nested/set, nested/nested/set/0, nested/nested/set/1, nested/nested/set/2, nested/nested/value, nested/set, nested/set/0, nested/set/1, nested/set/2, nested/value, set, set/0, set/1, set/2, value]'",
      "'**/0',        6,  '[list/0, nested/list/0, nested/nested/list/0, nested/nested/set/0, nested/set/0, set/0]'",
      "'nested/**/1', 4,  '[nested/list/1, nested/nested/list/1, nested/nested/set/1, nested/set/1]'",
  })
  public void resolvePattern(String patternStr, int expectedCount, String expected) {
    CollectionPathResolver resolver = new CollectionPathResolver();
    Path pattern = resolver.parse(patternStr);

    List<? extends Path> resolved = resolver.resolvePattern(SAMPLE, pattern).toList();
    assertThat(resolved).hasSize(expectedCount);
    assertThat(resolved.toString()).isEqualTo(expected);
  }
}
