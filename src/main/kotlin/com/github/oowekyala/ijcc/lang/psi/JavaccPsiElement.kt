package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.insight.model.JavaccConfig
import com.intellij.psi.NavigatablePsiElement

/**
 * Any javacc psi element.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JavaccPsiElement : NavigatablePsiElement {

    override fun getContainingFile(): JccFile
    val javaccConfig: JavaccConfig

}