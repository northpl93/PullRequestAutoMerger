package pl.north93.pullrequest.automerge.step

import mu.KotlinLogging
import org.eclipse.jgit.api.Git
import org.kohsuke.github.GHCheckRun
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHPullRequestReviewEvent
import org.kohsuke.github.GHRepository
import pl.north93.pullrequest.automerge.worker.RunStepConfig
import java.time.Duration

class ApprovePullRequest(
    private val repository: GHRepository,
    private val localRepository: Git,
    private val mainPullRequest: GHPullRequest
) : RunStep {
    override fun run(config: RunStepConfig): RunStep {
        val checkRuns = repository.getCheckRuns(mainPullRequest.head.ref).toList()
        return if (checkRuns.allCompleted()) {
            logger.info("All checks completed in {}", repository.name)
            if (checkRuns.allSuccess()) {
                logger.info("Approving pull request {}", mainPullRequest.title)
                approvePullRequest(config)
            } else {
                logger.warn("Some checks failed in pull request {}", mainPullRequest.title)
            }

            CleanupWorkdir(localRepository)
        } else {
            logger.info("Waiting for completion of checks in {}", repository.name)
            Wait(Duration.ofSeconds(10)) {
                ApprovePullRequest(repository, localRepository, mainPullRequest)
            }
        }
    }

    private fun approvePullRequest(config: RunStepConfig) {
        if (mainPullRequest.isAuthor(config.getCredentials().username)) {
            logger.warn("Author of pull request {} is same as currently operating user, can't approve pull request", mainPullRequest.title)
            return
        }

        mainPullRequest.createReview()
            .event(GHPullRequestReviewEvent.APPROVE)
            .create()
    }

    private fun List<GHCheckRun>.allCompleted() = all { it.status == GHCheckRun.Status.COMPLETED }

    private fun List<GHCheckRun>.allSuccess() = all { it.conclusion == GHCheckRun.Conclusion.SUCCESS }

    private fun GHPullRequest.isAuthor(username: String) = username == user.login

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}