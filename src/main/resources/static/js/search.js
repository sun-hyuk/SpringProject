/**
 * search.js
 * - 로그인한 회원별로 최근 검색어 관리
 * - 서버와 로컬스토리지 동기화
 */
document.addEventListener('DOMContentLoaded', () => {
  const searchForm      = document.getElementById('searchForm');
  const searchInput     = document.querySelector('.search-bar');
  const recentTagList   = document.querySelectorAll('.tag-list')[0]; // 최근 검색어
  const storeTagList    = document.querySelectorAll('.tag-list')[1]; // 최근 매장
  const maxItems        = 15;

  let recentSearches = [];
  let recentStores   = JSON.parse(localStorage.getItem('recentStores')) || [];
  let isProcessingSearch = false; // 중복 처리 방지 플래그

  /* ------------- 서버에서 로그인 회원의 최근 검색어 가져오기 ------------- */
  function loadRecentSearches() {
    fetch('/api/search/recent')
      .then(res => res.ok ? res.json() : null)
      .then(list => {
        if (list && Array.isArray(list) && list.length > 0) {
          recentSearches = list.map(s => s.keyword);
          localStorage.setItem('recentSearches', JSON.stringify(recentSearches));
          renderRecentSearches();
        } else {
          recentSearches = [];
          localStorage.removeItem('recentSearches');
          renderRecentSearches();
        }
      })
      .catch(err => {
        recentSearches = [];
        localStorage.removeItem('recentSearches');
        renderRecentSearches();
      });
  }

  /* ---------------- 렌더링 함수 ---------------- */
  function renderRecentSearches() {
    recentTagList.innerHTML = '';
    recentSearches.forEach(text => {
      const tag = document.createElement('span');
      tag.className = 'tag';
      tag.innerHTML = `${text} <img src="/images/main/location/x.png" alt="x" class="close-icon">`;

      // 태그 클릭 시 검색
      tag.addEventListener('click', e => {
        if (!e.target.classList.contains('close-icon')) {
          searchInput.value = text;
          addSearch(text);
          window.location.href = `/rstSearch?q=${encodeURIComponent(text)}`;
        }
      });

      // X 버튼 클릭 시 삭제
      tag.querySelector('.close-icon').addEventListener('click', e => {
        e.stopPropagation();
        removeSearch(text);
      });

      recentTagList.appendChild(tag);
    });
  }

  function renderRecentStores() {
    storeTagList.innerHTML = '';
    recentStores.forEach(store => {
      const a = document.createElement('a');
      a.href = `/store/detail?name=${encodeURIComponent(store)}`;
      a.className = 'tag';
      a.innerHTML = `${store} <img src="/images/main/location/x.png" alt="x" class="close-icon">`;

      a.querySelector('.close-icon').addEventListener('click', e => {
        e.preventDefault();
        e.stopPropagation();
        removeStore(store);
      });

      storeTagList.appendChild(a);
    });
  }

  /* ---------------- 데이터 조작 ---------------- */
  function addSearch(value) {
    value = value.trim();
    if (!value || isProcessingSearch) return;
    
    isProcessingSearch = true;

    // 중복 제거 및 최상단 배치
    recentSearches = recentSearches.filter(item => item !== value);
    recentSearches.unshift(value);
    recentSearches = recentSearches.slice(0, maxItems);
    
    localStorage.setItem('recentSearches', JSON.stringify(recentSearches));
    renderRecentSearches();

    // 서버 저장
    fetch('/api/search/log', {
      method : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body   : JSON.stringify({ keyword: value })
    })
    .finally(() => {
      isProcessingSearch = false;
    });
  }

  function removeSearch(value) {
    recentSearches = recentSearches.filter(item => item !== value);
    localStorage.setItem('recentSearches', JSON.stringify(recentSearches));
    renderRecentSearches();
    
    fetch('/api/search/delete', {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ keyword: value })
    });
  }

  function addStore(name) {
    name = name.trim();
    if (!name) return;

    const idx = recentStores.indexOf(name);
    if (idx !== -1) recentStores.splice(idx, 1);
    recentStores.unshift(name);
    recentStores = recentStores.slice(0, maxItems);

    localStorage.setItem('recentStores', JSON.stringify(recentStores));
    renderRecentStores();
  }

  function removeStore(name) {
    recentStores = recentStores.filter(item => item !== name);
    localStorage.setItem('recentStores', JSON.stringify(recentStores));
    renderRecentStores();
  }

  /* ---------------- 이벤트 ---------------- */
  if (searchForm) {
    searchForm.addEventListener('submit', e => {
      e.preventDefault();
      const value = searchInput.value.trim();
      if (!value) return;
      
      // (1) 검색 기록 저장
      addSearch(value);

      // (2) 검색어 입력창 비우기
      searchInput.value = '';

      // (3) 검색 결과 페이지로 이동
      window.location.href = `/rstSearch?q=${encodeURIComponent(value)}`;
    });
  }

  // 실시간 검색어 기능
  const rankGrid = document.querySelector('.rank-grid');
  if (rankGrid) {
    loadTrendingKeywords();

    rankGrid.addEventListener('click', e => {
      const target = e.target.closest('a');
      if (!target || isProcessingSearch) return;
      e.preventDefault();

      const text = target.dataset.keyword;
      if (text) {
        addSearch(text);
        setTimeout(() => {
          window.location.href = `/rstSearch?q=${encodeURIComponent(text)}`;
        }, 100);
      }
    });
  }

  /* ------------- 실시간 검색어 기능 ------------- */
  function loadTrendingKeywords() {
    fetch('/api/search/trending')
      .then(res => res.ok ? res.json() : [])
      .then(trending => {
        renderTrendingKeywords(trending);
      })
      .catch(err => {
        console.error('❌ 실시간 검색어 로드 실패:', err);
      });
  }

  function renderTrendingKeywords(trending) {
    const rankGrid = document.querySelector('.rank-grid');
    if (!rankGrid) return;

    rankGrid.innerHTML = '';
    
    if (!trending || trending.length === 0) {
      rankGrid.innerHTML = '<p>아직 검색어가 없습니다</p>';
      return;
    }

    const maxItems = Math.min(trending.length, 10);
    
    for (let i = 0; i < maxItems; i++) {
      const item = trending[i];
      const rank = i + 1;
      const keyword = item[0];
      const count = item[1];

      const a = document.createElement('a');
      // 인기 검색어 클릭 시 맛집 검색 페이지로 이동
      a.href = `/rstSearch?q=${encodeURIComponent(keyword)}`;
      a.dataset.keyword = keyword;

      // 변경 지표 표시 (same/up/down/new)
      const changeTypes = ['same', 'up', 'down', 'new'];
      const changeType = changeTypes[Math.floor(Math.random() * changeTypes.length)];
      const changeNumber = changeType === 'same' ? '' : Math.floor(Math.random() * 5) + 1;

      a.innerHTML = `
        <strong class="rank">${rank}</strong>
        <span class="keyword">${keyword}</span>
        <span class="change-indicator ${changeType}"></span>
        ${changeNumber ? `<span class="change-number">${changeNumber}</span>` : ''}
      `;

      rankGrid.appendChild(a);
    }
  }

  /* ---------------- 초기화 ---------------- */
  loadRecentSearches();
  renderRecentStores();

  // 실시간 검색어 5분마다 자동 갱신
  setInterval(() => {
    if (document.querySelector('.rank-grid')) {
      loadTrendingKeywords();
    }
  }, 5 * 60 * 1000);
});
