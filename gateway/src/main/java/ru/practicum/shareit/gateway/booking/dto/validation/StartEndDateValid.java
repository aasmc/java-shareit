package ru.practicum.shareit.gateway.booking.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = StartEndDateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface StartEndDateValid {
    String message() default "End date cannot be before start date.";

    String start();

    String endField();

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
