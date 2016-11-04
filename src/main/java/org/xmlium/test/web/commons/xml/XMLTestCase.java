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
package org.xmlium.test.web.commons.xml;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlium.testsuite.ObjectFactory;
import org.xmlium.testsuite.Repeat;
import org.xmlium.testsuite.Test;

@SuppressWarnings("restriction")
public class XMLTestCase {
	private static final Logger logger = Logger.getLogger(XMLTestCase.class.getSimpleName());
	private Test test = null;
	private XMLTestSuite suite = null;

	@SuppressWarnings({ "unchecked" })
	void initFromXML(final XMLTestSuite testSuite, String xmlFileName) throws Exception {
		JAXBContext jaxbContext = null;
		Unmarshaller unmarshaller = null;
		SAXParserFactory spf = null;
		XMLReader xr = null;
		SAXSource source = null;
		JAXBElement<Test> unmarshalledObject = null;
		suite = testSuite;
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
			unmarshalledObject = (JAXBElement<Test>) unmarshaller.unmarshal(source);

			// 4. Get the instance of the required JAXB Root Class from the
			// JAXBElement.
			test = unmarshalledObject.getValue();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage(), e);
			throw e;
		} finally {
			jaxbContext = null;
			unmarshaller = null;
			spf = null;
			xr = null;
			source = null;
			unmarshalledObject = null;

		}
	}
	
	void reset(){
		test = null;
	}

	void test() throws Exception{
		if (test.getRepeat() != null) {
			Repeat repeat = test.getRepeat();
			XMLTestSteps xmlTestSteps = new XMLTestSteps(getSuite(), repeat.getRepeatSteps());
			int repeatCount = repeat.getRepeatCount().intValue();
			int infiniteCount = 1000;
			if (repeatCount != -1) {
				infiniteCount = repeatCount;
			}else{
				repeatCount = infiniteCount;
			}
			for (int i = 0; i < repeatCount && i<infiniteCount; i++) {
				try {
					xmlTestSteps.processSteps();
				} catch (Exception e) {
					if (infiniteCount == 1000) {
						if (e instanceof org.openqa.selenium.NoSuchElementException) {
							NoSuchElementException nse = (NoSuchElementException) e;
							logger.debug("message: " + nse.getMessage());
							boolean throwException = true;
							List<String> values = xmlTestSteps.getAvailableValuesForSteps();
							for (String value : values) {
								logger.debug("value: " + value);
								logger.debug("message: "+nse.getMessage());
								if (nse.getMessage().contains(value)) {
									throwException = false;
									break;
								}
							}
							if (throwException) {
								throw e;
							}
						} else if (e instanceof org.openqa.selenium.TimeoutException) {
							org.openqa.selenium.TimeoutException toe = (org.openqa.selenium.TimeoutException) e;
							if (toe.getCause() instanceof org.openqa.selenium.NoSuchElementException) {
								NoSuchElementException nse = (NoSuchElementException) toe.getCause();
								logger.debug("message: " + nse.getMessage());
								boolean throwException = true;
								List<String> values = xmlTestSteps.getAvailableValuesForSteps();
								for (String value : values) {
									logger.debug("value: " + value);
									logger.debug("message: "+nse.getMessage());
									if (nse.getMessage().contains(value)) {
										throwException = false;
										break;
									}
								}
								if (throwException) {
									throw e;
								}
							} else {
								throw e;
							}
						}
					} else {
						throw e;
					}

//					logger.error(e.getMessage(), e);
//					e.printStackTrace();
					break;
				}
			}
			logger.debug("exit repeat loop");
		} else {
			XMLTestSteps xmlTestSteps = new XMLTestSteps(getSuite(), test.getTestSteps());
			xmlTestSteps.processSteps();
		}
	}

	public XMLTestSuite getSuite() {
		return suite;
	}

	public void setSuite(XMLTestSuite suite) {
		this.suite = suite;
	}

}
