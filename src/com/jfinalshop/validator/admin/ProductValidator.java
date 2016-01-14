package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class ProductValidator extends Validator{

	@Override
	protected void validate(Controller c) {
		c.getFile();
		validateRequiredString("product.name", "errorMessages", "商品名称不允许为空!");
		validateRequiredString("product.price", "errorMessages", "销售价不允许为空!");
		validateRequiredString("product.marketPrice", "errorMessages", "市场价不允许为空!");
		validateRequiredString("product.weight", "errorMessages", "商品重量不允许为空!");
		validateRequiredString("weightUnit", "errorMessages", "商品重量单位不允许为空!");
		validateRequiredString("product.isMarketable", "errorMessages", "是否上架不允许为空!");
		validateRequiredString("product.isBest", "errorMessages", "是否精品不允许为空!");
		validateRequiredString("product.isNew", "errorMessages", "是否新品不允许为空!");
		validateRequiredString("product.isHot", "errorMessages", "是否热销不允许为空!");
		validateRequiredString("product.productCategory_id", "errorMessages", "所属分类不允许为空!");
		
		validateInteger("product.point", 0, 10000, "errorMessages", "积分必须为零或正整数!");
		validateInteger("product.store", 0, 10000, "errorMessages", "库存必须为零或正整数!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");
	}

}
