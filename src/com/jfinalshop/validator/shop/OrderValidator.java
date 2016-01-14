package com.jfinalshop.validator.shop;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class OrderValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("deliveryType_id", "errorMessages", "请选择配送方式！");
		validateRequiredString("paymentConfig_id", "errorMessages", "请选择支付方式！");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/shop/error.html");
	}

}
