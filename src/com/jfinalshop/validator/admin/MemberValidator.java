package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;

public class MemberValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("member.username", "errorMessages", "用户名不允许为空!");
		validateString("member.username", 2, 20, "errorMessages", "用户名长度必须在【2】到【20】之间!");		
		validateRegex("member.username", "^[0-9a-z_A-Z\u4e00-\u9fa5]+$", "errorMessages", "用户名只允许包含中文、英文、数字和下划线!");		
		validateRequiredString("member.email", "errorMessages", "E-mail不允许为空!");
		validateEmail("member.email", "errorMessages", "E-mail格式错误!");
		validateRequiredString("member.point", "errorMessages", "积分不允许为空!");
		validateInteger("member.point", 0, 10000, "errorMessages", "积分必须为零或正整数!");
		validateRequiredString("member.deposit", "errorMessages", "预存款不允许为空!");
		validateRequiredString("member.memberRank_id", "errorMessages", "会员等级不允许为空!");
		validateRequiredString("member.isAccountEnabled", "errorMessages", "是否启用不允许为空!");
		
		String password = c.getPara("member.password","");
		if (StrKit.notBlank(password)) {
			validateRequiredString("member.password", "errorMessages", "密码不允许为空!");
			validateString("member.password", 4, 20, "errorMessages", "密码长度必须在【4】到【20】之间!");
		}		
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");
	}

}
