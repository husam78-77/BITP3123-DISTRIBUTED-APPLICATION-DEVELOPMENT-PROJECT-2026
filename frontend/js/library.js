document.addEventListener('DOMContentLoaded', () => {
    loadBooks();
    loadLoans();
    loadRooms();
    
    document.getElementById('book-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const book = {
            title: document.getElementById('bookTitle').value,
            author: document.getElementById('bookAuthor').value,
            isbn: document.getElementById('bookIsbn').value,
            available: true
        };
        
        toggleButton('bookSubmitBtn', true);
        
        try {
            const response = await fetch(`${API.library}/books`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(book)
            });
            
            if (response.ok) {
                showToast('Book added successfully');
                document.getElementById('book-form').reset();
                loadBooks();
            } else {
                showToast('Failed to add book', 'error');
            }
        } catch (error) {
            showToast('Error connecting to server', 'error');
        } finally {
            toggleButton('bookSubmitBtn', false);
        }
    });

    document.getElementById('borrow-form').addEventListener('submit', async (e) => {
        e.preventDefault();

        const studentId = document.getElementById('loanStudentId').value;
        const bookId = document.getElementById('loanBookId').value;

        toggleButton('borrowSubmitBtn', true);

        try {
            const response = await fetch(
                `${API.library}/books/borrow?studentId=${studentId}&bookId=${bookId}`,
                { method: 'POST' }
            );

            if (response.ok) {
                showToast('Book borrowed successfully');
                document.getElementById('borrow-form').reset();
                loadBooks();
                loadLoans();
            } else {
                const err = await response.text();
                showToast('Failed to borrow book: ' + (err.substring(0, 80) || response.statusText), 'error');
            }
        } catch (error) {
            showToast('Error connecting to server', 'error');
        } finally {
            toggleButton('borrowSubmitBtn', false);
        }
    });

    document.getElementById('return-form').addEventListener('submit', async (e) => {
        e.preventDefault();

        const loanId = document.getElementById('returnLoanId').value;

        toggleButton('returnSubmitBtn', true);

        try {
            const response = await fetch(`${API.library}/books/return/${loanId}`, { method: 'PUT' });

            if (response.ok) {
                showToast('Book returned successfully');
                document.getElementById('return-form').reset();
                loadBooks();
                loadLoans();
            } else {
                const err = await response.text();
                showToast('Failed to return book: ' + (err.substring(0, 80) || response.statusText), 'error');
            }
        } catch (error) {
            showToast('Error connecting to server', 'error');
        } finally {
            toggleButton('returnSubmitBtn', false);
        }
    });

    document.getElementById('room-form').addEventListener('submit', async (e) => {
        e.preventDefault();

        const studentId = document.getElementById('roomStudentId').value;
        const roomId = document.getElementById('roomId').value;
        const startTime = document.getElementById('roomStartTime').value.replace('T', 'T').replace('T', 'T');
        const endTime = document.getElementById('roomEndTime').value;

        // datetime-local gives "2026-06-29T10:00" — backend needs seconds too
        const formatDt = (dt) => dt.length === 16 ? dt + ':00' : dt;

        toggleButton('roomSubmitBtn', true);

        try {
            const response = await fetch(
                `${API.library}/rooms/book?studentId=${studentId}&roomId=${encodeURIComponent(roomId)}&startTime=${encodeURIComponent(formatDt(startTime))}&endTime=${encodeURIComponent(formatDt(endTime))}`,
                { method: 'POST' }
            );

            if (response.ok) {
                showToast('Room booked successfully');
                document.getElementById('room-form').reset();
                loadRooms();
            } else {
                const err = await response.text();
                showToast('Failed to book room: ' + (err.substring(0, 80) || response.statusText), 'error');
            }
        } catch (error) {
            showToast('Error connecting to server', 'error');
        } finally {
            toggleButton('roomSubmitBtn', false);
        }
    });
});

async function loadBooks() {
    showLoading('bookLoader');
    const tbody = document.getElementById('bookTableBody');
    tbody.innerHTML = '';
    
    try {
        const response = await fetch(`${API.library}/books`);
        if (response.ok) {
            const books = await response.json();
            books.forEach(book => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${book.id || '-'}</td>
                    <td>${book.title}</td>
                    <td>${book.author}</td>
                    <td>${book.isbn}</td>
                    <td>${book.available ? 'Yes' : 'No'}</td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (error) {
        showToast('Error loading books', 'error');
    } finally {
        hideLoading('bookLoader');
    }
}

async function loadLoans() {
    showLoading('loanLoader');
    const tbody = document.getElementById('loanTableBody');
    tbody.innerHTML = '';

    try {
        const response = await fetch(`${API.library}/books/loans`);
        if (response.ok) {
            const loans = await response.json();
            if (loans.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No loans found</td></tr>';
                return;
            }
            loans.forEach(loan => {
                const tr = document.createElement('tr');
                const isActive = loan.status === 'ACTIVE';
                tr.innerHTML = `
                    <td>${loan.id || '-'}</td>
                    <td>${loan.studentId || '-'}</td>
                    <td>${loan.bookId || '-'}</td>
                    <td>${loan.borrowDate || '-'}</td>
                    <td>${loan.dueDate || '-'}</td>
                    <td>${loan.returnDate || '-'}</td>
                    <td>${loan.status || '-'}</td>
                    <td>${isActive ? `<button class="btn-sm btn-danger" onclick="returnBook(${loan.id})">Return</button>` : '-'}</td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (error) {
        showToast('Error loading loans', 'error');
    } finally {
        hideLoading('loanLoader');
    }
}

async function returnBook(loanId) {
    if (!confirm('Return this book?')) return;
    try {
        const response = await fetch(`${API.library}/books/return/${loanId}`, { method: 'PUT' });
        if (response.ok) {
            showToast('Book returned successfully');
            loadBooks();
            loadLoans();
        } else {
            const err = await response.text();
            showToast('Failed to return: ' + (err.substring(0, 80) || response.statusText), 'error');
        }
    } catch (error) {
        showToast('Error returning book', 'error');
    }
}

async function loadRooms() {
    showLoading('roomLoader');
    const tbody = document.getElementById('roomTableBody');
    tbody.innerHTML = '';
    
    try {
        const response = await fetch(`${API.library}/rooms`);
        if (response.ok) {
            const rooms = await response.json();
            rooms.forEach(room => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${room.id || '-'}</td>
                    <td>${room.roomId || '-'}</td>
                    <td>${room.studentId || '-'}</td>
                    <td>${room.startTime ? room.startTime.replace('T', ' ') : '-'}</td>
                    <td>${room.endTime ? room.endTime.replace('T', ' ') : '-'}</td>
                    <td>${room.status || '-'}</td>
                    <td>
                        <button class="btn-sm btn-danger" onclick="cancelRoom(${room.id})">Cancel</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (error) {
        // Handle gracefully if endpoint is slightly different
    } finally {
        hideLoading('roomLoader');
    }
}

async function cancelRoom(id) {
    if (!confirm('Cancel this room booking?')) return;
    try {
        const response = await fetch(`${API.library}/rooms/cancel/${id}`, { method: 'PUT' });
        if (response.ok) {
            showToast('Booking cancelled');
            loadRooms();
            loadLoans();
        } else {
            showToast('Failed to cancel', 'error');
        }
    } catch (error) {
        showToast('Error cancelling', 'error');
    }
}
