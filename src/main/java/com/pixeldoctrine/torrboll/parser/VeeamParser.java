package com.pixeldoctrine.torrboll.parser;

import com.pixeldoctrine.torrboll.entity.BackupResult;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class VeeamParser implements SupplierHtmlParser {

    @Override
    public boolean match(String subject, Message msg) throws IOException, MessagingException {
        String html = EmailParser.getHtml(msg);
        return html.contains("Veeam Backup");
    }

    @Override
    public List<BackupResult> parse(Date date, String subject, Message msg) throws IOException, MessagingException {
        Element body = EmailParser.getHtmlBody(subject, msg);
		//System.out.println(body);
        List<BackupResult> result = new ArrayList<>();
		result.add(parsePercentSuccess(date, msg, body));
        return result;
    }

	public BackupResult parsePercentSuccess(Date date, Message msg, Element body) {
        Element jobTag = body.select("div.jobDescription").first();
		String job = jobTag.text().trim().replace("  ", " ");
		//System.out.println("Veeam job: " + job);
		job = job.startsWith("Created by")? job.split(" ")[2] : job.replace("Veeam Backup for Microsoft ", "");
		Element clientTag = jobTag.parent();
		//System.out.println("Veeam job: " + job);
		String client = clientTag.ownText().split(":")[1].split(" - ")[0].split("\\(")[0].trim();
		String system = job.contains("Office")? job : "Data";
		// ok, now get the result
		//System.out.println(clientTag.parent());
		Element columnNameElement = clientTag.parent().nextElementSibling().select("tr.processObjectsHeader").first();
		int statusIndex = 0;
		for (Element col : columnNameElement.children()) {
			if (col.text().contains("Status")) {
				break;
			}
			++statusIndex;
		}
		Element tr = columnNameElement.nextElementSibling();
		int systemsCounted = 0;
		float systemsOk = 0;
		while (tr != null) {
			Element status = tr.children().get(statusIndex);
			if (status.text().contains("Success")) {
				systemsOk += 1;
			} else if (status.text().contains("Warning")) {
				systemsOk += 0.5;
			}
			tr = tr.nextElementSibling();
			++systemsCounted;
		}
		int percent = (int)(systemsOk*100 / systemsCounted);
		return new BackupResult(date, "Veeam", client, system, job, percent, msg);
	}
}
