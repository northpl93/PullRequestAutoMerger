package pl.north93.pullrequest.automerge.step

import mu.KotlinLogging
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRepository
import pl.north93.pullrequest.automerge.worker.RunStepConfig
import pl.north93.pullrequest.automerge.strategy.TweakedResolveMergeStrategy
import java.time.Duration

class MergePullRequest(
    private val repository: GHRepository,
    private val localRepository: Git,
    private val mainPullRequest: GHPullRequest,
    private val pullRequestsToMerge: List<GHPullRequest>
) : RunStep {
    override fun run(config: RunStepConfig): RunStep {
        pullRequestsToMerge.forEach { it.refresh() }

        pullRequestsToMerge.filterNot { it.isMerged }
            .firstOrNull { it.mergeable != null }
            ?.let { processPullRequest(config, it) }

        return if (pullRequestsToMerge.count { !it.isMerged } > 0) {
            logger.info("There are still pull requests to merge, waiting 2500ms...")
            Wait(Duration.ofMillis(2500)) {
                MergePullRequest(repository, localRepository, mainPullRequest, pullRequestsToMerge)
            }
        } else {
            ApprovePullRequest(repository, localRepository, mainPullRequest)
        }
    }

    private fun processPullRequest(config: RunStepConfig, selectedPullRequest: GHPullRequest) {
        if (selectedPullRequest.mergeable) {
            logger.info("Merging pull request {} via github API", selectedPullRequest.title)
            selectedPullRequest.merge(null)
        } else {
            val conflictingBranchName = selectedPullRequest.head.ref
            if (wasAlreadyUpdatedLocally(conflictingBranchName)) {
                logger.info("Branch {} was already processed locally, github API returned stale info...", conflictingBranchName)
                return
            }

            logger.info("Updating pull request {} locally", selectedPullRequest.title)
            // make sure we have latest version of repo before updating branch
            localRepository.fetch()
                .setCredentialsProvider(config.getCredentials().toGitCredentials())
                .call()

            // checkout branch that conflicts with mainPullRequest
            checkoutBranch(conflictingBranchName)

            // merge mainPullRequest into this branch using our custom strategy
            mergeBranchWithCustomStrategy(mainPullRequest.head.ref)

            // push merge commit to remote
            localRepository.push()
                .setCredentialsProvider(config.getCredentials().toGitCredentials())
                .call()
        }
    }

    private fun wasAlreadyUpdatedLocally(branchName: String): Boolean {
        return localRepository.branchList().call().any { it.name == branchName }
    }

    private fun checkoutBranch(branchName: String) {
        logger.info("Checking out branch {}", branchName)
        localRepository.checkout()
            .setCreateBranch(true)
            .setName(branchName)
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
            .setStartPoint("origin/$branchName")
            .call()
    }

    private fun mergeBranchWithCustomStrategy(branchName: String) {
        logger.info("Merging branch {} into {}", branchName, localRepository.repository.branch)
        localRepository.merge()
            .include(localRepository.repository.resolve("origin/$branchName"))
            .setStrategy(TweakedResolveMergeStrategy())
            .call()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}