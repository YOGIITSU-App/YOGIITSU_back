package com.YOGIITSU.exception.building;

import com.YOGIITSU.exception.resource.ResourceException;
import com.YOGIITSU.exception.ErrorCode;

/**
 * 건물을 찾을 수 없는 예외
 */
public class BuildingNotFoundException extends ResourceException {

	public BuildingNotFoundException(Long buildingId) {
		super(ErrorCode.BUILDING_NOT_FOUND, "buildingId=" + buildingId);
	}

	public BuildingNotFoundException() {
		super(ErrorCode.BUILDING_NOT_FOUND);
	}
}