package com.pixeldoctrine.torrboll.parser;

import com.pixeldoctrine.torrboll.entity.BackupResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;
import org.junit.Test;

public class VeeamParserTest {
	@Test
	public void testParse() throws Exception {
		VeeamParser parser = new VeeamParser();
		String html = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("test.html").toURI())), StandardCharsets.UTF_8);
		Element body = Jsoup.parse(html).select("body").first();
		BackupResult result = parser.parsePercentSuccess(null, null, body);
		System.out.println(result.getPercent());
		assert result.getPercent() == 62;
	}
}
