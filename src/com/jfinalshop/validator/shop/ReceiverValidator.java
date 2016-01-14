package com.jfinalshop.validator.shop;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class ReceiverValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("receiver.name", "errorMessages", "收货人不允许为空!");
		validateRequiredString("receiver.areaPath", "errorMessages", "地区不允许为空!");
		validateRequiredString("receiver.address", "errorMessages", "联系地址不允许为空!");
		validateRequiredString("receiver.zipCode", "errorMessages", "邮编不允许为空!");
		validateRequiredString("receiver.isDefault", "errorMessages", "是否默认不允许为空!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/shop/error.html");
	}

}
