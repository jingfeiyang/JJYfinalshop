package com.jfinalshop.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="product_productattributemapstore")
public class ProductAttributeMapStore extends Model<ProductAttributeMapStore>{

	private static final long serialVersionUID = 6847616019701945804L;
	
	public static final ProductAttributeMapStore dao = new ProductAttributeMapStore();

	// 返回产品属性
	public ProductAttribute getProductAttribute(){
		String sql = "select * from productattribute where id = ?";
		return ProductAttribute.dao.findById(sql,getStr("mapkey_id"));
	}
}
