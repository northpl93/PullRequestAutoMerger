package pl.north93.pullrequest.automerge.config

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

data class PullRequestFilterConfig(
    val title: String?,
    val branch: String?,
    @field:JsonSetter(nulls = Nulls.AS_EMPTY)
    val labels: Set<String>
)
