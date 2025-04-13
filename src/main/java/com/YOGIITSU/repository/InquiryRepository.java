package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByOrderByInquiryAtDesc();  // 문의 리스트 최신순 정렬

}
