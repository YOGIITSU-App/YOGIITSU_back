package com.YOGIITSU.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "building_aliases")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BuildingAlias {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "building_id", nullable = false)
	private Building building;

	@Column(name = "alias", nullable = false, length = 100)
	private String alias;
}
