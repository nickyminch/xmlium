package org.seleniumxml.test.commons.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;

public class XMLTest{
	private static final Logger logger = Logger.getLogger(XMLTest.class.getSimpleName());

	List<XMLTestSuite> tests = new ArrayList<XMLTestSuite>();

	public void test() throws Exception{
		Properties testSuite = new Properties();
		String config = System.getProperty("xmltestsuite");
		logger.info("config: "+config);
		InputStream is = getClass().getResourceAsStream(config);
		if(is==null){
			is = new FileInputStream(config);
		}
		if(is!=null){
			testSuite.load(is);
		}
		logger.debug("testSuite: "+testSuite.toString());
		File f = new File(".");
		System.out.println(f.getAbsolutePath());
		int i=0;
		while (true) {
			String key = "testcase"+Integer.toString(i);
			logger.debug("key: "+key);
			String testCase = testSuite.getProperty(key);
			logger.debug("testCase: "+testCase);
			if(testCase==null){
				break;
			}
			try {
				XMLTestSuite test = new XMLTestSuite();
				test.setLocale(Locale.getDefault());
				System.out.println(testCase);
				test.initFromXML(testCase);
				test.test();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw e;
			}
			i++;
		}
	}
}
