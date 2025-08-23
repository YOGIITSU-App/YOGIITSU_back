package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.ChatOptionItemRequestDto;
import com.YOGIITSU.dto.ResponseDto.ChatNodeResponseDto;
import com.YOGIITSU.enums.ResponseType;
import com.YOGIITSU.repository.ChatOptionRepository;
import com.YOGIITSU.repository.DepartmentRepository;
import com.YOGIITSU.repository.CollegeRepository;
import com.YOGIITSU.entity.College;
import com.YOGIITSU.service.gpt.GptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.UnauthenticatedAccessException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotServiceImpl implements ChatbotService {

	private final ChatOptionRepository optionRepo;
	private final DynamicResponseDispatcher dispatcher;
	private final GptService gpt;

	private final DepartmentRepository departmentRepository;
	private final CollegeRepository collegeRepository;

	@Override
	// CHANGED: collegeName, deptName 제거 — ID만 받도록 변경
	public ChatNodeResponseDto getNode(Long nodeId,
		Long collegeId,
		Long deptId) {

		// 로그인 체크 (기존 그대로)
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()
			|| auth.getPrincipal() == null
			|| "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
			throw new UnauthenticatedAccessException();
		}

		var node = optionRepo.findByIdAndIsActiveTrue(nodeId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid node id: " + nodeId));

		var key = node.getResponseKey();
		log.debug("[ChatbotService] nodeId={}, key={}, collegeId={}, deptId={}",
			nodeId, key, collegeId, deptId);

		// null-safe 정규화
		var type = node.getResponseType() == null ? ResponseType.STATIC : node.getResponseType();

		// ----- 1) 단과대 목록 (ID 기반) -----
		if ("COLLEGE_LIST".equalsIgnoreCase(key)) {
			List<College> colleges = collegeRepository.findAllByOrderByNameAsc();
			if (colleges == null || colleges.isEmpty()) {
				return ChatNodeResponseDto.finalText("등록된 단과대가 없습니다.", false, key);
			}
			List<ChatOptionItemRequestDto> items = new ArrayList<>();
			int ord = 1;
			for (College c : colleges) {
				items.add(ChatOptionItemRequestDto.of(c.getId(), c.getName(), ord++));
			}
			return ChatNodeResponseDto.optionsCustom(items);
		}

		// ----- 1.5) 학과 목록 (단과대 ID 필수) -----
		if ("DEPT_LIST".equalsIgnoreCase(key)) {
			// CHANGED: collegeId 없으면 에러 메시지, collegeName 분기 삭제
			if (collegeId == null) {
				return ChatNodeResponseDto.finalText(
					"단과대(collegeId)를 먼저 선택해주세요.",
					false, key
				);
			}

			var depts = departmentRepository.findDeptItemsByCollegeId(collegeId); // (id, name) 프로젝션
			if (depts == null || depts.isEmpty()) {
				return ChatNodeResponseDto.finalText("해당 단과대의 학과가 없습니다.", false, key);
			}

			List<ChatOptionItemRequestDto> items = new ArrayList<>();
			int ord = 1;
			for (var d : depts) {
				items.add(ChatOptionItemRequestDto.of(d.getId(), d.getDepartmentName(), ord++));
			}
			return ChatNodeResponseDto.optionsCustom(items);
		}

		// 2) 자식 옵션이 있으면 OPTIONS
		var children = optionRepo.findByParent_IdAndIsActiveTrueOrderByDisplayOrderAsc(nodeId);
		if (!children.isEmpty()) {
			return ChatNodeResponseDto.options(children);
		}

		// 3) STATIC
		if (type == ResponseType.STATIC) {
			return ChatNodeResponseDto.finalText(
				node.getResponseText(),
				false,
				key
			);
		}

		// 3.5) DYNAMIC인데 키 없음
		if (type == ResponseType.DYNAMIC && (key == null || key.isBlank())) {
			return ChatNodeResponseDto.finalText(
				"일시적으로 답변을 제공할 수 없어요. 잠시 후 다시 시도해 주세요.",
				false,
				key
			);
		}

		// 4) DYNAMIC → 핸들러 raw 생성 → GPT 다듬기
		var ctx = new HashMap<String, Object>();
		if (collegeId != null) {
			ctx.put("collegeId", collegeId);   // CHANGED: collegeId만 전달
		}
		if (deptId != null) {
			ctx.put("deptId", deptId);         // CHANGED: deptId만 전달
		}

		String raw = dispatcher.buildRaw(key, node.getId(), ctx);

		// 개요는 줄바꿈 유지
		if ("DEPT_OVERVIEW".equalsIgnoreCase(key)) {
			return ChatNodeResponseDto.finalText(raw, false, key);
		}

		log.info("[GPT API] ChatGPT 호출 시작 - key: {}, raw length: {}", key, raw.length());
		var gptResult = gpt.polish(raw, null);
		log.info("[GPT API] ChatGPT 호출 완료 - success: {}, outLen: {}", gptResult.success(),
			gptResult.text().length());

		return ChatNodeResponseDto.finalText(
			gptResult.text(),
			gptResult.success(),
			key
		);
	}
}