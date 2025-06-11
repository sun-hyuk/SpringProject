document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('passwordForm');
  const currentInput = document.getElementById('currentPwd');
  const newInput = document.getElementById('newPwd');
  const confirmInput = document.getElementById('confirmPwd');
  const completeBtn = document.querySelector('.btn-complete');

  // 비밀번호 가시성 토글: HTML에서는 .btn-clear와 .clear-icon을 사용함
  document.querySelectorAll('.btn-clear').forEach(btn => {
    btn.addEventListener('click', () => {
      // 같은 input-group 내부의 input-field를 찾아서 type을 토글
      const input = btn.parentElement.querySelector('.input-field');
      const icon = btn.querySelector('.clear-icon');
      if (input.type === 'password') {
        input.type = 'text';
        icon.src = '/images/myInfo/visibility-icon.png';
      } else {
        input.type = 'password';
        icon.src = '/images/myInfo/visibility-off-icon.png';
      }
      input.focus();
    });
  });

  // “완료” 버튼 클릭 시 폼 검증 후 제출
  if (completeBtn && form && currentInput && newInput && confirmInput) {
    completeBtn.addEventListener('click', () => {
      const currentVal = currentInput.value.trim();
      const newVal = newInput.value.trim();
      const confirmVal = confirmInput.value.trim();

      // (1) 필수 입력 체크
      if (!currentVal) {
        alert('현재 비밀번호를 입력해주세요.');
        currentInput.focus();
        return;
      }
      if (!newVal) {
        alert('새 비밀번호를 입력해주세요.');
        newInput.focus();
        return;
      }
      if (!confirmVal) {
        alert('새 비밀번호 확인을 입력해주세요.');
        confirmInput.focus();
        return;
      }

      // (2) 새 비밀번호 & 확인 일치 여부
      if (newVal !== confirmVal) {
        alert('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
        confirmInput.focus();
        return;
      }

      // (3) 비밀번호 정책(옵션): 길이/복잡도 검사 등
      if (newVal.length < 8) {
        alert('새 비밀번호는 8자 이상이어야 합니다.');
        newInput.focus();
        return;
      }

      // (4) 이상 없으면 폼 제출
      form.submit();
    });
  }
});
