package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class AreaValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("area.name", "errorMessages", "地区名称不允许为空!");		
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");		
	}
}
