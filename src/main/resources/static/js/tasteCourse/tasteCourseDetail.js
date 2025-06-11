document.addEventListener('DOMContentLoaded', () => {
  const qs = s => document.querySelector(s);

  const bookmarkBtn = qs('.bookmark-btn');
  const shareBtn = qs('.share-btn');
  const courseId = Number(new URLSearchParams(location.search).get('course'));
  const isLoggedIn = document.body.dataset.loggedIn === "true";

  if (!bookmarkBtn) return;

  // ⭐ 찜 버튼 클릭 처리
  bookmarkBtn.addEventListener('click', () => {
    if (!isLoggedIn) {
      showLoginRequiredPopup();  // ⛔ 로그인 팝업 표시
      return;
    }

    const icon = bookmarkBtn.querySelector('img');
    const countSpan = bookmarkBtn.querySelector('span');
    const isBookmarked = bookmarkBtn.classList.toggle('bookmarked');
    const currentCount = Number(countSpan.textContent);

    // UI 업데이트
    icon.src = isBookmarked
      ? '/images/tasteCourse/bookmark-filled.png'
      : '/images/tasteCourse/bookmark.png';
    countSpan.textContent = isBookmarked ? currentCount + 1 : currentCount - 1;

    // 서버 연동
    const url = isBookmarked ? '/api/jjim' : `/api/jjim/${courseId}`;
    const method = isBookmarked ? 'POST' : 'DELETE';

    fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: isBookmarked ? JSON.stringify({ courseId }) : null
    })
    .then(res => {
      if (res.status === 401) {
        showLoginRequiredPopup();
        throw new Error("Unauthorized");
      }
      return res.text();
    })
    .then(result => {
      if (!result.includes('완료') && result !== 'added' && result !== 'removed') {
        alert("찜 처리 중 오류가 발생했습니다.");
      }
    })
    .catch(err => {
      console.error(err);
      alert("서버 오류가 발생했습니다.");
    });
  });

  // 📤 공유 버튼 처리
  if (shareBtn) {
    shareBtn.addEventListener('click', () => {
      const url = `${location.origin}/tasteCourseDetail?course=${courseId}`;
      if (navigator.share) {
        navigator.share({ title: document.title, url }).catch(() => {});
      } else {
        navigator.clipboard.writeText(url)
          .then(() => alert("링크가 복사되었습니다!"));
      }
    });
  }
});


// ✅ 로그인 필요 팝업 함수 (해당 ID 존재해야 함)
function showLoginRequiredPopup() {
  const popup = document.getElementById('loginRequiredPopup');
  if (popup) popup.classList.add('active');
}

function hideLoginPopup() {
  const popup = document.getElementById('loginRequiredPopup');
  if (popup) popup.classList.remove('active');
}

function goToLogin() {
  const currentUrl = encodeURIComponent(window.location.href);
  window.location.href = `/login?redirectURL=${currentUrl}`;
}
