package com.YOGIITSU.repository;

import com.YOGIITSU.entity.ChatOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatOptionRepository extends JpaRepository<ChatOption, Long> {

	Optional<ChatOption> findByIdAndIsActiveTrue(Long id);

	List<ChatOption> findByParent_IdAndIsActiveTrueOrderByDisplayOrderAsc(Long parentId);
}