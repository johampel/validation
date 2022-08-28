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

import de.hipphampel.validation.core.example.Unit;
import de.hipphampel.validation.core.example.Worker;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class BeanPathResolverTest {

  private static final Worker TIM_LEAD = new Worker("Tim ", "Lead", 1000.0,
      new TreeMap<>(Map.of("development", "lead", "management", "consultant")));
  private static final Worker FRITZ_BOX = new Worker("Fritz ", "Box", 800.0,
      new TreeMap<>(Map.of("development", "member")));
  private static final Worker JOE_DOE = new Worker("Joe ", "Doe", 1000.0,
      new TreeMap<>(Map.of("development", "external")));
  private static final Worker THE_BOSS = new Worker("The ", "Boss", 2000.0,
      new TreeMap<>(Map.of("management", "lead")));
  private static final Unit DEVELOPMENT = new Unit("development",
      List.of(TIM_LEAD, FRITZ_BOX, JOE_DOE));
  private static final Unit MANAGEMENT = new Unit("management",
      List.of(TIM_LEAD, THE_BOSS));
  private static final Unit SAMPLE = new Unit("Company", List.of(DEVELOPMENT, MANAGEMENT));

  @ParameterizedTest
  @CsvSource({
      "'', 'Unit[name=Company, members=[Unit[name=development, members=[Worker[firstName=Tim , lastName=Lead, salary=1000.0, roles={development=lead, management=consultant}], Worker[firstName=Fritz , lastName=Box, salary=800.0, roles={development=member}], Worker[firstName=Joe , lastName=Doe, salary=1000.0, roles={development=external}]]], Unit[name=management, members=[Worker[firstName=Tim , lastName=Lead, salary=1000.0, roles={development=lead, management=consultant}], Worker[firstName=The , lastName=Boss, salary=2000.0, roles={management=lead}]]]]]'",
      "'name', 'Company'",
      "'members/0', 'Unit[name=development, members=[Worker[firstName=Tim , lastName=Lead, salary=1000.0, roles={development=lead, management=consultant}], Worker[firstName=Fritz , lastName=Box, salary=800.0, roles={development=member}], Worker[firstName=Joe , lastName=Doe, salary=1000.0, roles={development=external}]]]'",
      "'members/2',",
      "'members/0/name', 'development'",
      "'members/0/members/0/roles/development', 'lead'",
      "'unknown',",
      "'members/*',",
      "'**',",
  })
  public void resolve(String pathStr, String expected) {
    BeanPathResolver resolver = new BeanPathResolver();
    Path path = resolver.parse(pathStr);
    Resolved<?> resolved = resolver.resolve(SAMPLE, path);
    assertThat(String.valueOf(resolved.orElse(null))).isEqualTo(String.valueOf(expected));
  }

  @ParameterizedTest
  @CsvSource({
      "'',                       1,  '[]'",
      "'notFound',               0,  '[]'",
      "'name',                   1,  '[name]'",
      "'*',                      2,  '[members, name]'",
      "'members/*',              2,  '[members/0, members/1]'",
      "'members/*/members/*',    5,  '[members/0/members/0, members/0/members/1, members/0/members/2, members/1/members/0, members/1/members/1]'",
      "'**/members',             3,  '[members, members/0/members, members/1/members]'",
  })
  public void resolvePattern(String patternStr, int expectedCount, String expected) {
    BeanPathResolver resolver = new BeanPathResolver();
    Path pattern = resolver.parse(patternStr);

    List<? extends Path> resolved = resolver.resolvePattern(SAMPLE, pattern).toList();
    assertThat(resolved).hasSize(expectedCount);
    assertThat(resolved.toString()).isEqualTo(expected);
  }


}
