package pl.north93.pullrequest.automerge.strategy

import mu.KotlinLogging
import org.eclipse.jgit.diff.RawText
import org.eclipse.jgit.merge.MergeChunk.ConflictState
import org.eclipse.jgit.merge.MergeResult

object LinePerLineMerger {
    // this method is called from tweaked method in TweakedResolveMerger
    fun contentMerge(base: RawText, ours: RawText, theirs: RawText): MergeResult<RawText> {
        if (base.size() != ours.size() || base.size() != theirs.size()) {
            throw IllegalStateException()
        }

        val mergeResult = MergeResult(listOf(base, ours, theirs))
        for (currentLine in 0 until base.size()) {
            val differenceOurs = base.getString(currentLine) != ours.getString(currentLine)
            val differenceTheirs = base.getString(currentLine) != theirs.getString(currentLine)
            if (differenceOurs && differenceTheirs) {
                // TODO jak to obsluzyc? Ktore treeId podac? FIRST_CONFLICTING_RANGE / NEXT_CONFLICTING_RANGE?
                mergeResult.add(T_THEIRS, currentLine, currentLine, ConflictState.FIRST_CONFLICTING_RANGE)
            } else if (differenceOurs) {
                logger.debug("ours, line $currentLine, content: ${ours.getString(currentLine)}")
                mergeResult.add(T_OURS, currentLine, currentLine + 1, ConflictState.NO_CONFLICT)
            } else if (differenceTheirs) {
                logger.debug("theirs, line $currentLine, content: ${theirs.getString(currentLine)}")
                mergeResult.add(T_THEIRS, currentLine, currentLine + 1, ConflictState.NO_CONFLICT)
            } else {
                logger.debug("base, line $currentLine, content: ${base.getString(currentLine)}")
                mergeResult.add(T_BASE, currentLine, currentLine + 1, ConflictState.NO_CONFLICT)
            }
        }

        return mergeResult
    }

    private const val T_BASE = 0
    private const val T_OURS = 1
    private const val T_THEIRS = 2
    private val logger = KotlinLogging.logger {}
}