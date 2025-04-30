package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Inquiry 엔티티 전용 Repository
 */
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    /**
     * 전체 문의 리스트를 작성일 기준 내림차순으로 조회
     * @return
     */
    List<Inquiry> findAllByOrderByInquiryAtDesc();

}
