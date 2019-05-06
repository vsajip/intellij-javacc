package com.github.oowekyala.jjtx.typeHierarchy

import com.github.oowekyala.jjtx.*
import com.github.oowekyala.jjtx.util.ErrorCategory
import com.github.oowekyala.jjtx.util.Severity
import java.util.regex.PatternSyntaxException


fun TypeHierarchyTree.expandAllNames(grammarNodeNames: Set<String>,
                                     ctx: JjtxContext): TypeHierarchyTree =
    resolveAgainst(grammarNodeNames, ctx).first()

/**
 * Unescaped the node shorthands in the type hierarchy and produces a new
 * root.
 *
 * Shorthands are
 *
 * "r:something"      -> match all nodes with regex, can only be a leaf pattern, then expanded to package + prefix + name
 * "Something"        -> expanded to eg package.ASTSomething
 * "%Something"       -> expanded to package.Something
 * "foo.Something"    -> exactly foo.Something
 *
 */
private fun TypeHierarchyTree.resolveAgainst(grammarNodeNames: Set<String>,
                                             ctx: JjtxContext): List<TypeHierarchyTree> {


    RegexPattern.matchEntire(nodeName)?.groups?.get(1)?.run {

        val r = try {
            Regex(value)
        } catch (e: PatternSyntaxException) {
            ctx.messageCollector.report(
                e.message.orEmpty(),
                ErrorCategory.EXACT_NODE_NOT_IN_GRAMMAR,
                null,
                positionInfo
            )
            return listOf()
        }

        return resolveRegex(grammarNodeNames,r , ctx) // TODO invalid regex?
    }

    val (qname, prodName, spec) = when {
        nodeName[0] == '%'              -> {
            val short = nodeName.substring(1)
            Triple(
                ctx.jjtxOptsModel.addPackage(short),
                short,
                Specificity.QUOTED
            )
        }
        nodeName.matches(Regex("\\w+")) ->
            Triple(
                ctx.jjtxOptsModel.addPackage(ctx.jjtxOptsModel.nodePrefix + nodeName),
                nodeName,
                Specificity.RESOLVED
            )
        else                            ->
            Triple(nodeName, nodeName, Specificity.QNAME)
    }

    if (prodName !in grammarNodeNames) {
        ctx.messageCollector.report(
            qname,
            ErrorCategory.EXACT_NODE_NOT_IN_GRAMMAR,
            null,
            positionInfo
        )
    }

    val rootSpec = if (parent == null) Specificity.ROOT else spec

    return listOf(
        TypeHierarchyTree(
            qname,
            positionInfo,
            children.flatMap { it.resolveAgainst(grammarNodeNames, ctx) },
            rootSpec
        )
    )
}

private fun TypeHierarchyTree.resolveRegex(grammarNodeNames: Set<String>,
                                           extractedRegex: Regex,
                                           ctx: JjtxContext): List<TypeHierarchyTree> {


    val matching = grammarNodeNames.filter { extractedRegex.matches(it) }


    if (children.isNotEmpty()) {
        val override = if (matching.size == 1) null else Severity.FAIL
        ctx.messageCollector.report(
            extractedRegex.pattern,
            ErrorCategory.REGEX_SHOULD_BE_LEAF,
            override,
            positionInfo
        )
        if (override == Severity.FAIL) {
            return emptyList()
        }
    }


    if (matching.isEmpty()) {
        ctx.messageCollector.report(
            extractedRegex.pattern,
            ErrorCategory.UNMATCHED_HIERARCHY_REGEX,
            null,
            positionInfo
        )
    }
    return matching.map {
        TypeHierarchyTree(
            ctx.jjtxOptsModel.addPackage(ctx.jjtxOptsModel.nodePrefix + it),
            positionInfo,
            emptyList(),
            Specificity.REGEX
        )
    }

}


enum class Specificity {
    UNKNOWN,
    REGEX,
    RESOLVED,
    QUOTED,
    QNAME,
    ROOT,
}

private val RegexPattern = Regex("^r:(.*)")