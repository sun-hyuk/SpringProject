/**
 * 문의 관리 페이지 관련 JavaScript 함수
 */

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 상세보기 버튼 클릭 이벤트
    document.querySelectorAll('.view-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const inqId = this.getAttribute('data-inqid');
            if (inqId) {
                openInquiryDetailPopup(inqId);
            }
        });
    });

    // 검색 폼 이벤트
    const filterForm = document.getElementById('filterForm');
    if (filterForm) {
        // 엔터 키 검색 처리
        const keywordInput = filterForm.querySelector('input[name="keyword"]');
        if (keywordInput) {
            keywordInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    filterForm.submit();
                }
            });
        }

        // 셀렉트 박스 자동 서브밋 처리
        const selectBoxes = filterForm.querySelectorAll('select');
        selectBoxes.forEach(select => {
            select.addEventListener('change', function() {
                filterForm.submit();
            });
        });
    }
});

/**
 * 문의 상세 정보 팝업 창을 엽니다.
 * @param {string} inqId 문의 ID
 */
function openInquiryDetailPopup(inqId) {
    const url = `/admin/inquiry/detail/${inqId}`;
    const popupName = 'inquiryDetail';
    const popupOptions = 'width=650,height=700,resizable=no,scrollbars=yes';
    
    window.open(url, popupName, popupOptions);
}

/**
 * 답변 상태 업데이트 함수
 * @param {string} inqId 문의 ID
 * @param {string} status 상태값 ('대기중' 또는 '완료')
 */
function updateInquiryStatus(inqId, status) {
    if (!inqId) return;
    
    fetch(`/api/admin/inquiry/${inqId}/status`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
        },
        body: JSON.stringify({ status: status })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('상태 업데이트에 실패했습니다.');
        }
        return response.json();
    })
    .then(data => {
        // 성공 시 알림
        alert('상태가 업데이트되었습니다.');
        
        // 부모 창 새로고침 (팝업에서 호출했을 경우)
        if (window.opener && !window.opener.closed) {
            window.opener.location.reload();
        } else {
            // 현재 페이지 새로고침
            window.location.reload();
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('상태 업데이트 중 오류가 발생했습니다: ' + error.message);
    });
}

/**
 * 문의 답변 등록 함수
 * @param {string} inqId 문의 ID
 * @param {string} replyContent 답변 내용
 */
function submitInquiryReply(inqId, replyContent) {
    if (!inqId || !replyContent) {
        alert('답변 내용을 입력해주세요.');
        return;
    }
    
    fetch(`/api/admin/inquiry/${inqId}/reply`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
        },
        body: JSON.stringify({ replyContent: replyContent })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('답변 등록에 실패했습니다.');
        }
        return response.json();
    })
    .then(data => {
        // 성공 시 알림
        alert('답변이 등록되었습니다.');
        
        // 부모 창 새로고침 (팝업에서 호출했을 경우)
        if (window.opener && !window.opener.closed) {
            window.opener.location.reload();
        }
        
        // 현재 창 닫기 (팝업인 경우)
        window.close();
    })
    .catch(error => {
        console.error('Error:', error);
        alert('답변 등록 중 오류가 발생했습니다: ' + error.message);
    });
}