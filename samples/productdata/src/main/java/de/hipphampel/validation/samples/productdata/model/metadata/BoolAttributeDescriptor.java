package de.hipphampel.validation.samples.productdata.model.metadata;

import org.springframework.core.convert.TypeDescriptor;

public record BoolAttributeDescriptor(String name, boolean mandatory) implements AttributeDescriptor {

  @Override
  public TypeDescriptor type() {
    return TypeDescriptor.valueOf(Boolean.class);
  }
}
