package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.lang.psi.JccPsiElement
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import com.intellij.testFramework.PlatformTestUtil.assertTreeEqual
import com.intellij.testFramework.PlatformTestUtil.expandAll
import com.intellij.ui.RowIcon
import junit.framework.TestCase
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.1
 */
class JccStructureViewTest : JccTestBase() {
    fun `test tokens`() = doTest(
        """
        $DummyHeader

        <Astate> TOKEN: {
              <FOO: "hello">
            | <BAR: "hye"> { foo(); }
        }


        <Bstate> SPECIAL_TOKEN: {
              <SPECIAL: "SPECIAL"> : Astate
        }



    """, """
        -dummy.jjt
         class Dummy
         -TOKEN
          <FOO : "hello">
          <BAR : "hye">
         -SPECIAL_TOKEN
          <SPECIAL : "SPECIAL">
    """
    )

    fun `test synthetic tokens`() = doTest(
        """
        $DummyHeader

        <Astate> TOKEN: {
              <FOO: "hello">
            | <BAR: "hye"> { foo(); }
        }

        void Foo():
        {}
        {
            "flabberGasted" // synthetic
            "flabberGasted" // no duplicate
        }
        """,
        """
            -dummy.jjt
             class Dummy
             -TOKEN
              <FOO : "hello">
              <BAR : "hye">
             -Foo()
              <"flabberGasted">
            """
    )


    fun `test synthetic token duplicates`() = doTest(
        """
        $DummyHeader

        TOKEN: {
              <FOO: "hello">
            | <BAR: "hye"> { foo(); }
        }

        void Foo():
        {}
        {
            "hello" // refs the explicit
            "flabberGasted" // no duplicate
        }
    """,
        """
        -dummy.jjt
         class Dummy
         -TOKEN
          <FOO : "hello">
          <BAR : "hye">
         -Foo()
          <"flabberGasted">
         """
    )


    fun `test EOF regex is not added`() = doTest(
        """
        $DummyHeader

        void Foo():
        {}
        {
            <EOF>
        }
    """,
        """
        -dummy.jjt
         class Dummy
         Foo()
         """
    )

    private fun doPresentationDataTest(@Language("JavaCC") code: String, expectedPresentableText: String,
                                       isPublic: Boolean) {
        myFixture.configureByText("main.rs", code)
        val psi = myFixture.file.children.mapNotNull { it as? JccPsiElement }.first()
        val data = psi.presentationForStructure
        TestCase.assertEquals(data.presentableText, expectedPresentableText)
        val icon = data.getIcon(false) as? RowIcon
        if (isPublic) {
            TestCase.assertNotNull(icon)
            TestCase.assertEquals(icon?.iconCount, 2);
        } else {
            if (icon != null) {
                TestCase.assertEquals(icon.iconCount, 1);
            }
        }
    }

    private fun doTest(@Language("JavaCC") code: String, expected: String) {
        val normExpected = expected.trimIndent() + "\n"
        myFixture.configureByText("dummy.jjt", code)
        myFixture.testStructureView {
            expandAll(it.tree)
            assertTreeEqual(it.tree, normExpected)
        }
    }
}