// 로그아웃 기능
function logout() {
    // Spring Boot 방식으로 로그아웃 처리
    // POST 방식으로 요청하거나 logout endpoint로 이동
    window.location.href = '/logout';
}

// 자바스크립트 백업 메뉴 활성화 (Thymeleaf가 작동하지 않을 경우를 대비)
document.addEventListener("DOMContentLoaded", function() {
    // Thymeleaf로 활성화된 메뉴가 있는지 확인
    const hasActiveMenu = document.querySelector(".admin-sidebar ul li a.admin-active");
    
    // Thymeleaf로 활성화된 메뉴가 없을 경우에만 자바스크립트로 처리
    if (!hasActiveMenu) {
        const currentPath = window.location.pathname;
        
        // 페이지 그룹 매핑 - 자바스크립트 버전
        const pageGroups = {
            '/admin/main': '대시보드',
            '/admin/users': '사용자 관리',
            '/admin/restaurants': '가게 승인/관리',
            '/admin/approval': '가게 승인/관리',
            '/admin/courses': '맛집 코스 등록',
            '/admin/event': '이벤트 등록/관리',
            '/admin/participants': '이벤트 등록/관리',
            '/admin/inquiry': '문의 관리',
            '/admin/reports': '신고 관리',
            '/admin/notices': '공지 관리'
        };
        
        // 현재 페이지가 속한 그룹 찾기
        const currentGroup = pageGroups[currentPath];
        
        // 그룹에 맞는 메뉴 활성화
        if (currentGroup) {
            const sidebarLinks = document.querySelectorAll(".admin-sidebar ul li a");
            
            sidebarLinks.forEach(link => {
                if (link.textContent.trim() === currentGroup) {
                    link.classList.add("admin-active");
                }
            });
        }
    }
});

// 추가: 메뉴 클릭 시 활성화 상태 변경 (SPA 방식일 경우 사용)
function setActiveMenu(menuText) {
    // 기존 활성화된 메뉴 제거
    const activeMenus = document.querySelectorAll(".admin-sidebar ul li a.admin-active");
    activeMenus.forEach(menu => {
        menu.classList.remove("admin-active");
    });
    
    // 새로운 메뉴 활성화
    const sidebarLinks = document.querySelectorAll(".admin-sidebar ul li a");
    sidebarLinks.forEach(link => {
        if (link.textContent.trim() === menuText) {
            link.classList.add("admin-active");
        }
    });
}

// 추가: 사이드바 토글 기능 (모바일 대응)
function toggleSidebar() {
    const sidebar = document.getElementById('adminSidebar');
    sidebar.classList.toggle('sidebar-collapsed');
}

// 추가: 반응형 사이드바 처리
function handleResponsiveSidebar() {
    const sidebar = document.getElementById('adminSidebar');
    const header = document.getElementById('adminHeader');
    
    if (window.innerWidth <= 768) {
        // 모바일에서는 사이드바를 숨김 처리할 수 있음
        sidebar.classList.add('mobile-sidebar');
    } else {
        sidebar.classList.remove('mobile-sidebar');
    }
}

// 화면 크기 변경 시 반응형 처리
window.addEventListener('resize', handleResponsiveSidebar);

// 페이지 로드 시 반응형 처리 실행
handleResponsiveSidebar();