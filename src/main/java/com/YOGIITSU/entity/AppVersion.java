package com.YOGIITSU.entity;


import com.YOGIITSU.enums.Platform;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_version")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private Platform platform; // android / ios

	@Column(nullable = false)
	private String minVersion;

	@Column(nullable = false)
	private String latestVersion;

	private String updatedBy;

	@Column(updatable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
