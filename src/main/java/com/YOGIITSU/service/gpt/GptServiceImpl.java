package com.YOGIITSU.service.gpt;

import com.YOGIITSU.config.OpenAiProperties;
import com.YOGIITSU.dto.gpt.GptRequestDto;
import com.YOGIITSU.dto.gpt.GptResponseDto;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.netty.channel.ChannelOption;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.List;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class GptServiceImpl implements GptService {

	private final OpenAiProperties props;
	private WebClient client;

	@PostConstruct
	void initClient() {
		var http = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
			.responseTimeout(Duration.ofSeconds(30));

		this.client = WebClient.builder()
			.baseUrl("https://api.openai.com/v1/chat/completions")
			.clientConnector(new ReactorClientHttpConnector(http))
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
			.build();
	}

	@CircuitBreaker(name = "gptBreaker", fallbackMethod = "fallback")
	@Retry(name = "gptRetry")
	@RateLimiter(name = "gptRate")
	@Bulkhead(name = "gptBulkhead")
	@Override
	public Result polish(String rawInfo, Duration timeout) {

		if (rawInfo == null || rawInfo.isBlank()) {
			return new Result(rawInfo, false);
		}

		log.debug("[GPT] model={}, timeoutSec={}, keyPresent={}",
			props.getModel(), props.getTimeoutSeconds(),
			props.getApiKey() != null && !props.getApiKey().isBlank());

		if (props.getApiKey() == null || props.getApiKey().isBlank()) {
			log.error("[GPT] API key is missing. Check openai.api-key / ${OPENAI_API_KEY}");
			return new Result(rawInfo, false);
		}

		String system = """
			역할: 당신은 대학 캠퍼스 안내 도우미입니다.
			규칙:
			- 제공된 정보만 사용하고 추측하지 마세요.
			- 건물명, 학과명, 위치(층/호수)는 원문 그대로 유지하세요.
			- 1~2문장, 한국어 존댓말, 불필요한 수식어 금지.
			""";
		String user = "정보:\n" + rawInfo;

		GptRequestDto req = new GptRequestDto(
			props.getModel(),
			List.of(
				new GptRequestDto.Message("system", system),
				new GptRequestDto.Message("user", user)
			),
			0.3
		);

		GptResponseDto resp = client.post()
			.bodyValue(req)
			.exchangeToMono(res -> {
				if (res.statusCode().is2xxSuccessful()) {
					return res.bodyToMono(GptResponseDto.class);
				}
				return res.bodyToMono(String.class).flatMap(body -> {
					String b = body != null ? body : "";
					int code = res.statusCode().value();

					if (code == 429) {
						String ra = res.headers().asHttpHeaders().getFirst("Retry-After");
						long retryAfterSec = parseLongSafe(ra);

						if (b.contains("insufficient_quota")) {
							log.error("[GPT] 429 insufficient_quota (quota exhausted). body={}", b);
							return Mono.error(new InsufficientQuotaException(b)); // 리트라이 X
						}
						log.warn("[GPT] 429 rate-limited. Retry-After={}s, body={}", retryAfterSec,
							b);
						return Mono.error(new TooManyRequestsException(b, retryAfterSec)); // 리트라이 O
					}

					log.error("[GPT] HTTP {} error: {}", res.statusCode(), b);
					return Mono.error(new RuntimeException("OpenAI error: " + res.statusCode()));
				});
			})
			.doOnError(err -> log.error("[GPT] HTTP call failed", err))
			.block(timeout != null ? timeout : Duration.ofSeconds(props.getTimeoutSeconds()));

		if (resp == null || resp.getChoices() == null || resp.getChoices().isEmpty()) {
			log.warn("[GPT] empty response/choices");
			return new Result(rawInfo, false);
		}

		var first = resp.getChoices().getFirst(); // Java 21
		var msg = first.getMessage();
		if (msg == null || msg.getContent() == null || msg.getContent().isBlank()) {
			log.warn("[GPT] empty content");
			return new Result(rawInfo, false);
		}

		String content = msg.getContent();
		log.info("[GPT] call end: outLen={}", content.length());
		return new Result(content, true);
	}

	@SuppressWarnings("unused")
	private Result fallback(String rawInfo, Duration timeout, Throwable ex) {
		log.warn("[GPT] fallback triggered: {}", ex.toString());
		return new Result(rawInfo, false);
	}

	// 안전한 long 파싱 util
	private static long parseLongSafe(String s) {
		if (s == null) {
			return 0;
		}
		try {
			return Long.parseLong(s.trim());
		} catch (Exception ignore) {
			return 0;
		}
	}
}