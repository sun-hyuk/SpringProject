// 파일명: adminNotices.js
document.addEventListener('DOMContentLoaded', function() {
    // 1) 필터 버튼(유형별 필터링) 기능 수정: select 변경 시 필터링
    const filterSelect = document.querySelector('.filter-select');
    const noticeRows   = document.querySelectorAll('.notice-table tbody tr:not(.no-data)');

    function applyTypeFilter() {
        const selectedType = filterSelect.value;  // "" 혹은 "공지", "이벤트", "점검"
        noticeRows.forEach(row => {
            // 2번째 열에 있는 공지 유형 셀 텍스트
            const typeCell = row.querySelector('td:nth-child(2)');
            const typeText = typeCell.textContent.trim();
            // selectedType이 빈 문자열이면 모두 보여주고, 특정 값이면 일치 여부로 판단
            if (selectedType === '' || typeText === selectedType) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
        // 타입 필터 후, 검색 결과도 다시 체크
        applySearchFilter();
    }
    if (filterSelect) {
        filterSelect.addEventListener('change', applyTypeFilter);
    }

    // 2) 검색 기능
    const searchInput = document.querySelector('.search-input');
    const searchBtn   = document.querySelector('.search-btn');

    function applySearchFilter() {
        const query = searchInput.value.trim().toLowerCase();
        noticeRows.forEach(row => {
            // 이미 'display: none' 처리된 행은 검색하지 않아도 됨
            if (row.style.display === 'none') return;

            const titleCell = row.querySelector('td:nth-child(3)');
            const titleText = titleCell.textContent.toLowerCase();
            if (titleText.includes(query)) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
        checkNoData();
    }

    if (searchBtn) {
        searchBtn.addEventListener('click', applySearchFilter);
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                applySearchFilter();
            }
        });
    }

    // 3) “등록된 공지가 없습니다” 표시 처리
    function checkNoData() {
        const tbody = document.querySelector('.notice-table tbody');
        let visibleCount = 0;
        noticeRows.forEach(row => {
            if (row.style.display !== 'none') visibleCount++;
        });

        let noDataRow = tbody.querySelector('.no-data-row');
        if (visibleCount === 0) {
            if (!noDataRow) {
                noDataRow = document.createElement('tr');
                noDataRow.className = 'no-data-row';
                const td = document.createElement('td');
                td.colSpan = 6;
                td.className = 'no-data';
                td.textContent = '등록된 공지가 없습니다.';
                noDataRow.appendChild(td);
                tbody.appendChild(noDataRow);
            }
            noDataRow.style.display = '';
        } else if (noDataRow) {
            noDataRow.style.display = 'none';
        }
    }

    // 4) 삭제 버튼 클릭 → 폼 전송
    const deleteButtons = document.querySelectorAll('.delete-btn');
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const noticeId = this.getAttribute('data-id');
            if (!noticeId) return;
            if (confirm('정말 삭제하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/adminNoticeDelete';
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'id';
                input.value = noticeId;
                form.appendChild(input);
                document.body.appendChild(form);
                form.submit();
            }
        });
    });

    // 5) 상세보기 버튼 클릭 → 모달 오픈 ( Ajax 로 처리하거나 새 URL 이동 )
    const detailButtons = document.querySelectorAll('.detail-btn');
    detailButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const noticeId = this.getAttribute('data-id');
            if (!noticeId) return;
            window.location.href = '/adminNoticeDetail/' + noticeId;
        });
    });

    // 6) 알림 메시지(성공/실패) 자동 사라짐
    const successMsg = document.querySelector('#successMessage');
    const errorMsg   = document.querySelector('#errorMessage');
    if (successMsg || errorMsg) {
        setTimeout(() => {
            if (successMsg) successMsg.style.display = 'none';
            if (errorMsg)   errorMsg.style.display = 'none';
        }, 5000);
    }

    // 페이지 로드 직후, 혹시 이미 검색어/필터 상태가 있으면 한번 실행
    applyTypeFilter();
});
