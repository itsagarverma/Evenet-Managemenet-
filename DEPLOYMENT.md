# The Sneh Moments deployment runbook

This runbook reflects the checked-in configuration. The repository is on `master` with remote `origin` at `https://github.com/itsagarverma/Evenet-Managemenet-.git`. GitHub Actions runs CI only; it does not publish either app. The frontend is configured for Netlify and its production API URL is `https://evenet-managemenet.onrender.com` on Render. Provider dashboard access, the Netlify site assignment, and the production database account are not available from this repository.

## 1. Local prerequisites

- Java 21 and Maven 3.9+
- Node.js 22 and npm
- Docker Compose v2 or the installed `docker-compose` command
- PostgreSQL client tools (`pg_dump`, `pg_restore`, and `psql`) for backup and database checks

The Docker Compose file starts PostgreSQL 15 only. Set `POSTGRES_PASSWORD` before starting it. The backend uses `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`; there is no committed database password fallback.

```bash
POSTGRES_PASSWORD='a-local-only-password' docker compose up -d db
```

## 2. Frontend build and tests

```bash
cd frontend
npm ci
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```

The production build writes to `frontend/dist/sneh-moments-frontend/browser`. Google Fonts remain loaded by the browser; Angular does not fetch them during the build.

## 3. Backend build and tests

```bash
cd backend
mvn verify
```

Tests use H2 and do not verify the production PostgreSQL connection or migration. To build the production container from the repository root:

```bash
docker build -t sneh-moments-backend ./backend
```

The container runs the packaged Spring Boot jar on port 8080 (or the platform-provided `PORT`). Flyway runs at application startup.

## 4. Backend environment variables

Set these in the backend host’s secret/configuration dashboard. Never put secret values in this file, Docker Compose, Angular source, or Git.

| Variable | Purpose |
|---|---|
| `DATABASE_URL` | Combined PostgreSQL URL where the host supplies one; takes precedence over the split values |
| `DB_URL` | JDBC PostgreSQL URL when not using `DATABASE_URL` |
| `DB_USERNAME`, `DB_PASSWORD` | Database credentials when using `DB_URL` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated exact frontend origins, including scheme |
| `ADMIN_BOOTSTRAP_EMAIL`, `ADMIN_BOOTSTRAP_PASSWORD` | One-time first admin creation; password must be at least 12 characters |
| `SECURE_COOKIES=true` | Secure/SameSite=None CSRF cookie for HTTPS cross-site frontend/API |
| `SESSION_COOKIE_SECURE=true` | Secure session cookie on HTTPS |
| `SESSION_COOKIE_SAME_SITE=None` | Cross-site session cookie when frontend/API are on separate sites over HTTPS |
| `UPLOAD_DIRECTORY` | Absolute path to durable image storage mounted into the backend |
| `BREVO_API_KEY`, `BREVO_SENDER_EMAIL` | Brevo notification configuration |
| `NOTIFICATION_EMAIL` | Optional enquiry notification recipient; defaults to the sender email |

After first startup creates the admin, remove the bootstrap variables. The admin password can be changed in Admin → Settings. Password reset is not implemented; it needs verified email delivery plus expiring single-use reset tokens.

## 5. Database backup, migration, and verification

Do not deploy a schema change until a current backup has completed and its archive can be read. Use the database provider’s managed snapshot feature when available, and retain the snapshot identifier with the release record. For a portable PostgreSQL backup, configure the database connection variables in the shell or a protected `.pgpass` file (mode `0600`); do not put passwords in the command line or shell history.

```bash
export PGHOST='database-host'
export PGPORT='5432'
export PGDATABASE='database-name'
export PGUSER='database-user'
export BACKUP_FILE="$PWD/sneh-moments-before-release-$(date -u +%Y%m%dT%H%M%SZ).dump"
pg_dump --format=custom --no-owner --no-acl --file="$BACKUP_FILE" --host="$PGHOST" --port="$PGPORT" --username="$PGUSER" --dbname="$PGDATABASE"
pg_restore --list "$BACKUP_FILE" >/dev/null
```

Get connection values from the authorized database dashboard; do not guess them. Store the archive outside the app container and outside its image disk. For restore validation, restore into a separate temporary PostgreSQL database and inspect it before relying on the archive. Never restore over production as a verification step.

The checked-in Flyway history contains `V1__foundation_schema.sql` plus `V2__contact_settings.sql`. V2 adds the singleton contact settings table. Flyway is configured to baseline an existing schema at version 0; the DDL is additive and does not drop or truncate data. Production migration is performed automatically at backend startup. Before that startup:

1. Confirm the selected database is the intended production database and inspect its current schema/data.
2. Take and verify the backup above (or provider snapshot).
3. Deploy the backend with `DATABASE_URL` or split `DB_*` values and inspect startup logs for Flyway migration success and Hibernate schema validation.
4. Verify existing enquiry rows remain available and verify the contact settings endpoint.
5. Keep the backup until post-deploy checks pass. If startup validation fails, roll back the app image and restore only through a reviewed recovery plan.

Production PostgreSQL access was not available during this repository task, so the production migration is not claimed as verified.

## 6. Durable image storage

Gallery files are stored on the backend filesystem; PostgreSQL stores the image key and gallery metadata only. The actual service disk configuration cannot be inspected from this repository. Render web service filesystems are ephemeral unless a persistent disk is attached. Before enabling real uploads, attach a persistent disk to the backend service, mount it at an absolute path, set `UPLOAD_DIRECTORY` to that mount path, and verify a test upload survives a redeploy. Retain that disk when replacing the service. If the account/plan does not support a persistent disk, uploads must remain disabled until the application is connected to an approved object store.

Do not upload real wedding photos until persistence has been confirmed in the deployed service. No object-storage account or credentials are configured in this repository.

## 7. Netlify frontend deployment

The root `netlify.toml` sets the base directory to `frontend`, runs `npm run build`, and publishes `dist/sneh-moments-frontend/browser`. `frontend/public/_redirects` is copied into that publish directory and routes browser paths to `index.html` with HTTP 200, as required for Angular history-based routes ([Netlify SPA guidance](https://docs.netlify.com/build/configure-builds/javascript-spas/), [redirect rules](https://docs.netlify.com/manage/routing/redirects/overview/)). Netlify can read build settings from this file; matching UI settings are overridden by the file ([file-based configuration](https://docs.netlify.com/build/configure-builds/file-based-configuration/)).

Connect the GitHub repository in Netlify and use the settings in section 13. The production API URL is compiled from `frontend/src/environments/environment.prod.ts`, so there are no frontend environment variables to add for the current build. The intended canonical origin is `https://thesnehmoments.in`; verify the custom domain is attached to the Netlify site before launch. Add any actual `*.netlify.app` origin to Render CORS only if that origin needs API access. Always use exact origins, never `*` with credentials.

## 8. Render backend deployment

The API URL is `https://evenet-managemenet.onrender.com`. Configure the Render Web Service from this repository with Root Directory `backend`, Docker runtime, Dockerfile path `Dockerfile`, and Docker context `.`. The container listens on Render’s `PORT` (8080 locally). Attach the intended Render PostgreSQL database and set `DATABASE_URL` from that database’s dashboard. The repository does not identify the database instance or provide dashboard access.

Set `CORS_ALLOWED_ORIGINS=https://thesnehmoments.in`, `SECURE_COOKIES=true`, `SESSION_COOKIE_SECURE=true`, and `SESSION_COOKIE_SAME_SITE=None` for the HTTPS frontend and API. If the site uses another origin, include that exact origin too. Configure a persistent disk mounted at `/var/data` and `UPLOAD_DIRECTORY=/var/data/uploads` before enabling gallery uploads. Render filesystems are ephemeral unless a persistent disk is attached; only data under the mount path persists ([persistent disk documentation](https://render.com/docs/disks), [free plan limitations](https://render.com/docs/free)). The disk attachment and survival test require the service dashboard. The checked-in GitHub workflow does not deploy the backend.

## 9. Git workflow and frontend ZIP

Review the source before manually committing and pushing. No commit or push is performed by this runbook.

```bash
git status --short
git diff
git diff --check
```

If the recipient specifically needs the source ZIP, create one updated package from the repository root (it includes frontend source/configuration, not `node_modules` or build output):

```bash
rm -f frontend-update.zip
zip -rq frontend-update.zip frontend \
  -x 'frontend/node_modules/*' 'frontend/dist/*' 'frontend/.angular/*' \
     'frontend/.env' 'frontend/.env.*' 'frontend/**/.env' 'frontend/**/.env.*'
unzip -l frontend-update.zip
```

The supplied `frontend.zip` and `backend.zip` are original input archives and should be kept separate from this generated package.

## 10. Post-deployment checks

- Confirm backend startup reports Flyway success and no Hibernate schema validation error.
- `GET /api/gallery/categories` returns published categories; direct category URLs and refresh work.
- `GET /api/testimonials` and `GET /api/services` return published rows only.
- `GET /api/contact-settings` returns the public contact fields only; anonymous `GET /api/admin/contact-settings` is denied.
- Verify login, invalid login, logout, CSRF-protected admin writes, and that `/api/enquiries` and `/queries/all` reject anonymous access.
- Submit a test enquiry and confirm it appears in the admin list and notification delivery is configured.
- Upload a disposable image, publish it, view it publicly, redeploy, and verify the same media URL still works before uploading real photos. Delete the disposable test category afterward.
- Open and refresh `/our-work/barat`, `/admin`, and `/admin/login` at the deployed frontend host.
- Verify Netlify direct navigation to those routes serves the Angular app.
- Test WhatsApp links on desktop and mobile; current configured target is `https://wa.me/919302259211`.

## 11. Rollback considerations

Keep the last known-good backend image/build and frontend bundle. Roll back application versions through the selected hosting provider’s release controls. A schema migration may be forward-compatible but cannot be assumed reversible; restore a verified database backup only after assessing writes made since deployment. Preserve the persistent image disk independently of app rollback. Do not delete or recreate the production database or upload disk as a routine rollback action.

## 12. Deployment readiness

The Netlify build and SPA fallback are checked into source, and the Render production values are documented. This does not certify a production deployment: the Netlify domain and direct-route behavior, Render persistent disk, production PostgreSQL migration, real backup/restore, and deployed browser flows require provider access and post-deployment verification. Keep real gallery uploads disabled until the disk survival check passes.

## 13. Exact Netlify settings

- Repository: `itsagarverma/Evenet-Managemenet-`
- Base directory: `frontend`
- Build command: `npm run build`
- Publish directory: `dist/sneh-moments-frontend/browser` (relative to `frontend`)
- Environment variables: none required by the current frontend build
- SPA fallback: `frontend/public/_redirects`, copied to the publish root
- Custom domain: verify `thesnehmoments.in` in the Netlify dashboard; do not assume it is assigned

## 14. Exact Render settings

- Service type: Docker Web Service
- Root directory: `backend`
- Dockerfile path: `Dockerfile`; build context: `.`
- Health check: none configured in this application; do not set an actuator path
- Database: attach/select the intended PostgreSQL instance and use its dashboard-provided `DATABASE_URL`
- Required production values: `CORS_ALLOWED_ORIGINS=https://thesnehmoments.in`, `SECURE_COOKIES=true`, `SESSION_COOKIE_SECURE=true`, `SESSION_COOKIE_SAME_SITE=None`
- Durable uploads: attach disk at `/var/data`, set `UPLOAD_DIRECTORY=/var/data/uploads`, then verify a file survives redeploy
- Conditional values: admin bootstrap credentials for first setup only; Brevo settings only when email notifications are configured

Apply Render settings and disk attachment in its dashboard. Production secrets are not stored in the repository.

## 15. Exact migration and backup procedure

1. Confirm the selected Render PostgreSQL database and inspect its existing schema and enquiry data.
2. Create a provider snapshot or run the verified `pg_dump` procedure in section 5; retain the archive outside the service disk.
3. Deploy the backend and inspect Render logs for Flyway V1/V2 completion and Hibernate validation success.
4. Check existing enquiries and public/admin contact settings. Keep the backup until all checks pass.
5. For recovery testing, restore the archive only into a separate temporary PostgreSQL database. Never test restore over production.

Flyway runs automatically at startup. The actual production migration and backup remain unverified until run against the selected database.
