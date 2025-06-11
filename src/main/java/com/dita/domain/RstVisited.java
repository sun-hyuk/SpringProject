package com.dita.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "rst_visited")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RstVisited {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visited_id")
    private Integer visitedId;

    @Column(name = "member_id", nullable = false, length = 50)
    private String memberId;

    @Column(name = "restaurant_id", nullable = false)
    private Integer restaurantId;

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt;
}