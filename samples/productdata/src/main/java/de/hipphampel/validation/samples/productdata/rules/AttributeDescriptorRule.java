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
package de.hipphampel.validation.samples.productdata.rules;

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.ComponentPath;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.rule.AbstractRule;
import de.hipphampel.validation.core.rule.ListResultReason;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.ResultReason;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.StringResultReason;
import de.hipphampel.validation.samples.productdata.model.metadata.AttributeDescriptor;
import de.hipphampel.validation.samples.productdata.model.metadata.BoolAttributeDescriptor;
import de.hipphampel.validation.samples.productdata.model.metadata.CompositeAttributeDescriptor;
import de.hipphampel.validation.samples.productdata.model.metadata.IntegerAttributeDescriptor;
import de.hipphampel.validation.samples.productdata.model.metadata.ListAttributeDescriptor;
import de.hipphampel.validation.samples.productdata.model.metadata.NumberAttributeDescriptor;
import de.hipphampel.validation.samples.productdata.model.metadata.StringAttributeDescriptor;
import de.hipphampel.validation.samples.productdata.service.MetadataService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

/**
 * A {@link Rule} that checks whether the attribute being validated conforms to an {@link AttributeDescriptor}.
 * <p>
 * The basic assumption of this implementation is that this rule is invoked via specific {@link Path Paths}, so that when it is invoked the
 * {@link ValidationContext#getCurrentPath() current path} points to the attribute ant the {@code facts} passed to the validaiton method
 * contain the attribute value. For example, the following rule declaration would do that job:
 * <pre>
 *  RuleBuilder.dispatchingRule("basic:attributes:hasCorrectType", Map.class)
 *       .forPaths("attr1", "attr2").validateWith("attribute:attributeDescriptor")
 *       .build();
 * </pre>
 * So when the rule 'basic:attributes:hasCorrectType' is executed for a {@code Map} (of attributes), it calls this rule named
 * 'attribute:attributeDescriptor' for each entry in that map and passed the attribute value as the {@code facts} parameter.
 * <p>
 * It uses then the injected {@link MetadataService} to look up the matching {@code AttributeDescriptor} and - if it is present - checks
 * whether the value can be converted to the type.
 */
@Component
public class AttributeDescriptorRule extends AbstractRule<Object> {

  private final MetadataService metadataService;
  private final ConversionService validationConversionService;

  public AttributeDescriptorRule(MetadataService metadataService, ConversionService validationConversionService) {
    super("attribute:attributeDescriptor");
    this.metadataService = metadataService;
    this.validationConversionService = validationConversionService;
  }


  @Override
  public Result validate(ValidationContext context, Object facts) {
    // This tries to figure out for which attribute name was used when call this rule.
    // The attribute name is basically the name of the last component of the path.
    Path path = context.getCurrentPath();
    if (!(path instanceof ComponentPath componentPath)) {
      return Result.failed("Rule executed with a wrong PathResolver - no predictable results!");
    }
    String attributeName = componentPath.getLastComponent().map(ComponentPath.Component::name).orElse("");
    AttributeDescriptor descriptor = metadataService.getAttributeDescriptor(attributeName).orElse(null);
    if (descriptor == null) {
      return Result.ok();
    }

    List<ResultReason> problems = new ArrayList<>();
    check(null, facts, descriptor, problems);

    return problems.isEmpty() ? Result.ok() : Result.failed(new ListResultReason(problems));
  }


  void check(String prefix, Object facts, AttributeDescriptor descriptor, List<ResultReason> problems) {
    String name = prefix == null ? descriptor.name() : prefix + "/" + descriptor.name();
    try {
      Object converted = validationConversionService.convert(
          facts,
          facts == null ? null : TypeDescriptor.valueOf(facts.getClass()),
          descriptor.type());

      if (descriptor instanceof ListAttributeDescriptor lad) {
        checkList(name, converted, lad, problems);
      } else if (descriptor instanceof CompositeAttributeDescriptor cad) {
        checkComposite(name, converted, cad, problems);
      } else if (descriptor instanceof IntegerAttributeDescriptor iad) {
        checkInteger(name, converted, iad, problems);
      } else if (descriptor instanceof NumberAttributeDescriptor nad) {
        checkNumber(name, converted, nad, problems);
      } else if (descriptor instanceof StringAttributeDescriptor sad) {
        checkString(name, converted, sad, problems);
      } else if (descriptor instanceof BoolAttributeDescriptor bad) {
        checkBoolean(name, converted, bad, problems);
      }
    } catch (ConversionException ce) {
      problems.add(new StringResultReason(String.format("Attribute '%s' has not the required type %s", name, descriptor.type())));
    }
  }

  void checkComposite(String name, Object facts, CompositeAttributeDescriptor descriptor, List<ResultReason> problems) {
    if (facts instanceof Map<?, ?> map && !map.isEmpty()) {
      for (AttributeDescriptor component : descriptor.components()) {
        check(name + "/" + component.name(), map.get(component.name()), component, problems);
      }
    } else if (descriptor.mandatory()) {
      problems.add(new StringResultReason(String.format("Attribute '%s' is mandatory but not provided", name)));
    }
  }

  void checkList(String name, Object facts, ListAttributeDescriptor descriptor, List<ResultReason> problems) {
    if ((facts instanceof List<?> list) && !list.isEmpty()) {
      int index = 0;
      for (Object entry : list) {
        check(name + "/" + index, entry, descriptor.elementType(), problems);
        index++;
      }
    } else if (descriptor.mandatory()) {
      problems.add(new StringResultReason(String.format("Attribute '%s' is mandatory but not provided", name)));
    }
  }

  void checkString(String name, Object facts, StringAttributeDescriptor descriptor, List<ResultReason> problems) {
    if ((!(facts instanceof String value) || value.isEmpty()) && descriptor.mandatory()) {
      problems.add(new StringResultReason(String.format("Attribute '%s' is mandatory but not provided", name)));
    }
  }
  void checkBoolean(String name, Object facts, BoolAttributeDescriptor descriptor, List<ResultReason> problems) {
    if (!(facts instanceof Boolean) && descriptor.mandatory()) {
      problems.add(new StringResultReason(String.format("Attribute '%s' is mandatory but not provided", name)));
    }
  }

  void checkInteger(String name, Object facts, IntegerAttributeDescriptor descriptor, List<ResultReason> problems) {
    if (facts instanceof Integer value) {
      if (descriptor.minValue() != null && descriptor.minValue() > value) {
        problems.add(new StringResultReason(String.format("Attribute '%s' is %d, which is smaller than the minimum value of %d",
            name,
            value,
            descriptor.minValue())));
      }
      if (descriptor.maxValue() != null && descriptor.maxValue() < value) {
        problems.add(new StringResultReason(String.format("Attribute '%s' is %d, which is bigger than the maximum value of %d",
            name,
            value,
            descriptor.minValue())));
      }
    } else if (descriptor.mandatory()) {
      problems.add(new StringResultReason(String.format("Attribute '%s' is mandatory but not provided", name)));
    }
  }

  void checkNumber(String name, Object facts, NumberAttributeDescriptor descriptor, List<ResultReason> problems) {
    if (facts instanceof BigDecimal value) {
      if (descriptor.minValue() != null && descriptor.minValue().compareTo(value) > 0) {
        problems.add(new StringResultReason(String.format("Attribute '%s' is %s, which is smaller than the minimum value of %s",
            name,
            value,
            descriptor.minValue())));
      }
      if (descriptor.maxValue() != null && descriptor.maxValue().compareTo(value) < 0) {
        problems.add(new StringResultReason(String.format("Attribute '%s' is %s, which is bigger than the maximum value of %s",
            name,
            value,
            descriptor.minValue())));
      }
    } else if (descriptor.mandatory()) {
      problems.add(new StringResultReason(String.format("Attribute '%s' is mandatory but not provided", name)));
    }
  }
}
