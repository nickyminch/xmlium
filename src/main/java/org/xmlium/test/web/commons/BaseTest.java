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
package org.xmlium.test.web.commons;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

public class BaseTest extends ResourcesLocator{

	protected WebDriver driver = null;
	protected FluentWait<WebDriver> fluentWait = null;
	protected long timeout = 5;
	protected TimeUnit timeoutUnits = TimeUnit.MINUTES;
	protected String url = null;
	private String lang = null;
	private Locale locale = null;
	private ResourceBundle bundle = null;
	String pathPatternString = ".+\\.properties";
	String filePatternString = ".+\\.properties";
	private boolean initialized = false;
	private NumberFormat currencyFormat = NumberFormat.getInstance();

	public BaseTest() {
		super();
	}
	
	protected void init(){
		if(!initialized){
			//extractLocale();
			//loadBundles();
			initialized = true;
		}
	}


	protected void extractLocale() {
		Pattern p = Pattern.compile("language=(\\w{2})");
		Matcher m = p.matcher(url);
		if (m.find()) {
			lang = m.group(1);
		}
		System.err.println(lang);
		if (lang != null) {
			locale = new Locale(lang);
		} else {
			locale = Locale.getDefault();
		}

		System.err.println(locale);
	}

	protected void loadBundles() {
		if(pathPatternString==null){
			return;
		}
		Pattern properties = Pattern.compile(filePatternString);
		List<String> files = getPropertyResources(pathPatternString, properties);
		System.err.println(properties.matcher(filePatternString ).matches());
		System.err.println(getUnderscorePatter().matcher(pathPatternString).matches());
		ResourceBundle parent = null;
		URLClassLoader cl = null;
		try {
			File f = new File(pathPatternString);
			cl = new URLClassLoader(new URL[]{f.toURI().toURL()});
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Iterator<String> iterResources = files.iterator();
		while (iterResources.hasNext()) {
			String fileName = (String) iterResources.next();
			System.err.println(fileName);
			try {
				TestsResourceBundle bundle = new TestsResourceBundle((PropertyResourceBundle)ResourceBundle.getBundle(fileName, locale, cl));
				if(parent!=null){
					bundle.setParent(parent);
				}
				parent = bundle;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		bundle = parent;
	}

	public ResourceBundle getBundle() {
		return bundle;
	}

	public WebDriver getDriver() {
		return driver;
	}

	public FluentWait<WebDriver> getFluentWait() {
		return fluentWait;
	}

	public long getTimeout() {
		return timeout;
	}

	public TimeUnit getTimeoutUnits() {
		return timeoutUnits;
	}

	public String getUrl() {
		return url;
	}

	public String getLang() {
		return lang;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	public void setFluentWait(FluentWait<WebDriver> fluentWait) {
		this.fluentWait = fluentWait;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void setTimeoutUnits(TimeUnit timeoutUnits) {
		this.timeoutUnits = timeoutUnits;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setBundle(ResourceBundle bundle) {
		this.bundle = bundle;
	}

	public String getPathPatternString() {
		return pathPatternString;
	}

	public void setPathPatternString(String pathPatternString) {
		this.pathPatternString = pathPatternString;
	}

	public String getFilePatternString() {
		return filePatternString;
	}

	public void setFilePatternString(String filePatternString) {
		this.filePatternString = filePatternString;
	}

	public NumberFormat getCurrencyFormat() {
		return currencyFormat;
	}

	public void setCurrencyFormat(NumberFormat currencyFormat) {
		this.currencyFormat = currencyFormat;
	}

}