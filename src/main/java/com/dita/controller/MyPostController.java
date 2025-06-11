package com.dita.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.dita.dto.BoardDTO;
import com.dita.dto.CommentDTO;
import com.dita.dto.MemberDTO;
import com.dita.service.BoardService;
import com.dita.service.CommentService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MyPostController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/myPost")
    public String myPostPage(Model model, HttpSession session) {
        MemberDTO loggedInMember = (MemberDTO) session.getAttribute("loggedInMember");
        if (loggedInMember == null) return "redirect:/login";

        String memberId = loggedInMember.getMemberId();

        List<BoardDTO> myPosts = boardService.getBoardsByMemberId(memberId);
        List<CommentDTO> myComments = commentService.getCommentsByMemberId(memberId);

        model.addAttribute("loggedInMember", loggedInMember);
        model.addAttribute("myPosts", myPosts);
        model.addAttribute("myComments", myComments);

        return "myPost";  // => myPost.html
    }
}