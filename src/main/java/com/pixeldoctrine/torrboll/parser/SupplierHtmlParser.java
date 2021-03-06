package com.pixeldoctrine.torrboll.parser;

import com.pixeldoctrine.torrboll.entity.BackupResult;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface SupplierHtmlParser {
    boolean match(String subject, Message msg) throws IOException, MessagingException;
    List<BackupResult> parse(Date date, String subject, Message msg) throws IOException, MessagingException;
}
