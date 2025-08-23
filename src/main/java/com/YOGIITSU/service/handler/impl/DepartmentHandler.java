package com.YOGIITSU.service.handler.impl;

import com.YOGIITSU.repository.DepartmentRepository;
import com.YOGIITSU.repository.projection.*;
import com.YOGIITSU.service.handler.DynamicResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DepartmentHandler implements DynamicResponseHandler {

	private final DepartmentRepository departmentRepository;

	@Override
	public boolean supports(String key) {
		// 설계: DEPT_LOCATION / DEPT_PHONE / DEPT_FAX / DEPT_HOURS / DEPT_OVERVIEW
		return key != null && key.startsWith("DEPT_");
	}

	@Override
	public String buildRawAnswer(Long nodeId, String key, Map<String, Object> ctx) {
		// ID 우선 사용
		Long deptId = (Long) ctx.get("deptId");
		if (deptId == null) {
			throw new IllegalArgumentException("deptId가 필요합니다.");
		}

		return switch (key) {
			case "DEPT_LOCATION" ->
				formatLocation(departmentRepository.findLocationByDeptId(deptId));
			case "DEPT_PHONE" -> formatPhone(departmentRepository.findPhoneByDeptId(deptId));
			case "DEPT_FAX" -> formatFax(departmentRepository.findFaxByDeptId(deptId));
			case "DEPT_HOURS" -> formatHours(departmentRepository.findHoursByDeptId(deptId));
			case "DEPT_OVERVIEW" -> {
				var loc = departmentRepository.findLocationByDeptId(deptId);
				var phone = departmentRepository.findPhoneByDeptId(deptId);
				var fax = departmentRepository.findFaxByDeptId(deptId);
				var hours = departmentRepository.findHoursByDeptId(deptId);
				yield formatOverview(loc, phone, fax, hours);
			}
			default -> "학과 정보를 찾지 못했습니다.";
		};
	}

	private String formatLocation(List<DeptLocationView> list) {
		if (list == null || list.isEmpty()) {
			return "학과 위치 정보를 찾지 못했습니다.";
		}
		return list.stream()
			.map(v -> v.getDepartmentName() + "은(는) " + (v.getLocation() != null ? v.getLocation()
				: "위치 정보 없음") + "입니다.")
			.collect(Collectors.joining(" "));
	}

	private String formatPhone(List<DeptPhoneView> list) {
		if (list == null || list.isEmpty()) {
			return "학과 대표전화를 찾지 못했습니다.";
		}
		return list.stream()
			.map(v -> v.getDepartmentName() + " 대표전화는 " + v.getPhone() + "입니다.")
			.collect(Collectors.joining(" "));
	}

	private String formatFax(List<DeptFaxView> list) {
		if (list == null || list.isEmpty()) {
			return "FAX 정보를 찾지 못했습니다.";
		}
		return list.stream()
			.map(v -> v.getDepartmentName() + " FAX는 " + v.getFax() + "입니다.")
			.collect(Collectors.joining(" "));
	}

	private String formatHours(List<DeptHoursView> list) {
		if (list == null || list.isEmpty()) {
			return "업무시간 정보를 찾지 못했습니다.";
		}
		return list.stream()
			.map(v -> v.getDepartmentName() + " 업무시간은 " + v.getOfficeHours() + "입니다.")
			.collect(Collectors.joining(" "));
	}

	// OVERVIEW를 멀티라인/불릿으로 구성
	private String formatOverview(
		List<DeptLocationView> locList,
		List<DeptPhoneView> phoneList,
		List<DeptFaxView> faxList,
		List<DeptHoursView> hoursList
	) {
		// 학과명 기준으로 합치기 (여러 학과가 들어와도 블록 별로 출력)
		record Ov(String loc, String phone, String fax, String hours) {

		}
		Map<String, Ov> m = new LinkedHashMap<>();

		// 위치
		if (locList != null) {
			for (var v : locList) {
				final String loc = v.getLocation() != null ? v.getLocation() : "위치 정보 없음";
				m.compute(v.getDepartmentName(), (k, old) ->
					new Ov(
						loc,
						old != null ? old.phone() : null,
						old != null ? old.fax() : null,
						old != null ? old.hours() : null
					)
				);
			}
		}

		// 대표전화
		if (phoneList != null) {
			for (var v : phoneList) {
				final String phone = v.getPhone() != null ? v.getPhone() : "전화 정보 없음";
				m.compute(v.getDepartmentName(), (k, old) ->
					new Ov(
						old != null ? old.loc() : null,
						phone,
						old != null ? old.fax() : null,
						old != null ? old.hours() : null
					)
				);
			}
		}

		// FAX
		if (faxList != null) {
			for (var v : faxList) {
				final String fax = v.getFax() != null ? v.getFax() : "FAX 정보 없음";
				m.compute(v.getDepartmentName(), (k, old) ->
					new Ov(
						old != null ? old.loc() : null,
						old != null ? old.phone() : null,
						fax,
						old != null ? old.hours() : null
					)
				);
			}
		}

		// 업무시간
		if (hoursList != null) {
			for (var v : hoursList) {
				final String hours = v.getOfficeHours() != null ? v.getOfficeHours() : "업무시간 정보 없음";
				m.compute(v.getDepartmentName(), (k, old) ->
					new Ov(
						old != null ? old.loc() : null,
						old != null ? old.phone() : null,
						old != null ? old.fax() : null,
						hours
					)
				);
			}
		}

		if (m.isEmpty()) {
			return "학과 정보를 찾지 못했습니다.";
		}

		// 블록 구성: 학과명\n- 위치: ...\n- 대표전화: ...\n- FAX: ...\n- 업무시간: ...
		StringBuilder sb = new StringBuilder();
		for (var e : m.entrySet()) {
			var name = e.getKey();
			var ov = e.getValue();
			sb.append(name).append("\n")
				.append("- 위치: ").append(ov.loc() != null ? ov.loc() : "위치 정보 없음").append("\n")
				.append("- 대표전화: ").append(ov.phone() != null ? ov.phone() : "전화 정보 없음")
				.append("\n")
				.append("- FAX: ").append(ov.fax() != null ? ov.fax() : "FAX 정보 없음").append("\n")
				.append("- 업무시간: ").append(ov.hours() != null ? ov.hours() : "업무시간 정보 없음")
				.append("\n\n");
		}
		return sb.toString().trim();
	}
}