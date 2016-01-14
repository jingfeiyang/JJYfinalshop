package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class DeliveryTypeValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("deliveryType.name", "errorMessages","配送方式名称不允许为空!");
		validateRequiredString("deliveryType.firstWeight", "errorMessages", "首重量不允许为空!");
		validateRequiredString("deliveryType.continueWeight", "errorMessages", "续重量不允许为空!");
		validateRequiredString("firstWeightUnit", "errorMessages", "首重单位不允许为空!");
		validateRequiredString("continueWeightUnit", "errorMessages", "续重单位不允许为空!");
		validateRequiredString("deliveryType.firstWeightPrice", "errorMessages", "首重价格不允许为空!");
		validateRequiredString("deliveryType.continueWeightPrice", "errorMessages", "续重价格不允许为空!");
		validateRequiredString("deliveryType.orderList", "errorMessages", "排序不允许为空!");
		
		validateInteger("deliveryType.orderList", 0, 100, "errorMessages", "排序必须为零或正整数!");		
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");		
	}

}
