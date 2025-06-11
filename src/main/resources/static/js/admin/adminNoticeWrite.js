// 파일명: adminNoticeWrite.js
document.addEventListener('DOMContentLoaded', function() {
    // 1) 파일 첨부 버튼 및 미리보기 기능
    const fileInput    = document.getElementById('notice-file');
    const previewImg   = document.getElementById('preview');
    const uploadText   = document.getElementById('uploadText');
    
    if (fileInput) {
        fileInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (file) {
                // 이미지 파일이라면 미리보기
                const reader = new FileReader();
                reader.onload = function(e) {
                    previewImg.src = e.target.result;
                    previewImg.style.display = 'block';
                    uploadText.style.display = 'none';
                };
                reader.readAsDataURL(file);
            } else {
                clearPreview();
            }
        });
    }

    window.previewFile = function(event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                previewImg.src = e.target.result;
                previewImg.style.display = 'block';
                uploadText.style.display = 'none';
            };
            reader.readAsDataURL(file);
        } else {
            clearPreview();
        }
    };

    window.clearPreview = function() {
        if (fileInput) fileInput.value = '';
        if (previewImg) previewImg.style.display = 'none';
        if (uploadText) uploadText.style.display = 'block';
    };

    // 2) 폼 유효성 검사
    const noticeForm = document.querySelector('.event-form');
    if (noticeForm) {
        noticeForm.addEventListener('submit', function(e) {
            const title   = document.getElementById('notice-title').value.trim();
            const type    = document.getElementById('notice-type').value.trim();
            const content = document.getElementById('notice-content').value.trim();

            if (!title) {
                e.preventDefault();
                alert('제목을 입력해주세요.');
                return;
            }
            if (!type) {
                e.preventDefault();
                alert('공지 유형을 선택해주세요.');
                return;
            }
            if (!content) {
                e.preventDefault();
                alert('내용을 입력해주세요.');
                return;
            }
        });
    }
});
