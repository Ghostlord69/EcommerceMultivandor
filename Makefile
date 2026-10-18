.PHONY: jar frontend-build test up down run-api run-web

jar:
	cd backend && ./gradlew bootJar

frontend-build:
	cd frontend && npm install && npm run build

test:
	cd backend && ./gradlew test

up: jar frontend-build
	docker compose up --build -d

down:
	docker compose down

run-api:
	cd backend && ./gradlew bootRun

run-web:
	cd frontend && npm run dev
