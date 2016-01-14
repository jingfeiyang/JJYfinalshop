package com.jfinalshop.validator.admin;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.ProductAttribute.AttributeType;
import com.jfinalshop.model.ProductType;

public class ProductAttributeValidator extends Validator{

	@Override
	protected void validate(Controller c) {
		validateRequiredString("productAttribute.name", "nameMessages", "商品属性名称不允许为空!");
		validateRequiredString("productAttribute.productType_id", "productTypeIdMessages", "商品类型不允许为空!");
		validateRequiredString("attributeType", "attributeTypeMessages", "商品属性类型不允许为空!");
		validateRequiredString("productAttribute.isRequired", "isRequiredMessages", "是否必填不允许为空!");
		validateRequiredString("productAttribute.isEnabled", "isEnabledMessages", "是否启用不允许为空!");
		validateRequiredString("productAttribute.orderList", "orderListMessages", "排序不允许为空!");
		validateInteger("productAttribute.orderList", 1, 1000, "orderListMessages", "排序必须为零或正整数，小于1000!");
	}

	@Override
	protected void handleError(Controller c) {
		c.setAttr("allProductType", ProductType.dao.getAll());
		c.setAttr("allAttributeType", getAllAttributeType());		
		c.render("/admin/product_attribute_input.html");
	}
	
	// 获取所有商品属性类型
	public List<AttributeType> getAllAttributeType() {
		List<AttributeType> allAttributeType = new ArrayList<AttributeType>();
		for (AttributeType attributeType : AttributeType.values()) {
			allAttributeType.add(attributeType);
		}
		return allAttributeType;
	}
}
