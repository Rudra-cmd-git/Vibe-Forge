package com.vibecoder.agent.tools

import com.vibecoder.runtime.proot.ProotRuntime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path
import okio.fs

/**
 * Bash tool - executes commands in PRoot environment
 */
class BashTool(
    private val runtime: ProotRuntime
) : Tool {
    override val name = "bash"
    override val description = "Execute a bash command in the project environment. Use for running builds, tests, git commands, etc."
    override val type = ToolActionType.Bash
    override val parameters = ToolParameters(properties = mapOf(
        "command" to ToolParameter("string", "The bash command to execute"),
        "cwd" to ToolParameter("string", "Working directory (relative to project root)", null),
        "timeout" to ToolParameter("integer", "Timeout in milliseconds (default: 60000)", null)
    ), required = listOf("command"))

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val command = args["command"] as String
        val cwd = args["cwd"] as? String ?: "."
        val timeout = (args["timeout"] as? Int) ?: 60000

        return withContext(Dispatchers.IO) {
            try {
                val result = runtime.execute(command, cwd, timeout)
                ToolResult.success(
                    output = result.output,
                    files = if (result.filesChanged.isNotEmpty()) {
                        result.filesChanged.map { ToolFile(it, FileAction.Modified) }
                    } else emptyList()
                )
            } catch (e: Exception) {
                ToolResult.error(e.message ?: "Command failed")
            }
        }
    }
}

/**
 * Read file tool
 */
class ReadFileTool(
    private val projectRoot: Path
) : Tool {
    override val name = "read_file"
    override val description = "Read the contents of a file"
    override val type = ToolActionType.ReadFile
    override val parameters = ToolParameters(properties = mapOf(
        "path" to ToolParameter("string", "Path to file relative to project root")
    ), required = listOf("path"))

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val path = args["path"] as String
        return withContext(Dispatchers.IO) {
            try {
                val file = projectRoot.resolve(path)
                if (!file.exists()) {
                    return@withContext ToolResult.error("File not found: $path")
                }
                val content = file.readText()
                ToolResult.success(output = content)
            } catch (e: Exception) {
                ToolResult.error(e.message ?: "Failed to read file")
            }
        }
    }
}

/**
 * Write file tool
 */
class WriteFileTool(
    private val projectRoot: Path
) : Tool {
    override val name = "write_file"
    override val description = "Write content to a file (creates or overwrites)"
    override val type = ToolActionType.WriteFile
    override val parameters = ToolParameters(properties = mapOf(
        "path" to ToolParameter("string", "Path to file relative to project root"),
        "content" to ToolParameter("string", "Content to write")
    ), required = listOf("path", "content"))

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val path = args["path"] as String
        val content = args["content"] as String
        return withContext(Dispatchers.IO) {
            try {
                val file = projectRoot.resolve(path)
                file.parent.createDirectories()
                file.writeText(content)
                ToolResult.success(
                    output = "File written: $path",
                    files = listOf(ToolFile(path, FileAction.Created))
                )
            } catch (e: Exception) {
                ToolResult.error(e.message ?: "Failed to write file")
            }
        }
    }
}

/**
 * Edit file tool - string replace
 */
class EditFileTool(
    private val projectRoot: Path
) : Tool {
    override val name = "edit_file"
    override val description = "Replace a string in a file with another string"
    override val type = ToolActionType.EditFile
    override val parameters = ToolParameters(properties = mapOf(
        "path" to ToolParameter("string", "Path to file relative to project root"),
        "old_string" to ToolParameter("string", "String to replace"),
        "new_string" to ToolParameter("string", "String to replace with")
    ), required = listOf("path", "old_string", "new_string"))

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val path = args["path"] as String
        val oldString = args["old_string"] as String
        val newString = args["new_string"] as String
        return withContext(Dispatchers.IO) {
            try {
                val file = projectRoot.resolve(path)
                if (!file.exists()) {
                    return@withContext ToolResult.error("File not found: $path")
                }
                val content = file.readText()
                if (!content.contains(oldString)) {
                    return@withContext ToolResult.error("String not found in file: $oldString")
                }
                val newContent = content.replace(oldString, newString)
                file.writeText(newContent)
                ToolResult.success(
                    output = "File edited: $path",
                    files = listOf(ToolFile(path, FileAction.Modified))
                )
            } catch (e: Exception) {
                ToolResult.error(e.message ?: "Failed to edit file")
            }
        }
    }
}

/**
 * List directory tool
 */
class ListDirTool(
    private val projectRoot: Path
) : Tool {
    override val name = "list_dir"
    override val description = "List contents of a directory"
    override val type = ToolActionType.ListDir
    override val parameters = ToolParameters(properties = mapOf(
        "path" to ToolParameter("string", "Path to directory relative to project root (default: .)")
    ), required = emptyList())

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val path = args["path"] as? String ?: "."
        return withContext(Dispatchers.IO) {
            try {
                val dir = projectRoot.resolve(path)
                if (!dir.exists() || !dir.isDirectory) {
                    return@withContext ToolResult.error("Directory not found: $path")
                }
                val entries = dir.list().map { entry ->
                    val name = entry.name
                    val isDir = entry.isDirectory
                    "${if (isDir) "📁 " else "📄 "}$name"
                }.joinToString("\n")
                ToolResult.success(output = entries)
            } catch (e: Exception) {
                ToolResult.error(e.message ?: "Failed to list directory")
            }
        }
    }
}

/**
 * Search code tool
 */
class SearchCodeTool(
    private val projectRoot: Path
) : Tool {
    override val name = "search_code"
    override val description = "Search for text/code across files in the project"
    override val type = ToolActionType.SearchCode
    override val parameters = ToolParameters(properties = mapOf(
        "query" to ToolParameter("string", "Search query (regex supported)"),
        "path" to ToolParameter("string", "Directory to search (default: project root)"),
        "file_pattern" to ToolParameter("string", "File pattern to match (e.g., *.kt, *.tsx)")
    ), required = listOf("query"))

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val query = args["query"] as String
        val path = args["path"] as? String ?: "."
        val filePattern = args["file_pattern"] as? String
        return withContext(Dispatchers.IO) {
            try {
                val dir = projectRoot.resolve(path)
                if (!dir.exists()) {
                    return@withContext ToolResult.error("Directory not found: $path")
                }

                val results = mutableListOf<String>()
                val pattern = filePattern?.let { globToRegex(it) }

                fun searchFile(file: Path) {
                    if (pattern != null && !file.name.matches(pattern)) return
                    try {
                        val content = file.readText()
                        content.lines().forEachIndexed { index, line ->
                            if (line.contains(query, ignoreCase = true)) {
                                results.add("${file.relativeTo(projectRoot)}:${index + 1}: $line")
                            }
                        }
                    } catch (e: Exception) {
                        // Skip binary files
                    }
                }

                fun walk(dir: Path) {
                    dir.list().forEach { entry ->
                        if (entry.isDirectory) walk(entry) else searchFile(entry)
                    }
                }

                walk(dir)

                ToolResult.success(output = if (results.isEmpty()) "No matches found" else results.joinToString("\n"))
            } catch (e: Exception) {
                ToolResult.error(e.message ?: "Search failed")
            }
        }
    }

    private fun globToRegex(glob: String): kotlin.text.Regex {
        val escaped = glob
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".")
        return kotlin.text.Regex("^$escaped\$")
    }
}

/**
 * Web fetch tool
 */
class WebFetchTool : Tool {
    override val name = "web_fetch"
    override val description = "Fetch content from a URL"
    override val type = ToolActionType.WebFetch
    override val parameters = ToolParameters(properties = mapOf(
        "url" to ToolParameter("string", "URL to fetch"),
        "method" to ToolParameter("string", "HTTP method (GET, POST)", listOf("GET", "POST")),
        "headers" to ToolParameter("object", "HTTP headers as key-value pairs"),
        "body" to ToolParameter("string", "Request body for POST")
    ), required = listOf("url"))

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val url = args["url"] as String
        val method = args["method"] as? String ?: "GET"
        // In real implementation, use OkHttp to fetch
        // This is a placeholder
        return withContext(Dispatchers.IO) {
            try {
                // Simulated fetch
                ToolResult.success(output = "Fetched content from $url (${method})")
            } catch (e: Exception) {
                ToolResult.error(e.message ?: "Fetch failed")
            }
        }
    }
}

/**
 * Git commit tool
 */
class GitCommitTool(
    private val runtime: ProotRuntime
) : Tool {
    override val name = "git_commit"
    override val description = "Commit changes to git"
    override val type = ToolActionType.GitCommit
    override val parameters = ToolParameters(properties = mapOf(
        "message" to ToolParameter("string", "Commit message"),
        "add_all" to ToolParameter("boolean", "Stage all changes (default: true)")
    ), required = listOf("message"))

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val message = args["message"] as String
        val addAll = args["add_all"] as? Boolean ?: true

        return withContext(Dispatchers.IO) {
            try {
                val commands = mutableListOf<String>()
                if (addAll) commands.add("git add -A")
                commands.add("git commit -m \"$message\"")
                val result = runtime.execute(commands.joinToString(" && "), ".", 30000)
                ToolResult.success(output = result.output)
            } catch (e: Exception) {
                ToolResult.error(e.message ?: "Git commit failed")
            }
        }
    }
}

/**
 * Tool factory - creates all tools for a project
 */
class ToolFactory(
    private val runtime: ProotRuntime,
    private val projectRoot: okio.Path
) {
    fun createAll(): List<Tool> = listOf(
        BashTool(runtime),
        ReadFileTool(projectRoot),
        WriteFileTool(projectRoot),
        EditFileTool(projectRoot),
        ListDirTool(projectRoot),
        SearchCodeTool(projectRoot),
        WebFetchTool(),
        GitCommitTool(runtime)
    )
}