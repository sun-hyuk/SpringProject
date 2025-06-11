/**
 * 네이버 지도 API를 이용한 맛집 검색 지도
 * 파일 위치: src/main/resources/static/js/restaurant-map.js
 */

// 전역 변수
let naverMap = null;
let currentMarkers = [];
let currentInfoWindows = [];
let userLocationMarker = null;

/**
 * 네이버 지도 초기화
 * window.RESTAURANT_DATA 전역 변수를 사용합니다.
 */
function initializeNaverMap() {
    // 네이버 지도 API 로드 확인
    if (typeof naver === 'undefined' || !naver.maps) {
        console.error('네이버 지도 API가 로드되지 않았습니다.');
        showMapError();
        return;
    }

    try {
        // 1) 기본 위치 설정: RESTAURANT_DATA 중 첫 항목이 있으면 그 위치, 없으면 기본(서울)으로
        const data = window.RESTAURANT_DATA || [];
        let defaultLat = 37.5665;
        let defaultLng = 126.9780;
        if (data.length > 0) {
            defaultLat = data[0].latitude;
            defaultLng = data[0].longitude;
        }
        const defaultLocation = new naver.maps.LatLng(defaultLat, defaultLng);

        // 2) 지도 옵션
        const mapOptions = {
            center: defaultLocation,
            zoom: 16,
            minZoom: 10,
            maxZoom: 20,
            mapTypeControl: false,
            mapDataControl: false,
            logoControl: false,
            scaleControl: true,
            zoomControl: false
        };

        // 3) 지도 생성
        naverMap = new naver.maps.Map('naverMap', mapOptions);
        window.map = naverMap;

        // 4) 지도 로드 완료 시점에 마커 표시
        naver.maps.Event.addListener(naverMap, 'idle', () => {
            displayRestaurantMarkers();
        });

        // 5) 컨트롤 버튼 이벤트 등록
        initializeMapControls();

        console.log('네이버 지도 초기화 완료');
    } catch (error) {
        console.error('지도 초기화 오류:', error);
        showMapError();
    }
}

/**
 * 맛집 마커들을 지도에 표시
 */
function displayRestaurantMarkers() {
    clearAllMarkers();

    const data = window.RESTAURANT_DATA || [];
    if (data.length === 0) {
        console.log('표시할 식당 데이터가 없습니다.');
        return;
    }

    data.forEach(rst => {
        if (rst.latitude == null || rst.longitude == null) return;
        const position = new naver.maps.LatLng(rst.latitude, rst.longitude);

        const isHighlighted = rst.rating >= 4.8;
        const marker = new naver.maps.Marker({
            position,
            map: naverMap,
            title: rst.name,
            icon: {
                content: `<div class="custom-marker${isHighlighted ? ' highlighted' : ''}">⭐${rst.rating != null ? rst.rating.toFixed(1) : '0.0'}</div>`,
                anchor: new naver.maps.Point(25, 25)
            }
        });

        const infoContent = `
            <div class="info-window">
                <div class="info-title">${rst.name}</div>
                <div class="info-image">
                    <img src="${rst.image}" alt="${rst.name}" style="width:100%;height:80px;object-fit:cover;border-radius:4px;margin:8px 0;" />
                </div>
                <div><span class="info-rating">⭐ ${rst.rating != null ? rst.rating.toFixed(1) : '0.0'}</span></div>
                <div class="info-address" style="font-size:12px;color:#666;margin:4px 0;">${rst.address ? rst.address.substring(0,30)+'...' : ''}</div>
                <button class="info-button" onclick="window.location.href='/rstDetail?rstId=${rst.rstId}'" style="width:100%;margin-top:8px;padding:6px;background:#ff6b35;color:white;border:none;border-radius:4px;cursor:pointer;">상세보기</button>
            </div>
        `;

        const infoWindow = new naver.maps.InfoWindow({
            content: infoContent,
            maxWidth: 220,
            backgroundColor: 'white',
            borderColor: '#ccc',
            borderWidth: 1,
            anchorSize: new naver.maps.Size(10, 10),
            anchorColor: 'white'
        });

        naver.maps.Event.addListener(marker, 'click', () => {
            closeAllInfoWindows();
            infoWindow.open(naverMap, marker);
            scrollToRestaurant(rst.name);
        });

        currentMarkers.push(marker);
        currentInfoWindows.push(infoWindow);
    });
}

/**
 * 지도 컨트롤 초기화
 */
function initializeMapControls() {
    const currentLocationBtn = document.getElementById('currentLocationBtn');
    const zoomInBtn = document.getElementById('zoomInBtn');
    const zoomOutBtn = document.getElementById('zoomOutBtn');

    if (currentLocationBtn) currentLocationBtn.addEventListener('click', handleCurrentLocation);
    if (zoomInBtn) zoomInBtn.addEventListener('click', () => naverMap.setZoom(naverMap.getZoom() + 1));
    if (zoomOutBtn) zoomOutBtn.addEventListener('click', () => naverMap.setZoom(naverMap.getZoom() - 1));
}

/**
 * 현재 위치로 이동
 */
function handleCurrentLocation() {
    if (!navigator.geolocation) {
        alert('이 브라우저는 위치 서비스를 지원하지 않습니다.');
        return;
    }
    const btn = document.getElementById('currentLocationBtn');
    const btnImg = btn.querySelector('img');
    btn.disabled = true;
    btnImg.src = '/images/rstSearch/location-loading.png';

    navigator.geolocation.getCurrentPosition(
        position => {
            const { latitude, longitude } = position.coords;
            const userLocation = new naver.maps.LatLng(latitude, longitude);
            naverMap.setCenter(userLocation);
            naverMap.setZoom(17);
            updateUserLocationMarker(userLocation);
            btn.disabled = false;
            btnImg.src = '/images/rstSearch/location.png';
            btnImg.alt = '현재 위치';
        },
        error => {
            console.error('위치 정보 오류:', error);
            alert('위치 정보를 가져올 수 없습니다.');
            btn.disabled = false;
            btnImg.src = '/images/rstSearch/location.png';
            btnImg.alt = '현재 위치';
        },
        { enableHighAccuracy: true, timeout:10000, maximumAge:300000 }
    );
}

/**
 * 사용자 위치 마커 업데이트
 */
function updateUserLocationMarker(location) {
    if (userLocationMarker) userLocationMarker.setMap(null);
    userLocationMarker = new naver.maps.Marker({
        position: location,
        map: naverMap,
        icon: { content: '<div style="width:20px;height:20px;background:#4285f4;border:3px solid white;border-radius:50%;box-shadow:0 2px 8px rgba(66,133,244,0.5);"></div>', anchor: new naver.maps.Point(10,10) },
        title: '현재 위치'
    });
}

/**
 * 모든 마커 및 인포윈도우 정리
 */
function clearAllMarkers() {
    currentMarkers.forEach(marker => marker.setMap(null));
    currentMarkers = [];
    closeAllInfoWindows();
}

function closeAllInfoWindows() {
    currentInfoWindows.forEach(infoWin => infoWin.close());
    currentInfoWindows = [];
}

/**
 * 리스트 스크롤 및 강조
 */
function scrollToRestaurant(restaurantName) {
    document.querySelectorAll('.restaurant-card h3').forEach(card => {
        if (card.textContent.trim() === restaurantName) {
            const el = card.closest('.restaurant-card');
            el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            el.style.background = '#fff3cd';
            setTimeout(() => el.style.background = '', 2000);
        }
    });
}

/**
 * 상세 페이지 이동
 */
function goToRestaurantDetail(rstId) {
    window.location.href = `/rstDetail?rstId=${rstId}`;
}

/**
 * 지도 오류 표시
 */
function showMapError() {
    const container = document.getElementById('naverMap');
    if (container) {
        container.innerHTML = `
            <div style="display:flex;align-items:center;justify-content:center;height:100%;background:#f5f5f5;color:#666;">
                <div style="text-align:center;"><div style="font-size:48px;margin-bottom:16px;">🗺️</div><div>지도를 불러올 수 없습니다</div><div style="font-size:12px;margin-top:8px;">네이버 지도 API 설정을 확인해주세요</div></div>
            </div>
        `;
    }
}

/**
 * 리스트 오버레이 드래그 기능
 */
function initializeListOverlayDrag() {
    // 기존 로직 그대로… (생략)
}

// 모듈 바인딩
export {
    initializeNaverMap,
    initializeMapControls,
    initializeListOverlayDrag,
    displayRestaurantMarkers,
    handleCurrentLocation,
    updateUserLocationMarker,
    clearAllMarkers,
    closeAllInfoWindows,
    scrollToRestaurant,
    goToRestaurantDetail,
    showMapError
};
