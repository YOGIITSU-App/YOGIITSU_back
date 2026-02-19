package com.YOGIITSU.repository;

import com.YOGIITSU.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CollegeRepository extends JpaRepository<College, Long> {

	//이름순 정렬 목록
	List<College> findAllByOrderByNameAsc();
}