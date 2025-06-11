document.addEventListener('DOMContentLoaded', function () {
  const isLoggedIn = document.body.dataset.loggedIn === 'true';
  const alarmDot = document.getElementById('alarmDot');
  const bellLink = document.querySelector('.icon-bell');

  if (isLoggedIn && alarmDot) {
    fetch('/alerts/unread')
      .then(res => res.json())
      .then(data => {
        if (data.hasUnread) {
          alarmDot.style.display = 'inline-block';
        } else {
          alarmDot.style.display = 'none';
        }
      })
      .catch(err => {
        console.error('알림 확인 실패:', err);
      });

    if (bellLink) {
      bellLink.addEventListener('click', function () {
        fetch('/alerts/read/all', { method: 'POST' })
          .then(res => res.text())
          .then(result => {
            console.log('🔔 전체 알림 읽음 처리됨:', result);
            alarmDot.style.display = 'none';
          })
          .catch(err => {
            console.error('🔔 읽음 처리 실패:', err);
          });
      });
    }
  }

  // ✅ 리뷰 클릭
  document.querySelectorAll('.review-grid .review-item').forEach(item => {
    item.addEventListener('click', function () {
      const onclick = this.getAttribute('onclick');
      if (onclick) eval(onclick);
    });
  });

  // ✅ 기존 리뷰 클릭
  document.querySelectorAll('.review-list .review-item').forEach(item => {
    item.addEventListener('click', function () {
      const rstId = this.dataset.rstid;
      if (rstId) {
        window.location.href = '/rstDetail?rstId=' + rstId;
      }
    });
  });

  // ✅ 찜한 코스 클릭
  document.querySelectorAll('.recommend-grid .recommend-item').forEach(item => {
    item.addEventListener('click', function () {
      const courseId = this.getAttribute('data-course-id');
      if (courseId) {
        window.location.href = '/tasteCourseDetail?course=' + courseId;
      }
    });
  });

  // ✅ 🔥 최근 방문한 맛집 클릭
  document.querySelectorAll('.order-grid .order-item').forEach(item => {
    item.addEventListener('click', function () {
      const onclick = this.getAttribute('onclick');
      if (onclick) eval(onclick);
    });
  });
});