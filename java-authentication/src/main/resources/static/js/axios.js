// auth.js (Axios JWT 설정)

// 요청 헤더에서 토큰을 얻는 함수 (localStorage에서 Authorization 헤더 형식에 맞춰 추출)
function resolveToken() {
    const bearerToken = localStorage.getItem('accessToken');
    if (bearerToken) {
        return 'Bearer ' + bearerToken;
    }
    return null;
}

// Axios 기본 헤더에 토큰 설정
const token = resolveToken();
if (token) {
    axios.defaults.headers.common['Authorization'] = token;
} else {
    console.warn('JWT 토큰이 없습니다. 로그인이 필요합니다.');
}

// JWT 만료(401 에러) 자동 처리 설정 (옵션 - 권장)
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.clear();
            window.location.href = '/login.html';
        }
        return Promise.reject(error);
    }
);
