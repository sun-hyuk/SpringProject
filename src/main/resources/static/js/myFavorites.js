/**
 * 나의 찜 목록 페이지 기능 구현
 * - 탭 전환 기능
 * - 그리드 뷰와 리스트 뷰 전환 기능
 * - 아이템 클릭 시 상세 페이지 이동 기능
 * - 북마크 토글 기능
 * - 하트, 북마크, 공유하기 아이콘 토글 기능
 */

document.addEventListener('DOMContentLoaded', function() {
  // 탭 전환 기능 바인딩
  const tabButtons = document.querySelectorAll('.tab-btn');
  const tabPanes   = document.querySelectorAll('.tab-pane');

  tabButtons.forEach(btn => {
    btn.addEventListener('click', function() {
      // 모든 탭 버튼/패널 비활성화
      tabButtons.forEach(x => x.classList.remove('active'));
      tabPanes.forEach(x => x.classList.remove('active'));

      // 클릭된 탭과 해당 패널 활성화
      this.classList.add('active');
      const tabId = this.getAttribute('data-tab');
      document.getElementById(tabId).classList.add('active');
    });
  });

  // 맛집 탭: 그리드/리스트 뷰 토글
  setupViewToggle(
    'grid-view-restaurant',  // 그리드 아이콘 버튼 ID
    'list-view-restaurant',  // 리스트 아이콘 버튼 ID
    'grid-container-restaurant', // 그리드 컨테이너 ID
    'list-container-restaurant'  // 리스트 컨테이너 ID
  );

  // 코스 탭: 그리드/리스트 뷰 토글 (필요 시 구현)
  setupViewToggle(
    'grid-view-course',
    'list-view-course',
    'grid-container-course',
    'list-container-course'
  );

  // 아이템 클릭 시 상세 페이지로 이동
  bindItemClickEvents();

  // 초기 북마크/좋아요/코스북마크 상태 바인딩
  bindInitialStates();

  // 북마크 버튼 토글
  setupRestaurantBookmarkButtons();

  // 코스 좋아요/북마크/공유 버튼 토글
  setupCourseIconButtons();
});


/**
 * 뷰 토글 함수 (그리드 뷰 ↔ 리스트 뷰)
 * @param {string} gridBtnId - 그리드 뷰 버튼 ID 
 * @param {string} listBtnId - 리스트 뷰 버튼 ID
 * @param {string} gridContainerId - 그리드 아이템들이 들어있는 컨테이너 ID
 * @param {string} listContainerId - 리스트 아이템들이 들어있는 컨테이너 ID
 */
function setupViewToggle(gridBtnId, listBtnId, gridContainerId, listContainerId) {
  const gridBtn = document.getElementById(gridBtnId);
  const listBtn = document.getElementById(listBtnId);
  const gridContainer = document.getElementById(gridContainerId);
  const listContainer = document.getElementById(listContainerId);

  if (!gridBtn || !listBtn || !gridContainer || !listContainer) return;

  // 초기 상태: 그리드 보임, 리스트 숨김
  gridContainer.style.display = '';
  listContainer.style.display = 'none';

  // 그리드 버튼 클릭
  gridBtn.addEventListener('click', function() {
    gridBtn.classList.add('active-view');
    listBtn.classList.remove('active-view');
    gridContainer.style.display = '';
    listContainer.style.display = 'none';
  });

  // 리스트 버튼 클릭
  listBtn.addEventListener('click', function() {
    listBtn.classList.add('active-view');
    gridBtn.classList.remove('active-view');
    listContainer.style.display = '';
    gridContainer.style.display = 'none';
  });
}

/**
 * 아이템 클릭 이벤트 바인딩
 * - 레스토랑 아이템 클릭 시 /rstDetail?name=xxx 로 이동
 * - 코스 아이템 클릭 시 /course-detail?id=xxx 로 이동 (예시)
 */
function bindItemClickEvents() {
  // 맛집 아이템
  document.querySelectorAll('.favorite-item.restaurant-item').forEach(item => {
    item.addEventListener('click', function(e) {
      // 북마크 버튼(아이콘)을 눌렀을 때는 상세 이동 방지
      if (e.target.closest('.bookmark-btn')) {
        return;
      }
      const btn = this.querySelector('.bookmark-btn');
      if (btn) {
        const rstId = btn.getAttribute('data-rst-id');
        // ▶ 여기만 수정
        window.location.href = `/rstDetail?rstId=${encodeURIComponent(rstId)}`;
      }
    });
  });


  // 코스 아이템
  document.querySelectorAll('.favorite-item.course-item').forEach(item => {
    item.addEventListener('click', function(e) {
      // 좋아요 / 댓글(코스북마크) / 공유 버튼 클릭 시에는 상세 이동 방지
      if (e.target.closest('.likes') || e.target.closest('.comments') || e.target.closest('.share')) {
        return;
      }
      // data-course-id 에서 읽어서 상세 페이지로 이동 (예시 URL)
      const courseId = this.querySelector('.likes[data-course-id], .comments[data-course-id]').getAttribute('data-course-id');
      window.location.href = `/course-detail?id=${encodeURIComponent(courseId)}`;
    });
  });

  }


/**
 * 서버에서 받아온 “찜한 맛집”/“찜한 코스”의 초기 상태를 읽어 들임
 * (서버에서 내려준 HTML에 이미 active 클래스가 붙어 있으면 그 상태로)
 */
function bindInitialStates() {
  window.restaurantBookmarkStates = {};
  window.courseLikeStates          = {};
  window.courseBookmarkStates      = {};

  // 맛집 북마크 상태
  document.querySelectorAll('.bookmark-btn[data-rst-id]').forEach(btn => {
    const id = btn.getAttribute('data-rst-id');
    restaurantBookmarkStates[id] = btn.classList.contains('active');
  });

  // 코스 좋아요 상태
  document.querySelectorAll('.likes[data-course-id]').forEach(btn => {
    const id = btn.getAttribute('data-course-id');
    const countElem = btn.querySelector('.like-count');
    courseLikeStates[id] = {
      isLiked: btn.classList.contains('active'),
      count: countElem ? parseInt(countElem.textContent) : 0
    };
  });

  // 코스 북마크 상태
  document.querySelectorAll('.comments[data-course-id]').forEach(btn => {
    const id = btn.getAttribute('data-course-id');
    const countElem = btn.querySelector('.bookmark-count');
    courseBookmarkStates[id] = {
      isBookmarked: btn.classList.contains('active'),
      count: countElem ? parseInt(countElem.textContent) : 0
    };
  });
}


/**
 * 맛집 북마크 버튼 클릭 시 토글 로직
 * - 화면에 있는 모든 같은 ID 요소에도 active 상태 동기화
 */
function setupRestaurantBookmarkButtons() {
  document.querySelectorAll('.bookmark-btn[data-rst-id]').forEach(origBtn => {
    // 이벤트 중복 바인딩을 방지하기 위해 cloneNode 후 교체
    const newBtn = origBtn.cloneNode(true);
    origBtn.parentNode.replaceChild(newBtn, origBtn);

    newBtn.addEventListener('click', function(e) {
      e.stopPropagation();
      const rstId = this.getAttribute('data-rst-id');
      const prevState = restaurantBookmarkStates[rstId] || false;
      const nextState = !prevState;
      restaurantBookmarkStates[rstId] = nextState;

      // 아이콘 이미지를 교체
      document.querySelectorAll(`.bookmark-btn[data-rst-id="${rstId}"]`).forEach(btn => {
        const icon = btn.querySelector('.bookmark-icon');
        if (nextState) {
          btn.classList.add('active');
          icon.src = '/images/myFavorites/bookmark-filled.png';
        } else {
          btn.classList.remove('active');
          icon.src = '/images/myFavorites/bookmark.png';
        }
      });

      // 서버에 북마크 추가/제거 요청
      if (nextState) {
        addBookmark(rstId);
      } else {
        removeBookmark(rstId);
      }
    });
  });
}


/**
 * 코스 탭 내 하트(좋아요), 댓글(북마크), 공유 버튼 클릭 토글
 */
function setupCourseIconButtons() {
  // 하트 (좋아요)
  document.querySelectorAll('.likes[data-course-id]').forEach(origBtn => {
    const newBtn = origBtn.cloneNode(true);
    origBtn.parentNode.replaceChild(newBtn, origBtn);

    newBtn.addEventListener('click', function(e) {
      e.stopPropagation();
      const courseId = this.getAttribute('data-course-id');
      const prev = courseLikeStates[courseId] || { isLiked: false, count: 0 };
      const next = {
        isLiked: !prev.isLiked,
        count: prev.isLiked ? prev.count - 1 : prev.count + 1
      };
      courseLikeStates[courseId] = next;

      // 화면에 있는 모든 같은 데이터에도 동기화
      document.querySelectorAll(`.likes[data-course-id="${courseId}"]`).forEach(btn => {
        const heartIcon = btn.querySelector('.heart-icon');
        const countEl   = btn.querySelector('.like-count');

        if (next.isLiked) {
          btn.classList.add('active');
          heartIcon.src = '/images/myFavorites/heart-filled.png';
        } else {
          btn.classList.remove('active');
          heartIcon.src = '/images/myFavorites/heart.png';
        }
        if (countEl) countEl.textContent = next.count;
      });

      // 서버에 좋아요/취소 요청
      toggleCourseLike(courseId, next.isLiked);
    });
  });

  // 댓글(코스북마크)
  document.querySelectorAll('.comments[data-course-id]').forEach(origBtn => {
    const newBtn = origBtn.cloneNode(true);
    origBtn.parentNode.replaceChild(newBtn, origBtn);

    newBtn.addEventListener('click', function(e) {
      e.stopPropagation();
      const courseId = this.getAttribute('data-course-id');
      const prev = courseBookmarkStates[courseId] || { isBookmarked: false, count: 0 };
      const next = {
        isBookmarked: !prev.isBookmarked,
        count: prev.isBookmarked ? prev.count - 1 : prev.count + 1
      };
      courseBookmarkStates[courseId] = next;

      // 화면에 있는 모든 같은 데이터에도 동기화
      document.querySelectorAll(`.comments[data-course-id="${courseId}"]`).forEach(btn => {
        const bkIcon   = btn.querySelector('.bookmark-icon');
        const countEl  = btn.querySelector('.bookmark-count');

        if (next.isBookmarked) {
          btn.classList.add('active');
          bkIcon.src = '/images/myFavorites/bookmark-filled.png';
        } else {
          btn.classList.remove('active');
          bkIcon.src = '/images/myFavorites/bookmark.png';
        }
        if (countEl) countEl.textContent = next.count;
      });

      // 서버에 코스북마크/해제 요청
      toggleCourseBookmark(courseId, next.isBookmarked);
    });
  });

  // 공유 버튼
  document.querySelectorAll('.share[data-course-id]').forEach(origBtn => {
    const newBtn = origBtn.cloneNode(true);
    origBtn.parentNode.replaceChild(newBtn, origBtn);

    newBtn.addEventListener('click', function(e) {
      e.stopPropagation();
      const courseId = this.getAttribute('data-course-id');
      const shareUrl = `${window.location.origin}/course/${courseId}`;

      if (navigator.share) {
        navigator.share({
          title: '싫  맛집 코스 공유',
          text: '이 코스가 마음에 들어요! 확인해보세요.',
          url: shareUrl
        }).catch(err => console.error('공유 실패:', err));
      } else {
        // Web Share API 지원 안 할 때 클립보드 복사 처리
        navigator.clipboard.writeText(shareUrl)
          .then(() => alert('코스 링크가 복사되었습니다!'))
          .catch(() => alert('클립보드 복사에 실패했습니다.'));
      }
    });
  });
}


/**
 * 맛집 북마크 추가 API 호출 예시
 */
function addBookmark(restaurantId) {
  fetch('/api/bookmarks', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ restaurantId: restaurantId })
  })
  .then(res => {
    if (!res.ok) throw new Error('네트워크 응답 오류');
    return res.json();
  })
  .then(data => console.log('북마크 추가 성공:', data))
  .catch(err => console.error('북마크 추가 실패:', err));
}

/**
 * 맛집 북마크 제거 API 호출 예시
 */
function removeBookmark(restaurantId) {
  fetch(`/api/bookmarks/${restaurantId}`, {
    method: 'DELETE'
  })
  .then(res => {
    if (!res.ok) throw new Error('네트워크 응답 오류');
    return res.json();
  })
  .then(data => console.log('북마크 제거 성공:', data))
  .catch(err => console.error('북마크 제거 실패:', err));
}

/**
 * 코스 좋아요 토글 API 호출 예시
 */
function toggleCourseLike(courseId, isLiked) {
  const url    = isLiked ? '/api/courses/like' : `/api/courses/${courseId}/like`;
  const method = isLiked ? 'POST' : 'DELETE';

  fetch(url, {
    method: method,
    headers: { 'Content-Type': 'application/json' },
    body: isLiked ? JSON.stringify({ courseId: courseId }) : undefined
  })
  .then(res => {
    if (!res.ok) throw new Error('네트워크 응답 오류');
    return res.json();
  })
  .then(data => console.log(`코스 좋아요 ${isLiked ? '추가' : '제거'} 성공:`, data))
  .catch(err => console.error(`코스 좋아요 ${isLiked ? '추가' : '제거'} 실패:`, err));
}

/**
 * 코스 북마크 토글 API 호출 예시
 */
function toggleCourseBookmark(courseId, isBookmarked) {
  const url    = isBookmarked ? '/api/courses/bookmark' : `/api/courses/${courseId}/bookmark`;
  const method = isBookmarked ? 'POST' : 'DELETE';

  fetch(url, {
    method: method,
    headers: { 'Content-Type': 'application/json' },
    body: isBookmarked ? JSON.stringify({ courseId: courseId }) : undefined
  })
  .then(res => {
    if (!res.ok) throw new Error('네트워크 응답 오류');
    return res.json();
  })
  .then(data => console.log(`코스 북마크 ${isBookmarked ? '추가' : '제거'} 성공:`, data))
  .catch(err => console.error(`코스 북마크 ${isBookmarked ? '추가' : '제거'} 실패:`, err));
}

document.addEventListener('DOMContentLoaded', function () {
  const urlParams = new URLSearchParams(window.location.search);
  const tabParam = urlParams.get('tab');

  if (tabParam === 'course') {
    // 코스 탭 버튼을 클릭한 것처럼 처리
    document.querySelector('[data-tab="tab2"]')?.click();
  } else {
    // 기본으로 맛집 탭 열기
    document.querySelector('[data-tab="tab1"]')?.click();
  }
});


document.addEventListener("DOMContentLoaded", function () {
	// 코스: 그리드/리스트 뷰 전환
	const gridBtnCourse = document.getElementById("grid-view-course");
	const listBtnCourse = document.getElementById("list-view-course");
	const gridContainerCourse = document.getElementById("grid-container-course");
	const listContainerCourse = document.getElementById("list-container-course");

	gridBtnCourse.addEventListener("click", function () {
		gridBtnCourse.classList.add("active-view");
		listBtnCourse.classList.remove("active-view");
		gridContainerCourse.style.display = "flex";
		listContainerCourse.style.display = "none";
	});

	listBtnCourse.addEventListener("click", function () {
		gridBtnCourse.classList.remove("active-view");
		listBtnCourse.classList.add("active-view");
		gridContainerCourse.style.display = "none";
		listContainerCourse.style.display = "flex";
	});
});
