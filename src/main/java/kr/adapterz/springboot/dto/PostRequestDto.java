package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostRequestDto {
    private String title;
    private String content;
    @JsonProperty("image_url")
    private String imageUrl;
}
