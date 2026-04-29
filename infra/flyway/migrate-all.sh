#!/bin/bash
set -euo pipefail

# Default to "migrate" if no command is provided
CMD=("${@:-migrate}")

# Map of database names to their environment variable prefixes
declare -A DB_MAP=(
    ["wl_auth"]="AUTH"
    ["wl_account"]="ACCOUNT"
    ["wl_role"]="ROLE"
    ["wl_app_management"]="APP_MANAGEMENT"
    ["wl_notification"]="NOTIFICATION"
    ["wl_audit"]="AUDIT"
)

echo "🚀 Starting multi-database Flyway execution: ${CMD[*]}..."

for DB_NAME in "${!DB_MAP[@]}"; do
    PREFIX="${DB_MAP[$DB_NAME]}"
    
    # Dynamically read user and password variables
    USER_VAR="${PREFIX}_USER"
    PASS_VAR="${PREFIX}_PASSWORD"
    
    DB_USER="${!USER_VAR}"
    DB_PASS="${!PASS_VAR}"
    
    echo "--------------------------------------------------------"
    echo "📦 Executing '${CMD[*]}' on database: $DB_NAME"
    echo "--------------------------------------------------------"
    
    flyway \
        -url="jdbc:postgresql://postgres:5432/${DB_NAME}" \
        -user="${DB_USER}" \
        -password="${DB_PASS}" \
        -locations="filesystem:/flyway/sql/${DB_NAME}" \
        "${CMD[@]}"
done

echo "✅ All database operations completed successfully."
