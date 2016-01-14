package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.Article;
import com.jfinalshop.model.ArticleCategory;

public class ArticleValidator extends Validator{
	
	@Override
	protected void validate(Controller c) {
		validateRequiredString("article.title", "titleMessages", "标题不允许为空!");
		validateRequiredString("article.articleCategory_id", "articleCategoryIdMessages", "文章分类不允许为空!");
		validateRequiredString("article.content", "contentMessages", "文章内容不允许为空!");
		
		validateRequiredString("article.isPublication", "isPublicationMessages", "是否发布不允许为空!");
		validateRequiredString("article.isTop", "isTopMessages", "是否置顶不允许为空!");
		validateRequiredString("article.isRecommend", "isRecommendMessages", "是否推荐不允许为空!");
	}

	@Override
	protected void handleError(Controller c) {
		c.keepModel(Article.class);
		c.setAttr("articleCategoryTreeList", ArticleCategory.dao.getArticleCategoryTreeList());
		c.render("/admin/article_input.html");
	}

}
