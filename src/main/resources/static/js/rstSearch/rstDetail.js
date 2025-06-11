let currentVisibleCount = 4; // 초기 표시 개수를 4개로 변경

function loadMoreMenus() {
    const allMenus = document.querySelectorAll('#menuList .menu-card');
    const btn = document.getElementById('loadMoreBtn');
    const total = allMenus.length;

    const nextCount = currentVisibleCount + 4; // 4개씩 더 보기

    for (let i = currentVisibleCount; i < nextCount && i < total; i++) {
        allMenus[i].style.display = "block";
    }

    currentVisibleCount = nextCount;

    if (currentVisibleCount >= total) {
        btn.style.display = "none";
    }
}

window.addEventListener("DOMContentLoaded", () => {
    // 초기 로드 시 4개만 보이도록 설정
    const allMenus = document.querySelectorAll('#menuList .menu-card');
    allMenus.forEach((item, index) => {
        item.style.display = index < 4 ? "block" : "none";
    });
});

let currentSort = 'latest';

function loadReviews(sort = 'latest', page = 0) {
    currentSort = sort;
    const rstId = document.getElementById("restaurantId").value;

    // 로딩 상태 표시
    const reviewList = document.getElementById("reviewList");
    reviewList.innerHTML = '<div class="loading">리뷰를 불러오는 중...</div>';

    fetch(`/reviews?rstId=${rstId}&sort=${sort}&page=${page}`)
        .then(res => {
            if (!res.ok) {
                throw new Error('네트워크 응답이 좋지 않습니다.');
            }
            return res.json();
        })
        .then(data => {
            const pagination = document.getElementById("reviewPagination");
            reviewList.innerHTML = "";
            pagination.innerHTML = "";

            // 정렬 버튼 활성화 상태 업데이트
            document.querySelectorAll('.sort-btn').forEach(btn => {
                btn.classList.remove('active');
            });
            document.querySelector(`[onclick="loadReviews('${sort}', 0)"]`).classList.add('active');

            // 리뷰가 없는 경우
            if (data.content.length === 0) {
                reviewList.innerHTML = `
                    <div class="empty-state">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                            <path d="M9 11H15M9 15H15M21 5V19C21 19.5523 20.5523 20 20 20H4C3.44772 20 3 19.5523 3 19V5C3 4.44772 3.44772 4 4 4H20C20.5523 4 21 4.44772 21 5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <p>아직 작성된 리뷰가 없습니다</p>
                    </div>
                `;
                return;
            }

            // 리뷰 출력
            data.content.forEach(review => {
                const reviewElement = document.createElement("div");
                reviewElement.classList.add("review-item");

                // 날짜 포맷팅
                const reviewDate = new Date(review.createdAt);
                const formattedDate = reviewDate.toLocaleDateString('ko-KR', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });

                // 별점 표시
                const stars = Array.from({length: 5}, (_, i) => {
                    return i < review.rating 
                        ? '<svg width="16" height="16" viewBox="0 0 24 24" fill="#FFD700"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>'
                        : '<svg width="16" height="16" viewBox="0 0 24 24" fill="#E0E0E0"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>';
                }).join('');

                reviewElement.innerHTML = `
                    <div class="review-header">
                        <span class="reviewer-name">${review.nickname}</span>
                        <div class="review-meta">
                            <span class="review-date">${formattedDate}</span>
                            <button class="report-btn" onclick="openReportModal(${review.reviewId})">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                    <path d="M3 3L21 21M9 9V13C9 13.5523 9.44772 14 10 14H14C14.5523 14 15 13.5523 15 13V9M12 3V9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                신고
                            </button>
                        </div>
                    </div>
                    <div class="review-rating">
                        <div class="stars">${stars}</div>
                        <span class="rating-text">${review.rating}점</span>
                    </div>
                    ${review.imageUrls.length > 0 ? `
                        <div class="review-images">
                            ${review.imageUrls.map(url => `<img src="${url}" alt="리뷰 이미지" loading="lazy">`).join("")}
                        </div>
                    ` : ''}
                    <div class="review-text">${review.content}</div>
                `;

                // 좋아요 버튼 추가
                const template = document.getElementById("like-template");
                const likeClone = template.content.cloneNode(true);
                const likeBtn = likeClone.querySelector(".like-btn");
                const likeCount = likeClone.querySelector("span");
                
                likeBtn.id = `like-btn-${review.reviewId}`;
                likeBtn.setAttribute("onclick", `toggleReviewLike(${review.reviewId})`);
                likeCount.id = `like-count-${review.reviewId}`;
                likeCount.textContent = review.likeCount;
                
                // 이미 좋아요를 눌렀다면 active 클래스 추가
                if (review.liked) {
                    likeBtn.classList.add("liked");
                }

                reviewElement.appendChild(likeClone);
                reviewList.appendChild(reviewElement);
            });

            // 페이지네이션 출력 (개선된 디자인)
            if (data.totalPages > 1) {
                // 이전 페이지 버튼
                if (data.number > 0) {
                    pagination.innerHTML += `
                        <button onclick="loadReviews('${sort}', ${data.number - 1})" class="prev-btn">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </button>
                    `;
                }

                // 페이지 번호들 (최대 5개까지만 표시)
                const startPage = Math.max(0, data.number - 2);
                const endPage = Math.min(data.totalPages - 1, data.number + 2);
                
                for (let i = startPage; i <= endPage; i++) {
                    pagination.innerHTML += `
                        <button onclick="loadReviews('${sort}', ${i})" class="${i === data.number ? 'active' : ''}">
                            ${i + 1}
                        </button>
                    `;
                }

                // 다음 페이지 버튼
                if (data.number < data.totalPages - 1) {
                    pagination.innerHTML += `
                        <button onclick="loadReviews('${sort}', ${data.number + 1})" class="next-btn">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </button>
                    `;
                }
            }
        })
        .catch(error => {
            console.error('리뷰 로딩 오류:', error);
            reviewList.innerHTML = `
                <div class="empty-state">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                        <path d="M12 9V13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <p>리뷰를 불러오는데 실패했습니다</p>
                </div>
            `;
        });
}

// 페이지 로드 시 최신순으로 첫 페이지 불러오기
document.addEventListener("DOMContentLoaded", function () {
    loadReviews('latest', 0);
});

// 신고 모달 관련 함수들
function openReportModal(reviewId) {
	if (!isUserLoggedIn()) {
	       promptLogin();
	       return;
	   }
	
    document.getElementById('reportReviewId').value = reviewId;
    document.getElementById('reportReason').value = '';
    const modal = document.getElementById('reportModal');
    modal.style.display = 'flex';
    
    // 모달 애니메이션
    const modalContent = modal.querySelector('.modal-content');
    modalContent.style.transform = 'translateY(20px)';
    modalContent.style.opacity = '0';
    
    setTimeout(() => {
        modalContent.style.transform = 'translateY(0)';
        modalContent.style.opacity = '1';
        modalContent.style.transition = 'all 0.3s ease';
    }, 10);
    
    // 바깥 영역 클릭 시 모달 닫기
    modal.onclick = function(e) {
        if (e.target === modal) {
            closeReportModal();
        }
    };
}

function closeReportModal() {
    const modal = document.getElementById('reportModal');
    const modalContent = modal.querySelector('.modal-content');
    
    modalContent.style.transform = 'translateY(20px)';
    modalContent.style.opacity = '0';
    
    setTimeout(() => {
        modal.style.display = 'none';
    }, 300);
}

function submitReviewReport() {
    const reviewId = document.getElementById('reportReviewId').value;
    const reason = document.getElementById('reportReason').value.trim();

    if (!reason) {
        alert("신고 사유를 입력해주세요.");
        return;
    }

    if (reason.length < 10) {
        alert("신고 사유를 10자 이상 상세히 입력해주세요.");
        return;
    }

    // 버튼 비활성화
    const submitBtn = document.querySelector('.modal-footer .submit-btn');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = '신고 중...';
    submitBtn.disabled = true;

    fetch(`/report/review/${reviewId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ reason })
    })
    .then(res => {
        if (!res.ok) {
            throw new Error("신고 처리에 실패했습니다.");
        }
        return res.text();
    })
    .then(msg => {
        alert("신고가 접수되었습니다. 검토 후 조치하겠습니다.");
        closeReportModal();
    })
    .catch(err => {
        console.error('신고 오류:', err);
        alert("신고 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    })
    .finally(() => {
        // 버튼 상태 복원
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    });
}

// 리뷰 좋아요 토글
function toggleReviewLike(reviewId) {
	
	if (!isUserLoggedIn()) {
	       promptLogin();
	       return;
	   }
	   
    const likeBtn = document.querySelector(`#like-btn-${reviewId}`);
    const likeCount = document.querySelector(`#like-count-${reviewId}`);
    
    // 버튼 비활성화 (중복 클릭 방지)
    likeBtn.style.pointerEvents = 'none';
    
    fetch(`/api/review-like/${reviewId}`, {
        method: 'POST'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('네트워크 응답이 좋지 않습니다.');
        }
        return response.json();
    })
    .then(data => {
        // 좋아요 상태 업데이트
        if (data.liked) {
            likeBtn.classList.add("liked");
        } else {
            likeBtn.classList.remove("liked");
        }
        likeCount.textContent = data.likeCount;
        
        // 애니메이션 효과
        likeBtn.style.transform = 'scale(1.2)';
        setTimeout(() => {
            likeBtn.style.transform = 'scale(1)';
        }, 150);
    })
    .catch(error => {
        console.error('좋아요 처리 오류:', error);
        alert('좋아요 처리 중 오류가 발생했습니다.');
    })
    .finally(() => {
        // 버튼 재활성화
        setTimeout(() => {
            likeBtn.style.pointerEvents = 'auto';
        }, 300);
    });
}

// 찜하기 토글
function toggleJjim() {
    const rstId = document.getElementById("restaurantId").value;
    const jjimIcon = document.getElementById("jjimIcon");
    
    // 버튼 비활성화 (중복 클릭 방지)
    jjimIcon.style.pointerEvents = 'none';

    fetch(`/api/jjim/toggle/${rstId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" }
    })
    .then(res => {
        if (res.status === 401) {
            alert("로그인이 필요한 서비스입니다.");
            // 로그인 페이지로 리다이렉트
            const currentUrl = encodeURIComponent(window.location.href);
            window.location.href = `/login?redirectURL=${currentUrl}`;
            return;
        }
        if (!res.ok) {
            throw new Error('찜하기 처리에 실패했습니다.');
        }
        return res.json();
    })
    .then(data => {
        // 찜 상태에 따라 아이콘 변경
        if (data.result === "added") {
            jjimIcon.classList.add("filled");
            // 성공 피드백
            showToast("찜 목록에 추가되었습니다 ❤️");
        } else {
            jjimIcon.classList.remove("filled");
            showToast("찜 목록에서 제거되었습니다");
        }
        
        // 애니메이션 효과
        jjimIcon.style.transform = 'scale(1.3)';
        setTimeout(() => {
            jjimIcon.style.transform = 'scale(1)';
        }, 200);
    })
    .catch(error => {
        console.error('찜하기 오류:', error);
        alert('찜하기 처리 중 오류가 발생했습니다.');
    })
    .finally(() => {
        // 버튼 재활성화
        setTimeout(() => {
            jjimIcon.style.pointerEvents = 'auto';
        }, 300);
    });
}

// 간단한 토스트 메시지 함수
function showToast(message) {
    // 기존 토스트가 있으면 제거
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
        existingToast.remove();
    }
    
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        bottom: 100px;
        left: 50%;
        transform: translateX(-50%);
        background: rgba(0, 0, 0, 0.8);
        color: white;
        padding: 12px 20px;
        border-radius: 20px;
        font-size: 14px;
        z-index: 1000;
        opacity: 0;
        transition: opacity 0.3s ease;
    `;
    
    document.body.appendChild(toast);
    
    // 애니메이션
    setTimeout(() => toast.style.opacity = '1', 10);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 2000);
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        const modal = document.getElementById('reportModal');
        if (modal.style.display === 'flex') {
            closeReportModal();
        }
    }
});

// 이미지 지연 로딩 최적화
document.addEventListener('DOMContentLoaded', function() {
    // Intersection Observer로 이미지 지연 로딩
    if ('IntersectionObserver' in window) {
        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    img.src = img.dataset.src;
                    img.classList.remove('lazy');
                    observer.unobserve(img);
                }
            });
        });

        document.querySelectorAll('img[data-src]').forEach(img => {
            imageObserver.observe(img);
        });
    }
});

function isUserLoggedIn() {
    return document.body.dataset.loggedIn === 'true';
}

function promptLogin() {
    showLoginRequiredPopup();  // 이미 있는 로그인 팝업 함수 호출
}
document.getElementById("writeReviewBtn").addEventListener("click", () => {
    if (!isUserLoggedIn()) {
        promptLogin();
        return;
    }
    const rstId = document.getElementById("restaurantId").value;
    window.location.href = `/reviewWrite?rstId=${rstId}`;
});