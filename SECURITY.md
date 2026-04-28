# Security Policy

## Supported Versions

| Version | Status |
| ------- | ------ |
| 1.0.x (upcoming) | Pre-release, not yet GA |
| < 1.0   | Not released |

## Scope

### In scope:
- Authentication, authorization, and session management bugs.
- Cryptographic flaws (JWT signing, password hashing, key handling).
- Privilege escalation across services.
- Secret leakage in logs, errors, or responses.

### Out of scope:
- Self-XSS without realistic impact.
- Missing security headers on dev/test domains only.
- Rate-limit bypass that does not enable account takeover.
- Denial-of-service via brute compute (covered by infra rate limits).

## Reporting a Vulnerability

If you discover a security vulnerability within WardedLock, please send an e-mail to:
**trunglcct@gmail.com** (PGP key: TBD before 1.0.0 GA)

### Please include in your report:
- Clear description of the vulnerability.
- Reproduction steps.
- Impact assessment.

### Response Timeline:
- Initial acknowledgement: within 48 hours.
- Patch deployment: please allow a reasonable timeframe before public disclosure.

WardedLock follows the principle of coordinated vulnerability disclosure. We ask that you do not disclose the vulnerability publicly until a patch has been released.
