# 🎓 Academix – Student Management System

A full-stack web application designed for comprehensive academic administration. It provides efficient record-keeping and CRUD operations for students, courses, grades, attendance tracking, and enrollment metrics.

🔗 **Live Demo:** [https://student-management-system-mctp.onrender.com](https://student-management-system-mctp.onrender.com)  
💻 **Repository:** [https://github.com/simpal1501/student-management-system](https://github.com/simpal1501/student-management-system)

---

## 🚀 Key Features

- **Student Records Management:** Add, view, edit, and delete student details with real-time validation.
- **Course & Enrollment Tracking:** Assign students to courses, manage enrollment lists, and view class rosters.
- **Grades & Attendance Administration:** Record academic performance, calculate metrics, and track daily attendance.
- **Interactive Dashboard:** Modern, responsive Single-Page Application (SPA) dashboard built with vanilla JavaScript Fetch APIs and CSS.
- **In-Memory H2 Database:** Fast local and cloud-based relational database setup for development and testing.

---

## 🛠️ Tech Stack

- **Backend:** Java 17, Spring Boot, RESTful APIs
- **Database:** H2 Database (In-Memory / Persistent)
- **Frontend:** HTML5, CSS3, JavaScript (Fetch API)
- **Deployment:** Render

---

## 📡 API Endpoints (Sample)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/students` | Retrieve all student records |
| `POST` | `/api/students` | Register a new student |
| `GET` | `/api/students/{id}` | Get specific student details |
| `DELETE` | `/api/students/{id}` | Delete a student record |
| `GET` | `/api/courses` | List all available courses |
| `GET` | `/api/attendance` | View attendance summaries |

---

## 💻 Local Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/simpal1501/student-management-system.git
   cd student-management-system
   ```

2. **Run using Maven:**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Open in your browser:**
   ```
   http://localhost:8080
   ```

---

## 👤 Author

- **Simpal Kumari**  
  - GitHub: [@simpal1501](https://github.com/simpal1501)  
  - LinkedIn: [Simpal Kumari](https://www.linkedin.com/in/simpal-kumari-76b050310/)
