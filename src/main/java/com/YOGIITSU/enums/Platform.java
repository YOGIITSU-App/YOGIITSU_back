package com.YOGIITSU.enums;

import lombok.Getter;

@Getter
public enum Platform {
	ANDROID("android"),
	IOS("ios");

	private final String value;

	Platform(String value) {
		this.value = value;
	}
}