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
package de.hipphampel.validation.core.rule;

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.PathResolver;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PathBasedRuleTest {

  private ValidationContext context;


  @BeforeEach
  public void beforeEach() {
    context = new ValidationContext();
  }

  @Test
  public void validate() {
    UnderTest rule = new UnderTest("test");
    Map<String, String> facts = Map.of(
        "valid", "a",
        "invalid1", "b",
        "invalid2", "c"
    );

    Result result = rule.validate(context, facts);
    assertThat(result).isEqualTo(Result.failed(new ListResultReason(List.of(
        new StringResultReason("Not containing 'a'"),
        new StringResultReason("Not containing 'a'")
    ))));
  }

  static class UnderTest extends PathBasedRule<Map<String, String>, String> {

    public UnderTest(String id) {
      super(id);
    }

    @Override
    protected Stream<? extends Path> getPaths(ValidationContext context, Map<String, String> facts) {
      PathResolver pathResolver = context.getPathResolver();
      return pathResolver.resolvePattern(facts, pathResolver.parse("*"));
    }

    @Override
    protected Result validatePath(ValidationContext context, Map<String, String> facts, String pathFacts, Path path) {
      if (pathFacts.contains("a")) {
        return Result.ok();
      }
      return Result.failed("Not containing 'a'");
    }
  }
}
