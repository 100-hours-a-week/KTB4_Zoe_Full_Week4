package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PaginationResponseDto {

    private int page;
    private int size;
    @JsonProperty("has_next")
    private boolean hasNext;

    public PaginationResponseDto(int page, int size, boolean hasNext) {
        this.page = page;
        this.size = size;
        this.hasNext = hasNext;
    }
}
