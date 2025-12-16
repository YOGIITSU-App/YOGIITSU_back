package com.YOGIITSU.entity;

import com.YOGIITSU.enums.FacilityDetailType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "building_facilities")
public class BuildingFacility {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "building_id", nullable = false)
	@JsonBackReference
	private Building building;

	@Column(nullable = false)
	private String name;

	private String floor;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private FacilityDetailType type;
}
