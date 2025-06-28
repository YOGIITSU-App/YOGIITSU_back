package com.YOGIITSU.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_message")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_message_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String code;

    @Column(name = "is_approved", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isApproved;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailPurpose purpose;

    // 인증 성공 시 승인 처리
    public void approve() {
        this.isApproved = true;
    }
}
