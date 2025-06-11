package com.dita.persistence;

import com.dita.domain.Board;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 제목 검색
    List<Board> findByTitleContainingOrderByCreateAtDesc(String keyword);

    // 전체 목록 최신순
    List<Board> findAllByOrderByCreateAtDesc();

    List<Board> findByMemberId(String memberId);
    
    // 상세 조회 (작성자 정보 포함)
    @Query("SELECT b FROM Board b JOIN FETCH b.member WHERE b.boardId = :boardId")
    Optional<Board> findDetailById(@Param("boardId") Long boardId);

    // 좋아요 여부 확인
    @Query("SELECT COUNT(bl) > 0 FROM BoardLike bl WHERE bl.board.boardId = :boardId AND bl.member.memberId = :memberId")
    boolean existsLike(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    // 좋아요 수 조회
    @Query(value = "SELECT COUNT(*) FROM board_like WHERE board_id = :boardId", nativeQuery = true)
    int countLikes(@Param("boardId") Long boardId);
    
    @Query("SELECT b.likes FROM Board b WHERE b.boardId = :boardId")
    int findLikesByBoardId(@Param("boardId") Long boardId);
    
    @Query(value = "SELECT COUNT(*) FROM comments WHERE board_id = :boardId AND parent_id IS NULL", nativeQuery = true)
    int countCommentsByBoardId(@Param("boardId") Long boardId);
    
    // 조회수 증가
    @Modifying
    @Transactional
    @Query("UPDATE Board b SET b.views = b.views + 1 WHERE b.boardId = :boardId")
    void increaseViews(@Param("boardId") Long boardId);
    
    // 좋아요 추가 (insert만)
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO board_like (board_id, member_id) VALUES (:boardId, :memberId)", nativeQuery = true)
    void insertLike(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    // 좋아요 삭제
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM board_like WHERE board_id = :boardId AND member_id = :memberId", nativeQuery = true)
    void deleteLike(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    // 댓글 좋아요 삭제 (게시글 삭제 시)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comment_like WHERE comment_id IN (SELECT comment_id FROM comments WHERE board_id = :boardId)", nativeQuery = true)
    void deleteCommentLikesByBoardId(@Param("boardId") Long boardId);

    // 게시글 좋아요 삭제 (게시글 삭제 시)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM board_like WHERE board_id = :boardId", nativeQuery = true)
    void deleteBoardLikesByBoardId(@Param("boardId") Long boardId);

    // 댓글 삭제 (게시글 삭제 시)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comments WHERE board_id = :boardId", nativeQuery = true)
    void deleteCommentsByBoardId(@Param("boardId") Long boardId);

    // 게시글 삭제 (작성자 확인 포함)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM board WHERE board_id = :boardId AND member_id = :memberId", nativeQuery = true)
    int deleteBoardByBoardIdAndMemberId(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    // 게시글 작성자 확인
    @Query("SELECT b.memberId FROM Board b WHERE b.boardId = :boardId")
    String findMemberIdByBoardId(@Param("boardId") Long boardId);

    @Modifying
    @Transactional
    @Query("UPDATE Board b SET b.likes = b.likes + 1 WHERE b.boardId = :boardId")
    void increaseLike(@Param("boardId") Long boardId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Board b SET b.likes = CASE WHEN b.likes > 0 THEN b.likes - 1 ELSE 0 END WHERE b.boardId = :boardId")
    void decreaseLike(@Param("boardId") Long boardId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Board b SET b.commentCount = :count WHERE b.boardId = :boardId")
    void updateCommentCount(@Param("boardId") Long boardId, @Param("count") int count);
    
    // 커스텀 메서드: 게시글과 관련된 모든 데이터를 삭제
    default boolean deleteBoardAndComments(Long boardId, String memberId) {
        // 1. 작성자 확인
        String actualMemberId = findMemberIdByBoardId(boardId);
        if (actualMemberId == null || !actualMemberId.equals(memberId)) {
            return false;
        }
        
        // 2. 댓글 좋아요 삭제
        deleteCommentLikesByBoardId(boardId);
        
        // 3. 게시글 좋아요 삭제
        deleteBoardLikesByBoardId(boardId);
        
        // 4. 댓글 삭제
        deleteCommentsByBoardId(boardId);
        
        // 5. 게시글 삭제
        return deleteBoardByBoardIdAndMemberId(boardId, memberId) > 0;
    }
}