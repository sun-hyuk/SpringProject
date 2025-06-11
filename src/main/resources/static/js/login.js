/**
 * EatoMeter 로그인 페이지 JavaScript
 * 파일 경로: src/main/resources/static/js/login.js
 */

// DOM 로드 완료 후 실행
document.addEventListener('DOMContentLoaded', function() {
    initializeLoginForm();
});

/**
 * 로그인 폼 초기화
 */
function initializeLoginForm() {
    initializeTabSwitcher();
    initializeFormValidation();
    initializeEnterKeyLogin();
    initializeNavigationLinks();
    checkUrlParams();
}

/**
 * 탭 전환 기능 초기화
 */
function initializeTabSwitcher() {
    const tabs = document.querySelectorAll('.tab');
    
    tabs.forEach(tab => {
        tab.addEventListener('click', handleTabSwitch);
    });
}

function handleTabSwitch(event) {
    const clickedTab = event.target;
    const allTabs   = document.querySelectorAll('.tab');
    const form      = document.querySelector('.login-form');
    const roleField = document.getElementById('roleField');

    allTabs.forEach(tab => tab.classList.remove('active'));
    clickedTab.classList.add('active');

    const tabRole = clickedTab.getAttribute('data-role');
    if (tabRole === 'admin') {
        handleAdminTab(form, roleField);
    } else {
        handleMemberTab(form, roleField);
    }
}

/**
 * 관리자 탭 처리
 */
function handleAdminTab(form, roleField) {
    roleField.value = 'admin';
    form.setAttribute('action', '/login');
}

/**
 * 회원 탭 처리
 */

function handleMemberTab(form, roleField) {
    roleField.value = 'user';
    form.setAttribute('action', '/login');
}

/**
 * 폼 유효성 검사 초기화
 */
function initializeFormValidation() {
    const form = document.querySelector('.login-form');
    const inputs = form.querySelectorAll('.form-input');
    
    // 폼 제출 이벤트
    form.addEventListener('submit', handleFormSubmit);
    
    // 입력 필드 실시간 검증
    inputs.forEach(input => {
        input.addEventListener('blur', validateInput);
        input.addEventListener('input', clearErrorState);
    });
}

/**
 * 폼 제출 처리
 */
function handleFormSubmit(event) {
    const form = event.target;
    const username = form.username.value.trim();
    const password = form.password.value.trim();
    
    // 클라이언트 측 유효성 검사
    if (!username || !password) {
        event.preventDefault();
        showAlert('아이디와 비밀번호를 모두 입력해주세요.');
        return false;
    }
    
    // 로딩 상태 표시
    showLoadingState();
    
    return true;
}

/**
 * 입력 필드 유효성 검사
 */
function validateInput(event) {
    const input = event.target;
    const value = input.value.trim();
    
    if (value.length === 0) {
        input.classList.add('error');
        return false;
    } else {
        input.classList.remove('error');
        return true;
    }
}

/**
 * 에러 상태 제거
 */
function clearErrorState(event) {
    const input = event.target;
    if (input.value.trim().length > 0) {
        input.classList.remove('error');
    }
}

/**
 * 네비게이션 링크 초기화
 */
function initializeNavigationLinks() {
    const signupLink = document.getElementById('signup-link');
    
    if (signupLink) {
        signupLink.addEventListener('click', handleSignupNavigation);
    }
}

/**
 * 회원가입 페이지 이동 처리
 */
function handleSignupNavigation(event) {
    // 기본 링크 동작 허용하되, 필요시 추가 로직 수행
    console.log('회원가입 페이지로 이동');
    
    // 현재 입력된 데이터 임시 저장 (사용자 편의)
    const username = document.querySelector('input[name="username"]').value;
    if (username) {
        sessionStorage.setItem('tempUsername', username);
    }
}

/**
 * Enter 키로 로그인 기능
 */
function initializeEnterKeyLogin() {
    const inputs = document.querySelectorAll('.form-input');
    
    inputs.forEach(input => {
        input.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                const form = document.querySelector('.login-form');
                const submitBtn = form.querySelector('.login-btn');
                submitBtn.click();
            }
        });
    });
}

/**
 * 로딩 상태 표시
 */
function showLoadingState() {
    const submitBtn = document.querySelector('.login-btn');
    const originalText = submitBtn.textContent;
    
    submitBtn.disabled = true;
    submitBtn.textContent = '로그인 중...';
    
    // 5초 후 원상복구 (타임아웃 방지)
    setTimeout(() => {
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    }, 5000);
}

/**
 * URL 파라미터 확인 (회원가입 완료 등)
 */
function checkUrlParams() {
    const urlParams = new URLSearchParams(window.location.search);
    
    if (urlParams.get('signup') === 'success') {
        showAlert('회원가입이 완료되었습니다. 로그인해주세요.');
        // URL에서 파라미터 제거
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    
    if (urlParams.get('error') === 'true') {
        showAlert('로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.');
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}

/**
 * 소셜 로그인 처리 (네이버)
 */
function initializeSocialLogin() {
    const naverBtn = document.querySelector('.naver-btn');
    
    if (naverBtn) {
        naverBtn.addEventListener('click', handleNaverLogin);
    }
}

/**
 * 네이버 로그인 처리
 */
function handleNaverLogin(event) {
    event.preventDefault();
    
    // 네이버 로그인 API 연동 또는 서버 엔드포인트 호출
    console.log('네이버 로그인 시도');
    
    // 임시: 서버의 네이버 OAuth 엔드포인트로 이동
    window.location.href = '/oauth2/authorization/naver';
}

/**
 * 유틸리티 함수들
 */

// 알림 표시
function showAlert(message) {
    alert(message);
}

// 쿠키에서 값 가져오기
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

// 아이디 저장 기능 (체크박스가 있다면)
function initializeRememberMe() {
    const rememberCheckbox = document.getElementById('remember-me');
    const usernameInput = document.querySelector('input[name="username"]');
    
    if (rememberCheckbox && usernameInput) {
        // 저장된 아이디 불러오기
        const savedUsername = localStorage.getItem('rememberedUsername');
        if (savedUsername) {
            usernameInput.value = savedUsername;
            rememberCheckbox.checked = true;
        }
        
        // 폼 제출 시 아이디 저장
        document.querySelector('.login-form').addEventListener('submit', function() {
            if (rememberCheckbox.checked) {
                localStorage.setItem('rememberedUsername', usernameInput.value);
            } else {
                localStorage.removeItem('rememberedUsername');
            }
        });
    }
}

// 초기화 함수들 실행
document.addEventListener('DOMContentLoaded', function() {
    initializeSocialLogin();
    initializeRememberMe();
});