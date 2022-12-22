package de.hipphampel.validation.core.rule;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link ResultReason} that cantains more than one {@code ResultReason}
 *
 * @param reasons List of reasons.
 */
public record ListResultReason(List<ResultReason> reasons) implements ResultReason {

  public ListResultReason {
    Objects.requireNonNull(reasons);
  }

  @Override
  public Stream<ResultReason> flatten() {
    return reasons.isEmpty() ? Stream.of(this) : reasons.stream().flatMap(ResultReason::flatten);
  }
}
