package com.dita.persistence;

import com.dita.domain.Comment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // 댓글 목록 + 작성자 정보 함께 조회
    @Query("SELECT c FROM Comment c JOIN FETCH c.member WHERE c.boardId = :boardId ORDER BY c.createAt ASC")
    List<Comment> findWithMemberByBoardIdOrderByCreateAtAsc(@Param("boardId") Long boardId);

    // 특정 회원이 작성한 댓글 목록 조회
    List<Comment> findByMember_MemberIdOrderByCreateAtDesc(String memberId);
    
    // 좋아요 여부
    @Query("SELECT COUNT(cl) > 0 FROM CommentLike cl WHERE cl.comment.commentId = :commentId AND cl.member.memberId = :memberId")
    boolean existsLike(@Param("commentId") int commentId, @Param("memberId") String memberId);

    // 좋아요 수
    @Query("SELECT c.likes FROM Comment c WHERE c.commentId = :commentId")
    int findLikesByCommentId(@Param("commentId") int commentId);

    // 좋아요 등록
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO comment_like (comment_id, member_id, created_at) VALUES (:commentId, :memberId, NOW())", nativeQuery = true)
    void insertLike(@Param("commentId") int commentId, @Param("memberId") String memberId);

    // 좋아요 취소
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comment_like WHERE comment_id = :commentId AND member_id = :memberId", nativeQuery = true)
    void deleteLike(@Param("commentId") int commentId, @Param("memberId") String memberId);

    // 좋아요 감소
    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.likes = CASE WHEN c.likes > 0 THEN c.likes - 1 ELSE 0 END WHERE c.commentId = :commentId")
    void decreaseLike(@Param("commentId") int commentId);

    // 좋아요 증가
    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.likes = c.likes + 1 WHERE c.commentId = :commentId")
    void increaseLike(@Param("commentId") int commentId);

    // 댓글 주인 확인
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Comment c WHERE c.commentId = :commentId AND c.memberId = :memberId")
    boolean isOwner(@Param("commentId") int commentId, @Param("memberId") String memberId);

    // 댓글 좋아요 먼저 삭제 (외래키 제약조건 때문)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comment_like WHERE comment_id = :commentId", nativeQuery = true)
    void deleteCommentLikes(@Param("commentId") int commentId);

    // 대댓글의 좋아요도 삭제
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comment_like WHERE comment_id IN (SELECT comment_id FROM comments WHERE parent_id = :commentId)", nativeQuery = true)
    void deleteReplyLikes(@Param("commentId") int commentId);

    // 대댓글 삭제
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comments WHERE parent_id = :commentId", nativeQuery = true)
    void deleteReplies(@Param("commentId") int commentId);

    // 댓글 ID로 게시글 ID 조회
    @Query("SELECT c.boardId FROM Comment c WHERE c.commentId = :commentId")
    Long findBoardIdByCommentId(@Param("commentId") int commentId);

    // 댓글 삭제
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comments WHERE comment_id = :commentId", nativeQuery = true)
    void deleteComment(@Param("commentId") int commentId);
}