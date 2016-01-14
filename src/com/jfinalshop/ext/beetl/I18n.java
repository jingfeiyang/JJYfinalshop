package com.jfinalshop.ext.beetl;

import javax.servlet.http.HttpServletRequest;

import org.beetl.core.Context;
import org.beetl.core.Function;




public class I18n implements Function {
	
	public Object call(Object[] obj, Context context) {
		HttpServletRequest req = (HttpServletRequest) context.getGlobal("request");
		try {
			return com.jfinal.i18n.I18n.use(req.getLocale().toString()).get((String) obj[0]);
		} catch (NullPointerException e) {
			return com.jfinal.i18n.I18n.use(req.getLocale().toString()).get((String) obj[0]);
		}
	}

}
