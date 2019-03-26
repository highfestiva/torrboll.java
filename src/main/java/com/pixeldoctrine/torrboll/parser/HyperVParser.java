package com.pixeldoctrine.torrboll.parser;

import com.pixeldoctrine.torrboll.entity.BackupResult;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class HyperVParser implements SupplierHtmlParser {

    @Autowired
    private EmailParser emailParser;

    @Override
    public boolean match(String subject) {
        return subject.contains("Hyper-V Server Report");
    }

    @Override
    public List<BackupResult> parse(Date date, String subject, Message msg) throws IOException, MessagingException {
        List<BackupResult> result = new ArrayList<>();
        Element body = emailParser.getHtmlBody(subject, msg);
        String[] clientWords = body.select("h2").first().text().split("'");
        String client = clientWords[1];
        Element systemRow = body.select("table").first();
        Elements trs = systemRow.select("tr");
        for (Element tr: trs.subList(1, trs.size())) {
            Elements tds = tr.select("td");
            String system = tds.get(0).text().trim();
            int percent = tds.get(2).text().equals("Operating normally")? 100 : 0;
            result.add(new BackupResult(date, "Hyper-V", client, system, system, percent, msg));
        }
        return result;
    }
}
