package wardedlock.testing

/**
 * FR-TEST-002, FR-TEST-006, ARCH-B-015. Centralized microservice-to-database naming mapping.
 * Avoids hardcoding database targets and relative filesystem migration paths (e.g. "../../infra/migrations")
 * in test YAML configurations or Java sources.
 */
object FlywayDatabaseMap {
    private val map = mapOf(
        "account-service" to "wl_account",
        "auth-service" to "wl_auth",
        "role-service" to "wl_role",
        "app-management-service" to "wl_app_management",
        "notification-service" to "wl_notification"
    )

    fun getDatabaseForProject(projectName: String): String? {
        return map[projectName]
    }
}
