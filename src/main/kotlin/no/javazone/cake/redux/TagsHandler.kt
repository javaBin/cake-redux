package no.javazone.cake.redux

import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.JsonGenerator

data class TagsToDisplay(val summary:JsonArray,val full:JsonArray)

private fun JsonObject.objectOptional(key:String):JsonObject? = objectValue(key).orElse(null)

data class TagWithAuthor(val tag:String,val author:String)

object TagsHandler {
    fun readTagsFromDataObject(dataObject: JsonObject):TagsToDisplay {
        val origTags:List<String> = dataObject.objectOptional("tags")?.requiredArray("value")?.strings()?: emptyList()
        val extendedTags:List<JsonObject> = dataObject.objectOptional("tagswithauthor")?.requiredArray("value")?.objects { it }?: emptyList()
        val tagsWithAuthor:MutableList<TagWithAuthor> = mutableListOf()
        tagsWithAuthor.addAll(extendedTags.map { TagWithAuthor(it.requiredString("tag"),it.requiredString("author")) })
        tagsWithAuthor.addAll(origTags.map { TagWithAuthor(it,"Unknown") })
        tagsWithAuthor.sortBy { it.tag }
        val summary:JsonArray = JsonArray.fromStringStream(tagsWithAuthor.map { it.tag }.toSet().sorted().stream())
        val full:JsonArray = JsonArray.fromNodeList(tagsWithAuthor.map { JsonGenerator.generate(it)})
        return TagsToDisplay(summary,full)
    }
}