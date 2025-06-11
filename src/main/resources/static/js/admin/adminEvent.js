// 이미지 미리보기 기능
function previewImage(event) {
    const input = event.target;
    const preview = document.getElementById('preview');
    const uploadText = document.getElementById('uploadText');

    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
            uploadText.style.display = 'none';
        };
        reader.readAsDataURL(input.files[0]);
    }
}

// 이미지 삭제 기능
function clearPreview() {
    const preview = document.getElementById('preview');
    const uploadText = document.getElementById('uploadText');
    const imageInput = document.getElementById('imageInput');

    preview.src = '';
    preview.style.display = 'none';
    uploadText.style.display = 'inline';
    imageInput.value = '';  // 파일 선택 초기화
}

// 날짜 유효성 검사
function validateDates() {
    const start = document.querySelector('input[name="startDate"]').value;
    const end = document.querySelector('input[name="endDate"]').value;

    if (start && end && start > end) {
        alert("⚠ 종료 날짜는 시작 날짜보다 이후여야 합니다.");
        return false;
    }

    return true;
}
