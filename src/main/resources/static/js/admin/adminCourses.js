// 전역 변수
let map;
let searchMarkers = [];
let tagList = [];
let storeList = [];

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    renderTagTable();
    renderStoreTable();
    setupFormValidation();
    setupEventListeners();
});

// 이벤트 리스너 설정
function setupEventListeners() {
    // Enter 키로 검색 실행
    document.getElementById('restaurantSearch').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            searchRestaurant();
        }
    });
    
    // 모달 외부 클릭 시 닫기
    document.getElementById('restaurantModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeRestaurantModal();
        }
    });
    
    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeRestaurantModal();
        }
    });
}

// 폼 유효성 검사 설정
function setupFormValidation() {
    const form = document.getElementById('courseForm');
    const inputs = form.querySelectorAll('input[required], textarea[required]');
    
    inputs.forEach(input => {
        input.addEventListener('blur', validateField);
        input.addEventListener('input', clearFieldError);
    });
}

// 필드 유효성 검사
function validateField(e) {
    const field = e.target;
    const value = field.value.trim();
    
    // 기존 에러 메시지 제거
    clearFieldError(e);
    
    if (!value) {
        showFieldError(field, '이 필드는 필수입니다.');
        return false;
    }
    
    // 코스명 길이 검사
    if (field.id === 'course_name' && value.length < 2) {
        showFieldError(field, '코스명은 2글자 이상이어야 합니다.');
        return false;
    }
    
    // 설명 길이 검사
    if (field.id === 'description' && value.length < 10) {
        showFieldError(field, '설명은 10글자 이상이어야 합니다.');
        return false;
    }
    
    return true;
}

// 필드 에러 표시
function showFieldError(field, message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'field-error';
    errorDiv.textContent = message;
    errorDiv.style.cssText = `
        color: #e74c3c;
        font-size: 12px;
        margin-top: 5px;
        font-weight: 500;
    `;
    
    field.style.borderColor = '#e74c3c';
    field.parentNode.appendChild(errorDiv);
}

// 필드 에러 제거
function clearFieldError(e) {
    const field = e.target;
    const errorDiv = field.parentNode.querySelector('.field-error');
    if (errorDiv) {
        errorDiv.remove();
    }
    field.style.borderColor = '';
}

// 문자열 정리 함수
function cleanString(input) {
    if (!input) return '';
    return input
        .replace(/[\u0000-\u001F\u007F-\u009F]/g, '')  // 제어문자 제거
        .replace(/[\u200B-\u200D\uFEFF]/g, '')         // zero-width
        .replace(/[\u202A-\u202E]/g, '')               // 방향 제어
        .replace(/[\r\n\t]/g, '')                      // 줄바꿈, 탭
        .replace(/"/g, '&quot;')                       // 따옴표 대응
        .replace(/'/g, '&#39;')                        // 홑따옴표 대응
        .trim();
}

// 태그 테이블 렌더링
function renderTagTable() {
    const tagTableBody = document.getElementById("tagTableBody");
    tagTableBody.innerHTML = "";
    
    tagList.forEach((tag, index) => {
        const row = document.createElement("tr");
        
        const tdTag = document.createElement("td");
        tdTag.textContent = tag;
        
        const tdDelete = document.createElement("td");
        tdDelete.innerHTML = "<span class='delete-btn'>삭제</span>";
        tdDelete.onclick = function() {
            deleteTag(index);
        };
        
        row.appendChild(tdTag);
        row.appendChild(tdDelete);
        tagTableBody.appendChild(row);
    });
    
    // 추가 행
    const addRow = document.createElement("tr");
    const addTd = document.createElement("td");
    addTd.colSpan = 2;
    addTd.className = "add-row";
    addTd.textContent = "+ 태그 추가";
    addTd.onclick = addTag;
    addRow.appendChild(addTd);
    tagTableBody.appendChild(addRow);
}

// 태그 추가
function addTag() {
    const newTag = prompt("새 태그를 입력하세요 (예: #분위기좋음)");
    if (newTag && newTag.trim()) {
        const cleanTag = cleanString(newTag.trim());
        
        // 중복 검사
        if (tagList.includes(cleanTag)) {
            alert("이미 추가된 태그입니다.");
            return;
        }
        
        // 태그 형식 검사
        if (!cleanTag.startsWith('#')) {
            alert("태그는 #으로 시작해야 합니다.");
            return;
        }
        
        tagList.push(cleanTag);
        renderTagTable();
    }
}

// 태그 삭제
function deleteTag(index) {
    if (confirm("태그를 삭제하시겠습니까?")) {
        tagList.splice(index, 1);
        renderTagTable();
    }
}

// 선택한 맛집 목록 렌더링
function renderStoreTable() {
    const storeTableBody = document.getElementById("storeTableBody");
    storeTableBody.innerHTML = "";
    
    storeList.forEach((store, index) => {
        const row = document.createElement("tr");
        
        const tdName = document.createElement("td");
        tdName.textContent = (store.order ? store.order + ". " : "") + store.name;
        
        const tdDelete = document.createElement("td");
        tdDelete.innerHTML = "<span class='delete-btn'>삭제</span>";
        tdDelete.onclick = function() {
            deleteStore(index);
        };
        
        row.appendChild(tdName);
        row.appendChild(tdDelete);
        storeTableBody.appendChild(row);
    });
    
    // 가게 수 표시
    const countInfo = document.createElement("div");
    countInfo.style.cssText = `
        margin-top: 10px;
        font-size: 14px;
        color: #666;
        text-align: center;
    `;
    countInfo.textContent = `현재 ${storeList.length}개 가게 선택됨 (최소 3개, 최대 5개)`;
    
    const existingInfo = storeTableBody.parentNode.parentNode.querySelector('.store-count-info');
    if (existingInfo) {
        existingInfo.remove();
    }
    
    countInfo.className = 'store-count-info';
    storeTableBody.parentNode.parentNode.appendChild(countInfo);
}

// 가게 삭제
function deleteStore(index) {
    if (confirm("가게를 삭제하시겠습니까?")) {
        storeList.splice(index, 1);
        // 순서 재정렬
        storeList.forEach((store, i) => {
            store.order = i + 1;
        });
        renderStoreTable();
    }
}

// 맛집 검색 모달 열기
function openRestaurantModal() {
    document.getElementById("restaurantModal").style.display = "block";
    document.body.style.overflow = "hidden"; // 스크롤 방지
    
    // 지도 초기화는 약간의 지연 후 실행 (모달이 완전히 열린 후)
    setTimeout(() => {
        initRestaurantSelectMap();
    }, 100);
}

// 맛집 검색 모달 닫기
function closeRestaurantModal() {
    document.getElementById("restaurantModal").style.display = "none";
    document.body.style.overflow = ""; // 스크롤 복원
    
    // 검색 결과 초기화
    document.getElementById("restaurantSearch").value = "";
    document.getElementById("restaurantResults").innerHTML = "";
    
    // 마커 제거
    if (searchMarkers.length > 0) {
        searchMarkers.forEach(marker => marker.setMap(null));
        searchMarkers = [];
    }
}

// 지도 초기화
function initRestaurantSelectMap() {
    try {
        map = new naver.maps.Map('restaurantSelectMap', {
            center: new naver.maps.LatLng(33.499621, 126.531188), // 제주도 중심
            zoom: 11,
            mapTypeControl: true,
            zoomControl: true,
            mapTypeControlOptions: {
                style: naver.maps.MapTypeControlStyle.BUTTON,
                position: naver.maps.Position.TOP_RIGHT
            }
        });
        
        // 기존 마커 제거
        searchMarkers.forEach(marker => marker.setMap(null));
        searchMarkers = [];
        
        console.log('지도 초기화 완료');
    } catch (error) {
        console.error('지도 초기화 오류:', error);
        showMessage('error', '지도를 불러오는 중 오류가 발생했습니다.');
    }
}

// AJAX로 맛집 검색
// 검색 함수
function searchRestaurant() {
    const keyword = document.getElementById("restaurantSearch").value.trim();
    
    if (!keyword) {
        alert("검색어를 입력해주세요.");
        return;
    }
    
    // 로딩 표시
    const resultsDiv = document.getElementById("restaurantResults");
    resultsDiv.innerHTML = '<div class="loading-message" style="text-align: center; padding: 20px;"><span class="loading-spinner"></span> 검색 중...</div>';
    
    // 서버에 검색 요청
    fetch(`/api/restaurants/search?keyword=${encodeURIComponent(keyword)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('네트워크 응답이 올바르지 않습니다.');
            }
            return response.json();  // 응답이 JSON 형식일 때
        })
        .then(data => {
            console.log("검색 결과:", data); // 서버에서 반환된 데이터를 확인해봅니다.
            displaySearchResults(data);  // 검색된 결과를 화면에 표시
            addMarkersToMap(data);  // 지도에 마커 추가
        })
        .catch(error => {
            console.error("검색 오류:", error);
            resultsDiv.innerHTML = '<div class="no-results">검색 중 오류가 발생했습니다. 다시 시도해주세요.</div>';
            showMessage('error', '검색 중 오류가 발생했습니다.');
        });
}

// 검색 결과 표시 함수
function displaySearchResults(results) {
    const resultsDiv = document.getElementById("restaurantResults");
    
    if (!results || results.length === 0) {
        resultsDiv.innerHTML = '<div class="no-results">검색 결과가 없습니다.</div>';
        return;
    }
    
    let html = '<table class="result-table"><thead><tr><th>가게명</th><th>주소</th><th>선택</th></tr></thead><tbody>';
    
    results.forEach(item => {
        const safeName = item.name ? cleanString(item.name) : "이름 없음";  // 서버에서 'name'을 받을 것
        const safeAddress = item.address ? cleanString(item.address) : "주소 없음";  // 'address'도 포함해야함
        
        html += `<tr>
            <td>${safeName}</td>
            <td>${safeAddress}</td>
            <td><a href="javascript:void(0);" onclick="addStore(${item.rstId}, '${safeName.replace(/'/g, "\\'")}', ${item.latitude || 0}, ${item.longitude || 0}, '${item.image || ''}')">선택</a></td>
        </tr>`;
    });
    
    html += '</tbody></table>';
    resultsDiv.innerHTML = html;
    console.log("검색 결과:", results);
}



// 지도에 검색 결과 마커 추가
function addMarkersToMap(results) {
    // 기존 마커 제거
    searchMarkers.forEach(marker => marker.setMap(null));
    searchMarkers = [];
    
    if (!results || results.length === 0) return;
    
    results.forEach((item, index) => {
        if (!item.rst_lat || !item.rst_long) return;
        
        const storeName = cleanString(item.rst_name || '이름 없음');
        
        const htmlContent = `
            <div style="
                display: flex; 
                align-items: center; 
                justify-content: center;
                background: rgba(255, 255, 255, 0.95);
                border: 1.5px solid #333;
                border-radius: 12px;
                padding: 6px 12px;
                font-size: 14px;
                font-weight: 600;
                color: #333;
                font-family: 'Noto Sans KR', sans-serif;
                box-shadow: 0 4px 8px rgba(0,0,0,0.15);
                white-space: nowrap;
                transition: transform 0.2s ease;
                cursor: pointer;
            ">
                <span>${storeName}</span>
            </div>
        `;

        const marker = new naver.maps.Marker({
            position: new naver.maps.LatLng(item.rst_lat, item.rst_long),
            map: map,
            icon: {
                content: htmlContent,
                anchor: new naver.maps.Point(0, 0)
            }
        });

        // 마커 클릭 이벤트
        naver.maps.Event.addListener(marker, "click", function() {
            addStore(item.rst_id, item.rst_name, item.rst_lat, item.rst_long, item.imgpath);
        });
        
        searchMarkers.push(marker);
    });
    
    // 첫 번째 결과로 지도 중심 이동
    if (results.length > 0 && results[0].rst_lat && results[0].rst_long) {
        const center = new naver.maps.LatLng(results[0].rst_lat, results[0].rst_long);
        map.setCenter(center);
        map.setZoom(13);
    }
}

// 맛집을 코스에 추가
function addStore(rst_id, name, lat, lng, image) {
    // 유효성 검사
    if (!rst_id || !name) {
        alert("가게 정보가 올바르지 않습니다.");
        return;
    }
    
    // 중복 추가 방지
    const existingStore = storeList.find(store => store.rst_id === rst_id);
    if (existingStore) {
        alert("이미 추가된 가게입니다.");
        return;
    }
    
    // 최대 5개 제한
    if (storeList.length >= 5) {
        alert("최대 5개까지만 등록할 수 있습니다.");
        return;
    }
    
    // 3개 이상일 때 확인
    if (storeList.length >= 3) {
        if (!confirm(`${storeList.length + 1}번째 가게를 추가하시겠습니까?`)) {
            return;
        }
    }
    
    // 가게 추가
    const newOrder = storeList.length + 1;
    const cleanName = cleanString(name);
    
    storeList.push({
        rst_id: rst_id,
        name: cleanName,
        lat: lat || 0,
        lng: lng || 0,
        image: image || '',
        order: newOrder
    });
    
    alert(`${newOrder}번째 음식점이 추가되었습니다.`);
    renderStoreTable();
    closeRestaurantModal();
}

// 이미지 파일 선택 트리거
function triggerFile() {
    document.getElementById("image_file").click();
}

// 파일 변경 처리
function handleFileChange(input) {
    const file = input.files[0];
    const label = document.getElementById("fileNameLabel");
    
    if (file) {
        // 파일 크기 검사 (10MB)
        if (file.size > 10 * 1024 * 1024) {
            alert("파일 크기는 10MB 이하여야 합니다.");
            input.value = "";
            label.textContent = "이미지 업로드";
            return;
        }
        
        // 파일 형식 검사
        if (!file.type.startsWith('image/')) {
            alert("이미지 파일만 업로드 가능합니다.");
            input.value = "";
            label.textContent = "이미지 업로드";
            return;
        }
        
        label.textContent = file.name;
        label.style.color = "#333";
    } else {
        label.textContent = "이미지 업로드";
        label.style.color = "#666";
    }
}

// 코스 등록 함수
// 코스 등록 함수
function submitCourse() {
    // 유효성 검사
    if (!validateForm()) {
        return;
    }
    
    // 버튼 비활성화
    const submitBtn = document.querySelector('.submit-btn');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="loading-spinner"></span> 등록 중...';
    
    try {
        // 폼 데이터 준비
        const form = document.getElementById('courseForm');
        const formData = new FormData(form);
        
        // JSON 데이터 추가
        formData.set('tagsJson', JSON.stringify(tagList));
        formData.set('storesJson', JSON.stringify(storeList));
        
        // 콘솔에 전송 데이터 출력
        console.log('전송 데이터:', {
            courseName: formData.get('courseName'),
            description: formData.get('description'),
            tagsJson: formData.get('tagsJson'),
            storesJson: formData.get('storesJson'),
            imageFile: formData.get('imageFile')
        });
        
        // 이 부분에서 'restaurants' 데이터도 로그로 확인해봅니다.
        const restaurantsJson = formData.get('storesJson');
        console.log('restaurantsJson:', restaurantsJson);  // 이 부분에서 'restaurants' 데이터 확인
        
        // 서버에 전송
        fetch('/adminCourses', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답 오류');
            }
            return response.text();
        })
        .then(result => {
            console.log('서버 응답:', result);
            
            if (result.includes('success') || result.includes('성공')) {
                showMessage('success', '맛집 추천 코스 등록이 완료되었습니다.');
                resetForm();
            } else {
                throw new Error('등록 실패');
            }
        })
        .catch(error => {
            console.error('등록 오류:', error);
            showMessage('error', '등록에 실패했습니다. 다시 시도해주세요.');
        })
        .finally(() => {
            // 버튼 복원
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        });
        
    } catch (error) {
        console.error('폼 전송 오류:', error);
        showMessage('error', '등록 중 오류가 발생했습니다.');
        
        // 버튼 복원
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    }
}


// 폼 유효성 검사
function validateForm() {
    const courseName = document.getElementById("course_name").value.trim();
    const description = document.getElementById("description").value.trim();
    
    // 필수 필드 검사
    if (!courseName) {
        alert("코스 이름을 입력해주세요.");
        document.getElementById("course_name").focus();
        return false;
    }
    
    if (courseName.length < 2) {
        alert("코스 이름은 2글자 이상이어야 합니다.");
        document.getElementById("course_name").focus();
        return false;
    }
    
    if (!description) {
        alert("설명을 입력해주세요.");
        document.getElementById("description").focus();
        return false;
    }
    
    if (description.length < 10) {
        alert("설명은 10글자 이상이어야 합니다.");
        document.getElementById("description").focus();
        return false;
    }
    
    // 가게 수 검사
    if (storeList.length < 3) {
        alert("코스를 등록하려면 최소 3개의 가게를 선택해야 합니다.");
        return false;
    }
    
    if (storeList.length > 5) {
        alert("코스에는 최대 5개의 가게만 등록할 수 있습니다.");
        return false;
    }
    
    return true;
}

// 폼 리셋
function resetForm() {
    document.getElementById('courseForm').reset();
    document.getElementById('fileNameLabel').textContent = '이미지 업로드';
    tagList = [];
    storeList = [];
    renderTagTable();
    renderStoreTable();
    
    // 에러 메시지 제거
    document.querySelectorAll('.field-error').forEach(error => error.remove());
    document.querySelectorAll('input, textarea').forEach(field => {
        field.style.borderColor = '';
    });
}

// 메시지 표시 함수
function showMessage(type, message) {
    // 기존 메시지 제거
    const existingAlert = document.querySelector('.alert');
    if (existingAlert) {
        existingAlert.remove();
    }
    
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.innerHTML = `<span>${message}</span>`;
    
    document.body.appendChild(alertDiv);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.style.animation = 'slideOut 0.3s ease forwards';
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.remove();
                }
            }, 300);
        }
    }, 3000);
}

// 페이지 언로드 시 확인
window.addEventListener('beforeunload', function(e) {
    if (storeList.length > 0 || tagList.length > 0 || 
        document.getElementById('course_name').value.trim() || 
        document.getElementById('description').value.trim()) {
        
        const message = '작성 중인 내용이 있습니다. 정말 페이지를 떠나시겠습니까?';
        e.returnValue = message;
        return message;
    }
});

// 에러 처리
window.addEventListener('error', function(e) {
    console.error('JavaScript 오류:', e.error);
    showMessage('error', '일시적인 오류가 발생했습니다.');
});

// CSS 애니메이션 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
    
    .field-error {
        animation: fadeIn 0.3s ease;
    }
    
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(-10px); }
        to { opacity: 1; transform: translateY(0); }
    }
`;
document.head.appendChild(style);