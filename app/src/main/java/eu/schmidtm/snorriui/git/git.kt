package eu.schmidtm.snorriui.git

import eu.schmidtm.snorriui.StatusTarget
import java.io.File
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import timber.log.Timber

data class GitRepo(val url: String, val user: String, val password: String)

/**
 * This method fetches or updates the data. There are three possible states here:
 * - no data at all: clone 1st time
 * - data available, but outdated: pull repo
 * - data available and up-to-data: do nothin'
 *
 * File directory is something like this:
 * /data/user/0/eu.schmidtm.snorriui/files
 *
 */
fun fetchOrClone(repo: GitRepo, localPath: String, statusTarget: StatusTarget): String {
    Timber.i("Tempdir: $localPath")

    if (!testForLocalGitCopy(localPath)) {
        val msg = "Empty local cache, initial cloning repository"
        Timber.i(msg)
        statusTarget.informUser(msg)
        cloneRepositoryInto(repo, localPath)
        return msg
    } else {
        val lastCommitId = showLastCommit(localPath)

        if (lastCommitId.isNotEmpty()) {
            Timber.d("Git ID of last commit: $lastCommitId")

            val remoteHead = getLatestHashFromMaster(repo)
            Timber.d("Latest Hash for remote HEAD: %s", remoteHead)

            if (lastCommitId == remoteHead) {
                val msg = "Cache and remote HEAD are the same: ${lastCommitId.substring(0,6)}, do not clone."
                Timber.i(msg)
                statusTarget.informUser(msg)
                return msg
            } else {
                statusTarget.informUser("Pulling repo: ${repo.url} -> $localPath")
                return pullRepo(repo, localPath)
            }
        }
        return "Last commit ID ist empty, something is wrong"
    }
}

class PullProgressMonitor : ProgressMonitor {

    override fun update(completed: Int) {
        Timber.i("update(), completed: $completed")
    }

    override fun start(totalTasks: Int) {
        Timber.i("start(), totalTasks: $totalTasks")
    }

    override fun beginTask(title: String?, totalWork: Int) {
        Timber.i("beginTask(), Title: $title, totalWork: $totalWork")
    }

    override fun endTask() {
        Timber.i("endTask()")
    }

    override fun isCancelled(): Boolean {
        Timber.i("isCancelled()")
        return false
    }
}

fun pullRepo(repo: GitRepo, localPath: String): String {
    val localRepository = FileRepository(File(localPath + File.separator + ".git"))
    val git = Git(localRepository)
    git.reset()
    git.clean()
    val pullCmd = git.pull()

    pullCmd.setCredentialsProvider(UsernamePasswordCredentialsProvider(repo.user, repo.password))
    pullCmd.setProgressMonitor(PullProgressMonitor())
    pullCmd.call()
    return "Pulling into: $localPath"
}

fun showLastCommit(path: String): String {
    val builder = FileRepositoryBuilder()
    val repository = builder.setGitDir(File(path + "/.git"))
            .readEnvironment() // scan environment GIT_* variables
            .findGitDir() // scan up the file system tree
            .build()

    try {
        return repository.resolve(Constants.HEAD).name
    } catch (e: Exception) {
        return ""
    }
}

fun cloneRepositoryInto(repo: GitRepo, localPath: String) {

    val cloneCommand = Git.cloneRepository()
    cloneCommand.setURI(repo.url)
    cloneCommand.setCredentialsProvider(UsernamePasswordCredentialsProvider(repo.user, repo.password))

    val file = File(localPath)

    cloneCommand.setDirectory(file)
    cloneCommand.call()
}

fun getLatestHashFromMaster(repo: GitRepo): String {

    val refs = Git.lsRemoteRepository()
            .setRemote(repo.url)
            .setCredentialsProvider(UsernamePasswordCredentialsProvider(repo.user, repo.password))
            .call()

    if (refs.isNotEmpty()) {
        refs.forEach { ref ->
            Timber.d("Name: ${ref.name}, ID: ${ref.objectId}")
            if (ref.name == "HEAD") {
                return ref.objectId.name
            }
        }
    }
    return ""
}

fun testForLocalGitCopy(localPath: String): Boolean {
    return File(localPath + File.separator + ".git").exists()
}
