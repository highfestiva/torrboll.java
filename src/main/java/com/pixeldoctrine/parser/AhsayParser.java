package com.pixeldoctrine.parser;

import com.pixeldoctrine.entity.BackupResult;
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
public class AhsayParser implements SupplierHtmlParser {

    @Autowired
    private EmailParser emailParser;

    @Override
    public boolean match(String subject) {
        return subject.contains("Backup Summary:");
    }

    @Override
    public List<BackupResult> parse(Date date, String subject, Message msg) throws IOException, MessagingException {
        String[] subjectWords = subject.split("Backup Summary:");
        subject = subjectWords[subjectWords.length-1].trim();
        subjectWords = emailParser.splitSubject(subject);
        String job = subjectWords[0];
        String client = subjectWords[1];
        List<BackupResult> result = new ArrayList<>();
        Element body = emailParser.getHtmlBody(subject, msg);
        for (Element systemRow: body.select("table[width=100%]")) {
            Element td = systemRow.select("td").first();
            String system = td.text().trim();
            int percent = 0;
            String[] systemWords = system.split("Backupset:");
            system = systemWords[systemWords.length-1].trim();
            for (Element span: systemRow.select("span")) {
                if (span.text().contains("SUCCESS")) {
                    percent = 100;
                    break;
                }
            }
            result.add(new BackupResult(date, "Ahsay", client, system, job, percent, msg));
        }
        return result;
    }
}
