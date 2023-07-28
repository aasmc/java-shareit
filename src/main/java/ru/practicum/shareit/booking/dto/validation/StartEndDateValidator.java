package ru.practicum.shareit.booking.dto.validation;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import ru.practicum.shareit.error.ServiceException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class StartEndDateValidator implements ConstraintValidator<StartEndDateValid, Object> {

    private String startField;
    private String endField;

    @Override
    public void initialize(StartEndDateValid constraintAnnotation) {
        this.startField = constraintAnnotation.start();
        this.endField = constraintAnnotation.endField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        LocalDateTime start = (LocalDateTime) new BeanWrapperImpl(value).getPropertyValue(startField);
        LocalDateTime end = (LocalDateTime) new BeanWrapperImpl(value).getPropertyValue(endField);
        if (start == null || end == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "Start date and end date cannot be null");
        }
        return start.isBefore(end);
    }
}
