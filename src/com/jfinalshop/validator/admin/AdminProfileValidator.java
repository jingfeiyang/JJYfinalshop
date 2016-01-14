package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.Admin;

public class AdminProfileValidator extends Validator{
	
	@Override
	protected void validate(Controller c) {		
		validateString("admin.password", 4, 20, "errorMessages", "新密码长度允许在【4】到【20】之间!");
		validateEmail("admin.email", "errorMessages", "E-mail格式错误!");
		validateEqualField("admin.password", "rePassword", "errorMessages", "两次密码输入不一致!");
	}

	@Override
	protected void handleError(Controller c) {
		c.keepModel(Admin.class);
		c.render("/admin/error.html");
	}
}
