package com.jfinalshop.controller.admin;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Product;
import com.jfinalshop.model.ProductCategory;
import com.jfinalshop.validator.admin.ProductCategoryValidator;

/**
 * 后台类 - 商品分类
 * 
 */
public class ProductCategoryController extends BaseAdminController<ProductCategory> {

	private ProductCategory productCategory;	
	
	// 添加
	public void add() {
		setAttr("productCategoryTreeList", ProductCategory.dao.getAll());
		render("/admin/product_category_input.html");
	}

	// 编辑
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("productCategory", ProductCategory.dao.findById(id));
		}
		setAttr("productCategoryTreeList", ProductCategory.dao.getAll());
		render("/admin/product_category_input.html");
	}

	// 列表
	public void list() {
		setAttr("productCategoryTreeList", ProductCategory.dao.getAll());
		render("/admin/product_category_list.html");
	}
		
	// 保存
	@Before(ProductCategoryValidator.class)
	public void save(){
		productCategory = getModel(ProductCategory.class);
		String parentId = getPara("parentId",null);		
		productCategory.set("parent_id", parentId);
		saved(productCategory);
		if (StringUtils.isNotEmpty(parentId)) {
			ProductCategory parent = ProductCategory.dao.findById(parentId);
			if (parent != null) {
				String parentPath = parent.getStr("path");
				productCategory.set("path",parentPath + ProductCategory.PATH_SEPARATOR + productCategory.getStr("id"));
			}			
		} else {
			productCategory.set("path",productCategory.getStr("id"));
		}
		updated(productCategory);
		redirect("/productCategory/list");	
	}
	
	// 更新
	public void update(){
		productCategory = getModel(ProductCategory.class);
		updated(productCategory);
		redirect("/productCategory/list");	
	}
	
	// 删除
	public void delete(){		
		String id = getPara("id","");		
		if (StrKit.notBlank(id)){			
			ProductCategory productCategory = ProductCategory.dao.findById(id);			
			// 是否存在下级
			List<ProductCategory> childrenProductCategoryList = productCategory.getChildren();
			if (childrenProductCategoryList != null && childrenProductCategoryList.size() > 0) {
				ajaxJsonErrorMessage("此商品分类存在下级分类，删除失败!");
				return;
			}			
			// 是否存在商品
			List<Product> productList = productCategory.getProductList();
			if (productList != null && productList.size() > 0) {
				ajaxJsonErrorMessage("此商品分类下存在商品，删除失败!");
				return;
			}			
			productCategory.delete();
			ajaxJsonSuccessMessage("删除成功！");
		}
	}
	
}
