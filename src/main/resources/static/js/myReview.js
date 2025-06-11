/**
 * 나의 리뷰 페이지 기능 구현
 * - 그리드 뷰와 리스트 뷰 전환 기능
 * - 리뷰 클릭 시 상세 페이지 이동 기능
 */
document.addEventListener('DOMContentLoaded', function() {
  const gridContainer = document.getElementById('grid-container');
  const gridViewBtn = document.getElementById('grid-view');
  const listViewBtn = document.getElementById('list-view');

  const listContainer = document.createElement('div');
  listContainer.className = 'review-list';
  listContainer.id = 'list-container';

  const gridItems = document.querySelectorAll('.review-item');
  gridItems.forEach(item => {
    const clone = item.cloneNode(true);

    const content = clone.querySelector('.review-content');
    const reviewText = content.querySelector('.review-text');
    const stats = content.querySelector('.review-stats');

    const contentInfo = document.createElement('div');
    contentInfo.className = 'content-info';
    contentInfo.appendChild(reviewText.cloneNode(true));
    contentInfo.appendChild(stats.cloneNode(true));

    reviewText.remove();
    stats.remove();
    content.appendChild(contentInfo);

    // 원래 rstId도 같이 복사 (중요!)
    clone.dataset.rstid = item.dataset.rstid;

    listContainer.appendChild(clone);
  });

  gridContainer.parentNode.insertBefore(listContainer, gridContainer.nextSibling);

  gridViewBtn.addEventListener('click', function() {
    gridContainer.style.display = 'grid';
    listContainer.style.display = 'none';
    gridViewBtn.classList.add('active-view');
    listViewBtn.classList.remove('active-view');
  });

  listViewBtn.addEventListener('click', function() {
    gridContainer.style.display = 'none';
    listContainer.style.display = 'flex';
    listViewBtn.classList.add('active-view');
    gridViewBtn.classList.remove('active-view');
  });

  // 👉 리뷰 클릭 시 rstId 붙여서 이동 (그리드 뷰)
  const gridReviewContents = document.querySelectorAll('#grid-container .review-content');
  gridReviewContents.forEach(content => {
    content.addEventListener('click', function() {
      const rstId = this.closest('.review-item').dataset.rstid;
      if (rstId) {
        window.location.href = '/rstDetail?rstId=' + rstId;
      }
    });
    content.style.cursor = 'pointer';
  });

  // 👉 리뷰 클릭 시 rstId 붙여서 이동 (리스트 뷰)
  const listReviewContents = document.querySelectorAll('#list-container .review-content');
  listReviewContents.forEach(content => {
    content.addEventListener('click', function() {
      const rstId = this.closest('.review-item').dataset.rstid;
      if (rstId) {
        window.location.href = '/rstDetail?rstId=' + rstId;
      }
    });
    content.style.cursor = 'pointer';
  });
});
