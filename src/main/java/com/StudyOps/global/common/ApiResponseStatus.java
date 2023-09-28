package com.StudyOps.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiResponseStatus {
    //이 부분에 응답상태 기록
    STUDY_GROUP_CREATE_SUCCESS(true,201,"새로운 스터디 그룹 생성되었습니다."),
    STUDY_GROUP_QUIT_SUCCESS(true,200,"현재 스터디 그룹을 탈퇴했습니다."),
    ALL_STUDY_GROUPS_GET_SUCCESS(true, 200, "참여중인 전체 스터디 목록을 조회합니다.");
    private final boolean isSuccess;
    private final int status;
    private final String message;
}
