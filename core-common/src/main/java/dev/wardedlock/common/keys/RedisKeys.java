package dev.wardedlock.common.keys;

public final class RedisKeys {
    private RedisKeys() {}

    public static final class Auth {
        private Auth() {}
        public static String session(String uuid) {
            return "auth:session:" + uuid;
        }
    }

    public static final class Role {
        private Role() {}
        public static String cache(String roleId) {
            return "role:cache:" + roleId;
        }
    }

    public static final class RateLimit {
        private RateLimit() {}
        public static String ip(String ipAddress) {
            return "ratelimit:ip:" + ipAddress;
        }
    }

    public static final class Idempotency {
        private Idempotency() {}
        public static String key(String idempotencyKey) {
            return "idempotency:key:" + idempotencyKey;
        }
    }

    public static final class Jwks {
        private Jwks() {}
        public static String cache() {
            return "jwks:cache";
        }
    }
}
