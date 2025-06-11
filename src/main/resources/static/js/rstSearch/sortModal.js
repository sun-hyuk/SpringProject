document.addEventListener('DOMContentLoaded', () => {
  const openBtn   = document.querySelector('[data-open-modal="sortModal"]');
  const modal     = document.getElementById('sortModal');
  const backdrop  = document.getElementById('modalBackdrop');
  const closeBtns = modal.querySelectorAll('[data-close-modal]');
  const options   = modal.querySelectorAll('.modal-options li');

  // 정렬 옵션과 라벨 매핑
  const sortLabels = {
    'name': '가나다순',
    'reviews': '리뷰 많은순',
    'rating': '별점 높은순',
    'jjim': '찜 많은순'
  };

  // 현재 정렬 상태 확인 및 버튼 텍스트 업데이트
  function updateSortButtonLabel() {
    const params = new URLSearchParams(window.location.search);
    const currentSort = params.get('sort') || 'name';
    const sortButton = document.querySelector('[data-open-modal="sortModal"]');
    const sortLabel = sortButton.querySelector('#sortLabel');
    
    if (sortLabel) {
      sortLabel.textContent = sortLabels[currentSort] || '가나다순';
    }
    
    // 정렬이 기본값(name)이 아닌 경우 버튼을 활성화 스타일로 변경
    if (currentSort !== 'name') {
      sortButton.classList.add('active');
    } else {
      sortButton.classList.remove('active');
    }
  }

  // 페이지 로드 시 초기 상태 설정
  updateSortButtonLabel();

  openBtn.addEventListener('click', () => {
    modal.hidden = false;
    backdrop.hidden = false;
  });

  // 닫기 버튼과 백드롭 모두에 닫기 이벤트 달기
  [...closeBtns, backdrop].forEach(el =>
    el.addEventListener('click', () => {
      modal.hidden = true;
      backdrop.hidden = true;
    })
  );

  options.forEach(li => {
    li.addEventListener('click', () => {
      options.forEach(o => o.classList.remove('active'));
      li.classList.add('active');

      const sortKey = li.dataset.sort;
      const params = new URLSearchParams(window.location.search);
      params.set('sort', sortKey);
      params.delete('page');
      
      // 모달 닫기
      modal.hidden = true;
      backdrop.hidden = true;
      
      // 즉시 버튼 라벨 업데이트
      const sortButton = document.querySelector('[data-open-modal="sortModal"]');
      const sortLabel = sortButton.querySelector('#sortLabel');
      if (sortLabel) {
        sortLabel.textContent = sortLabels[sortKey] || '가나다순';
      }
      
      // 정렬이 기본값이 아닌 경우 활성화 스타일 적용
      if (sortKey !== 'name') {
        sortButton.classList.add('active');
      } else {
        sortButton.classList.remove('active');
      }
      
      // 페이지 이동
      window.location.search = params.toString();
    });
  });
});