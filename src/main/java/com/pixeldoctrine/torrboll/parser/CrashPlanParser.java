package com.pixeldoctrine.torrboll.parser;

import com.pixeldoctrine.torrboll.entity.BackupResult;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class CrashPlanParser implements SupplierHtmlParser {

    @Autowired
    private EmailParser emailParser;

    @Override
    public boolean match(String subject) {
        return subject.contains("Code42") && subject.contains("Backup Report");
    }

    @Override
    public List<BackupResult> parse(Date date, String subject, Message msg) throws IOException, MessagingException {
        List<BackupResult> result = new ArrayList<>();
        Element body = emailParser.getHtmlBody(subject, msg);
        for (Element systemRow: body.select("tr.lastForComputer")) {
            Elements tds = systemRow.select("td");
            String system = tds.get(0).text().split("→")[0].trim();
            int percent = (int) Float.parseFloat(StringUtils.trimTrailingCharacter(tds.get(3).text(), '%'));
            if (!tds.get(4).text().contains("hrs") && !tds.get(4).text().contains(("mins"))) {
                percent = 0;
            }
            result.add(new BackupResult(date, "CrashPlan PRO", system, system, system, percent, msg));
        }
        return result;
    }
}
