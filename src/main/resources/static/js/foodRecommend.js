// 결과 팝업 추가 처리
function setupResultPopupInteraction() {
    const resultPopup = document.getElementById('resultPopup');
    
    // 팝업을 클릭하면 닫히도록 설정
    if (resultPopup) {
        resultPopup.addEventListener('click', function() {
            if (!isAnimating) {
                resultPopup.classList.remove('show');
            }
        });
    }
    
    // 페이지 어디든 클릭하면 팝업이 표시되어 있을 때 닫히도록 설정
    document.addEventListener('click', function(e) {
        if (resultPopup && 
            resultPopup.classList.contains('show') && 
            !isAnimating && 
            !e.target.closest('#resultPopup') && 
            !e.target.closest('#current-candy')) {
            resultPopup.classList.remove('show');
        }
    });
}

// 검색바 클릭 이벤트
const searchBar = document.querySelector('.search-bar');
searchBar.addEventListener('click', function() {
    window.location.href = '/search';
});

// 뒤로 가기 버튼 설정
const backButton = document.querySelector('.icon'); // .icon 클래스를 가진 요소를 선택합니다.
backButton.addEventListener('click', function() {
    window.history.back();
});

// 캔디 애니메이션 효과 추가
function animateCandies() {
    const candies = document.querySelectorAll('.candy');
    
    // 각 캔디에 랜덤한 움직임 추가
    candies.forEach((candy, index) => {
        // 랜덤 시작 위치
        const startX = 10 + Math.random() * 60; // 10% ~ 70%
        const startY = 10 + Math.random() * 60; // 10% ~ 70%
        
        candy.style.left = `${startX}%`;
        candy.style.top = `${startY}%`;
        
        // 랜덤 애니메이션 속도
        const duration = 3 + Math.random() * 2; // 3s ~ 5s
        candy.style.animationDuration = `${duration}s`;
        
        // 랜덤 애니메이션 지연
        const delay = Math.random() * 2; // 0s ~ 2s
        candy.style.animationDelay = `${delay}s`;
    });
}

// 게임 이미지 버튼(전체 ▶ 버튼) 이벤트
document.addEventListener('DOMContentLoaded', function() {
    const foodButtonGlobe = document.getElementById('foodButtonGlobe');
    
    // 전체 ▶ 버튼 클릭 이벤트 - 카테고리 선택 또는 다른 기능 추가 가능
    foodButtonGlobe.addEventListener('click', function(e) {
        e.stopPropagation(); // 이벤트 버블링 방지
        
        // 여기에 카테고리 선택 기능이나 다른 기능 추가 가능
        showMessage('전체 카테고리에서 음식을 선택합니다!');
    });
});

// 다시 뽑기 (새로운 애니메이션 사용)
function handleAgainClick() {
    if (isAnimating) return;
    
    addClickEffect(document.getElementById('againBtn'));
    
    // 현재 표시된 결과 팝업이 있으면 제거
    clearExistingAnimations();
    
    // 새로운 음식 선택 및 애니메이션 시작
    currentFood = getRandomFood();
    launchCandyAnimation(currentFood);
}

// 새로 뽑기 (새로운 애니메이션 사용)
function handleNewClick() {
    if (isAnimating) return;
    
    addClickEffect(document.getElementById('newBtn'));
    
    // 현재 표시된 결과 팝업이 있으면 제거
    clearExistingAnimations();
    
    // 새로운 음식 선택 및 애니메이션 시작
    currentFood = getRandomFood();
    launchCandyAnimation(currentFood);
}

// 음식에 맞는 이모지 선택
function getFoodEmoji(foodItem) {
    const specificFoodEmojis = {
        '김치찌개': '🍲',
        '된장찌개': '🥘',
        '비빔밥': '🍚',
        '불고기': '🥓',
        '삼겹살': '🥩',
        '떡볶이': '🌶️',
        '치킨': '🍗',
        '제육볶음': '🥘',
        '냉면': '🍜',
        '김밥': '🍱',
        
        '짜장면': '🍜',
        '짬뽕': '🥣',
        '탕수육': '🍖',
        '마라탕': '🌶️',
        '양꼬치': '🍢',
        
        '초밥': '🍣',
        '라멘': '🍜',
        '우동': '🍲',
        '돈까스': '🍱',
        '규동': '🍚',
        
        '파스타': '🍝',
        '피자': '🍕',
        '스테이크': '🥩',
        '햄버거': '🍔',
        '샐러드': '🥗'
    };
    
    if (specificFoodEmojis[foodItem]) {
        return specificFoodEmojis[foodItem];
    }
    
    const categoryEmojis = {
        '한식': '🍚',
        '중식': '🥡',
        '일식': '🍣',
        '양식': '🍕'
    };
    
    for (const [category, foods] of Object.entries(foodData)) {
        if (foods.includes(foodItem)) {
            return categoryEmojis[category] || '🍽️';
        }
    }
    
    return '🍽️';
}

// 공 날아오는 애니메이션
function launchCandyAnimation(foodItem) {
    clearExistingAnimations();
    
    const gumballBase = document.getElementById('gumballBase');
    const gumballRect = gumballBase.getBoundingClientRect();
    
    const colors = ['#ff9999', '#99ff99', '#9999ff', '#ffff99', '#ff99ff', '#99ffff', '#ffcc99', '#cc99ff'];
    const color = colors[Math.floor(Math.random() * colors.length)];
    
    const candy = document.createElement('div');
    candy.className = 'flying-candy';
    candy.style.backgroundColor = color;
    candy.textContent = '?';
    candy.id = 'current-candy';
    
    candy.style.left = (gumballRect.left + gumballRect.width / 2 - 25) + 'px';
    candy.style.top = (gumballRect.top + gumballRect.height / 2 - 10) + 'px';
    
    document.body.appendChild(candy);
    
    setTimeout(() => {
        candy.style.left = '50%';
        candy.style.top = '50%';
        candy.style.transform = 'translate(-50%, -50%) scale(1.5)';
        
        candy.addEventListener('click', function() {
            if (!candy.classList.contains('clicked')) {
                candy.classList.add('clicked');
                showFoodResult(foodItem, candy);
            }
        });
        
        setTimeout(() => {
            if (!candy.classList.contains('clicked')) {
                candy.classList.add('clicked');
                showFoodResult(foodItem, candy);
            }
        }, 1500);
    }, 100);
}

// 음식 결과 표시
function showFoodResult(foodItem, candy) {
    const resultPopup = document.getElementById('resultPopup');
    const resultFood = document.getElementById('resultFood');
    const resultEmoji = document.querySelector('.result-emoji');
    
    if (resultPopup && resultFood) {
        resultFood.textContent = foodItem;
        
        if (resultEmoji) {
            resultEmoji.textContent = getFoodEmoji(foodItem);
        }
        
        candy.remove();
        resultPopup.classList.add('show');
        
        isAnimating = false;
    }
}

// 기존 애니메이션 요소 제거
function clearExistingAnimations() {
    const existingCandy = document.getElementById('current-candy');
    if (existingCandy) {
        existingCandy.remove();
    }
    
    const resultPopup = document.getElementById('resultPopup');
    if (resultPopup && resultPopup.classList.contains('show')) {
        resultPopup.classList.remove('show');
    }
}

const foodData = {
    '한식': ['김치찌개', '된장찌개', '비빔밥', '불고기', '삼겹살', '떡볶이', '치킨', '제육볶음', '냉면', '김밥'],
    '중식': ['짜장면', '짬뽕', '탕수육', '마라탕', '양꼬치'],
    '일식': ['초밥', '라멘', '우동', '돈까스', '규동'],
    '양식': ['파스타', '피자', '스테이크', '햄버거', '샐러드']
};

const allFoods = Object.values(foodData).flat();
let currentFood = '';
let currentCategory = 'all';
let isAnimating = false;
let isResultView = false;

document.addEventListener('DOMContentLoaded', function() {
    const gumballContainer = document.getElementById('gumballContainer');
    const resultPopup = document.getElementById('resultPopup');
    
    currentFood = getRandomFood();
    gumballContainer.addEventListener('click', function(e) {
        if (!isAnimating) {
            currentFood = getRandomFood();
            const base = document.getElementById('gumballBase');
            if (base) {
                base.classList.add('btn-clicked');
                setTimeout(() => {
                    base.classList.remove('btn-clicked');
                }, 300);
            }
            clearExistingAnimations();
            launchCandyAnimation(currentFood);
            isAnimating = true;
        }
    });

    setupResultPopupInteraction();
    animateCandies();
    setTimeout(() => {
        showMessage('음식을 뽑으려면 기계를 클릭하세요! 🍽️', 3000);
    }, 1000);
});

function getRandomFood(category = currentCategory) {
    const foodList = category === 'all' ? allFoods : foodData[category] || allFoods;
    let newFood;
    
    do {
        newFood = foodList[Math.floor(Math.random() * foodList.length)];
    } while (newFood === currentFood && foodList.length > 1);
    
    return newFood;
}

function updateFoodDisplay(food, animate = false) {
    const foodName = document.getElementById('foodName');
    if (!foodName) return;
    
    if (animate) {
        foodName.classList.add('food-change');
        
        setTimeout(() => {
            foodName.textContent = food;
            currentFood = food;
        }, 300);
        
        setTimeout(() => {
            foodName.classList.remove('food-change');
        }, 600);
    } else {
        foodName.textContent = food;
        currentFood = food;
    }
}

function addClickEffect(button) {
    if (!button) return;
    
    button.classList.add('btn-clicked');
    setTimeout(() => {
        button.classList.remove('btn-clicked');
    }, 300);
}

function showMessage(message, duration = 2000) {
    const existingMessage = document.querySelector('.toast-message');
    if (existingMessage) {
        existingMessage.remove();
    }
    
    const messageEl = document.createElement('div');
    messageEl.className = 'toast-message';
    messageEl.textContent = message;
    messageEl.style.cssText = `
        position: fixed;
        top: 80px;
        left: 50%;
        transform: translateX(-50%);
        background: rgba(139, 69, 19, 0.9);
        color: white;
        padding: 12px 20px;
        border-radius: 20px;
        font-size: 14px;
        font-weight: 500;
        z-index: 2000;
        max-width: 80%;
        text-align: center;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
        animation: slideDown 0.3s ease;
    `;
    
    if (!document.querySelector('#toast-styles')) {
        const style = document.createElement('style');
        style.id = 'toast-styles';
        style.textContent = `
            @keyframes slideDown {
                0% { transform: translateX(-50%) translateY(-20px); opacity: 0; }
                100% { transform: translateX(-50%) translateY(0); opacity: 1; }
            }
            @keyframes slideUp {
                0% { transform: translateX(-50%) translateY(0); opacity: 1; }
                100% { transform: translateX(-50%) translateY(-20px); opacity: 0; }
            }
        `;
        document.head.appendChild(style);
    }
    
    document.body.appendChild(messageEl);
    
    setTimeout(() => {
        messageEl.style.animation = 'slideUp 0.3s ease forwards';
        setTimeout(() => {
            messageEl.remove();
        }, 300);
    }, duration);
}
