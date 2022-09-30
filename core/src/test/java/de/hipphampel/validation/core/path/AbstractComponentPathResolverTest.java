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

import de.hipphampel.validation.core.path.ComponentPath.Component;
import de.hipphampel.validation.core.utils.Pair;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

// NOTE: resolve and resolvePattern are implicitly tested by CollectionPathResolverTest
public class AbstractComponentPathResolverTest {

  @Test
  public void ctor() {
    TestPathResolver resolver = new TestPathResolver();
    assertThat(resolver.getSeparator()).isEqualTo("/");
    assertThat(resolver.getAllInLevel()).isEqualTo("*");
    assertThat(resolver.getManyLevels()).isEqualTo("**");

    resolver = new TestPathResolver("a", "b", "c");
    assertThat(resolver.getSeparator()).isEqualTo("a");
    assertThat(resolver.getAllInLevel()).isEqualTo("b");
    assertThat(resolver.getManyLevels()).isEqualTo("c");
  }

  @Test
  public void selfPath() {
    TestPathResolver resolver = new TestPathResolver("..", "??", "**");
    assertThat(resolver.selfPath()).isEqualTo(ComponentPath.empty());
  }

  @Test
  public void parse() {
    TestPathResolver resolver = new TestPathResolver("..", "??", "**");
    String str = "a..??....**..b..";
    Path path = resolver.parse(str);
    assertThat(path).isInstanceOf(ComponentPath.class);
    assertThat(((ComponentPath) path).getComponents()).containsExactly(
        new ComponentPath.Component(ComponentPath.ComponentType.NamedLevel, "a"),
        new ComponentPath.Component(ComponentPath.ComponentType.AnyInLevel, "??"),
        new ComponentPath.Component(ComponentPath.ComponentType.NamedLevel, ""),
        new ComponentPath.Component(ComponentPath.ComponentType.ManyLevels, "**"),
        new ComponentPath.Component(ComponentPath.ComponentType.NamedLevel, "b"),
        new ComponentPath.Component(ComponentPath.ComponentType.NamedLevel, "")
    );

    // Empty case
    path = resolver.parse("");
    assertThat(path).isInstanceOf(ComponentPath.class);
    assertThat(((ComponentPath) path).getComponents()).containsExactly();
  }

  @Test
  public void toStringForPath() {
    TestPathResolver resolver = new TestPathResolver("..", "??", "**");
    String str = "a..??....**..b..";
    Path path = resolver.parse(str);
    assertThat(resolver.toString(path)).isEqualTo(str);
  }

  private static class TestPathResolver extends AbstractComponentPathResolver {

    public TestPathResolver(String separator, String allInLevel, String manyLevels) {
      super(separator, allInLevel, manyLevels);
    }

    public TestPathResolver() {
    }

    @Override
    protected Resolved<Object> resolveLevel(Object ref, Component component) {
      return Resolved.empty();
    }

    @Override
    protected Stream<Pair<Component, Object>> resolvePatternLevel(Object ref, Component component) {
      return Stream.empty();
    }
  }
}
