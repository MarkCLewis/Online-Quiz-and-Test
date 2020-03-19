
CREATE DATABASE onlineclassroom; -- OWNER = mlewis as an option on personal machine.

/*
 * The goal is to have Google login be the primary form of login be a Google login. I'm allowing a password field as the
 * backup. I'm going to store passwords properly for this application so they are slated and hashed.
 */
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  email varchar(20) NOT NULL,
  password varchar(200)
);

CREATE TABLE course (
  id SERIAL PRIMARY KEY,
  name varchar(20) NOT NULL,
  semester varchar(4) NOT NULL,
  section int NOT NULL
);

CREATE TABLE user_course_assoc (
  userid int REFERENCES users(id) ON DELETE CASCADE,
  courseid int REFERENCES course(id) ON DELETE CASCADE,
  time_multiplier double precision NOT NULL
);

CREATE TABLE assessment (
  id SERIAL PRIMARY KEY,
  name varchar(30) NOT NULL,
  description varchar(2000) NOT NULL,
  points int NOT NULL,
  auto_grade boolean NOT NULL
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
  courseid int REFERENCES course(id) ON DELETE CASCADE,
  assessmentid int REFERENCES assessment(id) ON DELETE CASCADE,
  auto_grade int NOT NULL,
  start_time timestamp,
  end_time timestamp,
  time_limit int /* minutes that they have to work on this once they start. */
);

CREATE TABLE problem (
  id SERIAL PRIMARY KEY,
  spec json NOT NULL
);

/*
 * This associates a problem to an assessment.
 */
CREATE TABLE problem_assessment_assoc (
  id SERIAL PRIMARY KEY,
  assessmentid int REFERENCES assessment(id) ON DELETE CASCADE,
  problemid int REFERENCES problem(id) ON DELETE CASCADE,
  weight double precision NOT NULL
);

CREATE TABLE answer (
  id SERIAL PRIMARY KEY,
  userid int REFERENCES users(id) ON DELETE CASCADE,
  courseid int REFERENCES course(id) ON DELETE CASCADE,
  assessmentid int REFERENCES assessment(id) ON DELETE CASCADE,
  probid int REFERENCES problem(id) ON DELETE CASCADE,
  correct boolean NOT NULL,
  submit_time timestamp NOT NULL,
  details json NOT NULL
);