package pl.north93.pullrequest.automerge.step

import mu.KotlinLogging
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRepository
import pl.north93.pullrequest.automerge.worker.RunStepConfig

class SwitchBaseBranches(
    private val repository: GHRepository,
    private val mainPullRequest: GHPullRequest,
    private val pullRequestsToMerge: List<GHPullRequest>
) : RunStep {
    override fun run(config: RunStepConfig): RunStep {
        logger.info("Switching base branches in repository {}", repository.name)
        val newPullRequestsToMerge = pullRequestsToMerge.map {
            val newBaseBranch = mainPullRequest.head.ref
            logger.info("Switching base branch of {} to {}", it.title, newBaseBranch)
            it.setBaseBranch(newBaseBranch)
        }

        return CloneRepository(repository, mainPullRequest, newPullRequestsToMerge)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}