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
package org.xmlium.test.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.By;
import org.xmlium.test.commons.xml.XMLTestCase;
import org.xmlium.testsuite.ByXPath;

public class TestTests extends XMLTestCase {
	private static final Logger logger = Logger.getLogger(TestTests.class.getSimpleName());
//	public static void main(String[] args) {
//		TestTests tt = new TestTests();
//		tt.extractLocale();
//		tt.loadBundles();
//		final String strRefresh = tt.getBundle().getString("b2b.order.display.submit.update");
//		System.err.println(strRefresh);
//		Pattern p = Pattern.compile("properties:(.+)");
//		Matcher m = p.matcher("properties:abc.def");
//		if(m.matches()){
//			System.err.println(m.group(1));
//		}
//		m = p.matcher("abc.def");
//		if(m.matches()){
//			System.err.println(m.group(1));
//		}else{
//			System.err.println("OK!");
//		}
//	}
	
	@Test
	public void testCheckValue(){
		logger.info(checkValue("expression:java.lang.Math.random()*1000"));
	}
	
	@Test
	public void testByXpath(){
		Matcher matcher = xpathIndexStringPattern.matcher("index:${first}");
		if(!matcher.matches()){
			fail("does not match");
		}
		matcher = xpathIndexStringPattern.matcher("//div[contains(@id, 'portfoliodetail-form:etfDataTable:index:${first}')]/descendant::span[contains(@class,'ui-chkbox-icon ui-icon ui-icon-blank ui-c')]");
		if(!matcher.matches()){
			
			fail("does not match");
		}else{
			logger.debug(matcher.group(1));
			logger.debug(matcher.group(2));
		}
		ByXPath xpath = new ByXPath();
		xpath.setIndexedValue("//div[contains(@id, 'portfoliodetail-form:etfDataTable:index:${first}')]/descendant::span[contains(@class,'ui-chkbox-icon ui-icon ui-icon-blank ui-c')]");
		assertEquals(getByIndexedValue(xpath, xpath.getIndexedValue()), By.xpath("//div[contains(@id, 'portfoliodetail-form:etfDataTable:0')]/descendant::span[contains(@class,'ui-chkbox-icon ui-icon ui-icon-blank ui-c')]"));
	}
	
	@Test
	public void valueExpressionsTest(){
		String str1 = "5*${0}";
		String str2 = "${0}+2";
		String str3 = "3-${0}";
		String str4 = "3/${0}";
		Matcher valueMatcher1 = valueExpressionStringPattern1.matcher(str1);
		assertEquals(true, valueMatcher1.matches());
		Matcher valueMatcher2 = valueExpressionStringPattern2.matcher(str2);
		assertEquals(true, valueMatcher2.matches());
		Matcher valueMatcher3 = valueExpressionStringPattern1.matcher(str3);
		assertEquals(true, valueMatcher3.matches());
		Matcher valueMatcher4 = valueExpressionStringPattern1.matcher(str4);
		assertEquals(true, valueMatcher4.matches());
		
		Matcher valueMatcher5 = valueExpressionStringPattern2.matcher(str1);
		assertEquals(false, valueMatcher5.matches());
		Matcher valueMatcher6 = valueExpressionStringPattern1.matcher(str2);
		assertEquals(false, valueMatcher6.matches());
		Matcher valueMatcher7 = valueExpressionStringPattern2.matcher(str3);
		assertEquals(false, valueMatcher7.matches());
		Matcher valueMatcher8 = valueExpressionStringPattern2.matcher(str4);
		assertEquals(false, valueMatcher8.matches());
	}

	@Test
	public void valueExpression3Test(){
		String str1 = "${1}*${0}";
		String str2 = "${0}+${1}";
		String str3 = "${1}-${0}";
		String str4 = "${1}/${0}";
		Matcher valueMatcher1 = valueExpressionStringPattern3.matcher(str1);
		assertEquals(true, valueMatcher1.matches());
		Matcher valueMatcher2 = valueExpressionStringPattern3.matcher(str2);
		assertEquals(true, valueMatcher2.matches());
		Matcher valueMatcher3 = valueExpressionStringPattern3.matcher(str3);
		assertEquals(true, valueMatcher3.matches());
		Matcher valueMatcher4 = valueExpressionStringPattern3.matcher(str4);
		assertEquals(true, valueMatcher4.matches());
		
		Matcher valueMatcher5 = valueExpressionStringPattern2.matcher(str1);
		assertEquals(false, valueMatcher5.matches());
		Matcher valueMatcher6 = valueExpressionStringPattern1.matcher(str2);
		assertEquals(false, valueMatcher6.matches());
		Matcher valueMatcher7 = valueExpressionStringPattern2.matcher(str3);
		assertEquals(false, valueMatcher7.matches());
		Matcher valueMatcher8 = valueExpressionStringPattern1.matcher(str4);
		assertEquals(false, valueMatcher8.matches());
	}
}
