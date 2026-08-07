package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class MyPageActivityResponseDto {

    private final String tab;
    private final List<MyPageActivityItemResponseDto> items;

    @JsonProperty("next_cursor")
    private final String nextCursor;

    @JsonProperty("has_next")
    private final boolean hasNext;

    public MyPageActivityResponseDto(
            String tab,
            List<MyPageActivityItemResponseDto> items,
            String nextCursor,
            boolean hasNext
    ) {
        this.tab = tab;
        this.items = items;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }
}
