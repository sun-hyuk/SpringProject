export function initializeFilterModal() {
  const regionData = {
    '전체': { detail: ['전체'] },
    '서울': { detail: ['전체', '강남구', '서초구'] },
    '부산': { detail: ['전체', '해운대구', '수영', '남구', '중구'] },
    '제주': { detail: ['전체', '제주시', '서귀포시'] },
    '울산': { detail: ['전체', '남구', '동구', '북구', '중구'] },
  };

  const scrollWrapper = document.querySelector('.filter-scroll-wrapper');
  const sections      = document.querySelectorAll('.filter-section');
  let isProgrammaticScroll = false;

  /*===============================
    1) URL 파라미터 읽기 헬퍼
  ===============================*/
  function getCurrentFilterParams() {
    const params = new URLSearchParams(window.location.search);
    return {
      region:  params.get('region') || '',
      details: params.get('details')|| '',
      types:   params.get('types')  || '',
      newTag:  params.get('new')    || ''
    };
  }

  /*===============================
    2) 확인 버튼 보이/숨김
  ===============================*/
  function updateConfirmButtonVisibility() {
    const confirmBtn = document.querySelector('.confirm-btn');
    if (confirmBtn) {
      // 항상 선택 완료 버튼을 표시하도록 변경
      confirmBtn.style.display = 'block';
    }
  }

  /*===============================
    3) 상단 탭 라벨 업데이트 (페이지의 필터 버튼들)
  ===============================*/
  function updateFilterButtonLabels() {
    // 현재 선택된 값들 수집
    const selectedRegionBtn = document.querySelector('.region-btn.selected');
    const selectedDetailTags = Array.from(document.querySelectorAll('#regionSection .detail-tags .region-tag.selected'));
    const selectedTypeTags = Array.from(document.querySelectorAll('#typeSection .type-tag.selected'));
    const selectedNewTags = Array.from(document.querySelectorAll('#newSection .new-tag.selected'));

    // FILTER_STATE 실시간 업데이트
    window.FILTER_STATE = window.FILTER_STATE || {};
    window.FILTER_STATE.region = selectedRegionBtn ? selectedRegionBtn.dataset.region : '';
    window.FILTER_STATE.details = selectedDetailTags.map(tag => tag.textContent.trim()).join(',');
    window.FILTER_STATE.types = selectedTypeTags.map(tag => tag.textContent.trim()).join(',');
    window.FILTER_STATE.newStore = selectedNewTags.length > 0 ? selectedNewTags[0].textContent.trim() : '';

    console.log('모달에서 필터 상태 업데이트:', window.FILTER_STATE);

    // 즉시 필터 버튼 업데이트
    if (window.updateAllFilterButtons) {
      window.updateAllFilterButtons();
    }
    
    // 추가 안전장치로 약간의 지연 후에도 업데이트
    setTimeout(() => {
      if (window.updateAllFilterButtons) {
        window.updateAllFilterButtons();
      }
    }, 50);
  }

  /*===============================
    4) 탭 클릭 시 해당 섹션으로 스크롤
  ===============================*/
  function activateFilterTab(tabType, skipScroll = false) {
    const targetSection = document.getElementById(`${tabType}Section`);
    if (!scrollWrapper || !targetSection) return;

    document.querySelectorAll('.filter-tab-bar .tab').forEach(tab => {
      tab.classList.toggle('active', tab.dataset.tab === tabType);
    });

    if (!skipScroll) {
      const modalTop          = scrollWrapper.getBoundingClientRect().top;
      const sectionTopInModal = targetSection.getBoundingClientRect().top;
      const currentScroll     = scrollWrapper.scrollTop;
      const offset            = sectionTopInModal - modalTop;

      isProgrammaticScroll = true;
      scrollWrapper.scrollTo({
        top: currentScroll + offset - 10,
        behavior: 'smooth'
      });
      setTimeout(() => {
        isProgrammaticScroll = false;
      }, 300);
    }
  }

  if (scrollWrapper) {
    scrollWrapper.addEventListener('scroll', () => {
      if (isProgrammaticScroll) return;

      const scrollTop      = scrollWrapper.scrollTop;
      const wrapperHeight  = scrollWrapper.clientHeight;
      const scrollHeight   = scrollWrapper.scrollHeight;
      let currentTab = '';
      let minDiff    = Infinity;

      sections.forEach(section => {
        const sectionTop    = section.offsetTop;
        const sectionHeight = section.offsetHeight;
        const center        = scrollTop + wrapperHeight / 2;
        const sectionCenter = sectionTop + sectionHeight / 2;
        const diff          = Math.abs(center - sectionCenter);
        if (diff < minDiff) {
          minDiff    = diff;
          currentTab = section.id.replace('Section', '');
        }
      });

      if (scrollTop + wrapperHeight >= scrollHeight - 5) {
        currentTab = sections[sections.length - 1].id.replace('Section', '');
      }
      if (scrollTop <= 1) {
        currentTab = sections[0].id.replace('Section', '');
      }

      if (currentTab) {
        document.querySelectorAll('.filter-tab-bar .tab').forEach(tab => {
          tab.classList.toggle('active', tab.dataset.tab === currentTab);
        });
      }
    });
  }

  /*===============================
    5) 초기 렌더링: Region / Detail / Type / New 생성
  ===============================*/
  function renderRegionButtons() {
    const container = document.querySelector('.region-btn-container');
    if (!container) return;
    container.innerHTML = '';
    for (const regionName in regionData) {
      const btn = document.createElement('button');
      btn.className     = 'region-btn';
      btn.dataset.region = regionName;
      btn.textContent   = regionName;

      btn.addEventListener('click', () => {
        document.querySelectorAll('.region-btn').forEach(b => b.classList.remove('selected'));
        btn.classList.add('selected');
        renderDetailTags(regionName);
        updateConfirmButtonVisibility();
        updateFilterButtonLabels();
      });

      container.appendChild(btn);
    }
  }

  function renderDetailTags(regionName) {
    const tagContainer = document.querySelector('#regionSection .detail-tags');
    if (!tagContainer) return;
    tagContainer.innerHTML = '';
    const details = regionData[regionName] ? regionData[regionName].detail : ['전체'];
    details.forEach(name => {
      const span = document.createElement('span');
      span.className   = 'region-tag';
      span.textContent = name;
      span.addEventListener('click', () => {
        span.classList.toggle('selected');
        updateConfirmButtonVisibility();
        updateFilterButtonLabels();
      });
      tagContainer.appendChild(span);
    });
  }

  const foodTypes = ['카페, 디저트', '한식', '중식', '일식', '양식', '치킨', '햄버거', '피자', '분식', '고기', '해산물'];
  function renderFoodTypeTags() {
    const container = document.querySelector('.type-tag-container');
    if (!container) return;
    container.innerHTML = '';
    foodTypes.forEach(type => {
      const span = document.createElement('span');
      span.className   = 'region-tag type-tag';
      span.textContent = type;
      span.addEventListener('click', () => {
        span.classList.toggle('selected');
        updateConfirmButtonVisibility();
        updateFilterButtonLabels();
        console.log('음식 종류 선택:', type, span.classList.contains('selected')); // 디버깅
      });
      container.appendChild(span);
    });
  }

  const newStoreTags = ['오픈 1개월 이내', '오픈 3개월 이내', '최근 등록'];
  function renderNewStoreTags() {
    const container = document.querySelector('.new-tag-container');
    if (!container) return;
    container.innerHTML = '';
    newStoreTags.forEach(label => {
      const span = document.createElement('span');
      span.className   = 'region-tag new-tag';
      span.textContent = label;
      span.addEventListener('click', () => {
        const alreadySelected = span.classList.contains('selected');
        container.querySelectorAll('.new-tag.selected').forEach(tag => tag.classList.remove('selected'));
        if (!alreadySelected) span.classList.add('selected');
        updateConfirmButtonVisibility();
        updateFilterButtonLabels();
        console.log('신상 가게 선택:', label, span.classList.contains('selected')); // 디버깅
      });
      container.appendChild(span);
    });
  }

  // 최초 렌더링
  renderRegionButtons();
  renderDetailTags('전체');
  renderFoodTypeTags();
  renderNewStoreTags();

  /*===============================
    6) 모달 열 때 기존 선택 복원
  ===============================*/
  function restoreSelections() {
    const { region, details, types, newTag } = getCurrentFilterParams();
    
    console.log('복원할 파라미터:', { region, details, types, newTag }); // 디버깅용

    // – Region 버튼 복원
    if (region && region !== '전체') {
      document.querySelectorAll('.region-btn').forEach(btn => {
        if (btn.dataset.region === region) {
          btn.classList.add('selected');
          renderDetailTags(region);
        } else {
          btn.classList.remove('selected');
        }
      });
    } else {
      // 기본값 '전체' 선택
      document.querySelectorAll('.region-btn').forEach(btn => {
        if (btn.dataset.region === '전체') {
          btn.classList.add('selected');
          renderDetailTags('전체');
        } else {
          btn.classList.remove('selected');
        }
      });
    }
    
    // – Detail 태그 복원
    if (details) {
      const detailArray = details.split(',');
      setTimeout(() => { // DOM 렌더링 후 실행
        document.querySelectorAll('#regionSection .detail-tags .region-tag').forEach(tagEl => {
          const tagText = tagEl.textContent.trim();
          if (detailArray.includes(tagText)) {
            tagEl.classList.add('selected');
            console.log('Detail 선택 복원:', tagText); // 디버깅용
          }
        });
      }, 50);
    }
    
    // – Type 태그 복원
    if (types) {
      const typeArray = types.split(',');
      console.log('복원할 음식 종류:', typeArray); // 디버깅용
      document.querySelectorAll('#typeSection .type-tag').forEach(tagEl => {
        const tagText = tagEl.textContent.trim();
        if (typeArray.includes(tagText)) {
          tagEl.classList.add('selected');
          console.log('Type 선택 복원:', tagText); // 디버깅용
        } else {
          tagEl.classList.remove('selected');
        }
      });
    } else {
      // types가 없으면 모든 선택 해제
      document.querySelectorAll('#typeSection .type-tag').forEach(tagEl => {
        tagEl.classList.remove('selected');
      });
    }
    
    // – New 태그 복원
    if (newTag) {
      console.log('복원할 신상 가게:', newTag); // 디버깅용
      document.querySelectorAll('#newSection .new-tag').forEach(tagEl => {
        if (tagEl.textContent.trim() === newTag.trim()) {
          tagEl.classList.add('selected');
          console.log('New 선택 복원:', newTag); // 디버깅용
        } else {
          tagEl.classList.remove('selected');
        }
      });
    } else {
      // newTag가 없으면 모든 선택 해제
      document.querySelectorAll('#newSection .new-tag').forEach(tagEl => {
        tagEl.classList.remove('selected');
      });
    }
  }

  /*===============================
    7) 페이지 로드 시 초기 상태 설정
  ===============================*/
  function initializePageState() {
    const { region, details, types, newTag } = getCurrentFilterParams();
    
    // 현재 URL 파라미터에 따라 필터 버튼 상태 설정
    updateFilterButtonLabels();
    
    // 모달 없이도 기본 선택 상태 복원
    restoreSelections();
  }

  /*===============================
    8) 모달 열기, 닫기, 탭, 정렬, 확인, 리셋 바인딩
  ===============================*/
  document.querySelectorAll('[data-open-modal]').forEach(btn => {
    btn.addEventListener('click', () => {
      const modalId = btn.getAttribute('data-open-modal');
      const modal   = document.getElementById(modalId);
      if (!modal) return;
      modal.classList.add('active');
      document.body.classList.add('modal-open');

      if (modalId === 'filterModal') {
        // 다시 한번 선택 상태 복원
        setTimeout(() => {
          restoreSelections();
          updateFilterButtonLabels();
          updateConfirmButtonVisibility();
        }, 100);
        
        const tabType = btn.getAttribute('data-tab');
        if (tabType) {
          activateFilterTab(tabType, true);
        }
      }
    });
  });

  document.querySelectorAll('.filter-tab-bar .tab').forEach(tab => {
    tab.addEventListener('click', () => {
      activateFilterTab(tab.getAttribute('data-tab'));
    });
  });

  document.querySelectorAll('[data-close-modal]').forEach(btn => {
    btn.addEventListener('click', () => {
      const modal = btn.closest('.modal');
      if (modal) {
        modal.classList.remove('active');
        document.body.classList.remove('modal-open');
      }
    });
  });

  document.querySelectorAll('.modal').forEach(modal => {
    modal.addEventListener('click', e => {
      if (e.target === modal) {
        modal.classList.remove('active');
        document.body.classList.remove('modal-open');
      }
    });
  });

  // "선택 완료" 클릭 → 라벨 업데이트 → 모달 닫기 → 리다이렉트
  const confirmBtn = document.querySelector('.confirm-btn');
  if (confirmBtn) {
    confirmBtn.addEventListener('click', () => {
      console.log('선택 완료 버튼 클릭됨'); // 디버깅

      const selectedRegionBtn = document.querySelector('.region-btn.selected');
      const region = selectedRegionBtn ? selectedRegionBtn.dataset.region : '';

      const selectedDetailTags = Array.from(
        document.querySelectorAll('#regionSection .detail-tags .region-tag.selected')
      ).map(el => el.textContent.trim());

      const selectedTypeTags = Array.from(
        document.querySelectorAll('#typeSection .type-tag.selected')
      ).map(el => el.textContent.trim());

      const selectedNewTags = Array.from(
        document.querySelectorAll('#newSection .new-tag.selected')
      ).map(el => el.textContent.trim());

      console.log('선택된 필터:', {
        region,
        details: selectedDetailTags,
        types: selectedTypeTags,
        newTags: selectedNewTags
      }); // 디버깅

      const url    = new URL(window.location.href);
      const params = new URLSearchParams(url.search);

      // 지역 필터 처리
      if (region && region !== '전체') {
        params.set('region', region);
      } else {
        params.delete('region');
      }

      // 상세 지역 필터 처리
      if (selectedDetailTags.length && !selectedDetailTags.includes('전체')) {
        params.set('details', selectedDetailTags.join(','));
      } else {
        params.delete('details');
      }

      // 음식 종류 필터 처리
      if (selectedTypeTags.length) {
        params.set('types', selectedTypeTags.join(','));
        console.log('음식 종류 파라미터 설정:', selectedTypeTags.join(',')); // 디버깅
      } else {
        params.delete('types');
        console.log('음식 종류 파라미터 삭제'); // 디버깅
      }

      // 신상 가게 필터 처리
      if (selectedNewTags.length) {
        params.set('new', selectedNewTags[0]);
        console.log('신상 가게 파라미터 설정:', selectedNewTags[0]); // 디버깅
      } else {
        params.delete('new');
        console.log('신상 가게 파라미터 삭제'); // 디버깅
      }

      // 페이지를 첫 페이지로 리셋
      params.set('page', '0');

      const finalUrl = `/rstSearch?${params.toString()}`;
      console.log('최종 URL:', finalUrl); // 디버깅

      document.getElementById('filterModal')?.classList.remove('active');
      document.body.classList.remove('modal-open');

      window.location.href = finalUrl;
    });
  }

  // 리셋 버튼
  document.querySelector('.region-reset-btn')?.addEventListener('click', () => {
    document.querySelectorAll('.region-btn').forEach(btn => btn.classList.remove('selected'));
    document.querySelectorAll('#regionSection .region-tag').forEach(tag => tag.classList.remove('selected'));
    updateConfirmButtonVisibility();
    updateFilterButtonLabels();
  });
  document.querySelector('.type-reset-btn')?.addEventListener('click', () => {
    document.querySelectorAll('#typeSection .type-tag').forEach(tag => tag.classList.remove('selected'));
    updateConfirmButtonVisibility();
    updateFilterButtonLabels();
  });
  document.querySelector('.new-reset-btn')?.addEventListener('click', () => {
    document.querySelectorAll('#newSection .new-tag').forEach(tag => tag.classList.remove('selected'));
    updateConfirmButtonVisibility();
    updateFilterButtonLabels();
  });

  // 페이지 로드 시 초기 상태 설정 (가장 마지막에 실행)
  setTimeout(() => {
    initializePageState();
  }, 200);
}