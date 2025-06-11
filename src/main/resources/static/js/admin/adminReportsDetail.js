/**
 * 신고 상세 정보 페이지 관련 JavaScript 함수
 */

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 텍스트 영역 높이 자동 조정
    const textareas = document.querySelectorAll('textarea[readonly]');
    textareas.forEach(textarea => {
        autoResizeTextarea(textarea);
    });
});

/**
 * 모달 창 닫기
 */
function closeModal() {
    // 부모 창 새로고침 (필요한 경우)
    if (window.opener && !window.opener.closed) {
        window.opener.location.reload();
    }
    
    // 팝업 창 닫기
    window.close();
}

/**
 * 신고 처리 완료 기능
 */
function processReport() {
    const reportId = document.getElementById('reportId').value;

    if (!confirm("이 신고를 '처리 완료' 상태로 변경하시겠습니까?")) {
        return;
    }

    fetch(`/adminReports/${reportId}/process`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        }
    })
    .then(res => {
        if (!res.ok) throw new Error("처리 실패");
        return res.json();
    })
    .then(data => {
        alert("신고가 처리 완료되었습니다.");
        window.close();  // 또는 closeModal();
    })
    .catch(err => {
        console.error(err);
        alert("처리 중 오류가 발생했습니다.");
    });
}


/**
 * 텍스트 영역 높이 자동 조정
 * @param {HTMLTextAreaElement} textarea 텍스트 영역 요소
 */
function autoResizeTextarea(textarea) {
    textarea.style.height = 'auto';
    textarea.style.height = (textarea.scrollHeight) + 'px';
}

/**
 * 알림 메시지 표시
 * @param {string} message 메시지 내용
 * @param {string} type 메시지 타입 ('success' 또는 'error')
 */
function showAlert(message, type = 'success') {
    // 기존 알림 제거
    const existingAlerts = document.querySelectorAll('.alert');
    existingAlerts.forEach(alert => alert.remove());
    
    // 새 알림 생성
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type === 'error' ? 'error' : 'success'}`;
    alertDiv.textContent = message;
    
    // 폼 앞에 알림 삽입
    const form = document.getElementById('processForm');
    if (form) {
        form.parentNode.insertBefore(alertDiv, form);
    } else {
        // 폼이 없는 경우 (이미 처리된 신고)
        const processInfo = document.querySelector('.process-info');
        if (processInfo) {
            processInfo.parentNode.insertBefore(alertDiv, processInfo);
        } else {
            // 그 외의 경우 모달 내부 맨 아래에 추가
            document.querySelector('.modal-content').appendChild(alertDiv);
        }
    }
    
    // 일정 시간 후 자동으로 알림 제거 (선택 사항)
    if (type === 'success') {
        setTimeout(() => {
            alertDiv.style.opacity = '0';
            setTimeout(() => alertDiv.remove(), 300);
        }, 5000);
    }
}

/**
 * 신고 거절
 */
function rejectReport() {
    const reportId = document.getElementById("reportId").value;

    if (!confirm("정말 이 신고를 거절하시겠습니까?")) {
        return;
    }

    fetch(`/adminReports/reject/${reportId}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        }
    })
    .then(res => {
        if (!res.ok) throw new Error("거절 처리 실패");
        return res.json();
    })
    .then(data => {
        alert("신고가 거절되었습니다.");
        window.close(); // 또는 모달 닫기
        // 필요시 location.reload() 또는 부모 창 새로고침
    })
    .catch(err => {
        console.error(err);
        alert("오류가 발생했습니다.");
    });
}
