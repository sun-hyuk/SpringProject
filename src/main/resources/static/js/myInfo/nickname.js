document.addEventListener('DOMContentLoaded', () => {
  const clearBtn = document.querySelector('.btn-clear');
  const nickInput = document.getElementById('nickname');
  const form = document.getElementById('nicknameForm');
  const completeBtn = document.querySelector('.btn-complete');

  // 입력값 삭제 + 포커스
  if (clearBtn && nickInput) {
    clearBtn.addEventListener('click', () => {
      nickInput.value = '';
      nickInput.focus();
    });
  }

  // 헤더의 “완료” 버튼 클릭 시 폼 제출
  if (completeBtn && form && nickInput) {
    completeBtn.addEventListener('click', () => {
      // 입력값이 빈 문자열이면, 폼 제출하지 않고 포커스만 줌
      if (nickInput.value.trim() === '') {
        nickInput.focus();
        return;
      }
      // 값이 있으면 폼을 실제로 submit
      form.submit();
    });
  }
});
