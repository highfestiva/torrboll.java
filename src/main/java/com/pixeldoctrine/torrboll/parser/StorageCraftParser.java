package com.pixeldoctrine.torrboll.parser;

import com.pixeldoctrine.torrboll.entity.BackupResult;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class StorageCraftParser implements SupplierHtmlParser {

    @Override
    public boolean match(String subject, Message msg) throws IOException, MessagingException {
        return subject.contains("Online Image Report");
    }

    @Override
    public List<BackupResult> parse(Date date, String subject, Message msg) throws IOException, MessagingException {
        String[] subjectWords = subject.split("Online Image Report:");
        subject = subjectWords[subjectWords.length-1].trim();
        subjectWords = EmailParser.splitSubject(subject);
        String job = subjectWords[0];
        String client = subjectWords[1];
        List<BackupResult> result = new ArrayList<>();
        Element body = EmailParser.getHtmlBody(subject, msg);
        for (Element systemRow: body.select("table[cellspacing=15]")) {
            for (Element td: systemRow.select("td")) {
                Element span = td.select("span").first();
                String system = span.text().trim();
                int percent = 0;
                String style = td.attr("style");
                for (String stylePart: style.split(";")) {
                    if (stylePart.contains("border") && stylePart.contains("#5DE01B")) {
                        percent = 100;
                        break;
                    }
                }
                result.add(new BackupResult(date, "Storage Craft", client, system, job, percent, msg));
            }
        }
        return result;
    }
}
