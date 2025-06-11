/**
 * 신고 관리 페이지 관련 JavaScript 함수
 */

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 필터 폼 이벤트 설정
    setupFilterForm();
    
    // 신고 상세보기 버튼 이벤트 설정
    setupViewButtons();
    
    // 자동 선택 상태 유지
    maintainFilterSelections();
});

/**
 * 필터 폼 이벤트 설정
 */
function setupFilterForm() {
    const filterForm = document.getElementById('filterForm');
    if (!filterForm) return;
    
    // 셀렉트 박스 변경 시 자동 제출
    const selects = filterForm.querySelectorAll('select');
    selects.forEach(select => {
        select.addEventListener('change', function() {
            filterForm.submit();
        });
    });
    
    // 키워드 입력 후 엔터 키 입력 시 제출
    const keywordInput = document.getElementById('keywordInput');
    if (keywordInput) {
        keywordInput.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                filterForm.submit();
            }
        });
    }
}

/**
 * 신고 상세보기 버튼 이벤트 설정
 */
function setupViewButtons() {
    document.querySelectorAll('.view-btn').forEach(btn => {
        btn.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            
            const reportId = this.getAttribute('data-reportid');
            if (reportId) {
                openReportDetailPopup(reportId);
            }
        });
    });
}

/**
 * 신고 상세보기 팝업 열기
 * @param {string} reportId 신고 ID
 */
function openReportDetailPopup(reportId) {
    window.open(
        `/adminReports/detail/${reportId}`,
        'reportDetail',
        'width=650,height=700,resizable=no,scrollbars=yes'
    );
}

/**
 * 신고 삭제 기능
 * @param {string} reportId 신고 ID
 * @param {Event} event 이벤트 객체
 */
function deleteReport(reportId, event) {
    if (!confirm("정말 이 신고를 삭제하시겠습니까?")) {
        return;
    }

    fetch(`/adminReports/${reportId}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json"
        }
    })
    .then(res => {
        if (!res.ok) throw new Error("삭제 실패");
        return res.json();
    })
    .then(data => {
        alert("신고가 삭제되었습니다.");
        location.reload(); // 새로고침으로 리스트 갱신
    })
    .catch(err => {
        console.error(err);
        alert("삭제 중 오류가 발생했습니다.");
    });

    // 이벤트 버블링 방지 (예: 모달 열기 방지)
    event.stopPropagation();
}


/**
 * 알림 메시지 표시
 * @param {string} message 메시지 내용
 * @param {string} type 메시지 타입 ('success' 또는 'danger')
 */
function showAlert(message, type = 'success') {
    // 기존 알림 제거
    const existingAlerts = document.querySelectorAll('.alert');
    existingAlerts.forEach(alert => alert.remove());
    
    // 새 알림 생성
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    
    // 페이지에 삽입
    const reportSection = document.querySelector('.report-section');
    if (reportSection) {
        reportSection.insertBefore(alertDiv, reportSection.firstChild);
        
        // 자동 제거 타이머 (5초)
        setTimeout(() => {
            alertDiv.style.opacity = '0';
            alertDiv.style.transform = 'translateY(-10px)';
            
            setTimeout(() => {
                alertDiv.remove();
            }, 300);
        }, 5000);
    }
}

/**
 * URL 파라미터에 따라 필터 선택 상태 유지
 */
function maintainFilterSelections() {
    const urlParams = new URLSearchParams(window.location.search);
    
    // 상태 선택
    const status = urlParams.get('status');
    if (status) {
        const statusSelect = document.getElementById('statusSelect');
        if (statusSelect) {
            const option = Array.from(statusSelect.options).find(opt => opt.value === status);
            if (option) {
                option.selected = true;
            }
        }
    }
    
    // 유형 선택
    const type = urlParams.get('type');
    if (type) {
        const typeSelect = document.getElementById('typeSelect');
        if (typeSelect) {
            const option = Array.from(typeSelect.options).find(opt => opt.value === type);
            if (option) {
                option.selected = true;
            }
        }
    }
}