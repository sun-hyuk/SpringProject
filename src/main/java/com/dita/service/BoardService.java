package com.dita.service;

import com.dita.dto.BoardDTO;
import java.util.List;

public interface BoardService {
    List<BoardDTO> searchByTitle(String keyword);
    
    // 로그인된 회원 ID를 기준으로 좋아요 여부도 포함하여 전체 게시글 조회
    List<BoardDTO> getAllBoards(String memberId);

    BoardDTO getBoardDetail(Long boardId);
    BoardDTO getBoardDetail(Long boardId, String memberId);
    
    // 조회수 증가 메서드 추가
    void increaseViews(Long boardId);
    
    void decreaseLike(Long boardId);
    void increaseLike(Long boardId);
    boolean toggleBoardLike(Long boardId, String memberId);
    int getLikeCount(Long boardId);
    boolean hasUserLiked(Long boardId, String memberId);
    boolean deleteBoardWithComments(Long boardId, String memberId);
    boolean updateBoard(Long boardId, String title, String content, String memberId);
    void saveBoard(String title, String content, String memberId);
    void updateCommentCount(Long boardId);
    
    List<BoardDTO> getBoardsByMemberId(String memberId);
    void deleteBoardById(Long id);
    
    // 기본값 없이 사용하는 getAllBoards()도 추가하고 싶다면 default 메서드로 처리 가능
    default List<BoardDTO> getAllBoards() {
        return getAllBoards(null);
    }
}