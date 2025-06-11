package com.dita.service;

import com.dita.dto.CommentDTO;
import java.util.List;

public interface CommentService {
    List<CommentDTO> getCommentsByBoardId(Long boardId);
    List<CommentDTO> getCommentsByBoardId(Long boardId, String memberId);
    void saveComment(CommentDTO comment);
    void saveComment(Long boardId, String memberId, String content, Integer parentId);

    // 댓글 좋아요 토글
    boolean toggleCommentLike(int commentId, String memberId);

    // 댓글 좋아요 수 조회
    int getLikeCount(int commentId);

    // 특정 유저가 해당 댓글에 좋아요 눌렀는지 확인
    boolean hasUserLiked(int commentId, String memberId);

    // 사용자 확인 포함
    boolean deleteCommentCascade(int commentId, String memberId);
    
    // 댓글 ID로 게시글 ID 조회
    Long getBoardIdByCommentId(int commentId);
    
    List<CommentDTO> getCommentsByMemberId(String memberId);

}