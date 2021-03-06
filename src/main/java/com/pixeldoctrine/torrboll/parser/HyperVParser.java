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

    @Override
    public boolean match(String subject, Message msg) throws IOException, MessagingException {
        return subject.contains("Hyper-V Server Report");
    }

    @Override
    public List<BackupResult> parse(Date date, String subject, Message msg) throws IOException, MessagingException {
        List<BackupResult> result = new ArrayList<>();
        Element body = EmailParser.getHtmlBody(subject, msg);
        String[] clientWords = body.select("h2").first().text().split("'");
        String client = clientWords[1];
        Elements systemRows = body.select("table");
		for (Element systemRow: systemRows) {
			Elements trs = systemRow.select("tr");
			if (trs.size() < 2) {
				continue;
			}
			Elements ths = trs.get(0).select("th");
			if (ths.size() < 4) {
				continue;
			}
			if (!ths.get(2).text().trim().equals("Health")) {
				continue;
			}
			for (Element tr: trs.subList(1, trs.size())) {
				Elements tds = tr.select("td");
				String system = tds.get(0).text().trim();
				int percent = tds.get(2).text().trim().equals("Normal")? 100 : 0;
				result.add(new BackupResult(date, "Hyper-V", client, system, system, percent, msg));
			}
		}
        return result;
    }
}
