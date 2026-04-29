#!/bin/bash
set -e

# This script creates multiple databases for the microservices architecture.
# It reads from the environment variables or a predefined list.

function create_user_and_database() {
	local database=$1
	local user=$2
	local password=$3
	echo "  Creating user and database '$database'..."
	psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
	    CREATE USER $user WITH PASSWORD '$password';
	    CREATE DATABASE $database;
	    GRANT ALL PRIVILEGES ON DATABASE $database TO $user;
	    ALTER DATABASE $database OWNER TO $user;
EOSQL
}

echo "🐘 Initializing Multi-DB Setup for WardedLock..."

# Create databases using variables from .env
# Note: Root user is already created by official image via POSTGRES_USER/PASSWORD
if [ -n "$AUTH_DB" ]; then create_user_and_database "$AUTH_DB" "$AUTH_USER" "$AUTH_PASSWORD"; fi
if [ -n "$ACCOUNT_DB" ]; then create_user_and_database "$ACCOUNT_DB" "$ACCOUNT_USER" "$ACCOUNT_PASSWORD"; fi
if [ -n "$ROLE_DB" ]; then create_user_and_database "$ROLE_DB" "$ROLE_USER" "$ROLE_PASSWORD"; fi
if [ -n "$APP_MANAGEMENT_DB" ]; then create_user_and_database "$APP_MANAGEMENT_DB" "$APP_MANAGEMENT_USER" "$APP_MANAGEMENT_PASSWORD"; fi
if [ -n "$NOTIFICATION_DB" ]; then create_user_and_database "$NOTIFICATION_DB" "$NOTIFICATION_USER" "$NOTIFICATION_PASSWORD"; fi
if [ -n "$AUDIT_DB" ]; then create_user_and_database "$AUDIT_DB" "$AUDIT_USER" "$AUDIT_PASSWORD"; fi

echo "✅ All microservice databases initialized."
