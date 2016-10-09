/**
 *  Xmlium, is an extension of selenium-java test framework allowing for tests
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
package org.xmlium.test.commons.xml;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlium.test.commons.BaseTest;
import org.xmlium.testsuite.Config;
import org.xmlium.testsuite.ObjectFactory;
import org.xmlium.testsuite.TestCase;
import org.xmlium.testsuite.TestSuite;

@SuppressWarnings("restriction")
public class XMLTestSuite extends BaseTest {
	private static final Logger logger = Logger.getLogger(XMLTestSuite.class.getSimpleName());

	private TestSuite testSuite = null;
	private TestCase testCase = new TestCase();
	private org.xmlium.test.commons.xml.XMLTestCase test = new org.xmlium.test.commons.xml.XMLTestCase();

	private boolean errors = false;
	
	private long delay = 0;
	
	List<String> tests = new ArrayList<String>();

	@SuppressWarnings({ "unchecked" })
	void initFromXML(String xmlFileName) throws Exception {
		JAXBContext jaxbContext = null;
		Unmarshaller unmarshaller = null;
		SAXParserFactory spf = null;
		XMLReader xr = null;
		SAXSource source = null;
		JAXBElement<TestSuite> unmarshalledObject = null;
		try {
			// 1. We need to create JAXContext instance
			jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

			// 2. Use JAXBContext instance to create the Unmarshaller.
			unmarshaller = jaxbContext.createUnmarshaller();

			logger.debug("xmlFileName: " + xmlFileName);

			spf = SAXParserFactory.newInstance();

			spf.setXIncludeAware(true);
			spf.setNamespaceAware(true);

			// 3. Use the Unmarshaller to unmarshal the XML document to get an
			// instance of JAXBElement.
			xr = (XMLReader) spf.newSAXParser().getXMLReader();
			source = new SAXSource(xr, new InputSource(getClass().getResourceAsStream(xmlFileName)));
			unmarshalledObject = (JAXBElement<TestSuite>) unmarshaller.unmarshal(source);

			// 4. Get the instance of the required JAXB Root Class from the
			// JAXBElement.
			testSuite = unmarshalledObject.getValue();
			if (testSuite != null) {
				testCase = testSuite.getTestCase();
				Config config = testCase.getInitialConfig();
				setDriver((WebDriver) Class.forName(config.getDriverClass()).newInstance());
				setTimeout(config.getTimeout());
				setUrl(config.getUrl());
				if (config.getLocale() != null && !config.getLocale().isEmpty()) {
					setLocale(new Locale(config.getLocale().substring(0, config.getLocale().indexOf('_')),
							config.getLocale().substring(config.getLocale().indexOf('_') + 1)));
					setCurrencyFormat(NumberFormat.getInstance(getLocale()));
					((DecimalFormat) getCurrencyFormat()).setParseBigDecimal(true);
				}
				if(config.getDelay()!=null){
					setDelay(config.getDelay().longValue());
				}
				System.out.println("URL: " + getUrl());
				setTimeoutUnits(TimeUnit.valueOf(config.getTimeUnit()));
				setPathPatternString(config.getPathString());
				setFluentWait(new FluentWait<WebDriver>(getDriver()).withTimeout(getTimeout(), getTimeoutUnits())
						.pollingEvery(200, TimeUnit.MILLISECONDS));
				Iterator<String> iterClassNames = config.getFluentWaitIgnore().getExceptionClassNames().iterator();
				while (iterClassNames.hasNext()) {
					String className = iterClassNames.next();
					Class<? extends Throwable> c = (Class<? extends Throwable>)Class.forName(className);
					getFluentWait().ignoring(c);
				}
				// extractLocale();
				loadBundles();
				getTests().addAll(testSuite.getTestCase().getCurrentTest());
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage(), e);
			errors = true;
			throw e;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage(), e);
			errors = true;
			throw e;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage(), e);
			errors = true;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage(), e);
			errors = true;
			throw e;
		}catch (Exception e) {
			logger.debug(e.getMessage(), e);
			errors = true;
			throw e;
		}finally {
			jaxbContext = null;
			unmarshaller = null;
			spf = null;
			xr = null;
			source = null;
			unmarshalledObject = null;

		}
	}

	void test() throws Exception{
		if (errors) {
			throw new RuntimeException("errors: " + errors);
		}
		if (testSuite == null) {
			throw new RuntimeException("" + testSuite);
		}
		if (testCase == null) {
			throw new RuntimeException("TestCase is null!");
		}

		
		if (getTests().size() == 0) {
			throw new RuntimeException("No tests configured!");
		}

		if (testCase.getGet() != null) {
			getDriver().get(getUrl());
		}

		getDriver().manage().window().maximize();

		for (String testFile : tests) {
			test.reset();
			logger.info("Running test file: "+testFile);
			test.initFromXML(this, testFile);
			test.test();
		}
	}
	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public List<String> getTests() {
		return tests;
	}

	public void setTests(List<String> tests) {
		this.tests = tests;
	}
}
