document.addEventListener('DOMContentLoaded', () => {
  const courseGrid = document.getElementById('courseGrid');
  let allCourses = [];

  // 1) 코스 목록 API 호출
  fetch('/api/courses/member')
    .then(res => {
      if (!res.ok) throw new Error("응답 실패");
      return res.json();
    })
    .then(data => {
      console.log("받은 데이터:", data);
      allCourses = data;
      renderCourses(data);
    })
    .catch(err => {
      console.error("코스 데이터 가져오기 실패:", err);
    });

  // 2) 렌더링 함수
  function renderCourses(courses) {
    courseGrid.innerHTML = '';
    if (courses.length === 0) {
      courseGrid.innerHTML = '<p class="no-results">등록된 코스가 없습니다.</p>';
      return;
    }
    courses.forEach(course => {
      courseGrid.appendChild(createCard(course));
    });
  }

  // 3) 카드 생성
  function createCard(course) {
    const id = course.courseId ?? course.course_id;
    if (!id) return document.createElement('div');

    const imgSrc = course.image?.startsWith('/') ? course.image : '/' + course.image;
    const fallback = '/images/tasteCourse/default.png';
    const detailUrl = `/tasteCourseDetail?course=${id}`;

    const isBookmarked = course.jjimmed === true;
    const bookmarkClass = isBookmarked ? 'action-button bookmark-button bookmarked' : 'action-button bookmark-button';
    const bookmarkIcon = isBookmarked ? '/images/tasteCourse/bookmark-filled.png' : '/images/tasteCourse/bookmark.png';

    const card = document.createElement('div');
    card.className = 'course-card';
    card.innerHTML = `
      <a href="${detailUrl}" class="course-image-link">
        <img class="course-image" src="${imgSrc}" alt="${course.title}" onerror="this.onerror=null;this.src='${fallback}'">
      </a>
      <div class="course-info">
        <h3 class="course-title">${course.title}</h3>
        <div class="course-actions">
          <button class="${bookmarkClass}" data-id="${id}">
            <img class="action-icon" src="${bookmarkIcon}" alt="북마크">
            <span>${course.jjimCount}</span>
          </button>
          <button class="action-button share-button" data-id="${id}">
            <img class="action-icon" src="/images/tasteCourse/share.png" alt="공유">
          </button>
        </div>
      </div>`;
    return card;
  }

  // 4) 북마크/공유 이벤트
  courseGrid.addEventListener('click', e => {
    const btn = e.target.closest('button');
    if (!btn) return;

    const id = btn.dataset.id;

	// 북마크 토글
	if (btn.classList.contains('bookmark-button')) {
	  // ✅ 로그인 확인 먼저
	  if (document.body.dataset.loggedIn !== "true") {
		// 현재 주소를 서버에 전달해서 세션에 저장시킴
		  fetch('/savePrevPage', {
		    method: 'POST',
		    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		    body: `url=${encodeURIComponent(location.pathname + location.search)}`
		  }).then(() => {
		    showLoginRequiredPopup();
		  });

		  return;
		}

	  // ✅ 로그인 되어 있으면 UI 토글
	  const span = btn.querySelector('span');
	  const icon = btn.querySelector('img');
	  const cnt  = Number(span.textContent);
	  const on   = btn.classList.toggle('bookmarked');
	  span.textContent = on ? cnt + 1 : cnt - 1;
	  icon.src = on
	    ? '/images/tasteCourse/bookmark-filled.png'
	    : '/images/tasteCourse/bookmark.png';

	  const url = on ? '/api/jjim' : `/api/jjim/${id}`;
	  const method = on ? 'POST' : 'DELETE';

	  fetch(url, {
	    method: method,
	    headers: { 'Content-Type': 'application/json' },
	    credentials: 'include',
	    body: on ? JSON.stringify({ courseId: id }) : null
	  })
	    .then(res => {
	      if (res.status === 401) {
	        showLoginRequiredPopup();
	        throw new Error('로그인 필요');
	      }
	      return res.text();
	    })
	    .then(result => {
	      if (result !== 'added' && result !== 'removed') {
	        alert('찜 처리 중 오류가 발생했습니다.');
	      }
	    })
	    .catch(() => {
	      alert('서버 연결에 실패했습니다.');
	    });
	}

    // 공유
    if (btn.classList.contains('share-button')) {
      const url = `${location.origin}/tasteCourseDetail?course=${id}`;
      if (navigator.share) {
        navigator.share({ title: '맛집 코스 공유', url }).catch(() => {});
      } else {
        navigator.clipboard.writeText(url)
          .then(() => alert('링크가 복사되었습니다!'));
      }
    }
  });
  // 5) 지역 필터링 (제목 포함 기준)
  regionFilter.addEventListener('change', () => {
    const sel = regionFilter.value;  // "전체", "서울", "부산" 등
    let filtered;

    if (sel === '전체') {
      // 전체 코스 보여주기
      filtered = allCourses;
    } else {
      // 제목에 sel(지역명)이 들어간 코스만
      filtered = allCourses.filter(course =>
        typeof course.title === 'string' && course.title.includes(sel)
      );
    }

    renderCourses(filtered);
  });
  
});



