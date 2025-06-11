// 팝업 표시 함수
function showLoginPopup() {
    const popup = document.getElementById('loginRequiredPopup');
    if (popup) {
        popup.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
}

// 팝업 숨기기 함수
function hideLoginPopup() {
    const popup = document.getElementById('loginRequiredPopup');
    if (popup) {
        popup.classList.remove('active');
        document.body.style.overflow = 'auto';
    }
}

// 로그인하러 가기
function goToLogin() {
    // login 페이지로 이동
    window.location.href = '/login';
    
    // 또는 다른 경로로 설정하려면:
    // window.location.href = '/login';
    // window.location.href = 'pages/login.html';
}

// 전역 함수로 내보내기
window.showLoginPopup = showLoginPopup;
window.hideLoginPopup = hideLoginPopup;
window.goToLogin = goToLogin;

console.log('로그인 팝업 시스템이 로드되었습니다.');
console.log('사용법: showLoginPopup() - 팝업 표시');