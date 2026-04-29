package dev.wardedlock.common.keys;

public final class EventNames {

    public static final String STREAM_NOTIFICATIONS = "stream:notifications.v1";
    public static final String STREAM_AUDIT = "stream:audit.v1";
    public static final String DLQ_NOTIFICATIONS = "stream:notifications.dlq";

    private EventNames() {}

    public static final class User {
        public static final String CREATED = "user.created.v1";
        public static final String UPDATED = "user.updated.v1";
        public static final String DELETED = "user.deleted.v1";
        public static final String EMAIL_VERIFIED = "user.email_verified.v1";
        public static final String LOCKED = "user.locked.v1";

        private User() {}
    }

    public static final class Auth {
        public static final String LOGIN_SUCCESS = "auth.login_success.v1";
        public static final String LOGIN_FAILED = "auth.login_failed.v1";
        public static final String PASSWORD_CHANGED = "auth.password_changed.v1";
        public static final String MFA_ENABLED = "auth.mfa_enabled.v1";
        public static final String IDEMPOTENCY_REPLAY = "auth.idempotency_replay.v1";

        private Auth() {}
    }

    public static final class App {
        public static final String CREATED = "app.created.v1";
        public static final String SECRET_ROTATED = "app.secret_rotated.v1";

        private App() {}
    }

    public static final class Webhook {
        public static final String DELIVERED = "webhook.delivered.v1";
        public static final String FAILED = "webhook.failed.v1";
        public static final String DISABLED = "webhook.disabled.v1";

        private Webhook() {}
    }
}
