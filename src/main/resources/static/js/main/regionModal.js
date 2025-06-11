document.addEventListener('DOMContentLoaded', () => {
	// 새로고침 시 localStorage 초기화
	localStorage.removeItem('selectedRegionKey');
	localStorage.removeItem('selectedTags');
	
	const modal = document.getElementById('regionModal');
	const locationSpan = document.querySelector('.location');
	let selectedRegion = '전국'; // 기본값

  const regionData = {
	nationwide: {
	  name: '전국',
	  detail: ['전체']
	},
    capital: {
      name: '전국',
      detail: ['전체']
    },
	seoul: {
	  name: '서울',
	  detail: ['전체', '강남구', '서초구', '관악구', '송파구']
	},
    busan: {
      name: '부산',
      detail: ['전체', '해운대구', '수영구', '진구', '동래구']
    },
    jeju: {
      name: '제주',
      detail: ['전체', '제주시', '서귀포시']
    },
    ulsan: {
      name: '울산',
      detail: ['전체', '울주군', '중구', '동구', '남구', '북구']
    },
  };

  let currentRegionKey = 'nationwide';
  let selectedTags = new Set();

  function updateRegionDetail(key) {
    currentRegionKey = key;
    selectedTags.clear();
    const detailList = regionData[key].detail;

    const regionRight = document.querySelector('.region-right');
    const h4 = regionRight.querySelector('h4');
    const tagContainer = regionRight.querySelector('.region-tags');

    h4.textContent = regionData[key].name;
    tagContainer.innerHTML = '';

    detailList.forEach(name => {
      const span = document.createElement('span');
      span.className = 'region-tag';
      span.textContent = name;
      span.addEventListener('click', () => {
        if (span.classList.contains('selected')) {
          span.classList.remove('selected');
          selectedTags.delete(name);
        } else {
          span.classList.add('selected');
          selectedTags.add(name);
        }
      });
      tagContainer.appendChild(span);
    });
  }

  document.querySelectorAll('.region-left li').forEach(li => {
    li.addEventListener('click', () => {
      document.querySelectorAll('.region-left li').forEach(el => el.classList.remove('active'));
      li.classList.add('active');
      updateRegionDetail(li.dataset.region);
    });
  });

  document.querySelector('.reset-btn').addEventListener('click', () => {
    selectedTags.clear();
    updateRegionDetail(currentRegionKey);
  });

  document.querySelector('.confirm-btn').addEventListener('click', () => {
    const label = regionData[currentRegionKey].name;
	const selectedArray = Array.from(selectedTags);
    const count = selectedTags.size;
    if (count === 0) {
      locationSpan.textContent = label;
    } else if (count === 1) {
      locationSpan.textContent = `${label} ${selectedArray[0]}`;
    } else {
      locationSpan.textContent = `${label} 외 ${count}개`;
    }
	
	// 선택 정보 저장
	localStorage.setItem('selectedRegionKey', currentRegionKey);
	localStorage.setItem('selectedTags', JSON.stringify(selectedArray));
	  
    modal.classList.remove('active');
  });

  document.getElementById('openRegionModal')?.addEventListener('click', () => {
    modal.classList.add('active');
	
	// localStorage에서 이전 선택 복원
    const savedRegion = localStorage.getItem('selectedRegionKey');
    const savedTags = JSON.parse(localStorage.getItem('selectedTags') || '[]');

    if (savedRegion && regionData[savedRegion]) {
      currentRegionKey = savedRegion;
      document.querySelectorAll('.region-left li').forEach(li => {
        li.classList.toggle('active', li.dataset.region === savedRegion);
      });
    }
	
    updateRegionDetail(currentRegionKey);
	
	// 👉 태그 선택 복원 (update 후 실행)
	setTimeout(() => {
	  document.querySelectorAll('.region-tag').forEach(tag => {
	    if (savedTags.includes(tag.textContent)) {
	      tag.classList.add('selected');
	      selectedTags.add(tag.textContent);
	    }
	  });
	}, 0);
  });

  document.getElementById('closeRegionModal')?.addEventListener('click', () => {
    modal.classList.remove('active');
  });
  document.getElementById('closeRegionButton')?.addEventListener('click', () => {
    modal.classList.remove('active');
  });
});