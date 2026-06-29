document.addEventListener('DOMContentLoaded', () => {
    loadStudents();
    
    document.getElementById('student-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const id = document.getElementById('studentId').value;
        const student = {
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            email: document.getElementById('email').value,
            matricNumber: document.getElementById('matricNumber').value,
            programme: document.getElementById('programme').value,
            currentYear: parseInt(document.getElementById('currentYear').value)
        };
        
        toggleButton('submitBtn', true);
        
        try {
            const url = id ? `${API.student}/${id}` : API.student;
            const method = id ? 'PUT' : 'POST';
            
            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(student)
            });
            
            if (response.ok) {
                showToast(id ? 'Student updated successfully' : 'Student created successfully');
                document.getElementById('student-form').reset();
                resetForm();
                loadStudents();
            } else {
                const err = await response.text();
                showToast('Failed to save: ' + (err.substring(0, 50) || response.statusText), 'error');
            }
        } catch (error) {
            showToast('Error connecting to server', 'error');
        } finally {
            toggleButton('submitBtn', false);
        }
    });
    
    document.getElementById('cancelBtn').addEventListener('click', resetForm);
});

async function loadStudents() {
    showLoading('studentLoader');
    const tbody = document.getElementById('studentTableBody');
    tbody.innerHTML = '';
    
    try {
        const response = await fetch(API.student);
        if (response.ok) {
            const students = await response.json();
            students.forEach(student => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${student.id || '-'}</td>
                    <td>${student.firstName} ${student.lastName}</td>
                    <td>${student.matricNumber}</td>
                    <td>${student.programme}</td>
                    <td>${student.currentYear}</td>
                    <td>
                        <button class="btn-sm" onclick='editStudent(${JSON.stringify(student).replace(/'/g, "\\'")})'>Edit</button>
                        <button class="btn-sm btn-danger" onclick="deleteStudent(${student.id})">Delete</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (error) {
        showToast('Error loading students', 'error');
    } finally {
        hideLoading('studentLoader');
    }
}

function editStudent(student) {
    document.getElementById('studentId').value = student.id;
    document.getElementById('firstName').value = student.firstName;
    document.getElementById('lastName').value = student.lastName;
    document.getElementById('email').value = student.email;
    document.getElementById('matricNumber').value = student.matricNumber;
    document.getElementById('programme').value = student.programme;
    document.getElementById('currentYear').value = student.currentYear;
    
    document.getElementById('submitBtn').textContent = 'Update Student';
    document.getElementById('cancelBtn').style.display = 'inline-block';
}

function resetForm() {
    document.getElementById('student-form').reset();
    document.getElementById('studentId').value = '';
    document.getElementById('submitBtn').textContent = 'Save Student';
    document.getElementById('cancelBtn').style.display = 'none';
}

async function deleteStudent(id) {
    if (!confirm('Are you sure you want to delete this student?')) return;
    
    try {
        const response = await fetch(`${API.student}/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showToast('Student deleted successfully');
            loadStudents();
        } else {
            showToast('Failed to delete student', 'error');
        }
    } catch (error) {
        showToast('Error deleting student', 'error');
    }
}
