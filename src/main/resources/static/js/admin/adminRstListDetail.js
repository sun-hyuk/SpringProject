// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 이미지 로드 에러 처리
    setupImageErrorHandling();
    
    // 테이블 행 애니메이션
    animateTableRows();
    
    // 키보드 단축키 설정
    setupKeyboardShortcuts();
    
    // 리뷰 삭제 버튼 개선
    enhanceDeleteButtons();
    
    // 이미지 확대 기능
    setupImageZoom();
    
    // 자동 새로고침 설정 (리뷰 업데이트 확인)
    setupAutoRefresh();
});

// 리뷰 삭제 함수 (전역 함수로 HTML에서 호출)
function deleteReview(reviewId, rstId, currentPage) {
    if (!reviewId || !rstId) {
        alert('삭제할 리뷰 정보가 올바르지 않습니다.');
        return;
    }
    
    const confirmMessage = '해당 리뷰를 정말 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.';
    if (confirm(confirmMessage)) {
        // 삭제 버튼 비활성화 및 로딩 표시
        const deleteBtn = event.target;
        const originalText = deleteBtn.textContent;
        deleteBtn.disabled = true;
        deleteBtn.innerHTML = '<span class="loading"></span>';
        
        // 삭제 요청 전송
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/reviews/adminDeleteReview';
        
        const reviewIdInput = document.createElement('input');
        reviewIdInput.type = 'hidden';
        reviewIdInput.name = 'reviewId';
        reviewIdInput.value = reviewId;
        
        const rstIdInput = document.createElement('input');
        rstIdInput.type = 'hidden';
        rstIdInput.name = 'rstId';
        rstIdInput.value = rstId;
        
        const pageInput = document.createElement('input');
        pageInput.type = 'hidden';
        pageInput.name = 'page';
        pageInput.value = currentPage || 1;
        
        form.appendChild(reviewIdInput);
        form.appendChild(rstIdInput);
        form.appendChild(pageInput);
        
        document.body.appendChild(form);
        form.submit();
    }
}

// 가게 목록으로 돌아가기
function goBack() {
    // 이전 페이지 정보가 있다면 사용, 없다면 기본 가게 목록으로
    const referrer = document.referrer;
    if (referrer && referrer.includes('/adminRstList')) {
        window.history.back();
    } else {
        window.location.href = '/adminRstList';
    }
}

// 이미지 에러 처리
function setupImageErrorHandling() {
    const images = document.querySelectorAll('img');
    images.forEach(img => {
        img.addEventListener('error', function() {
            this.src = '/images/no-image.jpg';
            this.alt = '이미지를 불러올 수 없습니다';
            this.style.opacity = '0.7';
        });
        
        img.addEventListener('load', function() {
            this.style.opacity = '1';
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
        // ESC: 가게 목록으로 돌아가기
        if (e.key === 'Escape') {
            if (confirm('가게 목록으로 돌아가시겠습니까?')) {
                goBack();
            }
        }
        
        // F5: 새로고침
        if (e.key === 'F5') {
            e.preventDefault();
            window.location.reload();
        }
        
        // Ctrl/Cmd + 방향키: 페이지네이션
        if ((e.ctrlKey || e.metaKey) && e.key === 'ArrowLeft') {
            e.preventDefault();
            const prevLink = document.querySelector('.pagination a[title="이전"]');
            if (prevLink) {
                window.location.href = prevLink.href;
            }
        }
        
        if ((e.ctrlKey || e.metaKey) && e.key === 'ArrowRight') {
            e.preventDefault();
            const nextLink = document.querySelector('.pagination a[title="다음"]');
            if (nextLink) {
                window.location.href = nextLink.href;
            }
        }
        
        // Ctrl/Cmd + H: 도움말
        if ((e.ctrlKey || e.metaKey) && e.key === 'h') {
            e.preventDefault();
            showKeyboardHelp();
        }
    });
}

// 리뷰 삭제 버튼 개선
function enhanceDeleteButtons() {
    const deleteButtons = document.querySelectorAll('.delete-review-btn');
    deleteButtons.forEach(button => {
        // 호버 효과 개선
        button.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px) scale(1.05)';
        });
        
        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
        
        // 더블 클릭 방지
        let clickTimeout = null;
        button.addEventListener('click', function(e) {
            if (clickTimeout) {
                clearTimeout(clickTimeout);
                clickTimeout = null;
                e.preventDefault();
                return;
            }
            
            clickTimeout = setTimeout(() => {
                clickTimeout = null;
            }, 1000);
        });
    });
}

// 이미지 확대 기능
function setupImageZoom() {
    const images = document.querySelectorAll('.store-image-container img');
    images.forEach(img => {
        img.style.cursor = 'pointer';
        img.addEventListener('click', function() {
            showImageModal(this.src, this.alt);
        });
    });
}

// 이미지 모달 표시
function showImageModal(src, alt) {
    const modal = document.createElement('div');
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0,0,0,0.8);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 1000;
        cursor: pointer;
    `;
    
    const img = document.createElement('img');
    img.src = src;
    img.alt = alt;
    img.style.cssText = `
        max-width: 90%;
        max-height: 90%;
        object-fit: contain;
        border-radius: 8px;
        box-shadow: 0 4px 20px rgba(0,0,0,0.3);
    `;
    
    modal.appendChild(img);
    document.body.appendChild(modal);
    
    // 모달 닫기
    modal.addEventListener('click', function() {
        document.body.removeChild(modal);
    });
    
    // ESC로 모달 닫기
    const closeModal = function(e) {
        if (e.key === 'Escape') {
            document.body.removeChild(modal);
            document.removeEventListener('keydown', closeModal);
        }
    };
    document.addEventListener('keydown', closeModal);
}

// 자동 새로고침 (선택사항)
function setupAutoRefresh() {
    // 개발 환경에서는 비활성화
    if (window.location.hostname === 'localhost') return;
    
    // 5분마다 리뷰 업데이트 확인
    setInterval(() => {
        if (!document.hidden) {
            checkForUpdates();
        }
    }, 5 * 60 * 1000);
}

// 업데이트 확인 (AJAX 요청)
function checkForUpdates() {
    const rstId = getRestaurantIdFromUrl();
    if (!rstId) return;
    
    fetch(`/api/restaurant/${rstId}/review-count`)
        .then(response => response.json())
        .then(data => {
            const currentCount = document.querySelectorAll('tbody tr:not([style*="display: none"]):not(:has(td[colspan]))').length;
            if (data.count !== currentCount) {
                if (confirm('새로운 리뷰가 등록되었습니다. 페이지를 새로고침하시겠습니까?')) {
                    window.location.reload();
                }
            }
        })
        .catch(error => {
            console.log('업데이트 확인 중 오류:', error);
        });
}

// URL에서 레스토랑 ID 추출
function getRestaurantIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('rst_id');
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
        ${type === 'success' ? 'background-color: #27ae60;' : 'background-color: #e74c3c;'}
    `;
    messageDiv.textContent = message;
    
    document.body.appendChild(messageDiv);
    
    setTimeout(() => {
        messageDiv.style.opacity = '0';
        messageDiv.style.transform = 'translateX(100%)';
        setTimeout(() => messageDiv.remove(), 300);
    }, 3000);
}

// 키보드 단축키 도움말
function showKeyboardHelp() {
    const helpContent = `
        <h4>키보드 단축키</h4>
        <ul style="text-align: left; line-height: 1.6;">
            <li><strong>ESC:</strong> 가게 목록으로 돌아가기</li>
            <li><strong>F5:</strong> 페이지 새로고침</li>
            <li><strong>Ctrl/Cmd + ←:</strong> 이전 페이지</li>
            <li><strong>Ctrl/Cmd + →:</strong> 다음 페이지</li>
            <li><strong>Ctrl/Cmd + H:</strong> 이 도움말 표시</li>
            <li><strong>이미지 클릭:</strong> 이미지 확대보기</li>
        </ul>
        <div style="margin-top: 15px;">
            <button onclick="closeKeyboardHelp()" style="background: #333; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">닫기</button>
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

// 리뷰 통계 표시
function showReviewStats() {
    const reviews = document.querySelectorAll('tbody tr:not(:has(td[colspan])):not([id])');
    if (reviews.length === 0) return;
    
    // 통계 정보 계산
    const totalReviews = reviews.length;
    const reviewsPerPage = Math.min(totalReviews, 8); // pageSize와 동일
    
    // 통계 정보 표시
    const statsContainer = document.createElement('div');
    statsContainer.style.cssText = `
        margin-top: 10px;
        padding: 10px;
        background-color: #f8f9fa;
        border-radius: 6px;
        text-align: center;
        font-size: 14px;
        color: #666;
    `;
    statsContainer.innerHTML = `현재 페이지에 ${reviewsPerPage}개의 리뷰가 표시되고 있습니다.`;
    
    const table = document.querySelector('table');
    table.parentNode.insertBefore(statsContainer, table.nextSibling);
}

// 접근성 개선
function setupAccessibility() {
    // 테이블에 aria-label 추가
    const table = document.querySelector('table');
    if (table) {
        table.setAttribute('aria-label', '가게 리뷰 목록');
    }
    
    // 버튼에 aria-label 추가
    const deleteButtons = document.querySelectorAll('.delete-review-btn');
    deleteButtons.forEach((button, index) => {
        const row = button.closest('tr');
        const reviewContent = row.querySelector('td:nth-child(2)').textContent.trim();
        const shortContent = reviewContent.length > 20 ? reviewContent.substring(0, 20) + '...' : reviewContent;
        button.setAttribute('aria-label', `"${shortContent}" 리뷰 삭제`);
    });
    
    // 페이지네이션 링크에 aria-label 추가
    const paginationLinks = document.querySelectorAll('.pagination a');
    paginationLinks.forEach(link => {
        const text = link.textContent.trim();
        if (text === '‹') {
            link.setAttribute('aria-label', '이전 페이지');
        } else if (text === '›') {
            link.setAttribute('aria-label', '다음 페이지');
        } else {
            link.setAttribute('aria-label', `${text}페이지로 이동`);
        }
    });
}

// 인쇄 기능
function printRestaurantDetail() {
    const printWindow = window.open('', '_blank');
    const restaurantName = document.querySelector('.detail-value').textContent;
    
    printWindow.document.write(`
        <!DOCTYPE html>
        <html>
        <head>
            <title>${restaurantName} - 상세정보</title>
            <style>
                body { font-family: 'Noto Sans KR', sans-serif; margin: 20px; }
                .detail-item { margin-bottom: 15px; }
                .detail-item label { font-weight: bold; }
                table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                th { background-color: #f5f5f5; }
                @media print { .no-print { display: none !important; } }
            </style>
        </head>
        <body>
            ${document.querySelector('.store-detail').innerHTML}
        </body>
        </html>
    `);
    
    printWindow.document.close();
    printWindow.focus();
    printWindow.print();
    printWindow.close();
}

// 페이지 로드 완료 후 실행
window.addEventListener('load', function() {
    // URL 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('deleteSuccess') === 'true') {
        showMessage('success', '리뷰가 성공적으로 삭제되었습니다.');
        
        // URL 정리
        urlParams.delete('deleteSuccess');
        const newUrl = window.location.pathname + (urlParams.toString() ? '?' + urlParams.toString() : '');
        window.history.replaceState({}, document.title, newUrl);
    }
    
    // 추가 기능 초기화
    showReviewStats();
    setupAccessibility();
    
    // 페이지 로드 완료 메시지
    console.log('가게 상세 페이지가 로드되었습니다.');
});

// 에러 처리
window.addEventListener('error', function(e) {
	// 리소스 로딩 에러는 무시
    if (e.target && e.target.tagName && e.target.tagName.toLowerCase() === 'img') {
        return;
    }

    // 진짜 JS 에러일 때만 처리
    if (e.error) {
        console.error('페이지 에러:', e.error);
        showMessage('error', '일시적인 오류가 발생했습니다. 페이지를 새로고침해주세요.');
    }
});

// 브라우저 뒤로가기/앞으로가기 처리
window.addEventListener('popstate', function(e) {
    setTimeout(() => {
        // 페이지 상태 복원
        animateTableRows();
    }, 100);
});