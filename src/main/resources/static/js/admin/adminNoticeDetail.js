// 파일명: adminNoticeDetail.js
document.addEventListener('DOMContentLoaded', function() {
    // 1) 모달 닫기 버튼
    window.closeModal = function() {
        // 공지관리 목록으로 돌아가기
        // (모달이 단순 overlay라면, display:none 처리해도 되지만 여기서는 목록 페이지로 리다이렉트)
        window.location.href = '/adminNotices';
    };

    // 2) 공지 삭제 함수
    window.deleteNotice = function(id) {
        if (!id) return;
        if (confirm('정말 삭제하시겠습니까?')) {
            // POST 방식으로 삭제 요청
            const form = document.createElement('form');
            form.method = 'post';
            form.action = '/adminNoticeDelete';
            const hidden = document.createElement('input');
            hidden.type = 'hidden';
            hidden.name = 'id';
            hidden.value = id;
            form.appendChild(hidden);
            document.body.appendChild(form);
            form.submit();
        }
    };
});
