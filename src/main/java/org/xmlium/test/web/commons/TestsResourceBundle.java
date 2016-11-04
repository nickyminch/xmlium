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
package org.xmlium.test.web.commons;

import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class TestsResourceBundle extends ResourceBundle {

	PropertyResourceBundle bundle;
	
	public TestsResourceBundle(PropertyResourceBundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public Enumeration<String> getKeys() {
		// TODO Auto-generated method stub
		return bundle.getKeys();
	}

	@Override
	protected Object handleGetObject(String arg0) {
		// TODO Auto-generated method stub
		if(!bundle.containsKey(arg0))
		{
			if(parent!=null){
				return parent.getString(arg0);
			}else{
				return null;
			}
		}
		return bundle.getString(arg0);
	}
	
	public void setParent(ResourceBundle parent){
		super.setParent(parent);
	}
}
