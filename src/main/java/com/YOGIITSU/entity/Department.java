package com.YOGIITSU.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "departments")
public class Department {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "building_id", nullable = false)
	@JsonBackReference
	private Building building;

	@Column(nullable = false)
	private String collegeName;

	@Column(nullable = false)
	private String departmentName;

	@Column(nullable = false)
	private String location;

	private String phone;

	private String fax;

	private String officeHours;
}
