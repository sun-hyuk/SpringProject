let isMenuSelected = false;
let isRatingSelected = false;
let isTextEntered = false;
let isPhotoValid = false;

// 폼 검증 상태 체크 및 UI 업데이트
function checkReviewEligibility() {
    const submitBtn = document.getElementById("submitBtn");
    const isComplete = isMenuSelected && isRatingSelected && isTextEntered && isPhotoValid;
    
    submitBtn.disabled = !isComplete;
    
    // 시각적 피드백
    if (isComplete) {
        submitBtn.classList.add('ready');
        showFeedback('모든 조건이 충족되었습니다! 리뷰를 등록할 수 있어요 🎉', 'success');
    } else {
        submitBtn.classList.remove('ready');
    }
    
    // 각 섹션 완료 상태 업데이트
    updateSectionStatus('menu-select-section', isMenuSelected);
    updateSectionStatus('rating-section', isRatingSelected);
    updateSectionStatus('text-review-section', isTextEntered);
    updateSectionStatus('photo-section', isPhotoValid);
}

// 섹션 완료 상태 UI 업데이트
function updateSectionStatus(sectionClass, isCompleted) {
    const section = document.querySelector(`.${sectionClass}`);
    const header = section?.querySelector('.section-header');
    
    if (header) {
        if (isCompleted) {
            header.classList.add('completed');
        } else {
            header.classList.remove('completed');
        }
    }
}

// 피드백 메시지 표시
function showFeedback(message, type = 'info') {
    // 기존 메시지 제거
    const existingMessage = document.querySelector('.feedback-message');
    if (existingMessage) {
        existingMessage.remove();
    }
    
    const feedbackDiv = document.createElement('div');
    feedbackDiv.className = `feedback-message ${type}`;
    feedbackDiv.textContent = message;
    
    document.body.appendChild(feedbackDiv);
    
    // 애니메이션
    setTimeout(() => feedbackDiv.classList.add('show'), 10);
    setTimeout(() => {
        feedbackDiv.classList.remove('show');
        setTimeout(() => feedbackDiv.remove(), 300);
    }, 3000);
}

// ⭐ 별점 선택 기능
document.addEventListener('DOMContentLoaded', function () {
    const stars = document.querySelectorAll('.star');
    const ratingInput = document.getElementById('ratingInput');
    const ratingDescription = document.querySelector('.rating-description');
    let selectedRating = 0;

    const ratingTexts = {
        1: '별로예요 😞',
        2: '그저 그래요 😐',
        3: '괜찮아요 🙂',
        4: '좋아요 😊',
        5: '최고예요! 😍'
    };

    stars.forEach((star, index) => {
        // 호버 효과
        star.addEventListener('mouseenter', () => {
            if (selectedRating === 0) {
                for (let i = 0; i <= index; i++) {
                    stars[i].style.fill = '#FFD700';
                    stars[i].style.transform = 'scale(1.1)';
                }
                ratingDescription.textContent = ratingTexts[index + 1];
            }
        });
        
        star.addEventListener('mouseleave', () => {
            if (selectedRating === 0) {
                stars.forEach((s, i) => {
                    s.style.fill = '#e0e0e0';
                    s.style.transform = 'scale(1)';
                });
                ratingDescription.textContent = '별점을 선택해주세요';
            }
        });

        // 클릭 이벤트
        star.addEventListener('click', () => {
            selectedRating = index + 1;
            ratingInput.value = selectedRating;
            
            stars.forEach((s, i) => {
                if (i < selectedRating) {
                    s.classList.add('active');
                    s.style.fill = '#FFD700';
                } else {
                    s.classList.remove('active');
                    s.style.fill = '#e0e0e0';
                }
                s.style.transform = 'scale(1)';
            });
            
            ratingDescription.textContent = ratingTexts[selectedRating];
            ratingDescription.style.color = '#ff6b35';
            ratingDescription.style.fontWeight = '600';
            
            isRatingSelected = selectedRating > 0;
            checkReviewEligibility();
            
            // 성공 애니메이션
            stars[index].style.transform = 'scale(1.3)';
            setTimeout(() => {
                stars[index].style.transform = 'scale(1)';
            }, 200);
        });
    });

    // 🍴 메뉴 선택 기능
    const selectedMenus = new Set();
    const menuInput = document.getElementById("selectedMenus");

    document.querySelectorAll(".menu-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault(); // 폼 제출 방지
            
            const menuName = btn.dataset.name;
            
            if (selectedMenus.has(menuName)) {
                selectedMenus.delete(menuName);
                btn.classList.remove("selected");
                btn.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    btn.style.transform = 'scale(1)';
                }, 100);
            } else {
                selectedMenus.add(menuName);
                btn.classList.add("selected");
                btn.style.transform = 'scale(1.05)';
                setTimeout(() => {
                    btn.style.transform = 'scale(1)';
                }, 200);
            }
            
            menuInput.value = Array.from(selectedMenus).join(",");
            isMenuSelected = selectedMenus.size > 0;
            checkReviewEligibility();
            
            // 선택된 메뉴 수 피드백
            if (selectedMenus.size > 0) {
                const menuText = selectedMenus.size === 1 ? '1개 메뉴' : `${selectedMenus.size}개 메뉴`;
                document.querySelector('.menu-select-section .section-title').innerHTML = `
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                        <path d="M12 2L13.09 8.26L20 9L13.09 9.74L12 16L10.91 9.74L4 9L10.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    ${menuText} 선택됨
                `;
            } else {
                document.querySelector('.menu-select-section .section-title').innerHTML = `
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                        <path d="M12 2L13.09 8.26L20 9L13.09 9.74L12 16L10.91 9.74L4 9L10.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    어떤 메뉴를 드셨나요?
                `;
            }
        });
    });

    // 📝 텍스트 입력 감지
    const textarea = document.querySelector('.review-textarea');
    const charCountElement = document.querySelector('.char-count');
    
    textarea.addEventListener('input', function () {
        const count = textarea.value.trim().length;
        charCountElement.textContent = `${count}/500자`;
        
        // 글자 수에 따른 색상 변경
        if (count > 450) {
            charCountElement.style.color = '#ff6b35';
            charCountElement.style.fontWeight = '600';
        } else if (count > 400) {
            charCountElement.style.color = '#ffa500';
            charCountElement.style.fontWeight = '500';
        } else {
            charCountElement.style.color = '#6c757d';
            charCountElement.style.fontWeight = '400';
        }
        
        isTextEntered = count >= 10; // 최소 10자 이상
        checkReviewEligibility();
        
        // 텍스트 입력 완료 피드백
        if (isTextEntered && count >= 20) {
            document.querySelector('.text-review-section .section-title').innerHTML = `
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                    <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M16 13H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M16 17H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M10 9H9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                ${count}자 작성 완료 ✓
            `;
        }
    });

    // 📸 사진 업로드 초기 설정
    isPhotoValid = true; // 사진은 선택사항이므로 기본값을 true로 설정
    checkReviewEligibility();
});

// 📸 사진 검증 함수
function handlePhoto(input) {
    const files = input.files;
    if (files.length === 0) {
        isPhotoValid = true; // 사진이 없어도 OK
        checkReviewEligibility();
        return;
    }

    let processed = 0;
    let validPhotos = 0;
    isPhotoValid = false;

    showFeedback('사진을 검증하고 있습니다...', 'info');

    Array.from(files).forEach((file, fileIndex) => {
        // 파일 크기 체크 (5MB 제한)
        if (file.size > 5 * 1024 * 1024) {
            showFeedback('사진 크기는 5MB 이하로 업로드해주세요.', 'error');
            processed++;
            if (processed === files.length) {
                finalizePhotoValidation(validPhotos, files.length);
            }
            return;
        }

        const reader = new FileReader();
        reader.onload = function (e) {
        // ✅ 미리보기 렌더링
        const previewContainer = document.getElementById('photoPreviewContainer');
        if (fileIndex === 0) previewContainer.innerHTML = ''; // 첫 파일이면 초기화
        const previewImg = document.createElement('img');
        previewImg.src = e.target.result;
        previewContainer.appendChild(previewImg);

            const img = new Image();
            img.onload = function () {
                const imgClone = img.cloneNode();
                
                // EXIF 데이터 추출
                EXIF.getData(imgClone, function () {
                    processed++;

                    const lat = EXIF.getTag(this, "GPSLatitude");
                    const lon = EXIF.getTag(this, "GPSLongitude");
                    const date = EXIF.getTag(this, "DateTimeOriginal");
                    const latRef = EXIF.getTag(this, "GPSLatitudeRef");
                    const lonRef = EXIF.getTag(this, "GPSLongitudeRef");

                    console.log(`[사진 ${fileIndex + 1}] GPS 정보:`, { lat, lon, date, latRef, lonRef });

                    if (!lat || !lon || !date) {
                        console.log(`[사진 ${fileIndex + 1}] ❌ GPS 또는 날짜 정보 없음`);
                        if (processed === files.length) {
                            finalizePhotoValidation(validPhotos, files.length);
                        }
                        return;
                    }

                    const latDecimal = convertToDecimal(lat);
                    const lonDecimal = convertToDecimal(lon);
                    const finalLat = latRef === "S" ? -latDecimal : latDecimal;
                    const finalLon = lonRef === "W" ? -lonDecimal : lonDecimal;

                    const distance = getDistanceFromLatLonInM(
                        window.restaurantLat,
                        window.restaurantLon,
                        finalLat,
                        finalLon
                    );

                    const [y, m, d] = date.split(" ")[0].split(":").map(Number);
                    const photoDate = new Date(y, m - 1, d);
                    const today = new Date();
                    const diffDays = Math.floor((today - photoDate) / (1000 * 60 * 60 * 24));

                    console.log(`[사진 ${fileIndex + 1}] 📸 위도:`, finalLat);
                    console.log(`[사진 ${fileIndex + 1}] 📸 경도:`, finalLon);
                    console.log(`[사진 ${fileIndex + 1}] 📍 거리: ${Math.round(distance)}m`);
                    console.log(`[사진 ${fileIndex + 1}] 🕒 날짜 차이: ${diffDays}일`);

                    const isValidDistance = distance <= 500;
                    const isValidDate = diffDays <= 14;
                    const isValidPhoto = isValidDistance && isValidDate;

                    console.log(`[사진 ${fileIndex + 1}] ✅ 조건 ${isValidPhoto ? '만족' : '불만족'}`);

                    if (isValidPhoto) {
                        validPhotos++;
                    }

                    if (processed === files.length) {
                        finalizePhotoValidation(validPhotos, files.length);
                    }
                });
            };
            img.src = e.target.result;
        };
        reader.readAsDataURL(file);
    });
}

// 사진 검증 완료 처리
function finalizePhotoValidation(validPhotos, totalPhotos) {
    if (validPhotos > 0) {
        isPhotoValid = true;
        showFeedback(`${validPhotos}장의 사진이 검증되었습니다! 📸`, 'success');
        updatePhotoSectionUI(validPhotos, totalPhotos);
    } else if (totalPhotos > 0) {
        isPhotoValid = false;
        showFeedback('조건을 만족하는 사진이 없습니다. (식당 근처에서 2주 이내 촬영한 사진을 업로드해주세요)', 'error');
    } else {
        isPhotoValid = true; // 사진이 없어도 OK
    }
    
    checkReviewEligibility();
}

// 사진 섹션 UI 업데이트
function updatePhotoSectionUI(validPhotos, totalPhotos) {
    const photoTitle = document.querySelector('.photo-section .section-title');
    if (validPhotos > 0) {
        photoTitle.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M23 19C23 19.5304 22.7893 20.0391 22.4142 20.4142C22.0391 20.7893 21.5304 21 21 21H3C2.46957 21 1.96086 20.7893 1.58579 20.4142C1.21071 20.0391 1 19.5304 1 19V8C1 7.46957 1.21071 6.96086 1.58579 6.58579C1.96086 6.21071 2.46957 6 3 6H7L9 3H15L17 6H21C21.5304 6 22.0391 6.21071 22.4142 6.58579C22.7893 6.96086 23 7.46957 23 8V19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"/>
            </svg>
            ${validPhotos}장 업로드 완료 ✓
        `;
    }
}

// 사진 input 버튼 트리거
function uploadPhoto() {
    document.getElementById('photoInput').click();
}

// GPS DMS → Decimal 변환
function convertToDecimal(dms) {
    if (Array.isArray(dms) && dms.length === 3) {
        const [d, m, s] = dms.map(Number);
        return d + (m / 60) + (s / 3600);
    } else {
        console.warn("⛔ GPS 형식 오류:", dms);
        return NaN;
    }
}

// 거리 계산 (Haversine)
function getDistanceFromLatLonInM(lat1, lon1, lat2, lon2) {
    const R = 6371000; // Earth radius in meters
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) ** 2 +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) ** 2;
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

// 글자 수 업데이트 함수 (HTML에서 직접 호출용)
function updateCharCount(textarea) {
    const count = textarea.value.length;
    const charCountElement = textarea.parentElement.querySelector('.char-count');
    if (charCountElement) {
        charCountElement.textContent = `${count}/500자`;
    }
}

// 폼 제출 시 로딩 처리
document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    const submitBtn = document.getElementById('submitBtn');
    
    form.addEventListener('submit', function(e) {
        if (submitBtn.disabled) {
            e.preventDefault();
            return;
        }
        
        // 로딩 상태로 변경
        submitBtn.classList.add('loading');
        submitBtn.disabled = true;
        submitBtn.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            리뷰 등록 중...
        `;
        
        showFeedback('리뷰를 등록하고 있습니다...', 'info');
    });
});

// 페이지 로드 시 애니메이션
document.addEventListener('DOMContentLoaded', function() {
    // 섹션들을 순차적으로 나타나게 하는 애니메이션
    const sections = document.querySelectorAll('.menu-select-section, .rating-section, .text-review-section, .photo-section');
    sections.forEach((section, index) => {
        section.style.opacity = '0';
        section.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            section.style.transition = 'all 0.5s ease';
            section.style.opacity = '1';
            section.style.transform = 'translateY(0)';
        }, (index + 1) * 100);
    });
});