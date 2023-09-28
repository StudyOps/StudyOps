package com.StudyOps.domain.group.controller;

import com.StudyOps.domain.group.dto.StudyGroupReqDto;
import com.StudyOps.domain.group.dto.StudyGroupResDto;
import com.StudyOps.domain.group.service.StudyGroupService;
import com.StudyOps.global.common.ApiResponse;
import com.StudyOps.global.common.ApiResponseStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.StudyOps.global.common.ApiResponseStatus.*;

@RestController
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    //스터디 생성
    @PostMapping("/groups/{userId}")
    public ResponseEntity<ApiResponse<Object>> createStudyGroup(@PathVariable(value = "userId") Long userId,@RequestBody StudyGroupReqDto studyGroupReqDto) {

        //로직 처리
        studyGroupService.createStudyGroup(userId, studyGroupReqDto);
        //응답 처리
        ApiResponse<Object> successResponse = new ApiResponse<>(STUDY_GROUP_CREATE_SUCCESS);

        return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
    }
    //스터디 탈퇴
    @DeleteMapping("/groups/{groupId}/{userId}")
    public ResponseEntity<ApiResponse<Object>> quitStudyGroup(@PathVariable(value = "groupId") Long groupId, @PathVariable(value = "userId") Long userId){

        studyGroupService.quitStudyGroup(groupId,userId);

        ApiResponse<Object> successResponse = new ApiResponse<>(STUDY_GROUP_QUIT_SUCCESS);

        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    //스터디 전체 조회
    @GetMapping("/groups/{userId}")
    public ResponseEntity<ApiResponse<List<StudyGroupResDto>>> getAllOfStudyGroups(@PathVariable(value = "userId") Long userId){

        ApiResponse<List<StudyGroupResDto>> successResponse = new ApiResponse<>(ALL_STUDY_GROUPS_GET_SUCCESS,studyGroupService.getAllOfStudyGroups(userId));

        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }
}
