# FriendFinderSFU
A friend-finding (and more!) app dedicated to the SFU community. CMPT 276 group projects 3.

## Table of Contents

- Abstract
- The problem our App solves
- Currently available solutions
- Customers' needs
- Target Audience
- Competitve Analysis
- Our app's value
- List of Epics
- Group members
- Citations and Acknowledgements
- **User Stories and Use Cases**

## Abstract

Our app is a hybrid and innovative university-specific social app which acts as a dating app, a friend finding app, and an academic study buddy (study partner) app. Specifically, users will have the option to use any of these features by creating profiles customized for each, and then getting matched to other users who share their interests or meet their specified criteria.

## The problem our App solves

Meeting people in a large university is a challenge for everyone, especially new students. New students (and sometimes even upper-year students) often have to meet lots of people before they find their community, which in itself can be difficult and at times very awkward. While all universities have this problem to some extent, SFU in particular is famous for having a poor social environment.

Our app is designed to help mitigate this problem by providing an easy, fast, and organized way to meet people within the university, whether it be for making new friends, finding a romantic partner, or finding a study buddy.

## Currently Available Solutions

The closest existing public-facing solution is **Bumble**[1], which is a hybrid dating/friendship app. It has features such as friend-finding, dating, and clubs. There are many other dating apps as well, mostly focusing on dating. There are also some university-specific solutions such as **College Mixer** for Western University[2]. However, most of these solutions focus solely on dating, and not friendships or non-romantic relationships.

## Customers’ Needs (projected)

1. An easy and fast way to meet new friends, study buddies, or romantic partners online within the university.  
2. A way to find people who share their interests (and possibly preferences), which can be difficult in a large university or for people with niche hobbies and interests.  
3. A way to stay safe from dating-app-related online threats, such as harassment and “catfishing”.  
4. A secure communication channel with potential new friends or partners which is exclusive to a trusted community (university community) and minimizes risks of online harassment.

## Target Audience

Our app is exclusively targeted toward **SFU students**, with the possibility of having versions for other universities as well in the future. We will focus on single-university instances of the application (no cross-university memberships) since the goal is for students to form lasting friendships and relationships within their home university. There are also potential security advantages with this approach as users will have to prove their student status, which both puts an initial filter on potential bad actors and enables additional resources (for example the university board of student conduct) in case issues occur.

## Competitive Analysis (SWOT)

Strengths:

1. Our app will be developed by SFU students, for SFU students, boosting acceptance and fitness to the task as we can relate to many issues students face.  
2. If connected to SFU’s CAS server, our app needs very little support/maintenance to function.  
3. Our app will be using the SFU computing id and email so it should limit spam accounts and catfishing.

Weaknesses:

1. Our app would need lots of users to be potentially profitable as a SaaS.  
2. Our app would be accessible on web only in the first release (most dating apps have mobile versions)  
3. Compared to social media which has a lot more features and a larger user group, our app will need to start from zero.

Opportunities:

1. There are no direct competitors within the SFU community (and many other universities)  
2. There is an atmosphere of risk in public opinion around public-facing dating apps.

Threats:

1. Public-facing dating apps have been around for a long time and already have many users.  
2. The study buddy finding feature in our app is very new, with little data on whether it will be met with enthusiasm by university students in the context of a larger social app.

## Our App’s Value

Our app provides near equal focus on romantic relationships, friendships, and study buddies among university students. Most similar apps focus on dating only, which ignores the growing needs for social community finding specially for new students.

Additionally, our app provides a host of features such as deterministic (score based) matching, text chats, voice and video calls, event/date planning, and more, to meet the needs of all users.

## List of Epics/Features 

- Profiles and Questionnair: Three interleaved profiles built using a ~20-question questionnair, divided into three parts (dating, friendship, study buddies) with the possibility of disabling each profile. Includes questions about interests, preferences, exclusions, and academic experiences
- Score/Match function: used to establish a partial order on users given their profile properties set (e.g., interests, preferences, skills (for study buddies), etc.) to facilitate match-making and suggestions.  
- Feeds: Allow users to see profiles matched to their profile and send expressions of interest.  
- Chat and virtual meeting features: Individual chats with security features (e.g., blocking, no media/photo sharing) and voice/video calls (outsourced \- using **APIs** of either Zoom, BigBlueButton, or similar solution)  
  - APIs will be used to obtain meeting join links once a user initiates or joins a call, and involve sending the user's display name to the API.
- Login and CAS Integration: app allows logging in with a CAS server (with the ultimate goal being the SFU CAS server), using a username and password, or both.  
- Administration (minimal): panel that allows admins to view and suspend/edit/delete users.
- Profile (global) optimization: Users can edit their profile adding pictures, biography, and description \- not directly used for matching (with some automated moderation)


## Group Members and Expertise

- David (left team July 3rd, 2026) 
  - 2nd-year Computer Science student.  
  - Familiar with C++. and Java.  
  - Comfortable with backend development


- Parsa  
  - 2nd-year Computer Science student at Simon Fraser University.  
  - Experienced with Python and familiar with C, C++, Haskell, and Java.  
  - Comfortable with both frontend and backend development.  
  - Strong interest in frontend development and user interface design.  
      
- Pravit  
  - Second-year Computer Science student at Simon Fraser University with hands-on experience in full-stack development, machine learning, and software testing. Proficient in Python, Java, JavaScript, and SQL with practical experience building production-grade ML pipelines (PyTorch, scikit-learn, Pandas) and modern web apps (React, Next.js, FastAPI, Flask, Streamlit).   
      
- Taha  
  - 1st year CS student  
  - Some experience with PHP and Django  
  - Proficient in Java, C++, and Python  
- William  
  - 3rd year SOSY student  
  - Main coding language is C++, experience in Python, Java, SQL  
  - Preference for backend coding


## Works Cited (this file)

[1] M. Zhao, “Review: Swiping right on College Mixer, the dating app for Western students,” *The Gazette • Western University’s Student Newspaper*, Feb. 12, 2024\. https://westerngazette.ca/culture/student\_life/review-swiping-right-on-college-mixer-the-dating-app-for-western-students/article\_c73dce46-c9c2-11ee-90c4-27de88c99ac6.html (accessed Jun. 19, 2026).  
[2]	Bumble, “Bumble \- Date, Meet, Network Better,” *Bumble*, 2023\. https://bumble.com/ (accessed Jun. 19, 2026).  

## Other Project Acknowledgements
See docs/DECLARATIONS.md

# User Stories

## Case: Auth-wall
**Personas/Actors**
1. Primary actor: Jane - a random person

**Pre-conditions**
None

**Actions/Triggers**
Jane attempts to go to the app's dashboard or other authenticated page by opening the app URL.

**Acceptance Criteria**
- If Jane has an active session (has logged in before), she must be able to view the dashboard or requested page
- If Jane has not yet logged in, she should be redirected to the login page

**Post-conditions**
- If Jane has not logged in, she should not be served any protected information from the database (other users' profiles, etc)

**Non-functional requirements**
- An average user should be able to understand why they have been redirected to the login page through the UX
- All pages should load in less than one second

**Tests**
- An unauthenticated user sending a request to the application root (dashboard) should be redirected to `/login`
- An authenticated user sending a request to the application root should get a 200-level result code and should not be redirected.

## Case: Sign-up
**Personas/Actors**
1. Primary actor: Mike - Second-year SFU student looking to meet new friends
2. Secondary actor: Jane - Second-year SFU student already using the app to find new friends

**Pre-conditions**
- Jane must have an existing account in the App

**Actions/Triggers**
Mike opens the app URL and must be redirected to the login page. Then, he clicks the sign-up link in the login page and is redirected to the sign-up page where he is asked for his first and last name, email address, a chosen password repeated twice, and his gender. He is also asked to accept the terms of use.

Mike then enters his information and clicks Submit to create his account.

**Acceptance Criteria**
- If Mike enters any string as their first and last name, a valid and not previously used valid email address, matching passwords in the two password fields, a valid dropdown item for gender, and accepts the terms of use by checking the applicable checkbox, then his account must be registered and he must be redirected to the login page with a success message.
- If any of the fields are left empty once the form is submitted, mike should be redirected back to the signup page with an error message
- If Mike enters an invalid email, or enters Jane's email (or any already registered user's email), then he should be redirected back to the signup page with an error message

**Post-conditions**
- If Mike's input is accepted, then a database record of his new account must be created.
- Otherwise, no new records should be entered in the database

**Non-functional requirements**
- New passwords should be hashed prior to being saved
- All pages should load in less than one second

**Tests**
- The input {Mike, Brown, mike@sfu.ca, 1234, 1234, male, true} should result in a record creation and redirection to the sign-in page with a success message.
- An input missing the last name field should be rejected and redirected back to the sign-up page with an error message
- Using Jane's email address with the first test scenario should be rejected and redirected back to the sign-up page with an error message
- Using the email address "mikesfuca" in the first test scenario be rejected and redirected back to the sign-up page with an error message


## Case: Log in - regular user
**Personas/Actors**
1. Primary actor: John - a Second-year SFU student

**Pre-conditions**
- John must have an existing account in the app with a non-admin role

**Actions/Triggers**
John opens the app URL, and is redirected to the login page with two inputs: email and password, as well as a Log in button. 

He then enters his username and password and clicks on the Log in button.

**Acceptance Criteria**
- If John uses the email and password associated with his account correctly, he should be redirected to the dashboard page
- If John enters the wrong password, he should be redirected back to the login page with an error message
- If John enters the wrong email address, or both a wrong email address and the wrong password, then he should be redirected back to the login page with an error message.

**Post-conditions**
- If John logs in successfully, his session variables must reflect his user ID.
- If John's login attempt is unsuccessful, then his session variables should not be modified.

**Non-functional requirements**
- All pages should load in less than one second

**Tests**
- The correct email address and password should produce a redirect to the dashboard page, and result in a session variable creation
- Any of wrong password, wrong email, or both, should result redirection back to the login page. No session variable should be set.

## Case: Questionnair Completion
**Personas/Actors**
1. Primary actor: Ryan - a second-year SFU student looking to make friends

**Pre-conditions**
- Ryan must have an active account and must be logged in
- Ryan must be on the questionnair page

**Actions/Triggers**
Ryan fills out the required fields and clicks "Submit"

**Acceptance Criteria**
- If all required fields have been filled, the form must be submitted and a success message must be displayed
- If there are missing fields, Ryan should be redirected back to the questionnair with an error message
**Post-conditions**
- If there are no missing fields, the matching profile for Ryan must be created or updated

**Non-functional requirements**
- All pages should load in less than one second
- Errors must be easy to understand

**Tests**
- A fully complete questionnair must be accepted and result in a record update/creation
- If the user has indicated that they would like to have a friendship profile and then leaves friendship questions empty, their input must be rejected and they should get an error message.

## Case: Questionnair Loading
**Personas/Actors**
1. Primary actor: Ryan - a second-year SFU student looking to make friends

**Pre-conditions**
- Ryan must have an active account and must be logged in

**Actions/Triggers**
Ryan clicks on the questionnair link from the menu in any other page of the app

**Acceptance Criteria**
- If Ryan has previously completed the survey, he should see a form pre-filled with his previous answers
- If Ryan has not previously completed the survey, he should see an empty form
**Post-conditions**
- None

**Non-functional requirements**
- All pages should load in less than one second

**Tests**
- A user with an existing questionnair/matching profile record must see their information pre-filled in the form
- A user who has not yet completed the survey must see an empty form


## Case: Profile Viewing
**Personas/Actors**
1. Primary actor: Joyce - a second-year SFU student

**Pre-conditions**
- Joyce must have an active account and must be logged in
- Joyce must have completed the profile questionnair

**Actions/Triggers**
Joyce opens the profile page for a user by opening a URL sent to them by a friend (in the future this will also happen through feeds)

**Acceptance Criteria**
- If the user exists, but they have not yet completed the profile, then Joyce must see only their name and gender.
- If the user exists and has completed the questionnair, then Joyce must see their answers to the questionnair questions as well as their name and gender, subject to that user's preferences for displaying friendship, dating, or study-buddy-related profile sections.
- If the user does not exist, then a 404 error page must be displayed to Joyce
- If the user exists, has completed their profile, and is not Joyce herself, then a friendship match percentage must be displayed ranging from 0% to 102%
**Post-conditions**
- None

**Non-functional requirements**
- All pages should load in less than one second

**Tests**
- The profile URL for a non-existing user must result in a 404 error.
- An existing user's profile URL should not return a 404 error, even if the questionnair is not completed
- An existing user's profile should display their 5 selected hobbies if they have completed the questionnair