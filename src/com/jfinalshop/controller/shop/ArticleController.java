package com.jfinalshop.controller.shop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinalshop.interceptor.NavigationInterceptor;
import com.jfinalshop.model.Article;
import com.jfinalshop.model.ArticleCategory;

/**
 * 前台类 - 文章
 * 
 */
@ControllerBind(controllerKey = "/shop/article")
@Before(NavigationInterceptor.class)
public class ArticleController extends Controller {
	
	private ArticleCategory articleCategory;
	private List<Article> recommendArticleList;
	private List<Article> hotArticleList;
	private List<Article> newArticleList;
	private List<ArticleCategory> pathList;
	private Page<Article> pager;
	private String id;
	
	// 文章分类列表
	
	public void list() {
		id = getPara("id","");
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",Article.DEFAULT_ARTICLE_LIST_PAGE_SIZE);
		
		if(StrKit.notBlank(id)){
			articleCategory = ArticleCategory.dao.findById(id);
		}
		
		recommendArticleList = Article.dao.getRecommendArticleList(articleCategory, Article.MAX_RECOMMEND_ARTICLE_LIST_COUNT);
		hotArticleList = Article.dao.getHotArticleList(articleCategory, Article.MAX_HOT_ARTICLE_LIST_COUNT);
		newArticleList = Article.dao.getNewArticleList(articleCategory, Article.MAX_NEW_ARTICLE_LIST_COUNT);
		pathList = ArticleCategory.dao.getArticleCategoryPathList(articleCategory);
		
		pager = Article.dao.getArticlePager(pageNumber,pageSize,articleCategory);
		
		setAttr("recommendArticleList", recommendArticleList);
		setAttr("hotArticleList", hotArticleList);
		setAttr("pathList", pathList);
		setAttr("newArticleList", newArticleList);
		setAttr("articleCategory", articleCategory);
		setAttr("pager", pager);
		render("/shop/article_list.html");
	}
	
	// 文章搜索
	public void search() {
		String keyword = getPara("keyword","");		
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",Article.DEFAULT_ARTICLE_LIST_PAGE_SIZE);
		
		if (StrKit.isBlank(keyword)){
			addActionError("搜索关键词不允许为空!");
			return;
		}		
		recommendArticleList = Article.dao.getRecommendArticleList(Article.MAX_RECOMMEND_ARTICLE_LIST_COUNT);
		hotArticleList = Article.dao.getHotArticleList(Article.MAX_HOT_ARTICLE_LIST_COUNT);
		newArticleList = Article.dao.getNewArticleList(Article.MAX_NEW_ARTICLE_LIST_COUNT);
		
		pager = Article.dao.search(pageNumber, pageSize,keyword);
		
		setAttr("recommendArticleList", recommendArticleList);
		setAttr("hotArticleList", hotArticleList);
		setAttr("newArticleList", newArticleList);
		setAttr("pager", pager);
		render("/shop/article_search.html");
	}

	// 文章点击统计
	public void ajaxCounter() {
		id = getPara("id");
		if (StrKit.isBlank(id)){
			addActionError("文章ID不允许为空!");
			return;
		}
		Article article =  Article.dao.findById(id);
		if (!article.getBoolean("isPublication")) {
			addActionError("您访问的文章尚未发布!");
			return;
		}
		Integer hits = article.getInt("hits") + 1;
		article.set("hits",hits);		
		article.update();
		
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("status", "success");
		jsonMap.put("hits", hits.toString());
		renderJson(jsonMap);
	}
	
	public void addActionError(String error){
		setAttr("errorMessages", error);
		render("/shop/error.html");	
	}
}
