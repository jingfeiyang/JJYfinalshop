package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class ShippingValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("shipping.deliveryCorpName", "errorMessages", "物流公司不允许为空!");
		validateRequiredString("shipping.shipName", "errorMessages", "收货人姓名不允许为空!");
		validateRequiredString("shipping.shipAreaPath", "errorMessages", "收货人地区不允许为空!");
		validateRequiredString("shipping.shipAddress", "errorMessages", "收货人地址不允许为空!");
		validateRequiredString("shipping.shipZipCode", "errorMessages", "邮编不允许为空!");
		validateRequiredString("shipping.deliveryType_id", "errorMessages", "配送方式不允许为空!");
		validateRequiredString("shipping.deliveryFee", "errorMessages", "物流费用不允许为空!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");
	}

}
