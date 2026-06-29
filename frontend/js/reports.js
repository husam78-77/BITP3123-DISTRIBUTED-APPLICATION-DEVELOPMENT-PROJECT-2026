async function fetchReport(type) {
    const btnId = `btn-${type}`;
    toggleButton(btnId, true);
    showLoading('reportLoader');
    
    const outputElement = document.getElementById('reportOutput');
    outputElement.textContent = 'Loading...';
    
    try {
        const endpointMap = { student: 'students', enrolment: 'enrolments', library: 'library', campus: 'overview' };
        let endpoint = `${API.reporting}/${endpointMap[type] || type}`;
        
        const response = await fetch(endpoint);
        
        if (response.ok) {
            const data = await response.json();
            outputElement.textContent = JSON.stringify(data, null, 4);
            showToast(`${type.charAt(0).toUpperCase() + type.slice(1)} report generated`);
        } else {
            // Handle fallback scenarios or API differences
            const text = await response.text();
            outputElement.textContent = `Error Status: ${response.status}\n\n${text}`;
            showToast('Failed to generate report', 'error');
        }
    } catch (error) {
        outputElement.textContent = `Connection Error: Failed to fetch report.\nEnsure the Reporting & Analytics Service is running on port 8085.`;
        showToast('Error connecting to server', 'error');
    } finally {
        hideLoading('reportLoader');
        toggleButton(btnId, false);
    }
}
