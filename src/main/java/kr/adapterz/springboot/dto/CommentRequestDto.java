package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto {

    @JsonProperty("parent_id")
    private Long parentId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 500, message = "댓글 내용은 500글자 이하여야 합니다.")
    private String content;
}
