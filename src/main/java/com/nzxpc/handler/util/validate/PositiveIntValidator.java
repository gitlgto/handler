package com.nzxpc.handler.util.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PositiveIntValidator implements ConstraintValidator<PositiveInt, Object> {
    private boolean canBeZero;

    @Override
    public void initialize(PositiveInt constraintAnnotation) {
        canBeZero = constraintAnnotation.canBeZero();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        String data = String.valueOf(value);
        if (canBeZero) {
            return data.matches("^\\d+$");
        } else {
            return data.matches("^[0-9]*[1-9][0-9]*$");
        }
    }
}
