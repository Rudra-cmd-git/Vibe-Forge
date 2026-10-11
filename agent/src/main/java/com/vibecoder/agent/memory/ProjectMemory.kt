package com.vibecoder.agent.memory

import okio.Path
import okio.fs

/**
 * Project memory - maintains PROJECT.md and TODO.md
 */
class ProjectMemory(
    private val projectRoot: Path
) {
    private val projectMdPath = projectRoot.resolve("PROJECT.md")
    private val todoMdPath = projectRoot.resolve("TODO.md")

    /**
     * Read PROJECT.md - structure, tech choices, conventions, decisions
     */
    fun readProjectMd(): String {
        return try {
            projectMdPath.readText()
        } catch (e: Exception) {
            // Create default if not exists
            createDefaultProjectMd()
            projectMdPath.readText()
        }
    }

    /**
     * Update PROJECT.md after changes
     */
    fun updateProjectMd(updates: Map<String, String>) {
        val current = readProjectMd()
        var updated = current
        updates.forEach { (section, content) ->
            updated = updateSection(updated, section, content)
        }
        projectMdPath.writeText(updated)
    }

    /**
     * Read TODO.md - task list
     */
    fun readTodoMd(): String {
        return try {
            todoMdPath.readText()
        } catch (e: Exception) {
            createDefaultTodoMd()
            todoMdPath.readText()
        }
    }

    /**
     * Update TODO.md - check off tasks, add new ones
     */
    fun updateTodoMd(tasks: List<TodoTask>) {
        val content = tasks.map { task ->
            val checkbox = if (task.completed) "[x]" else "[ ]"
            "- $checkbox ${task.description}${if (task.details != null) " - ${task.details}" else ""}"
        }.joinToString("\n")
        val fullContent = "# TODO\n\n$content\n"
        todoMdPath.writeText(fullContent)
    }

    /**
     * Add a task to TODO.md
     */
    fun addTask(description: String, details: String? = null) {
        val tasks = parseTasks(readTodoMd())
        tasks.add(TodoTask(description, false, details))
        updateTodoMd(tasks)
    }

    /**
     * Mark task as completed
     */
    fun completeTask(index: Int) {
        val tasks = parseTasks(readTodoMd())
        if (index in tasks.indices) {
            tasks[index] = tasks[index].copy(completed = true)
            updateTodoMd(tasks)
        }
    }

    /**
     * Get next incomplete task
     */
    fun getNextTask(): TodoTask? {
        return parseTasks(readTodoMd()).firstOrNull { !it.completed }
    }

    private fun createDefaultProjectMd() {
        val default = """# Project: ${projectRoot.name}

## Structure
- \`src/\` - Source code
- \`public/\` - Static assets
- \`tests/\` - Test files

## Tech Stack
- Language: TypeScript/JavaScript
- Framework: React + Vite
- Styling: Tailwind CSS
- Build: Vite

## Conventions
- Use functional components with hooks
- Follow ESLint + Prettier config
- Write tests for new features

## Decisions
- Single-page application
- REST API backend
- PostgreSQL database

## Commands
- \`npm run dev\` - Start dev server
- \`npm run build\` - Production build
- \`npm run test\` - Run tests
"""
        projectMdPath.writeText(default)
    }

    private fun createDefaultTodoMd() {
        val default = """# TODO

- [ ] Set up project structure
- [ ] Configure build tools
- [ ] Implement core features
- [ ] Add tests
- [ ] Deploy
"""
        todoMdPath.writeText(default)
    }

    private fun updateSection(content: String, section: String, newContent: String): String {
        val pattern = "(## $section\\n)(.*?)(?=\\n## |\\z)".toRegex()
        return content.replace(pattern) { "## $section\n$newContent\n" }
    }

    private fun parseTasks(content: String): MutableList<TodoTask> {
        val tasks = mutableListOf<TodoTask>()
        content.lines().forEach { line ->
            val match = "\\-\\s+\\[([ x])\\]\\s+(.+?)(?:\\s+-\\s+(.+))?".toRegex().matchEntire(line.trim())
            if (match != null) {
                val completed = match.groupValues[1] == "x"
                val description = match.groupValues[2]
                val details = match.groupValues.getOrNull(3)
                tasks.add(TodoTask(description, completed, details))
            }
        }
        return tasks
    }
}

data class TodoTask(
    val description: String,
    val completed: Boolean,
    val details: String? = null
)