package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.ArticleCategory;

public class ArticleCategoryValidator extends Validator{

	@Override
	protected void validate(Controller c) {
		validateRequiredString("articleCategory.name", "nameMessages", "分类名称不允许为空!");
		validateRequiredString("articleCategory.orderList", "orderListMessages", "排序不允许为空!");
		
		validateInteger("articleCategory.orderList", 0, 100, "orderListMessages", "排序必须为零或正整数!");
	}

	@Override
	protected void handleError(Controller c) {
		c.keepModel(ArticleCategory.class);
		c.setAttr("articleCategoryTreeList", ArticleCategory.dao.getArticleCategoryTreeList());
		c.render("/admin/article_category_input.html");
	}
}
