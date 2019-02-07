package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.ide.inspections.BnfStringCanNeverBeMatchedInspection.Companion.problemDescription

/**
 * @author Clément Fournier
 * @since 1.1
 */
class BnfStringCanNeverBeMatchedInspectionTest : JccInspectionTestBase(BnfStringCanNeverBeMatchedInspection()) {


    private fun warning(content: String, missedMatchName:String?, realMatchName: String?) =
            warningAnnot(content, problemDescription(content, missedMatchName, realMatchName))

    fun `test neg`() = checkByText(
        """
            $DummyHeader

           TOKEN: {
               <FOO: "foo">
             | <BAR: "bar" | "foo">
           }

           void Foo():
           {}
           {
               "foo"
           }


        """
    )

    fun `test pos`() = checkByText(
        """
            $DummyHeader

           TOKEN: {
               <BAR: "bar" | "foo">
             | <FOO: "foo">
           }

           void Foo():
           {}
           {
               ${warning("\"foo\"", "FOO", "BAR")}
           }
        """
    )


    fun `test non-literal override neg`() = checkByText(
        """
            $DummyHeader

           TOKEN: {
               < "foo" > // has higher precedence
             | < NCNAME: (["a"-"z"])+ >
           }

           void Foo():
           {}
           {
               "foo"
           }
        """
    )

    fun `test non-literal override pos`() = checkByText(
        """
            $DummyHeader

           TOKEN: {
                < NCNAME: (["a"-"z"])+ >
           }

           void Foo():
           {}
           {
               ${warning("\"foo\"", null, "NCNAME")}
           }
        """
    )


    fun `test in different state neg`() = checkByText(
        """
           $DummyHeader

           <A> TOKEN: {
               <NCNAME: (["a"-"z"])+ >
           }

           void Foo():
           {}
           {
              "foo"
           }
        """
    )


    fun `test in explicit default state pos`() = checkByText(
        """
           $DummyHeader

           <DEFAULT> TOKEN: {
               <NCNAME: (["a"-"z"])+ >
           }

           void Foo():
           {}
           {
               ${warning("\"foo\"", null, "NCNAME")}
           }
        """
    )

    fun `test synthetic has precedence neg`() = checkByText(
        """
            $DummyHeader


           void Foo():
           {}
           {
               "foo"
           }
           
           TOKEN: {
             < NCNAME: (["a"-"z"])+ >
           }
        """
    )


}