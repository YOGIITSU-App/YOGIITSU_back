package com.YOGIITSU.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
	name = "cafeteria",
	uniqueConstraints = @UniqueConstraint(name = "uq_cafeteria", columnNames = {"building_id",
		"name"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Cafeteria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "building_id", nullable = false)
	private Long buildingId;

	@Column(nullable = false, length = 50)
	private String name;       // 학생식당 / 교직원식당
}