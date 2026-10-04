// =====================================================
// EXAMPRO - ADMIN JAVASCRIPT
// =====================================================

const API_BASE =
    "http://localhost:8080/online-exam";

let selectedExamId = null;


// =====================================================
// ADMIN LOGIN
// =====================================================

async function adminLogin() {

    const username =
        document.getElementById("adminUsername").value.trim();

    const password =
        document.getElementById("adminPassword").value.trim();

    if (!username || !password) {
        alert("Please enter username and password.");
        return;
    }

    try {

        const response =
            await fetch(
                API_BASE + "/admin/login",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        new URLSearchParams({
                            username: username,
                            password: password
                        })
                }
            );

        const data =
            await response.json();

        if (response.ok && data.success) {

            localStorage.setItem(
                "admin",
                JSON.stringify(data.admin)
            );

            alert("Admin login successful!");

            document
                .getElementById("adminLogin")
                .classList.add("hidden");

            document
                .getElementById("adminDashboard")
                .classList.remove("hidden");

            loadDashboard();

            showPanel("dashboardPanel");

        } else {

            alert(
                data.message ||
                "Invalid username or password."
            );

        }

    } catch (error) {

        console.error(
            "Admin login error:",
            error
        );

        alert(
            "Unable to connect to the server. Make sure Tomcat is running."
        );

    }

}


// =====================================================
// ADMIN LOGOUT
// =====================================================

function adminLogout() {

    localStorage.removeItem("admin");

    document
        .getElementById("adminDashboard")
        .classList.add("hidden");

    document
        .getElementById("adminLogin")
        .classList.remove("hidden");

}


// =====================================================
// SHOW PANEL
// =====================================================

function showPanel(panelId) {

    const panels = [
        "dashboardPanel",
        "examPanel",
        "questionPanel",
        "studentPanel",
        "resultPanel"
    ];

    panels.forEach(function (id) {

        const panel =
            document.getElementById(id);

        if (panel) {

            panel.classList.add("hidden");

        }

    });

    const selectedPanel =
        document.getElementById(panelId);

    if (selectedPanel) {

        selectedPanel.classList.remove("hidden");

    }


    if (panelId === "dashboardPanel") {

        loadDashboard();

    }


    if (panelId === "examPanel") {

        loadDashboard();

    }


    if (panelId === "questionPanel") {

        loadExamSelector();

    }


    if (panelId === "studentPanel") {

        loadStudents();

    }


    if (panelId === "resultPanel") {

        loadResults();

    }

}


// =====================================================
// DASHBOARD
// =====================================================

async function loadDashboard() {

    try {

        const response =
            await fetch(
                API_BASE + "/admin/dashboard"
            );

        const data =
            await response.json();

        if (!data.success) {

            alert(
                data.message ||
                "Failed to load dashboard data."
            );

            return;

        }

        const dashboard =
            data.dashboard;

        document.getElementById(
            "totalQuestions"
        ).innerText =
            dashboard.total_questions;

        document.getElementById(
            "totalStudents"
        ).innerText =
            dashboard.total_students;

        document.getElementById(
            "totalResults"
        ).innerText =
            dashboard.total_results;

        document.getElementById(
            "totalExams"
        ).innerText =
            dashboard.total_exams;

    } catch (error) {

        console.error(
            "Dashboard error:",
            error
        );

        alert(
            "Unable to connect to the server. Make sure Tomcat is running."
        );

    }

}


// =====================================================
// CREATE EXAM
// =====================================================

async function createExam() {

    const name =
        document
            .getElementById("examName")
            .value
            .trim();

    const description =
        document
            .getElementById("examDescription")
            .value
            .trim();

    const duration =
        document
            .getElementById("examDuration")
            .value;

    const marks =
        document
            .getElementById("examMarks")
            .value;

    if (
        !name ||
        !description ||
        !duration ||
        !marks
    ) {

        alert(
            "Please fill all examination details."
        );

        return;

    }

    try {

        const response =
            await fetch(
                API_BASE + "/admin/exam/create",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        new URLSearchParams({
                            name: name,
                            description: description,
                            duration: duration,
                            marks: marks
                        })
                }
            );

        const data =
            await response.json();

        if (
            response.ok &&
            data.success
        ) {

            alert(
                "Examination created successfully!"
            );

            document
                .getElementById("examName")
                .value = "";

            document
                .getElementById("examDescription")
                .value = "";

            document
                .getElementById("examDuration")
                .value = "";

            document
                .getElementById("examMarks")
                .value = "";

            loadDashboard();

        } else {

            alert(
                data.message ||
                "Failed to create examination."
            );

        }

    } catch (error) {

        console.error(
            "Create exam error:",
            error
        );

        alert(
            "Unable to connect to the server. Make sure Tomcat is running."
        );

    }

}


// =====================================================
// LOAD EXAM SELECTOR
// =====================================================

async function loadExamSelector() {

    const questionsContainer =
        document.getElementById(
            "questionsList"
        );

    if (!questionsContainer) {
        return;
    }

    questionsContainer.innerHTML =
        "<p>Loading examinations...</p>";

    try {

        const response =
            await fetch(
                API_BASE + "/student/exams"
            );

        const data =
            await response.json();

        if (
            !data.success ||
            !data.exams
        ) {

            questionsContainer.innerHTML =
                "<p>Unable to load examinations.</p>";

            return;

        }

        const exams =
            data.exams;

        if (exams.length === 0) {

            questionsContainer.innerHTML =
                "<p>No examinations available.</p>";

            return;

        }


        // Create selector
        const wrapper =
            document.createElement("div");

        wrapper.id =
            "adminExamSelectorWrapper";


        const label =
            document.createElement("label");

        label.innerText =
            "Select Examination";


        const select =
            document.createElement("select");

        select.id =
            "adminExamSelector";


        exams.forEach(function (exam) {

            const option =
                document.createElement("option");

            option.value =
                exam.exam_id;

            option.textContent =
                exam.exam_name;

            select.appendChild(option);

        });


        wrapper.appendChild(label);

        wrapper.appendChild(select);


        // Put selector before questions
        questionsContainer.innerHTML = "";

        questionsContainer.appendChild(
            wrapper
        );


        if (
            selectedExamId &&
            exams.some(function (exam) {
                return Number(exam.exam_id) ===
                    Number(selectedExamId);
            })
        ) {

            select.value =
                selectedExamId;

        } else {

            selectedExamId =
                Number(select.value);

        }


        select.addEventListener(
            "change",
            function () {

                selectedExamId =
                    Number(this.value);

                loadQuestions();

            }
        );


        loadQuestions();

    } catch (error) {

        console.error(
            "Load exam selector error:",
            error
        );

        questionsContainer.innerHTML =
            "<p>Unable to connect to the server.</p>";

    }

}


// =====================================================
// ADD QUESTION
// =====================================================

async function addQuestion() {

    const question =
        document
            .getElementById("questionText")
            .value
            .trim();

    const optionA =
        document
            .getElementById("optionA")
            .value
            .trim();

    const optionB =
        document
            .getElementById("optionB")
            .value
            .trim();

    const optionC =
        document
            .getElementById("optionC")
            .value
            .trim();

    const optionD =
        document
            .getElementById("optionD")
            .value
            .trim();

    const answer =
        document
            .getElementById("correctAnswer")
            .value;


    if (!selectedExamId) {

        alert(
            "Please select an examination first."
        );

        return;

    }


    if (
        !question ||
        !optionA ||
        !optionB ||
        !optionC ||
        !optionD ||
        !answer
    ) {

        alert(
            "Please fill all question fields."
        );

        return;

    }


    // Each question = 1 mark
    const marks = 1;


    try {

        const response =
            await fetch(
                API_BASE +
                "/admin/question/add",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        new URLSearchParams({

                            exam_id:
                                selectedExamId,

                            question_text:
                                question,

                            option_a:
                                optionA,

                            option_b:
                                optionB,

                            option_c:
                                optionC,

                            option_d:
                                optionD,

                            correct_answer:
                                answer,

                            marks:
                                marks
                        })
                }
            );

        const data =
            await response.json();


        if (
            response.ok &&
            data.success
        ) {

            alert(
                "Question added successfully!"
            );


            document
                .getElementById("questionText")
                .value = "";

            document
                .getElementById("optionA")
                .value = "";

            document
                .getElementById("optionB")
                .value = "";

            document
                .getElementById("optionC")
                .value = "";

            document
                .getElementById("optionD")
                .value = "";

            document
                .getElementById("correctAnswer")
                .value = "";


            loadQuestions();

            loadDashboard();

        } else {

            alert(
                data.message ||
                "Failed to add question."
            );

        }

    } catch (error) {

        console.error(
            "Add question error:",
            error
        );

        alert(
            "Unable to connect to the server. Make sure Tomcat is running."
        );

    }

}


// =====================================================
// LOAD QUESTIONS FOR SELECTED EXAM
// =====================================================

async function loadQuestions() {

    const container =
        document.getElementById(
            "questionsList"
        );

    if (!container) {
        return;
    }


    if (!selectedExamId) {
        return;
    }


    // Keep selector
    const selector =
        document.getElementById(
            "adminExamSelectorWrapper"
        );


    container.innerHTML = "";


    if (selector) {

        container.appendChild(
            selector
        );

    }


    const loading =
        document.createElement("p");

    loading.innerText =
        "Loading questions...";

    container.appendChild(
        loading
    );


    try {

        const response =
            await fetch(
                API_BASE +
                "/admin/questions?exam_id=" +
                selectedExamId
            );

        const data =
            await response.json();


        if (
            !data.success ||
            !data.questions
        ) {

            container.innerHTML =
                "<p>Unable to load questions.</p>";

            return;

        }


        const questions =
            data.questions;


        // Remove loading text
        if (loading.parentNode) {
            loading.remove();
        }


        if (questions.length === 0) {

            const empty =
                document.createElement("div");

            empty.className =
                "empty-message";

            empty.innerText =
                "No questions added for this examination yet.";

            container.appendChild(
                empty
            );

            return;

        }


        questions.forEach(
            function (q, index) {

                const card =
                    document.createElement(
                        "div"
                    );

                card.className =
                    "admin-question-card";


                card.innerHTML = `

                    <div class="question-card-header">

                        <h3>
                            Question ${index + 1}
                        </h3>

                        <button
                            class="delete-btn"
                            onclick="deleteQuestion(${q.question_id})">
                            Delete
                        </button>

                    </div>

                    <p class="admin-question-text">
                        ${escapeHtml(
                            q.question_text
                        )}
                    </p>

                    <p>
                        <strong>A.</strong>
                        ${escapeHtml(q.option_a)}
                    </p>

                    <p>
                        <strong>B.</strong>
                        ${escapeHtml(q.option_b)}
                    </p>

                    <p>
                        <strong>C.</strong>
                        ${escapeHtml(q.option_c)}
                    </p>

                    <p>
                        <strong>D.</strong>
                        ${escapeHtml(q.option_d)}
                    </p>

                    <div class="correct-answer">

                        Correct Answer:
                        <strong>
                            ${escapeHtml(
                                q.correct_answer
                            )}
                        </strong>

                    </div>
                `;


                container.appendChild(
                    card
                );

            }
        );

    } catch (error) {

        console.error(
            "Load questions error:",
            error
        );

        container.innerHTML =
            "<p>Unable to connect to the server.</p>";

    }

}


// =====================================================
// DELETE QUESTION
// =====================================================

async function deleteQuestion(
    questionId
) {

    if (
        !confirm(
            "Are you sure you want to delete this question?"
        )
    ) {

        return;

    }


    try {

        const response =
            await fetch(
                API_BASE +
                "/admin/question/delete",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        new URLSearchParams({
                            question_id:
                                questionId
                        })
                }
            );


        const data =
            await response.json();


        if (
            response.ok &&
            data.success
        ) {

            alert(
                "Question deleted successfully!"
            );

            loadQuestions();

            loadDashboard();

        } else {

            alert(
                data.message ||
                "Failed to delete question."
            );

        }

    } catch (error) {

        console.error(
            "Delete question error:",
            error
        );

        alert(
            "Unable to connect to the server. Make sure Tomcat is running."
        );

    }

}


// =====================================================
// LOAD STUDENTS
// =====================================================

async function loadStudents() {

    const container =
        document.getElementById(
            "studentsList"
        );

    if (!container) {
        return;
    }


    container.innerHTML =
        "<p>Loading students...</p>";


    try {

        const response =
            await fetch(
                API_BASE + "/admin/students"
            );

        const data =
            await response.json();


        if (
            !data.success ||
            !data.students
        ) {

            container.innerHTML =
                '<div class="empty-message">' +
                'Unable to load students.' +
                '</div>';

            return;

        }


        const students =
            data.students;


        if (students.length === 0) {

            container.innerHTML =
                '<div class="empty-message">' +
                'No students registered yet.' +
                '</div>';

            return;

        }


        container.innerHTML = "";


        students.forEach(
            function (student) {

                const card =
                    document.createElement(
                        "div"
                    );

                card.className =
                    "student-admin-card";


                card.innerHTML = `

                    <div class="student-avatar">
                        👨‍🎓
                    </div>

                    <div>

                        <h3>
                            ${escapeHtml(
                                student.name
                            )}
                        </h3>

                        <p>
                            ${escapeHtml(
                                student.email
                            )}
                        </p>

                        <p>
                            Student ID:
                            ${student.student_id}
                        </p>

                        <p>
                            Registered:
                            ${escapeHtml(
                                student.created_at
                            )}
                        </p>

                    </div>
                `;


                container.appendChild(
                    card
                );

            }
        );

    } catch (error) {

        console.error(
            "Load students error:",
            error
        );

        container.innerHTML =
            '<div class="empty-message">' +
            'Unable to connect to the server.' +
            '</div>';

    }

}


// =====================================================
// LOAD RESULTS
// =====================================================

async function loadResults() {

    const container =
        document.getElementById(
            "resultsList"
        );

    if (!container) {
        return;
    }


    container.innerHTML =
        "<p>Loading examination results...</p>";


    try {

        const response =
            await fetch(
                API_BASE + "/admin/results"
            );

        const data =
            await response.json();


        if (
            !data.success ||
            !data.results
        ) {

            container.innerHTML =
                '<div class="empty-message">' +
                'Unable to load examination results.' +
                '</div>';

            return;

        }


        const results =
            data.results;


        if (results.length === 0) {

            container.innerHTML =
                '<div class="empty-message">' +
                'No examination results available yet.' +
                '</div>';

            return;

        }


        container.innerHTML = "";


        results.forEach(
            function (result) {

                const status =
                    result.status;


                const card =
                    document.createElement(
                        "div"
                    );

                card.className =
                    "admin-result-card";


                card.innerHTML = `

                    <div class="result-header">

                        <div class="result-avatar">
                            📊
                        </div>

                        <div>

                            <h2>
                                ${escapeHtml(
                                    result.student_name
                                )}
                            </h2>

                            <p>
                                ${escapeHtml(
                                    result.student_email
                                )}
                            </p>

                        </div>

                    </div>


                    <div class="result-grid">

                        <div class="result-box">

                            <span>
                                Exam
                            </span>

                            <strong>
                                ${escapeHtml(
                                    result.exam_name
                                )}
                            </strong>

                        </div>


                        <div class="result-box">

                            <span>
                                Score
                            </span>

                            <strong>
                                ${result.score}
                                /
                                ${result.total_marks}
                            </strong>

                        </div>


                        <div class="result-box">

                            <span>
                                Percentage
                            </span>

                            <strong>
                                ${Number(
                                    result.percentage
                                ).toFixed(2)}%
                            </strong>

                        </div>


                        <div class="result-box">

                            <span>
                                Status
                            </span>

                            <strong class="${
                                status === "PASS"
                                    ? "pass"
                                    : "fail"
                            }">

                                ${escapeHtml(
                                    status
                                )}

                            </strong>

                        </div>


                        <div class="result-box">

                            <span>
                                Date
                            </span>

                            <strong>
                                ${escapeHtml(
                                    result.exam_date
                                )}
                            </strong>

                        </div>

                    </div>
                `;


                container.appendChild(
                    card
                );

            }
        );

    } catch (error) {

        console.error(
            "Load results error:",
            error
        );

        container.innerHTML =
            '<div class="empty-message">' +
            'Unable to connect to the server.' +
            '</div>';

    }

}


// =====================================================
// ESCAPE HTML
// =====================================================

function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";

    }


    return String(value)

        .replace(
            /&/g,
            "&amp;"
        )

        .replace(
            /</g,
            "&lt;"
        )

        .replace(
            />/g,
            "&gt;"
        )

        .replace(
            /"/g,
            "&quot;"
        )

        .replace(
            /'/g,
            "&#039;"
        );

}