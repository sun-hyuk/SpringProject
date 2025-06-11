package com.dita.controller;

import com.dita.dto.BoardDTO;
import com.dita.dto.CommentDTO;
import com.dita.dto.MemberDTO;
import com.dita.service.BoardService;
import com.dita.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("")
public class BoardController {

    @Autowired
    private BoardService boardService;
    @Autowired
    private CommentService commentService;

    @GetMapping("/board")
    public String boardMain(@RequestParam(value = "keyword", required = false) String keyword,
                            HttpSession session, Model model) {
        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");
        if (memberDto != null) {
            model.addAttribute("loggedInMember", memberDto);
        }

        // 게시글 목록 조회 (검색 또는 전체)
        List<BoardDTO> boardList;
        if (keyword != null && !keyword.trim().isEmpty()) {
            boardList = boardService.searchByTitle(keyword);
        } else {
            boardList = (memberDto != null) 
                ? boardService.getAllBoards(memberDto.getMemberId())
                : boardService.getAllBoards();
        }
        
        model.addAttribute("boardList", boardList);

        return "board/board";
    }

    @GetMapping("/boardDetail")
    public String boardDetail(@RequestParam("boardId") Long boardId,
                               HttpSession session, Model model) {
        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");
        if (memberDto != null) {
            model.addAttribute("loggedInMember", memberDto);
        }

        // 조회수 증가
        boardService.increaseViews(boardId);

        // 게시글 조회 - 로그인 사용자의 좋아요 상태 포함
        BoardDTO board = boardService.getBoardDetail(boardId);
        if (board == null) {
            return "redirect:/board";
        }

        // 로그인된 사용자의 좋아요 상태 확인
        if (memberDto != null) {
            boolean liked = boardService.hasUserLiked(boardId, memberDto.getMemberId());
            board.setLiked(liked);
        }

        List<CommentDTO> commentList = (memberDto != null)
        	    ? commentService.getCommentsByBoardId(boardId, memberDto.getMemberId())
        	    : commentService.getCommentsByBoardId(boardId);
        model.addAttribute("board", board);
        model.addAttribute("commentList", commentList);

        return "board/boardDetail";
    }
    
    @GetMapping("/boardDetail/{id}")
    public String getBoardDetail(@PathVariable Long id, Model model, HttpSession session) {
        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");
        if (memberDto != null) {
            model.addAttribute("loggedInMember", memberDto);
        }

        boardService.increaseViews(id);
        BoardDTO board = boardService.getBoardDetail(id);
        if (board == null) {
            return "redirect:/board";
        }

        if (memberDto != null) {
            boolean liked = boardService.hasUserLiked(id, memberDto.getMemberId());
            board.setLiked(liked);
        }

        List<CommentDTO> commentList = (memberDto != null)
            ? commentService.getCommentsByBoardId(id, memberDto.getMemberId())
            : commentService.getCommentsByBoardId(id);
        model.addAttribute("board", board);
        model.addAttribute("commentList", commentList);

        return "board/boardDetail";
    }

    @PostMapping("/board/write")
    public String submitPost(HttpSession session,
                             @RequestParam String title,
                             @RequestParam String content,
                             Model model) {
        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");
        if (memberDto == null) {
            return "redirect:/login";
        }

        System.out.println("글 작성 - 제목: " + title + ", 내용: " + content + ", 작성자 ID: " + memberDto.getMemberId());
        
        boardService.saveBoard(title, content, memberDto.getMemberId());
        return "redirect:/board";
    }

    @GetMapping("/board/write")
    public String boardWrite(HttpSession session, Model model) {
        MemberDTO memberDto = (MemberDTO) session.getAttribute("loggedInMember");
        if (memberDto == null) {
            return "redirect:/board";
        }
        model.addAttribute("loggedInMember", memberDto);
        return "board/boardWrite";
    }

    @GetMapping("/boardWrite")
    public String boardWriteAlternative(HttpSession session, Model model) {
        return boardWrite(session, model);
    }

    @PostMapping("/board/like")
    @ResponseBody
    public ResponseEntity<?> toggleBoardLike(@RequestParam Long boardId, HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
        }

        boolean liked = boardService.toggleBoardLike(boardId, member.getMemberId());
        int updatedLikeCount = boardService.getLikeCount(boardId);

        return ResponseEntity.ok(Map.of("liked", liked, "likeCount", updatedLikeCount));
    }

    @PostMapping("/board/comment/write")
    @ResponseBody
    public String writeComment(@RequestParam Long boardId,
                               @RequestParam String content,
                               @RequestParam(required = false) Integer parentId,
                               HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) return "로그인 필요";

        commentService.saveComment(boardId, member.getMemberId(), content, parentId);
        boardService.updateCommentCount(boardId);
        return "success";
    }

    @PostMapping("/board/delete")
    @ResponseBody
    public ResponseEntity<String> deleteBoard(@RequestParam("boardId") Long boardId,
                                              @SessionAttribute(name = "loggedInMember", required = false) MemberDTO loginMember) {
        System.out.println("삭제 요청 받음 - 게시글 ID: " + boardId);
        
        if (loginMember == null) {
            System.out.println("로그인되지 않은 사용자입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
        }
        
        System.out.println("로그인된 사용자 ID: " + loginMember.getMemberId());
        
        boolean result = boardService.deleteBoardWithComments(boardId, loginMember.getMemberId());
        
        if (result) {
            System.out.println("삭제 성공");
            return ResponseEntity.ok("삭제 완료");
        } else {
            System.out.println("삭제 실패");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 실패 또는 권한 없음");
        }
    }

    @PostMapping("/board/edit")
    @ResponseBody
    public ResponseEntity<String> editPost(@RequestParam Long boardId,
                                           @RequestParam String title,
                                           @RequestParam String content,
                                           HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);

        boolean success = boardService.updateBoard(boardId, title, content, member.getMemberId());
        return success ? ResponseEntity.ok("수정 완료") : new ResponseEntity<>("실패", HttpStatus.FORBIDDEN);
    }
    
    @DeleteMapping("/board/delete/{id}")
    public ResponseEntity<?> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoardById(id);
        return ResponseEntity.ok().build();
    }
}