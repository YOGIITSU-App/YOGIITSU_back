package com.YOGIITSU.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "member")
public class Member implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id; // 내부 식별용 ID

	@Column(name = "member_id", unique = true, nullable = false, length = 50)
	private String memberId; // 사용자 ID (로그인용)

	@Column(name = "password", nullable = false, length = 100)
	private String password; // 비밀번호 (해시 처리)

	@Column(name = "email", unique = true, nullable = false, length = 100)
	private String email; // 본인 인증용 이메일

	@Column(name = "user_name", length = 50)
	private String userName; // 사용자 이름

	@Column(name = "role", length = 50)
	private String role; // 유저 or 관리자

	@Column(name = "join_at", nullable = false)
	private LocalDateTime joinAt; // 가입일

	@OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Inquiry> inquiries;

	// UserDetails 인터페이스 구현 메서드들
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role)); // role을 "ROLE_USER" 등으로 반환
	}

	@Override
	public String getUsername() {
		return memberId; // 로그인 ID 반환
	}

	@Override
	public String getPassword() {
		return password; // 비밀번호 반환
	}

	// 비밀번호 변경 메서드
	public void changePassword(String newPassword) {
		this.password = newPassword;
	}

	// 이메일 변경 메서드
	public void setEmail(String newEmail) {
		this.email = newEmail;
	}

	// 사용자 이름 반환
	public String getUserName() {
		return this.userName;
	}
}
