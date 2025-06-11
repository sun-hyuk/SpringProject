package com.dita.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Setter
@Getter
@ToString
@Table(name = "board")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;

    private String title;
    private String content;

    @CreationTimestamp
    @Column(name = "regdate", updatable = false)
    private Timestamp regdate;

    @UpdateTimestamp
    @Column(name = "updatedate")
    private Timestamp updatedate;

    @Column(name = "views")
    private int views;

    @Column(name = "likes")
    private int likes;

    @Column(name = "comment_count")
    private int commentCount;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "create_at", insertable = false, updatable = false)
    private Timestamp createAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;
}