package de.hipphampel.validation.samples.productdata.model.metadata;

import org.springframework.core.convert.TypeDescriptor;

public record IntegerAttributeDescriptor(String name, boolean mandatory, Integer minValue, Integer maxValue) implements AttributeDescriptor {

  @Override
  public TypeDescriptor type() {
    return TypeDescriptor.valueOf(Integer.class);
  }
}
