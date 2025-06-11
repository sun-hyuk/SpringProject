/**
 * EatoMeter 회원가입 페이지 JavaScript
 * 파일 경로: src/main/resources/static/js/signup.js
 */

// DOM 로드 완료 후 실행
document.addEventListener('DOMContentLoaded', function() {
	initializeSignupForm();
});

/**
 * 회원가입 폼 초기화
 */
function initializeSignupForm() {
	initializeDuplicateCheck();
	initializePasswordValidation();
	initializePhoneFormatter();
	initializeFormValidation();
	initializeNavigationLinks();
	loadTempData();
}

/**
 * 네비게이션 링크 초기화
 */
function initializeNavigationLinks() {
	const loginLink = document.getElementById('login-link');

	if (loginLink) {
		loginLink.addEventListener('click', handleLoginNavigation);
	}
}

/**
 * 로그인 페이지 이동 처리
 */
function handleLoginNavigation(event) {
	console.log('로그인 페이지로 이동');
	sessionStorage.removeItem('tempUsername');
}

/**
 * 로그인에서 넘어온 임시 데이터 로드
 */
function loadTempData() {
	const tempUsername = sessionStorage.getItem('tempUsername');
	if (tempUsername) {
		const usernameInput = document.getElementById('username');
		if (usernameInput) {
			usernameInput.value = tempUsername;
		}
		sessionStorage.removeItem('tempUsername');
	}
}

/**
 * 중복확인 기능 초기화
 */
function initializeDuplicateCheck() {
	const checkButtons = document.querySelectorAll('.check-btn');
	checkButtons.forEach(btn => {
		btn.addEventListener('click', handleDuplicateCheck);
	});
}

/**
 * 중복확인 처리 (개선판)
 */
async function handleDuplicateCheck(event) {
	const button = event.currentTarget;
	const wrapper = button.closest('.input-with-button');
	if (!wrapper) {
		console.error('중복확인: .input-with-button 래퍼를 찾을 수 없습니다.');
		return;
	}

	const input = wrapper.querySelector('input');
	if (!input) {
		console.error('중복확인: wrapper 안에 input 태그가 없습니다.');
		return;
	}

	const value = input.value.trim();
	const type = input.id;  // 예: id="nickname" 또는 id="phone"

	// 값이 비어 있으면 focus하고 종료
	if (!value) {
		showAlert('값을 입력해주세요.');
		input.focus();
		return;
	}

	// 전화번호 형식 검증 (nickname은 별도 검증 없음)
	if (type === 'phone' && !isValidPhoneNumber(value)) {
		showAlert('올바른 전화번호 형식(01012345678)을 입력하세요.');
		input.focus();
		return;
	}

	// 버튼 비활성화 및 텍스트 변경
	button.disabled = true;
	button.textContent = '확인중...';

	try {
		const isDuplicate = await checkDuplicateAPI(type, value);

		if (isDuplicate) {
			showAlert(`이미 사용 중인 ${getFieldName(type)}입니다.`);
			input.classList.add('error');
			input.classList.remove('success');
		} else {
			showAlert(`사용 가능한 ${getFieldName(type)}입니다.`);
			input.classList.add('success');
			input.classList.remove('error');
		}
	} catch (error) {
		console.error('중복확인 오류:', error);
		showAlert('중복확인 중 오류가 발생했습니다. 다시 시도해주세요.');
	} finally {
		button.disabled = false;
		button.textContent = '중복확인';
	}
}

/**
 * 서버 중복확인 API 호출
 */
async function checkDuplicateAPI(type, value) {
	const response = await fetch('/check-duplicate', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded',
		},
		body: `type=${encodeURIComponent(type)}&value=${encodeURIComponent(value)}`
	});

	if (!response.ok) {
		throw new Error('서버 응답 오류');
	}

	const result = await response.text();
	return result === 'duplicate';
}

/**
 * 비밀번호 검증 초기화 (개선판)
 */
function initializePasswordValidation() {
	const passwordInput = document.getElementById('password');
	const passwordConfirmInput = document.getElementById('passwordConfirm');

	// 비밀번호 입력 시 유효성 색깔만 토글
	passwordInput.addEventListener('input', () => {
		const pwd = passwordInput.value;
		if (pwd.length === 0) {
			passwordInput.classList.remove('error', 'success');
		} else if (isValidPassword(pwd)) {
			passwordInput.classList.add('success');
			passwordInput.classList.remove('error');
		} else {
			passwordInput.classList.add('error');
			passwordInput.classList.remove('success');
		}
	});

	// 비밀번호 확인란 blur 시 최종 경고 및 색깔 토글
	passwordConfirmInput.addEventListener('blur', () => {
		const pwd = passwordInput.value;
		const confirm = passwordConfirmInput.value;
		if (confirm.length === 0) {
			passwordConfirmInput.classList.remove('error', 'success');
			return;
		}
		if (pwd === confirm) {
			passwordConfirmInput.classList.add('success');
			passwordConfirmInput.classList.remove('error');
		} else {
			passwordConfirmInput.classList.add('error');
			passwordConfirmInput.classList.remove('success');
			showAlert('비밀번호가 일치하지 않습니다.');
		}
	});

	// 입력 중에는 단순 색깔 토글
	passwordConfirmInput.addEventListener('input', () => {
		const pwd = passwordInput.value;
		const confirm = passwordConfirmInput.value;
		if (confirm.length === 0) {
			passwordConfirmInput.classList.remove('error', 'success');
			return;
		}
		if (pwd === confirm) {
			passwordConfirmInput.classList.add('success');
			passwordConfirmInput.classList.remove('error');
		} else {
			passwordConfirmInput.classList.add('error');
			passwordConfirmInput.classList.remove('success');
		}
	});
}

/**
 * 전화번호 포맷터 초기화
 */
function initializePhoneFormatter() {
	const phoneInput = document.getElementById('phone');
	phoneInput.addEventListener('input', formatPhoneNumber);
}

/**
 * 전화번호 자동 포맷팅
 */
function formatPhoneNumber(event) {
	const input = event.target;
	let value = input.value.replace(/[^0-9]/g, '');

	// 최대 11자리로 제한
	if (value.length > 11) {
		value = value.substring(0, 11);
	}

	input.value = value;

	// 전화번호 유효성 검사
	if (value.length === 0) {
		input.classList.remove('error', 'success');
	} else if (isValidPhoneNumber(value)) {
		input.classList.add('success');
		input.classList.remove('error');
	} else {
		input.classList.add('error');
		input.classList.remove('success');
	}
}

/**
 * 폼 전체 유효성 검사 초기화
 */
function initializeFormValidation() {
	const form = document.querySelector('.signup-form');
	form.addEventListener('submit', handleFormSubmit);
}

/**
 * 폼 제출 처리
 */
function handleFormSubmit(event) {
	event.preventDefault();

	if (validateAllFields()) {
		// 모든 필드가 유효하면 폼 제출
		event.target.submit();
	} else {
		showAlert('모든 필드를 올바르게 입력해주세요.');
	}
}

/**
 * 모든 필드 유효성 검사
 */
function validateAllFields() {
	const username = document.getElementById('username').value.trim();
	const password = document.getElementById('password').value;
	const passwordConfirm = document.getElementById('passwordConfirm').value;
	const name = document.getElementById('name').value.trim();
	const nickname = document.getElementById('nickname').value.trim();
	const phone = document.getElementById('phone').value.trim();

	// 필수 필드 검사
	if (!username || !password || !passwordConfirm || !name || !nickname || !phone) {
		return false;
	}

	// 비밀번호 형식 검사
	if (!isValidPassword(password)) return false;
	// 비밀번호 확인 일치 여부
	if (password !== passwordConfirm) return false;
	// 전화번호 형식 검사
	if (!isValidPhoneNumber(phone)) return false;

	return true;
}

/**
 * 유틸리티 함수들
 */

// 비밀번호 유효성 검사 (8자 이상, 영문+숫자 포함)
function isValidPassword(password) {
	const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/;
	return passwordRegex.test(password);
}

// 전화번호 유효성 검사 (10-11자리 숫자, 앞자리가 01로 시작)
function isValidPhoneNumber(phone) {
	const phoneRegex = /^01[0-9]\d{7,8}$/;
	return phoneRegex.test(phone);
}

// 필드명 한글 변환
function getFieldName(fieldType) {
	const fieldNames = {
		'nickname': '닉네임',
		'phone': '전화번호'
	};
	return fieldNames[fieldType] || fieldType;
}

// 알림 표시 (추후 모달로 변경 가능)
function showAlert(message) {
	alert(message);
}

/**
 * 추가 기능들
 */

// 입력 필드 실시간 유효성 검사
document.addEventListener('DOMContentLoaded', function() {
	const inputs = document.querySelectorAll('.form-input');

	inputs.forEach(input => {
		input.addEventListener('blur', function() {
			validateSingleField(this);
		});
	});
});

// 개별 필드 유효성 검사
function validateSingleField(input) {
	const value = input.value.trim();
	const fieldName = input.name;

	if (value.length === 0) {
		input.classList.remove('error', 'success');
		return;
	}

	let isValid = true;

	switch (fieldName) {
		case 'password':
			isValid = isValidPassword(value);
			break;
		case 'phone':
			isValid = isValidPhoneNumber(value);
			break;
		case 'name':
		case 'nickname':
			isValid = value.length >= 2;
			break;
		case 'username':
			// “아이디” 필드는 이메일이 아니므로, 최소 길이(예: 4자)만 체크
			isValid = value.length >= 4;
			break;
		default:
			isValid = true;
	}

	if (isValid) {
		input.classList.add('success');
		input.classList.remove('error');
	} else {
		input.classList.add('error');
		input.classList.remove('success');
	}
}
