package com.dita.controller;

import com.dita.dto.CommentDTO;
import com.dita.dto.MemberDTO;
import com.dita.service.CommentService;
import com.dita.service.BoardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private BoardService boardService;

    // 댓글 저장
    @PostMapping("/write")
    public String writeComment(@ModelAttribute CommentDTO comment, HttpSession session) {
        if (comment.getParentId() == null || comment.getParentId() == 0) {
            comment.setParentId(null);
        }
        commentService.saveComment(comment);
        
        // 댓글 수 업데이트
        boardService.updateCommentCount(comment.getBoardId());
        
        return "redirect:/boardDetail?boardId=" + comment.getBoardId();
    }

    // 댓글 리스트 (Ajax)
    @GetMapping("/list")
    @ResponseBody
    public List<CommentDTO> getComments(@RequestParam("boardId") Long boardId) {
        return commentService.getCommentsByBoardId(boardId);
    }

    // 댓글 삭제
    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<?> deleteComment(@RequestParam int commentId, HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
        }

        // 댓글이 속한 게시글 ID 먼저 조회
        Long boardId = commentService.getBoardIdByCommentId(commentId);
        
        boolean result = commentService.deleteCommentCascade(commentId, member.getMemberId());
        
        if (result && boardId != null) {
            // 댓글 삭제 성공 시 댓글 수 업데이트
            boardService.updateCommentCount(boardId);
            System.out.println("게시글 " + boardId + "의 댓글 수 업데이트 완료");
        }
        
        return result ? ResponseEntity.ok("success") : ResponseEntity.status(403).body("삭제 권한 없음");
    }
    
    // 댓글 좋아요
    @PostMapping("/like")
    @ResponseBody
    public ResponseEntity<?> toggleLike(@RequestParam int commentId, HttpSession session) {
        System.out.println("댓글 좋아요 요청 받음 - commentId: " + commentId);
        
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            System.out.println("로그인되지 않은 사용자");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        System.out.println("로그인된 사용자: " + member.getMemberId());
        
        try {
            boolean liked = commentService.toggleCommentLike(commentId, member.getMemberId());
            int likeCount = commentService.getLikeCount(commentId);
            
            System.out.println("좋아요 처리 결과 - liked: " + liked + ", count: " + likeCount);

            return ResponseEntity.ok().body(Map.of(
                "liked", liked,
                "likeCount", likeCount
            ));
        } catch (Exception e) {
            System.out.println("댓글 좋아요 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("좋아요 처리 실패");
        }
    }
    
    @GetMapping("/my")
    public String myComments(HttpSession session, Model model) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) return "redirect:/login";

        List<CommentDTO> myComments = commentService.getCommentsByMemberId(member.getMemberId());
        model.addAttribute("myComments", myComments);
        model.addAttribute("loggedInMember", member);

        return "member/myPost"; // 탭 구성 유지
    }

}