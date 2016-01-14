package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.Brand;

public class BrandValidator extends Validator{

	@Override
	protected void validate(Controller c) {
		c.getFile();
		validateRequiredString("brand.name", "nameMessages", "品牌名称不允许为空!");
		validateUrl("brand.url", "urlMessages", "网址格式错误!");
		validateRequiredString("brand.orderList", "orderListMessages", "排序不允许为空!");
		
		validateInteger("brand.orderList", 0, 100, "orderListMessages", "排序必须为零或正整数!");
	}

	@Override
	protected void handleError(Controller c) {
		c.keepModel(Brand.class);
	    c.render("/admin/brand_input.html");
	}
}
