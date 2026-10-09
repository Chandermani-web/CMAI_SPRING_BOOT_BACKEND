# CM.AI backend - Spring Boot

Java 21 · Spring Boot 3.5 (MVC) · Spring Data JPA/Hibernate · MySQL · Redis · Firebase Admin · Razorpay ·
Spring AI (resume + roadmap) · LangChain4j (interviews) · Groq `openai/gpt-oss-120b`

A drop-in replacement for the old Node/Express backend: same URLs, same JSON shapes (including `_id`).

## Run

Requirements: JDK 21, Docker (for MySQL) - or any MySQL 8 you point `DB_*` at.

```bash
docker compose up -d        # MySQL on :3306, password = DB_PASSWORD in .env
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
# -> http://localhost:8000/health   {"status":"ok","service":"cmai-backend"}
```
Run it from this folder: `.env` and `serviceAccountKey.json` are read from the working directory.
Tables are created automatically on first start (`DDL_AUTO=update`).

Build a jar: `./mvnw clean package` -> `target/cmai-backend-1.0.0.jar` (`java -jar ...`). Docker image: `docker build -t cmai-backend .`

## Connect your frontend

1. Set the frontend's API base URL to `http://localhost:8000` (production: your deployed URL).
2. Every request must send cookies: axios `withCredentials: true` / fetch `credentials: "include"`.
3. Put the frontend origin in `FRONTEND_URL` in `.env` (comma-separate several). It is the CORS allow-list.
4. Cookies:
   * local dev (`localhost:5173` -> `localhost:8000`): `COOKIE_SECURE=false`, `COOKIE_SAME_SITE=Lax` (already set)
   * production on different domains over HTTPS: `COOKIE_SECURE=true`, `COOKIE_SAME_SITE=None`, `APP_ENV=production`

## Config (`.env`)

| Variable | Purpose |
|---|---|
| `PORT`, `FRONTEND_URL`, `APP_ENV` | server |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | MySQL (**placeholders - your old project only had Mongo**) |
| `REDIS_URL` | built from your Upstash token (`rediss://default:<token>@<host>:6379`) |
| `GROQ_API_KEY`, `GROQ_MODEL` | LLM |
| `YOUTUBE_API_KEY`, `YOUTUBE_BASE_URL` | roadmap course lookup |
| `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET` | payments |
| `FIREBASE_PROJECT_ID`, `FIREBASE_SERVICE_ACCOUNT_PATH` | token verification (`serviceAccountKey.json`); or set `FIREBASE_SERVICE_ACCOUNT_JSON` |
| `INTERVIEW_COIN_COST` | `0` = server does not charge; set e.g. `50` to charge in `/interview/start` |

Real environment variables override `.env`. Don't commit `.env` or `serviceAccountKey.json` (git-ignored).

## API (unchanged paths)

`GET /health` · `POST /api/auth/login|logout|use-coins|add-coins` · `GET /api/me` ·
`POST /api/resume/upload` (field `resume`), `GET /api/resume/get-resume` ·
`POST /api/interview/start|answer`, `GET /api/interview/all-interview`, `GET /api/interview/{id}` ·
`POST /api/roadmap`, `GET /api/roadmap/all`, `GET /api/roadmap/{id}` ·
`POST /api/billing/create-order|verify-payment`, new `GET /api/billing/plans`

## Differences you should know about

* **`/api/auth/add-coins` is now a no-op** (returns the balance). The old one let any user give themselves unlimited coins.
* **`verify-payment` now credits the coins** (300 for the Starter plan) exactly once. It never did before - if your frontend calls `add-coins` after paying, that call is now harmless and you can delete it. `create-order` also returns `keyId`.
* The interview summary now includes the last answer's feedback (it was generated one step too early before).
* IDs are numbers (still named `_id`, also `id`) instead of Mongo ObjectIds.
* `POST /api/interview/start` and `/api/roadmap` read your resume from the database when `useResume` is true (the `resume` in the body is ignored).
* `GET /api/auth/..` errors use `{success:false,message}`; resume `.docx/.doc` upload works now too.

## Layout

```
src/main/java/com/cmai
  controller/   REST controllers (HTTP only)
  service/      business logic (auth, coins, billing, interview, resume, roadmap, cache, session)
  repository/   Spring Data JPA repositories
  entity/       JPA entities
  dto/          request/response records
  ai/           Spring AI + LangChain4j agents, prompt loader, JSON repair parser
  config/       properties, Firebase, Razorpay, AI, CORS/interceptors
  web/          session auth, Redis rate limit, origin check interceptors
  exception/    ApiException + global handler
src/main/resources/application.yml, prompts/*.txt
```
"# CMAI_SPRING_BOOT_BACKEND" 
