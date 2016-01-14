package com.jfinalshop.controller.admin;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.ProductAttribute;
import com.jfinalshop.model.ProductType;
import com.jfinalshop.validator.admin.ProductTypeValidator;

/**
 * 后台类 - 商品类型
 * 
 */
public class ProductTypeController extends BaseAdminController<ProductType> {
	
	private ProductType productType;
	
	// 添加
	public void add() {
		render("/admin/product_type_input.html");
	}

	// 编辑
	public void edit() {
		String id = getPara("id","");
		if (StrKit.notBlank(id)){
			setAttr("productType", ProductType.dao.findById(id));
		}
		render("/admin/product_type_input.html");
	}

	// 列表
	public void list() {
		findByPage();
		render("/admin/product_type_list.html");
	}

	// 添加
	@Before(ProductTypeValidator.class)
	public void save(){
		productType = getModel(ProductType.class);		
		saved(productType);
		redirect("/productType/list");
	}
	
	// 编辑
	@Before(ProductTypeValidator.class)
	public void update(){		
		productType = getModel(ProductType.class);
		updated(productType);
		redirect("/productType/list");
	}
	
	// 删除
	public void delete() {
		String[] ids = getParaValues("ids");
		if (ids != null && ids.length > 0) {
			for (String id : ids) {
				// 是否存在商品属性
				List<ProductAttribute> productAttribute = ProductType.dao.findById(id).getProductAttributeList();
				if (productAttribute != null && productAttribute.size() > 0) {
					ajaxJsonErrorMessage("此商品类型下存在商品属性，删除失败！");
					return;
				}	
				if(ProductType.dao.deleteById(id)){	
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			} 
		} else {
			ajaxJsonErrorMessage("id为空未选中，删除失败！");
		}
		
	}
	
}
