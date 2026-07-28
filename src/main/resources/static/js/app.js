// Base API URL configuration
const API_BASE = '/api';

// State management
let state = {
    students: [],
    courses: [],
    enrollments: [],
    grades: [],
    attendance: []
};

// DOM Load Event
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    loadDashboardStats();
    loadAllData();
    setupEventListeners();
});

// 1. Navigation Control
function initNavigation() {
    const menuLinks = document.querySelectorAll('.menu-item a');
    const tabs = document.querySelectorAll('.tab-content');

    menuLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            
            // Remove active classes
            document.querySelectorAll('.menu-item').forEach(item => item.classList.remove('active'));
            tabs.forEach(tab => tab.classList.remove('active'));

            // Add active class to clicked link parent
            link.parentElement.classList.add('active');

            // Show corresponding tab
            const tabId = link.getAttribute('href').substring(1);
            const targetTab = document.getElementById(tabId);
            if (targetTab) {
                targetTab.classList.add('active');
            }

            // Refresh specific tab data if necessary
            if (tabId === 'dashboard') {
                loadDashboardStats();
            }
        });
    });
}

// 2. Fetch and Load Functions
async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_BASE}/dashboard/stats`);
        if (!response.ok) throw new Error('Failed to load stats');
        const stats = await response.json();
        
        document.getElementById('total-students-kpi').textContent = stats.totalStudents;
        document.getElementById('total-courses-kpi').textContent = stats.totalCourses;
        document.getElementById('avg-gpa-kpi').textContent = stats.averageGpa.toFixed(2);
        document.getElementById('attendance-kpi').textContent = stats.attendanceRate.toFixed(1) + '%';
    } catch (error) {
        console.error('Error fetching dashboard stats:', error);
        showToast('Failed to load dashboard metrics', 'error');
    }
}

async function loadAllData() {
    await Promise.all([
        fetchStudents(),
        fetchCourses(),
        fetchEnrollments(),
        fetchGrades(),
        fetchAttendance()
    ]);
    
    // Update selectors in forms
    updateSelectors();
}

async function fetchStudents() {
    try {
        const response = await fetch(`${API_BASE}/students`);
        state.students = await response.json();
        renderStudentsTable();
    } catch (error) {
        console.error('Error fetching students:', error);
    }
}

async function fetchCourses() {
    try {
        const response = await fetch(`${API_BASE}/courses`);
        state.courses = await response.json();
        renderCoursesTable();
    } catch (error) {
        console.error('Error fetching courses:', error);
    }
}

async function fetchEnrollments() {
    try {
        const response = await fetch(`${API_BASE}/enrollments`);
        state.enrollments = await response.json();
        renderEnrollmentsTable();
    } catch (error) {
        console.error('Error fetching enrollments:', error);
    }
}

async function fetchGrades() {
    try {
        const response = await fetch(`${API_BASE}/grades`);
        state.grades = await response.json();
        renderGradesTable();
    } catch (error) {
        console.error('Error fetching grades:', error);
    }
}

async function fetchAttendance() {
    try {
        const response = await fetch(`${API_BASE}/attendance`);
        state.attendance = await response.json();
        renderAttendanceTable();
    } catch (error) {
        console.error('Error fetching attendance:', error);
    }
}

// 3. Render Tables
function renderStudentsTable(data = state.students) {
    const tbody = document.getElementById('students-table-body');
    tbody.innerHTML = '';
    
    if (data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No student records found.</td></tr>`;
        return;
    }

    data.forEach(student => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="font-weight: 600;">${student.studentId}</td>
            <td>${student.firstName} ${student.lastName}</td>
            <td>${student.email}</td>
            <td>${student.phoneNumber || 'N/A'}</td>
            <td>${student.dateOfBirth || 'N/A'}</td>
            <td>${student.enrollmentDate}</td>
            <td>
                <div class="actions-cell">
                    <button class="action-btn edit" onclick="openEditStudentModal(${student.id})"><i class="fas fa-edit"></i></button>
                    <button class="action-btn delete" onclick="deleteStudent(${student.id})"><i class="fas fa-trash"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function renderCoursesTable(data = state.courses) {
    const tbody = document.getElementById('courses-table-body');
    tbody.innerHTML = '';

    if (data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No course records found.</td></tr>`;
        return;
    }

    data.forEach(course => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><span class="badge badge-code">${course.courseCode}</span></td>
            <td style="font-weight: 600;">${course.courseName}</td>
            <td>${course.description || 'No description'}</td>
            <td style="text-align: center; font-weight: 500;">${course.credits}</td>
            <td>
                <div class="actions-cell">
                    <button class="action-btn edit" onclick="openEditCourseModal(${course.id})"><i class="fas fa-edit"></i></button>
                    <button class="action-btn delete" onclick="deleteCourse(${course.id})"><i class="fas fa-trash"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function renderEnrollmentsTable() {
    const tbody = document.getElementById('enrollments-table-body');
    tbody.innerHTML = '';

    if (state.enrollments.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No enrollment records found.</td></tr>`;
        return;
    }

    state.enrollments.forEach(enrollment => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="font-weight: 600;">${enrollment.student.studentId}</td>
            <td>${enrollment.student.firstName} ${enrollment.student.lastName}</td>
            <td><span class="badge badge-code">${enrollment.course.courseCode}</span> ${enrollment.course.courseName}</td>
            <td>${enrollment.enrollmentDate}</td>
            <td>
                <div class="actions-cell">
                    <button class="action-btn delete" onclick="deleteEnrollment(${enrollment.id})"><i class="fas fa-minus-circle"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function renderGradesTable() {
    const tbody = document.getElementById('grades-table-body');
    tbody.innerHTML = '';

    if (state.grades.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">No grade records found.</td></tr>`;
        return;
    }

    state.grades.forEach(gradeRec => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="font-weight: 600;">${gradeRec.student.studentId}</td>
            <td>${gradeRec.student.firstName} ${gradeRec.student.lastName}</td>
            <td><span class="badge badge-code">${gradeRec.course.courseCode}</span></td>
            <td style="font-weight: 700; color: var(--accent-emerald);">${gradeRec.grade}</td>
            <td>${gradeRec.remarks || '—'}</td>
            <td>
                <div class="actions-cell">
                    <button class="action-btn delete" onclick="deleteGrade(${gradeRec.id})"><i class="fas fa-trash"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function renderAttendanceTable() {
    const tbody = document.getElementById('attendance-table-body');
    tbody.innerHTML = '';

    if (state.attendance.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">No attendance records found.</td></tr>`;
        return;
    }

    state.attendance.forEach(attendanceRec => {
        const tr = document.createElement('tr');
        let badgeClass = 'badge-present';
        if (attendanceRec.status === 'ABSENT') badgeClass = 'badge-absent';
        if (attendanceRec.status === 'LATE') badgeClass = 'badge-late';

        tr.innerHTML = `
            <td style="font-weight: 600;">${attendanceRec.student.studentId}</td>
            <td>${attendanceRec.student.firstName} ${attendanceRec.student.lastName}</td>
            <td><span class="badge badge-code">${attendanceRec.course.courseCode}</span></td>
            <td>${attendanceRec.attendanceDate}</td>
            <td><span class="badge ${badgeClass}">${attendanceRec.status}</span></td>
            <td>
                <div class="actions-cell">
                    <button class="action-btn delete" onclick="deleteAttendance(${attendanceRec.id})"><i class="fas fa-trash"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Update select inputs in various enrollment, grade, and attendance forms
function updateSelectors() {
    const enrollStudentSelect = document.getElementById('enroll-student-select');
    const enrollCourseSelect = document.getElementById('enroll-course-select');
    
    const gradeStudentSelect = document.getElementById('grade-student-select');
    const gradeCourseSelect = document.getElementById('grade-course-select');
    
    const attStudentSelect = document.getElementById('attendance-student-select');
    const attCourseSelect = document.getElementById('attendance-course-select');

    const studentOptionsHTML = state.students.map(s => `<option value="${s.id}">${s.firstName} ${s.lastName} (${s.studentId})</option>`).join('');
    const courseOptionsHTML = state.courses.map(c => `<option value="${c.id}">${c.courseName} (${c.courseCode})</option>`).join('');

    if (enrollStudentSelect) enrollStudentSelect.innerHTML = '<option value="">Choose Student</option>' + studentOptionsHTML;
    if (enrollCourseSelect) enrollCourseSelect.innerHTML = '<option value="">Choose Course</option>' + courseOptionsHTML;

    if (gradeStudentSelect) gradeStudentSelect.innerHTML = '<option value="">Choose Student</option>' + studentOptionsHTML;
    if (gradeCourseSelect) gradeCourseSelect.innerHTML = '<option value="">Choose Course</option>' + courseOptionsHTML;

    if (attStudentSelect) attStudentSelect.innerHTML = '<option value="">Choose Student</option>' + studentOptionsHTML;
    if (attCourseSelect) attCourseSelect.innerHTML = '<option value="">Choose Course</option>' + courseOptionsHTML;
}

// 4. Modal Event Listeners and Setup
function setupEventListeners() {
    // Modals open/close handlers
    setupModalOpenClose('add-student-btn', 'student-modal', 'close-student-modal', 'student-form');
    setupModalOpenClose('add-course-btn', 'course-modal', 'close-course-modal', 'course-form');

    // Forms Submission handlers
    document.getElementById('student-form').addEventListener('submit', handleStudentFormSubmit);
    document.getElementById('course-form').addEventListener('submit', handleCourseFormSubmit);
    document.getElementById('enroll-form').addEventListener('submit', handleEnrollFormSubmit);
    document.getElementById('grade-form').addEventListener('submit', handleGradeFormSubmit);
    document.getElementById('attendance-form').addEventListener('submit', handleAttendanceFormSubmit);

    // Search inputs
    document.getElementById('student-search').addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        const filtered = state.students.filter(s => 
            s.firstName.toLowerCase().includes(query) || 
            s.lastName.toLowerCase().includes(query) || 
            s.studentId.toLowerCase().includes(query) ||
            s.email.toLowerCase().includes(query)
        );
        renderStudentsTable(filtered);
    });

    document.getElementById('course-search').addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        const filtered = state.courses.filter(c => 
            c.courseName.toLowerCase().includes(query) || 
            c.courseCode.toLowerCase().includes(query) ||
            (c.description && c.description.toLowerCase().includes(query))
        );
        renderCoursesTable(filtered);
    });
}

function setupModalOpenClose(btnId, modalId, closeBtnId, formId) {
    const btn = document.getElementById(btnId);
    const modal = document.getElementById(modalId);
    const closeBtn = document.getElementById(closeBtnId);
    const form = document.getElementById(formId);

    if (btn) {
        btn.addEventListener('click', () => {
            // Reset form details
            if (form) form.reset();
            const idField = document.getElementById(formId === 'student-form' ? 'student-id-field' : 'course-id-field');
            if (idField) idField.value = '';
            
            const title = modal.querySelector('h2');
            if (title) title.textContent = formId === 'student-form' ? 'Add Student' : 'Add Course';
            
            modal.classList.add('active');
        });
    }

    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            modal.classList.remove('active');
        });
    }

    // Close when clicking overlay outside content
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.classList.remove('active');
        }
    });
}

// 5. Submit Event Handlers
async function handleStudentFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('student-id-field').value;
    const studentData = {
        firstName: document.getElementById('student-first-name').value,
        lastName: document.getElementById('student-last-name').value,
        email: document.getElementById('student-email').value,
        phoneNumber: document.getElementById('student-phone').value,
        dateOfBirth: document.getElementById('student-dob').value || null,
        enrollmentDate: document.getElementById('student-enroll-date').value || null
    };

    try {
        let response;
        if (id) {
            // Update
            response = await fetch(`${API_BASE}/students/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(studentData)
            });
        } else {
            // Create
            response = await fetch(`${API_BASE}/students`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(studentData)
            });
        }

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Failed to save student record');
        }

        showToast(id ? 'Student record updated' : 'Student record created', 'success');
        document.getElementById('student-modal').classList.remove('active');
        
        await fetchStudents();
        updateSelectors();
        loadDashboardStats();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

async function handleCourseFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('course-id-field').value;
    const courseData = {
        courseCode: document.getElementById('course-code').value,
        courseName: document.getElementById('course-name').value,
        description: document.getElementById('course-desc').value,
        credits: parseInt(document.getElementById('course-credits').value)
    };

    try {
        let response;
        if (id) {
            // Update
            response = await fetch(`${API_BASE}/courses/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(courseData)
            });
        } else {
            // Create
            response = await fetch(`${API_BASE}/courses`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(courseData)
            });
        }

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Failed to save course record');
        }

        showToast(id ? 'Course updated successfully' : 'Course created successfully', 'success');
        document.getElementById('course-modal').classList.remove('active');

        await fetchCourses();
        updateSelectors();
        loadDashboardStats();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

async function handleEnrollFormSubmit(e) {
    e.preventDefault();
    const enrollmentData = {
        student: { id: parseInt(document.getElementById('enroll-student-select').value) },
        course: { id: parseInt(document.getElementById('enroll-course-select').value) },
        enrollmentDate: document.getElementById('enroll-date').value || null
    };

    if (!enrollmentData.student.id || !enrollmentData.course.id) {
        showToast('Please select both Student and Course', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/enrollments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(enrollmentData)
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Failed to enroll student');
        }

        showToast('Student enrolled successfully', 'success');
        document.getElementById('enroll-form').reset();
        
        await fetchEnrollments();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

async function handleGradeFormSubmit(e) {
    e.preventDefault();
    const gradeData = {
        student: { id: parseInt(document.getElementById('grade-student-select').value) },
        course: { id: parseInt(document.getElementById('grade-course-select').value) },
        grade: document.getElementById('grade-val').value,
        remarks: document.getElementById('grade-remarks').value
    };

    if (!gradeData.student.id || !gradeData.course.id || !gradeData.grade) {
        showToast('Please fill in Student, Course and Grade', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/grades`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(gradeData)
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Failed to record grade');
        }

        showToast('Grade recorded successfully', 'success');
        document.getElementById('grade-form').reset();

        await fetchGrades();
        loadDashboardStats();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

async function handleAttendanceFormSubmit(e) {
    e.preventDefault();
    const attendanceData = {
        student: { id: parseInt(document.getElementById('attendance-student-select').value) },
        course: { id: parseInt(document.getElementById('attendance-course-select').value) },
        attendanceDate: document.getElementById('attendance-date').value || null,
        status: document.getElementById('attendance-status').value
    };

    if (!attendanceData.student.id || !attendanceData.course.id || !attendanceData.status) {
        showToast('Please fill in Student, Course and Status', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/attendance`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(attendanceData)
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Failed to submit attendance');
        }

        showToast('Attendance recorded successfully', 'success');
        document.getElementById('attendance-form').reset();

        await fetchAttendance();
        loadDashboardStats();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

// 6. Edit Modals Launchers
function openEditStudentModal(id) {
    const student = state.students.find(s => s.id === id);
    if (!student) return;

    document.getElementById('student-id-field').value = student.id;
    document.getElementById('student-first-name').value = student.firstName;
    document.getElementById('student-last-name').value = student.lastName;
    document.getElementById('student-email').value = student.email;
    document.getElementById('student-phone').value = student.phoneNumber || '';
    document.getElementById('student-dob').value = student.dateOfBirth || '';
    document.getElementById('student-enroll-date').value = student.enrollmentDate || '';

    document.querySelector('#student-modal h2').textContent = 'Edit Student';
    document.getElementById('student-modal').classList.add('active');
}

function openEditCourseModal(id) {
    const course = state.courses.find(c => c.id === id);
    if (!course) return;

    document.getElementById('course-id-field').value = course.id;
    document.getElementById('course-code').value = course.courseCode;
    document.getElementById('course-name').value = course.courseName;
    document.getElementById('course-desc').value = course.description || '';
    document.getElementById('course-credits').value = course.credits;

    document.querySelector('#course-modal h2').textContent = 'Edit Course';
    document.getElementById('course-modal').classList.add('active');
}

// 7. Delete operations
async function deleteStudent(id) {
    if (!confirm('Are you sure you want to delete this student? All enrollments, grades, and attendance associated with this student will also be removed.')) return;

    try {
        const response = await fetch(`${API_BASE}/students/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete student');

        showToast('Student and linked records deleted', 'success');
        
        await fetchStudents();
        await fetchEnrollments();
        await fetchGrades();
        await fetchAttendance();
        updateSelectors();
        loadDashboardStats();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

async function deleteCourse(id) {
    if (!confirm('Are you sure you want to delete this course? All enrollments, grades, and attendance associated with this course will also be removed.')) return;

    try {
        const response = await fetch(`${API_BASE}/courses/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete course');

        showToast('Course and linked records deleted', 'success');

        await fetchCourses();
        await fetchEnrollments();
        await fetchGrades();
        await fetchAttendance();
        updateSelectors();
        loadDashboardStats();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

async function deleteEnrollment(id) {
    if (!confirm('Are you sure you want to remove this enrollment?')) return;

    try {
        const response = await fetch(`${API_BASE}/enrollments/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to remove enrollment');

        showToast('Enrollment removed', 'success');
        await fetchEnrollments();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

async function deleteGrade(id) {
    if (!confirm('Delete this grade record?')) return;

    try {
        const response = await fetch(`${API_BASE}/grades/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete grade record');

        showToast('Grade record deleted', 'success');
        await fetchGrades();
        loadDashboardStats();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

async function deleteAttendance(id) {
    if (!confirm('Delete this attendance record?')) return;

    try {
        const response = await fetch(`${API_BASE}/attendance/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete attendance record');

        showToast('Attendance record deleted', 'success');
        await fetchAttendance();
        loadDashboardStats();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

// 8. Toast Helper
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
    toast.innerHTML = `
        <i class="fas ${icon}" style="color: ${type === 'success' ? 'var(--accent-emerald)' : 'var(--accent-rose)'}"></i>
        <span>${message}</span>
    `;
    
    container.appendChild(toast);
    
    // Animate out and remove after 4 seconds
    setTimeout(() => {
        toast.style.animation = 'toastSlideIn 0.3s reverse forwards';
        toast.addEventListener('animationend', () => toast.remove());
    }, 4000);
}
