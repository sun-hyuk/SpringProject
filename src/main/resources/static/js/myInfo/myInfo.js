document.addEventListener('DOMContentLoaded', () => {
  const input = document.getElementById('profileInput');
  const img   = document.getElementById('profileImage');

  if (!input || !img) {
    return;
  }

  input.addEventListener('change', () => {
    const file = input.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = e => {
      img.src = e.target.result;  // 선택한 이미지로 교체
    };
    reader.readAsDataURL(file);
  });
});
