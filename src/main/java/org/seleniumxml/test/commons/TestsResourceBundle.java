package org.seleniumxml.test.commons;

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
