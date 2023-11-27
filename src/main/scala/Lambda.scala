import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.github.davidmoten.aws.lw.client.Client
import com.github.davidmoten.aws.lw.client.ResponseInputStream
import org.apache.commons.mail.util.MimeMessageParser
import org.jsoup.Jsoup

import javax.mail.internet.MimeMessage
import scala.xml.XML

object Lambda {
  private case class WebhookData(title: String, body: String)

  private def getEmailFromS3(bucket: String, key: String): ResponseInputStream =
    Client.s3().defaultClient().build().path(bucket, key).responseInputStream()

  private def parseEmail(email: ResponseInputStream): WebhookData = {
    val message = new MimeMessage(null, email)
    val mimeMessageParser = new MimeMessageParser(message).parse()

    WebhookData(
      title = mimeMessageParser.getSubject,
      body = if (mimeMessageParser.hasHtmlContent) {
        val html = Jsoup.parse(mimeMessageParser.getHtmlContent)
        val breaksEscaped = html.html().replaceAll("<br>", "<br />")
        var xmlParsed = XML.loadString(breaksEscaped).text

        html.select("a").forEach(link => xmlParsed =
          xmlParsed.replaceAll(link.html(), s"[${link.html()}](${link.attr("href")})"))

        xmlParsed.trim
      } else mimeMessageParser.getPlainContent.trim
    )
  }

  private def sendWebhookEmbedded(webhookData: WebhookData, webhookAddress: String): Unit = {
    val webhookEmbed = new WebhookEmbedBuilder()
      .setColor(0x006EE6)
      .setTitle(new WebhookEmbed.EmbedTitle(webhookData.title, ""))
      .setDescription(webhookData.body)
      .build()

    WebhookClient.withUrl(webhookAddress).send(webhookEmbed).get()
  }

  def publishWebhookToDiscord(event: S3Event): Unit = {
    val bucket = event.getRecords.get(0).getS3.getBucket.getName
    val key = event.getRecords.get(0).getS3.getObject.getKey
    sendWebhookEmbedded(parseEmail(getEmailFromS3(bucket, key)), sys.env("WEBHOOK_ADDRESS"))
  }
}
