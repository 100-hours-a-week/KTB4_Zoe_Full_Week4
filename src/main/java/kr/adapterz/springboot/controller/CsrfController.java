package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.dto.ApiResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponseDto<Void>> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.ok(new ApiResponseDto<>("csrf_token_issued", null));
    }
}
