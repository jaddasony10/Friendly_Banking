# Friendly_Banking
Friendly Banking Web App is a Java-based online banking system with a clean, responsive design. Users log in with default credentials, then verify account number and PIN for actions like viewing balance, depositing, or withdrawing. It also records a full transaction history with smooth, user-friendly animations.

📖 Project Description – Friendly Banking Web App

This project is a web-based banking application built entirely in Java using the lightweight com.sun.net.httpserver.HttpServer. Instead of using traditional frameworks like Spring Boot, it runs on a simple embedded HTTP server, making it minimal yet functional.

The web app is styled with a modern, user-friendly design, providing a clean, full-page responsive interface with smooth animations.

🔑 Features:
	1.	Login System
	•	Users must log in with username (bank) and password (123) to access the system.
	•	Once logged in, before performing banking actions, users must verify their Account Number (1234567890) and PIN (123321).
	2.	Banking Operations
	•	View Balance – Displays the user’s current balance in a stylish card format.
	•	Deposit Money – Users can deposit money into their account via a form.
	•	Withdraw Money – Allows withdrawal if sufficient balance is available.
	3.	Transaction History
	•	Keeps a record of all deposits and withdrawals.
	•	History is displayed in a clear, user-friendly list with timestamps.
	4.	UI / UX Design
	•	Friendly, minimal, and professional interface.
	•	Smooth hover and fade animations for buttons and links.
	•	Responsive, full-page layout that works on all devices.
	5.	Security Checks
	•	After login, every critical action (Balance, Deposit, Withdraw) asks for Account Number & PIN verification before showing or modifying data.

⸻

🎯 Why This Project?

This project is designed as a beginner-friendly full-stack concept where the backend and frontend are tightly integrated in Java. It demonstrates how to:
	•	Handle HTTP requests in Java
	•	Create a mini web server without external frameworks
	•	Build session-like login systems
	•	Add transaction logic (balance, deposits, withdrawals, history)
	•	Design a user-friendly UI for easy banking operations




Credentials
	•	Login:
Username → bank
Password → 123
	•	Account Verification:
Account Number → 1234567890
PIN → 123321
