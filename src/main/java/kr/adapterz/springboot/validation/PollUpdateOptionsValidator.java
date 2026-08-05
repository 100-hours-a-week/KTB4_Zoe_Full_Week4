package kr.adapterz.springboot.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kr.adapterz.springboot.dto.PollUpdateRequestDto;

import java.util.List;

public class PollUpdateOptionsValidator
        implements ConstraintValidator<ValidPollUpdateOptions, List<PollUpdateRequestDto.Option>> {

    @Override
    public boolean isValid(List<PollUpdateRequestDto.Option> options, ConstraintValidatorContext context) {
        if (options == null) {
            return true;
        }

        return options.stream()
                .allMatch(option -> option != null
                        && option.getContent() != null
                        && option.getContent().strip().length() <= 30);
    }
}
