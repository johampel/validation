package de.hipphampel.validation.samples.triangle.v1;

/*-
 * #%L
 * validation-samples-triangle
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hipphampel.validation.core.Validator;
import de.hipphampel.validation.core.ValidatorBuilder;
import de.hipphampel.validation.core.event.Event;
import de.hipphampel.validation.core.event.EventListener;
import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.event.payloads.RuleFinishedPayload;
import de.hipphampel.validation.core.event.payloads.RuleStartedPayload;
import de.hipphampel.validation.core.execution.SimpleRuleExecutor;
import de.hipphampel.validation.core.provider.AnnotationRuleRepository;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.ReportFormatter;
import de.hipphampel.validation.core.rule.ResultCode;
import de.hipphampel.validation.samples.triangle.model.Polygon;
import java.io.File;
import java.util.List;

public class TriangleApp1 {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final SubscribableEventPublisher eventPublisher = new SubscribableEventPublisher();
  private static final Validator<Report> validator = ValidatorBuilder.newBuilder()
      .withRuleRepository(AnnotationRuleRepository.ofClass(TriangleRules1.class))
      .withEventPublisher(eventPublisher)
      .withRuleExecutor(new SimpleRuleExecutor())
      .build();
  private static final ReportFormatter reportFormatter = new ReportFormatter.Simple();

  public static void main(String[] args) {
    try {
      List<Polygon> polygons = objectMapper.readValue(new File(args[0]), new TypeReference<>() {
      });
      for (Polygon polygon : polygons) {
        Report report = validator.validate(polygon, RuleSelector.of("polygon:.*"));
        System.out.print(polygon == null ? null : polygon.name() + ": ");
        reportFormatter.format(report.filter(ResultCode.FAILED), System.out);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }
}
