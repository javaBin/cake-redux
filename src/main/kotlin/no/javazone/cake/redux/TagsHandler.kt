package no.javazone.cake.redux

import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.JsonGenerator
import org.jsonbuddy.pojo.PojoMapper
import java.util.stream.Collectors

data class TagsToDisplay(val summary:JsonArray,val full:JsonArray)

private fun JsonObject.objectOptional(key:String):JsonObject? = objectValue(key).orElse(null)

data class TagWithAuthor(val tag:String="",val author:String="")

object TagsHandler {
    fun readTagsFromDataObject(dataObject: JsonObject):TagsToDisplay {
        val extendedTagsNull:List<JsonObject>? = dataObject.objectOptional("tagswithauthor")?.requiredArray("value")?.objects { it }
        val origTags:List<String> = if (extendedTagsNull != null) emptyList() else dataObject.objectOptional("tags")?.requiredArray("value")?.strings()?: emptyList()


        val extendedTags = extendedTagsNull?: emptyList()

        val tagsWithAuthor:MutableList<TagWithAuthor> = mutableListOf()
        tagsWithAuthor.addAll(extendedTags.map { TagWithAuthor(it.requiredString("tag"),it.requiredString("author")) })
        tagsWithAuthor.addAll(origTags.map { TagWithAuthor(it,"Unknown") })
        tagsWithAuthor.sortBy { it.tag }
        val summary:JsonArray = JsonArray.fromStringStream(tagsWithAuthor.map { it.tag }.toSet().sorted().stream())
        val full:JsonArray = JsonArray.fromNodeList(tagsWithAuthor.map { JsonGenerator.generate(it)})
        return TagsToDisplay(summary,full)
    }

    fun computeTagChanges(newValues:List<TagWithAuthor>,origfromsp:JsonArray):JsonArray? {
        val origtags:Set<TagWithAuthor> = origfromsp.objectStream().map { PojoMapper.map(it,TagWithAuthor::class.java) }.collect(Collectors.toSet())
        val newValueSet:Set<TagWithAuthor> = newValues.toSet()
        if (newValueSet == origtags) {
            return null
        }
        return JsonArray.fromNodeList(newValueSet.map { JsonGenerator.generate(it) })
    }
}