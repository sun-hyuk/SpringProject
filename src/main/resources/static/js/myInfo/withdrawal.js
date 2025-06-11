document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('withdrawForm');
  const agreeCheckbox = document.getElementById('agree');
  const completeBtn = document.querySelector('.btn-complete');

  if (completeBtn && form && agreeCheckbox) {
    completeBtn.addEventListener('click', () => {
      // 체크박스가 체크되어 있지 않으면 submit 막고 알림
      if (!agreeCheckbox.checked) {
        alert('회원 탈퇴 유의사항에 동의하셔야 탈퇴가 진행됩니다.');
        agreeCheckbox.focus();
        return;
      }
      // 체크된 상태면 폼을 submit => POST /myInfo/withdrawal
      form.submit();
    });
  }
});
