package com.pixeldoctrine.torrboll.parser;

import com.pixeldoctrine.torrboll.entity.BackupResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class EmailParser {
    private static final Logger log = LoggerFactory.getLogger(EmailParser.class);

    @Autowired
    private List<SupplierHtmlParser> parsers;

    public List<BackupResult> parse(Message msg) {
        try {
            Date date = msg.getReceivedDate();
            String subject = msg.getSubject();
            subject = subject.replaceAll("(\\(\\d+/\\d+/\\d+\\)|\\d+/\\d+/\\d+)", "").trim();
            for (SupplierHtmlParser parser: parsers) {
                if (parser.match(subject, msg)) {
                    return parser.parse(date, subject, msg);
                }
            }
        } catch (MessagingException | IOException e) {
            log.error("Bad e-mail:", e);
        }
        return new ArrayList<>();
    }

    static String[] splitSubject(String subject) {
        String splitStr = StringUtils.countOccurrencesOf(subject, "-")==2? "-" : " - ";
        String[] words = subject.split(splitStr);
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].trim();
        }
        String job = words[0];
        String client = words[1];
        String ourCompany = words.length>=3? words[2] : "Björk IT";
        String[] jobWords = job.split("SUCCESS");
        job = jobWords[jobWords.length-1].trim();
        if (client.contains("Bjork") || client.contains("Björk")) {
            client = ourCompany; // flip
        }
        return new String[] { job, client };
    }

    static Element getHtmlBody(String subject, Part part) throws IOException, MessagingException {
        String html = getHtml(part);
        if (html != null) {
            log.debug("{}", subject);
            Document doc = Jsoup.parse(html);
            return doc.select("body").first();
        }
        throw new MessagingException("No HTML body found");
    }

    static String getHtml(Part part) throws IOException, MessagingException {
        if (part.isMimeType("text/html")) {
            return (String) part.getContent();
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                Part subpart = multipart.getBodyPart(i);
                String html = getHtml(subpart);
                if (html != null) {
                    return html;
                }
            }
        }
        return null;
    }
}
