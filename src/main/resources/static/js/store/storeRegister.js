document.addEventListener("DOMContentLoaded", function () {
    const body = document.body;
    const memberId = body.dataset.memberId;

    console.log("현재 로그인한 사용자 ID:", memberId);

    // 예: 로그인 상태 체크
    if (memberId) {
      // 로그인 된 사용자
    } else {
      // 비로그인 상태 (예외 처리)
    }
	
	// 이벤트 바인딩은 DOM 로드 이후에 해야 안전함!
    const mainImageInput = document.getElementById("mainImage");
    if (mainImageInput) {
      mainImageInput.addEventListener("change", previewImage);
    }
});

// 미리보기 함수는 전역에 선언해도 됨 (DOMContentLoaded 바깥에!)
function previewImage(event) {
  const file = event.target.files[0];
  const preview = document.getElementById("preview");

  if (file) {
    const reader = new FileReader();
    reader.onload = function (e) {
      preview.src = e.target.result;
      preview.style.display = "block";
    };
    reader.readAsDataURL(file);
  } else {
    preview.style.display = "none";
    preview.src = "";
  }
}
