#!/usr/bin/env bash

# Exit on error
set -e

# Change to the project root relative to this script
cd "$(dirname "$0")/.."

# Usage:
#   bash scripts/bootstrap.sh                       # Dev mode: copy as-is
#   bash scripts/bootstrap.sh --force               # Refresh dev .env
#   bash scripts/bootstrap.sh --randomize           # Randomize secrets
#   bash scripts/bootstrap.sh --force --randomize   # Wipe + randomize

FORCE=false
RANDOMIZE=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --force)
            FORCE=true
            ;;
        --randomize|--secure)
            RANDOMIZE=true
            ;;
        *)
            echo "❌ Unknown flag: $1"
            echo "Usage: $0 [--force] [--randomize]"
            exit 1
            ;;
    esac
    shift
done

echo "🚀 Bootstrapping WardedLock Environment Setup..."

# Make secrets directory safely ignored by git
mkdir -p ./secrets

# 1. Initialize .env
if [ ! -f .env ] || [ "$FORCE" = true ]; then
    if [ "$FORCE" = true ]; then
        echo "⚠️  Force flag detected. Regenerating .env..."
        rm -f .env
    fi
    
    echo "📋 Generating .env from .env.example..."
    
    SECRETS_TO_RANDOMIZE=(
        POSTGRES_PASSWORD
        AUTH_PASSWORD ACCOUNT_PASSWORD ROLE_PASSWORD
        APP_MANAGEMENT_PASSWORD NOTIFICATION_PASSWORD AUDIT_PASSWORD
        REDIS_PASSWORD S3_SECRET_KEY
    )

    while IFS= read -r line || [[ -n "$line" ]]; do
        # Extract the key (part before =)
        key="${line%%=*}"
        
        # Check if the key is in our list to randomize
        found=false
        for secret_key in "${SECRETS_TO_RANDOMIZE[@]}"; do
            if [[ "$key" == "$secret_key" ]]; then
                found=true
                break
            fi
        done

        if [ "$found" = true ] && [ "$RANDOMIZE" = true ]; then
            RANDOM_PASS=$(openssl rand -base64 32 | tr -dc 'A-Za-z0-9' | head -c 24)
            echo "$key=$RANDOM_PASS"
        else
            echo "$line"
        fi
    done < .env.example > .env
    
    if [ "$RANDOMIZE" = true ]; then
        echo "✅ Generated .env with randomized secrets (production-grade)."
    else
        echo "✅ Generated .env from .env.example (dev defaults preserved)."
        echo "ℹ️  Run with --randomize for staging/prod preparation."
    fi

    # Validate generated .env for empty assignments
    echo "🔍 Validating environment assignments..."
    EMPTY_KEYS=$(grep -E "^[A-Z_]+=$" .env | cut -d'=' -f1)
    if [[ -n "$EMPTY_KEYS" ]]; then
        echo "⚠️  WARNING: The following keys have empty assignments in .env:"
        echo "$EMPTY_KEYS"
    fi

    # Validate required docker-compose secrets are present and non-empty
    REQUIRED_KEYS=(
        POSTGRES_PASSWORD
        AUTH_PASSWORD ACCOUNT_PASSWORD ROLE_PASSWORD
        APP_MANAGEMENT_PASSWORD NOTIFICATION_PASSWORD AUDIT_PASSWORD
        REDIS_PASSWORD S3_SECRET_KEY
    )

    MISSING_REQUIRED_KEYS=()
    for required_key in "${REQUIRED_KEYS[@]}"; do
        if ! grep -qE "^${required_key}=.+" .env; then
            MISSING_REQUIRED_KEYS+=("$required_key")
        fi
    done

    if [[ ${#MISSING_REQUIRED_KEYS[@]} -gt 0 ]]; then
        echo "❌ ERROR: Missing required keys in generated .env:"
        printf ' - %s\n' "${MISSING_REQUIRED_KEYS[@]}"
        echo "Please fix .env.example and run 'bash scripts/bootstrap.sh --force' again."
        exit 1
    fi
else
    echo "⏭️  .env already exists. Use --force to regenerate. Skipping..."
fi

# 2. JWT RSA-3072 Keys
if [ ! -f ./secrets/jwt_rs256.pem ] || [ "$FORCE" = true ]; then
    echo "🔑 Generating RSA-3072 keys for JWT..."
    openssl genpkey -algorithm RSA -out ./secrets/jwt_rs256.pem -pkeyopt rsa_keygen_bits:3072
    openssl rsa -pubout -in ./secrets/jwt_rs256.pem -out ./secrets/jwt_rs256.pub
    echo "✅ JWT RSA Keypair generated successfully."
else
    echo "⏭️  JWT Keys already exist. Skipping..."
fi

# 3. Webhook HMAC Key
if [ ! -f ./secrets/webhook_hmac.key ] || [ "$FORCE" = true ]; then
    echo "🔑 Generating 256-bit Webhook HMAC Key..."
    openssl rand -hex 32 > ./secrets/webhook_hmac.key
    echo "✅ Webhook HMAC Key generated."
else
    echo "⏭️  Webhook HMAC Key already exists. Skipping..."
fi

echo "🎉 Environment Bootstrap Complete! Do not forget to NOT commit .env and ./secrets/"
