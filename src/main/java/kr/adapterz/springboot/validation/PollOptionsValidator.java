package kr.adapterz.springboot.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class PollOptionsValidator implements ConstraintValidator<ValidPollOptions, List<String>> {

    private static final int MAX_OPTION_LENGTH = 30;

    @Override
    public boolean isValid(List<String> options, ConstraintValidatorContext context) {
        if (options == null) {
            return true;
        }

        return options.stream().allMatch(this::isValidOption);
    }

    private boolean isValidOption(String option) {
        if (option == null) {
            return false;
        }

        int normalizedLength = option.strip().length();
        return normalizedLength >= 1 && normalizedLength <= MAX_OPTION_LENGTH;
    }
}
