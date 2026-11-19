package com.YOGIITSU;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultTimeZoneTest {

	private final TimeZone original = TimeZone.getDefault();

	@AfterEach
	void restore() {
		TimeZone.setDefault(original);
	}

	@Test
	@DisplayName("애플리케이션_기본_시간대_KST_설정")
	void configureDefaultTimeZone_setsKst() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		YogiitsuApplication.configureDefaultTimeZone();

		assertEquals("Asia/Seoul", TimeZone.getDefault().getID());
	}
}
