package com.YOGIITSU.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "buildings")
public class Building {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;

	@Column(name = "image_url")
	private String imageUrl;

	@OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private Set<BuildingFacility> buildingFacilities = new HashSet<>();

	@OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private Set<BuildingFloorImage> buildingFloorImages = new HashSet<>();

	@OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private Set<BuildingTag> buildingTags = new HashSet<>();
}
