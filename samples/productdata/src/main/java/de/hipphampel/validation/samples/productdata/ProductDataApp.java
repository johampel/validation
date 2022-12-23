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
package de.hipphampel.validation.samples.productdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hipphampel.validation.core.Validator;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.provider.RuleSelectorBuilder;
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.ReportFormatter;
import de.hipphampel.validation.core.rule.ForwardingRule;
import de.hipphampel.validation.core.rule.ResultCode;
import de.hipphampel.validation.samples.productdata.model.product.Product;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * The product data example.
 *
 * You may pass the name of a JSON formatted file with product data to this application
 */
@SpringBootApplication
@Configuration
public class ProductDataApp implements CommandLineRunner {

  private final Validator validator;
  private final ObjectMapper objectMapper;
  private final ThreadPoolTaskExecutor executor;

  public ProductDataApp(Validator validator, ObjectMapper objectMapper, ThreadPoolTaskExecutor executor) {
    this.validator = validator;
    this.objectMapper = objectMapper;
    this.executor = executor;
  }

  public static void main(String[] args) {
    SpringApplication.run(ProductDataApp.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    Product product = objectMapper.readValue(ProductDataApp.class.getResource(args[0]), Product.class);
    validateAndPrintReport(product);
    executor.shutdown(); // Terminate executor so that the application terminates as well
  }

  private void validateAndPrintReport(Product product) {
    Report report = validate(product);
    printReport(report);
  }

  private Report validate(Product product) {
    RuleSelector selector = RuleSelectorBuilder.withPredicate(rule -> Boolean.TRUE.equals(rule.getMetadata().get("master"))).build();
    return validator.validate(product, selector);
  }

  private void printReport(Report report) {
    ReportFormatter formatter = new ReportFormatter.Simple();
    report = report
        .filter(entry -> !(entry.rule() instanceof ForwardingRule<?>))
        .filter(ResultCode.FAILED);
    formatter.format(report, System.out);
  }
}
