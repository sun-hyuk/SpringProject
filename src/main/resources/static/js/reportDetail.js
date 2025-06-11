// 로그인 상태 확인 (선택적 활용)
const isLoggedIn = document.body.dataset.loggedIn === 'true';
const memberId = document.body.dataset.memberId || '비회원';

// 모달 열기
function openReportDetail(data) {
	document.querySelector('#report-type').innerText = data.type || '';
	document.querySelector('#report-author').innerText = data.author || '';
	document.querySelector('#report-reporter').innerText = data.reporter || '';
	document.querySelector('#report-date').innerText = data.date || '';
	document.querySelector('#report-result').innerText = data.result || '';
	document.querySelector('#report-content').innerText = data.content || '';

	document.getElementById('reportModal').classList.add('show');
}

// 모달 닫기
function closeReportDetail() {
	document.getElementById('reportModal').classList.remove('show');
	// 초기화(선택)
	document.querySelectorAll('#reportModal .value, #report-result, #report-content').forEach(el => el.innerText = '');
}

// 모달 외부 클릭 시 닫기
window.addEventListener('click', function (event) {
	const modal = document.getElementById('reportModal');
	if (modal.classList.contains('show') && !document.querySelector('.modal-content').contains(event.target)) {
		closeReportDetail();
	}
});

// 버튼 이벤트 바인딩
document.addEventListener('DOMContentLoaded', () => {
	const buttons = document.querySelectorAll('.view-report-btn');

	buttons.forEach(button => {
		button.addEventListener('click', async () => {
			const reportId = button.dataset.reportId;
			if (!reportId) return;

			try {
				const res = await fetch(`/report/api/${reportId}`);
				if (!res.ok) throw new Error('신고 정보를 불러올 수 없습니다.');

				const data = await res.json();
				openReportDetail(data);
			} catch (err) {
				alert(err.message);
			}
		});
	});
});
