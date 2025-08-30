package com.YOGIITSU.enums;

import lombok.Getter;

@Getter
public enum FacilityDetailType {
	PARKING("주차장"),
	READING_ROOM("열람실"),
	VENDING_MACHINE("자판기"),
	PRINTER("프린터기"),
	UNMANNED_LOCKER("무인택배함"),
	NURSE_OFFICE("보건실"),
	POWER_BANK("보조배터리"),
	STUDY_ROOM("스터디룸"),
	CAFETERIA("식당"),
	ELEVATOR("엘리베이터"),
	MICROWAVE("전자레인지"),
	ICE_MAKER("제빙기"),
	CAFE("카페"),
	CONVENIENCE_STORE("편의점"),
	GYM("헬스장");

	private final String description;

	FacilityDetailType(String description) {
		this.description = description;
	}
}
