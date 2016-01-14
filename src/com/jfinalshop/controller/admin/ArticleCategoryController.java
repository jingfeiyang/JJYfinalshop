package com.jfinalshop.controller.admin;

import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Article;
import com.jfinalshop.model.ArticleCategory;
import com.jfinalshop.util.CommonUtil;
import com.jfinalshop.validator.admin.ArticleCategoryValidator;

/**
 * 后台类 - 文章分类
 * 
 */
public class ArticleCategoryController extends BaseAdminController<ArticleCategory>{
	
	private ArticleCategory articleCategory;
	
	// 列表
	public void list(){
		setAttr("articleCategoryTreeList", ArticleCategory.dao.getArticleCategoryTreeList());
		render("/admin/article_category_list.html");
	}


	// 添加
	public void add() {
		setAttr("articleCategoryTreeList", ArticleCategory.dao.getArticleCategoryTreeList());
		render("/admin/article_category_input.html");
	}

	// 编辑
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("articleCategory", ArticleCategory.dao.findById(id));
		}
		setAttr("articleCategoryTreeList", ArticleCategory.dao.getArticleCategoryTreeList());
		render("/admin/article_category_input.html");
	}

	// 添加
	@Before(ArticleCategoryValidator.class)
	public void save(){
		articleCategory = getModel(ArticleCategory.class);	
		articleCategory.set("id", CommonUtil.getUUID());
		ArticleCategory parent = ArticleCategory.dao.findById(articleCategory.getStr("parent_id"));
		if (parent != null) {
			String parentPath = parent.getStr("path");
			articleCategory.set("path",parentPath + ArticleCategory.PATH_SEPARATOR + articleCategory.getStr("id"));
		} else {
			articleCategory.set("path",articleCategory.getStr("id"));
		}
		articleCategory.set("createDate", new Date());
		articleCategory.save();
		redirect("/articleCategory/list");		
	}
	
	// 修改
	@Before(ArticleCategoryValidator.class)
	public void update(){
		articleCategory = getModel(ArticleCategory.class);
		ArticleCategory parent = ArticleCategory.dao.findById(articleCategory.getStr("parent_id"));
		if (parent != null) {
			String parentPath = parent.getStr("path");
			articleCategory.set("path",parentPath + ArticleCategory.PATH_SEPARATOR + articleCategory.getStr("id"));
		} else {
			articleCategory.set("path",articleCategory.getStr("id"));
		}
		updated(articleCategory);
		redirect("/articleCategory/list");		
	}
	
	// 删除
	public void delete(){
		String id = getPara("id","");
		ArticleCategory articleCategory = ArticleCategory.dao.findById(id);
		List<ArticleCategory> childrenArticleCategoryList = articleCategory.getChildren();
		if (childrenArticleCategoryList != null && childrenArticleCategoryList.size() > 0) {
			ajaxJsonErrorMessage("此文章分类存在下级分类，删除失败!");
		}
		List<Article> articleList = ArticleCategory.dao.getArticleList();
		if (articleList != null && articleList.size() > 0) {
			ajaxJsonErrorMessage("此文章分类下存在文章，删除失败!");
		}
		ArticleCategory.dao.deleteById(id);
		ajaxJsonSuccessMessage("删除成功！");		
	}
}
