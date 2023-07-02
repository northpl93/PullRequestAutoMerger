package pl.north93.pullrequest.automerge.config

import pl.north93.pullrequest.automerge.worker.Credentials

data class MergerApplicationConfig(
    val repositories: List<String>,
    val concurrency: Int,
    val pullRequestsFilter: PullRequestFilterConfig,
    val credentials: Credentials
)
