package com.jfinalshop.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 商品类型
 * 
 */
public class ProductType extends Model<ProductType>{

	private static final long serialVersionUID = 6055737431593176334L;
	
	public static final ProductType dao = new ProductType();

	private List<ProductAttribute> productAttributeList;// 商品属性
	
	// 若新修改的值与原来值相等则直接返回true
	public boolean isUnique(String productTypeName) {
		return dao.findFirst("select name from producttype where name=? limit 1", productTypeName) == null;
	}
	
	// 获取所有商品类型
	public List<ProductType> getAll() {
		return dao.find("select * from producttype order by createDate desc");
	}
	
	// 获取所产品属性
	public List<ProductAttribute> getProductAttributeList(){
		String sql = "select * from productattribute where productType_id = ?";
		return ProductAttribute.dao.find(sql,getStr("id"));
	}
	
	// 获得已启用的商品属性
	public List<ProductAttribute> getEnabledProductAttributeList() {
		productAttributeList = ProductAttribute.dao.getEnabledProductAttributeList(getStr("id"));
		if (productAttributeList == null) {
			return null;
		}		
		return productAttributeList;
	}
}
