package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PostReportResponseDto {

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("report_count")
    private long reportCount;

    @JsonProperty("is_blinded")
    private boolean blinded;

    public PostReportResponseDto(Long postId, long reportCount, boolean blinded) {
        this.postId = postId;
        this.reportCount = reportCount;
        this.blinded = blinded;
    }
}
