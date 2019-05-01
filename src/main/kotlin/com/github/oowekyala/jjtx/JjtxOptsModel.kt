package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.jjtx.templates.VisitorConfig
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.Reader

/**
 * Models a jjtopts configuration file.
 *
 * @author Clément Fournier
 */
interface JjtxOptsModel : IGrammarOptions {

    val parentModel: JjtxOptsModel?
    override val nodePrefix: String
    override val nodePackage: String
    override val isDefaultVoid: Boolean
    val typeHierarchy: TypeHierarchyTree

    val templateContext: Map<String, Any>

    val visitors: List<VisitorConfig>

    companion object {

        const val DefaultRootNodeName = "Node"


        fun parse(ctx: JjtxContext,
                  file: File,
                  parent: JjtxOptsModel): JjtxOptsModel? {

            assert(file.exists() && !file.isDirectory)

            return when (file.extension) {
                "json" -> parseJson(ctx, file.bufferedReader(), parent)
                "yaml" -> parseYaml(ctx, file.bufferedReader(), parent)
                else   -> null
            }

        }


        fun parseYaml(ctx: JjtxContext,
                      reader: Reader,
                      parent: JjtxOptsModel): JjtxOptsModel? {
            val yaml: Any = Yaml().load(reader)

            val json = GsonBuilder().also {
                it.setLenient()
            }.create().toJsonTree(yaml)

            // TODO don't swallow errors
            return fromElement(ctx, json, parent)
        }

        fun parseJson(ctx: JjtxContext,
                      reader: Reader,
                      parent: JjtxOptsModel): JjtxOptsModel? {

            val jsonReader = JsonReader(reader)
            jsonReader.isLenient = true

            // TODO don't swallow errors
            val jsonParser = JsonParser()
            return fromElement(ctx, jsonParser.parse(jsonReader), parent)
        }

        private fun fromElement(ctx: JjtxContext,
                                jsonElement: JsonElement?,
                                parent: JjtxOptsModel) =
            jsonElement?.asJsonObject?.let { JsonOptsModel(ctx, parent, it) }

    }
}


fun JjtxOptsModel.addPackage(simpleName: String) =
    nodePackage.let { if (it.isNotEmpty()) "$it.$simpleName" else simpleName }
