package com.YOGIITSU.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recent_search",
	uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "keyword"}))
public class RecentSearch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(name = "keyword", nullable = false, length = 100)
	private String keyword;

	@Column(name = "searched_at", nullable = false)
	private LocalDateTime searchedAt;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "building_id", nullable = false)
	private Building building;
}