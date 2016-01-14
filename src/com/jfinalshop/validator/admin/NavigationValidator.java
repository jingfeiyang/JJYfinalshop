package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.ArticleCategory;
import com.jfinalshop.model.Navigation;
import com.jfinalshop.model.ProductCategory;

public class NavigationValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("navigation.name", "nameMessages", "导航名称不允许为空!");
		validateRequiredString("navigation.url", "urlMessages", "链接地址不允许为空!");
		validateRequiredString("navigation.orderList", "orderListMessages", "排序不允许为空!");
		validateRequiredString("navigation.isVisible", "isVisibleMessages", "是否显示不允许为空!");
		validateRequiredString("navigation.isBlankTarget", "isBlankTargetMessages", "在新窗口中打开不允许为空!");
		
		validateInteger("navigation.orderList", 0, 100, "orderListMessages", "排序必须为零或正整数!");
	}

	@Override
	protected void handleError(Controller c) {
		c.keepModel(Navigation.class);
		c.setAttr("productCategoryTreeList", ProductCategory.dao.getProductCategoryTreeList());
		c.setAttr("articleCategoryTreeList", ArticleCategory.dao.getArticleCategoryTreeList());
		c.render("/admin/navigation_input.html");
	}

}
