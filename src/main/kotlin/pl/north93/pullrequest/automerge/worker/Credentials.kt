package pl.north93.pullrequest.automerge.worker

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

data class Credentials(val username: String, val password: String) {
    fun toGitCredentials(): UsernamePasswordCredentialsProvider = UsernamePasswordCredentialsProvider(username, password)
}