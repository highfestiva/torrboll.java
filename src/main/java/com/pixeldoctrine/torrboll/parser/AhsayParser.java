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
public class AhsayParser implements SupplierHtmlParser {

	private static String NL = "Backup Samenvatting:";
	private static String EN = "Backup Summary:";

    @Override
    public boolean match(String subject, Message msg) throws IOException, MessagingException {
        return subject.contains(NL) || subject.contains(EN);
    }

    @Override
    public List<BackupResult> parse(Date date, String subject, Message msg) throws IOException, MessagingException {
        String[] subjectWords = subject.contains(NL)? subject.split(NL) : subject.split(EN);
        subject = subjectWords[subjectWords.length-1].trim();
        subjectWords = EmailParser.splitSubject(subject);
        String[] words = subjectWords[0].split(" ");
        String job = words[words.length-1];
        String client = subjectWords[1];
        List<BackupResult> result = new ArrayList<>();
        Element body = EmailParser.getHtmlBody(subject, msg);
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
