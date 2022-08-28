package de.hipphampel.validation.core.utils;

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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CollectionUtilsTest {

  @Test
  public void streamOf() {
    Iterator<Integer> iterator = List.of(1, 2, 3).iterator();

    assertThat(CollectionUtils.streamOf(iterator)).containsExactly(1, 2, 3);
  }

  @ParameterizedTest
  @CsvSource({
      "abcde, 0,",
      "abcde, 1, 'a,b,c,d,e'",
      "abcde, 2, 'ab,ac,ad,ae,bc,bd,be,cd,ce,de'",
      "abcde, 3, 'abc,abd,abe,acd,ace,ade,bcd,bce,bde,cde'",
      "abcde, 4, 'abcd,abce,abde,acde,bcde'",
      "abcde, 5, 'abcde'",
      "abcde, 6,",
  })
  public void streamOfPossibleTuples(String listString, int n, String resultString) {
    List<Integer> list = stringToList(listString);
    List<List<Integer>> results = resultString == null ? List.of() :
        Arrays.stream(resultString.split(",")).map(this::stringToList).toList();
    assertThat(CollectionUtils.streamOfPossibleTuples(list, n).toList()).isEqualTo(results);
  }

  @ParameterizedTest
  @CsvSource({
      "a,",
      "ab,   'ab'",
      "abc,  'ab,ac,bc'",
      "abcd, 'ab,ac,ad,bc,bd,cd'",
  })
  public void streamOfPossiblePairs(String listString, String resultString) {
    List<Integer> list = stringToList(listString);
    List<Pair<Integer, Integer>> results = resultString == null ? List.of() :
        Arrays.stream(resultString.split(",")).map(this::stringToPair).toList();
    assertThat(CollectionUtils.streamOfPossiblePairs(list)
        .toList()).isEqualTo(results);
  }

  private List<Integer> stringToList(String str) {
    return str.chars().boxed().toList();
  }

  private Pair<Integer, Integer> stringToPair(String str) {
    return Pair.of((int) str.charAt(0), (int) str.charAt(1));
  }
}
