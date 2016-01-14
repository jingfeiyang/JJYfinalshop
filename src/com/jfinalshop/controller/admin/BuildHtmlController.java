package com.jfinalshop.controller.admin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinalshop.interceptor.AdminInterceptor;
import com.jfinalshop.model.Article;
import com.jfinalshop.model.ArticleCategory;
import com.jfinalshop.model.Product;
import com.jfinalshop.model.ProductCategory;
import com.jfinalshop.service.HtmlService;

/**
 * 后台类 - 生成静态
 * 
 */
@Before(AdminInterceptor.class)
public class BuildHtmlController extends Controller {

	private String buildType;// 更新类型
	private String buildContent;// 更新内容
	private int maxResults;// 每次更新数
	private int firstResult;// 更新文章起始结果数
	private Date beginDate;// 开始日期
	private Date endDate;// 结束日期
	private String id;
	public static final String STATUS = "status";

	public void allInput() {
		setAttr("defaultBeginDate", getDefaultBeginDate());
		setAttr("defaultEndDate", getDefaultEndDate());
		render("/admin/build_html_all_input.html");
	}

	public void all() {
		buildType = getPara("buildType","");
		//buildTypeInput = getPara("buildTypeInput");
		buildContent = getPara("buildContent","");
		firstResult = getParaToInt("firstResult",0);
		maxResults = getParaToInt("maxResults",50);
		beginDate = getParaToDate("beginDate",getDefaultBeginDate());
		endDate = getParaToDate("endDate",getDefaultEndDate());
		
		if (StringUtils.isEmpty(buildType)) {
			buildType = "all";
		}
		if (StringUtils.isEmpty(buildContent)) {
			buildContent = "baseJavascript";
		}
		
		if (buildContent.equalsIgnoreCase("baseJavascript")) {
			HtmlService.service.baseJavascriptBuildHtml();
			Map<String, String> jsonMap = new HashMap<String, String>();
			jsonMap.put(STATUS, "baseJavascriptFinish");
			jsonMap.put("buildTotal", "1");
			renderJson(jsonMap);
		}
		if (buildContent.equalsIgnoreCase("errorPage")) {
			HtmlService.service.errorPageBuildHtml();
			HtmlService.service.errorPageAccessDeniedBuildHtml();
			HtmlService.service.errorPage500BuildHtml();
			HtmlService.service.errorPage404BuildHtml();
			HtmlService.service.errorPage403BuildHtml();
			Map<String, String> jsonMap = new HashMap<String, String>();
			jsonMap.put(STATUS, "errorPageFinish");
			jsonMap.put("buildTotal", "1");
			renderJson(jsonMap);
		}
		if (buildContent.equalsIgnoreCase("index")) {
			HtmlService.service.indexBuildHtml();
			Map<String, String> jsonMap = new HashMap<String, String>();
			jsonMap.put(STATUS, "indexFinish");
			jsonMap.put("buildTotal", "1");
			renderJson(jsonMap);
		}
		if (buildContent.equalsIgnoreCase("login")) {
			HtmlService.service.loginBuildHtml();
			Map<String, String> jsonMap = new HashMap<String, String>();
			jsonMap.put(STATUS, "loginFinish");
			jsonMap.put("buildTotal", "1");
			renderJson(jsonMap);
		}
		if (buildContent.equalsIgnoreCase("article")) {
			List<Article> articleList = null;
			if (buildType.equalsIgnoreCase("all")) {
				articleList = Article.dao.getArticleList(firstResult, maxResults);
			} else if (buildType.equalsIgnoreCase("date")) {
			
				if (beginDate != null) {
					Calendar c = Calendar.getInstance();
					c.setTime(beginDate);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					beginDate = c.getTime();
				}
				if (endDate != null) {
					Calendar c = Calendar.getInstance();
					c.setTime(endDate);
					c.set(Calendar.HOUR_OF_DAY, 23);
					c.set(Calendar.MINUTE, 59);
					c.set(Calendar.SECOND, 59);
					endDate = c.getTime();
				}
				articleList = Article.dao.getArticleList(beginDate, endDate,firstResult, maxResults);
			}
			if (articleList != null && articleList.size() > 0) {
				for (Article article : articleList) {
					HtmlService.service.articleContentBuildHtml(article);
				}
			}
			if (articleList != null && articleList.size() == maxResults) {
				Map<String, String> jsonMap = new HashMap<String, String>();
				int nextFirstResult = firstResult + articleList.size();
				jsonMap.put(STATUS, "articleBuilding");
				jsonMap.put("firstResult", String.valueOf(nextFirstResult));
				renderJson(jsonMap);
			} else {
				Map<String, String> jsonMap = new HashMap<String, String>();
				int buildTotal = firstResult + 1 + articleList.size();
				jsonMap.put(STATUS, "articleFinish");
				jsonMap.put("buildTotal", String.valueOf(buildTotal));
				renderJson(jsonMap);
			}
		}
		if (buildContent.equalsIgnoreCase("product")) {
			List<Product> productList = null;
			if (buildType.equalsIgnoreCase("all")) {
				productList = Product.dao.getProductList(firstResult, maxResults);
			} else if (buildType.equalsIgnoreCase("date")) {
				if (beginDate != null) {
					Calendar c = Calendar.getInstance();
					c.setTime(beginDate);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					beginDate = c.getTime();
				}
				if (endDate != null) {
					Calendar c = Calendar.getInstance();
					c.setTime(endDate);
					c.set(Calendar.HOUR_OF_DAY, 23);
					c.set(Calendar.MINUTE, 59);
					c.set(Calendar.SECOND, 59);
					endDate = c.getTime();
				}
				productList = Product.dao.getProductList(beginDate, endDate, firstResult, maxResults);
			}
			if (productList != null && productList.size() > 0) {
				for (Product product : productList) {
					HtmlService.service.productContentBuildHtml(product);
				}
			}
			if (productList != null && productList.size() == maxResults) {
				Map<String, String> jsonMap = new HashMap<String, String>();
				int nextFirstResult = firstResult + productList.size();
				jsonMap.put(STATUS, "productBuilding");
				jsonMap.put("firstResult", String.valueOf(nextFirstResult));
				renderJson(jsonMap);
			} else {
				Map<String, String> jsonMap = new HashMap<String, String>();
				int buildTotal = firstResult + 1 + productList.size();
				jsonMap.put(STATUS, "productFinish");
				jsonMap.put("buildTotal", String.valueOf(buildTotal));
				renderJson(jsonMap);
			}
		}
	}

	public void productInput() {
		setAttr("productCategoryTreeList", getProductCategoryTreeList());
		render("/admin/build_html_product_input.html");
	}
	
	public void product() {
		firstResult = getParaToInt("firstResult",0);
		maxResults = getParaToInt("maxResults",50);
		id = getPara("id","");
		List<Product> productList = new ArrayList<Product>();
		if (StringUtils.isEmpty(id)) {
			productList = Product.dao.getProductList(firstResult, maxResults);
		} else {
			ProductCategory productCategory = ProductCategory.dao.findById(id);
			productList = Product.dao.getProductList(productCategory, firstResult, maxResults);
		}
		if (productList != null && productList.size() > 0) {
			for (Product product : productList) {
				HtmlService.service.productContentBuildHtml(product);
			}
		}
		if (productList != null && productList.size() == maxResults) {
			Map<String, String> jsonMap = new HashMap<String, String>();
			int nextFirstResult = firstResult + productList.size();
			jsonMap.put(STATUS, "PRODUCT_BUILDING");
			jsonMap.put("firstResult", String.valueOf(nextFirstResult));
			renderJson(jsonMap);
		} else {
			Map<String, String> jsonMap = new HashMap<String, String>();
			int buildTotal = firstResult + 1 + productList.size();
			jsonMap.put(STATUS, "PRODUCT_FINISH");
			jsonMap.put("buildTotal", String.valueOf(buildTotal));
			renderJson(jsonMap);
		}
	}
	
	public void articleInput() {
		setAttr("articleCategoryTreeList", getArticleCategoryTreeList());
		render("/admin/build_html_article_input.html");
	}
	
	public void article() {
		firstResult = getParaToInt("firstResult",0);
		maxResults = getParaToInt("maxResults",50);
		id = getPara("id","");
		List<Article> articleList = new ArrayList<Article>();
		if (StringUtils.isEmpty(id)) {
			articleList = Article.dao.getArticleList(firstResult, maxResults);
		} else {
			ArticleCategory articleCategory = ArticleCategory.dao.findById(id);
			articleList = Article.dao.getArticleList(articleCategory, firstResult, maxResults);
		}
		if (articleList != null && articleList.size() > 0) {
			for (Article article : articleList) {
				HtmlService.service.articleContentBuildHtml(article);
			}
		}
		if (articleList != null && articleList.size() == maxResults) {
			Map<String, String> jsonMap = new HashMap<String, String>();
			int nextFirstResult = firstResult + articleList.size();
			jsonMap.put(STATUS, "ARTICLE_BUILDING");
			jsonMap.put("firstResult", String.valueOf(nextFirstResult));
			renderJson(jsonMap);
		} else {
			Map<String, String> jsonMap = new HashMap<String, String>();
			int buildTotal = firstResult + 1 + articleList.size();
			jsonMap.put(STATUS, "ARTICLE_FINISH");
			jsonMap.put("buildTotal", String.valueOf(buildTotal));
			renderJson(jsonMap);
		}
	}
	
	// 获取文章分类树
	public List<ArticleCategory> getArticleCategoryTreeList() {
		return ArticleCategory.dao.getArticleCategoryTreeList();
	}
		
	// 获取商品分类树
	public List<ProductCategory> getProductCategoryTreeList() {
		return ProductCategory.dao.getProductCategoryTreeList();
	}
		
	// 获取默认开始日期
	public Date getDefaultBeginDate() {
		return DateUtils.addDays(new Date(), -7);
	}

	// 获取默认结束日期
	public Date getDefaultEndDate() {
		return new Date();
	}
}
