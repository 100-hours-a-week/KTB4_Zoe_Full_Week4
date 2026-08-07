package kr.adapterz.springboot.controller;

import jakarta.validation.Valid;
import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.MyPageActivitiesQuery;
import kr.adapterz.springboot.dto.MyPageActivityResponseDto;
import kr.adapterz.springboot.dto.MyPageQuery;
import kr.adapterz.springboot.dto.MyPageResponseDto;
import kr.adapterz.springboot.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<MyPageResponseDto>> getMyPage(
            @Valid @ModelAttribute MyPageQuery query
    ) {
        return ResponseEntity.ok(
                new ApiResponseDto<>("fetch_success", myPageService.getMyPage(query))
        );
    }

    @GetMapping("/activities")
    public ResponseEntity<ApiResponseDto<MyPageActivityResponseDto>> getActivities(
            @Valid @ModelAttribute MyPageActivitiesQuery query
    ) {
        return ResponseEntity.ok(
                new ApiResponseDto<>("fetch_success", myPageService.getActivities(query))
        );
    }
}
