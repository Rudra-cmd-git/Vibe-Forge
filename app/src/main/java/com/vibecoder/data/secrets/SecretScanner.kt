package com.vibecoder.data.secrets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern

class SecretScanner @Inject constructor() {

    private val secretPatterns = mapOf(
        "AWS Access Key" to Pattern.compile("(?i)(AKIA|A3T|AGPA|AIDA|AROA|AIPA|ANPA|ANVA|ASIA)[A-Z0-9]{16}"),
        "AWS Secret Key" to Pattern.compile("(?i)aws[_-]?secret[_-]?access[_-]?key[\"']?\s*[:=]\s*[\"']?[A-Za-z0-9/+=]{40}[\"']?"),
        "GitHub Token" to Pattern.compile("(?i)(gh[ps]_[a-zA-Z0-9]{36,}|github[_-]?token[\"']?\s*[:=]\s*[\"']?[a-zA-Z0-9_]{36,}[\"']?)"),
        "GitLab Token" to Pattern.compile("(?i)glpat-[a-zA-Z0-9_\-]{20,}"),
        "Slack Token" to Pattern.compile("(?i)(xox[baprs]-[a-zA-Z0-9-]{10,})"),
        "Discord Token" to Pattern.compile("(?i)[MN][A-Za-z\d]{23}\.[\w-]{6}\.[\w-]{27}"),
        "Telegram Bot Token" to Pattern.compile("(?i)\d{8,10}:[a-zA-Z0-9_-]{35}"),
        "Google API Key" to Pattern.compile("(?i)AIza[0-9A-Za-z\\-_]{35}"),
        "Firebase Key" to Pattern.compile("(?i)AAAA[a-zA-Z0-9_-]{7}:[a-zA-Z0-9_-]{140}"),
        "Stripe Key" to Pattern.compile("(?i)(sk_live|pk_live|sk_test|pk_test)_[a-zA-Z0-9]{24,}"),
        "Twilio Key" to Pattern.compile("(?i)(AC|SK)[a-zA-Z0-9]{32}"),
        "SendGrid Key" to Pattern.compile("(?i)SG\.[a-zA-Z0-9_-]{22}\.[a-zA-Z0-9_-]{43}"),
        "Mailgun Key" to Pattern.compile("(?i)key-[a-zA-Z0-9]{32}"),
        "Generic API Key" to Pattern.compile("(?i)(api[_-]?key|apikey|api_secret)[\"']?\s*[:=]\s*[\"']?[a-zA-Z0-9_\-]{20,}[\"']?"),
        "Generic Secret" to Pattern.compile("(?i)(secret|password|passwd|pwd)[\"']?\s*[:=]\s*[\"']?[a-zA-Z0-9_\-]{8,}[\"']?"),
        "Private Key (RSA)" to Pattern.compile("-----BEGIN (RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----"),
        "Private Key (PKCS8)" to Pattern.compile("-----BEGIN PRIVATE KEY-----"),
        "SSH Private Key" to Pattern.compile("-----BEGIN OPENSSH PRIVATE KEY-----"),
        "PGP Private Key" to Pattern.compile("-----BEGIN PGP PRIVATE KEY BLOCK-----"),
        "JWT Token" to Pattern.compile("(?i)eyJ[a-zA-Z0-9_-]{10,}\.[a-zA-Z0-9_-]{10,}\.[a-zA-Z0-9_-]{10,}"),
        "Bearer Token" to Pattern.compile("(?i)Bearer\s+[a-zA-Z0-9_\-]{20,}"),
        "Basic Auth" to Pattern.compile("(?i)Basic\s+[a-zA-Z0-9+/=]{20,}"),
    )

    private val filePatterns = setOf(
        ".env",
        ".env.local",
        ".env.production",
        ".env.development",
        ".env.staging",
        ".env.test",
        "id_rsa",
        "id_dsa",
        "id_ecdsa",
        "id_ed25519",
        "*.pem",
        "*.key",
        "*.p12",
        "*.pfx",
        "*.jks",
        "*.keystore",
        "*.cert",
        "*.crt",
        "*.pem",
        "*.gpg",
        "*.pgp",
        "secrets.yml",
        "secrets.yaml",
        "credentials.yml",
        "credentials.yaml",
        "config.secrets.yml",
    )

    data class SecretMatch(
        val file: File,
        val lineNumber: Int,
        val line: String,
        val secretType: String,
        val matchedText: String,
        val severity: Severity
    )

    enum class Severity {
        CRITICAL, // Private keys, AWS keys, etc.
        HIGH,     // API tokens, passwords
        MEDIUM,   // Generic secrets
        LOW       // Potential secrets
    }

    data class ScanResult(
        val matches: List<SecretMatch>,
        val scannedFiles: Int,
        val totalLines: Long,
        val durationMs: Long
    ) {
        val hasSecrets: Boolean = matches.isNotEmpty()
        val criticalCount: Int = matches.count { it.severity == Severity.CRITICAL }
        val highCount: Int = matches.count { it.severity == Severity.HIGH }
        val mediumCount: Int = matches.count { it.severity == Severity.MEDIUM }
        val lowCount: Int = matches.count { it.severity == Severity.LOW }
    }

    suspend fun scanRepository(repoPath: File): ScanResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val matches = mutableListOf<SecretMatch>()
        var scannedFiles = 0
        var totalLines = 0L

        val files = findScannableFiles(repoPath)
        
        for (file in files) {
            if (shouldSkipFile(file)) continue
            
            try {
                val result = scanFile(file)
                matches.addAll(result)
                scannedFiles++
                totalLines += countLines(file)
            } catch (e: Exception) {
                // Skip unreadable files
            }
        }

        ScanResult(
            matches = matches,
            scannedFiles = scannedFiles,
            totalLines = totalLines,
            durationMs = System.currentTimeMillis() - startTime
        )
    }

    suspend fun scanFile(file: File): List<SecretMatch> = withContext(Dispatchers.IO) {
        val matches = mutableListOf<SecretMatch>()
        
        if (!file.exists() || file.isDirectory || file.length() > 10_000_000) { // 10MB limit
            return@withContext matches
        }

        try {
            file.readLines().forEachIndexed { index, line ->
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("//") || trimmedLine.startsWith("#")) return@forEachIndexed

                // Check file name patterns first
                if (isSensitiveFileName(file.name)) {
                    matches.add(SecretMatch(
                        file = file,
                        lineNumber = index + 1,
                        line = line,
                        secretType = "Sensitive File",
                        matchedText = file.name,
                        severity = Severity.HIGH
                    ))
                }

                // Check content patterns
                for ((secretType, pattern) in secretPatterns) {
                    val matcher = pattern.matcher(line)
                    while (matcher.find()) {
                        val matchedText = matcher.group()
                        val severity = determineSeverity(secretType)
                        matches.add(SecretMatch(
                            file = file,
                            lineNumber = index + 1,
                            line = line,
                            secretType = secretType,
                            matchedText = maskSecret(matchedText),
                            severity = severity
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            // Skip binary/unreadable files
        }

        matches
    }

    private fun findScannableFiles(root: File): List<File> {
        val files = mutableListOf<File>()
        val queue = mutableListOf(root)
        
        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            if (current.isDirectory) {
                // Skip .git, node_modules, build, .gradle, etc.
                if (!isIgnoredDirectory(current.name)) {
                    current.listFiles()?.forEach { queue.add(it) }
                }
            } else if (isTextFile(current)) {
                files.add(current)
            }
        }
        return files
    }

    private fun isIgnoredDirectory(name: String): Boolean {
        return name in setOf(".git", "node_modules", "build", ".gradle", "gradle", "target", "dist", "out", ".idea", ".vscode", "__pycache__", ".pytest_cache", "venv", "env", ".venv")
    }

    private fun isTextFile(file: File): Boolean {
        val textExtensions = setOf(
            "kt", "java", "js", "ts", "tsx", "jsx", "py", "rb", "go", "rs", "swift", "cpp", "c", "h", "hpp",
            "json", "yaml", "yml", "xml", "toml", "ini", "cfg", "conf", "config",
            "md", "txt", "rst", "adoc",
            "sh", "bash", "zsh", "fish", "ps1", "bat", "cmd",
            "sql", "graphql", "proto",
            "dockerfile", "Dockerfile",
            "gradle", "maven", "pom",
            "properties", "props",
            "env", "envrc",
            "tf", "tfvars", "hcl",
            "css", "scss", "sass", "less",
            "html", "htm", "vue", "svelte"
        )
        val ext = file.extension.lowercase()
        return ext in textExtensions || file.nameWithoutExtension in setOf("Dockerfile", "Makefile", "Jenkinsfile", "Vagrantfile")
    }

    private fun shouldSkipFile(file: File): Boolean {
        return file.length() > 10_000_000 || isBinaryFile(file)
    }

    private fun isBinaryFile(file: File): Boolean {
        try {
            val bytes = file.readBytes(8192)
            for (b in bytes) {
                if (b == 0) return true // Null byte indicates binary
            }
            return false
        } catch (e: Exception) {
            return true
        }
    }

    private fun isSensitiveFileName(fileName: String): Boolean {
        val lowerName = fileName.lowercase()
        return filePatterns.any { pattern ->
            if (pattern.startsWith("*.")) {
                lowerName.endsWith(pattern.substring(1))
            } else {
                lowerName == pattern.lowercase()
            }
        }
    }

    private fun determineSeverity(secretType: String): Severity {
        return when {
            secretType.contains("Private Key", ignoreCase = true) -> Severity.CRITICAL
            secretType.contains("AWS", ignoreCase = true) -> Severity.CRITICAL
            secretType.contains("GitHub", ignoreCase = true) -> Severity.CRITICAL
            secretType.contains("Token", ignoreCase = true) -> Severity.HIGH
            secretType.contains("Key", ignoreCase = true) -> Severity.HIGH
            secretType.contains("Secret", ignoreCase = true) -> Severity.HIGH
            secretType.contains("Password", ignoreCase = true) -> Severity.HIGH
            secretType.contains("Auth", ignoreCase = true) -> Severity.HIGH
            secretType.contains("JWT", ignoreCase = true) -> Severity.HIGH
            secretType == "Sensitive File" -> Severity.HIGH
            else -> Severity.MEDIUM
        }
    }

    private fun maskSecret(secret: String): String {
        if (secret.length <= 8) return "****"
        return secret.take(4) + "****" + secret.takeLast(4)
    }

    private fun countLines(file: File): Long {
        return try {
            file.useLines { it.count() }.toLong()
        } catch (e: Exception) {
            0L
        }
    }
}