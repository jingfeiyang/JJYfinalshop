package com.jfinalshop.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.WebAppResourceLoader;

import com.jfinal.core.JFinal;
import com.jfinalshop.bean.HtmlConfig;
import com.jfinalshop.bean.SystemConfig;
import com.jfinalshop.model.Article;
import com.jfinalshop.model.ArticleCategory;
import com.jfinalshop.model.Footer;
import com.jfinalshop.model.FriendLink;
import com.jfinalshop.model.Navigation;
import com.jfinalshop.model.Product;
import com.jfinalshop.model.ProductCategory;
import com.jfinalshop.util.SystemConfigUtil;
import com.jfinalshop.util.TemplateConfigUtil;

/**
 * Service实现类 - 生成静态
 * 
 */
public class HtmlService {
	
	public static final HtmlService service = new HtmlService();
	
	public void buildHtml(String templateFilePath, String htmlFilePath, Map<String, Object> data) {
		try {
			ServletContext servletContext = JFinal.me().getServletContext();
			WebAppResourceLoader resourceLoader = new WebAppResourceLoader();
			Configuration cfg = Configuration.defaultConfiguration();
			GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
			Template template = gt.getTemplate(templateFilePath);
			template.binding(data);
			
			File htmlFile = new File(servletContext.getRealPath(htmlFilePath));
			File htmlDirectory = htmlFile.getParentFile();
			if (!htmlDirectory.exists()) {
				htmlDirectory.mkdirs();
			}
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(htmlFile), "UTF-8"));
			template.renderTo(out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 获取公共数据
	public Map<String, Object> getCommonData() {
		Map<String, Object> commonData = new HashMap<String, Object>();
		ServletContext servletContext = JFinal.me().getServletContext();
		SystemConfig systemConfig = SystemConfigUtil.getSystemConfig();
		commonData.put("base", servletContext.getContextPath());
		commonData.put("systemConfig", systemConfig);		
		commonData.put("topNavigationList", Navigation.dao.getTopNavigationList());
		commonData.put("middleNavigationList", Navigation.dao.getMiddleNavigationList());
		commonData.put("bottomNavigationList", Navigation.dao.getBottomNavigationList());
		commonData.put("friendLinkList", FriendLink.dao.getAll());
		commonData.put("pictureFriendLinkList", FriendLink.dao.getPictureFriendLinkList());
		commonData.put("textFriendLinkList", FriendLink.dao.getTextFriendLinkList());
		commonData.put("footer", Footer.dao.getFooter());
		return commonData;
	}
	
	public void baseJavascriptBuildHtml() {
		Map<String, Object> data = getCommonData();
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.BASE_JAVASCRIPT);
		String htmlFilePath = htmlConfig.getHtmlFilePath();
		String templateFilePath = htmlConfig.getTemplateFilePath();
		buildHtml(templateFilePath, htmlFilePath, data);
	}
	
	public void indexBuildHtml() {
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.INDEX);
		Map<String, Object> data = getCommonData();
		data.put("rootProductCategoryList", ProductCategory.dao.getRootProductCategoryList());
		data.put("bestProductList", Product.dao.getBestProductList(Product.MAX_BEST_PRODUCT_LIST_COUNT));
		data.put("hotProductList", Product.dao.getHotProductList(Product.MAX_HOT_PRODUCT_LIST_COUNT));
		data.put("newProductList", Product.dao.getNewProductList(Product.MAX_NEW_PRODUCT_LIST_COUNT));
		List<ProductCategory> allProductCategory = ProductCategory.dao.getAll();
		data.put("allProductCategoryList", allProductCategory);
		Map<String, List<ProductCategory>> productCategoryMap = new HashMap<String, List<ProductCategory>>();
		Map<String, List<Product>> bestProductMap = new HashMap<String, List<Product>>();
		Map<String, List<Product>> hotProductMap = new HashMap<String, List<Product>>();
		Map<String, List<Product>> newProductMap = new HashMap<String, List<Product>>();
		for (ProductCategory productCategory : allProductCategory) {
			String productCategory_id = productCategory.getStr("id");
			productCategoryMap.put(productCategory_id, productCategory.getChildrenProductCategoryList(productCategory));
			bestProductMap.put(productCategory_id, Product.dao.getBestProductList(productCategory_id, Product.MAX_BEST_PRODUCT_LIST_COUNT));
			hotProductMap.put(productCategory_id, Product.dao.getHotProductList(productCategory_id, Product.MAX_HOT_PRODUCT_LIST_COUNT));
			newProductMap.put(productCategory_id, Product.dao.getNewProductList(productCategory_id, Product.MAX_NEW_PRODUCT_LIST_COUNT));
		}
		data.put("productCategoryMap", productCategoryMap);
		data.put("bestProductMap", bestProductMap);
		data.put("hotProductMap", hotProductMap);
		data.put("newProductMap", newProductMap);
		
		data.put("rootArticleCategoryList", ArticleCategory.dao.getRootArticleCategoryList());
		data.put("recommendArticleList", Article.dao.getRecommendArticleList(Article.MAX_RECOMMEND_ARTICLE_LIST_COUNT));
		data.put("hotArticleList", Article.dao.getHotArticleList(Article.MAX_HOT_ARTICLE_LIST_COUNT));
		data.put("newArticleList", Article.dao.getNewArticleList(Article.MAX_NEW_ARTICLE_LIST_COUNT));
		List<ArticleCategory> allArticleCategory = ArticleCategory.dao.getAll();
		data.put("allArticleCategoryList", allArticleCategory);
		Map<String, List<ArticleCategory>> articleCategoryMap = new HashMap<String, List<ArticleCategory>>();
		Map<String, List<Article>> recommendArticleMap = new HashMap<String, List<Article>>();
		Map<String, List<Article>> hotArticleMap = new HashMap<String, List<Article>>();
		Map<String, List<Article>> newArticleMap = new HashMap<String, List<Article>>();
		for (ArticleCategory articleCategory : allArticleCategory) {
			articleCategoryMap.put(articleCategory.getStr("id"), articleCategory.getChildrenArticleCategoryList(articleCategory));
			recommendArticleMap.put(articleCategory.getStr("id"), Article.dao.getRecommendArticleList(articleCategory, Article.MAX_RECOMMEND_ARTICLE_LIST_COUNT));
			hotArticleMap.put(articleCategory.getStr("id"), Article.dao.getHotArticleList(articleCategory, Article.MAX_HOT_ARTICLE_LIST_COUNT));
			newArticleMap.put(articleCategory.getStr("id"), Article.dao.getNewArticleList(articleCategory, Article.MAX_NEW_ARTICLE_LIST_COUNT));
		}
		data.put("articleCategoryMap", articleCategoryMap);
		data.put("recommendArticleMap", recommendArticleMap);
		data.put("hotArticleMap", hotArticleMap);
		data.put("newArticleMap", newArticleMap);
		
		String htmlFilePath = htmlConfig.getHtmlFilePath();
		String templateFilePath = htmlConfig.getTemplateFilePath();
		buildHtml(templateFilePath, htmlFilePath, data);
	}
	
	public void loginBuildHtml() {
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.LOGIN);
		Map<String, Object> data = getCommonData();
		String htmlFilePath = htmlConfig.getHtmlFilePath();
		String templateFilePath = htmlConfig.getTemplateFilePath();
		buildHtml(templateFilePath, htmlFilePath, data);
	}
	
	public void articleContentBuildHtml(Article article) {
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.ARTICLE_CONTENT);
		ArticleCategory articleCategory = article.getArticleCategory();
		Map<String, Object> data = getCommonData();
		data.put("article", article);
		data.put("pathList", ArticleCategory.dao.getArticleCategoryPathList(article));
		data.put("rootArticleCategoryList", ArticleCategory.dao.getRootArticleCategoryList());
		data.put("recommendArticleList", Article.dao.getRecommendArticleList(articleCategory, Article.MAX_RECOMMEND_ARTICLE_LIST_COUNT));
		data.put("hotArticleList", Article.dao.getHotArticleList(articleCategory, Article.MAX_HOT_ARTICLE_LIST_COUNT));
		data.put("newArticleList", Article.dao.getNewArticleList(articleCategory, Article.MAX_NEW_ARTICLE_LIST_COUNT));
		String htmlFilePath = article.getStr("htmlFilePath");
		String prefix = StringUtils.substringBeforeLast(htmlFilePath, ".");
		String extension = StringUtils.substringAfterLast(htmlFilePath, ".");
		List<String> pageContentList = article.getPageContentList();
		article.set("pageCount",pageContentList.size());
		article.update();
		//articleDao.flush();
		for (int i = 0; i < pageContentList.size(); i++) {
			data.put("content", pageContentList.get(i));
			data.put("pageNumber", i + 1);
			data.put("pageCount", pageContentList.size());
			String templateFilePath = htmlConfig.getTemplateFilePath();
			String currentHtmlFilePath = null;
			if (i == 0) {
				currentHtmlFilePath = htmlFilePath;
			} else {
				currentHtmlFilePath = prefix + "_" + (i + 1) + "." + extension;
			}
			buildHtml(templateFilePath, currentHtmlFilePath, data);
		}
	}
	
	public void productContentBuildHtml(Product product) {
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.PRODUCT_CONTENT);
		String productCategory_id = product.getStr("productCategory_id");
		Map<String, Object> data = getCommonData();
		data.put("product", product);
		data.put("pathList", ProductCategory.dao.getProductCategoryPathList(product));
		data.put("rootProductCategoryList", ProductCategory.dao.getRootProductCategoryList());
		data.put("bestProductList", Product.dao.getBestProductList(productCategory_id, Product.MAX_BEST_PRODUCT_LIST_COUNT));
		data.put("hotProductList", Product.dao.getHotProductList(productCategory_id, Product.MAX_HOT_PRODUCT_LIST_COUNT));
		data.put("newProductList", Product.dao.getNewProductList(productCategory_id, Product.MAX_NEW_PRODUCT_LIST_COUNT));
		String htmlFilePath = product.getStr("htmlFilePath");;
		String templateFilePath = htmlConfig.getTemplateFilePath();
		buildHtml(templateFilePath, htmlFilePath, data);
	}
	
	public void errorPageBuildHtml() {
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.ERROR_PAGE);
		Map<String, Object> data = getCommonData();
		data.put("errorContent", "系统出现异常，请与管理员联系！");
		String htmlFilePath = htmlConfig.getHtmlFilePath();
		String templateFilePath = htmlConfig.getTemplateFilePath();
		buildHtml(templateFilePath, htmlFilePath, data);
	}
	
	public void errorPageAccessDeniedBuildHtml() {
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.ERROR_PAGE);
		Map<String, Object> data = getCommonData();
		data.put("errorContent", "您无此访问权限！");
		String htmlFilePath = htmlConfig.getHtmlFilePath();
		String templateFilePath = htmlConfig.getTemplateFilePath();
		buildHtml(templateFilePath, htmlFilePath, data);
	}
	
	public void errorPage500BuildHtml() {
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.ERROR_PAGE_500);
		Map<String, Object> data = getCommonData();
		data.put("errorContent", "系统出现异常，请与管理员联系！");
		String htmlFilePath = htmlConfig.getHtmlFilePath();
		String templateFilePath = htmlConfig.getTemplateFilePath();
		buildHtml(templateFilePath, htmlFilePath, data);
	}
	
	public void errorPage404BuildHtml() {
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.ERROR_PAGE_404);
		Map<String, Object> data = getCommonData();
		data.put("errorContent", "您访问的页面不存在！");
		String htmlFilePath = htmlConfig.getHtmlFilePath();
		String templateFilePath = htmlConfig.getTemplateFilePath();
		buildHtml(templateFilePath, htmlFilePath, data);
	}
	
	public void errorPage403BuildHtml() {
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.ERROR_PAGE_403);
		Map<String, Object> data = getCommonData();
		data.put("errorContent", "系统出现异常，请与管理员联系！");
		String htmlFilePath = htmlConfig.getHtmlFilePath();
		String templateFilePath = htmlConfig.getTemplateFilePath();
		buildHtml(templateFilePath, htmlFilePath, data);
	}

}
