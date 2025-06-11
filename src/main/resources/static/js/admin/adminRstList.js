// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 검색 폼 개선
    enhanceSearchForm();
    
    // 테이블 행 애니메이션
    animateTableRows();
    
    // 키보드 단축키 설정
    setupKeyboardShortcuts();
    
    // 정렬 기능 추가
    setupTableSorting();
    
    // 통계 정보 업데이트
    updateStatistics();
    
    // 삭제 확인 개선
    enhanceDeleteConfirmation();
    
    // 검색어 하이라이트
    highlightSearchTerm();
});

// 검색 폼 개선
function enhanceSearchForm() {
    const searchForm = document.querySelector('.search-bar');
    const searchInput = document.querySelector('.search-input');
    
    // Enter 키로 검색 실행
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchForm.submit();
            }
        });
    }
}

// 검색어 하이라이트
function highlightSearchTerm() {
    const searchInput = document.querySelector('.search-input');
    if (!searchInput) return;
    
    const searchKeyword = searchInput.value;
    if (!searchKeyword || !searchKeyword.trim()) return;
    
    const tableRows = document.querySelectorAll('tbody tr');
    tableRows.forEach(row => {
        const cells = row.querySelectorAll('td');
        cells.forEach((cell, index) => {
            // 상세보기, 삭제 버튼 컬럼 제외 (index 5, 6)
            if (index === 5 || index === 6) return;
            
            const originalText = cell.textContent;
            if (originalText.toLowerCase().includes(searchKeyword.toLowerCase())) {
                const regex = new RegExp(`(${searchKeyword})`, 'gi');
                cell.innerHTML = originalText.replace(regex, '<mark style="background-color: #fff3cd; padding: 2px 4px; border-radius: 3px;">$1</mark>');
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
            }, index * 50);
        }
    });
}

// 키보드 단축키 설정
function setupKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + F: 검색 입력 필드에 포커스
        if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
            e.preventDefault();
            const searchInput = document.querySelector('.search-input');
            if (searchInput) {
                searchInput.focus();
                searchInput.select();
            }
        }
        
        // F5: 새로고침
        if (e.key === 'F5') {
            e.preventDefault();
            window.location.reload();
        }
        
        // Escape: 검색 초기화
        if (e.key === 'Escape') {
            const searchInput = document.querySelector('.search-input');
            const searchType = document.querySelector('.filter-select');
            if (searchInput && searchInput.value) {
                searchInput.value = '';
                searchType.value = '';
                // 전체 보기로 리다이렉트
                window.location.href = '/adminRstList';
            }
        }
        
        // Ctrl/Cmd + H: 도움말 표시
        if ((e.ctrlKey || e.metaKey) && e.key === 'h') {
            e.preventDefault();
            showKeyboardHelp();
        }
    });
}

// 테이블 정렬 기능
function setupTableSorting() {
    const headers = document.querySelectorAll('th');
    headers.forEach((header, index) => {
        // 상세보기, 삭제 컬럼은 정렬 제외
        if (index === 5 || index === 6) return;
        
        header.style.cursor = 'pointer';
        header.title = '클릭하여 정렬';
        
        header.addEventListener('click', function() {
            sortTable(index);
        });
    });
}

let currentSortColumn = -1;
let sortAscending = true;

function sortTable(columnIndex) {
    const table = document.querySelector('table tbody');
    const rows = Array.from(table.rows);
    
    // 빈 행 제외
    const dataRows = rows.filter(row => !row.querySelector('td[colspan]'));
    
    if (dataRows.length === 0) return;
    
    // 정렬 방향 결정
    if (currentSortColumn === columnIndex) {
        sortAscending = !sortAscending;
    } else {
        sortAscending = true;
        currentSortColumn = columnIndex;
    }
    
    dataRows.sort((a, b) => {
        const aText = a.cells[columnIndex].textContent.trim();
        const bText = b.cells[columnIndex].textContent.trim();
        
        let comparison = 0;
        
        switch(columnIndex) {
            case 0: // No (숫자)
            case 4: // 좋아요 (숫자)
                comparison = parseInt(aText) - parseInt(bText);
                break;
            case 3: // 등록일 (날짜)
                comparison = new Date(aText) - new Date(bText);
                break;
            default: // 문자열
                comparison = aText.localeCompare(bText);
                break;
        }
        
        return sortAscending ? comparison : -comparison;
    });
    
    // 테이블 재구성
    table.innerHTML = '';
    if (dataRows.length === 0) {
        table.innerHTML = '<tr><td colspan="7">등록된 가게가 없습니다.</td></tr>';
    } else {
        dataRows.forEach(row => table.appendChild(row));
    }
    
    // 정렬 표시 업데이트
    updateSortIndicators(columnIndex);
    
    // 정렬 후 다시 애니메이션
    animateTableRows();
}

function updateSortIndicators(sortedColumn) {
    const headers = document.querySelectorAll('th');
    headers.forEach((header, index) => {
        // 기존 정렬 표시 제거
        header.textContent = header.textContent.replace(' ↑', '').replace(' ↓', '');
        
        // 현재 정렬된 컬럼에 표시 추가
        if (index === sortedColumn) {
            header.textContent += sortAscending ? ' ↑' : ' ↓';
        }
    });
}

// 삭제 확인 개선
function enhanceDeleteConfirmation() {
    const deleteForms = document.querySelectorAll('form[action*="adminDeleteRestaurant"]');
    deleteForms.forEach(form => {
        form.onsubmit = function(e) {
            const restaurantName = this.closest('tr').querySelector('td:nth-child(2)').textContent.trim();
            const confirmMessage = `'${restaurantName}' 가게를 정말 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`;
            
            return confirm(confirmMessage);
        };
    });
}

// 통계 정보 업데이트
function updateStatistics() {
    const dataRows = document.querySelectorAll('tbody tr:not([style*="display: none"]):not(:has(td[colspan]))');
    const emptyRow = document.querySelector('tbody tr td[colspan]');
    
    if (emptyRow) return;
    
    // 상태 정보 업데이트
    const statusInfo = document.querySelector('.status-info p');
    if (statusInfo && dataRows.length > 0) {
        const totalSpan = statusInfo.querySelector('span:first-child');
        const displayedSpan = statusInfo.querySelector('span:nth-child(2)');
        
        if (totalSpan && displayedSpan) {
            displayedSpan.textContent = Math.min(dataRows.length, 9); // pageSize
        }
    }
}

// 성공/오류 메시지 표시
function showMessage(type, message) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `alert alert-${type}`;
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
    messageDiv.textContent = message;
    
    document.body.appendChild(messageDiv);
    
    setTimeout(() => {
        messageDiv.style.opacity = '0';
        messageDiv.style.transform = 'translateX(100%)';
        setTimeout(() => messageDiv.remove(), 300);
    }, 3000);
}

// 가게 상세 정보 미리보기 (호버 시)
function setupRestaurantPreview() {
    const restaurantNames = document.querySelectorAll('tbody td:nth-child(2)');
    restaurantNames.forEach(nameCell => {
        if (nameCell.textContent.trim() && !nameCell.querySelector('[colspan]')) {
            nameCell.style.cursor = 'pointer';
            nameCell.style.textDecoration = 'underline';
            nameCell.style.color = '#0d4d62';
            
            nameCell.addEventListener('mouseenter', function() {
                const row = this.closest('tr');
                const restaurantData = {
                    name: row.cells[1].textContent.trim(),
                    location: row.cells[2].textContent.trim(),
                    date: row.cells[3].textContent.trim(),
                    likes: row.cells[4].textContent.trim()
                };
                showPreviewTooltip(this, restaurantData);
            });
            
            nameCell.addEventListener('mouseleave', function() {
                hidePreviewTooltip();
            });
        }
    });
}

// 미리보기 툴팁 표시
function showPreviewTooltip(element, data) {
    const tooltip = document.createElement('div');
    tooltip.id = 'restaurant-tooltip';
    tooltip.style.cssText = `
        position: absolute;
        background: #333;
        color: white;
        padding: 10px;
        border-radius: 5px;
        font-size: 12px;
        z-index: 1000;
        max-width: 200px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.2);
        pointer-events: none;
    `;
    
    tooltip.innerHTML = `
        <strong>${data.name}</strong><br>
        위치: ${data.location}<br>
        등록일: ${data.date}<br>
        좋아요: ${data.likes}개
    `;
    
    const rect = element.getBoundingClientRect();
    tooltip.style.left = rect.left + window.scrollX + 'px';
    tooltip.style.top = (rect.bottom + window.scrollY + 10) + 'px';
    
    document.body.appendChild(tooltip);
}

// 미리보기 툴팁 숨기기
function hidePreviewTooltip() {
    const tooltip = document.getElementById('restaurant-tooltip');
    if (tooltip) {
        tooltip.remove();
    }
}

// 검색 필터 저장/복원
function setupSearchMemory() {
    const searchForm = document.querySelector('.search-bar');
    if (searchForm) {
        searchForm.addEventListener('submit', function() {
            const searchType = document.querySelector('.filter-select').value;
            const searchKeyword = document.querySelector('.search-input').value;
            
            localStorage.setItem('adminRestaurantSearchType', searchType);
            localStorage.setItem('adminRestaurantSearchKeyword', searchKeyword);
        });
    }
    
    // 페이지 로드 시 검색 조건 복원
    const urlParams = new URLSearchParams(window.location.search);
    if (!urlParams.has('searchType') && !urlParams.has('searchKeyword')) {
        const savedSearchType = localStorage.getItem('adminRestaurantSearchType');
        const savedSearchKeyword = localStorage.getItem('adminRestaurantSearchKeyword');
        
        if (savedSearchType) {
            document.querySelector('.filter-select').value = savedSearchType;
        }
        if (savedSearchKeyword) {
            document.querySelector('.search-input').value = savedSearchKeyword;
        }
    }
}

// CSV 내보내기 기능
function exportRestaurantList() {
    const restaurants = [];
    const tableRows = document.querySelectorAll('tbody tr:not([style*="display: none"])');
    
    tableRows.forEach(row => {
        if (!row.querySelector('td[colspan]')) {
            restaurants.push({
                no: row.cells[0].textContent.trim(),
                name: row.cells[1].textContent.trim(),
                location: row.cells[2].textContent.trim(),
                date: row.cells[3].textContent.trim(),
                likes: row.cells[4].textContent.trim()
            });
        }
    });
    
    if (restaurants.length === 0) {
        alert('내보낼 데이터가 없습니다.');
        return;
    }
    
    // CSV 형식으로 변환
    const csvContent = [
        ['번호', '가게이름', '위치', '등록일', '좋아요'],
        ...restaurants.map(r => [r.no, r.name, r.location, r.date, r.likes])
    ].map(row => row.join(',')).join('\n');
    
    // 파일 다운로드
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `restaurant_list_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    showMessage('success', 'CSV 파일이 다운로드되었습니다.');
}

// 키보드 단축키 도움말 표시
function showKeyboardHelp() {
    const helpContent = `
        <h4>키보드 단축키</h4>
        <ul style="text-align: left; line-height: 1.6;">
            <li><strong>Ctrl/Cmd + F:</strong> 검색창 포커스</li>
            <li><strong>F5:</strong> 페이지 새로고침</li>
            <li><strong>ESC:</strong> 검색 초기화</li>
            <li><strong>Ctrl/Cmd + H:</strong> 이 도움말 표시</li>
            <li><strong>테이블 헤더 클릭:</strong> 컬럼별 정렬</li>
        </ul>
        <div style="margin-top: 15px;">
            <button onclick="closeKeyboardHelp()" style="background: #333; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">닫기</button>
            <button onclick="exportRestaurantList(); closeKeyboardHelp();" style="background: #28a745; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; margin-left: 10px;">CSV 내보내기</button>
        </div>
    `;
    
    const modal = document.createElement('div');
    modal.id = 'keyboard-help-modal';
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0,0,0,0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 1000;
    `;
    
    const modalContent = document.createElement('div');
    modalContent.style.cssText = `
        background: white;
        padding: 20px;
        border-radius: 8px;
        max-width: 500px;
        width: 90%;
        text-align: center;
    `;
    modalContent.innerHTML = helpContent;
    
    modal.appendChild(modalContent);
    document.body.appendChild(modal);
    
    // 모달 외부 클릭 시 닫기
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            closeKeyboardHelp();
        }
    });
}

// 키보드 도움말 닫기
function closeKeyboardHelp() {
    const modal = document.getElementById('keyboard-help-modal');
    if (modal) {
        modal.remove();
    }
}

// 페이지 로드 완료 후 실행
window.addEventListener('load', function() {
    // URL 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('deleteSuccess') === 'true') {
        showMessage('success', '가게가 성공적으로 삭제되었습니다.');
        
        // URL 정리
        urlParams.delete('deleteSuccess');
        const newUrl = window.location.pathname + (urlParams.toString() ? '?' + urlParams.toString() : '');
        window.history.replaceState({}, document.title, newUrl);
    }
    
    // 추가 기능 초기화
    setupRestaurantPreview();
    setupSearchMemory();
});

// 에러 처리
window.addEventListener('error', function(e) {
    console.error('페이지 에러:', e.error);
    showMessage('error', '일시적인 오류가 발생했습니다. 페이지를 새로고침해주세요.');
});

// 브라우저 뒤로가기/앞으로가기 처리
window.addEventListener('popstate', function(e) {
    setTimeout(() => {
        highlightSearchTerm();
        updateStatistics();
    }, 100);
});