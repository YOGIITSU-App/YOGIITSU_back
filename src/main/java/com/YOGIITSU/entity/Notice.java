package com.YOGIITSU.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notice")
public class Notice {

    // 공지사항 id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id", updatable = false, nullable = false)
    private Long noticeId;

    // 공지 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 공지 제목
    @Column(name = "notice_title", nullable = false)
    private String noticeTitle;

    // 공지 본문 내용
    @Column(name = "notice_content", nullable = false, columnDefinition = "TEXT")
    private String noticeContent;

    // 공지 작성일
    @Column(name = "notice_at", nullable = false)
    private LocalDateTime noticeAt;

    // 공지사항이 처음 저장될 때 현재 시간도 저장됨
    @PrePersist
    protected void onCreate() {
        this.noticeAt = LocalDateTime.now();
    }

    // 공지사항 제목 및 내용 수정용 메서드
    public void update(String title, String content) {
        this.noticeTitle = title;
        this.noticeContent = content;
    }
}