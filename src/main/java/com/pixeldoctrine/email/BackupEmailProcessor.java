package com.pixeldoctrine.email;

import com.pixeldoctrine.db.BackupResultRepository;
import com.pixeldoctrine.entity.BackupResult;
import com.pixeldoctrine.parser.EmailParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import java.sql.SQLException;
import java.util.*;

@Service
public class BackupEmailProcessor {
	private static final Logger log = LoggerFactory.getLogger(BackupEmailProcessor.class);

	@Autowired
	private EmailParser emailParser;

	@Autowired
	private BackupResultRepository resultRepository;

	@Value("${imap.user}")
	private String username;
	@Value("${imap.password}")
	private String password;
	@Value("${imap.host}")
	private String host;
	@Value("${imap.port}")
	private int port;

	public int process() {
		Store imap = null;
		try {
			imap = imapConnect();
			return processAll(imap);
		} catch (MessagingException e) {
			log.error("Crash when processing e-mails.", e);
		} finally {
			imapDisconnect(imap);
		}
		return 0;
	}

	private Store imapConnect() throws MessagingException {
		Session session = Session.getDefaultInstance(new Properties());
		Store imap = session.getStore("imaps");
		imap.connect(host, port, username, password);
		return imap;
	}

	private int processAll(Store imap) throws MessagingException {
		Folder inbox = imap.getFolder("INBOX");
		inbox.open(Folder.READ_WRITE);
		Message[] messages = inbox.getMessages();
		Set<Message> handledMessages = new HashSet<>();
		for (Message msg: messages) {
			List<BackupResult> results = emailParser.parse(msg);
			for (BackupResult result: results) {
				log.info("{}, {}, {}, {}, {}", result.getService(), result.getClient(), result.getSystem(), result.getJob(), result.getPercent());
				try {
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
