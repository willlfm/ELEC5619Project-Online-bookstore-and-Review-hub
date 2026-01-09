# ELEC5619-Practical2-Group-5

# Online Bookstore & Review Hub

## 1. Project overview

This project delivers a complete full-stack bookstore management platform built with a Spring Boot 3 backend and a React 19 TypeScript frontend. It integrates modern web development practices, including secure authentication, real-time data management, community engagement features, automated testing, monitoring, and containerization, ensuring both scalability and maintainability.

## 2. Key Features

### 2.1 Book Main System

-  **User Authentication & Profile Management** — Sign up，Secure sign in, profile editing and reset password.
-  **Book Browsing & Search** — Filter by genre, author, or keyword.
-  **Book Reservation** — Allow users to reserve specific books to read.
-  **Shopping Cart & Secure Checkout** — Support for both e-books and physical copies.  
-  **Order Tracking** — Users can view their order history and apply for refunds.
-  **Refund Tracking** — Users can view their refund requests and status, and can also cancel refund requests.
-  **Ebook Tracking** — Users can view and download the ebooks they have purchased.

### 2.2. Reservation Management

-  **user can reserve books on their preferred date/time** — within next 7 days(8am-6pm, 2-hour intervals).
-  **availability notifications** — notify users when the reservation status was changed.
-  **follow the status logic** — user only can change the reservation status from 'reserve' to 'reservation cancelled'; Admin change the status need to follow the logic: 
    - reserve->reservation cancelled
    - reserve->picked->returned
    - reserve->picked->warning
    - warning->returned

### 2.3. Community Reviews

-  **Review & Rating** — Allows for book reviews and ratings.
-  **Community bestseller books** — Display books with the highest ratings.
-  **Reader's Column** — Display recent reviews within the community.

### 2.4. Management

-  **Book Management** — Update book item information and upload E-book files.
-  **Account Management** — The manager can manage users' information and add users.
-  **Review & Rating Management** (Comment Moderation)— The manager can delete inappropriate comments.
-  **Sales Statistics** — Sales Volume, monthly revenue.
-  **Order Management** — Check fulfilment pipelines, update delivery progress, review refunds, and keep customers informed.
-  **Reservation Management** — Monitor book reservations, update status, and notify users of changes.
-  **Feedback Tracking and Notification** — View and manage user feedback and issues (resolve supported).
-  **Announcements & Notifications** — Communicate with staff and customers.

## 3. Technology Stack

### 3.1. Back-end

- **Framework**: Spring Boot(Java)
- **Language**: Java 17, Maven
- **Data access and storage**: MySQL, Spring Data JPA
- **Security**: Spring Security, Bean Validation, JWT
- **Testing**: JUnit 5, Mockito, Spring Boot Starter Test, Spring Security Test
- **Web and Interfaces**: Spring Boot Web (MVC), Spring WebFlux

### 3.2. **Front-end**

- **Framework**: Vite + React 19.1.1
- **Language**: TypeScript
- **Forms**: React Hook Form
- **State Management**: React Hooks + Context

## 4. High-Level Goals

- **Enhance User Experience and Engagement**: 
    Foster interaction and loyalty through reviews and ratings.
- **Improve Inventory and Operational Efficiency**: 
    Updated in real-time to support accurate decision-making.
- **Ensure Transaction and Content Security**: 
    Provide secure authentication and checkout processes to protect user accounts and payment information.

## 5. Roles & Responsibilities

- **Project Manager-(Hongwei Xu)**：
    Oversees sprints, manages GitHub/JIRA, conducts reviews, ensures timely delivery of documentation and demos.

- **Backend Developer(Xiang Zhou & Fengming Lin)**：
    Develops REST APIs (book, reservation, review, admin), handles security, writes tests, documents APIs.

- **Frontend Developer(Wenhao Wang & Fengming Lin)**：
    Implements React UI, form validation, API integration, responsive design, writes frontend tests.

- **Database & DevOps Engineer(Lele Zhao & Xiang Zhou)**：
    Designs DB schema, sets up migrations, Docker dev/prod environments, configures CI/CD and monitoring.

- **QA/Testing Engineer(Hongwei Xu)**：
    Writes test cases, executes manual/E2E tests, ensures test coverage, tracks bugs and regression fixes.

- **Documentation Specialist(Wenhao Wang)**：
    Prepares README, API docs, developer guides, sprint summaries, and maintains documentation coherence.

- **More following distributions of work are in JIRA: https://uni-team-fu071ucl.atlassian.net/jira/software/projects/SMS/boards/1**

## 6. Payment test accounts


Sandbox URL: https://sandbox.paypal.com

Store account: 

- Client ID: ATqXEU_-wixDaZq64240QLOfyBWBzyYCa8RMEyF-KpBji7tTQsfjnvvPOXA1b3VItY6T3dUQg_RuEf5z
- Secret: EHblYba_oVz3GR_MqoCh30ZW_77GEOfR09hbkLdQD9VNurcxU14kwczAs0a5sm-Mmo_oM8Ne2Q1U8vfk
- Email: sb-54352x46747377_api1.business.example.com
- Password: 1udb>5<Z
- Name: John Doe
- Phone: 0366170649
- Account ID: 7C6NE7NPH8ZCA


Client account:

- Email: sb-43ptxq46571772@personal.example.com
- Password: Qt2/JMo!
- Name: John Doe
- Phone: 0363365630
- Account ID: M3ZXZMJYG4HDG