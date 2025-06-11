document.addEventListener('DOMContentLoaded', function() {
	const isLoggedIn = document.body.dataset.loggedIn === 'true';
	const memberId = document.body.dataset.memberId || '로그인 안됨';

	console.log('세션 로그인 상태:', isLoggedIn);
	console.log('로그인된 사용자 ID:', memberId);

	const alertsList = document.getElementById('alertsList');

	// 날짜별 그룹핑
	function groupAlertsByDate(alerts) {
		const grouped = {};
		alerts.forEach(alert => {
			const date = alert.createdAt?.split('T')[0] || '날짜 없음';
			if (!grouped[date]) grouped[date] = [];
			grouped[date].push(alert);
		});
		return grouped;
	}

	// 알림 텍스트 반환
	function getAlertText(alert) {
		switch (alert.type) {
			case '식당': return '가게 등록이 승인되었습니다.';
			case '문의': return '문의글에 대한 답변이 등록되었습니다.';
			case '신고': return '신고가 처리되었습니다.';
			default: return '새로운 알림이 있습니다.';
		}
	}

	// 알림 렌더링
	function renderAlerts(type) {
		const allItems = document.querySelectorAll('.alert-item');
		const allDividers = document.querySelectorAll('.date-divider');

		if (!isLoggedIn) {
			alertsList.innerHTML = '<div class="empty-message"><a href="/login" class="login-link">로그인</a>이 필요합니다.</div>';
			return;
		}

		let matchedCount = 0;

		allItems.forEach(item => {
			const itemType = item.getAttribute('data-type');
			if (type === 'all' || itemType === tabTypeToText(type)) {
				item.style.display = 'flex';
				matchedCount++;
			} else {
				item.style.display = 'none';
			}
		});

		// 날짜 구분선도 같이 숨김 처리
		allDividers.forEach(divider => {
			const next = divider.nextElementSibling;
			if (!next || next.style.display === 'none') {
				divider.style.display = 'none';
			} else {
				divider.style.display = 'block';
			}
		});

		// 없을 경우 메시지 표시
		if (matchedCount === 0) {
			alertsList.innerHTML = '<div class="empty-message">알림이 없습니다.</div>';
		}
	}
	
	function tabTypeToText(type) {
		const map = {
			store: '식당',
			inquiry: '문의',
			report: '신고'
		};
		return map[type] || '';
	}

	/* 알림 삭제 */
	document.addEventListener('click', function(e) {
		if (e.target.classList.contains('alert-close')) {
			const alertItem = e.target.closest('.alert-item');
			const alertId = alertItem?.getAttribute('data-alert-id'); // ← 이 부분 필요

			if (alertItem && alertId) {
				// 1. 서버에 삭제 요청
				fetch(`/alerts/delete/${alertId}`, { method: 'DELETE' })
					.then(res => {
						if (res.ok) {
							// 2. 애니메이션 + 제거
							alertItem.style.opacity = '0';
							setTimeout(() => {
								alertItem.remove();
								// 날짜 구분선 정리
								document.querySelectorAll('.date-divider').forEach(divider => {
									const next = divider.nextElementSibling;
									if (!next || next.classList.contains('date-divider')) {
										divider.remove();
									}
								});
							}, 300);
						} else {
							alert("삭제 실패");
						}
					})
					.catch(err => {
						console.error(err);
						alert("서버 오류");
					});
			}
		}
	});


	// 탭 클릭 이벤트
	document.querySelectorAll('.tab-button').forEach(button => {
		button.addEventListener('click', function() {
			document.querySelector('.tab-button.active')?.classList.remove('active');
			this.classList.add('active');
			const type = this.getAttribute('data-tab');
			renderAlerts(type);
		});
	});

	// 초기 렌더링
	renderAlerts('all');
});
