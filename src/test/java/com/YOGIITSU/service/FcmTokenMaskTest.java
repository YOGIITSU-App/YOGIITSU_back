package com.YOGIITSU.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FcmTokenMaskTest {

	@Test
	@DisplayName("FCM_토큰_로그_마스킹")
	void maskToken() {
		assertEquals("abcdefgh****", FcmService.maskToken("abcdefghijklmnopqrstuvwxyz"));
		assertEquals("****", FcmService.maskToken("short"));
		assertEquals("****", FcmService.maskToken(null));
	}
}
