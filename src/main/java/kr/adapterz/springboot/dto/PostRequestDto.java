package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostRequestDto {
    private String title;
    private String content;
    @JsonProperty("image_urls")
    private List<String> imageUrls = List.of();
}
