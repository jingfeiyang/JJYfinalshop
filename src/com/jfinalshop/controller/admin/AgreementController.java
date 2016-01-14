package com.jfinalshop.controller.admin;

import com.jfinal.aop.Before;
import com.jfinalshop.model.Agreement;
import com.jfinalshop.validator.admin.AgreementValidator;

/**
 * 后台类 - 会员注册协议
 * 
 */
public class AgreementController extends BaseAdminController<Agreement>{
	
	private Agreement agreement;
	
	// 编辑
	public void edit() {
		setAttr("agreement", Agreement.dao.getAll());
		render("/admin/agreement_input.html");
	}
	
	// 更新
	@Before(AgreementValidator.class)
	public void update(){
		agreement = getModel(Agreement.class);
		agreement.update();
	}

}
