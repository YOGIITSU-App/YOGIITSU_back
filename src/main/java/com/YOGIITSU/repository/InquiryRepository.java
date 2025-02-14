package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByMemberId(Long memberId);  // 특정 회원 문의 조회
    Optional<Inquiry> findByInquiryId(Long inquiryId);  // 특정 문의 1개 조회
}
