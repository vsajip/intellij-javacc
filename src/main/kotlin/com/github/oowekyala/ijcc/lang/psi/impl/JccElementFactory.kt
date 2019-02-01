package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.insight.inspections.isJccComment
import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil


/**
 * @author Clément Fournier
 * @since 1.0
 */

object JccElementFactory {
    private fun <T : PsiElement> PsiElement.findChildOfType(clazz: Class<out T>): T? =
            PsiTreeUtil.findChildOfType(this, clazz)

    private val Project.psiManager
        get() = PsiManager.getInstance(this)


    private val Project.psiFileFactory
        get() = PsiFileFactory.getInstance(this)

    fun createEolComment(project: Project, name: String): PsiElement {
        val fileText = """
            // $name
            options {
             FOO = $name;
            }
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

        """.trimIndent()

        val file = createFile(project, fileText)

        return file.firstChild.also { assert(it.isJccComment) }
    }

    fun createOptionValue(project: Project, name: String): JccOptionValue {
        val fileText = """
            options {
             FOO = $name;
            }
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

        """.trimIndent()
        val file = createFile(project, fileText)

        return file.options!!.optionBindingList[0].optionValue!!
    }

    fun createRegexReferenceUnit(project: Project, name: String): JccTokenReferenceUnit =
            createRegularExpressionReference(project, name).unit

    fun createRegularExpressionReference(project: Project, name: String): JccRegularExpressionReference {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            void foo(): {} { $name }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first()
            .let { it as JccBnfProduction }
            .expansion
            .let { it as JccRegexpExpansionUnit }
            .let { it.regularExpression as JccRegularExpressionReference }
    }


    fun createBracedExpansionUnit(project: Project, name: String): JccOptionalExpansionUnit =
            createBnfExpansion(project, name).let { it as JccOptionalExpansionUnit }

    fun createParenthesizedExpansionUnit(project: Project, name: String): JccParenthesizedExpansionUnit =
            createBnfExpansion(project, name).let { it as JccParenthesizedExpansionUnit }


    fun createLiteralRegexUnit(project: Project, name: String): JccLiteralRegexpUnit {
        return createBnfExpansion(project, name)
            .let { it as JccRegexpExpansionUnit }
            .let { it.regularExpression as JccLiteralRegularExpression }
            .let { it.unit }
    }

    fun createBnfExpansion(project: Project, name: String): JccExpansion {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            void foo(): {} { $name }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first()
            .let { it as JccBnfProduction }
            .expansion!!
    }

    fun createIdentifier(project: Project, name: String): JccIdentifier {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            void $name() {} { "dummy" }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first().nameIdentifier
    }


    fun createJavaExpression(project: Project, text: String): JccJavaExpression {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            void foo() {} { LOOKAHEAD({$text}) "dummy" }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first().findChildOfType(JccLocalLookahead::class.java)!!.javaExpression!!
    }


    fun createJavaBlock(project: Project, text: String): JccJavaBlock {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            JAVACODE void foo() $text
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first().javaBlock!!
    }


    fun createAssignmentLhs(project: Project, text: String): JccJavaAssignmentLhs {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            void foo(): {} {
                $text = hello()
            }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first()
            .let { it as JccBnfProduction }
            .expansion
            .let { it as JccAssignedExpansionUnit }
            .let { it.javaAssignmentLhs }
    }


    fun createJavaNonterminalHeader(project: Project, text: String): JccJavaNonTerminalProductionHeader {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            JAVACODE $text {}
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first().header
    }

    fun createJcu(project: Project, text: String): JccJavaCompilationUnit {
        val fileText = """
            PARSER_BEGIN(dummy)
                $text
            PARSER_END(dummy)
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.parserDeclaration!!.javaCompilationUnit!!
    }

    fun createJavaMethodForNonterminal(project: Project, header: JccJavaNonTerminalProductionHeader): PsiMethod {
        val text = """
            class Foo {
                ${header.toJavaMethodHeader()} {

                }
            }
        """.trimIndent()

        return project.psiFileFactory.createFileFromText("dummy.java", JavaFileType.INSTANCE, text)
            .findChildOfType(PsiMethod::class.java)!!
    }

    fun createFile(project: Project, text: String): JccFile =
            project.psiFileFactory.createFileFromText("dummy.javacc", JavaccFileType, text) as JccFile

    /**
     * Create from an AST node, used by the parser.
     */
    fun createElement(node: ASTNode): PsiElement = JavaccTypes.Factory.createElement(node)
}


