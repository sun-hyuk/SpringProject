// 거절 확인 함수 (기존 confirmReject 함수와 동일)
function confirmReject(formElement) {
    if(confirm('정말로 이 가게 등록을 거절하시겠습니까?')) {
        formElement.submit();
    }
    return false;
}

// 승인 확인 함수
function confirmApprove(formElement) {
    if(confirm('이 가게를 승인하시겠습니까?')) {
        formElement.submit();
    }
    return false;
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 기존 onsubmit 속성 대신 이벤트 리스너로 처리
    setupFormEventListeners();
    
    // 테이블 행 애니메이션
    animateTableRows();
    
    // 키보드 단축키 설정
    setupKeyboardShortcuts();
    
    // 자동 새로고침 설정 (선택사항)
    setupAutoRefresh();
    
    // 통계 정보 업데이트
    updateStatistics();
});

// 폼 이벤트 리스너 설정
function setupFormEventListeners() {
    // 승인 버튼에 확인 다이얼로그 추가
    const approveButtons = document.querySelectorAll('button.approve');
    approveButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('이 가게를 승인하시겠습니까?')) {
                e.preventDefault();
            }
        });
    });

    // 거절 버튼은 이미 onsubmit에 confirm이 있으므로 추가 처리
    const rejectForms = document.querySelectorAll('form[action*="reject"]');
    rejectForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            // onsubmit에 이미 confirm이 있지만, 추가 처리 가능
            const restaurantName = this.closest('tr').querySelector('td:first-child').textContent;
            if (!confirm(`'${restaurantName}' 가게 등록을 정말 거절하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`)) {
                e.preventDefault();
            }
        });
    });
}

// 테이블 행 애니메이션
function animateTableRows() {
    const tableRows = document.querySelectorAll('tbody tr');
    tableRows.forEach((row, index) => {
        if (!row.querySelector('td[colspan]')) {
            row.style.opacity = '0';
            row.style.transform = 'translateY(20px)';
            setTimeout(() => {
                row.style.transition = 'all 0.3s ease';
                row.style.opacity = '1';
                row.style.transform = 'translateY(0)';
            }, index * 100);
        }
    });
}

// 키보드 단축키 설정
function setupKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // F5: 새로고침
        if (e.key === 'F5') {
            e.preventDefault();
            window.location.reload();
        }
        
        // Ctrl/Cmd + R: 새로고침
        if ((e.ctrlKey || e.metaKey) && e.key === 'r') {
            e.preventDefault();
            window.location.reload();
        }
        
        // Escape: 모달 닫기
        if (e.key === 'Escape') {
            closeModal();
        }
        
        // Ctrl/Cmd + A: 전체 승인 (관리자 전용)
        if ((e.ctrlKey || e.metaKey) && e.key === 'a') {
            e.preventDefault();
            if (confirm('모든 대기 중인 가게를 승인하시겠습니까?')) {
                approveAllRestaurants();
            }
        }
    });
}

// 자동 새로고침 설정 (10분마다)
function setupAutoRefresh() {
    // 개발 환경에서는 비활성화
    if (window.location.hostname === 'localhost') {
        return;
    }
    
    setInterval(() => {
        // 사용자가 페이지를 보고 있을 때만 새로고침
        if (!document.hidden) {
            const userConfirm = confirm('새로운 승인 요청이 있을 수 있습니다. 페이지를 새로고침하시겠습니까?');
            if (userConfirm) {
                window.location.reload();
            }
        }
    }, 10 * 60 * 1000); // 10분
}

// 전체 승인 기능 (선택사항)
function approveAllRestaurants() {
    const approveButtons = document.querySelectorAll('button.approve');
    if (approveButtons.length === 0) {
        alert('승인할 가게가 없습니다.');
        return;
    }
    
    // 순차적으로 승인 처리
    let index = 0;
    function processNext() {
        if (index < approveButtons.length) {
            approveButtons[index].closest('form').submit();
            index++;
            setTimeout(processNext, 1000); // 1초 간격
        }
    }
    processNext();
}

// 모달 관련 함수들
function showModal(title, content) {
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.innerHTML = `
        <span class="close-btn" onclick="closeModal()">&times;</span>
        <h3>${title}</h3>
        <div>${content}</div>
    `;
    
    const modalBg = document.createElement('div');
    modalBg.className = 'modal-bg';
    modalBg.onclick = closeModal;
    
    document.body.appendChild(modalBg);
    document.body.appendChild(modal);
}

function closeModal() {
    const modal = document.querySelector('.modal');
    const modalBg = document.querySelector('.modal-bg');
    
    if (modal) modal.remove();
    if (modalBg) modalBg.remove();
}

// 가게 상세 정보 모달 표시
function showRestaurantDetail(restaurantData) {
    const content = `
        <p><strong>가게명:</strong> ${restaurantData.name}</p>
        <p><strong>주소:</strong> ${restaurantData.address}</p>
        <p><strong>연락처:</strong> ${restaurantData.phone || '정보 없음'}</p>
        <p><strong>요청일:</strong> ${restaurantData.requestDate}</p>
        <p><strong>사업자:</strong> ${restaurantData.owner || '정보 없음'}</p>
    `;
    showModal('가게 상세 정보', content);
}

// 성공/실패 메시지 표시
function showMessage(type, message) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 5px;
        color: white;
        font-weight: bold;
        z-index: 1000;
        transition: all 0.3s ease;
        ${type === 'success' ? 'background-color: #28a745;' : 'background-color: #dc3545;'}
    `;
    
    document.body.appendChild(messageDiv);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        messageDiv.style.opacity = '0';
        messageDiv.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
        }, 300);
    }, 3000);
}

// URL 파라미터 처리
function handleUrlParams() {
    const urlParams = new URLSearchParams(window.location.search);
    const approveSuccess = urlParams.get('approveSuccess');
    const rejectSuccess = urlParams.get('rejectSuccess');
    const error = urlParams.get('error');
    
    if (approveSuccess === 'true') {
        showMessage('success', '가게가 성공적으로 승인되었습니다.');
        removeUrlParam('approveSuccess');
    }
    
    if (rejectSuccess === 'true') {
        showMessage('success', '가게 등록이 거절되었습니다.');
        removeUrlParam('rejectSuccess');
    }
    
    if (error) {
        showMessage('error', '처리 중 오류가 발생했습니다. 다시 시도해주세요.');
        removeUrlParam('error');
    }
}

// URL 파라미터 제거
function removeUrlParam(paramName) {
    const url = new URL(window.location);
    url.searchParams.delete(paramName);
    window.history.replaceState({}, document.title, url);
}

// 통계 정보 업데이트
function updateStatistics() {
    const totalRows = document.querySelectorAll('tbody tr:not([style*="display: none"])').length;
    const emptyRow = document.querySelector('tbody tr td[colspan]');
    
    if (emptyRow) {
        return;
    }
    
    const statusInfo = document.querySelector('.status-info p');
    if (statusInfo && totalRows > 0) {
        statusInfo.innerHTML = `총 <span>${totalRows}</span>개의 승인 대기 중인 식당이 있습니다.`;
    }
}

// 테이블 정렬 기능
function sortTable(columnIndex, ascending = true) {
    const table = document.querySelector('table tbody');
    const rows = Array.from(table.rows);
    
    // 빈 행 제외
    const dataRows = rows.filter(row => !row.querySelector('td[colspan]'));
    
    if (dataRows.length === 0) return;
    
    dataRows.sort((a, b) => {
        const aText = a.cells[columnIndex].textContent.trim();
        const bText = b.cells[columnIndex].textContent.trim();
        
        if (columnIndex === 2) { // 날짜 컬럼
            return ascending ? 
                new Date(aText) - new Date(bText) : 
                new Date(bText) - new Date(aText);
        } else { // 문자열 컬럼
            return ascending ? 
                aText.localeCompare(bText) : 
                bText.localeCompare(aText);
        }
    });
    
    // 테이블 재구성
    table.innerHTML = '';
    dataRows.forEach(row => table.appendChild(row));
    
    // 빈 행이 있었다면 다시 추가
    if (dataRows.length === 0) {
        table.innerHTML = '<tr><td colspan="4">승인 대기 중인 식당이 없습니다.</td></tr>';
    }
}

// 테이블 헤더 클릭 이벤트 설정
function setupTableSorting() {
    const headers = document.querySelectorAll('th');
    headers.forEach((header, index) => {
        // 관리 컬럼은 정렬 제외
        if (index === 3) return;
        
        header.style.cursor = 'pointer';
        header.title = '클릭하여 정렬';
        
        let ascending = true;
        header.addEventListener('click', function() {
            sortTable(index, ascending);
            ascending = !ascending;
            
            // 정렬 표시 업데이트
            headers.forEach(h => h.textContent = h.textContent.replace(' ↑', '').replace(' ↓', ''));
            this.textContent += ascending ? ' ↓' : ' ↑';
        });
    });
}

// 페이지 로드 완료 후 실행
window.addEventListener('load', function() {
    handleUrlParams();
    updateStatistics();
    setupTableSorting();
    
    // 가게명 클릭 시 상세 정보 표시 (선택사항)
    const restaurantNames = document.querySelectorAll('tbody td:first-child');
    restaurantNames.forEach(nameCell => {
        if (nameCell.textContent.trim() && !nameCell.querySelector('[colspan]')) {
            nameCell.classList.add('restaurant-name');
            nameCell.addEventListener('click', function() {
                const row = this.closest('tr');
                const restaurantData = {
                    name: row.cells[0].textContent.trim(),
                    address: row.cells[1].textContent.trim(),
                    requestDate: row.cells[2].textContent.trim()
                };
                showRestaurantDetail(restaurantData);
            });
        }
    });
});