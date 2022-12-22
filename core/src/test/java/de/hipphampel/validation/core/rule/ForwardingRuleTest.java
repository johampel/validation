package de.hipphampel.validation.core.rule;

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.validation.core.execution.ValidationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ForwardingRuleTest {


  @Test
  public void noRuleResult() {
    ForwardingRule<Object> rule = new TestRule();
    assertThat(rule.noRuleResult()).isEqualTo(Result.ok());
  }

  @ParameterizedTest
  @CsvSource({
      "OK,      OK,       OK",
      "SKIPPED, OK,       SKIPPED",
      "SKIPPED, FAILED,   FAILED",
  })
  public void mergeResults(ResultCode firstCode, ResultCode secondCode, ResultCode expectedCode) {
    ForwardingRule<Object> rule = new TestRule();
    assertThat(rule.mergeResults(new Result(firstCode, null), new Result(secondCode, null)))
        .isEqualTo(new Result(expectedCode, null));
  }


  private static class TestRule extends AbstractRule<Object> implements ForwardingRule<Object> {

    protected TestRule() {
      super("id");
    }

    @Override
    public Result validate(ValidationContext context, Object facts) {
      return Result.ok();
    }
  }
}
