package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.PostDraft;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostDraftResponseDto {

    @JsonProperty("draft_id")
    private Long draftId;

    @JsonProperty("post_id")
    private Long postId;

    private String title;

    private String content;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public PostDraftResponseDto(PostDraft draft) {
        this.draftId = draft.getId();
        this.postId = draft.getPostId();
        this.title = draft.getTitle();
        this.content = draft.getContent();
        this.createdAt = draft.getCreatedAt();
        this.updatedAt = draft.getUpdatedAt();
    }
}
