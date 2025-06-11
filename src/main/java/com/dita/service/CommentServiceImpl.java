package com.dita.service;

import com.dita.domain.Comment;
import com.dita.dto.CommentDTO;
import com.dita.persistence.CommentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private DataSource dataSource;

    @Override
    @Transactional
    public void saveComment(CommentDTO comment) {
        Comment entity = new Comment();
        entity.setBoardId(comment.getBoardId());
        entity.setMemberId(comment.getMemberId());
        entity.setContent(comment.getContent());
        entity.setParentId(comment.getParentId());
        commentRepository.save(entity);
    }
    
    @Override
    @Transactional
    public void saveComment(Long boardId, String memberId, String content, Integer parentId) {
        Comment entity = new Comment();
        entity.setBoardId(boardId);
        entity.setMemberId(memberId);
        entity.setContent(content);
        entity.setParentId(parentId);
        commentRepository.save(entity);
    }
    
    @Override
    public List<CommentDTO> getCommentsByBoardId(Long boardId) {
        return commentRepository.findWithMemberByBoardIdOrderByCreateAtAsc(boardId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDTO> getCommentsByBoardId(Long boardId, String memberId) {
        return commentRepository.findWithMemberByBoardIdOrderByCreateAtAsc(boardId)
                .stream()
                .map(c -> toDTO(c, memberId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean toggleCommentLike(int commentId, String memberId) {
        System.out.println("댓글 좋아요 토글 - commentId: " + commentId + ", memberId: " + memberId);
        
        try {
            boolean currentlyLiked = commentRepository.existsLike(commentId, memberId);
            System.out.println("현재 좋아요 상태: " + currentlyLiked);
            
            if (currentlyLiked) {
                System.out.println("좋아요 취소 중...");
                commentRepository.deleteLike(commentId, memberId);
                commentRepository.decreaseLike(commentId);
                System.out.println("좋아요 취소 완료");
                return false;
            } else {
                System.out.println("좋아요 추가 중...");
                commentRepository.insertLike(commentId, memberId);
                commentRepository.increaseLike(commentId);
                System.out.println("좋아요 추가 완료");
                return true;
            }
        } catch (Exception e) {
            System.out.println("댓글 좋아요 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
            return false;  // 오류 시 현재 상태 유지
        }
    }

    @Override
    public int getLikeCount(int commentId) {
        return commentRepository.findLikesByCommentId(commentId);
    }

    @Override
    public boolean hasUserLiked(int commentId, String memberId) {
        return commentRepository.existsLike(commentId, memberId);
    }

    @Override
    @Transactional
    public boolean deleteCommentCascade(int commentId, String memberId) {
        System.out.println("댓글 삭제 요청 - commentId: " + commentId + ", memberId: " + memberId);
        
        try {
            // 1. 작성자 확인
            if (!commentRepository.isOwner(commentId, memberId)) {
                System.out.println("댓글 작성자가 아닙니다.");
                return false;
            }
            
            // 2. 대댓글들의 좋아요 먼저 삭제
            System.out.println("대댓글 좋아요 삭제 중...");
            commentRepository.deleteReplyLikes(commentId);
            
            // 3. 현재 댓글의 좋아요 삭제
            System.out.println("댓글 좋아요 삭제 중...");
            commentRepository.deleteCommentLikes(commentId);
            
            // 4. 대댓글 삭제
            System.out.println("대댓글 삭제 중...");
            commentRepository.deleteReplies(commentId);
            
            // 5. 댓글 삭제
            System.out.println("댓글 삭제 중...");
            commentRepository.deleteComment(commentId);
            
            System.out.println("댓글 삭제 완료");
            return true;
            
        } catch (Exception e) {
            System.out.println("댓글 삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 좋아요 없이 변환
    private CommentDTO toDTO(Comment c) {
        CommentDTO dto = new CommentDTO();
        dto.setCommentId(c.getCommentId());
        dto.setBoardId(c.getBoardId());
        dto.setMemberId(c.getMemberId());
        dto.setContent(c.getContent());
        dto.setCreateAt(c.getCreateAt());
        dto.setLikes(c.getLikes());
        dto.setParentId(c.getParentId());

        if (c.getMember() != null) {
            dto.setNickname(c.getMember().getNickname());
            dto.setProfile(c.getMember().getImage());
        } else {
            dto.setNickname("알 수 없음");
            dto.setProfile(null);
        }

        return dto;
    }

    // 좋아요 여부 포함
    private CommentDTO toDTO(Comment c, String memberId) {
        CommentDTO dto = toDTO(c); // 공통 부분 호출
        if (memberId != null) {
            dto.setLiked(commentRepository.existsLike(c.getCommentId(), memberId));
        } else {
            dto.setLiked(false);
        }
        return dto;
    }

    // 댓글 ID로 게시글 ID 조회 (삭제 시 필요)
    public Long getBoardIdByCommentId(int commentId) {
        return commentRepository.findBoardIdByCommentId(commentId);
    }
    
    @Override
    public List<CommentDTO> getCommentsByMemberId(String memberId) {
    	String sql = """
    	        SELECT c.*, m.nickname, m.image AS profile, b.title AS board_title
    	        FROM comments c
    	        JOIN member m ON c.member_id = m.member_id
    	        JOIN board b ON c.board_id = b.board_id
    	        WHERE c.member_id = ?
    	        ORDER BY c.create_at DESC
    	    """;

        List<CommentDTO> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, memberId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CommentDTO dto = new CommentDTO();
                dto.setCommentId(rs.getInt("comment_id"));
                dto.setContent(rs.getString("content"));
                dto.setCreateAt(rs.getTimestamp("create_at"));
                dto.setLikes(rs.getInt("likes"));
                dto.setMemberId(rs.getString("member_id"));
                dto.setBoardId(rs.getLong("board_id"));
                dto.setParentId(rs.getInt("parent_id"));
                dto.setNickname(rs.getString("nickname"));
                dto.setProfile(rs.getString("profile"));
                dto.setBoardTitle(rs.getString("board_title"));
                list.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}