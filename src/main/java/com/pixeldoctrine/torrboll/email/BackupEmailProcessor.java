package com.pixeldoctrine.torrboll.email;

import com.pixeldoctrine.parser.EmailParser;
import com.pixeldoctrine.torrboll.db.BackupResultRepository;
import com.pixeldoctrine.torrboll.entity.BackupResult;
import com.sun.mail.smtp.SMTPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class BackupEmailProcessor {
	private static final Logger log = LoggerFactory.getLogger(BackupEmailProcessor.class);

	@Autowired
	private EmailParser emailParser;

	@Autowired
	private BackupResultRepository resultRepository;

	@Value("${imap.user}")
	private String imapUsername;
	@Value("${imap.password}")
	private String imapPassword;
	@Value("${imap.host}")
	private String imapHost;
	@Value("${imap.port}")
	private int imapPort;
	@Value("${smtp.user}")
	private String smtpUsername;
	@Value("${smtp.password}")
	private String smtpPassword;
	@Value("${smtp.host}")
	private String smtpHost;
	@Value("${smtp.port}")
	private int smtpPort;
	@Value("${receivers}")
	private String receivers;

	public int process() {
		Store imap = null;
		try {
			imap = imapConnect();
			return processAll(imap);
		} catch (MessagingException | UnknownHostException e) {
			log.error("Crash when processing e-mails.", e);
		} finally {
			imapDisconnect(imap);
		}
		return 0;
	}

	private Store imapConnect() throws MessagingException {
		Session session = Session.getDefaultInstance(new Properties());
		Store imap = session.getStore("imaps");
		imap.connect(imapHost, imapPort, imapUsername, imapPassword);
		return imap;
	}

	private int processAll(Store imap) throws MessagingException, UnknownHostException {
		Folder inbox = imap.getFolder("INBOX");
		inbox.open(Folder.READ_WRITE);
		Message[] messages = inbox.getMessages();
		Set<Message> handledMessages = new HashSet<>();
		List<BackupResult> failedJobs = new ArrayList<>();
		for (Message msg: messages) {
			List<BackupResult> results = emailParser.parse(msg);
			for (BackupResult result: results) {
				log.info("{}, {}, {}, {}, {}", result.getService(), result.getClient(), result.getSystem(), result.getJob(), result.getPercent());
				try {
					if (result.getPercent() != 100) {
						failedJobs.add(result);
					}
					resultRepository.save(result);
					handledMessages.add(msg);
				} catch (SQLException e) {
					log.error("Unable to save e-mail.", e);
				}
			}
			if (results.isEmpty()) {
				log.info("Junk e-mail: {}", msg.getSubject());
			}
		}
		// move all processed messages
		if (!handledMessages.isEmpty()) {
			Folder processed = imap.getFolder("Processed");
			Message[] msgs = handledMessages.toArray(new Message[0]);
			inbox.copyMessages(msgs, processed);
			inbox.setFlags(msgs, new Flags(Flags.Flag.DELETED), true);
			inbox.expunge();
		}
		inbox.close();
		// place ticket on error
		if (!failedJobs.isEmpty()) {
			log.info("Placing ticket.");
			String info = String.join("\r\n", failedJobs.stream()
					.map(j -> j.getClient()+" "+j.getSystem()+" ("+j.getJob()+")")
					.collect(toList()));
			String url = String.format("http://%s:5009/status", InetAddress.getLocalHost().getHostName());
			String ticket = "Failed backups:\n\n" + info + String.format("\n\nMore info here: %s\n", url);
			// smtp
			Properties props = System.getProperties();
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.host", smtpHost);
			props.put("mail.smtp.port", smtpPort);
			props.put("mail.smtp.user", smtpUsername);
			props.put("mail.smtp.password", smtpPassword);
			Session session = Session.getInstance(props);
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(smtpUsername));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receivers, false));
			msg.setSubject("Backup failure");
			msg.setText(ticket);
			SMTPTransport t = (SMTPTransport)session.getTransport("smtp");
			t.connect(smtpHost, smtpPort, smtpUsername, smtpPassword);
			t.sendMessage(msg, msg.getAllRecipients());
			t.close();
			log.info("Ticket placed.");
		}
		return handledMessages.size();
	}

	private void imapDisconnect(Store imap) {
		if (imap != null) {
			try {
				imap.close();
			} catch (MessagingException e) {
				log.error("Unable to close IMAP connection.", e);
			}
		}
	}
}
