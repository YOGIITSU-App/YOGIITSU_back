package com.YOGIITSU.jwt;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

	private final Long id;
	private final String memberId;
	private final String userName;
	private final String email;
	private final String role;

	public CustomUserDetails(Long id, String memberId, String userName, String email, String password, String role, Collection<? extends GrantedAuthority> authorities) {
		super(memberId, password, authorities);
		this.id = id;
		this.memberId = memberId;
		this.userName = userName;
		this.email = email;
		this.role = role;
	}
}
