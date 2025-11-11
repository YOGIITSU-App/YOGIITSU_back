package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	// 오래된 순으로 모든 리뷰 조회 (새 리뷰는 맨 마지막)
	List<Review> findAllByOrderByCreatedAtAsc();

}