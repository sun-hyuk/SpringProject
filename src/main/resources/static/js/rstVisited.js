/**
 * 최근 방문한 맛집 페이지 기능 구현
 * - 아이템 클릭 시 상세 페이지 이동 기능
 * - 북마크 토글 기능
 */

// 북마크 상태 저장을 위한 전역 변수
let restaurantBookmarkStates = {};

document.addEventListener('DOMContentLoaded', function() {
  // 초기 북마크 상태 저장
  saveInitialBookmarkStates();
  
  // 모든 아이템에 클릭 이벤트 추가
  addClickEventToItems();
  
  // 북마크 기능 추가
  setupBookmarkButtons();
});

/**
 * 초기 북마크 상태 저장
 */
function saveInitialBookmarkStates() {
  document.querySelectorAll('.bookmark-btn[data-restaurant-id]').forEach(btn => {
    const restaurantId = btn.getAttribute('data-restaurant-id');
    restaurantBookmarkStates[restaurantId] = btn.classList.contains('active');
  });
}

/**
 * 모든 아이템에 클릭 이벤트 추가
 */
function addClickEventToItems() {
  const visitedItems = document.querySelectorAll('.visited-item');
  visitedItems.forEach(item => {
    item.addEventListener('click', function(e) {
      // 북마크 버튼 클릭 시에는 상세 페이지로 이동하지 않음
      if (e.target.closest('.bookmark-btn')) {
        return;
      }
      
      // 맛집 상세 페이지로 이동
      const restaurantId = this.getAttribute('data-restaurant-id');
      console.log(`맛집 ${restaurantId} 상세 페이지로 이동`);
      window.location.href = '/restaurantDetail.html';
    });
  });
}

/**
 * 북마크 버튼 클릭 이벤트 설정
 */
function setupBookmarkButtons() {
  const bookmarkButtons = document.querySelectorAll('.bookmark-btn');
  bookmarkButtons.forEach(button => {
    // 기존 이벤트 리스너 제거를 위해 새로운 버튼으로 교체
    const newButton = button.cloneNode(true);
    button.parentNode.replaceChild(newButton, button);
    
    newButton.addEventListener('click', function(e) {
      e.stopPropagation(); // 부모 요소 클릭 이벤트 전파 방지
      
      const restaurantId = this.getAttribute('data-restaurant-id');
      
      // 상태 토글
      const currentState = restaurantBookmarkStates[restaurantId] || false;
      const newState = !currentState;
      restaurantBookmarkStates[restaurantId] = newState;
      
      // 아이콘 이미지 변경
      const icon = this.querySelector('.bookmark-icon');
      
      if (newState) {
        this.classList.add('active');
        // 채워진 북마크 이미지로 변경
        icon.src = '/images/myFavorites/bookmark-filled.png';
        console.log(`맛집 ${restaurantId}를 북마크에 추가했습니다.`);
        
        // 실제 API 호출 (주석 해제하여 사용)
        // addBookmark(restaurantId);
      } else {
        this.classList.remove('active');
        // 기본 북마크 이미지로 변경
        icon.src = '/images/myFavorites/bookmark.png';
        console.log(`맛집 ${restaurantId}를 북마크에서 제거했습니다.`);
        
        // 실제 API 호출 (주석 해제하여 사용)
        // removeBookmark(restaurantId);
      }
    });
  });
}

/**
 * 북마크 추가 API 호출 (실제 구현 시 사용)
 */
function addBookmark(restaurantId) {
  fetch('/api/bookmarks', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ restaurantId: restaurantId })
  })
  .then(response => response.json())
  .then(data => {
    console.log('북마크 추가 성공:', data);
  })
  .catch(error => {
    console.error('북마크 추가 실패:', error);
  });
}

/**
 * 북마크 제거 API 호출 (실제 구현 시 사용)
 */
function removeBookmark(restaurantId) {
  fetch(`/api/bookmarks/${restaurantId}`, {
    method: 'DELETE',
  })
  .then(response => response.json())
  .then(data => {
    console.log('북마크 제거 성공:', data);
  })
  .catch(error => {
    console.error('북마크 제거 실패:', error);
  });
}

/**
 * 최근 방문한 맛집 데이터 가져오기 (실제 구현 시 사용)
 */
function loadVisitedRestaurants() {
  fetch('/api/visited-restaurants')
  .then(response => response.json())
  .then(data => {
    console.log('최근 방문한 맛집 데이터:', data);
    // 데이터를 받아서 DOM에 렌더링
    renderVisitedRestaurants(data);
  })
  .catch(error => {
    console.error('최근 방문한 맛집 데이터 로드 실패:', error);
  });
}

/**
 * 최근 방문한 맛집 데이터 렌더링 (실제 구현 시 사용)
 */
function renderVisitedRestaurants(restaurants) {
  const visitedList = document.querySelector('.visited-list');
  visitedList.innerHTML = '';
  
  restaurants.forEach(restaurant => {
    const visitedItem = createVisitedItem(restaurant);
    visitedList.appendChild(visitedItem);
  });
  
  // 이벤트 재설정
  addClickEventToItems();
  setupBookmarkButtons();
}

/**
 * 방문한 맛집 아이템 DOM 생성 (실제 구현 시 사용)
 */
function createVisitedItem(restaurant) {
  const itemDiv = document.createElement('div');
  itemDiv.className = 'visited-item';
  itemDiv.setAttribute('data-restaurant-id', restaurant.id);
  
  itemDiv.innerHTML = `
    <div class="visited-image">
      <img src="${restaurant.imageUrl}" alt="음식 이미지" class="item-img"/>
    </div>
    <div class="visited-info">
      <div class="restaurant-header">
        <h3 class="store-name">${restaurant.name}</h3>
        <button class="bookmark-btn ${restaurant.isBookmarked ? 'active' : ''}" data-restaurant-id="${restaurant.id}">
          <img src="/images/myFavorites/${restaurant.isBookmarked ? 'bookmark-filled.png' : 'bookmark.png'}" alt="북마크" class="bookmark-icon"/>
        </button>
      </div>
      <div class="rating">
        <div class="rating-info">
          <span class="star">★</span>
          <span class="score">${restaurant.rating}</span>
        </div>
        <span class="details">${restaurant.category} · ${restaurant.location}</span>
      </div>
    </div>
  `;
  
  return itemDiv;
}