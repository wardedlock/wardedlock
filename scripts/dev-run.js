const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');

/**
 * WardedLock - Cross-Platform Development Wrapper
 * Loads .env variables and executes Gradle
 */

const rootDir = path.resolve(__dirname, '..');
const envPath = path.join(rootDir, '.env');

// 1. Load and parse .env
if (fs.existsSync(envPath)) {
    const envContent = fs.readFileSync(envPath, 'utf8');
    envContent.split(/\r?\n/).forEach(line => {
        const trimmed = line.trim();
        if (trimmed && !trimmed.startsWith('#') && trimmed.includes('=')) {
            const [key, ...valueParts] = trimmed.split('=');
            const value = valueParts.join('=').replace(/^["']|["']$/g, ''); // Remove quotes
            process.env[key.trim()] = value;
        }
    });
} else {
    console.error('❌ Error: .env file not found. Please run "npm run bootstrap" first.');
    process.exit(1);
}

// 2. Determine Gradle command
const isWindows = process.platform === 'win32';
const gradleCmd = isWindows ? 'gradlew.bat' : './gradlew';
const fullGradlePath = path.join(rootDir, gradleCmd);

// 3. Execute Gradle
const args = process.argv.slice(2);
console.log(`🚀 Running: ${gradleCmd} ${args.join(' ')}`);

const child = spawn(isWindows ? `"${fullGradlePath}"` : fullGradlePath, args, {
    stdio: 'inherit',
    shell: isWindows, // Required for .bat files on Windows
    cwd: rootDir
});

child.on('exit', (code) => {
    process.exit(code);
});
