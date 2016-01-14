package com.jfinalshop.controller.admin;


import java.io.File;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import com.jfinalshop.bean.HtmlConfig;
import com.jfinalshop.interceptor.AdminInterceptor;
import com.jfinalshop.model.Article;
import com.jfinalshop.model.ArticleCategory;
import com.jfinalshop.service.HtmlService;
import com.jfinalshop.util.TemplateConfigUtil;
import com.jfinalshop.validator.admin.ArticleValidator;

/**
 * 后台类 - 文章
 * 
 */
@Before(AdminInterceptor.class)
public class ArticleController extends BaseAdminController<Article>{
	
	private Article article;
	
	// 列表
	public void list(){		
		findByPage();
		render("/admin/article_list.html");
	}

	// 添加
	public void add() {
		setAttr("articleCategoryTreeList", ArticleCategory.dao.getArticleCategoryTreeList());
		render("/admin/article_input.html");
	}

	// 编辑
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("article", Article.dao.findById(id));
		}		
		setAttr("articleCategoryTreeList", ArticleCategory.dao.getArticleCategoryTreeList());
		render("/admin/article_input.html");
	}

	
	// 保存
	@Before(ArticleValidator.class)
	public void save(){
		article = getModel(Article.class);
		article.set("hits", 0);
		article.set("pageCount", 0);
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.ARTICLE_CONTENT);
		String htmlFilePath = htmlConfig.getHtmlFilePath();
		article.set("htmlFilePath", htmlFilePath);
		saved(article);
		HtmlService.service.articleContentBuildHtml(article);
		redirect("/article/list");	
	}
	
	// 更新
	@Before(ArticleValidator.class)
	public void update(){
		article = getModel(Article.class);
		List<String> htmlFilePathList = article.getHtmlFilePathList();
		if (htmlFilePathList != null && htmlFilePathList.size() > 0) {
			for (String htmlFilePath : htmlFilePathList) {
				File htmlFile = new File(PathKit.getWebRootPath() + htmlFilePath);
				if (htmlFile.exists()) {
					htmlFile.delete();
				}
			}
		}
		updated(article);
		redirect("/article/list");	
	}
	
	// 删除
	public void delete(){
		ids = getParaValues("ids");
		if(ids != null && ids.length > 0){
			for (String id : ids) {
				if(Article.dao.deleteById(id)){	
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}		
	}	
}
