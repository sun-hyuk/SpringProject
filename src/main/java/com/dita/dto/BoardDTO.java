package com.dita.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoardDTO {

    private Long boardId;       // board.board_id
    private String title;       // board.title
    private String content;     // board.content
    private int views;          // board.views
    private int likes;          // board.likes
    private int commentCount;   // board.comment_count
    private String memberId;    // board.member_id (작성자 ID, 닉네임과 다름)
    private Timestamp createAt; // board.create_at
    private Timestamp regdate;  // board.regdate
    private Timestamp updatedate; // board.updatedate
    private String nickname;    // member.nickname (조인 시 추가)
    private String image;       // member.image (조인 시 추가)
    private boolean liked;
}