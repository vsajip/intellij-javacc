package com.github.oowekyala.ijcc.lang

import com.github.oowekyala.ijcc.insight.model.GenericOption
import com.github.oowekyala.ijcc.insight.model.JccOption
import com.github.oowekyala.ijcc.insight.model.JjtOption
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccOptionsTests : LightCodeInsightFixtureTestCase() {


    override fun getTestDataPath(): String = TestResourcesPath

    fun testDefaultOverride() {
        myFixture.configureByFiles("$OptionsTestDataPath/LookaheadOverride.jjt")
        val file = myFixture.file as JccFile

        // overridden
        file.javaccConfig.lookahead shouldBe 4
    }


    fun testPackageResolution() {
        myFixture.configureByFiles("$OptionsTestDataPath/LookaheadOverride.jjt")
        val file = myFixture.file as JccFile

        file.javaccConfig.parserPackage shouldBe "org.javacc.jjtree"
        file.javaccConfig.nodePackage shouldBe file.javaccConfig.parserPackage
    }

    fun testNodePackageOverride() {
        myFixture.configureByFiles("$OptionsTestDataPath/PackageOverride.jjt")
        val file = myFixture.file as JccFile

        file.javaccConfig.parserPackage shouldBe "org.javacc.jjtree"
        file.javaccConfig.nodePackage shouldBe "org.foo"
    }

    fun testInvalidOptionType() {
        myFixture.configureByFiles("$OptionsTestDataPath/InvalidOptionType.jjt")
        val file = myFixture.file as JccFile
        val config = file.javaccConfig

        fun check(opt: GenericOption<*>) {
            val binding = file.options!!.getBindingFor(opt)!!
            binding.matchesType(opt.expectedType) shouldBe false
            opt.getValue(binding, config) shouldBe opt.getValue(null, config) // assert the default value is used
        }

        check(JjtOption.NODE_DEFAULT_VOID)
        check(JjtOption.NODE_PACKAGE)
        check(JccOption.LOOKAHEAD)
    }


}