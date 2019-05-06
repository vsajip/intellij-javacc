package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.util.*
import java.nio.file.Path
import java.nio.file.Paths


/**
 * TODO remove, doesn't know the full config chain
 *
 * @author Clément Fournier
 */
class JjtxLightContext(grammarFile: JccFile) : JjtxContext(grammarFile, grammarFile.defaultJjtopts()) {

    override val io: Io = Io()

    override val messageCollector = object : MessageCollector {
        override fun report(message: String,
                            category: ErrorCategory,
                            severityOverride: Severity?,
                            vararg sourcePosition: Position?): Severity {
            // do nothing
            return severityOverride ?: category.minSeverity
        }

    }

}

fun JccFile.defaultJjtopts(): List<Path> {

    val myPath = Paths.get(virtualFile.path)
    val grammarName = myPath.fileName

    val opts =
        myPath.resolveSibling("$grammarName.jjtopts").takeIf { it.isFile() }
            ?: myPath.resolveSibling("$grammarName.jjtopts.yaml").takeIf { it.isFile() }

    return listOfNotNull(opts)

}
