package com.pixeldoctrine.torrboll;

import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
public class AppConfiguration {
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
}
