package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Shortcut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortcutRepository extends JpaRepository<Shortcut, Long> {

}