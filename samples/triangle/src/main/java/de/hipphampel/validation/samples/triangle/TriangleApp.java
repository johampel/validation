package de.hipphampel.validation.samples.triangle;

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
import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.event.payloads.RuleFinishedPayload;
import de.hipphampel.validation.core.execution.SimpleRuleExecutor;
import de.hipphampel.validation.core.provider.AnnotationRuleRepository;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.ReportFormatter;
import de.hipphampel.validation.core.report.ReportReporter;
import de.hipphampel.validation.core.rule.ResultCode;
import de.hipphampel.validation.samples.triangle.model.Polygon;
import de.hipphampel.validation.samples.triangle.rules.TriangleRules;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriangleApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(TriangleApp.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static SubscribableEventPublisher eventPublisher = new SubscribableEventPublisher();
  private static final Validator<Report> validator = ValidatorBuilder.newBuilder()
      .withRuleRepository(AnnotationRuleRepository.ofClass(TriangleRules.class))
     .withRuleExecutor(new SimpleRuleExecutor())
      .withReporter(ReportReporter::new)
//      .withEventPublisher(eventPublisher)
      .build();

  public static void main(String[] args) {
    try {
      eventPublisher.subscribe(TriangleApp::logEvent);
      Map<String,Object> parameters= Map.of(
          "additionalValidations", Set.of("additional:.*")
      );
      List<Polygon> polygons = objectMapper.readValue(new File(args[0]),
          new TypeReference<List<Polygon>>() {
          });

      for (Polygon polygon : polygons) {
        System.out.println("Validating polygon '" + polygon + "'...:");
        Report report = validator.validate(polygon, RuleSelector.of("polygon:allRules"), parameters);
        new ReportFormatter.Csv().format(report.filter(ResultCode.OK), System.out);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

  private static void logEvent(Event<?> evt) {
    if (evt.payload() instanceof RuleFinishedPayload rfp) {
      LOGGER.info("Rule {} for {} done: {} ({}ns)", rfp.rule().getId(), rfp.facts(), rfp.result(),
          rfp.nanos());
    }
  }

}
