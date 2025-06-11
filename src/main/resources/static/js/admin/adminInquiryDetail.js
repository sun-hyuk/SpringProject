/**
 * 문의 상세 정보 페이지 관련 JavaScript 함수
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
 * 문의 답변 등록
 */
function submitInquiryReply() {
    const inquiryId = document.getElementById('inquiryId').value;
    const replyContent = document.getElementById('replyContent').value.trim();
    
    if (!replyContent) {
        showAlert('답변 내용을 입력해주세요.', 'error');
        return;
    }
    
    // 답변 등록 처리 - 실제로는 서버 API 호출
    // 테스트 목적으로 가상 응답 표시
    showAlert('답변이 성공적으로 등록되었습니다.', 'success');
    
    // 3초 후 창 닫기
    setTimeout(() => {
        closeModal();
    }, 3000);
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
 * @param {string} message 표시할 메시지
 * @param {string} type 메시지 타입 ('success' 또는 'error')
 */
function showAlert(message, type = 'success') {
    // 기존 알림 제거
    const existingAlerts = document.querySelectorAll('.alert');
    existingAlerts.forEach(alert => alert.remove());
    
    // 새 알림 생성
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    
    // 폼 앞에 알림 삽입
    const form = document.getElementById('replyForm');
    if (form) {
        form.parentNode.insertBefore(alertDiv, form);
    } else {
        // 폼이 없는 경우 (이미 처리된 문의)
        const replyContent = document.querySelector('.reply-content');
        if (replyContent) {
            replyContent.parentNode.insertBefore(alertDiv, replyContent);
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