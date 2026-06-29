document.addEventListener('DOMContentLoaded', () => {
    loadCourses();
    loadEnrolments();
    
    document.getElementById('course-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const course = {
            courseCode: document.getElementById('courseCode').value,
            courseName: document.getElementById('courseName').value,
            programme: document.getElementById('programme').value,
            capacity: parseInt(document.getElementById('capacity').value)
        };
        
        toggleButton('courseSubmitBtn', true);
        
        try {
            const response = await fetch(`${API.course}/courses`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(course)
            });
            
            if (response.ok) {
                showToast('Course created successfully');
                document.getElementById('course-form').reset();
                loadCourses();
            } else {
                showToast('Failed to create course', 'error');
            }
        } catch (error) {
            showToast('Error connecting to server', 'error');
        } finally {
            toggleButton('courseSubmitBtn', false);
        }
    });

    document.getElementById('enrol-form').addEventListener('submit', async (e) => {
        e.preventDefault();

        const studentId = document.getElementById('enrolStudentId').value;
        const courseId = document.getElementById('enrolCourseId').value;
        const semester = document.getElementById('enrolSemester').value;

        toggleButton('enrolSubmitBtn', true);

        try {
            const response = await fetch(
                `${API.course}/enrolments/enrol?studentId=${studentId}&courseId=${courseId}&semester=${encodeURIComponent(semester)}`,
                { method: 'POST' }
            );

            if (response.ok) {
                showToast('Student enrolled successfully');
                document.getElementById('enrol-form').reset();
                loadEnrolments();
            } else {
                const err = await response.text();
                showToast('Failed to enrol: ' + (err.substring(0, 80) || response.statusText), 'error');
            }
        } catch (error) {
            showToast('Error connecting to server', 'error');
        } finally {
            toggleButton('enrolSubmitBtn', false);
        }
    });
});

async function loadCourses() {
    showLoading('courseLoader');
    const tbody = document.getElementById('courseTableBody');
    tbody.innerHTML = '';
    
    try {
        const response = await fetch(`${API.course}/courses`);
        if (response.ok) {
            const courses = await response.json();
            courses.forEach(course => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${course.id || '-'}</td>
                    <td>${course.courseCode}</td>
                    <td>${course.courseName}</td>
                    <td>${course.programme || '-'}</td>
                    <td>${course.capacity || '-'}</td>
                    <td>${course.currentEnrolled || 0}</td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (error) {
        showToast('Error loading courses', 'error');
    } finally {
        hideLoading('courseLoader');
    }
}

async function loadEnrolments() {
    showLoading('enrolLoader');
    const tbody = document.getElementById('enrolTableBody');
    tbody.innerHTML = '';
    
    try {
        const response = await fetch(`${API.course}/enrolments`);
        if (response.ok) {
            const enrolments = await response.json();
            enrolments.forEach(enrol => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${enrol.id || '-'}</td>
                    <td>${enrol.studentId}</td>
                    <td>${enrol.courseId}</td>
                    <td>${enrol.semester || '-'}</td>
                    <td>${enrol.status || '-'}</td>
                    <td>
                        <button class="btn-sm btn-danger" onclick="dropCourse(${enrol.studentId}, ${enrol.courseId}, '${enrol.semester}')">Drop</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (error) {
        showToast('Error loading enrolments', 'error');
    } finally {
        hideLoading('enrolLoader');
    }
}

async function dropCourse(studentId, courseId, semester) {
    if (!confirm('Are you sure you want to drop this course?')) return;

    try {
        const response = await fetch(
            `${API.course}/enrolments/drop?studentId=${studentId}&courseId=${courseId}&semester=${encodeURIComponent(semester)}`,
            { method: 'PUT' }
        );

        if (response.ok) {
            showToast('Course dropped successfully');
            loadEnrolments();
        } else {
            showToast('Failed to drop course', 'error');
        }
    } catch (error) {
        showToast('Error dropping course', 'error');
    }
}
