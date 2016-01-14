package com.jfinalshop.validator.shop;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class PasswordValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("member.password", "errorMessages", "新密码长度必须在【4】到【20】之间!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/shop/error.html");
	}

}
