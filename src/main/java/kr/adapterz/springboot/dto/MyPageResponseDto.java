package kr.adapterz.springboot.dto;

import lombok.Getter;

@Getter
public class MyPageResponseDto {

    private final MyPageProfileResponseDto profile;
    private final MyPageStatsResponseDto stats;
    private final MyPageActivityResponseDto activity;

    public MyPageResponseDto(
            MyPageProfileResponseDto profile,
            MyPageStatsResponseDto stats,
            MyPageActivityResponseDto activity
    ) {
        this.profile = profile;
        this.stats = stats;
        this.activity = activity;
    }
}
