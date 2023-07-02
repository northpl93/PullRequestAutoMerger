package pl.north93.pullrequest.automerge.step

import mu.KotlinLogging
import org.eclipse.jgit.api.Git
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRepository
import pl.north93.pullrequest.automerge.worker.RunStepConfig
import java.io.File

class CloneRepository(
    private val repository: GHRepository,
    private val mainPullRequest: GHPullRequest,
    private val pullRequestsToMerge: List<GHPullRequest>
) : RunStep {
    override fun run(config: RunStepConfig): RunStep {
        val targetDirectory = File(config.getWorkdir(), repository.name)
        logger.info("Cloning repository {} to {}", repository.fullName, targetDirectory)

        val gitRepository = Git.cloneRepository()
            .setURI(repository.httpTransportUrl)
            .setDirectory(targetDirectory)
            .setCredentialsProvider(config.getCredentials().toGitCredentials())
            .setCloneAllBranches(true)
            .call()

        configureRepository(gitRepository)
        return MergePullRequest(repository, gitRepository, mainPullRequest, pullRequestsToMerge)
    }

    private fun configureRepository(gitRepository: Git) {
        val gitConfig = gitRepository.repository.config
        gitConfig.setBoolean("commit", null, "gpgsign", false)
        gitConfig.save()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}