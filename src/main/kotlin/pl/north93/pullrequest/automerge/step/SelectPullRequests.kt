package pl.north93.pullrequest.automerge.step

import mu.KotlinLogging
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRepository
import pl.north93.pullrequest.automerge.worker.RunStepConfig
import pl.north93.pullrequest.automerge.config.PullRequestFilterConfig
import java.lang.StringBuilder

class SelectPullRequests(
    private val repository: GHRepository,
    private val filterConfig: PullRequestFilterConfig
) : RunStep {
    override fun run(config: RunStepConfig): RunStep? {
        logger.info("Selecting pull requests in repo {}", repository.fullName)
        val selectedPullRequests = repository.getPullRequests(GHIssueState.OPEN)
            .selectPullRequests(filterConfig)

        if (selectedPullRequests.size < 2) {
            logger.info("Not enough pull request in repo {}", repository.fullName)
            return null
        }

        val mainPullRequest = selectedPullRequests.sortedBy { it.title }.first()
        val pullRequestsToMerge = selectedPullRequests.filterNot { it == mainPullRequest }

        logSelectedPullRequests(mainPullRequest, pullRequestsToMerge)
        return SwitchBaseBranches(repository, mainPullRequest, pullRequestsToMerge)
    }

    private fun List<GHPullRequest>.selectPullRequests(
        filterConfig: PullRequestFilterConfig
    ): List<GHPullRequest> {
        return filter { pullRequest ->
            filterConfig.title?.let {
                if (! pullRequest.title.contains(it)) {
                    return@filter false
                }
            }

            filterConfig.branch?.let {
                if (! pullRequest.head.ref.contains(it)) {
                    return@filter false
                }
            }

            return@filter with(filterConfig.labels) {
                !(isNotEmpty() && pullRequest.hasLabel(this))
            }
        }
    }

    private fun GHPullRequest.hasLabel(labels: Set<String>): Boolean =
        this.labels.map { it.name }.intersect(labels).isNotEmpty()

    private fun logSelectedPullRequests(mainPullRequest: GHPullRequest, pullRequestsToMerge: List<GHPullRequest>) {
        val message = StringBuilder()

        message.appendLine()
        message.appendLine("Selected pull requests in ${repository.fullName}")
        message.appendLine("Main: ${mainPullRequest.title} (${mainPullRequest.head.ref})")
        message.appendLine("Pull requests to merge:")
        pullRequestsToMerge.forEach {
            message.appendLine("* ${it.title} (${it.head.ref})")
        }

        logger.info(message.toString())
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}