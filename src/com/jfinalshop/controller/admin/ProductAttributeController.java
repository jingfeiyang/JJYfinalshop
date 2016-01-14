package com.jfinalshop.controller.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinalshop.model.ProductAttribute;
import com.jfinalshop.model.ProductAttribute.AttributeType;
import com.jfinalshop.model.ProductType;
import com.jfinalshop.validator.admin.ProductAttributeValidator;

/**
 * 后台类 - 商品属性
 * 
 */
public class ProductAttributeController extends BaseAdminController<ProductAttribute>{
	
	private Page<ProductAttribute> pager;
	private ProductAttribute productAttribute;
	private String[] attributeOptionList;
	
	// 列表
	public void list() {
		String productTypeId = getPara("productTypeId", "");
		
		if (StringUtils.isNotEmpty(productTypeId)) {
			pager = ProductAttribute.dao.findByPager(productTypeId);
			setAttr("pager", pager);
		} else {			
			findByPage();
		}
		render("/admin/product_attribute_list.html");
	}
		
	// 添加
	public void add() {
		setAttr("allProductType", ProductType.dao.getAll());
		setAttr("allAttributeType", getAllAttributeType());		
		render("/admin/product_attribute_input.html");
	}

	// 编辑
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("productAttribute", ProductAttribute.dao.findById(id));
		}
		setAttr("allProductType", ProductType.dao.getAll());
		setAttr("allAttributeType", getAllAttributeType());		
		render("/admin/product_attribute_input.html");
	}
		
	// 根据productTypeId获取已启用的商品属性JSON数据
	public void ajaxProductAttribute() {
		String productTypeId = getPara("productTypeId", "");
		if (StrKit.notBlank(productTypeId)) {
			ProductType productType = ProductType.dao.findById(productTypeId);
			List<ProductAttribute> enabledProductAttributeList = productType.getEnabledProductAttributeList();
			renderJson(enabledProductAttributeList);
		} else {
			renderJson("");
		}
	}
		
	// 添加
	@Before(ProductAttributeValidator.class)
	public void save(){
		productAttribute = getModel(ProductAttribute.class);
		
		ProductAttribute pa = ProductAttribute.dao.getProductAttribute(productAttribute.getStr("productType_id"), productAttribute.getStr("name"));
		if (pa != null) {
			addActionError("商品属性名称在此商品分类中已存在!");
			return;
		}		
		// 属性类型
		AttributeType attributeType  = AttributeType.valueOf(getPara("attributeType"));	
		
		// 可选项储存
		attributeOptionList = getParaValues("attributeOptionList");			
		if (attributeType == AttributeType.select || attributeType == AttributeType.checkbox) {
			if(attributeOptionList != null && attributeOptionList.length > 0) {	
				List<String> list = new ArrayList<String>();
				for (String optionList : attributeOptionList) {
					list.add(optionList);
				}
				productAttribute.set("attributeOptionStore", JsonKit.toJson(list, 1));
			}
		} 
		productAttribute.set("attributeType", attributeType.ordinal());		
		saved(productAttribute);
		redirect("/productAttribute/edit");		
	}
	
	// 编辑
	public void update() {
		productAttribute = getModel(ProductAttribute.class);
		// 属性类型
		String at = getPara("attributeType","");
		int attributeType = 0;
		if (StrKit.notBlank()){
			attributeType = AttributeType.valueOf(at).ordinal();
		}
		
		attributeOptionList = getParaValues("attributeOptionList");			
		if (attributeType == AttributeType.select.ordinal() || attributeType == AttributeType.checkbox.ordinal()) {
			if(attributeOptionList != null && attributeOptionList.length > 0) {	
				List<String> list = new ArrayList<String>();
				for (String optionList : attributeOptionList) {
					list.add(optionList);
				}
				productAttribute.set("attributeOptionStore", JsonKit.toJson(list, 1));
			}
		}
		productAttribute.set("attributeType", attributeType);
		updated(productAttribute);
		redirect("/productAttribute/list");		
	}
	
	// 删除
	public void delete() {
		ids = getParaValues("ids");
		if (ids != null && ids.length > 0) {
			for (String id : ids) {
				if(ProductAttribute.dao.deleteById(id)){	
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		} else {
			ajaxJsonErrorMessage("id为空未选中，删除失败！");
		}	
	}
	
	// 获取所有商品属性类型
	public List<AttributeType> getAllAttributeType() {
		List<AttributeType> allAttributeType = new ArrayList<AttributeType>();
		for (AttributeType attributeType : AttributeType.values()) {
			allAttributeType.add(attributeType);
		}
		return allAttributeType;
	}
}
