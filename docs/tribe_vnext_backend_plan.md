# Tribe vNext Backend Product & Architecture Plan

## 1. Product Requirements Document (PRD) Outline

### 1.1 Vision & Goals
- Deliver a cohesive social platform that deepens community engagement through real-time interaction, discovery, and premium experiences.
- Enable scalable growth with production-grade services, observability, and extensibility for future features.

### 1.2 Personas & Needs
- **Social Explorers:** active users seeking events, new connections, and streak-based engagement.
- **Club Organizers:** need tools for communication, content distribution, and premium monetization.
- **Premium Members:** desire enhanced visibility, exclusive access, and seamless payments.
- **City Curators:** maintain must-visit lists, require media uploads and moderation.

### 1.3 Feature Pillars & Functional Requirements
1. **Social Core**
   - 1:1 and group chat (event/club scoped) with persistence, moderation, read receipts, typing indicators.
   - Daily Tribe Feed with updates from friends, clubs, and recommendations (includes text, media, event cards).
   - Icebreaker prompts for new matches or recommended people (rotating daily prompts). 
2. **Gamification**
   - Daily streak tracking (check-in, RSVP, message interactions) with notifications for streak preservation.
   - Badge system tied to milestones (events attended, clubs joined, contributions).
   - Leaderboards per city/category to spur engagement.
3. **Discovery**
   - Personalized recommendations for events, clubs, and people using interest graphs, activity, and location.
   - Map view overlay for events and must-visit locations with filters (date, category, premium).
4. **Utility**
   - RSVP workflow with capacity management and waitlists, plus Google/Apple calendar sync.
   - Push notifications for RSVP confirmations, chat, streak reminders, and premium offers.
   - Media uploads (photos, reels) tied to events/clubs/feed with moderation tooling.
5. **Premium Layer**
   - Visibility boosts for events/users (time-boxed prominence in feeds/search).
   - Exclusive clubs/events gated by subscription tiers.
   - Paid ticketing and recurring subscription support via Stripe (or similar) integration.

### 1.4 Non-Functional Requirements
- Availability target 99.9%; chat latency <200ms in-region.
- Scalability to 5x current peak users via horizontal scaling and asynchronous pipelines.
- Compliance: GDPR-ready data handling, PCI scope limited to payment processor.
- Observability: end-to-end tracing, metrics, structured logging.

### 1.5 Success Metrics
- 30% increase in DAU, 20% growth in premium conversions within 6 months.
- 80% of active users interacting with chat weekly.
- 90% of RSVPs synced to external calendars within 1 hour.

## 2. REST API Specifications (JSON)
> All endpoints require `Authorization: Bearer <JWT>` except for account creation/login. Responses use snake_case. Pagination follows cursor-based pattern where applicable.

### 2.1 User Service
#### POST /users
- **Description:** Create a new user profile.
- **Request Body:**
```json
{
  "email": "alex@example.com",
  "password": "S3cur3!",
  "display_name": "Alex",
  "city": "Austin",
  "interests": ["hiking", "music"],
  "signup_source": "ios"
}
```
- **Response 201:**
```json
{
  "id": "usr_123",
  "display_name": "Alex",
  "city": "Austin",
  "interests": ["hiking", "music"],
  "created_at": "2024-04-20T15:30:00Z"
}
```

#### GET /users/{id}
- **Description:** Fetch user profile (includes public fields, premium status, badges summary).
- **Response 200:**
```json
{
  "id": "usr_123",
  "display_name": "Alex",
  "bio": "Explorer of local music scenes",
  "city": "Austin",
  "avatar_url": "https://cdn.tribe.app/u/usr_123/avatar.jpg",
  "interests": ["hiking", "music"],
  "badges": [
    {"code": "trailblazer", "earned_at": "2024-03-01T12:00:00Z"}
  ],
  "premium_tier": "plus",
  "mutual_clubs": ["club_45", "club_67"],
  "last_active_at": "2024-04-20T15:31:00Z"
}
```

#### PATCH /users/{id}
- **Description:** Update profile fields (partial).
- **Request Body Example:**
```json
{
  "bio": "Explorer of live music and foodie spots",
  "icebreaker_prompt_id": "prompt_20240420"
}
```
- **Response 200:** `{"success": true}`

#### GET /users/{id}/recommendations
- **Description:** Recommended people to connect with.
- **Query Params:** `limit` (default 10), `cursor`.
- **Response 200:**
```json
{
  "items": [
    {
      "id": "usr_789",
      "display_name": "Jordan",
      "mutual_interests": ["hiking"],
      "shared_clubs": 2,
      "compatibility_score": 0.86,
      "icebreaker_prompt": "Ask about their favorite hiking trail"
    }
  ],
  "next_cursor": "eyJpZCI6ICJ1c3JfNzg5In0="
}
```

### 2.2 Event Service
#### POST /events
- **Description:** Create event (organizers only).
- **Request Body:**
```json
{
  "title": "Sunset Hike",
  "description": "Join us for a scenic hike",
  "start_time": "2024-05-05T23:00:00Z",
  "end_time": "2024-05-06T02:00:00Z",
  "location": {
    "name": "Barton Creek",
    "latitude": 30.285,
    "longitude": -97.735
  },
  "capacity": 25,
  "club_id": "club_45",
  "tags": ["outdoors"],
  "ticketing": {
    "type": "paid",
    "price_cents": 1500,
    "currency": "USD"
  }
}
```
- **Response 201:**
```json
{
  "id": "evt_123",
  "status": "draft",
  "created_at": "2024-04-20T16:00:00Z"
}
```

#### GET /events/{id}
- **Description:** Event details + attendees summary.
- **Response 200:**
```json
{
  "id": "evt_123",
  "title": "Sunset Hike",
  "description": "Join us for a scenic hike",
  "start_time": "2024-05-05T23:00:00Z",
  "end_time": "2024-05-06T02:00:00Z",
  "location": {
    "name": "Barton Creek",
    "latitude": 30.285,
    "longitude": -97.735
  },
  "capacity": 25,
  "attendees": {
    "count": 18,
    "preview": [
      {"user_id": "usr_123", "display_name": "Alex", "avatar_url": "..."}
    ]
  },
  "media": [
    {"id": "med_45", "type": "photo", "url": "https://s3.amazonaws.com/..."}
  ],
  "ticketing": {
    "type": "paid",
    "price_cents": 1500,
    "currency": "USD",
    "status": "open"
  }
}
```

#### POST /events/{id}/rsvp
- **Description:** RSVP to an event (handles capacity/waitlist).
- **Request Body:**
```json
{
  "status": "going",
  "guest_count": 1,
  "payment_method_id": "pm_abc123"
}
```
- **Response 200:**
```json
{
  "rsvp_id": "rsvp_456",
  "status": "confirmed",
  "calendar_sync": {
    "google": "synced",
    "apple": "pending"
  }
}
```

#### GET /events/recommendations
- **Description:** Personalized events feed.
- **Query:** `city`, `cursor`, `limit`, `time_range`, `premium_only`.
- **Response 200:**
```json
{
  "items": [
    {
      "id": "evt_789",
      "title": "Sunrise Yoga",
      "start_time": "2024-05-07T11:00:00Z",
      "score": 0.91,
      "reason": "Friends attending"
    }
  ],
  "next_cursor": "eyJpZCI6ICJldnRfNzg5In0="
}
```

#### POST /events/{id}/media
- **Description:** Upload media (pre-signed URL workflow).
- **Request Body:**
```json
{
  "type": "photo",
  "content_type": "image/jpeg",
  "file_name": "sunset.jpg"
}
```
- **Response 200:**
```json
{
  "upload_url": "https://s3.amazonaws.com/tribe-events/...",
  "media_id": "med_456",
  "expires_at": "2024-04-20T16:10:00Z"
}
```

### 2.3 Club Service
#### POST /clubs
```json
{
  "name": "Austin Adventurers",
  "description": "Outdoor explorations",
  "city": "Austin",
  "tags": ["outdoors"],
  "privacy_level": "public"
}
```
- **Response 201:** `{"id": "club_45", "slug": "austin-adventurers"}`

#### POST /clubs/{id}/join
- **Request Body:** `{"message": "Excited to join!"}`
- **Response 200:** `{"status": "approved"}`

#### GET /clubs/{id}/feed
- Returns paginated posts, events, and media.
```json
{
  "items": [
    {
      "id": "post_123",
      "type": "post",
      "author": {"id": "usr_123", "display_name": "Alex"},
      "content": "Photos from last hike",
      "media": ["https://.../med_456"],
      "created_at": "2024-04-20T15:00:00Z"
    }
  ],
  "next_cursor": "eyJpZCI6ICJwb3N0XzEyMyJ9"
}
```

#### POST /clubs/{id}/posts
```json
{
  "content": "Weekly plan",
  "media_ids": ["med_789"],
  "event_id": "evt_123"
}
```
- **Response 201:** `{"id": "post_456", "created_at": "2024-04-20T17:00:00Z"}`

### 2.4 Chat Service
> Authentication via JWT; WebSocket connections require token query param or header upgrade.

#### POST /chats/{chatId}/messages
```json
{
  "client_message_id": "tmp-123",
  "sender_id": "usr_123",
  "content": {
    "type": "text",
    "text": "Ready for tonight's event?",
    "attachments": [
      {"media_id": "med_456"}
    ]
  }
}
```
- **Response 202:**
```json
{
  "message_id": "msg_789",
  "ack_token": "ack_123",
  "status": "queued"
}
```

#### GET /chats/{chatId}/messages?before=ts&limit=50
- **Response 200:**
```json
{
  "messages": [
    {
      "id": "msg_789",
      "sender_id": "usr_123",
      "content": {"type": "text", "text": "Ready for tonight's event?"},
      "sent_at": "2024-04-20T17:05:00Z",
      "delivery": {"status": "delivered", "read_by": ["usr_456"]}
    }
  ],
  "paging": {
    "next_before": "2024-04-20T17:04:59Z"
  }
}
```

#### DELETE /chats/{chatId}/messages/{id}
- **Response 200:** `{"status": "removed", "moderator_id": "usr_mod"}`

#### POST /chats/{chatId}/typing
```json
{
  "user_id": "usr_123",
  "is_typing": true
}
```
- **Response 200:** `{"expires_in_ms": 5000}`

#### WebSocket ws://.../chats/{chatId}
- **Message Types:**
  - `message.new`, `message.update`, `message.delete`
  - `typing.start/stop`
  - `delivery.read` (read receipts)
  - `system.moderation`
- **Handshake Payload:** `{ "type": "connection.init", "token": "<jwt>", "client": "ios" }`
- **Ack Flow:** Server returns `{ "type": "ack", "ack_token": "ack_123", "message_id": "msg_789" }` upon persistence.

### 2.5 Gamification Service
#### POST /streaks/{userId}
```json
{
  "event": "daily_checkin",
  "occurred_at": "2024-04-20T17:30:00Z"
}
```
- **Response:** `{"current_streak": 5, "longest_streak": 12}`

#### GET /leaderboards
- **Query:** `city`, `category`, `cursor`
```json
{
  "items": [
    {"rank": 1, "user_id": "usr_123", "score": 980},
    {"rank": 2, "user_id": "usr_456", "score": 920}
  ],
  "next_cursor": null
}
```

#### GET /badges/{userId}
```json
{
  "badges": [
    {"code": "event_10", "name": "Event Enthusiast", "earned_at": "2024-04-10T12:00:00Z"}
  ]
}
```

### 2.6 Premium Service
#### POST /payments/subscribe
```json
{
  "user_id": "usr_123",
  "plan_id": "plus_monthly",
  "payment_method_id": "pm_abc123",
  "promo_code": "SPRING24"
}
```
- **Response 201:**
```json
{
  "subscription_id": "sub_456",
  "status": "active",
  "renewal_date": "2024-05-20",
  "client_secret": "seti_..."
}
```

#### GET /users/{id}/subscription
```json
{
  "subscription_id": "sub_456",
  "tier": "plus",
  "status": "active",
  "renewal_date": "2024-05-20",
  "benefits": ["visibility_boost", "exclusive_events"]
}
```

#### POST /events/{id}/boost
```json
{
  "duration_hours": 24,
  "boost_type": "discover_feed",
  "payment_method_id": "pm_abc123"
}
```
- **Response:** `{"boost_id": "boost_123", "status": "scheduled"}`

## 3. Data Storage Design

### 3.1 Postgres Schema (Key Tables)
- **users** (`id` PK, `email` UNIQUE, `password_hash`, `display_name`, `bio`, `city`, `avatar_url`, `premium_tier`, `created_at`, `updated_at`, `last_active_at`)
- **user_profiles** (`user_id` PK/FK users, `interests` JSONB, `icebreaker_prompt_id`, `settings` JSONB)
- **clubs** (`id` PK, `name`, `slug`, `description`, `city`, `privacy_level`, `owner_id` FK users, `created_at`)
- **club_memberships** (`id` PK, `club_id` FK clubs, `user_id` FK users, `role`, `status`, `joined_at`)
- **club_posts** (`id` PK, `club_id` FK clubs, `author_id` FK users, `content`, `media_ids` JSONB, `event_id` FK events NULLABLE, `created_at`)
- **events** (`id` PK, `title`, `description`, `start_time`, `end_time`, `location` JSONB, `capacity`, `club_id` FK clubs NULLABLE, `visibility`, `ticketing` JSONB, `created_at`, `updated_at`)
- **event_attendees** (`id` PK, `event_id` FK events, `user_id` FK users, `status`, `guest_count`, `payment_status`, `calendar_synced_at`, `created_at`)
- **event_media** (`id` PK, `event_id` FK events, `media_id`, `type`, `url`, `uploaded_by`, `created_at`)
- **feeds** (`id` PK, `user_id` FK users, `source_type`, `source_id`, `payload` JSONB, `created_at`)
- **chat_chats** (`id` PK, `type` ENUM('direct','group','event','club'), `context_id`, `created_at`)
- **chat_participants** (`chat_id` FK chat_chats, `user_id` FK users, `role`, `last_read_message_id`, `last_read_at`)
- **chat_messages** (`id` PK, `chat_id` FK, `sender_id` FK users, `content` JSONB, `sent_at`, `edited_at`, `deleted_at`, `moderation_status`)
- **message_attachments** (`id` PK, `message_id` FK, `media_id`, `type`, `metadata` JSONB)
- **streaks** (`user_id` FK users, `streak_type`, `current_count`, `longest_count`, `last_incremented_at`)
- **badges** (`code` PK, `name`, `description`, `criteria` JSONB)
- **user_badges** (`user_id` FK users, `badge_code` FK badges, `earned_at`)
- **leaderboard_snapshots** (`id` PK, `city`, `category`, `period`, `data` JSONB, `generated_at`)
- **subscriptions** (`id` PK, `user_id` FK users, `tier`, `status`, `renewal_date`, `provider_subscription_id`, `created_at`, `updated_at`)
- **payments** (`id` PK, `user_id`, `event_id` NULLABLE, `amount_cents`, `currency`, `status`, `provider_payment_id`, `created_at`)
- **boosts** (`id` PK, `event_id` FK events, `user_id` FK users, `boost_type`, `start_at`, `end_at`, `status`)
- **notifications** (`id` PK, `user_id`, `type`, `payload` JSONB, `status`, `scheduled_at`, `sent_at`)

#### Relationships
- `users` 1:N `events` (organizers) via `events.created_by` (add column).
- `events` 1:N `event_attendees`.
- `chat_chats` N:M `users` through `chat_participants`.
- `subscriptions` linked to premium features gating `clubs`/`events`.
- `feeds` aggregated per user from events/clubs/gamification.

### 3.2 Redis Usage
- Session store: `session:{token}` ➝ user_id, expiry.
- Chat cursor offsets: `chat:{chatId}:offset:{userId}`.
- Typing indicators TTL entries: `typing:{chatId}:{userId}`.
- Streak counters: `streak:{userId}:{type}` with atomic increments + expiry handling.
- Rate limiting keys (e.g., `rate:user:{id}:endpoint`).

### 3.3 ElasticSearch Indices
- `users_index`: fields `display_name`, `bio`, `interests`, `city`, `boost_score`.
- `events_index`: `title`, `description`, `tags`, geo_point `location`, `start_time`, `popularity_score`, `premium_boost`.
- `clubs_index`: `name`, `tags`, `city`, `member_count`.
- `feeds_index` (optional) for personalized retrieval when scale demands.
- Use ingest pipelines for media metadata extraction (EXIF) for map view enhancements.

### 3.4 S3 Buckets & Structure
- `tribe-user-media/{userId}/avatars/{file}`
- `tribe-event-media/{eventId}/{mediaId}` (photos, reels).
- `tribe-club-media/{clubId}/{mediaId}`
- Lifecycle policies: auto-transition to Glacier after 180 days for past events, keep thumbnails in Standard.
- Store transcoded videos in separate `reels` prefix; track status in Postgres `media_processing_jobs` table.

## 4. Production-Grade Chat Service Design

### 4.1 Architecture Overview
- **Gateway Layer:** HTTPS/WebSocket termination (NGINX/ALB) -> Chat API service.
- **Chat API Service:** Stateless nodes handling REST requests (message send/delete, history), publishing to Kafka `chat-messages` topic.
- **Chat Realtime Service:** WebSocket workers subscribed to Kafka; maintain in-memory subscription maps, use Redis Pub/Sub for fan-out across nodes.
- **Persistence Layer:**
  - Write path: Chat API validates, enriches, persists message to Postgres (`chat_messages`) via transactional outbox; outbox events streamed to Kafka for delivery and Elastic indexing.
  - Read path: History endpoint reads from Postgres with pagination using `sent_at` and `id` cursor.
- **Delivery Guarantees:** Client sends `client_message_id`; server deduplicates via Redis set. After DB commit, ack published to Kafka → WebSocket service -> client `ack` message.

### 4.2 Real-Time Features
- **Typing Indicators:** Stored in Redis with 5s TTL; broadcast via WebSocket event `typing.start/stop`.
- **Read Receipts:** Client sends `delivery.read` event with last_read_message_id; Chat API updates `chat_participants.last_read_message_id` and publishes to Kafka for fan-out.
- **Presence (optional extension):** Use Redis SETs `presence:chat:{chatId}`.

### 4.3 Moderation & Compliance
- **Deletion:** Moderators or sender can soft-delete; `chat_messages.deleted_at` set, content replaced with tombstone message on fetch.
- **Reporting:** POST /chats/{chatId}/messages/{id}/report (future) storing in `moderation_cases` table, triggers review workflow.
- **Content Filtering:** Integrate with moderation service (e.g., AWS Comprehend, Perspective API) asynchronously; flagged content updates `moderation_status` and can auto-hide via WebSocket `system.moderation` events.
- **Audit Logging:** All moderation actions emitted to security audit log (append-only store).

### 4.4 Scalability & Fault Tolerance
- Horizontal scaling of WebSocket service using sticky sessions + Redis cluster for coordination.
- Back-pressure handling with rate limits (messages per user per minute) enforced via Redis.
- Use Kafka partitions keyed by chatId to maintain ordering per conversation.
- Disaster recovery with cross-region replicas; Postgres logical replication for warm standby.

### 4.5 Testing & Monitoring
- Contract tests for REST endpoints (OpenAPI).
- Load testing using Gatling/Locust for WebSocket throughput.
- Metrics: message send latency, delivery success rate, active WebSocket connections, Kafka lag.
- Alerts when ack latency > 1s or error rate > 2%.

## 5. Backend Engineering Guidelines

### 5.1 Authentication & Authorization
- JWT access tokens (15 min) + refresh tokens (7 days); stored securely with rotation.
- OAuth integrations for Apple/Google sign-in; link to existing accounts via email verification.
- RBAC: roles `user`, `moderator`, `organizer`, `admin`, `premium_plus`. Use policy engine (e.g., OPA) for fine-grained checks on premium features and moderation.
- Service-to-service auth via mTLS and short-lived service tokens issued by IAM (e.g., AWS STS).

### 5.2 Scaling & Performance
- Microservice decomposition (user, event, club, chat, gamification, premium) with API gateway for routing.
- Async jobs through Kafka (preferred) or SQS for notifications, recommendation recalculations, media processing, calendar sync.
- Cache layers: Redis for hot feeds, user profiles; CDN (CloudFront) for media.
- Use feature flags (LaunchDarkly) for incremental rollouts.

### 5.3 Monitoring & Observability
- Instrument services with Micrometer → Prometheus → Grafana dashboards.
- Distributed tracing (OpenTelemetry) with correlation IDs propagated via headers (`X-Correlation-Id`).
- Centralized structured logging (ELK stack), include user_id, chat_id where applicable.
- SLOs defined per service; alerting via PagerDuty.

### 5.4 Testing & QA
- Unit tests with JUnit/Mockito (Java stack) + coverage targets.
- Integration tests using Testcontainers (Postgres, Redis, Kafka) executed in CI.
- Contract tests for public APIs; schema validations.
- Performance testing before major releases; chaos testing for chat service.
- Blue/green deployments with automated smoke tests.

### 5.5 DevOps & Security
- Infrastructure as Code (Terraform) for reproducible environments.
- Secrets management via AWS Secrets Manager or Vault.
- Regular dependency scanning (OWASP, Snyk) and container image scanning.
- Data retention policies and right-to-be-forgotten workflows.

## 6. Milestone Plan for Junior Developers

| Milestone | Duration | Focus | Key Deliverables |
|-----------|----------|-------|------------------|
| **M1: Core CRUD Foundations** | 3 weeks | User, Event, Club base services | Postgres schemas, REST endpoints (create/read/update), unit tests, CI pipelines, seed data scripts |
| **M2: RSVP & Notifications** | 2 weeks | RSVP flows, calendar sync scaffolding | RSVP endpoint with capacity checks, notification service MVP (email/push queue), calendar webhook handlers, integration tests |
| **M3: Chat Service Launch** | 4 weeks | Real-time messaging | Chat DB schema, message send/history APIs, WebSocket gateway, Redis/Kafka setup, delivery ack + read receipts, load test report |
| **M4: Gamification & Recommendations** | 3 weeks | Engagement systems | Streak tracking worker, badges API, leaderboard snapshots, ElasticSearch recommendation queries, map view API hooks |
| **M5: Premium & Payments** | 3 weeks | Monetization layer | Stripe integration (subscriptions + ticketing), boost workflow, premium gating middleware, audit logs, production readiness checklist |

### Milestone Execution Notes
- Each milestone ends with demo + retrospective.
- Maintain shared API schema (OpenAPI) updated per milestone.
- Parallelize frontend integration via mock servers and contract tests.
- QA begins during milestone with feature flags protecting unfinished flows.

## 7. Dependencies & Risks
- Payment provider onboarding timeline (Stripe) may affect premium launch; create sandbox plan early.
- WebSocket infrastructure needs load testing environment; allocate resources in advance.
- Recommendation quality depends on data volume; plan for iterative tuning and A/B tests.
- Compliance for storing media in S3 (moderation) requires retention policies and human review pipeline.

## 8. Open Questions
- Finalize pricing tiers and premium benefits granularity.
- Determine moderation staffing and SLAs.
- Decide on in-house vs third-party push notification service (Firebase/APNs vs. Segment/Braze).
- Align legal review for privacy policy updates due to new tracking (leaderboards, streaks).

