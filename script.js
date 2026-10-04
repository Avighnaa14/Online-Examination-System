// =====================================================
// EXAMPRO - STUDENT JAVASCRIPT
// =====================================================

const API_BASE =
    "http://localhost:8080/online-exam";


// =====================================================
// VARIABLES
// =====================================================

let questions = [];
let currentQuestion = 0;
let answers = [];
let timeLeft = 600;
let timerInterval = null;
let student = null;
let currentExam = null;


// =====================================================
// PAGE LOAD
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        student =
            JSON.parse(
                localStorage.getItem("student")
            );

    }
);


// =====================================================
// SHOW REGISTER
// =====================================================

function showRegister() {

    document
        .getElementById("login")
        .classList.add("hidden");

    document
        .getElementById("register")
        .classList.remove("hidden");

}


// =====================================================
// SHOW LOGIN
// =====================================================

function showLogin() {

    document
        .getElementById("register")
        .classList.add("hidden");

    document
        .getElementById("login")
        .classList.remove("hidden");

}


// =====================================================
// REGISTER
// =====================================================

async function registerStudent() {

    const name =
        document
            .getElementById("registerName")
            .value
            .trim();

    const email =
        document
            .getElementById("registerEmail")
            .value
            .trim();

    const password =
        document
            .getElementById("registerPassword")
            .value
            .trim();


    if (
        !name ||
        !email ||
        !password
    ) {

        alert(
            "Please fill all registration fields."
        );

        return;
    }


    const formData =
        new URLSearchParams();


    formData.append(
        "name",
        name
    );

    formData.append(
        "email",
        email
    );

    formData.append(
        "password",
        password
    );


    try {

        const response =
            await fetch(
                API_BASE +
                "/student/register",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        formData.toString()
                }
            );


        const data =
            await response.json();


        if (!data.success) {

            alert(
                data.message ||
                "Registration failed."
            );

            return;
        }


        alert(
            "Registration successful! Please login."
        );


        document
            .getElementById("registerName")
            .value = "";

        document
            .getElementById("registerEmail")
            .value = "";

        document
            .getElementById("registerPassword")
            .value = "";


        showLogin();


    } catch (error) {

        console.error(error);

        alert(
            "Unable to connect to the server."
        );

    }

}


// =====================================================
// LOGIN
// =====================================================

async function studentLogin() {

    const email =
        document
            .getElementById("loginEmail")
            .value
            .trim();

    const password =
        document
            .getElementById("loginPassword")
            .value
            .trim();


    if (
        !email ||
        !password
    ) {

        alert(
            "Please enter email and password."
        );

        return;
    }


    const formData =
        new URLSearchParams();


    formData.append(
        "email",
        email
    );

    formData.append(
        "password",
        password
    );


    try {

        const response =
            await fetch(
                API_BASE +
                "/student/login",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        formData.toString()
                }
            );


        const data =
            await response.json();


        if (!data.success) {

            alert(
                data.message ||
                "Invalid email or password."
            );

            return;
        }


        student =
            data.student;


        localStorage.setItem(
            "student",
            JSON.stringify(student)
        );


        alert(
            "Login successful! Welcome " +
            student.name
        );


        document
            .getElementById("login")
            .classList.add("hidden");


        await loadAvailableExam();


        document
            .getElementById("examStartBox")
            .classList.remove("hidden");


        document
            .getElementById("examStartBox")
            .scrollIntoView({
                behavior: "smooth"
            });


    } catch (error) {

        console.error(error);

        alert(
            "Unable to connect to the server."
        );

    }

}


// =====================================================
// LOAD ALL AVAILABLE EXAMS
// =====================================================

async function loadAvailableExam() {

    try {

        const response =
            await fetch(
                API_BASE +
                "/student/exams"
            );


        const data =
            await response.json();


        if (!data.success) {

            alert(
                data.message ||
                "Failed to load examinations."
            );

            return;
        }


        const examStartBox =
            document.getElementById(
                "examStartBox"
            );


        if (!examStartBox) {
            return;
        }


        // Clear old exam cards
        examStartBox.innerHTML = "";


        if (
            !data.exams ||
            data.exams.length === 0
        ) {

            showNoExams();

            return;
        }


        currentExam = null;


        // Create one card for every exam
        for (
            let i = 0;
            i < data.exams.length;
            i++
        ) {

            const exam =
                data.exams[i];


            const questionCount =
                await getQuestionCount(
                    exam.exam_id
                );


            const examCard =
                document.createElement(
                    "div"
                );


            // IMPORTANT:
            // Use the existing CSS class
            examCard.className =
                "exam-start-card";


            examCard.innerHTML = `

                <div class="exam-icon">
                    🎓
                </div>

                <h2>
                    Available Examination
                </h2>

                <h3>
                    ${escapeHtml(
                        exam.exam_name
                    )}
                </h3>

                <p>
                    ${escapeHtml(
                        exam.description || ""
                    )}
                </p>

                <div class="exam-info">

                    <div>

                        <strong>
                            Duration
                        </strong>

                        <span>
                            ${exam.duration}
                            Minutes
                        </span>

                    </div>

                    <div>

                        <strong>
                            Questions
                        </strong>

                        <span>
                            ${questionCount}
                        </span>

                    </div>

                </div>

                <button
                    class="attend-btn"
                    type="button"
                    onclick="startExam(${exam.exam_id})">

                    Attend Exam

                </button>

            `;


            examStartBox.appendChild(
                examCard
            );

        }


    } catch (error) {

        console.error(
            "Load examinations error:",
            error
        );


        alert(
            "Unable to load examinations."
        );

    }

}


// =====================================================
// GET QUESTION COUNT
// =====================================================

async function getQuestionCount(examId) {

    try {

        const response =
            await fetch(
                API_BASE +
                "/admin/questions?exam_id=" +
                examId
            );


        const data =
            await response.json();


        if (
            data.success &&
            data.questions
        ) {

            return data.questions.length;
        }


        return 0;


    } catch (error) {

        console.error(
            "Question count error:",
            error
        );


        return 0;

    }

}


// =====================================================
// SHOW NO EXAMS
// =====================================================

function showNoExams() {

    const examStartBox =
        document.getElementById(
            "examStartBox"
        );


    if (!examStartBox) {
        return;
    }


    examStartBox.innerHTML = `

        <div class="exam-start-card">

            <div class="exam-icon">
                🎓
            </div>

            <h2>
                No Exam Available
            </h2>

            <p>
                There are currently no active examinations.
            </p>

        </div>

    `;


    currentExam = null;

}


// =====================================================
// START SELECTED EXAM
// =====================================================

async function startExam(examId) {

    try {

        // Get all active exams
        const examResponse =
            await fetch(
                API_BASE +
                "/student/exams"
            );


        const examData =
            await examResponse.json();


        if (
            !examData.success ||
            !examData.exams
        ) {

            alert(
                "Unable to load examination details."
            );

            return;
        }


        // Find the exam that was clicked
        const selectedExam =
            examData.exams.find(
                function (exam) {

                    return Number(
                        exam.exam_id
                    ) === Number(
                        examId
                    );

                }
            );


        if (!selectedExam) {

            alert(
                "Selected examination was not found."
            );

            return;
        }


        currentExam =
            selectedExam;


        // Get questions for ONLY this exam
        const response =
            await fetch(
                API_BASE +
                "/admin/questions?exam_id=" +
                currentExam.exam_id
            );


        const data =
            await response.json();


        if (!data.success) {

            alert(
                data.message ||
                "Failed to load questions."
            );

            return;
        }


        if (
            !data.questions ||
            data.questions.length === 0
        ) {

            alert(
                "No questions available for this exam."
            );

            return;
        }


        // Convert database questions
        questions =
            data.questions.map(
                function (question) {

                    return {

                        question_id:
                            question.question_id,

                        question:
                            question.question_text,

                        options: [

                            question.option_a,

                            question.option_b,

                            question.option_c,

                            question.option_d

                        ],

                        answer:
                            question.correct_answer,

                        marks:
                            question.marks

                    };

                }
            );


        currentQuestion = 0;


        answers =
            new Array(
                questions.length
            );


        // Get duration from database
        timeLeft =
            Number(
                currentExam.duration
            ) * 60;


        if (
            !timeLeft ||
            timeLeft <= 0
        ) {

            timeLeft =
                10 * 60;

        }


        // Hide exam selection
        document
            .getElementById("examStartBox")
            .classList.add("hidden");


        // Show exam
        document
            .getElementById("examSection")
            .classList.remove("hidden");


        // Set exam details
        document
            .getElementById("examTitle")
            .innerText =
                currentExam.exam_name;


        document
            .getElementById("examDescription")
            .innerText =
                currentExam.description || "";


        document
            .getElementById("totalQuestions")
            .innerText =
                questions.length;


        // Create question numbers
        createPalette();


        // Show first question
        showQuestion();


        // Start timer
        startTimer();


        document
            .getElementById("examSection")
            .scrollIntoView({
                behavior: "smooth"
            });


    } catch (error) {

        console.error(
            "Start exam error:",
            error
        );


        alert(
            "Unable to load exam questions."
        );

    }

}


// =====================================================
// SHOW QUESTION
// =====================================================

function showQuestion() {

    const question =
        questions[currentQuestion];


    if (!question) {
        return;
    }


    document
        .getElementById("questionNumber")
        .innerText =
            currentQuestion + 1;


    document
        .getElementById("totalQuestions")
        .innerText =
            questions.length;


    document
        .getElementById("questionText")
        .innerText =
            question.question;


    const optionsContainer =
        document.getElementById(
            "options"
        );


    optionsContainer.innerHTML = "";


    question.options.forEach(
        function (option, index) {

            const letter =
                String.fromCharCode(
                    65 + index
                );


            const div =
                document.createElement(
                    "div"
                );


            div.className =
                "exam-option";


            div.innerHTML = `

                <label>

                    <input
                        type="radio"
                        name="answer"
                        value="${letter}"
                    >

                    <span>
                        ${letter}.
                        ${escapeHtml(option)}
                    </span>

                </label>

            `;


            const radio =
                div.querySelector(
                    "input"
                );


            if (
                answers[currentQuestion] ===
                letter
            ) {

                radio.checked =
                    true;

            }


            radio.addEventListener(
                "change",
                function () {

                    answers[currentQuestion] =
                        this.value;


                    updatePalette();

                }
            );


            optionsContainer.appendChild(
                div
            );

        }
    );


    updateButtons();

    updatePalette();

}


// =====================================================
// NEXT QUESTION
// =====================================================

function nextQuestion() {

    saveCurrentAnswer();


    if (
        currentQuestion <
        questions.length - 1
    ) {

        currentQuestion++;

        showQuestion();

    }

}


// =====================================================
// PREVIOUS QUESTION
// =====================================================

function previousQuestion() {

    saveCurrentAnswer();


    if (
        currentQuestion > 0
    ) {

        currentQuestion--;

        showQuestion();

    }

}


// =====================================================
// SAVE CURRENT ANSWER
// =====================================================

function saveCurrentAnswer() {

    const selected =
        document.querySelector(
            'input[name="answer"]:checked'
        );


    if (selected) {

        answers[currentQuestion] =
            selected.value;

    }

}


// =====================================================
// UPDATE BUTTONS
// =====================================================

function updateButtons() {

    const previous =
        document.getElementById(
            "previousBtn"
        );


    const next =
        document.getElementById(
            "nextBtn"
        );


    const submit =
        document.getElementById(
            "submitBtn"
        );


    previous.disabled =
        currentQuestion === 0;


    if (
        currentQuestion ===
        questions.length - 1
    ) {

        next.style.display =
            "none";

        submit.style.display =
            "inline-block";


    } else {

        next.style.display =
            "inline-block";

        submit.style.display =
            "none";

    }

}


// =====================================================
// CREATE QUESTION PALETTE
// =====================================================

function createPalette() {

    const palette =
        document.getElementById(
            "palette"
        );


    palette.innerHTML = "";


    questions.forEach(
        function (_, index) {

            const button =
                document.createElement(
                    "button"
                );


            button.innerText =
                index + 1;


            button.className =
                "palette-btn";


            button.onclick =
                function () {

                    saveCurrentAnswer();


                    currentQuestion =
                        index;


                    showQuestion();

                };


            palette.appendChild(
                button
            );

        }
    );

}


// =====================================================
// UPDATE QUESTION PALETTE
// =====================================================

function updatePalette() {

    const buttons =
        document.querySelectorAll(
            ".palette-btn"
        );


    buttons.forEach(
        function (button, index) {

            button.classList.remove(
                "current"
            );


            button.classList.remove(
                "answered"
            );


            if (
                index === currentQuestion
            ) {

                button.classList.add(
                    "current"
                );

            }


            if (answers[index]) {

                button.classList.add(
                    "answered"
                );

            }

        }
    );

}


// =====================================================
// TIMER
// =====================================================

function startTimer() {

    clearInterval(
        timerInterval
    );


    updateTimerDisplay();


    timerInterval =
        setInterval(
            function () {

                timeLeft--;


                updateTimerDisplay();


                if (
                    timeLeft <= 0
                ) {

                    clearInterval(
                        timerInterval
                    );


                    alert(
                        "Time is over! Your exam will be submitted automatically."
                    );


                    submitExam();

                }

            },
            1000
        );

}


// =====================================================
// TIMER DISPLAY
// =====================================================

function updateTimerDisplay() {

    const timer =
        document.getElementById(
            "timer"
        );


    const minutes =
        Math.floor(
            timeLeft / 60
        );


    const seconds =
        timeLeft % 60;


    timer.innerText =
        String(minutes).padStart(
            2,
            "0"
        ) +
        ":" +
        String(seconds).padStart(
            2,
            "0"
        );


    if (
        timeLeft <= 60
    ) {

        timer.classList.add(
            "timer-danger"
        );


    } else {

        timer.classList.remove(
            "timer-danger"
        );

    }

}


// =====================================================
// SUBMIT EXAM
// =====================================================

async function submitExam() {

    saveCurrentAnswer();


    clearInterval(
        timerInterval
    );


    if (
        !student ||
        !student.student_id
    ) {

        alert(
            "Student login information is missing."
        );

        return;
    }


    if (
        !currentExam ||
        !currentExam.exam_id
    ) {

        alert(
            "Exam information is missing."
        );

        return;
    }


    const formData =
        new URLSearchParams();


    formData.append(
        "student_id",
        student.student_id
    );


    formData.append(
        "exam_id",
        currentExam.exam_id
    );


    questions.forEach(
        function (question, index) {

            const selectedAnswer =
                answers[index];


            if (selectedAnswer) {

                formData.append(
                    "question_" +
                    question.question_id,
                    selectedAnswer
                );

            }

        }
    );


    try {

        const response =
            await fetch(
                API_BASE +
                "/student/exam/submit",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        formData.toString()
                }
            );


        const data =
            await response.json();


        if (!data.success) {

            alert(
                data.message ||
                "Failed to submit exam."
            );

            return;
        }


        const result =
            data.result;


        localStorage.setItem(
            "lastResult",
            JSON.stringify(result)
        );


        document
            .getElementById("examSection")
            .classList.add("hidden");


        document
            .getElementById("resultSection")
            .classList.remove("hidden");


        document
            .getElementById("resultName")
            .innerText =
                student.name;


        document
            .getElementById("resultScore")
            .innerText =
                result.score +
                " / " +
                result.total_marks;


        document
            .getElementById("resultPercentage")
            .innerText =
                Number(
                    result.percentage
                ).toFixed(2) +
                "%";


        document
            .getElementById("resultStatus")
            .innerText =
                result.status;


        document
            .getElementById("resultStatus")
            .className =
                result.status === "PASS"
                    ? "pass"
                    : "fail";


        document
            .getElementById("resultSection")
            .scrollIntoView({
                behavior: "smooth"
            });


    } catch (error) {

        console.error(error);


        alert(
            "Unable to submit exam. Please check the server connection."
        );

    }

}


// =====================================================
// RETAKE EXAM
// =====================================================

async function retakeExam() {

    document
        .getElementById("resultSection")
        .classList.add("hidden");


    currentQuestion = 0;

    answers = [];

    questions = [];

    currentExam = null;


    await loadAvailableExam();


    document
        .getElementById("examStartBox")
        .classList.remove("hidden");


    document
        .getElementById("examStartBox")
        .scrollIntoView({
            behavior: "smooth"
        });

}


// =====================================================
// LOGOUT
// =====================================================

function studentLogout() {

    clearInterval(
        timerInterval
    );


    localStorage.removeItem(
        "student"
    );


    localStorage.removeItem(
        "lastResult"
    );


    student = null;

    currentExam = null;

    questions = [];

    answers = [];


    location.reload();

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