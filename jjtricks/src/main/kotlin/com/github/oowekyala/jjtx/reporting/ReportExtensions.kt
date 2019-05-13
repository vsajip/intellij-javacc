package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.util.Position
import java.util.*


fun MessageCollector.reportException(throwable: Throwable,
                                     contextStr: String? = null,
                                     altMessage: String? = null,
                                     fatal: Boolean = false,
                                     position: Position? = null) {
    reportEntry(
        ReportEntry(
            thrown = throwable,
            severity = if (fatal) Severity.FAIL else Severity.NON_FATAL,
            timeStamp = Date(),
            messageCategory = if (fatal) MessageCategory.FATAL_ERROR else MessageCategory.NON_FATAL,
            positions = listOfNotNull(position),
            message = altMessage ?: throwable.message ?: throwable.javaClass.name
        )
    )
}

/**
 * Report a normal execution trace.
 */
fun MessageCollector.reportNormal(message: String) {
    report(message, MessageCategory.NORMAL_EXEC_MESSAGE)
}

/**
 * Report a non-fatal error, probably followed later by termination anyway.
 */
fun MessageCollector.reportNonFatal(message: String, position: Position?) {
    report(message, MessageCategory.NON_FATAL, position)
}

/**
 * Report a normal execution trace.
 */
fun MessageCollector.reportFatal(message: String, position: Position? = null): Nothing {
    val m = if (position == null) message else message + "\n" + position.toString()
    report(message, MessageCategory.FATAL_ERROR, position)
    // shouldn't occur!
    throw IllegalStateException(m)
}

fun MessageCollector.report(message: String,
                            category: MessageCategory,
                            vararg sourcePosition: Position?) {
    reportEntry(
        ReportEntry(
            message = message,
            messageCategory = category,
            severity = category.minSeverity,
            positions = listOfNotNull(*sourcePosition),
            timeStamp = Date(),
            thrown = null
        )
    )
}