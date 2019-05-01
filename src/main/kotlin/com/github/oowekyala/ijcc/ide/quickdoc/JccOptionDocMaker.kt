package com.github.oowekyala.ijcc.ide.quickdoc

import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.grayed
import com.github.oowekyala.ijcc.ide.quickdoc.JccDocUtil.buildQuickDoc
import com.github.oowekyala.ijcc.lang.model.GenericOption
import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.ijcc.lang.model.presentValue
import com.github.oowekyala.ijcc.lang.psi.JccOptionBinding

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JccOptionDocMaker {

    fun makeDoc(binding: JccOptionBinding?,
                ctx: InlineGrammarOptions,
                opt: GenericOption<*>): String = buildQuickDoc {
        definition {
            "Option ${HtmlUtil.bold(opt.name)} " + grayed("(${opt.supportedNature.displayName})")
        }

        sections {
            section("Current value") {
                val curValue = opt.getValue(binding, ctx).presentValue()
                val default = opt.contextualDefaultValue(ctx).presentValue()

                val tail = when {
                    curValue == default -> "(=default)"
                    else                -> "(default $default)"
                }

                "$curValue ${grayed(tail)}"
            }
        }

        freeHtml {
            opt.description ?: HtmlUtil.emph("(no description)")
        }
    }

}
