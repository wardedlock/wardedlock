#!/usr/bin/env node
// Cross-platform Gradle wrapper invoker.
// Usage: node scripts/run-gradle.mjs <gradle-args...>
import { spawnSync } from "node:child_process";
import { platform } from "node:process";

const isWin = platform === "win32";
const cmd = isWin ? "gradlew.bat" : "./gradlew";
const args = process.argv.slice(2);

const result = spawnSync(cmd, args, { stdio: "inherit", shell: isWin });

if (result.error) {
    console.error(`Failed to run ${cmd}:`, result.error.message);
    process.exit(1);
}

process.exit(result.status ?? 1);
