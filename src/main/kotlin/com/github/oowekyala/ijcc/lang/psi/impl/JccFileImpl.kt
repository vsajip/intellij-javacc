package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.model.GrammarOptions
import com.github.oowekyala.ijcc.lang.model.LexicalGrammar
import com.github.oowekyala.ijcc.ide.refs.NonTerminalScopeProcessor
import com.github.oowekyala.ijcc.ide.refs.TerminalScopeProcessor
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.util.IncorrectOperationException


/**
 * File implementation.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccFileImpl(fileViewProvider: FileViewProvider) : PsiFileBase(fileViewProvider, JavaccLanguage), JccFile {

    override fun getFileType(): FileType = JavaccFileType

    override val grammarFileRoot: JccGrammarFileRoot?
        get() = findChildByClass(JccGrammarFileRoot::class.java)

    override val tokenManagerDecls: Sequence<JccTokenManagerDecls>
        get() = grammarFileRoot?.childrenSequence()?.filterIsInstance<JccTokenManagerDecls>() ?: emptySequence()

    override val regexpProductions: Sequence<JccRegularExprProduction>
        get() = grammarFileRoot?.childrenSequence()?.filterIsInstance<JccRegularExprProduction>() ?: emptySequence()

    override val parserDeclaration: JccParserDeclaration?
        get() = grammarFileRoot?.parserDeclaration

    override val nonTerminalProductions: Sequence<JccNonTerminalProduction>
        get() = grammarFileRoot?.childrenSequence()?.filterIsInstance<JccNonTerminalProduction>() ?: emptySequence()

    override val globalNamedTokens: Sequence<JccNamedRegularExpression>
        get() = globalTokenSpecs.map { it.regularExpression }.filterIsInstance<JccNamedRegularExpression>()


    override val globalTokenSpecs: Sequence<JccRegexprSpec>
        get() =
            grammarFileRoot
                ?.childrenSequence(reversed = false)
                ?.filterIsInstance<JccRegularExprProduction>()
                ?.filter { it.regexprKind.text == "TOKEN" }
                ?.flatMap { it.childrenSequence().filterIsInstance<JccRegexprSpec>() }
                ?: emptySequence()

    override val options: JccOptionSection?
        get() = grammarFileRoot?.optionSection

    override val grammarOptions: GrammarOptions by lazy {
        GrammarOptions(
            options,
            parserDeclaration
        )
    } // todo is lazy safe?

    override val lexicalGrammar: LexicalGrammar by lazy {
        LexicalGrammar(grammarFileRoot)
    }

    override fun getContainingFile(): JccFile = this

    override fun getPackageName(): String = grammarOptions.parserPackage

    override fun setPackageName(packageName: String?) {
        throw IncorrectOperationException("Cannot set the package of the parser that way")
    }

    override fun getClasses(): Array<PsiClass> {

        val injected =
                grammarFileRoot?.let { InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(it) }
                    ?.takeIf { it.isNotEmpty() }
                    ?: return emptyArray()

        return injected.mapNotNull {
            it.first.descendantSequence().map { it as? PsiClass }.firstOrNull { it != null }
        }.toTypedArray()
    }

    override fun processDeclarations(processor: PsiScopeProcessor,
                                     state: ResolveState,
                                     lastParent: PsiElement?,
                                     place: PsiElement): Boolean {
        return when (processor) {
            is NonTerminalScopeProcessor -> processor.executeUntilFound(nonTerminalProductions, state)
            is TerminalScopeProcessor    -> processor.executeUntilFound(globalTokenSpecs, state)
            else                         -> true
        }
    }

    private fun PsiScopeProcessor.executeUntilFound(list: Sequence<PsiElement>, state: ResolveState): Boolean {
        for (spec in list) {
            if (!execute(spec, state)) return false
        }
        return true
    }
}
