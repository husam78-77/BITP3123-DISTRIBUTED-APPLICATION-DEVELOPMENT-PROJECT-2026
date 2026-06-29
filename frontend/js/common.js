const API = {
    student: "http://localhost:8081/api/students",
    course: "http://localhost:8082/api",
    library: "http://localhost:8084/api",
    notification: "http://localhost:8083/api/notifications",
    reporting: "http://localhost:8085/api/reports",
    soap: "http://localhost:8084/ws"
};

function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function showLoading(elementId) {
    const el = document.getElementById(elementId);
    if (el) el.style.display = 'block';
}

function hideLoading(elementId) {
    const el = document.getElementById(elementId);
    if (el) el.style.display = 'none';
}

function toggleButton(buttonId, disable) {
    const btn = document.getElementById(buttonId);
    if (btn) {
        btn.disabled = disable;
        btn.style.opacity = disable ? '0.7' : '1';
    }
}
