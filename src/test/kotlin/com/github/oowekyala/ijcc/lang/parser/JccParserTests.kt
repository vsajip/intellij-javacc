package com.github.oowekyala.ijcc.lang.parser

import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.github.oowekyala.ijcc.lang.ParserTestDataPath
import com.github.oowekyala.ijcc.lang.TestResourcesPath
import com.intellij.testFramework.ParsingTestCase

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccParserTests : ParsingTestCase("", "jjt", JavaccParserDefinition) {

    fun testProductions() = doTest(true)
    fun testTokens() = doTest(true)
    fun testLookaheads() = doTest(true)

    override fun getTestDataPath(): String = ParserTestDataPath

    override fun skipSpaces(): Boolean = false
}