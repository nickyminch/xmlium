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
package org.xmlium.test.web.xmlium;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.xmlium.test.web.commons.xml.XMLLambdaTest;

public class XmliumLambdaTest extends XMLLambdaTest {
	private static final Logger logger = Logger.getLogger(XmliumLambdaTest.class.getSimpleName());

	public XmliumLambdaTest() {
		super();
		try {
			logger.info("-> prepare");
			prepare();
			logger.info("-> prepared!");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@TestFactory
	public Collection<DynamicTest> getTests() {
		logger.info("getTests() ->");

		logger.info("getTests() <-");
		return super.getTests();
	}
	
//	@Test
//	public void test() throws Throwable{
//		Collection<DynamicTest> tests = super.getTests();
//		for(DynamicTest test: tests){
//			test.getExecutable().execute();
//		}
//	}
}
