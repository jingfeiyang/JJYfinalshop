package com.jfinalshop.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 页面底部信息
 * 
 */
public class Footer extends Model<Footer>{

	private static final long serialVersionUID = 7698222765198914997L;

	public static final Footer dao = new Footer();
	
	public static final String FOOTER_ID = "1";// 记录ID
	
	// 获取Footer
	public Footer getAll(){
		return dao.findFirst("select * from footer");
	}
	
	// 获取Footer对象
	public Footer getFooter() {
		return dao.findById(Footer.FOOTER_ID);
	}
}
