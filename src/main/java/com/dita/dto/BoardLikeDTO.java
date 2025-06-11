package com.dita.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardLikeDTO {

    private Long likeId;
    private Long boardId;
    private String memberId;
    private LocalDateTime createdAt;
}
