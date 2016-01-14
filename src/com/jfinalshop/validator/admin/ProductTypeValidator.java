package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.ProductType;

public class ProductTypeValidator extends Validator{

	@Override
	protected void validate(Controller c) {
		validateRequiredString("productType.name", "productTypeMessage", "商品类型不允许为空!");
		// 若不存在直接返回true
		String newValue = c.getPara("productType.name","");	
		if (!ProductType.dao.isUnique(newValue)) {
			addError("productTypeMessage","商品类型名称已存在!");
		}
	}

	@Override
	protected void handleError(Controller c) {
		c.keepModel(ProductType.class);
	    c.render("/admin/product_type_input.html");
	}

}
