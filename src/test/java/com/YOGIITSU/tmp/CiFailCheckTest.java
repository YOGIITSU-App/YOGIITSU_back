package com.YOGIITSU.tmp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class CiFailCheckTest {
	@Test
	void intentionallyFails() {
		fail("CI 실패 감지 확인용 임시 테스트");
	}
}
