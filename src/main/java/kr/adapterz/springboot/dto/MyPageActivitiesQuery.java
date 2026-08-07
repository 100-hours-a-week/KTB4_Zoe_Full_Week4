package kr.adapterz.springboot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyPageActivitiesQuery {

    private static final String TAB_PATTERN = "written|participated|liked";

    @NotBlank(message = "활동 탭은 필수입니다.")
    @Pattern(
            regexp = TAB_PATTERN,
            message = "written, participated, liked 중 하나여야 합니다."
    )
    private String tab;

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 50, message = "페이지 크기는 50 이하여야 합니다.")
    private int size = 20;

    private String cursor;
}
