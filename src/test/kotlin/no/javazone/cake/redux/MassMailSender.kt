package no.javazone.cake.redux

import no.javazone.cake.redux.mail.MailToSend
import no.javazone.cake.redux.util.KotlinFix
import java.io.File

private val fromEmail = "javazone@java.no"
private val fromEmailName = "Javazone"
private val subject = "Regarding JavaZone 2021"


class MassMailSender(emailsFileName:String,contentFileName:String) {
    companion object {
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
            val sender = MassMailSender(args[1],args[2])
            sender.sendAll()
        }
    }

    val emails:Set<String>;
    val content:String;

    init {
        emails = File(emailsFileName).readLines().toSet()
        content = File(contentFileName).readText()
    }

    fun sendAll() {
        println("Sending ${emails.size} emails")
        var num = 0
        for (emailaddr in emails) {
            num++
            if (num % 10 == 0) {
                println("Send $num")
            }
            val mailToSend = MailToSend(fromEmail, fromEmailName, emptyList(), listOf(emailaddr), subject,content)
            val impl = KotlinFix().createMailImpl(mailToSend)
            try {
                impl.send()
            } catch (e:Exception) {
                println("Failed to send to $emailaddr -> ${e.message}")
            }
        }
        println("All done")
    }


}