package com.dita.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
public class CommentDTO {
    private Integer commentId;
    private String content;
    private Timestamp createAt;
    private Integer likes;
    private String memberId;
    private Long boardId;
    private Integer parentId;

    private String nickname; // 조인용
    private String profile;  // 조인용
    private boolean liked;
    
    private String boardTitle;
}
