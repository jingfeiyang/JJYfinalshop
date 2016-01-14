package com.jfinalshop.controller.shop;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinalshop.controller.shop.BaseShopController.OrderType;
import com.jfinalshop.interceptor.NavigationInterceptor;
import com.jfinalshop.model.Product;
import com.jfinalshop.model.ProductCategory;

/**
 * 前台类 - 商品
 * 
 */
@ControllerBind(controllerKey = "/shop/product")
@Before(NavigationInterceptor.class)
public class ProductController extends Controller {
	
	private ProductCategory productCategory;
	private String orderType;// 排序类型
	private String viewType;// 查看类型
	
	
	private List<Product> bestProductList;
	private List<Product> hotProductList;
	private List<Product> newProductList;
	private List<ProductCategory> pathList;
	private Page<Product> pager;
	
	// 列表
	public void list() {
		String productCategory_id = getPara("id","");
		orderType = getPara("orderType","");
		viewType = getPara("viewType","");
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",Product.DEFAULT_PRODUCT_LIST_PAGE_SIZE);
		
		if(StrKit.notBlank(productCategory_id)){
			productCategory = ProductCategory.dao.findById(productCategory_id);
		}		
		if (productCategory == null){
			addActionError("未找到内容");
			return;
		}						
		bestProductList = Product.dao.getBestProductList(productCategory_id, Product.MAX_BEST_PRODUCT_LIST_COUNT);
		hotProductList = Product.dao.getHotProductList(productCategory_id, Product.MAX_HOT_PRODUCT_LIST_COUNT);
		newProductList = Product.dao.getNewProductList(productCategory_id, Product.MAX_NEW_PRODUCT_LIST_COUNT);
		pathList = ProductCategory.dao.getProductCategoryPathList(productCategory);
		
		if (StringUtils.equalsIgnoreCase(orderType, "priceAsc")) {
			pager = Product.dao.categorySearch(pageNumber, pageSize,productCategory,"price",OrderType.asc);
		} else if (StringUtils.equalsIgnoreCase(orderType, "priceDesc")) {
			pager = Product.dao.categorySearch(pageNumber, pageSize,productCategory,"price",OrderType.desc);
		} else if (StringUtils.equalsIgnoreCase(orderType, "dateAsc")) {
			pager = Product.dao.categorySearch(pageNumber, pageSize,productCategory,"p.createDate",OrderType.asc);
		} else {
			pager = Product.dao.categorySearch(pageNumber, pageSize,productCategory,"p.createDate",OrderType.desc);
		}
		
		setAttr("pager", pager);
		setAttr("productCategory", productCategory);
		setAttr("rootProductCategoryList", getRootProductCategoryList());
		setAttr("hotProductList", hotProductList);
		setAttr("bestProductList", bestProductList);
		setAttr("newProductList", newProductList);
		setAttr("pathList", pathList);
		
		if (StringUtils.equalsIgnoreCase(viewType, "tableType")) {
			render("/shop/product_table_list.html");
		} else {
			render("/shop/product_picture_list.html");
		}
	}

	// 搜索
	public void search() {
		String keyword = getPara("keyword","");
		orderType = getPara("orderType","");
		viewType = getPara("viewType","");
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",Product.DEFAULT_PRODUCT_LIST_PAGE_SIZE);
				
		bestProductList = Product.dao.getBestProductList(Product.MAX_BEST_PRODUCT_LIST_COUNT);
		hotProductList = Product.dao.getHotProductList(Product.MAX_HOT_PRODUCT_LIST_COUNT);
		newProductList = Product.dao.getNewProductList(Product.MAX_NEW_PRODUCT_LIST_COUNT);
		
		if (StringUtils.equalsIgnoreCase(orderType, "priceAsc")) {			
			pager = Product.dao.search(pageNumber, pageSize,keyword,"price",OrderType.asc);
		} else if (StringUtils.equalsIgnoreCase(orderType, "priceDesc")) {			
			pager = Product.dao.search(pageNumber, pageSize,keyword,"price",OrderType.desc);
		} else if (StringUtils.equalsIgnoreCase(orderType, "dateAsc")) {			
			pager = Product.dao.search(pageNumber, pageSize,keyword,"createDate",OrderType.asc);
		} else {
			pager = Product.dao.search(pageNumber, pageSize,keyword,"createDate",OrderType.desc);
		}
		
		setAttr("pager", pager);
		setAttr("rootProductCategoryList", ProductCategory.dao.getRootProductCategoryList());
		setAttr("hotProductList", hotProductList);
		setAttr("bestProductList", bestProductList);
		setAttr("newProductList", newProductList);
		
		if (StringUtils.equalsIgnoreCase(viewType, "tableType")) {
			render("/shop/product_table_search.html");
		} else {
			render("/shop/product_picture_search.html");
		}
	}
	
	public List<ProductCategory> getRootProductCategoryList() {
		return ProductCategory.dao.getRootProductCategoryList();
	}
	
	public void addActionError(String error){
		setAttr("errorMessages", error);
		render("/shop/error.html");	
	}
	
}
