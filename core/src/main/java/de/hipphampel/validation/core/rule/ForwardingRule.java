package de.hipphampel.validation.core.rule;

import de.hipphampel.validation.core.execution.ValidationContext;

/**
 * Supplemental interface for {@link Rule Rules} that forward the rule execution to zero, one or many other {@code Rules}.
 * <p>
 * A {@code ForwardingRule} has typically no own business logic to validate an object, but it orchestrates other {@code Rules} meaning that
 * it selects zero, one or more other {@code Rules} and executes them. The exact mechanism for selecting the other {@code Rules} is not part
 * of this interface, but typically implemented in the {@link #validate(ValidationContext, Object) validate} method.
 * <p>
 * If there are none or more than one {@code Rules} to execute, it is common to all {@code ForwardingRules} that they need to report a
 * {@link Result}, if no {@code Rule} or merge {@code Results} if more than one {@code Rule} is executed. For these purposes the interface
 * provides some helper methods with a default implementation.
 *
 * @param <T>
 */
public interface ForwardingRule<T> extends Rule<T> {

  /**
   * Returns the {@link Result}, if no {@link Rule} is executed by this {@code ForwardingRule}.
   * <p>
   * By default, this returns {@link Result#ok() ok}
   *
   * @return The {@code Result}
   */
  default Result noRuleResult() {
    return Result.ok();
  }

  /**
   * Called to merge the results of two validations.
   * <p>
   * Since a {@link Rule} returns only one {@link Result} but this executes potentially more than one {@code Rule}, this method is used
   * merge two into one. The merged result has no reason by default
   * <p>
   * This returns a {@code Result} with the highest severity of the two inputs.
   *
   * @param first  First {@code Result}
   * @param second Second {@code Result}
   * @return Merged {@code Result}
   */
  default Result mergeResults(Result first, Result second) {
    return new Result(ResultCode.getHighestSeverityOf(first.code(), second.code()), null);
  }

}
