/**
 * 나의 문의 페이지 기능 구현
 * - 그리드 뷰와 리스트 뷰 전환 기능
 * - 문의 클릭 시 상세 페이지 이동 기능
 */
document.addEventListener('DOMContentLoaded', function() {
  // 그리드 컨테이너와 뷰 전환 버튼
  const gridContainer = document.getElementById('grid-container');
  const gridViewBtn = document.getElementById('grid-view');
  const listViewBtn = document.getElementById('list-view');
  
  // 리스트 뷰 컨테이너 생성
  const listContainer = document.createElement('div');
  listContainer.className = 'inquiry-list';
  listContainer.id = 'list-container';
  
  // 그리드 아이템 복제 후 리스트 컨테이너에 추가
  const gridItems = document.querySelectorAll('.inquiry-item');
  gridItems.forEach(item => {
    const clone = item.cloneNode(true);
    
    // 리스트 뷰용 구조로 변경
    const content = clone.querySelector('.inquiry-content');
    const inquiryText = content.querySelector('.inquiry-text');
    
    // 내용을 감싸는 div 생성
    const contentInfo = document.createElement('div');
    contentInfo.className = 'content-info';
    
    // 텍스트 이동
    contentInfo.appendChild(inquiryText.cloneNode(true));
    
    // 기존 요소 제거
    inquiryText.remove();
    
    // 새 구조 추가
    content.appendChild(contentInfo);
    
    listContainer.appendChild(clone);
  });
  
  // 리스트 컨테이너를 그리드 컨테이너 다음에 추가
  gridContainer.parentNode.insertBefore(listContainer, gridContainer.nextSibling);
  
  // 그리드 뷰 버튼 클릭 이벤트
  gridViewBtn.addEventListener('click', function() {
    gridContainer.style.display = 'grid';
    listContainer.style.display = 'none';
    gridViewBtn.classList.add('active-view');
    listViewBtn.classList.remove('active-view');
  });
  
  // 리스트 뷰 버튼 클릭 이벤트
  listViewBtn.addEventListener('click', function() {
    gridContainer.style.display = 'none';
    listContainer.style.display = 'flex';
    listViewBtn.classList.add('active-view');
    gridViewBtn.classList.remove('active-view');
  });
  
  // 모든 문의 컨텐츠에 클릭 이벤트 추가 (그리드 뷰)
  const gridInquiryContents = document.querySelectorAll('#grid-container .inquiry-content');
  gridInquiryContents.forEach(content => {
    content.addEventListener('click', function() {
      // inquiryDetail.html로 이동
      window.location.href = '/inquiryDetail.html';
    });
  });
  
  // 모든 문의 컨텐츠에 클릭 이벤트 추가 (리스트 뷰)
  const listInquiryContents = document.querySelectorAll('#list-container .inquiry-content');
  listInquiryContents.forEach(content => {
    content.addEventListener('click', function() {
      // inquiryDetail.html로 이동
      window.location.href = '/inquiryDetail.html';
    });
  });
});