package com.YOGIITSU.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildingLookupService {

	private final JdbcTemplate jdbc;

	/**
	 * 건물명으로 ID 조회
	 */
	public Optional<Long> findBuildingIdByName(String buildingName) {
		try {
			return jdbc.query(
				"SELECT id FROM buildings WHERE name = ? LIMIT 1",
				ps -> ps.setString(1, buildingName),
				rs -> rs.next() ? Optional.of(rs.getLong(1)) : Optional.empty()
			);
		} catch (DataAccessException e) {
			log.error("findBuildingIdByName 실패: {}", buildingName, e);
			return Optional.empty();
		}
	}

	/**
	 * 건물 ID로 이름 조회 (표시용)
	 */
	public Optional<String> findBuildingNameById(Long buildingId) {
		try {
			return jdbc.query(
				"SELECT name FROM buildings WHERE id = ? LIMIT 1",
				ps -> ps.setLong(1, buildingId),
				rs -> rs.next() ? Optional.of(rs.getString(1)) : Optional.empty()
			);
		} catch (DataAccessException e) {
			log.error("findBuildingNameById 실패: {}", buildingId, e);
			return Optional.empty();
		}
	}
}