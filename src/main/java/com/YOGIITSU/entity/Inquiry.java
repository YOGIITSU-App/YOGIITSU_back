package com.YOGIITSU.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "inquiry")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id", updatable = false, nullable = false)
    private Long inquiryId;  // 문의 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // Member 엔티티의 PK(id) 참조 (FK)

    @Column(name = "inquiry_title", nullable = false)
    private String inquiryTitle;

    @Column(name = "inquiry_content", columnDefinition = "TEXT", nullable = false)
    private String inquiryContent;  // 문의 내용

    @Column(name = "inquiry_password", nullable = false, length = 20)
    private String inquiryPassword;  // 문의 비밀번호

    @Column(name = "inquiry_at", nullable = false, updatable = false)
    private LocalDateTime inquiryAt;  // 문의 등록일

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;  // 관리자 답변 내용

    @Column(name = "response_at")
    private LocalDateTime responseAt;  // 답변 작성일

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_state", length = 50, nullable = false)
    private InquiryState inquiryState;  // 문의 상태 (진행 중 / 답변 완료)

    @PrePersist
    protected void onCreate() {
        this.inquiryAt = LocalDateTime.now();
        this.inquiryState = InquiryState.PROCESSING;
    }

    // 문의 제목 및 내용 수정을 위한 메서드
    public void updateInquiry(String newContent, String newTitle) {
        this.inquiryTitle = (newTitle != null) ? newTitle : this.inquiryTitle;
        this.inquiryContent = newContent;
    }

    // 관리자가 답변 등록 시 호출 메서드
    public void updateResponse(String response) {
        this.response = response;
        this.responseAt = LocalDateTime.now();  // 답변 날짜 함께 갱신
    }

    // 문의 상태 업데이트
    public void updateInquiryState(InquiryState newState) {
        this.inquiryState = newState;  // 문의 상태 변경
    }
}
