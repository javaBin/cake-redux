package no.javazone.cake.redux

import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import org.jsonbuddy.parse.JsonParser
import java.net.HttpURLConnection
import java.net.URL
import java.util.ListResourceBundle

class CheckMissingVideos {
    fun sessions():JsonArray {
        val conn:HttpURLConnection = URL("https://sleepingpill.javazone.no/public/allSessions/javazone_2022").openConnection() as HttpURLConnection
        val json:JsonObject = conn.inputStream.use { JsonParser.parseToObject(it) }
        return json.requiredArray("sessions")
    }

    fun filter(all:JsonArray):List<JsonObject> {
        val allobj:List<JsonObject> = all.objects { it }
        val res = allobj.filter { it.stringValue("video").isEmpty() && it.stringValue("format").orElse("") != "workshop" }
        return res
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val ch = CheckMissingVideos()
            val all = ch.sessions()
            val missing = ch.filter(all)
            println("Missing ${missing.size}")
            missing.forEach { println(it.requiredString("title")) }
        }
    }
}