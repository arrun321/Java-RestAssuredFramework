package com.restassured.framework.constants;

/**
 * Endpoint constants for the JSONPlaceholder API.
 */
public final class Endpoints {

    private Endpoints() {}

    // Users
    public static final String USERS = "/users";
    public static final String USER_BY_ID = "/users/{id}";

    // Posts
    public static final String POSTS = "/posts";
    public static final String POST_BY_ID = "/posts/{id}";
    public static final String POSTS_BY_USER = "/posts?userId={userId}";

    // Comments
    public static final String COMMENTS = "/comments";
    public static final String COMMENT_BY_ID = "/comments/{id}";
    public static final String COMMENTS_BY_POST = "/comments?postId={postId}";

    // Todos
    public static final String TODOS = "/todos";
    public static final String TODO_BY_ID = "/todos/{id}";

    // ─── Course Enrollment API ────────────────────────────────────────────────

    // Status
    public static final String CE_STATUS = "/status";

    // Auth
    public static final String CE_INSTRUCTOR_LOGIN = "/instructor/login";
    public static final String CE_STUDENT_LOGIN = "/student/login";

    // Courses
    public static final String CE_COURSES = "/courses";
    public static final String CE_COURSE_BY_ID = "/courses/{id}";
    public static final String CE_COURSES_ALL = "/courses/all";
    public static final String CE_COURSES_BY_TITLE = "/courses/title/{title}";
    public static final String CE_COURSES_BY_INSTRUCTOR = "/courses/instructor/{instructor}";
    public static final String CE_COURSES_AVAILABILITY = "/courses/availability/{courseCode}";

    // Enrolments
    public static final String CE_ENROL = "/enrolments/enrol";
    public static final String CE_DROP = "/enrolments/drop";
    public static final String CE_HISTORY = "/enrolments/history";
    public static final String CE_ACTIVE_ENROLMENTS = "/enrolments/active";
}
