package com.YOGIITSU.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Inquiry 엔티티
 * - 사용자 문의에 대한 정보 저장
 * - 제목, 내용, 답변, 상태, 작성일, 답변일 포함
 */
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
    private Long inquiryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "inquiry_title", nullable = false)
    private String inquiryTitle;

    @Column(name = "inquiry_content", columnDefinition = "TEXT", nullable = false)
    private String inquiryContent;

    @Column(name = "inquiry_at", nullable = false, updatable = false)
    private LocalDateTime inquiryAt;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "response_at")
    private LocalDateTime responseAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_state", length = 50, nullable = false)
    private InquiryState inquiryState;

    /**
     * 문의 등록 시 자동으로 호출되어 작성일과 상태 초기화
     */
    @PrePersist
    protected void onCreate() {
        this.inquiryAt = LocalDateTime.now();
        this.inquiryState = InquiryState.PROCESSING;
    }

    /**
     * 제목과 내용을 수정
     */
    public void updateInquiry(String newTitle, String newContent) {
        this.inquiryTitle = (newTitle != null) ? newTitle : this.inquiryTitle;
        this.inquiryContent = (newContent != null) ? newContent : this.inquiryContent;
    }

    /**
     * 답변 등록 및 답변일 설정
     */
    public void updateResponse(String response) {
        this.response = response;
        this.responseAt = LocalDateTime.now();  // 답변 날짜 함께 갱신
    }

    /**
     * 문의 상태 변경
     */
    public void updateInquiryState(InquiryState newState) {
        this.inquiryState = newState;
    }
}
