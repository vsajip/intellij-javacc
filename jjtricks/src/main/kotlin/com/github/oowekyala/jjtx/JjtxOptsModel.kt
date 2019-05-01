package com.github.oowekyala.jjtx

import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.jjtx.templates.VisitorConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.Reader
import java.io.StringReader

/**
 * Models a jjtopts configuration file.
 *
 * @author Clément Fournier
 */
interface JjtxOptsModel {

    val parentModel: JjtxOptsModel?
    val nodePrefix: String
    val nodePackage: String
    val typeHierarchy: TypeHierarchyTree

    val visitors: List<VisitorConfig>

    companion object {

        const val DefaultRootNodeName = "Node"


        fun parse(ctx: JjtxRunContext,
                  file: File): JjtxOptsModel {

            assert(file.exists() && !file.isDirectory)

            return when (file.extension) {
                "json" -> parseJson(ctx, file.bufferedReader())
                "yaml" -> parseYaml(ctx, file.bufferedReader())
                else   -> {
                    // TODO don't swallow errors
                    default(ctx)
                }
            }

        }

        fun default(ctx: JjtxRunContext): JjtxOptsModel = JsonOptsModel(ctx, null, JsonObject())


        fun parseYaml(ctx: JjtxRunContext,
                              reader: Reader): JjtxOptsModel {
            val yaml: Any = Yaml().load(reader)
            // TODO don't swallow errors
            return parseJson(ctx, StringReader(Gson().toJson(yaml)))
        }

        fun parseJson(ctx: JjtxRunContext,
                              reader: Reader): JjtxOptsModel {

            val jsonReader = JsonReader(reader)
            jsonReader.isLenient = true

            // TODO don't swallow errors
            val jsonParser = JsonParser()
            return jsonParser.parse(jsonReader)?.asJsonObject?.let { JsonOptsModel(ctx, null, it) }
                ?: default(ctx)
        }

    }
}


fun JjtxOptsModel.addPackage(simpleName:String) =
    nodePackage.let { if (it.isNotEmpty()) "$it.$simpleName" else simpleName }
