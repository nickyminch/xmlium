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

import org.apache.log4j.Logger;

public class NamedImpl implements NamedExecutable {
	static final Logger logger = Logger.getLogger(XMLLambdaTest.class);
	public void execute(XMLTestSuite suite, String name) throws Throwable{
		XMLTestCase currentTest = new XMLTestCase();
		currentTest.reset();
		logger.info("Running test file: "+name);
		currentTest.initFromXML(suite, name);
		currentTest.test();
	}
}
