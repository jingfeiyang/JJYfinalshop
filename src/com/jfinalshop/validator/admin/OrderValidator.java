package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class OrderValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("orders.deliveryType_id", "errorMessages", "内容不允许为空!");
		validateRequiredString("orders.shipName", "errorMessages", "收货人姓名不允许为空!");
		validateRequiredString("orders.shipAreaPath", "errorMessages", "收货人地区不允许为空!");
		validateRequiredString("orders.shipAddress", "errorMessages", "收货人地址不允许为空!");
		validateRequiredString("orders.shipZipCode", "errorMessages", "邮编不允许为空!");
		
		validateRequiredString("orders.deliveryFee", "errorMessages", "配送费用不允许为空!");
		validateRequiredString("orders.paymentFee", "errorMessages", "支付费用不允许为空!");
		validateRequiredString("orders.productWeight", "errorMessages", "商品重量不允许为空!");
		validateRequiredString("productWeightUnit", "errorMessages", "商品重量单位不允许为空!");
		validateRequiredString("orders.deliveryType_id", "errorMessages", "内容不允许为空!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");
	}

}
