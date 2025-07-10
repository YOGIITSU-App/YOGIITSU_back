package com.YOGIITSU.repository;

import com.YOGIITSU.entity.ShortcutCoordinate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortcutCoordinateRepository extends JpaRepository<ShortcutCoordinate, Long> {

    /**
     * 특정 지름길 ID에 해당하는 지점들을 pointOrder 순으로 정렬해서 조회
     */
    @Query("SELECT sc FROM ShortcutCoordinate sc WHERE sc.shortcut.shortcutId = :shortcutId ORDER BY sc.pointOrder ASC")
    List<ShortcutCoordinate> findCoordinateByShortcutId(@Param("shortcutId") Long shortcutId);

}