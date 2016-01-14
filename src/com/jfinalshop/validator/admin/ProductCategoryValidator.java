package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.ProductCategory;

public class ProductCategoryValidator extends Validator{

	@Override
	protected void validate(Controller c) {		
		validateRequiredString("productCategory.name", "nameMessages", "分类名称不允许为空!");
		validateInteger("productCategory.orderList", 1, 1000, "orderListMessages", "排序必须为零或正整数，小于1000!");
	}

	@Override
	protected void handleError(Controller c) {
		c.setAttr("productCategoryTreeList", ProductCategory.dao.getAll());
		c.render("/admin/product_category_input.html");
	}
	
}
