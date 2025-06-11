package com.dita.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@ToString
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer commentId;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private Timestamp createAt;

    @Column(name = "likes")
    private Integer likes = 0;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "parent_id")
    private Integer parentId;

    // Member 엔티티와 조인하려면 다음 추가 (선택사항)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;
}
