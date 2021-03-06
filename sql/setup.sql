
CREATE DATABASE onlineclassroom; -- OWNER = mlewis as an option on personal machine.

/*
 * The goal is to have Google login be the primary form of login be a Google login. I'm allowing a password field as the
 * backup. I'm going to store passwords properly for this application so they are slated and hashed.
 */
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  email varchar(20) NOT NULL,
  password varchar(200) NOT NULL,
  instructor boolean NOT NULL
);

CREATE TABLE course (
  id SERIAL PRIMARY KEY,
  name varchar(20) NOT NULL,
  semester varchar(4) NOT NULL,
  section int NOT NULL,
  active boolean NOT NULL
);

CREATE TABLE grade_formula (
  id SERIAL PRIMARY KEY,
  courseid int NOT NULL REFERENCES course(id) ON DELETE CASCADE,
  grade_group varchar(20) NOT NULL,
  formula varchar(1000) NOT NULL
);

CREATE TABLE user_course_assoc (
  userid int NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  courseid int NOT NULL REFERENCES course(id) ON DELETE CASCADE,
  time_multiplier double precision NOT NULL,
  PRIMARY KEY (userid, courseid)
);

CREATE TABLE assessment (
  id SERIAL PRIMARY KEY,
  name varchar(30) NOT NULL,
  description varchar(2000) NOT NULL,
  auto_grade int NOT NULL,
  creatorid int NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

/*
 * This associates an assessment with a course. The time values are all allowed to be null
 * because they are optional. Most things will have one or two of them specified, but none
 * are required. Students can't view the assessment until the start time and they can't
 * submit after the end time. Once they start, they have to be done in the allotted time
 * limit.
 */
CREATE TABLE assessment_course_assoc (
  id SERIAL PRIMARY KEY,
  courseid int NOT NULL REFERENCES course(id) ON DELETE CASCADE,
  assessmentid int NOT NULL REFERENCES assessment(id) ON DELETE CASCADE,
  points int NOT NULL,
  grade_group varchar(20) NOT NULL,
  auto_grade int NOT NULL,
  start_time timestamp,
  end_time timestamp,
  time_limit int /* minutes that they have to work on this once they start. */
);

CREATE TABLE problem (
  id SERIAL PRIMARY KEY,
  spec varchar(20000) NOT NULL,
  creatorid int NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

/*
 * This associates a problem to an assessment.
 */
CREATE TABLE problem_assessment_assoc (
  id SERIAL PRIMARY KEY,
  assessmentid int NOT NULL REFERENCES assessment(id) ON DELETE CASCADE,
  problemid int NOT NULL REFERENCES problem(id) ON DELETE CASCADE,
  weight double precision NOT NULL,
  extra_credit boolean NOT NULL
);

CREATE TABLE answer (
  id SERIAL PRIMARY KEY,
  userid int NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  courseid int NOT NULL REFERENCES course(id) ON DELETE CASCADE,
  paaid int NOT NULL REFERENCES problem_assessment_assoc(id) ON DELETE CASCADE,
  submit_time timestamp NOT NULL,
  details varchar(20000) NOT NULL
);

CREATE TABLE answer_grade (
  id SERIAL PRIMARY KEY,
  userid int NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  courseid int NOT NULL REFERENCES course(id) ON DELETE CASCADE,
  paaid int NOT NULL REFERENCES problem_assessment_assoc(id) ON DELETE CASCADE,
  percent_correct double precision NOT NULL,
  comments varchar(200) NOT NULL
);

CREATE TABLE assessment_start_time (
  id SERIAL PRIMARY KEY,
  userid int NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  acaid int NOT NULL REFERENCES assessment_course_assoc(id) ON DELETE CASCADE,
  time_started timestamp NOT NULL
);


/********************************************************/
/* This code is an old version of answer_grade and what */
/* was written to correct it.                           */
/********************************************************/
CREATE TABLE answer_grade (
  id SERIAL PRIMARY KEY,
  answerid int NOT NULL REFERENCES answer(id) ON DELETE CASCADE,
  percent_correct double precision NOT NULL,
  comments varchar(200) NOT NULL
);

CREATE TABLE answer_grade_copy (
  id SERIAL PRIMARY KEY,
  userid int NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  courseid int NOT NULL REFERENCES course(id) ON DELETE CASCADE,
  paaid int NOT NULL REFERENCES problem_assessment_assoc(id) ON DELETE CASCADE,
  percent_correct double precision NOT NULL,
  comments varchar(200) NOT NULL
);

INSERT INTO answer_grade_copy SELECT answer_grade.id, answer.userid, answer.courseid, answer.paaid, answer_grade.percent_correct, answer_grade.comments
  FROM answer, answer_grade WHERE answer.id=answer_grade.answerid;
ALTER TABLE answer_grade RENAME TO answer_grade_old;
ALTER TABLE answer_grade_copy RENAME TO answer_grade;

DROP TABLE answer_grade_old;

/**
 * Adding an active flag to the courses so I can prevent students from seeing questions in old courses.
 */
ALTER TABLE course ADD COLUMN active boolean;
UPDATE course SET active = true;
ALTER TABLE course ALTER COLUMN active SET NOT NULL;
