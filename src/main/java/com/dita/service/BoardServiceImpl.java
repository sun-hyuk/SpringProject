package com.dita.service;

import com.dita.domain.Board;
import com.dita.domain.Member;
import com.dita.dto.BoardDTO;
import com.dita.persistence.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BoardServiceImpl implements BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Override
    public List<BoardDTO> searchByTitle(String keyword) {
        return boardRepository.findByTitleContainingOrderByCreateAtDesc(keyword)
                .stream()
                .map(board -> convertToDTO(board, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<BoardDTO> getAllBoards() {
        return boardRepository.findAllByOrderByCreateAtDesc()
                .stream()
                .map(board -> convertToDTO(board, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<BoardDTO> getAllBoards(String memberId) {
        return boardRepository.findAllByOrderByCreateAtDesc()
                .stream()
                .map(board -> convertToDTO(board, memberId))
                .collect(Collectors.toList());
    }

    @Override
    public BoardDTO getBoardDetail(Long boardId, String memberId) {
        Optional<Board> opt = boardRepository.findDetailById(boardId);
        if (opt.isPresent()) {
            return convertToDTO(opt.get(), memberId);
        }
        return null;
    }
    
    @Override
    public BoardDTO getBoardDetail(Long boardId) {
        return getBoardDetail(boardId, null);
    }

    @Override
    @Transactional
    public void increaseLike(Long boardId) {
        boardRepository.increaseLike(boardId);
    }

    @Override
    @Transactional
    public void decreaseLike(Long boardId) {
        boardRepository.decreaseLike(boardId);
    }

    @Override
    @Transactional
    public boolean toggleBoardLike(Long boardId, String memberId) {
        if (boardRepository.existsLike(boardId, memberId)) {
            boardRepository.deleteLike(boardId, memberId);
            boardRepository.decreaseLike(boardId);
            return false;
        } else {
            boardRepository.insertLike(boardId, memberId);
            boardRepository.increaseLike(boardId);
            return true;
        }
    }

    @Override
    public int getLikeCount(Long boardId) {
        return boardRepository.findLikesByBoardId(boardId);
    }

    @Override
    public boolean hasUserLiked(Long boardId, String memberId) {
        return boardRepository.existsLike(boardId, memberId);
    }

    @Override
    @Transactional
    public boolean deleteBoardWithComments(Long boardId, String memberId) {
        System.out.println("삭제 요청 - 게시글 ID: " + boardId + ", 요청자 ID: " + memberId);
        
        // 게시글 작성자 확인
        String actualMemberId = boardRepository.findMemberIdByBoardId(boardId);
        System.out.println("실제 작성자 ID: " + actualMemberId);
        
        if (actualMemberId == null) {
            System.out.println("게시글을 찾을 수 없습니다.");
            return false;
        }
        
        if (!actualMemberId.equals(memberId)) {
            System.out.println("작성자가 아닙니다. 삭제 권한이 없습니다.");
            return false;
        }
        
        boolean result = boardRepository.deleteBoardAndComments(boardId, memberId);
        System.out.println("삭제 결과: " + result);
        return result;
    }

    @Override
    @Transactional
    public boolean updateBoard(Long boardId, String title, String content, String memberId) {
        Optional<Board> opt = boardRepository.findById(boardId);
        if (opt.isPresent()) {
            Board board = opt.get();
            // 작성자 확인
            if (!board.getMemberId().equals(memberId)) {
                return false;
            }
            board.setTitle(title);
            board.setContent(content);
            boardRepository.save(board);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void saveBoard(String title, String content, String memberId) {
        Board board = new Board();
        board.setTitle(title);
        board.setContent(content);
        board.setMemberId(memberId);  // writer 대신 memberId 설정
        boardRepository.save(board);
    }

    @Override
    @Transactional
    public void updateCommentCount(Long boardId) {
        int count = boardRepository.countCommentsByBoardId(boardId);
        boardRepository.updateCommentCount(boardId, count);
    }

    @Override
    @Transactional
    public void increaseViews(Long boardId) {
        boardRepository.increaseViews(boardId);
    }
    
    @Override
    public List<BoardDTO> getBoardsByMemberId(String memberId) {
    	List<Board> boardList = boardRepository.findByMemberId(memberId);

        return boardList.stream()
    		.map(board -> convertToDTO(board, memberId))  // Board → BoardDTO로 변환
            .collect(Collectors.toList());
    }

    @Override
    public void deleteBoardById(Long id) {
        boardRepository.deleteById(id);
    }
    
    private BoardDTO convertToDTO(Board board, String currentMemberId) {
        BoardDTO dto = new BoardDTO();
        dto.setBoardId(board.getBoardId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setCreateAt(board.getCreateAt());
        dto.setRegdate(board.getRegdate());
        dto.setUpdatedate(board.getUpdatedate());
        dto.setViews(board.getViews());
        dto.setLikes(board.getLikes());
        dto.setCommentCount(board.getCommentCount());
        dto.setMemberId(board.getMemberId());

        if (board.getMember() != null) {
            dto.setNickname(board.getMember().getNickname());
            dto.setImage(board.getMember().getImage());
        }

        dto.setLiked(currentMemberId != null && boardRepository.existsLike(board.getBoardId(), currentMemberId));

        return dto;
    }
}