package kr.adapterz.springboot.controller;

import jakarta.validation.Valid;
import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.PostDraftRequestDto;
import kr.adapterz.springboot.dto.PostDraftResponseDto;
import kr.adapterz.springboot.service.PostDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/drafts")
@RequiredArgsConstructor
public class PostDraftController {

    private final PostDraftService postDraftService;

    @PutMapping
    public ResponseEntity<ApiResponseDto<PostDraftResponseDto>> saveDraft(
            @Valid @RequestBody PostDraftRequestDto request
    ) {
        PostDraftResponseDto response = postDraftService.saveDraft(request);
        return ResponseEntity.ok(new ApiResponseDto<>("draft_saved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PostDraftResponseDto>> getDraft(
            @RequestParam(name = "post_id", required = false) Long postId
    ) {
        PostDraftResponseDto response = postDraftService.getDraft(postId);
        return ResponseEntity.ok(new ApiResponseDto<>("fetch_success", response));
    }
}
