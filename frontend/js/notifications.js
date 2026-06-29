document.addEventListener('DOMContentLoaded', () => {
    loadNotifications();
    
    // Auto refresh every 10 seconds
    setInterval(loadNotificationsAuto, 10000);
});

async function loadNotifications() {
    toggleButton('refreshBtn', true);
    await loadNotificationsAuto();
    toggleButton('refreshBtn', false);
}

async function loadNotificationsAuto() {
    showLoading('notifLoader');
    const tbody = document.getElementById('notificationTableBody');
    
    try {
        const response = await fetch(API.notification);
        if (response.ok) {
            const notifications = await response.json();
            
            // Sort by newest first assuming there's an id or timestamp
            notifications.sort((a, b) => (b.id || 0) - (a.id || 0));
            
            tbody.innerHTML = '';
            
            if (notifications.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No notifications found</td></tr>';
                return;
            }

            notifications.forEach(notif => {
                const tr = document.createElement('tr');
                
                // Color code the status
                let statusColor = 'inherit';
                if (notif.status === 'SUCCESS' || notif.status === 'DELIVERED') statusColor = 'var(--success-color)';
                if (notif.status === 'ERROR' || notif.status === 'FAILED') statusColor = 'var(--error-color)';

                const dateStr = notif.timestamp ? new Date(notif.timestamp).toLocaleString() : new Date().toLocaleString();

                tr.innerHTML = `
                    <td>${notif.id || '-'}</td>
                    <td>${dateStr}</td>
                    <td><strong>${notif.type || 'SYSTEM'}</strong></td>
                    <td>${notif.message}</td>
                    <td style="color: ${statusColor}; font-weight: bold;">${notif.status || 'PROCESSED'}</td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (error) {
        console.error('Failed to load notifications', error);
    } finally {
        hideLoading('notifLoader');
    }
}
