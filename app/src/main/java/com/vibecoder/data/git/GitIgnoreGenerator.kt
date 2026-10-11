package com.vibecoder.data.git

import java.io.File

class GitIgnoreGenerator @Inject constructor() {

    private val commonIgnores = """
        # OS & Editor
        .DS_Store
        Thumbs.db
        *.swp
        *.swo
        *~
        .vscode/
        .idea/
        *.iml
        *.iws
        *.ipr
        .vs/
        *.sublime-workspace
        *.sublime-project

        # Build & Dependency
        build/
        dist/
        out/
        target/
        *.class
        *.jar
        *.war
        *.ear
        *.apk
        *.aab

        # Gradle
        .gradle/
        gradle/
        gradlew
        gradlew.bat
        build.gradle.kts
        settings.gradle.kts

        # Maven
        target/
        pom.xml.tag
        pom.xml.releaseBackup
        pom.xml.versionsBackup
        pom.xml.next
        release.properties
        dependency-reduced-pom.xml

        # Node
        node_modules/
        npm-debug.log*
        yarn-debug.log*
        yarn-error.log*
        pnpm-debug.log*
        .pnpm-store/

        # Python
        __pycache__/
        *.py[cod]
        *$py.class
        .pytest_cache/
        .coverage
        htmlcov/
        .tox/
        .venv/
        venv/
        env/
        *.egg-info/
        dist/
        build/
        Pipfile.lock
        poetry.lock

        # Go
        vendor/
        *.exe
        *.test
        *.prof

        # Rust
        target/
        Cargo.lock

        # Java
        *.class
        *.jar
        *.war
        *.ear
        hs_err_pid*

        # Logs
        *.log
        logs/
        *.log.*

        # Environment & Secrets
        .env
        .env.local
        .env.*.local
        *.pem
        *.key
        *.p12
        *.pfx
        *.jks
        *.keystore
        secrets.*
        credentials.*

        # IDE specific
        *.swp
        *.swo
        *~
        .project
        .classpath
        .settings/
    """.trimIndent()

    private val languageSpecificIgnores = mapOf(
        "Kotlin" to """
            # Kotlin
            *.kt.metadata
            .kotlin/
        """.trimIndent(),
        "Java" to """
            # Java
            *.class
            *.jar
            *.war
            *.ear
            hs_err_pid*
        """.trimIndent(),
        "JavaScript" to """
            # Node.js
            node_modules/
            npm-debug.log*
            yarn-debug.log*
            yarn-error.log*
            .pnpm-store/
            coverage/
            .nyc_output/
            *.tsbuildinfo
        """.trimIndent(),
        "TypeScript" to """
            # TypeScript
            *.tsbuildinfo
            dist/
            .turbo/
        """.trimIndent(),
        "Python" to """
            # Python
            __pycache__/
            *.py[cod]
            *$py.class
            .pytest_cache/
            .coverage
            htmlcov/
            .tox/
            .venv/
            venv/
            env/
            *.egg-info/
            dist/
            build/
            Pipfile.lock
            poetry.lock
            .mypy_cache/
            .ruff_cache/
        """.trimIndent(),
        "Go" to """
            # Go
            vendor/
            *.exe
            *.test
            *.prof
            go.work
        """.trimIndent(),
        "Rust" to """
            # Rust
            target/
            Cargo.lock
        """.trimIndent(),
        "Swift" to """
            # Swift/iOS
            DerivedData/
            *.xcuserstate
            *.xcodeproj/xcuserdata/
            *.xcworkspace/xcuserdata/
            Pods/
            .swiftpm/
            Package.resolved
        """.trimIndent(),
        "Dart" to """
            # Dart/Flutter
            .dart_tool/
            .flutter-plugins
            .flutter-plugins-dependencies
            .packages
            pubspec.lock
            build/
            *.dart_tool/
        """.trimIndent(),
        "C#" to """
            # C#/.NET
            bin/
            obj/
            *.user
            *.suo
            *.userosscache
            *.sln.docstates
            *.nuget.props
            *.nuget.targets
            packages/
        """.trimIndent(),
        "C++" to """
            # C/C++
            *.o
            *.obj
            *.exe
            *.out
            *.a
            *.so
            *.dll
            *.lib
            *.exp
            cmake-build-*/
            CMakeFiles/
            CMakeCache.txt
            *.cmake
        """.trimIndent(),
        "Ruby" to """
            # Ruby
            *.gem
            *.rbc
            /.config
            /coverage/
            /InstalledFiles
            /pkg/
            /spec/reports/
            /spec/examples.txt
            /test/tmp/
            /test/version_tmp/
            /tmp/
            .bundle/
            vendor/bundle/
            Gemfile.lock
        """.trimIndent(),
        "PHP" to """
            # PHP
            vendor/
            composer.lock
            .phpunit.result.cache
            phpstan.neon
            .php-cs-fixer.cache
        """.trimIndent(),
        "Docker" to """
            # Docker
            .docker/
            docker-compose.override.yml
        """.trimIndent(),
        "Terraform" to """
            # Terraform
            .terraform/
            *.tfstate
            *.tfstate.*
            *.tfvars
            *.tfvars.json
            .terraform.lock.hcl
        """.trimIndent(),
    )

    fun generate(projectType: ProjectType, customPatterns: List<String> = emptyList()): String {
        val builder = StringBuilder()
        builder.append(commonIgnores).append("\n\n")
        
        languageSpecificIgnores[projectType.language]?.let { builder.append("\n# ${projectType.language}\n").append(it).append("\n\n") }
        
        projectType.frameworks.forEach { framework ->
            languageSpecificIgnores[framework]?.let { builder.append("\n# $framework\n").append(it).append("\n\n") }
        }
        
        if (customPatterns.isNotEmpty()) {
            builder.append("\n# Custom\n")
            customPatterns.forEach { builder.append(it).append("\n") }
        }
        
        return builder.toString().trim() + "\n"
    }

    fun writeToRepo(repoPath: File, projectType: ProjectType, customPatterns: List<String> = emptyList()) {
        val gitignoreFile = File(repoPath, ".gitignore")
        val content = generate(projectType, customPatterns)
        gitignoreFile.writeText(content)
    }

    data class ProjectType(
        val language: String,
        val frameworks: List<String> = emptyList()
    ) {
        companion object {
            val KOTLIN = ProjectType("Kotlin", listOf("Gradle"))
            val JAVA = ProjectType("Java", listOf("Gradle", "Maven"))
            val ANDROID = ProjectType("Kotlin", listOf("Gradle", "Android"))
            val JAVASCRIPT = ProjectType("JavaScript", listOf("Node.js"))
            val TYPESCRIPT = ProjectType("TypeScript", listOf("Node.js"))
            val REACT = ProjectType("TypeScript", listOf("Node.js", "React"))
            val VUE = ProjectType("TypeScript", listOf("Node.js", "Vue"))
            val PYTHON = ProjectType("Python")
            val DJANGO = ProjectType("Python", listOf("Django"))
            val FASTAPI = ProjectType("Python", listOf("FastAPI"))
            val GO = ProjectType("Go")
            val RUST = ProjectType("Rust")
            val SWIFT = ProjectType("Swift", listOf("iOS"))
            val FLUTTER = ProjectType("Dart", listOf("Flutter"))
            val CSHARP = ProjectType("C#", listOf(".NET"))
            val CPP = ProjectType("C++", listOf("CMake"))
            val DOCKER = ProjectType("Docker")
            val TERRAFORM = ProjectType("Terraform")
            
            fun detectFromFiles(files: List<String>): ProjectType {
                val extensions = files.map { File(it).extension.lowercase() }.toSet()
                val fileNames = files.map { File(it).name.lowercase() }.toSet()
                
                return when {
                    fileNames.contains("pubspec.yaml") -> FLUTTER
                    fileNames.contains("cargo.toml") -> RUST
                    fileNames.contains("go.mod") -> GO
                    fileNames.contains("package.json") -> {
                        if (fileNames.any { it.contains("tsconfig") }) TYPESCRIPT else JAVASCRIPT
                    }
                    fileNames.contains("requirements.txt") || fileNames.contains("pyproject.toml") || fileNames.contains("setup.py") -> PYTHON
                    fileNames.contains("pom.xml") -> JAVA
                    fileNames.contains("build.gradle.kts") || fileNames.contains("settings.gradle.kts") -> KOTLIN
                    fileNames.contains("build.gradle") -> JAVA
                    extensions.contains("swift") -> SWIFT
                    extensions.contains("cs") -> CSHARP
                    extensions.contains("cpp") || extensions.contains("cc") || extensions.contains("cxx") -> CPP
                    fileNames.contains("dockerfile") -> DOCKER
                    extensions.contains("tf") -> TERRAFORM
                    else -> ProjectType("Generic")
                }
            }
        }
    }
}