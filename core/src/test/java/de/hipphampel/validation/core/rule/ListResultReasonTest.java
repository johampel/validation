package de.hipphampel.validation.core.rule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

public class ListResultReasonTest {

  @Test
  public void flatten() {
    ResultReason reason = new ListResultReason(List.of(
        new ListResultReason(List.of(
            new StringResultReason("a"),
            new StringResultReason("b")
        )),
        new StringResultReason("c"),
        new StringResultReason("d")
    ));

    assertThat(reason.flatten().toList()).containsExactly(
        new StringResultReason("a"),
        new StringResultReason("b"),
        new StringResultReason("c"),
        new StringResultReason("d")
    );
  }

}
