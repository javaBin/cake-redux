package no.javazone.cake.redux

import no.javazone.cake.redux.mail.MailToSend
import no.javazone.cake.redux.util.KotlinFix
import java.io.File
import java.lang.StringBuilder

enum class FeedbackCol {
    ROOM,
    DAY,
    TIME,
    ATENDEES,
    GREEN,
    YELLOW,
    RED,
    ROOM2,
    DAYTIME,
    TITLE,
    NAME,
    MAIL

}

private class ToSendMail(val to:List<String>,val content:String) {
    fun sendit() {
        val mailToSend = MailToSend("program@java.no", "JavaZone", emptyList(), to, "Speaker feedback",content)
        val impl = KotlinFix().createMailImpl(mailToSend)
        try {
            impl.send()
        } catch (e:Exception) {
            println("Failed to send to $to -> ${e.message}")
        }
    }
}

class CsvFeedbackReader(val datafilename:String,temptefilename:String) {


    val template:String = File(temptefilename).readText()


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("cake-redux-config-file", "xxx")
            val csvFeedbackReader =
                CsvFeedbackReader("feedback.tsv","mailtospeaker.html")
            val linvals = csvFeedbackReader.readValuesFromData()
            val toSend = linvals.map { csvFeedbackReader.buildMailTSend(it) }
            println("Will send ${toSend.size}")
            var num=0
            for (send in toSend) {
                send.sendit()
                num++
                println("Sent $num")
            }

        }
    }

    private fun buildMailTSend(lineVals:Map<FeedbackCol,String>):ToSendMail {
        val mailtext = mergeText(lineVals)
        val emails = splitMail(lineVals[FeedbackCol.MAIL]!!)
        return ToSendMail(emails,mailtext)
    }

    private fun splitMail(mailtext:String):List<String> {
        return mailtext.split(" and ")
    }

    private fun mergeText(lineVals:Map<FeedbackCol,String>):String {
        val res = StringBuilder(template)
        while (true) {
            val startpos = res.indexOf("%")
            if (startpos == -1) {
                return res.toString()
            }
            val endpos = res.indexOf("%",startpos+1)
            val col:FeedbackCol = FeedbackCol.valueOf(res.substring(startpos+1,endpos))
            res.replace(startpos,endpos+1,lineVals[col])
        }

    }

    private fun readValuesFromData():List<Map<FeedbackCol,String>> {
        val alllines = File(datafilename).readLines()
        val feedbackValuesDef = FeedbackCol.values()
        val res:MutableList<Map<FeedbackCol,String>> = mutableListOf()
        for (line in alllines) {
            val parts = line.split("\t")
            val lineVals:MutableMap<FeedbackCol,String> = mutableMapOf()
            for (i in feedbackValuesDef.indices) {
                lineVals.put(feedbackValuesDef[i],parts[i])
            }
            res.add(lineVals)
        }
        return res
    }
}