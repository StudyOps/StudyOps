package com.StudyOps.domain.member.entity;

import com.StudyOps.domain.group.entity.StudyGroup;
import com.StudyOps.domain.user.entity.EndUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyMember {
    @Id @GeneratedValue
    @Column(name = "study_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private EndUser endUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    private StudyGroup studyGroup;
    private Boolean hostStatus;
    private int totalPenalty;
    private LocalDate joinDate;

    public void plusTotalPenalty(int cost){
        this.totalPenalty += cost;
    }

    public void minusTotalPenalty(int cost){
        this.totalPenalty -= cost;
    }
}
