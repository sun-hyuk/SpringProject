import { initializeNaverMap } from './restaurant-map.js';
import { initializeFilterModal } from './filterModalController.js';

// 페이지네이션 위치 상태 관리
let overlayPosition = 'middle'; // 'collapsed', 'middle', 'expanded'
let savedPositions = {
  collapsed: 70,
  middle: 45,
  expanded: 25
};

document.addEventListener('DOMContentLoaded', () => {
  restoreOverlayPosition();
  initializeNaverMap();
  initializeListOverlayDrag();
  updateAllFilterButtons();

  setTimeout(() => initializeFilterModal(), 100);
  initializePaginationPositionSave();

  // ───── 북마크 버튼 토글 기능 ─────
  document.querySelectorAll('.bookmark-btn[data-rstid], .bookmark-btn-inline[data-rstid], .bookmark-icon[data-rstid]').forEach(btn => {
    btn.addEventListener('click', async e => {
      e.stopPropagation();
      e.preventDefault();

      const rstId = btn.getAttribute('data-rstid');
      const url = `/api/jjim/toggle/${rstId}`;

      try {
        const res = await fetch(url, {
          method: 'POST',
          headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });

        if (res.status === 401) {
          alert('로그인 후 이용 가능합니다.');
          return;
        }

        const data = await res.json();

        // 버튼 상태 토글
        const isAdded = data.result === 'added';
        document.querySelectorAll(`[data-rstid="${rstId}"]`).forEach(otherBtn => {
          otherBtn.classList.toggle('active', isAdded);
        });

        // 찜 수 업데이트
        const countEl = document.querySelector(`.jjim-count[data-rstid="${rstId}"]`);
        if (countEl) {
          const current = parseInt(countEl.textContent, 10) || 0;
          countEl.textContent = data.count != null
            ? data.count
            : (isAdded ? current + 1 : current - 1);
        }

        // 시각적 피드백
        btn.style.transform = 'scale(1.2)';
        setTimeout(() => btn.style.transform = '', 200);

        console.log(isAdded ? '북마크 추가됨' : '북마크 제거됨');

      } catch (err) {
        console.error('북마크 오류:', err);
        alert('북마크 처리 중 오류 발생');
      }
    });
  });

  // 디버깅 로그
  console.log('북마크 버튼 개수:', document.querySelectorAll('.bookmark-btn[data-rstid], .bookmark-btn-inline[data-rstid], .bookmark-icon[data-rstid]').length);

  // 검색창 포커스 시 이동
  const searchInput = document.getElementById('searchBar');
  if (searchInput) {
    searchInput.addEventListener('focus', () => { window.location.href = '/search'; });
  }

  // 필터 버튼 상태 추가 업데이트
  setTimeout(() => updateAllFilterButtons(), 200);
  setTimeout(() => updateAllFilterButtons(), 500);
  setTimeout(() => updateAllFilterButtons(), 1000);
});

// 오버레이 위치 저장
function saveCurrentOverlayPosition() {
  const listOverlay = document.getElementById('listOverlay');
  const mapArea = document.getElementById('mapArea');
  if (!listOverlay || !mapArea) return;

  const currentTop = listOverlay.style.top || `calc(${savedPositions[overlayPosition]}vh + 80px)`;
  const currentMapHeight = mapArea.style.height || `${savedPositions[overlayPosition] - 6.67}vh`;

  const positionData = {
    position: overlayPosition,
    overlayTop: currentTop,
    mapHeight: currentMapHeight,
    timestamp: Date.now()
  };

  localStorage.setItem('overlayPosition', JSON.stringify(positionData));
  console.log('오버레이 위치 저장:', positionData);
}

// 오버레이 위치 복원
function restoreOverlayPosition() {
  const savedData = localStorage.getItem('overlayPosition');
  if (!savedData) return;

  try {
    const data = JSON.parse(savedData);
    const maxAge = 5 * 60 * 1000;
    if (Date.now() - data.timestamp > maxAge) {
      localStorage.removeItem('overlayPosition');
      return;
    }

    const listOverlay = document.getElementById('listOverlay');
    const mapArea = document.getElementById('mapArea');
    if (!listOverlay || !mapArea) return;

    listOverlay.classList.add('position-preserved');
    mapArea.classList.add('position-preserved');

    overlayPosition = data.position;
    listOverlay.style.top = data.overlayTop;
    mapArea.style.height = data.mapHeight;

    Object.keys(savedPositions).forEach(pos => {
      listOverlay.classList.remove(pos);
      mapArea.classList.remove(pos);
    });

    if (overlayPosition !== 'middle') {
      listOverlay.classList.add(overlayPosition);
      mapArea.classList.add(overlayPosition);
    }

    setTimeout(() => {
      listOverlay.classList.remove('position-preserved');
      mapArea.classList.remove('position-preserved');
    }, 100);

    console.log('오버레이 위치 복원:', data);

  } catch (e) {
    console.error('복원 오류:', e);
    localStorage.removeItem('overlayPosition');
  }
}

// 페이지네이션 위치 저장 기능
function initializePaginationPositionSave() {
  document.querySelectorAll('.pagination .page-link').forEach(link => {
    link.addEventListener('click', () => {
      saveCurrentOverlayPosition();
    });
  });
}

// 오버레이 드래그 기능 초기화
function initializeListOverlayDrag() {
  const dragHandle = document.getElementById('dragHandle');
  const listOverlay = document.getElementById('listOverlay');
  const mapArea = document.getElementById('mapArea');
  if (!dragHandle || !listOverlay || !mapArea) return;

  let isDragging = false;
  let startY = 0;
  let startTop = 0;

  function startDrag(clientY) {
    isDragging = true;
    startY = clientY;
    startTop = parseFloat(listOverlay.style.top) || savedPositions[overlayPosition];
    dragHandle.classList.add('dragging');
    listOverlay.classList.add('dragging');
    mapArea.classList.add('dragging');
    document.body.style.userSelect = 'none';
  }

  function updateDrag(clientY) {
    if (!isDragging) return;
    const deltaY = clientY - startY;
    const vh = window.innerHeight;
    let newTop = startTop + (deltaY / vh * 100);
    newTop = Math.max(20, Math.min(75, newTop));

    mapArea.style.height = `${newTop - (80 / vh * 100)}vh`;
    listOverlay.style.top = `${newTop}vh`;
  }

  function endDrag() {
    if (!isDragging) return;
    isDragging = false;
    dragHandle.classList.remove('dragging');
    listOverlay.classList.remove('dragging');
    mapArea.classList.remove('dragging');
    document.body.style.userSelect = '';

    const currentTop = parseFloat(listOverlay.style.top) || savedPositions[overlayPosition];
    let nearest = 'middle';
    let minDist = Math.abs(currentTop - savedPositions.middle);

    Object.entries(savedPositions).forEach(([pos, val]) => {
      const dist = Math.abs(currentTop - val);
      if (dist < minDist) {
        minDist = dist;
        nearest = pos;
      }
    });

    snapToPosition(nearest);
  }

  function snapToPosition(pos) {
    overlayPosition = pos;
    const vh = savedPositions[pos];
    const mapHeight = vh - (80 / window.innerHeight * 100);
    listOverlay.style.top = `${vh}vh`;
    mapArea.style.height = `${mapHeight}vh`;

    Object.keys(savedPositions).forEach(p => {
      listOverlay.classList.remove(p);
      mapArea.classList.remove(p);
    });

    if (pos !== 'middle') {
      listOverlay.classList.add(pos);
      mapArea.classList.add(pos);
    }

    saveCurrentOverlayPosition();
  }

  dragHandle.addEventListener('mousedown', e => { e.preventDefault(); startDrag(e.clientY); });
  document.addEventListener('mousemove', e => updateDrag(e.clientY));
  document.addEventListener('mouseup', endDrag);

  dragHandle.addEventListener('touchstart', e => {
    e.preventDefault();
    startDrag(e.touches[0].clientY);
  }, { passive: false });
  document.addEventListener('touchmove', e => {
    if (isDragging) {
      e.preventDefault();
      updateDrag(e.touches[0].clientY);
    }
  }, { passive: false });
  document.addEventListener('touchend', endDrag);

  let lastTap = 0;
  dragHandle.addEventListener('touchend', e => {
    const now = new Date().getTime();
    if (now - lastTap < 500) {
      e.preventDefault();
      snapToPosition(overlayPosition === 'expanded' ? 'collapsed' : 'expanded');
    }
    lastTap = now;
  });

  document.addEventListener('keydown', e => {
    if (e.ctrlKey || e.metaKey) {
      if (e.key === '1') snapToPosition('collapsed');
      if (e.key === '2') snapToPosition('middle');
      if (e.key === '3') snapToPosition('expanded');
    }
  });
}

// 필터 상태 업데이트
function updateAllFilterButtons() {
  const state = window.FILTER_STATE || {};
  const params = new URLSearchParams(window.location.search);
  const currentSort = state.currentSort || params.get('sort') || 'name';
  const region = state.region || params.get('region') || '';
  const details = state.details || params.get('details') || '';
  const types = state.types || params.get('types') || '';
  const newStore = state.newStore || params.get('new') || '';

  updateSortButton(currentSort);
  updateRegionButton(region, details);
  updateTypeButton(types);
  updateNewButton(newStore);
}

function updateSortButton(currentSort) {
  const sortBtn = document.getElementById('sortButton');
  const sortLabel = document.getElementById('sortLabel');
  if (!sortBtn || !sortLabel) return;

  const labels = {
    'name': '가나다순',
    'reviews': '리뷰 많은순',
    'rating': '별점 높은순',
    'jjim': '찜 많은순'
  };
  const label = labels[currentSort] || '가나다순';
  sortLabel.textContent = label;
  sortBtn.classList.toggle('active', currentSort !== 'name');
}

function updateRegionButton(region, details) {
  const btn = document.getElementById('regionButton');
  const span = btn?.querySelector('span');
  if (!btn || !span) return;

  if (region && region !== '전체') {
    let text = region;
    const detailList = details?.split(',').filter(d => d.trim()) || [];
    if (detailList.length > 0) {
      text += ` ${detailList[0]}${detailList.length > 1 ? ` 외 ${detailList.length - 1}개` : ''}`;
    }
    span.textContent = text;
    btn.classList.add('active');
  } else {
    span.textContent = '지역';
    btn.classList.remove('active');
  }
}

function updateTypeButton(types) {
  const btn = document.getElementById('typeButton');
  const span = btn?.querySelector('span');
  if (!btn || !span) return;

  const typeList = types?.split(',').filter(t => t.trim()) || [];
  if (typeList.length > 0) {
    span.textContent = `${typeList[0]}${typeList.length > 1 ? ` 외 ${typeList.length - 1}개` : ''}`;
    btn.classList.add('active');
  } else {
    span.textContent = '음식 종류';
    btn.classList.remove('active');
  }
}

function updateNewButton(newStore) {
  const btn = document.getElementById('newButton');
  const span = btn?.querySelector('span');
  if (!btn || !span) return;

  if (newStore) {
    span.textContent = newStore;
    btn.classList.add('active');
  } else {
    span.textContent = '신상 가게';
    btn.classList.remove('active');
  }
}

// 외부에서 호출 가능하도록
window.updateAllFilterButtons = updateAllFilterButtons;
