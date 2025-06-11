// 전역 변수
let currentSortedBtn = null;  // 마지막으로 클릭된 버튼
let currentSortAsc  = true;   // 마지막 정렬 방향
let currentIndex = 0;
const pageSize = 10;

// 더보기 기능 (페이지네이션 대신 사용할 경우)
function loadMore() {
    const rows = document.querySelectorAll(".member-row");
    let shown = 0;
    for (let i = currentIndex; i < rows.length && shown < pageSize; i++) {
        if (rows[i].style.display === "none") {
            rows[i].style.display = "table-row";
            shown++;
            currentIndex++;
        }
    }
    if (currentIndex >= rows.length) {
        const loadMoreBtn = document.getElementById("loadMoreBtn");
        if (loadMoreBtn) {
            loadMoreBtn.style.display = "none";
        }
    }
}

// 테이블 정렬 기능
function sortTable(button, colIndex) {
    const table = document.querySelector("table");
    const tbody = table.tBodies[0];
    // colspan 있는 빈 행 제외
    const rows = Array.from(tbody.rows)
        .filter(row => !row.querySelector("td[colspan]"));

    // 같은 버튼을 눌렀으면 방향 토글, 아니면 항상 오름차순부터
    const ascending = (button === currentSortedBtn) ? !currentSortAsc : true;

    rows.sort((a, b) => {
        const aText = a.cells[colIndex].innerText.trim();
        const bText = b.cells[colIndex].innerText.trim();
        let diff;

        // 가입일(날짜)과 리뷰 수(숫자) 칼럼 처리
        if (colIndex === /* 가입일 칼럼 인덱스 */ 2) {
            diff = new Date(aText) - new Date(bText);
        } else if (colIndex === /* 리뷰 수 칼럼 인덱스 */ 3) {
            diff = parseInt(aText, 10) - parseInt(bText, 10);
        } else {
            diff = aText.localeCompare(bText);
        }
        return ascending ? diff : -diff;
    });

    // 정렬된 행을 다시 붙여넣기
    tbody.innerHTML = "";
    rows.forEach(r => tbody.appendChild(r));

    // 모든 th 버튼을 기본 ▲로 초기화
    document.querySelectorAll("th button").forEach(btn => btn.textContent = "▲");
    // 클릭한 버튼만 현재 방향 표시
    button.textContent = ascending ? "▲" : "▼";

    // 상태 업데이트
    currentSortedBtn = button;
    currentSortAsc    = ascending;
}

// 전체 선택/해제
function toggleAll(source) {
    const checkboxes = document.querySelectorAll('input[name="member_id"]');
    checkboxes.forEach(cb => cb.checked = source.checked);
}

// 삭제 확인
function checkDelete(e) {
    const deleteBtnClicked = e.submitter && e.submitter.name === "delete";
    if (deleteBtnClicked) {
        const checkedBoxes = document.querySelectorAll('input[name="member_id"]:checked');
        if (checkedBoxes.length === 0) {
            alert("삭제할 회원을 선택해주세요.");
            return false;
        }
        return confirm(`선택한 ${checkedBoxes.length}명의 회원을 정말 삭제하시겠습니까?`);
    }
    // 검색 버튼이면 그냥 통과
    return true;
}

// 날짜 검증
function validateDateRange() {
    const startDate = document.querySelector('input[name="startDate"]').value;
    const endDate = document.querySelector('input[name="endDate"]').value;
    
    if (startDate && endDate) {
        if (new Date(startDate) > new Date(endDate)) {
            alert("시작일이 종료일보다 늦을 수 없습니다.");
            return false;
        }
    }
    return true;
}

// 검색 폼 개선
function enhanceSearchForm() {
    const form = document.querySelector('form');
    const searchButton = form.querySelector('button[type="submit"]:not([name="delete"])');
    
    if (searchButton) {
        searchButton.addEventListener('click', function(e) {
            if (!validateDateRange()) {
                e.preventDefault();
            }
        });
    }
}

// 키보드 단축키
function setupKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + A: 전체 선택
        if ((e.ctrlKey || e.metaKey) && e.key === 'a') {
            e.preventDefault();
            const selectAllCheckbox = document.querySelector('th input[type="checkbox"]');
            if (selectAllCheckbox) {
                selectAllCheckbox.checked = !selectAllCheckbox.checked;
                toggleAll(selectAllCheckbox);
            }
        }
        
        // Delete 키: 선택한 항목 삭제
        if (e.key === 'Delete') {
            const checkedBoxes = document.querySelectorAll('input[name="member_id"]:checked');
            if (checkedBoxes.length > 0) {
                const deleteBtn = document.querySelector('button[name="delete"]');
                if (deleteBtn && confirm(`선택한 ${checkedBoxes.length}명의 회원을 정말 삭제하시겠습니까?`)) {
                    deleteBtn.click();
                }
            }
        }
        
        // F5: 새로고침 대신 전체 보기
        if (e.key === 'F5') {
            e.preventDefault();
            window.location.href = '/admin/users';
        }
    });
}

// 테이블 행 클릭 시 체크박스 토글
function setupRowClickToggle() {
    const tableRows = document.querySelectorAll('tbody tr');
    tableRows.forEach(row => {
        const checkbox = row.querySelector('input[type="checkbox"]');
        if (checkbox) {
            row.addEventListener('click', function(e) {
                // 체크박스 자체를 클릭한 경우는 제외
                if (e.target.type !== 'checkbox') {
                    checkbox.checked = !checkbox.checked;
                }
            });
            
            // 체크박스에만 포인터 커서
            checkbox.addEventListener('click', function(e) {
                e.stopPropagation();
            });
        }
    });
}

// 검색 조건 저장/복원
function saveSearchConditions() {
    const urlParams = new URLSearchParams(window.location.search);
    const searchType = urlParams.get('searchType');
    const startDate = urlParams.get('startDate');
    const endDate = urlParams.get('endDate');
    
    if (searchType) {
        const selectElement = document.querySelector('select[name="searchType"]');
        if (selectElement) {
            selectElement.value = searchType;
        }
    }
    
    if (startDate) {
        const startDateInput = document.querySelector('input[name="startDate"]');
        if (startDateInput) {
            startDateInput.value = startDate;
        }
    }
    
    if (endDate) {
        const endDateInput = document.querySelector('input[name="endDate"]');
        if (endDateInput) {
            endDateInput.value = endDate;
        }
    }
}

// 통계 정보 표시
function showStatistics() {
    const totalRows = document.querySelectorAll('tbody tr:not([style*="display: none"])').length;
    const emptyRow = document.querySelector('tbody tr td[colspan]');
    
    if (!emptyRow && totalRows > 0) {
        // 통계 정보를 표시할 요소 생성
        const statsDiv = document.createElement('div');
        statsDiv.className = 'member-statistics';
        statsDiv.innerHTML = `
            <div class="stats-info">
                <span>전체 회원 수: <strong>${totalRows}명</strong></span>
                <span>선택된 회원: <strong id="selected-count">0명</strong></span>
            </div>
        `;
        
        // 테이블 위에 삽입
        const tableContainer = document.querySelector('.table-container');
        if (tableContainer && !document.querySelector('.member-statistics')) {
            tableContainer.insertBefore(statsDiv, tableContainer.firstChild);
        }
        
        // 선택된 항목 수 업데이트
        const checkboxes = document.querySelectorAll('input[name="member_id"]');
        const selectedCountElement = document.getElementById('selected-count');
        
        checkboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                const selected = document.querySelectorAll('input[name="member_id"]:checked').length;
                if (selectedCountElement) {
                    selectedCountElement.textContent = `${selected}명`;
                }
            });
        });
    }
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 기존 기능들
    enhanceSearchForm();
    setupKeyboardShortcuts();
    setupRowClickToggle();
    saveSearchConditions();
    showStatistics();
    
    // 삭제 성공 메시지는 HTML에서 Thymeleaf로 처리
    
    // 테이블 애니메이션
    const tableRows = document.querySelectorAll('tbody tr');
    tableRows.forEach((row, index) => {
        if (!row.querySelector('td[colspan]')) {
            row.style.opacity = '0';
            row.style.transform = 'translateY(20px)';
            setTimeout(() => {
                row.style.transition = 'all 0.3s ease';
                row.style.opacity = '1';
                row.style.transform = 'translateY(0)';
            }, index * 50);
        }
    });
});

// 윈도우 로드 시 실행 (기존 코드 호환성)
window.onload = function() {
    // 더보기 기능이 필요한 경우 (페이지네이션 대신)
    // loadMore();
};