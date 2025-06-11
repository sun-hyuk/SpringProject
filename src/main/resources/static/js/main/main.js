function toggleKeywordBar() {
	const dropdown = document.getElementById("keywordDropdown");
    const icon = document.getElementById("dropdownIcon");

    const isOpen = dropdown.style.display === "flex";

    // 토글 드롭다운 영역
    dropdown.style.display = isOpen ? "none" : "flex";

    // 화살표 방향 전환
    icon.textContent = isOpen ? "▲" : "▼";
    
    // 드롭다운이 열릴 때 실시간 검색어 새로고침
    if (!isOpen) {
        loadMainTrendingKeywords();
    }
}

/* ------------- 메인 페이지 실시간 검색어 기능 ------------- */
let mainTrendingData = []; // 실시간 검색어 데이터 저장
let currentRankIndex = 0;  // 현재 표시 중인 순위
let rankRotationInterval = null; // 순환 인터벌

function loadMainTrendingKeywords() {
    fetch('/api/search/trending')
        .then(res => res.ok ? res.json() : [])
        .then(trending => {
            console.log('🔥 메인 실시간 검색어:', trending);
            mainTrendingData = trending; // 데이터 저장
            updateMainTrendingDisplay();
            startRankRotation(); // 순환 시작
        })
        .catch(err => {
            console.error('❌ 메인 실시간 검색어 로드 실패:', err);
        });
}

function updateMainTrendingDisplay() {
    // 1. 상단 실시간 검색어 순환 표시 (현재 인덱스에 따라)
    const rankInfo = document.querySelector('.rank-info');
    if (rankInfo && mainTrendingData.length > 0) {
        const currentIndex = currentRankIndex % mainTrendingData.length;
        const keyword = mainTrendingData[currentIndex][0];
        const rank = currentIndex + 1;
        
        // 텍스트 길이 제한 (최대 6글자로 더 줄임)
        const displayKeyword = keyword.length > 6 ? keyword.substring(0, 6) + '...' : keyword;
        
        rankInfo.innerHTML = `<strong>${rank}</strong> ${displayKeyword}`;
        
        // 스타일 고정 - 너비를 더 작게 고정
        rankInfo.style.cursor = 'pointer';
        rankInfo.style.whiteSpace = 'nowrap';
        rankInfo.style.overflow = 'hidden';
        rankInfo.style.textOverflow = 'ellipsis';
        rankInfo.style.width = '80px'; // 고정 너비로 변경 (기존 min/max 대신)
        rankInfo.style.flexShrink = '0'; // 크기 축소 방지
        rankInfo.style.display = 'inline-block'; // 인라인 블록으로 설정
        
        // 클릭 이벤트 추가
        rankInfo.onclick = () => {
            window.location.href = `/rstSearch?q=${encodeURIComponent(keyword)}`;
        };
    }

    // 2. 드롭다운 실시간 검색어 (최대 9개, 등수별 숫자 표시)
    const keywordGrid = document.querySelector('.keyword-grid');
    if (keywordGrid) {
        keywordGrid.innerHTML = '';
        
        if (mainTrendingData.length === 0) {
            keywordGrid.innerHTML = '<span class="ellipsis">아직 검색어가 없습니다</span>';
            return;
        }

        const maxItems = Math.min(mainTrendingData.length, 9);
        for (let i = 0; i < maxItems; i++) {
            const keyword = mainTrendingData[i][0];
            const rank = i + 1; // 1, 2, 3, 4, 5, 6, 7, 8, 9
            
            const span = document.createElement('span');
            span.className = 'ellipsis';
            span.innerHTML = `<strong>${rank}</strong> ${keyword}`;
            span.style.cursor = 'pointer';
            
            // 클릭 이벤트 추가
            span.addEventListener('click', () => {
                window.location.href = `/rstSearch?q=${encodeURIComponent(keyword)}`;
            });
            
            keywordGrid.appendChild(span);
        }
        
        console.log(`🏆 메인 실시간 검색어 ${maxItems}개 표시 (1~${maxItems}위)`);
    }
}

function startRankRotation() {
    // 기존 인터벌 정리
    if (rankRotationInterval) {
        clearInterval(rankRotationInterval);
    }
    
    // 검색어가 없으면 순환하지 않음
    if (mainTrendingData.length === 0) return;
    
    // 3초마다 다음 순위로 변경
    rankRotationInterval = setInterval(() => {
        currentRankIndex = (currentRankIndex + 1) % mainTrendingData.length;
        
        // 상단 실시간 검색어만 업데이트
        const rankInfo = document.querySelector('.rank-info');
        if (rankInfo) {
            const keyword = mainTrendingData[currentRankIndex][0];
            const rank = currentRankIndex + 1;
            
            // 텍스트 길이 제한 (최대 6글자로 더 줄임)
            const displayKeyword = keyword.length > 6 ? keyword.substring(0, 6) + '...' : keyword;
            
            rankInfo.innerHTML = `<strong>${rank}</strong> ${displayKeyword}`;
            
            // 스타일 유지
            rankInfo.style.cursor = 'pointer';
            rankInfo.style.whiteSpace = 'nowrap';
            rankInfo.style.overflow = 'hidden';
            rankInfo.style.textOverflow = 'ellipsis';
            rankInfo.style.width = '80px'; // 고정 너비
            rankInfo.style.flexShrink = '0'; // 크기 축소 방지
            rankInfo.style.display = 'inline-block'; // 인라인 블록으로 설정
            
            rankInfo.onclick = () => {
                window.location.href = `/rstSearch?q=${encodeURIComponent(keyword)}`;
            };
        }
    }, 3000); // 3초마다 변경
}

// 슬라이드
document.addEventListener('DOMContentLoaded', () => {
  // 페이지 로드 시 실시간 검색어 불러오기
  loadMainTrendingKeywords();
  
  // 5분마다 자동 갱신
  setInterval(() => {
    loadMainTrendingKeywords();
    console.log('🔄 메인 실시간 검색어 자동 갱신');
  }, 5 * 60 * 1000); // 5분

  const slidesContainer = document.querySelector('.slides');
  const slides = document.querySelectorAll('.slide');
  const dots = document.querySelectorAll('.dot');

  let currentIndex = 0;
  const slideCount = slides.length;

  let startX = 0;
  let currentX = 0;
  let isDragging = false;
  let animationFrameId = null;

  const slideWidth = () => slidesContainer.clientWidth;

  function setSliderPosition(translateX) {
    slidesContainer.style.transform = `translateX(${translateX}px)`;
  }

  function updateCarousel() {
    const offset = -currentIndex * slideWidth();
    setSliderPosition(offset);

    dots.forEach((dot, i) => {
      dot.classList.toggle('active', i === currentIndex);
    });
  }

  function goTo(index) {
    currentIndex = (index + slideCount) % slideCount;
    updateCarousel();
  }

  // 자동 슬라이드
  let autoSlide = setInterval(() => {
    goTo(currentIndex + 1);
  }, 4000);

  // dot 클릭
  dots.forEach((dot, i) => {
    dot.addEventListener('click', () => {
      clearInterval(autoSlide);
      goTo(i);
    });
  });

  // 터치 시작
  slidesContainer.addEventListener('touchstart', (e) => {
    startX = e.touches[0].clientX;
    currentX = startX;
    isDragging = true;
    cancelAnimationFrame(animationFrameId);
    clearInterval(autoSlide);
    slidesContainer.style.transition = 'none';
  });

  // 터치 중
  slidesContainer.addEventListener('touchmove', (e) => {
    if (!isDragging) return;
    currentX = e.touches[0].clientX;
    const delta = currentX - startX;
    const baseX = -currentIndex * slideWidth();
    setSliderPosition(baseX + delta);
  });

  // 터치 끝
  slidesContainer.addEventListener('touchend', () => {
    const deltaX = currentX - startX;
    isDragging = false;
    slidesContainer.style.transition = 'transform 0.3s ease';

    if (deltaX > 50) {
      goTo(currentIndex - 1);
    } else if (deltaX < -50) {
      goTo(currentIndex + 1);
    } else {
      goTo(currentIndex);
    }

    autoSlide = setInterval(() => {
      goTo(currentIndex + 1);
    }, 4000);
  });

  // 반응형 대응
  window.addEventListener('resize', updateCarousel);

  updateCarousel();
  
  // 카테고리 아이템 클릭 이벤트 추가 (로그인 팝업 지원)
  const categoryItems = document.querySelectorAll('.category-item');
  categoryItems.forEach(item => {
    item.addEventListener('click', function(e) {
      // onclick 속성이 있는 경우 (로그인 필요한 항목) 기본 동작 중지
      if (this.getAttribute('onclick')) {
        return; // onclick 이벤트가 처리하도록 함
      }
      
      // href 속성이 있는 경우 (일반 링크) 기본 동작 유지
      if (this.getAttribute('href') && this.getAttribute('href') !== 'javascript:void(0)') {
        return; // 브라우저 기본 링크 동작 유지
      }
      
      // 카테고리 이름 가져오기
      const categoryName = this.querySelector('p').textContent;
      
      // 카테고리별 경로 설정
      let path = '';
      switch(categoryName) {
        case '카페·디저트':
          path = '/category/cafe';
          break;
        case '한식':
          path = '/category/korean';
          break;
        case '중식':
          path = '/category/chinese';
          break;
        case '일식':
          path = '/category/japanese';
          break;
        case '양식':
          path = '/category/western';
          break;
        case '치킨':
          path = '/category/chicken';
          break;
        case '햄버거':
          path = '/category/burger';
          break;
        case '피자':
          path = '/category/pizza';
          break;
        case '분식':
          path = '/category/tteokbokki';
          break;
        case '고기':
          path = '/category/meat';
          break;
        case '해산물':
          path = '/category/seafood';
          break;
        // 커뮤니티 탭 카테고리 - href가 있는 항목들은 이미 위에서 처리됨
        case '공지사항':
          path = '/notice';
          break;
        case '게시판':
          path = '/board';
          break;
        case '이벤트':
          path = '/event';
          break;
        case '음식뽑기':
          path = '/foodRecommend';
          break;
        // 운영 탭 카테고리 - href가 있는 항목들은 이미 위에서 처리됨
        case '하루맛집코스':
          path = '/tasteCourse';
          break;
        default:
          path = '/category';
      }
      
      // 페이지 이동 (onclick이나 href가 없는 경우만)
      if (path) {
        window.location.href = path;
      }
    });
  });
  
  // 검색바 클릭 이벤트
  const searchBar = document.querySelector('.search-bar');
  if (searchBar) {
    searchBar.addEventListener('click', function() {
      window.location.href = '/search';
    });
  }
  
  // 알림 아이콘 클릭 이벤트
  const alarmIcon = document.querySelector('img[alt="알림"]');
  if (alarmIcon) {
	alarmIcon.addEventListener('click', function () {
	    // 1. DB에 읽음 처리 요청
	    fetch('/alerts/read/all', {
	      method: 'POST'
	    })
	      .then(res => res.ok ? res.text() : Promise.reject('읽음 처리 실패'))
	      .then(() => {
	        // 2. 빨간 점 숨기기
	        hideAlarmDot();

	        // 3. 알림 페이지로 이동
	        window.location.href = '/alerts';
	      })
	      .catch(err => {
	        console.error('❌ 알림 읽음 처리 실패:', err);
	        // 실패하더라도 일단 이동
	        window.location.href = '/alerts';
	      });
	  });
  }
  
  // 상단 북마크 아이콘 클릭 이벤트
  const bookmarkIcon = document.querySelector('.icon2.bookmark-icon');
  if (bookmarkIcon) {
    bookmarkIcon.addEventListener('click', function() {
      window.location.href = '/myFavorites';
    });
  }
  
  // 음식 종류별 BEST 썸네일 클릭 이벤트
  const bestFoodItems = document.querySelectorAll('.category-thumbnail-item');
  bestFoodItems.forEach(item => {
    item.addEventListener('click', function() {
      const foodType = this.querySelector('p').textContent;
      window.location.href = `/category/${encodeURIComponent(foodType)}`;
    });
  });
  
  // 최근 방문한 맛집, 새로 오픈한 가게 클릭 이벤트
  const restaurantCards = document.querySelectorAll('.restaurant-card');
  restaurantCards.forEach(card => {
    card.addEventListener('click', function(e) {
      // 북마크 아이콘 클릭 시 이벤트 처리 방지
      if (e.target.classList.contains('bookmark-icon') || 
          e.target.closest('.bookmark-icon')) {
        return;
      }
      
      // 가게 이름 가져오기
      const restaurantName = this.querySelector('h4').textContent;
      // 상세 페이지로 이동
      window.location.href = `/restaurant/${encodeURIComponent(restaurantName)}`;
    });
  });
  
  // "전체 보기" 링크 클릭 이벤트 추가
  const viewAllLinks = document.querySelectorAll('.view-all');
  viewAllLinks.forEach(link => {
    link.addEventListener('click', function(e) {
      e.preventDefault(); // 기본 링크 동작 방지
      
      // 해당 섹션의 제목 찾기
      const sectionTitle = this.closest('.section-header')
                              .querySelector('.section-title h3')
                              .textContent;
      
      // 섹션 제목에 따라 다른 경로로 이동 이 부분 수정
      let path = '';
	  let queryParams = ''; // 쿼리 파라미터를 추가할 변수

	  			switch (sectionTitle) {
	  				case '음식 종류별 BEST':
						path = '/rstSearch';
						queryParams = '?page=0&sort=jjim';
	  					break;
	  				case '최근 방문한 맛집':
	  					path = '/rstVisited';
	  					break;
	  				case '새로 오픈 했어요!':
	  					path = '/rstSearch';
	  					queryParams = '?new=최근%20등록&page=0';  // 최근 등록 필터를 쿼리 파라미터로 추가
	  					break;
	  				default:
	  					path = '/';
	  			}

	  			// 페이지 이동 (쿼리 파라미터가 있으면 추가)
	  			window.location.href = path + queryParams;
    });
  });
});

// 카테고리 탭 슬라이드 스크립트
document.addEventListener('DOMContentLoaded', () => {
  const tabs = document.querySelectorAll('.tab');
  const tabSlider = document.getElementById('tabSlider');

  let isTabClick = false; // 탭 클릭 여부를 추적

  tabs.forEach((tab, index) => {
    tab.addEventListener('click', () => {
      isTabClick = true;

      // 탭 스타일 업데이트
      tabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');

      // 해당 인덱스로 슬라이더 스크롤 이동
      const slideWidth = tabSlider.clientWidth;
      tabSlider.scrollTo({
        left: slideWidth * index,
        behavior: 'smooth'
      });

      // 일정 시간 후 탭 클릭 플래그 해제 (스크롤이 끝날 시간)
      setTimeout(() => {
        isTabClick = false;
      }, 400); // scrollTo와 비슷한 시간 (0.4초)
    });
  });

  // 사용자 스와이프에만 탭 동기화 적용
  tabSlider.addEventListener('scroll', () => {
    if (isTabClick) return; // 탭 클릭 중에는 무시

    const index = Math.round(tabSlider.scrollLeft / tabSlider.clientWidth);
    tabs.forEach(t => t.classList.remove('active'));
    if (tabs[index]) tabs[index].classList.add('active');
  });
});

// 북마크 토글 (카드 내부 북마크용)
document.addEventListener('DOMContentLoaded', () => {
  // 카드 내부 북마크 아이콘 이벤트 설정
  document.querySelectorAll('.icon3.bookmark-icon').forEach(icon => {
    icon.addEventListener('click', function(e) {
      // 토글 기능 유지
      this.classList.toggle('active');
      
      // 이벤트 버블링 중지 (부모 요소로 이벤트 전파 방지)
      e.stopPropagation();
    });
  });
});


/**
 * 알림-----------------------------------------------------------------------------------------------------------
 */
// 알림이 있으면 빨간 점 표시
function showAlarmDot() {
  const dot = document.getElementById('alarmDot');
  if (dot) {
    dot.style.display = 'block';
  }
}

// 알림이 없으면 빨간 점 숨기기
function hideAlarmDot() {
  const dot = document.getElementById('alarmDot');
  if (dot) {
    dot.style.display = 'none';
  }
}

// 서버에서 알림 여부 확인 (예시 API: /api/alerts/unread)
function checkForUnreadAlarms() {
  fetch('/alerts/unread')
    .then(res => res.ok ? res.json() : Promise.reject('API 오류'))
    .then(data => {
      // data.hasUnread === true일 경우 표시
      if (data.hasUnread) {
        showAlarmDot();
      } else {
        hideAlarmDot();
      }
    })
    .catch(err => {
      console.error('❌ 알림 여부 확인 실패:', err);
    });
}

// 페이지 로드시 알림 상태 확인
document.addEventListener('DOMContentLoaded', () => {
  checkForUnreadAlarms();
});