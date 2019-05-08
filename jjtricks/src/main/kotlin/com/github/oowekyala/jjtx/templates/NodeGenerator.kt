package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.parse
import com.github.oowekyala.jjtx.reporting.ErrorCategory
import com.github.oowekyala.jjtx.util.*
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.util.regex.PatternSyntaxException


//    # Match predicates in order
//    # v:X template X with node generation context
//  nodeTemplates:
//    "v: $node == $grammar.rootNode":
//      - templateFile: "/jjtx/templates/RootInterface.java.vm"
//        formatter: "java"
//      - templateFile: "/jjtx/templates/NodeRoot.java.vm"
//        formatter: "java"
//    "v: !$node.subnodes.empty":
//      - templateFile: "/jjtx/templates/NodeInterface.java.vm"
//        genClassName: "$node.classQualifiedName"
//        formatter: "java"
//      - templateFile: "/jjtx/templates/NodeAbstractClass.java.vm"
//        genClassName: "${grammar.nodePackage}.Abstract${node.name}"
//        formatter: "java"
//    "r:.*":
//      - templateFile: "/jjtx/templates/Node.java.vm"
//        formatter: "java"


fun DataAstNode.toNodeGenerationScheme(ctx: JjtxContext): NodeGenerationScheme {

    // Here we're a bit more lenient


    val normalisedMap = when (this) {
        is AstMap    -> this
        is AstSeq    ->
            AstMap(
                map = mapOf("r:.*" to this),
                position = this.position
            )
        is AstScalar -> {
            ctx.messageCollector.reportError("Expected map or sequence", position)
        }
    }

    return normalisedMap.toNodeGenerationSchemeImpl(ctx)

}

private fun AstMap.toNodeGenerationSchemeImpl(ctx: JjtxContext): NodeGenerationScheme {

    val found = mutableSetOf<NodeBean>()

    val allSchemes = mutableListOf<SingleNodeGenerationScheme>()

    for ((k, node) in this) {

        val (alreadyMatched, newMatches) = k.findMatchingNodes(ctx, node.position).partition { it in found }

        found += newMatches

        if (alreadyMatched.isNotEmpty()) {
            ctx.messageCollector.report(
                "Pattern '$k' matched ${alreadyMatched.size} nodes that are already matched by some rules: "
                    + alreadyMatched.joinToString { it.name },
                ErrorCategory.DUPLICATE_MATCH,
                keyPositions[k]
            )
        }

        val schemes = when (node) {
            is AstMap -> {
                val b = node.parse<NodeGenerationBean>()
                listOfNotNull(b.promote(ctx, node.position, newMatches))
            }
            is AstSeq -> {
                val (maps, notMaps) = node.partition { it is AstMap }

                if (notMaps.isNotEmpty()) {
                    ctx.messageCollector.report(
                        "Expected a map",
                        ErrorCategory.WRONG_TYPE,
                        *notMaps.map { it.position }.toTypedArray()
                    )
                }

                maps.mapNotNull {
                    val b = node.parse<NodeGenerationBean>()
                    b.promote(ctx, node.position, newMatches)
                }
            }
            else      -> emptyList()
        }

        allSchemes += schemes

    }

    val allBeans = ctx.grammarBean.typeHierarchy.toSet()

    val remaining = allBeans - found

    if (remaining.isNotEmpty()) {
        ctx.messageCollector.report(
            "Some nodes matched no generation patterns: " + remaining.joinToString { it.name },
            ErrorCategory.UNCOVERED_NODE,
            position
        )
    }

    return NodeGenerationScheme(allSchemes)
}

fun String.findMatchingNodes(ctx: JjtxContext, positionInfo: Position?): List<NodeBean> {

    RegexPattern.matchEntire(this)?.groups?.get(1)?.run {
        return findByRegex(ctx, positionInfo, value)
    }

    TemplatePattern.matchEntire(this)?.groups?.get(1)?.run {
        return findByTemplate(ctx, positionInfo, value)
    }

    return ctx.jjtxOptsModel.typeHierarchy.descendantsOrSelf().filter { it.name == this }.toList()
}

private val RegexPattern = Regex("^r:(.*)")
private val TemplatePattern = Regex("^v:(.*)")


private fun findByTemplate(ctx: JjtxContext, positionInfo: Position?, templateStr: String): List<NodeBean> {

    val engine = VelocityEngine()

    fun NodeBean.matchesTemplate(): Boolean {

        val vctx = VelocityContext(ctx.globalVelocityContext).also {
            it["node"] = this@matchesTemplate
        }

        val fullTemplate =
            """
            #if ( $templateStr )
              true
            #end
        """.trimIndent()

        return engine.evaluate(vctx, fullTemplate).trim().isNotEmpty()
    }



    return ctx.grammarBean.typeHierarchy.filter { it.matchesTemplate() }.also {
        if (it.isEmpty()) {
            ctx.messageCollector.report(
                "Template pattern matches no nodes",
                ErrorCategory.UNMATCHED_HIERARCHY_REGEX,
                positionInfo
            )
        }
    }
}

private fun findByRegex(ctx: JjtxContext, positionInfo: Position?, regexStr: String): List<NodeBean> {

    val r = try {
        Regex(regexStr)
    } catch (e: PatternSyntaxException) {
        ctx.messageCollector.reportError(
            e.message ?: "Invalid regex",
            positionInfo
        )
    }

    val matching =
        ctx.jjtxOptsModel
            .typeHierarchy
            .descendantsOrSelf()
            .filter { r.matches(it.name) }
            .toList()

    if (matching.isEmpty()) {
        ctx.messageCollector.report(
            "Regex pattern matches no nodes",
            ErrorCategory.UNMATCHED_HIERARCHY_REGEX,
            positionInfo
        )
    }

    return matching
}


data class NodeGenerationBean(
    var formatter: String?,
    var genClassName: String?,
    var templateFile: String?,
    var context: Map<String, Any>
) {

    fun promote(ctx: JjtxContext, positionInfo: Position?, nodeBeans: List<NodeBean>): SingleNodeGenerationScheme? {


        if (templateFile == null) {
            // todo accumulate those
            ctx.messageCollector.reportNonFatal(
                "Node generation spec must mention the requested 'templateFile'",
                positionInfo
            )
            return null
        }

        if (genClassName == null) {
            ctx.messageCollector.reportNonFatal(
                "Node generation spec must mention 'genClassName', the template for the fully qualified class name of the generated class",
                positionInfo
            )
            return null
        }

        val formatterChoice = FormatterChoice.select(formatter)

        return SingleNodeGenerationScheme(
            nodeBeans,
            genClassName!!,
            templateFile!!,
            context,
            formatterChoice
        )

    }

}

data class SingleNodeGenerationScheme(
    val nodeBeans: List<NodeBean>,
    val genClassTemplate: String,
    val templateFile: String,
    val context: Map<String, Any>,
    val formatter: FormatterChoice?
)


data class NodeGenerationScheme(val templates: List<SingleNodeGenerationScheme>)
