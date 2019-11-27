package com.nzxpc.handler.util.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class PositiveDecimalValidator implements ConstraintValidator<PositiveDecimal, Object> {
    private boolean canBeZero;
    private int scale;

    @Override
    public void initialize(PositiveDecimal constraintAnnotation) {
        scale = constraintAnnotation.scale();
        canBeZero = constraintAnnotation.canBeZero();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return ValidateUtil.checkDecimal(canBeZero, false, (BigDecimal) value, scale, false);
    }
}
