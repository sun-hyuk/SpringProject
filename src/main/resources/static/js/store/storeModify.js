document.getElementById('mainImage').addEventListener('change', function (e) {
  const file = e.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = function (event) {
      const previewImg = document.getElementById('preview');
      if (previewImg) {
        previewImg.src = event.target.result;
		previewImg.style.display = 'block';
      }
    };
    reader.readAsDataURL(file);
  }
});