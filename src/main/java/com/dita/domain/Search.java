package com.dita.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "searchs")
@Getter @Setter
@NoArgsConstructor
public class Search {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    private Integer searchId;

    @Column(name = "keyword", length = 50, nullable = false)
    private String keyword;

    @Column(name = "search_at", nullable = false)
    private LocalDateTime searchAt;

    @Column(name = "member_id", length = 50)
    private String memberId;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @PrePersist
    public void prePersist() {
        if (this.searchAt == null) {
            this.searchAt = LocalDateTime.now();
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
    }
}