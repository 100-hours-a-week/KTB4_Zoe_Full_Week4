package kr.adapterz.springboot.controller;

import jakarta.validation.Valid;
import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.PostReportRequestDto;
import kr.adapterz.springboot.dto.PostReportResponseDto;
import kr.adapterz.springboot.service.PostReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/reports")
@RequiredArgsConstructor
public class PostReportController {

    private final PostReportService postReportService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<PostReportResponseDto>> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody PostReportRequestDto request
    ) {
        PostReportResponseDto response = postReportService.reportPost(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("post_report_created", response));
    }
}
