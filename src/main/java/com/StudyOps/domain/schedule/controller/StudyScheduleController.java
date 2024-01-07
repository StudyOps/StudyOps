package com.StudyOps.domain.schedule.controller;

import com.StudyOps.domain.schedule.dto.StudyScheduleDto;
import com.StudyOps.domain.schedule.dto.StudyScheduleListDto;
import com.StudyOps.domain.schedule.service.StudyScheduleService;
import com.StudyOps.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.StudyOps.global.common.ApiResponseStatus.STUDY_SCHEDULE_GET_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StudyScheduleController {
    private final StudyScheduleService studyScheduleService;

    @GetMapping("/schedules/{groupId}")
    public ResponseEntity<ApiResponse<StudyScheduleListDto>> getStudySchedule(@PathVariable (value = "groupId") Long groupId){

        ApiResponse<StudyScheduleListDto> successResponse = new ApiResponse<>(STUDY_SCHEDULE_GET_SUCCESS,studyScheduleService.getStudySchedule(groupId));

        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }
}
