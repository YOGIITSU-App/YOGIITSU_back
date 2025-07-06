package com.YOGIITSU.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "shortcut")
public class Shortcut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shortcut_id", updatable = false, nullable = false)
    private Long shortcutId;

    @Column(name = "shortcut_name", nullable = false)
    private String shortcutName;

    @Column(name = "start_point", nullable = false)
    private String startPoint;

    @Column(name = "end_point", nullable = false)
    private String endPoint;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "distance", nullable = false)
    private Double distance;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "shortcut", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShortcutCoordinate> coordinates = new ArrayList<>();
}
