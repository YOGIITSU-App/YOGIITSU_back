package com.YOGIITSU.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "favorites",
	uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "building_id"}))
public class Favorite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)  // N:1 관계 (여러 사용자가 같은 건물을 즐겨찾기 가능)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.EAGER)  // N:1 관계 (한 사용자가 여러 건물 즐겨찾기 가능)
	@JoinColumn(name = "building_id", nullable = false)
	private Building building;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
