package no.javazone.cake.redux

import no.javazone.cake.redux.mail.MailToSend
import no.javazone.cake.redux.util.KotlinFix
import java.io.File
import java.lang.RuntimeException


private class EmailWithTemplate(
    val email:String,
    val templateValues:List<Pair<String,String>>
) {
    fun genMail(template:String):String {
        val res = StringBuilder(template)
        for ((key,value) in templateValues) {
            val search = "#$key#"
            val ind = res.indexOf(search)
            if (ind == -1) {
                continue
            }
            res.replace(ind,ind+search.length,value)
        }
        return res.toString()
    }
}

class MassMailSenderTemplate(emailsFileName:String, contentFileName:String) {

    companion object {
        private val fromEmail = "program@java.no"
        private val fromEmailName = "Javazone"
        private val subject = "JavaZone workshops tomorrow"

        private val templateVariables = listOf("PARTICIPANT_ID")

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 3) {
                println("Usage MassMailSender <configfile> pathToMailList pathToMailContent")
                return
            }
            if (!File(args[1]).exists()) {
                println("Could not find file ${args[1]}")
                return
            }
            if (!File(args[2]).exists()) {
                println("Could not find file ${args[2]}")
                return
            }
            System.setProperty("cake-redux-config-file", args[0])
            val sender = MassMailSenderTemplate(args[1],args[2])
            sender.sendAll()
        }

        private fun readEmails(lines:List<String>):List<EmailWithTemplate> = lines.mapIndexed { lineNo:Int,line:String ->
            val parts = line.split(";")
            if (parts.size != templateVariables.size+1) {
                 throw RuntimeException("Wrong split in line $lineNo -> $line")
            }
            val templateValues:List<Pair<String,String>> = templateVariables.mapIndexed { partno: Int, partText: String ->
                Pair(partText,parts[partno+1])
            }
            EmailWithTemplate(parts[0],templateValues)
        }
    }

    private val emails:List<EmailWithTemplate>;
    private val content:String;

    init {

        emails = readEmails(File(emailsFileName).readLines())
        content = File(contentFileName).readText()
    }

    fun sendAll() {
        println("Sending ${emails.size} emails")
        var num = 0
        for (emailwithtemplate:EmailWithTemplate in emails) {
            num++
            if (num % 10 == 0) {
                println("Send $num")
            }

            val mailToSend = MailToSend(fromEmail, fromEmailName, listOf(), listOf(emailwithtemplate.email), subject,emailwithtemplate.genMail(content))
            val impl = KotlinFix().createMailImpl(mailToSend)
            try {
                impl.send()
            } catch (e:Exception) {
                println("Failed to send to ${emailwithtemplate.email} -> ${e.message}")
            }
        }
        println("All done")
    }


}