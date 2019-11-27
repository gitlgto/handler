package com.nzxpc.handler.util.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class PositiveRateValidator implements ConstraintValidator<PositiveRate, Object> {
    private boolean canBeZero;
    private boolean canBeOverOne;

    @Override
    public void initialize(PositiveRate constraintAnnotation) {
        canBeZero = constraintAnnotation.canBeZero();
        canBeOverOne = constraintAnnotation.canBeOverOne();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        return ValidateUtil.checkDecimal(canBeZero, false, (BigDecimal) value, 4, !canBeOverOne);
    }
}
