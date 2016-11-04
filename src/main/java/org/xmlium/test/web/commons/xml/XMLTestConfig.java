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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.xmlium.testsuite.Config;
import org.xmlium.testsuite.ObjectFactory;

@SuppressWarnings("restriction")
public class XMLTestConfig {
	private static final Logger logger = Logger.getLogger(XMLTestConfig.class.getSimpleName());

	XMLTestSuite suite;
	Config config;
	
	public XMLTestConfig(XMLTestSuite suite, String fileName) throws Exception{
		this.suite = suite;
		loadConfigFromFile(fileName);
	}
	
	@SuppressWarnings({ "unchecked" })
	void loadConfigFromFile(String xmlFileName) throws Exception {
		try {
			xmlFileName = XMLTestSteps.unformatValue(xmlFileName);
			logger.debug("xmlFileName: " + "'"+xmlFileName+"'");
			InputStream is = getClass().getResourceAsStream(xmlFileName);
			if(is==null){
				throw new FileNotFoundException(xmlFileName+" not found!");
			}

			JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);

	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        config = ((JAXBElement<Config>)unmarshaller.unmarshal(is)).getValue();
	        if(config==null){
	        	throw new NullPointerException("Config Object!");
	        }
	        loadConfig();
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw e;
		} finally {
		}
	}

	protected void loadConfig() throws Exception{
		try {
			getSuite().setDriver((WebDriver) Class.forName(config.getDriverClass()).newInstance());
			getSuite().setTimeout(config.getTimeout());
			getSuite().setUrl(config.getUrl());
			if (config.getLocale() != null && !config.getLocale().isEmpty()) {
				getSuite().setLocale(new Locale(config.getLocale().substring(0, config.getLocale().indexOf('_')),
						config.getLocale().substring(config.getLocale().indexOf('_') + 1)));
				getSuite().setCurrencyFormat(NumberFormat.getInstance(getSuite().getLocale()));
				((DecimalFormat) getSuite().getCurrencyFormat()).setParseBigDecimal(true);
			}
			if(config.getDelay()!=null){
				getSuite().setDelay(config.getDelay().longValue());
			}
			System.out.println("URL: " + getSuite().getUrl());
			getSuite().setTimeoutUnits(TimeUnit.valueOf(config.getTimeUnit()));
			getSuite().setPathPatternString(config.getPathString());
			getSuite().setFluentWait(new FluentWait<WebDriver>(getSuite().getDriver()).withTimeout(getSuite().getTimeout(), getSuite().getTimeoutUnits())
					.pollingEvery(200, TimeUnit.MILLISECONDS));
			Iterator<String> iterClassNames = config.getFluentWaitIgnore().getExceptionClassNames().iterator();
			while (iterClassNames.hasNext()) {
				String className = iterClassNames.next();
				Class<? extends Throwable> c = (Class<? extends Throwable>)Class.forName(className);
				getSuite().getFluentWait().ignoring(c);
			}
		} catch (InstantiationException e) {
			logger.debug(e.getMessage(), e);
			throw e;
		} catch (IllegalAccessException e) {
			logger.debug(e.getMessage(), e);
			throw e;
		} catch (ClassNotFoundException e) {
			logger.debug(e.getMessage(), e);
			throw e;
		}catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw e;
		}
	}

	public XMLTestSuite getSuite() {
		return suite;
	}
}
