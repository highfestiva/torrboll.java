package com.pixeldoctrine.torrboll.parser;

import com.pixeldoctrine.torrboll.entity.BackupResult;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class EkcoParser implements SupplierHtmlParser {

    @Override
    public boolean match(String subject, Message msg) throws IOException, MessagingException {
        String html = EmailParser.getHtml(msg);
        return html.contains("Office 365 Backupstatus Report");
    }

    @Override
    public List<BackupResult> parse(Date date, String subject, Message msg) throws IOException, MessagingException {
		int[] skip = {1};
        Element body = EmailParser.getHtmlBodySkip(subject, msg, skip);
        List<BackupResult> result = new ArrayList<>();
        for (Element customerTable: body.select("tbody.searchable")) {
			for (Element customerRow: customerTable.select("tr")) {
				Elements tds = customerRow.select("td");
				if (!tds.first().text().trim().isEmpty()) {
					continue;
				}
				String client = tds.get(1).text().replaceAll(".onmicrosoft.com", "");
				String system = tds.get(7).text().replaceAll(" Backup NL", "");
				String job = "Ekco: " + tds.get(3).text().replaceAll("[Oo]ffice", "").replaceAll("365", "").replaceAll("[Dd]aily", "").replaceAll("[Bb]ackup", "").replaceAll(" - ", " ").replaceAll(" +", " ");
				int percent = tds.get(8).className().contains("Success")? 100 : 0;
				result.add(new BackupResult(date, "Veeam", client, system, job, percent, msg));
			}
		}
        return result;
    }
}
