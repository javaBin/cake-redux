package no.javazone.cake.redux

import no.javazone.cake.redux.mail.*
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator
import no.javazone.cake.redux.util.*
import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import java.io.File
import java.io.PrintWriter
import java.time.LocalDate
import java.time.LocalDateTime

data class FeedBackRaw(
    val slotRaw:String,
    val enjoy:Int,
    val useful:Int,
    val comment:String?
)

data class SleepingPillSpeaker(
    val name:String,
    val email:String,
)

private fun toAvg(sum:Int,count:Int):String {
    val avg = (sum * 1000) / count
    var rest = "" + (avg % 1000)
    while (rest.length < 3) {
        rest = "0" + rest
    }
    val res = "${avg / 1000},$rest"
    return res
}

private fun commentToExport(commentList:List<String>):String {
    if (commentList.isEmpty()) {
        return ""
    }
    val joined = commentList.map { it.replace("\"","\"\"") }.joinToString("\n")
    return "\"${joined}\""
}

data class FeedBack(
    val talkid:String,
    val talktype: String,
    val speakerList:List<SleepingPillSpeaker>,
    val enjoySum:Int,
    val usefulSum:Int,
    val count:Int,
    val commentList:List<String>,
    val title:String,
) {
    val enjoyavg:String = toAvg(enjoySum,count)
    val usefulavg:String = toAvg(usefulSum,count)

    // EMAIL|SPEAKERNAME|TYPE|TITLE|COUNT|AVG_ENJOY|AVG_USEFUL|COMMENTS
    val asExport:String = "${speakerList.map { it.name }.joinToString(" and ")};${speakerList.map { it.email }.joinToString(",")};$talktype;$title;$count;$enjoyavg;$usefulavg;${commentToExport(commentList)}"

    val emailParaMap:Map<FeedbackMailParameter,String> = mapOf(
        FeedbackMailParameter.SPEAKERNAME to speakerList.map { it.name }.joinToString(" and "),
        FeedbackMailParameter.TYPE to talktype,
        FeedbackMailParameter.TITLE to title,
        FeedbackMailParameter.COUNT to "$count",
        FeedbackMailParameter.AVG_ENJOY to enjoyavg,
        FeedbackMailParameter.AVG_USEFUL to usefulavg,
        FeedbackMailParameter.COMMENTS to commentList.map { "<li>$it</li>" }.joinToString("\n"),
        )

    companion object {
        val exportHeader = "SPEAKERNAME;EMAIL;TYPE;TITLE;COUNT;AVG_ENJOY;AVG_USEFUL;COMMENTS"
    }
}


data class SleepingPillData(
    val talkid:String,
    val speakerList: List<SleepingPillSpeaker>,
    val room:String,
    val startTime:LocalDateTime,
    val talktype:String,
    val title:String,
)

enum class FeedbackMailParameter {
    SPEAKERNAME,
    TYPE,
    TITLE,
    COUNT,
    AVG_ENJOY,
    AVG_USEFUL,
    COMMENTS,
}

object FeedbackReader {
    private val dates:List<Pair<String,LocalDate>> = listOf (
        Pair("Tue",LocalDate.of(2024,9,3)),
        Pair("Wed",LocalDate.of(2024,9,4)),
        Pair("Thu",LocalDate.of(2024,9,5)),
    )

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size < 4) {
            println("Usage <cakeconfig> <feedback> <outputcvs> <emailtemplate>")
            return
        }
        System.setProperty("cake-redux-config-file", args[0])
        val rawList: List<FeedBackRaw> = readRaw(args[1])
        val sleepingPillTalks:JsonArray = SleepingpillCommunicator().allTalkFromConferenceSleepingPillFormat(Configuration.videoAdminConference())
        val sleepingPillDataList:List<SleepingPillData> = sleepingPillTalks.objects { sleepingPillData(it) }.mapNotNull { it }
        val feedbackList:List<FeedBack> = generateFeedback(rawList,sleepingPillDataList)
        println("Got feedback " + feedbackList.size)
        checkMissing(sleepingPillDataList,feedbackList)
        //writExport(feedbackList,args[2])
        generateMail(feedbackList,args[3])
    }

    fun generateMail(feedbackList:List<FeedBack>,emailTemplateFilename:String) {
        val emailTemplate:String = File(emailTemplateFilename).readText()
        for (feedback in feedbackList) {
            val emailContent = StringBuilder(emailTemplate)
            for (parameter in FeedbackMailParameter.values()) {
                val vartext = "#$parameter#"
                while (true) {
                    val pos = emailContent.indexOf(vartext)
                    if (pos == -1) {
                        break
                    }
                    emailContent.replace(pos,pos+vartext.length,feedback.emailParaMap[parameter]?:"")
                }
            }
            val mailToSend = MailToSend(
                "program@java.no",
                "JavaZone",
                emptyList(),
                feedback.speakerList.map { it.email },
                "JavaZone talk feedback",
                emailContent.toString(),
            )
            sendMail(mailToSend)
        }
    }

    private fun sendMail(mailToSend:MailToSend) {

        val impl = KotlinFix().createMailImpl(mailToSend)
        try {
            impl.send()
        } catch (e:Exception) {
            println("Failed to send to ${mailToSend.to} -> ${e.message}")
        }
    }

    private fun writExport(feedbackList: List<FeedBack>,filename: String) {
        val exportList: List<String> = feedbackList.map { it.asExport }
        PrintWriter(File(filename)).use { out ->
            out.println(FeedBack.exportHeader)
            exportList.forEach { out.println(it) }
        }
    }


    fun checkMissing(sleepingPillDataList:List<SleepingPillData>,feedbackList:List<FeedBack>) {
        for (sleepingPillData in sleepingPillDataList) {
            if (feedbackList.none { it.talkid == sleepingPillData.talkid }) {
                println("Missing *$sleepingPillData*")
            }
        }
    }

    fun generateFeedback(rawList:List<FeedBackRaw>,sleepingPillDataList:List<SleepingPillData>):List<FeedBack> {
        val resultMap:MutableMap<String,FeedBack> = mutableMapOf()
        for (raw in rawList) {
            val roomSlot:Pair<String,LocalDateTime> = readRoomSlot(raw.slotRaw)
            val sleepingPillData:SleepingPillData? = sleepingPillDataList.firstOrNull { it.room == roomSlot.first && it.startTime == roomSlot.second }
            if (sleepingPillData == null) {
                println("Did not match ${raw.slotRaw}")
                continue
            }
            val currentFeedback:FeedBack? = resultMap[sleepingPillData.talkid]
            if (currentFeedback == null) {
                resultMap[sleepingPillData.talkid] = FeedBack(
                    talkid = sleepingPillData.talkid,
                    talktype = sleepingPillData.talktype,
                    speakerList = sleepingPillData.speakerList,
                    enjoySum = raw.enjoy,
                    usefulSum = raw.useful,
                    count = 1,
                    commentList = listOfNotNull(raw.comment),
                    title = sleepingPillData.title,
                )
            } else {
                resultMap[sleepingPillData.talkid] = currentFeedback.copy(enjoySum = currentFeedback.enjoySum + raw.enjoy,usefulSum = currentFeedback.usefulSum + raw.useful,count = currentFeedback.count + 1,commentList = currentFeedback.commentList + listOfNotNull(raw.comment))
            }

        }
        return resultMap.values.toList()
    }

    fun readRoomSlot(rawvalue:String):Pair<String,LocalDateTime> {
        val wordList = rawvalue.split(" ")
        val room = wordList[0] + " " + wordList[1]
        val day:LocalDate = dates.first { it.first == wordList[2] }.second
        val hour:Int = wordList[3].substring(0,2).toInt()
        val minute:Int = wordList[3].substring(3,5).toInt()
        val dateTime = day.atTime(hour,minute)
        return Pair(room,dateTime)
    }

    private fun sleepingPillData(jsonObject: JsonObject):SleepingPillData? {
        if (jsonObject.stringValue("status").orElse(null) != "APPROVED") {
            return null
        }
        val room:String = jsonObject
            .objectValue("data").orElse(null)
            ?.objectValue("room")?.orElse(null)
            ?.stringValue("value")?.orElse(null)?:return null
        val startTime:LocalDateTime = jsonObject
            .objectValue("data").orElse(null)
            ?.objectValue("startTime")?.orElse(null)
            ?.stringValue("value")?.orElse(null)
            ?.let { LocalDateTime.parse(it) }?:return null
        val talktype:String = jsonObject
            .objectValue("data").orElse(null)
            ?.objectValue("format")?.orElse(null)
            ?.stringValue("value")?.orElse(null)?:return null
        val speakerList:List<SleepingPillSpeaker> = jsonObject.arrayValue("speakers").orElse(null)?.objects {
            val name:String? = it.stringValue("name").orElse(null)
            val email:String? = it.stringValue("email").orElse(null)
            if (name != null && email != null) {
                SleepingPillSpeaker(name,email)
            } else {
                null
            }
        }?.mapNotNull { it }?:emptyList()
        if (speakerList.isEmpty()) {
            return null
        }
        val talkid:String = jsonObject.stringValue("id").orElse(null)?:return null
        val title:String = jsonObject
            .objectValue("data").orElse(null)
            ?.objectValue("title")?.orElse(null)
            ?.stringValue("value")?.orElse(null)?:return null
        return SleepingPillData(talkid,speakerList,room,startTime,talktype,title)
    }


    private fun readRaw(filename: String): List<FeedBackRaw> {
        val readLines: List<String> = File(filename).readLines()
        val result: MutableList<FeedBackRaw> = mutableListOf()
        var current: FeedBackRaw? = null
        for ((index,line) in readLines.withIndex()) {
            if (current != null) {
                if (line.endsWith("\"")) {
                    current = current.copy(comment = current.comment + ", " + line.substring(0, line.length - 1))
                    result.add(current)
                    current = null
                    continue
                }
                current = current.copy(comment = current.comment + " " + line)
                continue
            }
            val parts = line.split(";")
            if (parts.size < 7) {
                println("Not enough parts ($index) '$line'")
                continue
            }
            val slotRaw = parts[3]
            if (!(slotRaw.startsWith("Room") || slotRaw.startsWith("Workshop"))) {
                continue
            }
            val enjoy = parts[4].toInt()
            val useful = parts[5].toInt()
            var commentValue = parts[6]
            for (i in 7 until parts.size) {
                commentValue = commentValue + " " + parts[i]
            }
            val doMerge:Boolean = (commentValue.startsWith("\"") && (!commentValue.endsWith("\"")))
            val comment = if (doMerge) commentValue.substring(1) else commentValue
            val newFeedBack = FeedBackRaw(slotRaw, enjoy, useful, if (comment.trim().isEmpty()) null else comment)
            if (doMerge) {
                current = newFeedBack
            } else {
                result.add(newFeedBack)
            }
        }
        return result
    }
}


