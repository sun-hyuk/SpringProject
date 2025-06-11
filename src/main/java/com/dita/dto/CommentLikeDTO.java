package com.dita.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentLikeDTO {

	private Long likeId;
    private Long commentId;
    private String memberId;
    private LocalDateTime createdAt;

}
