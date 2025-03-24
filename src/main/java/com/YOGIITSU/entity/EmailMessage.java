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

	@Column(nullable = false, length = 255)
	private String email;

	@Column(nullable = false, length = 255)
	private String code;

	@Column(name = "is_approved", nullable = false, columnDefinition = "TINYINT(1)")
	private Boolean isApproved;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	// 인증 코드 갱신 메서드
	public void updateCode(String code) {
		this.code = code;
		this.isApproved = false;
		this.expiresAt = LocalDateTime.now().plusMinutes(5); // 5분 후 만료 시간 갱신
	}

	// 인증 성공 시 승인 처리
	public void setIsApproved(Boolean isApproved) {
		this.isApproved = isApproved;
	}

}
