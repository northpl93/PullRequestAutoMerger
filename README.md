PullRequestAutoMerger
=====================
The tool that automatically merges a set of pull requests into a big one, so you can save time and avoid clutter in the main branch.
This application implements a custom merging algorithm optimised to handle Dependabot's pull request.

## Execution steps
1. Select all relevant pull requests and one _main_ pull request
    * If there is less than two pull requests then process stops, because there is nothing to merge
2. Switch base branch of all pull requests (except _main_'s) to _main_'s pull request
3. Try to merge all pull requests into _main_ pull request
    * If branch can be cleanly merged, then application calls GitHub API to do so
    * If there is a conflict, then _main_ pull request's branch is merged into processed pull request's branch locally using custom algorithm (see below).
      Then another attempt to merge via GitHub API is made.
4. When there are no more pull requests to merge then application waits for completion of all checks and approves the pull request if checks passed
5. Local repository used to merge conflicting changes is removed

## Merging algorithm
The default algorithm in Git has problems with automatically merging changes to two consequent lines.
Below is an example of a changeset that will fail to merge automatically.

<table>
<tbody>
<tr><td>Pull request A's head</td></tr>
<tr>
<td>

```diff
dependencies {
-   implementation("org.slf4j:slf4j-simple:2.0.7")
+   implementation("org.slf4j:slf4j-simple:2.0.8")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
}
```

</td>
</tr>
<tr><td>Base version</td></tr>
<tr>
<td>

```diff
dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
}
```

</td>
</tr>
<tr><td>Pull request B's head</td></tr>
<tr>
<td>

```diff
dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.7")
-   implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
+   implementation("io.github.microutils:kotlin-logging-jvm:3.0.6")
}
```

</td>
</tr>
</tbody>
</table>

I used very simple algorithm that looks for changes line-by-line, so it doesn't have issues in such cases.
Such approach doesn't support adding or removing lines, but it isn't needed in pull requests created by Dependabot.

## User guide

### Requirements
* Java 17

### Compiling / downloading
This project uses Gradle as a build tool, to build execute
```
./gradlew build
```
in project's root directory. You can also download compiled binary from [releases](https://github.com/northpl93/PullRequestAutoMerger/releases).

### Usage
1. Download or compile PullRequestAutoMerger
2. Make sure you have Java installed and `PATH` is properly configured, so you can use it from the command line
3. [Generate new token](https://github.com/settings/tokens) on GitHub
4. Generate default config `java -jar PullRequestAutoMerger-1.0-SNAPSHOT.jar --copy-default-config`
5. Configure the application
   * specify a list of repositories
   * ensure the pull requests filter suits your needs
   * specify username & token
6. Launch the application `java -jar PullRequestAutoMerger-1.0-SNAPSHOT.jar`
7. Observe the logs, if everything works correctly there should be no warns/errors