package com.github.oowekyala.jjtx.util.schema

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.reporting.reportFatal
import com.github.oowekyala.jjtx.reporting.reportNonFatal
import com.github.oowekyala.jjtx.util.*
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener


fun DataAstNode.validateJjtopts(ctx: JjtxContext) {
    Jjtricks.getResourceAsStream("/jjtx/schema/jjtopts.schema.json")!!.newInputStream().use { inputStream ->
        val rawSchema = JSONObject(JSONTokener(inputStream))
        val schema = SchemaLoader.load(rawSchema)
        val doc = this.toJson().toString()
        try {
            schema.validate(JSONObject(doc))
        } catch (e: ValidationException) {
            e.causingExceptions.forEach {
                val pos = jsonPointerToPosition(it.pointerToViolation).findPathIn(this as AstMap)?.position
                ctx.messageCollector.reportNonFatal(it.errorMessage!!, pos)
            }
            ctx.messageCollector.reportFatal(e.message!!)
        }
    }
}

fun jsonPointerToPosition(pointer: String): JsonPosition = JsonPosition(pointer.removePrefix("#/").split('/'))