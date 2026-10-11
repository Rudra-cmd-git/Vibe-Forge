package com.vibecoder.data.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.MergeResult
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.treewalk.TreeWalk
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class JGitRepositoryManager {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    @Throws(GitAPIException::class, IOException::class)
    fun clone(
        uri: String,
        localPath: File,
        credentials: CredentialsProvider? = null,
        branch: String? = null
    ): Git {
        localPath.parentFile?.mkdirs()
        
        val cloneCommand = Git.cloneRepository()
            .setURI(uri)
            .setDirectory(localPath)
            .setCloneAllBranches(true)
        
        credentials?.let { cloneCommand.setCredentialsProvider(it) }
        branch?.let { cloneCommand.setBranch(it) }
        
        return cloneCommand.call()
    }

    @Throws(IOException::class)
    fun open(localPath: File): Git {
        val gitDir = File(localPath, ".git")
        if (!gitDir.exists()) {
            throw GitException.NotARepository(localPath.absolutePath)
        }
        val repository = RepositoryBuilder().setGitDir(gitDir).readEnvironment().findGitDir().build()
        return Git(repository)
    }

    @Throws(GitAPIException::class)
    fun init(localPath: File): Git {
        localPath.mkdirs()
        return Git.init().setDirectory(localPath).call()
    }

    @Throws(GitAPIException::class)
    fun addAll(git: Git) {
        git.add().addFilepattern(".").call()
    }

    @Throws(GitAPIException::class)
    fun add(git: Git, paths: Array<String>) {
        git.add().addFilepattern(*paths).call()
    }

    @Throws(GitAPIException::class)
    fun commit(
        git: Git,
        message: String,
        authorName: String,
        authorEmail: String
    ): RevCommit {
        val author = PersonIdent(authorName, authorEmail)
        return git.commit()
            .setMessage(message)
            .setAuthor(author)
            .setCommitter(author)
            .call()
    }

    @Throws(GitAPIException::class)
    fun commitWithFiles(
        git: Git,
        message: String,
        authorName: String,
        authorEmail: String,
        files: List<String>
    ): RevCommit {
        add(git, files.toTypedArray())
        return commit(git, message, authorName, authorEmail)
    }

    @Throws(GitAPIException::class)
    fun push(git: Git, credentials: CredentialsProvider? = null, refSpec: String? = null) {
        val pushCommand = git.push()
        credentials?.let { pushCommand.setCredentialsProvider(it) }
        refSpec?.let { pushCommand.setRefSpecs(it) }
        pushCommand.call()
    }

    @Throws(GitAPIException::class)
    fun pull(git: Git, credentials: CredentialsProvider? = null) {
        val pullCommand = git.pull()
        credentials?.let { pullCommand.setCredentialsProvider(it) }
        pullCommand.call()
    }

    @Throws(GitAPIException::class)
    fun fetch(git: Git, credentials: CredentialsProvider? = null) {
        val fetchCommand = git.fetch()
        credentials?.let { fetchCommand.setCredentialsProvider(it) }
        fetchCommand.setPrune(true)
        fetchCommand.call()
    }

    @Throws(GitAPIException::class)
    fun checkout(git: Git, branchName: String): Unit {
        git.checkout().setName(branchName).call()
    }

    @Throws(GitAPIException::class)
    fun createBranch(
        git: Git,
        branchName: String,
        startPoint: String? = null,
        force: Boolean = false
    ): Ref {
        val command = git.branchCreate().setName(branchName).setForce(force)
        startPoint?.let { command.setStartPoint(it) }
        return command.call()
    }

    @Throws(GitAPIException::class)
    fun deleteBranch(git: Git, branchName: String, force: Boolean = false) {
        git.branchDelete().setBranchNames(branchName).setForce(force).call()
    }

    @Throws(GitAPIException::class)
    fun merge(git: Git, branchName: String, message: String): MergeResult {
        return git.merge()
            .include(git.repository.resolve(branchName))
            .setCommit(true)
            .setMessage(message)
            .call()
    }

    @Throws(IOException::class)
    fun getLog(git: Git, maxCount: Int = 50): List<GitCommit> {
        val commits = mutableListOf<GitCommit>()
        val walk = RevWalk(git.repository)
        try {
            val head = git.repository.resolve(Constants.HEAD)
            if (head != null) {
                val commit = walk.parseCommit(head)
                var count = 0
                var current: RevCommit? = commit
                while (current != null && count < maxCount) {
                    commits.add(revCommitToGitCommit(current))
                    count++
                    if (current.parents.isNotEmpty()) {
                        current = walk.parseCommit(current.parents[0])
                    } else {
                        current = null
                    }
                }
            }
        } finally {
            walk.close()
        }
        return commits
    }

    @Throws(GitAPIException::class, IOException::class)
    fun getStatus(git: Git): GitStatus {
        val status = git.status().call()
        return GitStatus(
            added = status.added.toList(),
            modified = status.modified.toList(),
            removed = status.removed.toList(),
            missing = status.missing.toList(),
            untracked = status.untracked.toList(),
            changed = status.changed.toList(),
            conflicting = status.conflicting.toList(),
            hasChanges = status.added.isNotEmpty() || status.modified.isNotEmpty() ||
                        status.removed.isNotEmpty() || status.missing.isNotEmpty() ||
                        status.untracked.isNotEmpty() || status.conflicting.isNotEmpty()
        )
    }

    @Throws(GitAPIException::class, IOException::class)
    fun listBranches(git: Git): List<GitBranch> {
        val branches = mutableListOf<GitBranch>()
        val branchList = git.branchList().call()
        val currentBranch = git.repository.branch

        for (ref in branchList) {
            val name = ref.name.replace("refs/heads/", "")
            val isCurrent = name == currentBranch
            val objectId = git.repository.resolve(ref.name)
            val lastCommit = objectId?.let { 
                val walk = RevWalk(git.repository)
                try {
                    walk.parseCommit(it)
                } finally {
                    walk.close()
                }
            }
            branches.add(GitBranch(
                name = name,
                isCurrent = isCurrent,
                isRemote = false,
                upstream = git.repository.getConfig().getString("branch", name, "remote"),
                lastCommitId = lastCommit?.name,
                lastCommitMessage = lastCommit?.shortMessage
            ))
        }
        return branches
    }

    @Throws(IOException::class)
    fun listRemotes(git: Git): List<GitRemote> {
        val remotes = mutableListOf<GitRemote>()
        val config = git.repository.getConfig()
        val remoteNames = config.getSubsections("remote")
        for (name in remoteNames) {
            val url = config.getString("remote", name, "url") ?: ""
            val fetchRefSpecs = config.getStringList("remote", name, "fetch")?.map { it.toString() } ?: emptyList()
            val pushRefSpecs = config.getStringList("remote", name, "push")?.map { it.toString() } ?: emptyList()
            remotes.add(GitRemote(name, url, fetchRefSpecs, pushRefSpecs))
        }
        return remotes
    }

    @Throws(GitAPIException::class)
    fun addRemote(git: Git, name: String, url: String) {
        git.remoteAdd().setName(name).setUri(url).call()
    }

    @Throws(GitAPIException::class)
    fun removeRemote(git: Git, name: String) {
        git.remoteRemove().setName(name).call()
    }

    @Throws(GitAPIException::class)
    fun setRemoteUrl(git: Git, name: String, url: String) {
        git.remoteSetUrl().setName(name).setUri(url).call()
    }

    fun getDiff(git: Git, path: String? = null): String {
        // Simplified diff - in production use DiffFormatter
        return "Diff for ${path ?: "all changes"}"
    }

    fun getFileContent(git: Git, path: String, revision: String = "HEAD"): String? {
        try {
            val objectId = git.repository.resolve(revision)
            objectId?.let { commitId ->
                val walk = RevWalk(git.repository)
                try {
                    val commit = walk.parseCommit(commitId)
                    val treeWalk = TreeWalk(git.repository)
                    try {
                        treeWalk.addTree(commit.tree)
                        treeWalk.setRecursive(true)
                        treeWalk.setFilter(org.eclipse.jgit.treewalk.filter.PathFilter.create(path))
                        if (treeWalk.next()) {
                            val objectReader = git.repository.newObjectReader()
                            val loader = objectReader.open(treeWalk.getObjectId(0))
                            return String(loader.bytes)
                        }
                    } finally {
                        treeWalk.close()
                    }
                } finally {
                    walk.close()
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return null
    }

    fun close(git: Git) {
        git.close()
    }

    private fun revCommitToGitCommit(commit: RevCommit): GitCommit {
        val author = commit.authorIdent
        val committer = commit.committerIdent
        return GitCommit(
            id = commit.name,
            shortId = commit.abbreviate(7).name,
            message = commit.fullMessage,
            shortMessage = commit.shortMessage,
            authorName = author.name,
            authorEmail = author.emailAddress,
            authorTime = author.when.time,
            committerName = committer.name,
            committerEmail = committer.emailAddress,
            committerTime = committer.when.time,
            parents = commit.parents.map { it.name },
            treeId = commit.tree.name
        )
    }
}