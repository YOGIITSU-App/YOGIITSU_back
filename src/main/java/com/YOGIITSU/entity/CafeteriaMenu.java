package com.YOGIITSU.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
	name = "cafeteria_menu",
	uniqueConstraints = @UniqueConstraint(
		name = "uq_menu",
		columnNames = {"cafeteria_id", "meal_date", "meal_type", "corner"}
	)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CafeteriaMenu {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "cafeteria_id", nullable = false)
	private Long cafeteriaId;

	@Column(name = "meal_date", nullable = false)
	private LocalDate mealDate;

	@Column(name = "meal_type", nullable = false, length = 30)
	private String mealType;

	@Column(nullable = false, length = 120)
	private String corner;

	@Lob
	@Column(name = "items_text", nullable = false)
	private String itemsText;

	@Column(name = "version_week", nullable = false)
	private LocalDate versionWeek;

	@Column(name = "hash_body", nullable = false, length = 40)
	private String hashBody;

	// 메뉴 본문/해시/버전 주차 업데이트 (setter 대체)
	public void updateMenu(String itemsText, String hashBody, LocalDate versionWeek) {
		this.itemsText = itemsText;
		this.hashBody = hashBody;
		this.versionWeek = versionWeek;
	}

	// 과거 데이터에서 corner가 NULL인 경우 ""로 보정
	public void normalizeCornerIfNull() {
		if (this.corner == null) {
			this.corner = "";
		}
	}
}