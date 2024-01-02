package com.StudyOps.domain.group.service;

import com.StudyOps.domain.attendance.service.StudyAttendanceService;
import com.StudyOps.domain.attendance.service.StudyAttendanceVoteService;
import com.StudyOps.domain.group.dto.*;
import com.StudyOps.domain.group.entity.StudyGroup;
import com.StudyOps.domain.group.repository.StudyGroupRepository;
import com.StudyOps.domain.member.entity.StudyMember;
import com.StudyOps.domain.member.repository.StudyMemberRepository;
import com.StudyOps.domain.member.service.InvitedMemberService;
import com.StudyOps.domain.member.service.StudyMemberService;
import com.StudyOps.domain.penalty.service.StudyPenaltyService;
import com.StudyOps.domain.schedule.dto.StudyScheduleDto;
import com.StudyOps.domain.schedule.entity.StudySchedule;
import com.StudyOps.domain.schedule.repository.StudyScheduleRepository;
import com.StudyOps.domain.schedule.service.StudyScheduleService;
import com.StudyOps.domain.user.entity.EndUser;
import com.StudyOps.domain.user.repository.EndUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StudyGroupService {
    private final StudyScheduleRepository studyScheduleRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final EndUserRepository endUserRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyMemberService studyMemberService;
    private final StudyScheduleService studyScheduleService;
    private final InvitedMemberService invitedMemberService;
    private final StudyAttendanceService studyAttendanceService;
    private final StudyAttendanceVoteService studyAttendanceVoteService;
    private final StudyPenaltyService studyPenaltyService;

    public StudyGroupCreatedIdDto createStudyGroup(Long userId, StudyGroupReqDto studyGroupReqDto) {
        //userId로 유저를 찾는다. Optional로 조회되므로 .get()매서드를 사용해준다.
        EndUser endUser = endUserRepository.findById(userId).get();

        //studyGroupCreateReqDto를 엔티티로 변환 후 디비에 정보를 저장한다.
        String hostName = endUserRepository.findById(userId).get().getNickname();
        StudyGroup studyGroup = studyGroupReqDto.toEntity(hostName);
        studyGroupRepository.save(studyGroup);

        //StudyMember 생성
        studyMemberService.createStudyMember(endUser, studyGroup, true);

        //StudySchedule 생성
        studyScheduleService.createStudySchedule(studyGroup, studyGroupReqDto.getSchedules());

        //InvitedMember 생성
        invitedMemberService.createInvitedMember(studyGroup.getId(), studyGroupReqDto.getInvitees());

        return StudyGroupCreatedIdDto.builder()
                .groupId(studyGroup.getId())
                .build();
    }

    /***********************************
     1.스터디 멤버테이블에서 삭제(마지막)
     2.스터디 그룹 인원수 한명감소(완료)
     3.스터디 출결테이블에서 삭제(완료)
     4.스터디 투표테이블에서 삭제(완료)
     4.스터디 벌금테이블에서 삭제(완료)
     5.스터디 게시판테이블에서 삭제(나중에 게시판 구현후)
     6.추후 스터디장이 탈퇴시 위임기능 추가
     ***********************************/
    public void quitStudyGroup(Long groupId, Long userId) {

        EndUser endUser = endUserRepository.findById(userId).get();
        StudyGroup studyGroup = studyGroupRepository.findById(groupId).get();
        studyGroup.decreaseHeadCount();

        //studyMember조회
        StudyMember studyMember = studyMemberRepository.findByStudyGroupAndEndUser(studyGroup, endUser).get();

        //출결 테이블에서 찾은 studyMember 삭제
        studyAttendanceService.deleteStudyMember(studyMember);

        //투표 테이블에서 찾은 studyMember 삭제
        studyAttendanceVoteService.deleteStudyMember(studyMember);

        //지각벌금 테이블에서 찾은 studyMember 삭제
        studyPenaltyService.deleteLateStudyMember(studyMember);

        //불참벌금 테이블에서 찾은 studyMember 삭제
        studyPenaltyService.deleteAbsentStudyMember(studyMember);

        if(studyMember.getHostStatus().equals(true))
            studyGroupRepository.delete(studyMember.getStudyGroup());
        else
        //스터디 멤버 테이블에서 최종적으로 그 스터디멤버 삭제
        studyMemberRepository.delete(studyMember);
    }

    /**
     예외 처리 1. 유효하지않은 userId
     **/
    public List<StudyGroupResDto> getAllStudyGroups(Long userId) {

        EndUser endUser = endUserRepository.findById(userId).get();

        List<StudyMember> studyMembers = studyMemberRepository.findAllByEndUser(endUser);
        List<StudyGroupResDto> resDtos = studyMembers.stream()
                .map(member -> {
                    StudyGroup studyGroup = member.getStudyGroup();
                    List<StudyScheduleDto> studyScheduleDtos = studyScheduleRepository.findAllByStudyGroup(studyGroup)
                            .stream()
                            .map(schedule -> StudyScheduleDto.builder()
                                    .dayWeek(schedule.getDayWeek())
                                    .startTime(schedule.getStartTime())
                                    .finishTime(schedule.getFinishTime())
                                    .build())
                            .collect(Collectors.toList());

                    return StudyGroupResDto.builder()
                            .groupId(studyGroup.getId())
                            .name(studyGroup.getName())
                            .intro(studyGroup.getIntro())
                            .schedules(studyScheduleDtos)
                            .hostName(studyGroup.getHostName())
                            .isHost(studyGroup.getHostName().equals(endUser.getNickname()))
                            .headCount(studyGroup.getHeadCount())
                            .absenceCost(studyGroup.getAbsentCost())
                            .lateCost(studyGroup.getLateCost())
                            .startDate(studyGroup.getStartDate())
                            .build();
                })
                .collect(Collectors.toList());

        return resDtos;

    }

    public StudyGroupInfoResDto getStudyGroupInfo(Long groupId, Long userId) {
        StudyGroup studyGroup = studyGroupRepository.findById(groupId).get();
        EndUser endUser = endUserRepository.findById(userId).get();
        StudyMember studyMember = studyMemberRepository.findByStudyGroupAndEndUser(studyGroup,endUser).get();
        List<StudyMember> memberList = studyMemberRepository.findAllByStudyGroup(studyGroup);
        List<String> members = new ArrayList<>();

        List<StudySchedule> scheduleList = studyScheduleRepository.findAllByStudyGroup(studyGroup);
        List<StudyScheduleDto> schedules = new ArrayList<>();
        for(int i=0; i<scheduleList.size(); i++){
            StudyScheduleDto scheduleDto = StudyScheduleDto.builder()
                    .dayWeek(scheduleList.get(i).getDayWeek())
                    .startTime(scheduleList.get(i).getStartTime())
                    .finishTime(scheduleList.get(i).getFinishTime())
                    .build();
            schedules.add(scheduleDto);
        }
        for(int i=0; i<memberList.size(); i++){
            members.add(memberList.get(i).getEndUser().getNickname());
        }
        StudyGroupInfoResDto studyGroupInfoResDto = StudyGroupInfoResDto.builder()
                .name(studyGroup.getName())
                .intro(studyGroup.getIntro())
                .rule(studyGroup.getRule())
                .hostName(studyGroup.getHostName())
                .hostProfileImageUrl(endUser.getProfileImageUrl())
                .isHost(studyMember.getHostStatus())
                .members(members)
                .startDate(studyGroup.getStartDate())
                .schedules(schedules)
                .allowedTime(studyGroup.getAllowedTime())
                .lateCost(studyGroup.getLateCost())
                .absenceCost(studyGroup.getAbsentCost())
                .build();

        return studyGroupInfoResDto;
    }

    public void changeStudyGroupRule(Long groupId, StudyGroupRuleReqDto studyGroupRuleReqDto) {

        StudyGroup studyGroup = studyGroupRepository.findById(groupId).get();
        studyGroup.changeRule(studyGroupRuleReqDto.getRule());
    }
    public void changeStudyGroupIntro(Long groupId, StudyGroupIntroReqDto studyGroupIntroReqDto) {

        StudyGroup studyGroup = studyGroupRepository.findById(groupId).get();
        studyGroup.changeIntro(studyGroupIntroReqDto.getIntro());
    }

    public void changeStudyGroupAccount(Long groupId, StudyGroupAccountReqDto studyGroupAccountReqDto) {

        StudyGroup studyGroup = studyGroupRepository.findById(groupId).get();
        studyGroup.changeAccount(studyGroupAccountReqDto.getAccount());
    }
}