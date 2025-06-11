/**
 * 내가 작성한 글 페이지 기능 구현
 * - 탭 전환 기능
 * - 게시글/맛집 아이템 클릭 이벤트
 */
document.addEventListener('DOMContentLoaded', function() {
	const isLoggedIn = document.body.dataset.loggedIn === 'true';
	const memberId = document.body.dataset.memberId || '로그인 안됨';

	console.log('세션 로그인 상태:', isLoggedIn);
	console.log('로그인된 사용자 ID:', memberId);

	// 탭 전환 기능
	const tabButtons = document.querySelectorAll('.tab-btn');
	const tabPanes = document.querySelectorAll('.tab-pane');

	tabButtons.forEach(button => {
		button.addEventListener('click', function() {
			// 모든 탭 버튼과 패널 비활성화
			tabButtons.forEach(btn => btn.classList.remove('active'));
			tabPanes.forEach(pane => pane.classList.remove('active'));

			// 클릭한 탭 버튼과 해당 패널 활성화
			button.classList.add('active');
			const tabId = button.getAttribute('data-tab');
			document.getElementById(tabId).classList.add('active');
		});
	});

	// 게시글 아이템 클릭 이벤트
	setupItemClickEvents();

	// 더보기 버튼 이벤트
	setupMoreButtonEvents();
	
	// 댓글 탭 클릭
	setupCommentClickEvents();
});

/**
 * 게시글/클릭 이벤트 설정
 */
function setupItemClickEvents() {
  // 게시글 탭 아이템 클릭 이벤트
  const postItems = document.querySelectorAll('#tab1 .post-item');
  postItems.forEach(item => {
    item.addEventListener('click', function (e) {
      // 더보기 버튼이나 옵션 메뉴 클릭 시 제외
      if (e.target.closest('.btn-more') || e.target.closest('.options-menu')) {
        return;
      }

      const boardId = item.getAttribute('data-board-id');
      if (boardId) {
        window.location.href = `/boardDetail?boardId=${boardId}`; // 상세 페이지로 이동
      }
    });
  });
}

function setupCommentClickEvents() {
  const commentItems = document.querySelectorAll('#tab2 .post-item');
  commentItems.forEach(item => {
    item.addEventListener('click', function (e) {
      if (
        e.target.tagName === 'A' ||
        e.target.closest('a') ||
        e.target.closest('.btn-more') ||
        e.target.closest('.options-menu')
      ) {
        return;
      }
      const boardId = item.getAttribute('data-board-id');
      if (boardId) {
        window.location.href = `/boardDetail?boardId=${boardId}`;
      }
    });
  });
}

/**
 * 더보기 버튼 이벤트 설정
 */
function setupMoreButtonEvents() {
	const moreButtons = document.querySelectorAll('.btn-more');

	moreButtons.forEach((button, index) => {
		button.dataset.buttonId = `btn-${index}`;

		button.addEventListener('click', function (e) {
			e.stopPropagation();

			const postItem = button.closest('.post-item');
			const isComment = postItem.hasAttribute('data-comment-id');

			const existingMenu = document.querySelector('.options-menu');
			if (existingMenu) {
				if (existingMenu.dataset.buttonId === this.dataset.buttonId) {
					existingMenu.remove();
					this.classList.remove('active');
					return;
				}
				existingMenu.remove();
				document.querySelectorAll('.btn-more').forEach(btn => btn.classList.remove('active'));
			}

			this.classList.add('active');
			showOptionsMenu(this, isComment);
		});
	});
}

/**
 * 옵션 메뉴 표시
 */
function showOptionsMenu(button) {
	const postItem = button.closest('.post-item');

	// 옵션 메뉴 생성
	const optionsMenu = document.createElement('div');
	optionsMenu.className = 'options-menu';
	optionsMenu.dataset.buttonId = button.dataset.buttonId;

	// 모바일 친화적인 UI로 변경
	optionsMenu.innerHTML = `
    <button class="delete-post">
      <img src="/images/myPost/trash-icon.png" alt="삭제" onerror="this.src='/images/myPost/trash.png'; this.onerror=null;" />
      <span>삭제하기</span>
    </button>
  `;

	// 옵션 메뉴를 post-item에 추가
	postItem.appendChild(optionsMenu);

	// 삭제 버튼 이벤트 설정
	const deleteButton = optionsMenu.querySelector('.delete-post');

	deleteButton.addEventListener('click', function(e) {
		e.stopPropagation(); // 부모 요소 클릭 이벤트 전파 방지

		// 모바일 스타일 삭제 확인 모달 표시
		showDeleteConfirmModal(postItem);

		// 옵션 메뉴 닫기
		optionsMenu.remove();
		button.classList.remove('active');
	});

	// 다른 곳 클릭 시 옵션 메뉴 닫기
	document.addEventListener('click', function closeMenu(e) {
		if (!optionsMenu.contains(e.target) && e.target !== button) {
			optionsMenu.remove();
			button.classList.remove('active');
			document.removeEventListener('click', closeMenu);
		}
	});
}

/**
 * 게시글 삭제 확인 모달 표시
 */
function showDeleteConfirmModal(postItem) {
	const modal = document.createElement('div');
	modal.className = 'delete-modal';

	modal.innerHTML = `
    <div class="modal-content">
      <div class="modal-header">
        <h3>게시글 삭제</h3>
      </div>
      <div class="modal-body">
        <p>이 게시글을 정말 삭제하시겠습니까?</p>
      </div>
      <div class="modal-footer">
        <button class="modal-btn modal-btn-cancel">취소</button>
        <button class="modal-btn modal-btn-confirm">삭제</button>
      </div>
    </div>
  `;

	document.body.appendChild(modal);
	document.body.style.overflow = 'hidden';

	const cancelButton = modal.querySelector('.modal-btn-cancel');
	cancelButton.addEventListener('click', function () {
		closeModal(modal);
	});

	const confirmButton = modal.querySelector('.modal-btn-confirm');
	confirmButton.addEventListener('click', function () {
		const boardId = postItem.getAttribute('data-board-id');

		fetch(`/board/delete/${boardId}`, {
			method: 'DELETE'
		})
		.then(response => {
			if (!response.ok) {
				throw new Error('삭제 실패');
			}

			postItem.style.opacity = '0.5';
			postItem.style.transition = 'opacity 0.3s ease';

			closeModal(modal);

			setTimeout(() => {
				postItem.style.height = postItem.offsetHeight + 'px';
				postItem.style.overflow = 'hidden';
				postItem.style.paddingTop = '0';
				postItem.style.paddingBottom = '0';
				postItem.style.marginTop = '0';
				postItem.style.marginBottom = '0';
				postItem.style.transition = 'all 0.3s ease';

				setTimeout(() => {
					postItem.style.height = '0';

					setTimeout(() => {
						postItem.remove();
						showToast('게시글이 삭제되었습니다.');
					}, 300);
				}, 50);
			}, 300);
		})
		.catch(error => {
			console.error(error);
			closeModal(modal);
			showToast('삭제에 실패했습니다.');
		});
	});

	modal.addEventListener('click', function (e) {
		if (e.target === modal) {
			closeModal(modal);
		}
	});
}

/**
 * 모달 닫기
 */
function closeModal(modal) {
	modal.style.opacity = '0';
	setTimeout(() => {
		modal.remove();
		document.body.style.overflow = ''; // 배경 스크롤 복원
	}, 200);
}

/**
 * 토스트 메시지 표시
 */
function showToast(message) {
	// 이미 표시된 토스트가 있으면 제거
	const existingToast = document.querySelector('.toast-message');
	if (existingToast) {
		existingToast.remove();
	}

	// 토스트 메시지 생성
	const toast = document.createElement('div');
	toast.className = 'toast-message';
	toast.textContent = message;

	// 토스트 스타일 설정
	toast.style.position = 'fixed';
	toast.style.bottom = '80px';
	toast.style.left = '50%';
	toast.style.transform = 'translateX(-50%)';
	toast.style.backgroundColor = 'rgba(0, 0, 0, 0.7)';
	toast.style.color = '#fff';
	toast.style.padding = '12px 24px';
	toast.style.borderRadius = '8px';
	toast.style.fontSize = '15px';
	toast.style.fontWeight = '500';
	toast.style.zIndex = '2000';
	toast.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.2)';
	toast.style.opacity = '0';
	toast.style.transition = 'opacity 0.3s ease';

	// 토스트를 body에 추가
	document.body.appendChild(toast);

	// 토스트 표시 및 자동 제거
	setTimeout(() => {
		toast.style.opacity = '1';

		setTimeout(() => {
			toast.style.opacity = '0';

			setTimeout(() => {
				toast.remove();
			}, 300);
		}, 2000);
	}, 100);
}

function showOptionsMenu(button, isComment) {
	const postItem = button.closest('.post-item');
	const optionsMenu = document.createElement('div');
	optionsMenu.className = 'options-menu';
	optionsMenu.dataset.buttonId = button.dataset.buttonId;

	optionsMenu.innerHTML = `
    <button class="delete-post">
      <img src="/images/myPost/trash-icon.png" alt="삭제" />
      <span>삭제하기</span>
    </button>
  `;

	postItem.appendChild(optionsMenu);

	const deleteButton = optionsMenu.querySelector('.delete-post');
	deleteButton.addEventListener('click', function (e) {
		e.stopPropagation();

		if (isComment) {
			showDeleteCommentModal(postItem);
		} else {
			showDeleteConfirmModal(postItem); // 기존 게시글 삭제
		}

		optionsMenu.remove();
		button.classList.remove('active');
	});

	document.addEventListener('click', function closeMenu(e) {
		if (!optionsMenu.contains(e.target) && e.target !== button) {
			optionsMenu.remove();
			button.classList.remove('active');
			document.removeEventListener('click', closeMenu);
		}
	});
}

function showDeleteCommentModal(postItem) {
	const modal = document.createElement('div');
	modal.className = 'delete-modal';
	modal.innerHTML = `
    <div class="modal-content">
      <div class="modal-header"><h3>댓글 삭제</h3></div>
      <div class="modal-body"><p>이 댓글을 정말 삭제하시겠습니까?</p></div>
      <div class="modal-footer">
        <button class="modal-btn modal-btn-cancel">취소</button>
        <button class="modal-btn modal-btn-confirm">삭제</button>
      </div>
    </div>
  `;

	document.body.appendChild(modal);
	document.body.style.overflow = 'hidden';

	modal.querySelector('.modal-btn-cancel').addEventListener('click', () => closeModal(modal));

	modal.querySelector('.modal-btn-confirm').addEventListener('click', () => {
		const commentId = postItem.getAttribute('data-comment-id');

		fetch('/comment/delete', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			body: new URLSearchParams({ commentId: commentId })
		})
		.then(res => res.text())
		.then(result => {
			if (result === 'success') {
				postItem.remove();
				showToast('댓글이 삭제되었습니다.');
			} else {
				showToast('삭제 실패: ' + result);
			}
			closeModal(modal);
		})
		.catch(error => {
			console.error(error);
			showToast('삭제 중 오류 발생');
			closeModal(modal);
		});
	});

	modal.addEventListener('click', function (e) {
		if (e.target === modal) {
			closeModal(modal);
		}
	});
}
