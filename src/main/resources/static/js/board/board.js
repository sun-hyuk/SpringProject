document.addEventListener('DOMContentLoaded', function () {
    const isLoggedIn = document.body.dataset.loggedIn === 'true';
	const memberId = document.body.dataset.memberId || '로그인 안됨';
	    
    console.log('세션 로그인 상태:', isLoggedIn);
    console.log('로그인된 사용자 ID:', memberId);

    // 로그인 확인 후 글쓰기 페이지 이동 또는 팝업
    function checkLoginAndWrite() {
		if (!isLoggedIn) {
	        const popup = document.getElementById('loginRequiredPopup');
	        if (popup) {
	            popup.classList.add('active');
	        } else {
	            console.error('로그인 팝업 요소가 존재하지 않습니다.');
	        }
	    } else {
	        window.location.href = '/board/write';
	    }
    }

    function closePopup() {
        document.getElementById('loginPopup').style.display = 'none';
    }

    function goToLogin() {
        window.location.href = '/login';
    }

    // 팝업 닫기/로그인 버튼 연결 (전역으로도 등록)
    window.closePopup = closePopup;
    window.goToLogin = goToLogin;

    // 글쓰기 버튼 클릭 이벤트 연결
    document.querySelector('.write-btn-small')?.addEventListener('click', checkLoginAndWrite);
    document.querySelector('.fixed-write-btn')?.addEventListener('click', checkLoginAndWrite);

    // 글쓰기 페이지 뒤로가기 확인
    const backBtn = document.querySelector('.back-btn');
    if (backBtn) {
        backBtn.addEventListener('click', function () {
            if (window.location.pathname.includes('/write')) {
                const titleInput = document.getElementById('title');
                const contentInput = document.getElementById('content');
                if ((titleInput?.value.trim() || contentInput?.value.trim())) {
                    const confirmExit = confirm('작성 중인 내용이 있습니다. 페이지를 나가시겠습니까?');
                    if (!confirmExit) return;
                }
            }
            history.back();
        });
    }

    // 제목 글자 수 카운트
    const titleInput = document.getElementById('title');
    const titleCount = document.getElementById('titleCount');
    if (titleInput && titleCount) {
        titleInput.addEventListener('input', function () {
            const count = this.value.length;
            titleCount.textContent = count;
            titleCount.style.color = count >= 20 ? '#ff6b6b' : '#999';
        });
        titleInput.focus();
    }

    // 작성 완료 버튼
    const submitBtn = document.getElementById('submitBtn');
	const writeForm = document.getElementById('writeForm');
	
    if (submitBtn && writeForm) {
		submitBtn.addEventListener('click', function () {
	        if (!titleInput.value.trim()) {
	            alert('제목을 입력해주세요.');
	            titleInput.focus();
	            return;
	        }
			
			// 제목 글자 수 20자 초과 시 경고
			if (titleInput.value.length > 20) {
				alert('제목을 20자 이내로 작성해주세요.');
				titleInput.focus();
				return;
			}

	        const contentInput = document.getElementById('content');
	        if (!contentInput.value.trim()) {
	            alert('내용을 입력해주세요.');
	            contentInput.focus();
	            return;
	        }

	        // 콘솔에 값 확인
	        console.log('제출할 데이터:', {
	            title: titleInput.value,
	            content: contentInput.value
	        });

	        // 폼 제출
	        writeForm.submit();
	    });
    }
	
	// 게시글 좋아요 하트
	const boardLikeIcon = document.querySelector('.board-like-icon');
	if (boardLikeIcon) {
	    // 서버에서 전달된 liked 상태를 확인
	    const liked = boardLikeIcon.dataset.liked === 'true';
	    console.log('게시글 좋아요 상태:', liked);
	    
	    // 초기 하트 아이콘 설정
	    boardLikeIcon.src = liked ? '/images/board/heart-filled.png' : '/images/board/heart.png';

		boardLikeIcon.addEventListener('click', function () {
		    const boardId = new URLSearchParams(window.location.search).get('boardId');
		    const icon = this;
		    const countSpan = this.closest('.reaction-btn').querySelector('.reaction-count');

		    fetch('/board/like', {
		        method: 'POST',
		        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		        body: `boardId=${boardId}`
		    })
		    .then(res => res.json())
		    .then(data => {
		        console.log('좋아요 응답:', data);
		        icon.src = data.liked ? '/images/board/heart-filled.png' : '/images/board/heart.png';
		        icon.dataset.liked = data.liked;
		        if (countSpan) countSpan.textContent = data.likeCount;
		    })
		    .catch(err => console.error('게시글 좋아요 오류:', err));
		});
	}

	// 댓글 및 대댓글 좋아요 하트
	document.querySelectorAll('.comment-like-icon, .reply-like-icon').forEach(icon => {
	    icon.addEventListener('click', function () {
	        const commentId = this.closest('[data-comment-id]')?.dataset.commentId;
	        const iconEl = this;
	        const button = iconEl.closest('.reply-like');
	        const countSpan = button?.querySelector('.like-count');
			console.log("commentId for like:", commentId);
			
	        if (!commentId) {
	            console.error('댓글 ID(commentId)를 찾을 수 없습니다.');
	            return;
	        }

	        fetch('/comment/like', {
	            method: 'POST',
	            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	            body: new URLSearchParams({ commentId })
	        })
	        .then(res => res.json())
	        .then(data => {
	            iconEl.src = data.liked ? '/images/board/heart-filled.png' : '/images/board/heart.png';
	            iconEl.dataset.liked = data.liked;
	            if (countSpan) countSpan.textContent = data.likeCount;
	        })
	        .catch(err => console.error('댓글 좋아요 오류:', err));
	    });
	});

	// 대댓글 입력창 토글
    document.querySelectorAll('.reply-btn').forEach(button => {
		button.addEventListener('click', function () {
            const commentItem = this.closest('.comment-item');
            const replyBox = commentItem?.querySelector('.reply-input-wrapper');

            if (replyBox) {
                const isVisible = replyBox.style.display === 'flex';
                replyBox.style.display = isVisible ? 'none' : 'flex';
            }
        });
    });
	
	// 대댓글 등록
    document.querySelectorAll('.reply-send-btn').forEach(button => {
        button.addEventListener('click', function () {
            const wrapper = this.closest('.reply-input-wrapper');
            const input = wrapper.querySelector('.reply-input');
            const content = input.value.trim();
            if (!content) return alert("내용을 입력하세요.");

            const commentItem = this.closest('.comment-item');
            const parentCommentId = commentItem?.getAttribute('data-comment-id'); // 데이터 속성 필요
            const boardId = new URLSearchParams(window.location.search).get('boardId');

            fetch('/board/comment/write', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `boardId=${boardId}&content=${encodeURIComponent(content)}&parentId=${parentCommentId}`
            })
            .then(res => res.ok ? res.text() : Promise.reject(res))
            .then(data => {
                console.log('대댓글 등록 성공:', data);
                location.reload();
            })
            .catch(err => {
                console.error('대댓글 등록 실패:', err);
            });
        });
    });
	
	// 최상위 댓글 작성 처리
	const commentSendBtn = document.getElementById('sendCommentBtn');
	const commentInput = document.getElementById('commentInput');

	if (commentSendBtn && commentInput) {
	    commentSendBtn.addEventListener('click', function () {
			if (!isLoggedIn) {
			    const popup = document.getElementById('loginRequiredPopup');
			    if (popup) popup.classList.add('active');
			    return;
			}
			
	        const content = commentInput.value.trim();
	        if (!content) {
	            alert("댓글을 입력하세요.");
	            commentInput.focus();
	            return;
	        }

	        // 게시글 ID 추출 (URL에서 ?boardId=123 식으로 있다고 가정)
	        const boardId = new URLSearchParams(window.location.search).get('boardId');

	        fetch('/board/comment/write', {
	            method: 'POST',
	            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	            body: `boardId=${boardId}&content=${encodeURIComponent(content)}`
	        })
	        .then(res => res.ok ? res.text() : Promise.reject(res))
	        .then(data => {
	            console.log('댓글 등록 성공:', data);
	            location.reload();
	        })
	        .catch(err => {
	            console.error('댓글 등록 실패:', err);
	        });
	    });
	}
	
	document.querySelectorAll('.more-btn2').forEach(button => {
	    button.addEventListener('click', function (e) {
	        e.stopPropagation(); // 이벤트 버블링 방지
	        const menu = this.nextElementSibling;
	        document.querySelectorAll('.more-menu').forEach(m => m.style.display = 'none'); // 다른 메뉴 숨김
	        if (menu) menu.style.display = (menu.style.display === 'block') ? 'none' : 'block';
	    });
	});

	document.addEventListener('click', () => {
	    document.querySelectorAll('.more-menu').forEach(menu => {
	        menu.style.display = 'none';
	    });
	});

	// 삭제 버튼 클릭
	document.querySelectorAll('.delete-btn').forEach(button => {
	    button.addEventListener('click', function () {
	        const commentId = this.dataset.id;

	        if (confirm('정말 삭제하시겠습니까?')) {
	            fetch('/comment/delete', {
	                method: 'POST',
	                headers: {
	                    'Content-Type': 'application/x-www-form-urlencoded'
	                },
	                body: `commentId=${commentId}`
	            })
	            .then(res => {
	                if (res.ok) {
	                    console.log('댓글 삭제 성공');
	                    location.reload(); // 페이지 새로고침으로 댓글 수 자동 업데이트
	                } else {
	                    alert("삭제 실패");
	                }
	            })
	            .catch(err => {
	                console.error('삭제 오류:', err);
	                alert("삭제 중 오류가 발생했습니다.");
	            });
	        }
	    });
	});
	
	// 검색 기능
	const searchInput = document.querySelector('.search-input');

	if (searchInput) {
	    // 엔터 키 입력 시 검색
	    searchInput.addEventListener('keypress', function (e) {
	        if (e.key === 'Enter') {
	            const keyword = searchInput.value.trim();
	            if (keyword) {
	                window.location.href = `/board?keyword=${encodeURIComponent(keyword)}`;
        		} else {
		            window.location.href = `/board`;  // 빈 경우엔 검색어 없이 이동
		        }
	        }
	    });

	    // 검색 아이콘 클릭 시에도 검색
	    const searchIcon = document.querySelector('.search-icon');
	    if (searchIcon) {
	        searchIcon.addEventListener('click', function () {
	            const keyword = searchInput.value.trim();
	            if (keyword) {
	                window.location.href = `/board?keyword=${encodeURIComponent(keyword)}`;
	            }
	        });
	    }
	}
	
	// 게시글 more 메뉴 열기/닫기
	document.querySelector('.more-btn')?.addEventListener('click', function (e) {
		e.stopPropagation();
		const menu = this.nextElementSibling;
		document.querySelectorAll('.more-menu.post').forEach(m => m.style.display = 'none');
		if (menu) menu.style.display = (menu.style.display === 'block') ? 'none' : 'block';
	});

	// 바깥 클릭 시 닫기
	document.addEventListener('click', () => {
		document.querySelectorAll('.more-menu.post').forEach(menu => {
			menu.style.display = 'none';
		});
	});

	// 게시글 삭제
	document.querySelector('.delete-post-btn')?.addEventListener('click', function () {
		const boardId = this.dataset.id;
		console.log('삭제 요청 - 게시글 ID:', boardId);
		console.log('현재 로그인된 사용자 ID:', memberId);
		
		if (confirm("정말 삭제하시겠습니까?")) {
			fetch('/board/delete', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/x-www-form-urlencoded'
				},
				body: `boardId=${boardId}`
			})
			.then(res => {
				console.log('응답 상태:', res.status);
				return res.text();
			})
			.then(data => {
				console.log('서버 응답:', data);
				if (data === "삭제 완료") {
					alert("삭제되었습니다.");
					location.href = "/board";
				} else {
					alert(data);
				}
			})
			.catch(err => {
				console.error("삭제 오류:", err);
				alert("삭제 중 오류가 발생했습니다.");
			});
		}
	});

	// 게시글 수정
	document.querySelector('.edit-post-btn')?.addEventListener('click', function () {
	    // 제목
	    const titleView = document.getElementById('post-title-view');
	    const titleEdit = document.getElementById('post-title-edit');
	    titleEdit.value = titleView.textContent.trim();
	    titleView.style.display = 'none';
	    titleEdit.style.display = 'block';

	    // 본문
	    const bodyView = document.getElementById('post-body-view');
	    const bodyEdit = document.getElementById('post-body-edit');
	    bodyEdit.value = bodyView.innerText.trim().replaceAll('<br>', '\n');
	    bodyView.style.display = 'none';
	    bodyEdit.style.display = 'block';

	    // 버튼
	    document.getElementById('edit-controls').style.display = 'block';
	});
	
	// 수정 취소
	document.getElementById('cancel-edit-btn')?.addEventListener('click', () => {
	    document.getElementById('post-title-edit').style.display = 'none';
	    document.getElementById('post-body-edit').style.display = 'none';
	    document.getElementById('post-title-view').style.display = 'block';
	    document.getElementById('post-body-view').style.display = 'block';
	    document.getElementById('edit-controls').style.display = 'none';
	});

	// 수정 완료
	document.getElementById('save-edit-btn')?.addEventListener('click', () => {
	    const boardId = new URLSearchParams(window.location.search).get('boardId');
	    const title = document.getElementById('post-title-edit').value.trim();
	    const content = document.getElementById('post-body-edit').value.trim();

	    if (!title || !content) {
	        alert("제목과 내용을 모두 입력하세요.");
	        return;
	    }

	    fetch('/board/edit', {
	        method: 'POST',
	        headers: {
	            'Content-Type': 'application/x-www-form-urlencoded'
	        },
	        body: `boardId=${boardId}&title=${encodeURIComponent(title)}&content=${encodeURIComponent(content)}`
	    })
	    .then(res => res.ok ? location.reload() : alert("수정 실패"))
	    .catch(err => console.error("수정 오류:", err));
	});

    // 게시글 more 버튼
	const postContentEl = document.querySelector('.post-content');
	const postMoreBtn = postContentEl?.querySelector('.more-btn');
	const postAuthorId = postContentEl?.dataset.authorId?.trim() || '';
	const loggedInUserId = document.body.dataset.memberId?.trim() || '';

	console.log('작성자 ID:', postAuthorId);
	console.log('로그인된 사용자 ID:', loggedInUserId);
	
	if (postMoreBtn && (postAuthorId === '' || loggedInUserId === '' || postAuthorId !== loggedInUserId)) {
	    postMoreBtn.style.display = 'none';
	}

    // 댓글/대댓글 more 버튼 숨기기
    document.querySelectorAll('.comment-item, .comment-reply').forEach(el => {
        const authorId = el.dataset.authorId;
        const moreBtn = el.querySelector('.more-btn2');
        if (authorId !== loggedInUserId && moreBtn) {
            moreBtn.style.display = 'none';
        }
    });
	
	document.querySelectorAll('.reply-actions-top, .action-buttons').forEach(container => {
	    const visibleBtns = Array.from(container.querySelectorAll('.action-btn'))
	        .filter(btn => btn.style.display !== 'none');

	    visibleBtns.forEach(btn => btn.classList.remove('last-visible'));

	    if (visibleBtns.length > 0) {
	        visibleBtns[visibleBtns.length - 1].classList.add('last-visible');
	    }
	});
});