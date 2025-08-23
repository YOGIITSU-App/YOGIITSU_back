package com.YOGIITSU.entity;

import com.YOGIITSU.enums.ResponseType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_option")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatOption {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "option_text", nullable = false)
	private String optionText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private ChatOption parent;

	@Enumerated(EnumType.STRING)
	@Column(name = "response_type", nullable = false)
	@Builder.Default
	private ResponseType responseType = ResponseType.STATIC; // STATIC, DYNAMIC

	@Column(name = "response_text", columnDefinition = "TEXT")
	private String responseText;

	@Column(name = "response_key")
	private String responseKey;

	@Column(name = "display_order", nullable = false)
	@Builder.Default
	private Integer displayOrder = 0;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private boolean isActive = true;

	private String icon;
	private String intentCode;
}