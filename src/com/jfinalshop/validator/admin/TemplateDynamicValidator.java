package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class TemplateDynamicValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("templateFileContent", "errorMessages", "模板内容不允许为空!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");
	}

}
