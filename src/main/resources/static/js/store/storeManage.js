document.addEventListener("DOMContentLoaded", function () {
    const body = document.body;
    const memberId = body.dataset.memberId;

    console.log("현재 로그인한 사용자 ID:", memberId);

    // 예: 로그인 상태 체크
    if (memberId) {
      // 로그인 된 사용자
    } else {
      // 비로그인 상태 (예외 처리)
    }
	
	// 이벤트 바인딩은 DOM 로드 이후에 해야 안전함!
    const mainImageInput = document.getElementById("mainImage");
    if (mainImageInput) {
      mainImageInput.addEventListener("change", previewImage);
    }
});

// URL 파라미터에서 탭 정보 가져오기
function getUrlParameter(name) {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(name);
}

// 탭 전환 함수
function switchTab(tabName) {
  // 1) 모든 탭 비활성화
  document.querySelectorAll('.history-tabs .pill').forEach(pill => {
    pill.classList.remove('active');
  });
  
  // 2) 선택된 탭 활성화
  const targetPill = document.querySelector(`[data-filter="${tabName}"]`);
  if (targetPill) {
    targetPill.classList.add('active');
  }
  
  // 3) 모든 리스트 숨기기
  document.getElementById('myStores').style.display = 'none';
  document.getElementById('appRequests').style.display = 'none';
  
  // 4) 선택된 리스트만 표시
  const targetList = document.getElementById(tabName);
  if (targetList) {
    targetList.style.display = 'block';
  }
}

// 페이지 로드 즉시 실행 (DOMContentLoaded 이전에)
(function() {
  const tabParam = getUrlParameter('tab');
  if (tabParam && (tabParam === 'myStores' || tabParam === 'appRequests')) {
    // URL 파라미터가 있으면 해당 탭으로 바로 설정
    setTimeout(() => switchTab(tabParam), 0);
  } else {
    // 기본값: 내 가게 목록
    setTimeout(() => switchTab('myStores'), 0);
  }
})();

// 탭 클릭 이벤트
document.addEventListener('DOMContentLoaded', function() {
  document.querySelectorAll('.history-tabs .pill').forEach(pill => {
    pill.addEventListener('click', () => {
      const filter = pill.dataset.filter;
      switchTab(filter);
      
      // URL 파라미터 업데이트 (브라우저 히스토리에 추가하지 않음)
      const newUrl = new URL(window.location);
      newUrl.searchParams.set('tab', filter);
      window.history.replaceState({}, '', newUrl);
    });
  });
});