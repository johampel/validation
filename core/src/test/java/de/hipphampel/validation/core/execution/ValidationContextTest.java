package de.hipphampel.validation.core.execution;

import de.hipphampel.validation.core.utils.Pair;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationContextTest {

  @Test
  public void sharedObject_isAvailableInCopies() {
    ValidationContext context = new ValidationContext();
    Pair<String,String> pair = new Pair<>("a","b");
    context.getOrCreateSharedExtension(Pair.class, ignore -> pair);

    assertThat(context.getSharedExtension(Pair.class)).isSameAs(pair);
    assertThat(context.copy().getSharedExtension(Pair.class)).isSameAs(pair);
  }
  @Test
  public void localObject_isNotAvailableInCopies() {
    ValidationContext context = new ValidationContext();
    Pair<String,String> pair = new Pair<>("a","b");
    context.getOrCreateLocalExtension(Pair.class, ignore -> pair);

    assertThat(context.getLocalExtension(Pair.class)).isSameAs(pair);
    assertThat(context.copy().knowsLocalExtension(Pair.class)).isFalse();
  }
}
