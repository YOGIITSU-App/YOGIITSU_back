package com.YOGIITSU.entity;

import com.YOGIITSU.enums.TurnType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "shortcut_coordinate")
public class ShortcutCoordinate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coordinate_id", updatable = false, nullable = false)
    private Long coordinateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shortcut_id", nullable = false)
    private Shortcut shortcut;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "point_order", nullable = false)
    private Integer pointOrder;

    @Column(name = "segment_distance")
    private Double segmentDistance; // 다음 좌표까지 거리 (단위: m)

    @Enumerated(EnumType.STRING)
    @Column(name = "turn_type", nullable = false)
    private TurnType turnType;    // 좌회전, 우회전, 직진 등

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;
}
