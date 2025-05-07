package com.YOGIITSU.exception.building;

public class BuildingNotFoundException extends RuntimeException {

	public BuildingNotFoundException(Long buildingId) {
		super("존재하지 않는 건물입니다. buildingId=" + buildingId);
	}
}