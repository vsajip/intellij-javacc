package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.postprocessor.SpecialTemplate
import com.github.oowekyala.jjtx.reporting.reportNonFatal
import com.github.oowekyala.jjtx.templates.vbeans.NodeVBean
import com.github.oowekyala.jjtx.util.Position
import com.github.oowekyala.jjtx.util.io.StringSource
import java.util.*


// The field names of this class are public API, because they're serialized
// They're decoupled from the real VisitorConfig though
/**
 * Configuration of a file generation task, as input under eg `jjtx.commonGen`.
 *
 * @property templateFile The path to a file containing the template. Can be a classpath resource.
 * @property template The source of a template, if present, overrides [templateFile]
 * @property formatter The name of a formatter to use, available formatters are listed in [FormatterRegistry]
 * @property genClassName A template evaluating to the FQCN of the class to generate.
 * @property context A map of additional context variables available in the template
 */
data class FileGenBean(
    val templateFile: String?,
    val template: String? = null,
    val formatter: String?,
    var genClassName: String?,
    val context: Map<String, Any?>?
)


// [this] is the inner model, higher precedence than the arg
fun FileGenBean.completeWith(parent: FileGenBean): FileGenBean {

    val overridesTemplate = templateFile != null || template != null

    return FileGenBean(
        templateFile = if (overridesTemplate) this.templateFile else parent.templateFile,
        template = if (overridesTemplate) this.template else parent.template,
        formatter = formatter ?: parent.formatter,
        genClassName = genClassName ?: parent.genClassName,
        context = parent.context.orEmpty() + context.orEmpty()
    )
}

// [this] is the inner model, higher precedence than the arg
fun Map<String, FileGenBean>?.completeWith(parent: Map<String, FileGenTask>) =
    completeWith(parent.mapValues { (_, v) -> v.toBean() }, emptySet())


// [this] is the inner model, higher precedence than the arg
fun Map<String, FileGenBean>?.completeWith(parent: Map<String, FileGenBean>,
                                           excludes: Collection<String> = emptyList()) =
    parent + this.orEmpty().mapValues { (id, bean) ->
        parent[id]?.let { bean.completeWith(it) } ?: bean
    } - excludes


private fun FileGenBean.getTemplate(ctx: JjtxContext,
                                    positionInfo: Position?,
                                    reportMissing: Boolean = true): StringSource? = when {
    template != null     -> StringSource.Str(template)
    templateFile != null -> StringSource.File(templateFile)
    else                 -> {
        if (reportMissing)
            ctx.messageCollector.reportNonFatal(
                "File generation task must mention either 'templateFile' or 'template'",
                positionInfo
            )
        null
    }
}

fun FileGenBean.toNodeGenScheme(ctx: JjtxContext,
                                positionInfo: Position?,
                                nodeBeans: List<NodeVBean>): NodeGenerationScheme? {

    val t = getTemplate(ctx, positionInfo) ?: return null

    return NodeGenerationScheme(
        nodeBeans = nodeBeans,
        genClassTemplate = genClassName,
        template = t,
        context = context ?: emptyMap(),
        formatter = FormatterRegistry.getOrDefault(formatter)
    )

}

/**
 * Creates a runnable [FileGenTask] from this configuration,
 * validating the parameters.
 *
 * @return A generation task if all parameters are valid. If [execute] is
 *  false, returns null.
 *
 * @throws IllegalStateException if this config is invalid
 */
fun FileGenBean.toFileGen(ctx: JjtxContext, positionInfo: Position?, id: String): FileGenTask? {

    val needsTemplate = id !in SpecialTemplate.externalGen.map { it.id }

    val t = getTemplate(ctx, positionInfo, reportMissing = needsTemplate)

    if (t == null && needsTemplate) return null

    if (genClassName == null) {
        ctx.messageCollector.reportNonFatal("File generation task '$id' must mention 'genClassName', the template for the fully qualified class name of the generated class")
        return null
    }

    return FileGenTask(
        template = t,
        formatter = FormatterRegistry.getOrDefault(formatter),
        genFqcn = genClassName!!,
        context = context ?: emptyMap()
    )
}

fun FileGenTask.toBean() =
    FileGenBean(
        templateFile = (template as? StringSource.File)?.fname,
        template = (template as? StringSource.Str)?.source,
        genClassName = genFqcn,
        formatter = formatter?.name?.toLowerCase(Locale.ROOT),
        context = context
    )