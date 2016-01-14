package com.jfinalshop.controller.admin;

import com.jfinal.aop.Before;
import com.jfinalshop.model.Footer;
import com.jfinalshop.validator.admin.FooterValidator;

/**
 * 后台类 - 页面底部信息
 * 
 */
public class FooterController extends BaseAdminController<Footer>{
	
	private Footer footer;
	
	// 编辑
	public void edit() {
		setAttr("footer", Footer.dao.getAll());
		render("/admin/footer_input.html");
	}
	
	// 更新
	@Before(FooterValidator.class)
	public void update(){
		footer = getModel(Footer.class);
		footer.update();
	}
}
