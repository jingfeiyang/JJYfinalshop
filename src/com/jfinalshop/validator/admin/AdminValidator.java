package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.Admin;
import com.jfinalshop.model.Role;

public class AdminValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("admin.username", "usernameMessages", "用户名不允许为空!");		
		validateRequiredString("admin.email", "emailMessages", "E-mail不允许为空!");
		validateRequiredString("admin.isAccountEnabled", "isAccountEnabledMessages", "是否启用不允许为空!");		
		validateString("admin.username", 2, 20, "usernameMessages", "用户名长度必须在【2】到【20】之间!");		
		validateEmail("admin.email", "emailMessages", "E-mail格式错误!");		
        validateRegex("admin.username", "^[0-9a-z_A-Z\u4e00-\u9fa5]+$", "usernameMessages", "用户名只允许包含中文、英文、数字和下划线!");
        // 更新为空不更新密码
        String actionKey = getActionKey();	
        if (!actionKey.equals("/admin/update")) {
			validateRequiredString("admin.password", "passwordMessages", "密码不允许为空!");
			validateEqualField("admin.password", "rePassword", "rePasswordMessages", "两次密码输入不一致!");
			validateString("admin.password", 4, 20, "passwordMessages", "密码长度必须在【4】到【20】之间!");
		}
        
		String newValue = c.getPara("admin.username","");	
		if (Admin.dao.getAdminByUsername(newValue) != null) {
			addError("usernameMessages","用户名称已存在!");
		}
	}

	@Override
	protected void handleError(Controller c) {
		c.keepModel(Admin.class);
		c.setAttr("allRole", Role.dao.getAllRole());
		c.render("/admin/admin_input.html");
	}
}
