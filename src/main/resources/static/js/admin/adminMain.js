// 페이지 네비게이션 함수
function navigateTo(page, id) {
	if (id) {
		window.location.href = page + "?id=" + id;
	} else {
		window.location.href = page;
	}
}

// 가게 상세 페이지로 이동
function navigateToStoreDetail(rstId) {
	window.location.href = '/adminRstApproval?rstId=' + rstId;
}

// 신고 상세 팝업 열기
function openReportDetail(reportId) {
	const url = '/admin/reports/detail?reportId=' + reportId;
	const windowFeatures = 'width=650,height=700,resizable=no,scrollbars=yes';
	window.open(url, 'reportDetail', windowFeatures);
}


// 문의 상세 팝업 열기
function openInquiryDetail(inqId) {
	const url = '/admin/inquiries/detail?inqId=' + inqId;
	const windowFeatures = 'width=650,height=700,resizable=no,scrollbars=yes';
	window.open(url, 'inquiryDetail', windowFeatures);
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
	document.querySelectorAll('tr.clickable').forEach(function(tr) {
		tr.style.cursor = 'pointer';
		tr.addEventListener('click', function() {
			const id = this.getAttribute('data-id');
			window.location.href = '/adminInquiryDetail/' + id;  // 문의 관리의 경우
			// 다른 카드(사용자/가게/신고)도 이 안에서 조건부로 분기하거나,
			// 각 테이블별로 별도 클래스(data-user-id, data-rst-id) + 클릭 핸들러를 추가하세요.
		});
		});
		// 할 일 목록 클릭 이벤트 추가
		const taskItems = document.querySelectorAll('.tasks ul li');
		taskItems.forEach(item => {
			item.addEventListener('click', function() {
				const taskText = this.textContent.trim();

				if (taskText.includes('사용자 관리')) {
					window.location.href = '/admin/users';
				} else if (taskText.includes('신고 관리')) {
					window.location.href = '/adminReports';
				} else if (taskText.includes('가게 승인/관리')) {
					window.location.href = '/adminRstApproval';
				} else if (taskText.includes('문의 관리')) {
					window.location.href = '/admin/inquiries';
				}

			});
			// 호버 효과를 위한 커서 포인터 추가
			item.style.cursor = 'pointer';
		});

		// 테이블 행 호버 효과 개선
		const tableRows = document.querySelectorAll('table tr[onclick]');
		tableRows.forEach(row => {
			row.addEventListener('mouseenter', function() {
				this.style.backgroundColor = '#f0f8ff';
			});

			row.addEventListener('mouseleave', function() {
				this.style.backgroundColor = '';
			});
		});

		

	// 숫자 카운트 애니메이션 함수 (첫 번째 카운터 한 개만 처리)
	function animateCounter(el, target) {
		if (!el) return;
		let current = 0;
		if (isNaN(target)) {
			el.textContent = '0';
			return;
		}
		const step = Math.ceil(target / 20);
		const timer = setInterval(() => {
			current += step;
			if (current >= target) {
				el.textContent = target;
				clearInterval(timer);
			} else {
				el.textContent = current;
			}
		}, 50);
	}
	
	// 최신 대기 중인 신고를 표시하는 함수
	function updateLatestPendingReport() {
	    fetch('/admin/reports/latest')  // 서버에서 최신 대기 중인 신고 정보 가져옴
	        .then(response => response.json())
	        .then(report => {
	            const reportSection = document.querySelector('tbody');  // 신고 관리 카드의 tbody
	            const latestReportRow = document.querySelector('tr#latestPendingReport');  // 대기 중인 신고 row

	            if (report && reportSection) {
	                // 최신 신고 데이터 업데이트
	                latestReportRow.innerHTML = `
	                    <td>${report.reportId}</td>
	                    <td>${report.reporterMember.nickname}</td>
	                    <td>${report.reportedMember.nickname}</td>
	                    <td>${new Date(report.reportedAt).toLocaleDateString()}</td>
	                    <td>${report.status}</td>
	                    <td><button onclick="openReportDetail(${report.reportId})">상세 보기</button></td>
	                `;
	            }
	        })
	        .catch(error => {
	            console.error('Error fetching latest pending report:', error);
	            const reportSection = document.querySelector('tbody');
	            if (reportSection) {
	                reportSection.innerHTML = `<tr><td colspan="6" style="text-align: center;">대기 중인 신고를 불러오는 중 오류가 발생했습니다.</td></tr>`;
	            }
	        });
	}

	// 신고 상세 팝업 열기
	function openReportDetail(reportId) {
	    const url = '/admin/reports/detail?reportId=' + reportId;
	    const windowFeatures = 'width=650,height=700,resizable=no,scrollbars=yes';
	    window.open(url, 'reportDetail', windowFeatures);
	}

	// 페이지 로드 시 최신 대기 중인 신고를 불러오기
	document.addEventListener('DOMContentLoaded', function() {
	    updateLatestPendingReport();  // 최초 데이터 로드
	});

	
	function refreshDashboardData() {
		fetch('/admin/dashboard/refresh')
			.then(res => res.json())
			.then(data => {
				// 1) 사용자 수
				document.querySelector('.tasks ul li:nth-child(1) .yellow-text')
					.textContent = data.userCount;
				// 3) 대기 중인 가게 수
				document.querySelector('.tasks ul li:nth-child(3) .yellow-text')
					.textContent = data.pendingCount;
					// 대기 중인 신고 수
				document.querySelector('.tasks ul li:nth-child(2) .yellow-text')
					.textContent = data.reportCount;
			})
			.catch(console.error);
	}

	// 1분마다 자동 갱신
	setInterval(refreshDashboardData, 60 * 1000);

	// “오늘의 할 일” 클릭 매핑
	document.querySelectorAll('.tasks ul li').forEach(item => {
		item.addEventListener('click', () => {
			const txt = item.textContent;
			if (txt.includes('사용자 관리')) location.href = '/admin/users';
			else if (txt.includes('신고 관리')) location.href = '/adminReports';
			else if (txt.includes('가게 승인/관리')) location.href = '/adminRstApproval';
			else if (txt.includes('문의 관리')) location.href = '/adminInquiry';
		});
	});

	// 새로고침 없이 바로 숫자 보이도록 한 번 실행
	refreshDashboardData();

	// 자동 새로고침 (1분마다)
	setInterval(refreshDashboardData, 1 * 60 * 1000);

	// 키보드 단축키 지원
	document.addEventListener('keydown', function(e) {
		if (e.ctrlKey || e.metaKey) {
			switch (e.key) {
				case '1':
					e.preventDefault();
					window.location.href = '/admin/users';
					break;
				case '2':
					e.preventDefault();
					window.location.href = '/adminReports';
					break;
				case '3':
					e.preventDefault();
					window.location.href = '/admin/restaurants';
					break;
				case '4':
					e.preventDefault();
					window.location.href = '/admin/inquiries';
					break;
				case 'r':
					e.preventDefault();
					window.location.reload();
					break;
					}
			}
		});
	});