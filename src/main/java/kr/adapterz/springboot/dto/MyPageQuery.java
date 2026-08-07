package kr.adapterz.springboot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyPageQuery {

    private static final String TAB_PATTERN = "written|participated|liked";

    @Pattern(
            regexp = TAB_PATTERN,
            message = "written, participated, liked 중 하나여야 합니다."
    )
    private String tab = "written";

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 50, message = "페이지 크기는 50 이하여야 합니다.")
    private int size = 20;

    private String cursor;
}
