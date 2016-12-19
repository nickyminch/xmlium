/**
 *  xmlium-web, is an extension for selenium-java test framework allowing for tests
 *  to be described in xml files.
 *
 *  The contents of this file are subject GNU Lesser General Public License
 *  Version 3 or later, you may not use this file except in compliance
 *  with the License.
 *
 *  You may obtain a copy of the License at:
 *  https://www.gnu.org/licenses/lgpl.html
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 */
package org.xmlium.test.web.commons.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

public class XMLLambdaTest{
	private static final Logger logger = Logger.getLogger(XMLLambdaTest.class);

	XMLTestSuite suite = new XMLTestSuite();
	private final Collection<DynamicTest> tests = new ArrayList<DynamicTest>();
	 
	
	protected void λ(XMLTestSuite suite, String name) {
		
		NamedImpl namedTest = new NamedImpl();
		Executable test = () -> namedTest.execute(suite, name);
		tests.add(DynamicTest.dynamicTest(name, test));
	}

	XMLTestSuite getSuite() {
		return suite;
	}

	public Collection<DynamicTest> getTests() {
		return tests;
	}
	
	public void prepare() throws Exception{
		try {
			init();
			SuiteNamedImpl suiteImpl = new SuiteNamedImpl();
			String url = suite.getUrl();
			if(url==null){
				url = "main";
			}
			final String testUrl = url;
			Executable testSuite = () -> suiteImpl.execute(suite, testUrl);
			tests.add(DynamicTest.dynamicTest(testUrl, testSuite));
			List<String> tests = getSuite().getTests();
			for(String test: tests){
				λ( getSuite(), test);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
	
	private void init() throws Exception{
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
		String key = "testcase";
		logger.debug("key: "+key);
		String testCase = testSuite.getProperty(key);
		logger.debug("testCase: "+testCase);
		if(testCase==null){
			throw new RuntimeException("No tests configured");
		}
		try {
			getSuite().setLocale(Locale.getDefault());
			System.out.println(testCase);
			getSuite().initFromXML(testCase);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
	
	public void releaseTest(){
		if(getSuite().getConfig()==null){
			return;
		}
		Object server = getSuite().getConfig().getSelendroidServer();
		if(server!=null){
			try {
				Method m = server.getClass().getDeclaredMethod("stopSelendroid");
				m.invoke(server);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			} 			
		}
	}
}
