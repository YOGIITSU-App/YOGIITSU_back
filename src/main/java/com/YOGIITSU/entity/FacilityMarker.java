package com.YOGIITSU.entity;

import com.YOGIITSU.enums.FacilityType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facility_marker")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityMarker {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "place_name")
	private String placeName;

	@Enumerated(EnumType.STRING)
	private FacilityType type;

	private Double latitude;

	private Double longitude;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "building_id", nullable = false)
	private Building building;
}
