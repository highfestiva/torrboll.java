package com.pixeldoctrine.parser;

import com.pixeldoctrine.entity.BackupResult;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface SupplierHtmlParser {
    boolean match(String subject);
    List<BackupResult> parse(Date date, String subject, Message msg) throws IOException, MessagingException;
}
