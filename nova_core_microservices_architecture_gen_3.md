# NovaCore — Gen3 Microservices Architecture

> Stack baseline: **Spring Boot (Java)** · **PostgreSQL** per service · **JWT (Access/Refresh)**. Recommended add-ons: **Redis** (tokens/ratelimiting), **Kafka** (events) — optional but highly recommended for scale.

---

## 1) مبادئ التصميم (Design Principles)

- **Domain‑Oriented**: كل نطاق (Auth, Users, Chat…) خدمة مستقلة تملك قاعدة بياناتها (**DB per service**).
- **Stateless APIs**: كل الخدمات عديمة الحالة قدر الإمكان، والحالة الحرجة (Sessions/Rate limits) تخزن في Redis/DB.
- **Security‑first**: JWT قصير العمر + Refresh Rotation + Reuse Detection + Single Active Session لكل مستخدم.
- **Observability‑ready**: Centralized logs, metrics, tracing.
- **Zero Trust بين الخدمات**: mTLS بين الخدمات أو توقيع HMAC على الرسائل.

---

## 2) خريطة الخدمات (High‑Level Services)

### Edge & Platform

- **API Gateway**: توحيد الدخول، مصادقة JWT، تمرير الهوية، تحديد السرعة (Rate‑Limit)، CORS.
- **Config Server** (اختياري) + **Service Registry** (إذا ليس Kubernetes).
- **Notification Worker**: إرسال SMS/Push/WebSocket fanout.
- **Media Edge**: رفع/جلب الوسائط (pre‑signed URLs)، تشفير على مستوى الملف عند الحاجة.

### Core Domains

- **Auth Service** *(نبدأ به بعد هذه الوثيقة)*: OTP via SMS، إصدار/تدوير Refresh، سياسة جلسة واحدة.
- **Users Service**: ملف المستخدم، الحالة، الحظر النظامي/اليدوي (Integration مع Moderation).
- **Contacts Service**: إدارة جهات الاتصال، اكتشاف من مسار الهاتف (Privacy‑aware hashing).
- **Chat Service**: المحادثات والمشاركين.
- **Message Service**: الرسائل، الحالة، التفاعلات، التسلسل.
- **Media Service**: بيانات ملفات الوسائط والروابط المؤقتة.
- **Presence Service**: الحالة اللحظية (Online/Last seen) عبر WebSocket + Redis.
- **Moderation Service**: user blocks, system bans, reports.
- **Audit Service**: سجل عمليات إدارية/حساسة.
- **Admin/Backoffice**: لوحات تحكم وإدارة القواعد.

> ملاحظة: يمكن دمج Chat+Message في خدمة واحدة في البداية ثم فصلها لاحقًا.

---

## 3) قواعد البيانات وملكية البيانات (DB Ownership)

- **Auth DB**: users\_auth, user\_sessions, otp\_attempts, device\_fingerprint.
- **Users DB**: users\_profile, system\_bans.
- **Contacts DB**: contacts, import\_batches.
- **Chat DB**: conversations, participants.
- **Message DB**: messages, message\_status, message\_reactions.
- **Media DB**: media, uploads, retention.
- **Moderation DB**: user\_blocks, reports.
- **Audit DB**: audit\_log.

> التبادل بين الخدمات عبر **Events** و/أو **API‑to‑API** مع حدود واضحة.

---

## 4) الاتصالات بين الخدمات (Communication)

- **Synchronous**: REST/JSON عبر Gateway (Clients → Gateway → Services). داخل الشبكة يمكن gRPC اختياري.
- **Asynchronous (موصى به)**: Kafka Topics (e.g., `auth.user.verified`, `message.sent`, `moderation.blocked`).
- **Outbox Pattern**: عند الحاجة لضمان التسليم مرة واحدة.

---

## 5) الأمن (Security Model)

- **Access Token (JWT)**: عمر قصير (مثلاً 10–15 دقيقة)، توقيع بـ RSA أو HMAC، يحتوي claims: `sub`, `uid`, `phone`, `scope`, `iat`, `exp`, `jti`.
- **Refresh Token**: عُمر أطول (مثلاً 14 يوم)، يخزن **hash** فقط في DB/Redis مع `jti`, `device`, `ip`, `ua`.
- **Rotation & Reuse detection**: كل refresh يستبدل بآخر جديد ويُبطل القديم. اكتشاف إعادة الاستخدام يؤدي لإلغاء جميع الجلسات.
- **Single Active Session**: سياسة Auth Service: جلسة واحدة فعالة لكل مستخدم (أو لكل جهاز—قابلة للتهيئة). أي جلسة جديدة تُبطل السابقة.
- **Service‑to‑Service**: mTLS أو JWT داخلي موقّع من Authority خاصة.

---

## 6) قابلية التوسع (Scalability)

- **Stateless scale‑out** لجميع الخدمات خلف Gateway/Ingress.
- **Caching**: Redis لملفات تعريف خفيفة، presence، counters.
- **Cold storage**: وسائط على S3‑compatible + CDN.
- **Sharding لاحقًا**: تقطيع رسائل حسب conversation\_id، وفهارس مخصصة للقراءة.

---

## 7) المراقبة والتشغيل (Ops)

- **Logs**: JSON logs → ELK/Opensearch.
- **Metrics**: Micrometer → Prometheus + Grafana.
- **Tracing**: OpenTelemetry → Tempo/Jaeger.
- **Health/Readiness**: Spring Actuator.

---

## 8) مخطط معماري (Mermaid)

```mermaid
flowchart LR
    C[Clients\nMobile/Web] --> G[API Gateway]
    subgraph Core
      A[Auth Service]\nPostgreSQL
      U[Users Service]\nPostgreSQL
      CT[Contacts Service]\nPostgreSQL
      CH[Chat Service]\nPostgreSQL
      M[Message Service]\nPostgreSQL
      MD[Media Service]\nPostgreSQL
      PR[Presence Service]\nRedis
      MO[Moderation Service]\nPostgreSQL
      AU[Audit Service]\nPostgreSQL
      N[Notification Worker]
    end
    G --> A
    G --> U
    G --> CT
    G --> CH
    G --> M
    G --> MD
    G --> PR
    G --> MO
    G --> AU
    A -->|events| CH
    A -->|events| U
    M -->|events| PR
    CH -->|events| PR
    A <-->|token introspect (cache)| PR
    N --> C
```

---

## 9) بوابة الدخول (API Gateway)

- **Auth Filter**: التحقق من JWT، استخراج `uid`, `scope`.
- **Rate Limit**: لكل IP/phone/device (Redis Leaky/Bucket).
- **Request‑ID**: يولد Trace/Correlation IDs.
- **Routing**: مسارات نظيفة `/auth/*`, `/users/*`, `/chat/*`, `/messages/*`, …

---

## 10) حدود العقود بين الخدمات (Service Contracts)

- **Auth → Users**: حدث `auth.user.verified { userId, phone }` أو REST `POST /users/activate`.
- **Chat ↔ Message**: Message يملك الرسائل؛ Chat يملك عضوية المحادثة.
- **Moderation hooks**: قبل إرسال رسالة/دعوة، تحقق من الحظر (sync call أو Cache مشتركة).
- **Presence broadcast**: PR ينشر حالة Online/Offline للأصدقاء/أعضاء المحادثة فقط.

---

## 11) خارطة المسارات الخارجية (Public API Examples)

- `POST /auth/otp/request` — طلب رمز OTP.
- `POST /auth/otp/verify` — تحقق وإصدار Tokens.
- `POST /auth/token/refresh` — تدوير Refresh.
- `POST /auth/logout` — إنهاء الجلسة الحالية.
- `GET /users/me` — ملف المستخدم.
- `POST /chat/direct` — إنشاء/جلب محادثة مباشرة.
- `POST /messages` — إرسال رسالة مشفرة.
- `GET /presence` — الاشتراك بالقناة اللحظية (WebSocket upgrade).

---

## 12) خارطة الطريق (Next Steps)

1. بناء **Auth Service** كما طلبت (OTP, JWT, Refresh Rotation, Single Session).
2. إعداد **Gateway** بقواعد الأمن والـ Rate‑limit.
3. تجهيز **Presence/WebSocket** للـ Online/Last seen.
4. كتابة **migrations** (Flyway) لكل خدمة وCI للتشغيل.

> جاهز الآن للانتقال إلى **Auth Service**: endpoints, جداول، تدفق OTP، منطق الجلسة الواحدة، وSecurity config بالتفصيل.

