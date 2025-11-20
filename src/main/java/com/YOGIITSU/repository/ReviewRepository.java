package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Review;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	// 최신순으로 모든 리뷰 조회 (새 리뷰는 맨 첫번째)
	List<Review> findAllByOrderByCreatedAtDesc();

	// 최신순으로 페이지네이션된 리뷰 조회
	Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

}