package com.jfinalshop.model;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

/**
 * 实体类 - 商品属性
 * 
 */

public class ProductAttribute extends Model<ProductAttribute> {

	private static final long serialVersionUID = 2809717181719370225L;

	public static final ProductAttribute dao = new ProductAttribute();
	private List<ProductAttribute> productAttributeList;// 商品属性
	
	public enum AttributeType {
		text, number, alphaint, select, checkbox, date
	}
	
	/**
	 * 按产品分类查询产品属性
	 * 
	 */
	public Page<ProductAttribute> findByPager(String productTypeId) {
		String select = "select * ";
		String sqlExceptSelect = " from productAttribute where productType_id = ? order by createDate desc ";
		
		Page<ProductAttribute> pager = dao.paginate(1, 100, select, sqlExceptSelect,productTypeId);
		return pager;
	}
	
	/**
	 * 根据商品类型、商品名称查找，若不存在则返回null
	 * 
	 * @param productType
	 *            商品类型
	 * 
	 * @param name
	 *            商品属性名称 
	 * 
	 */
	public ProductAttribute getProductAttribute(String productTypeId, String name){
		return dao.findFirst("select * from ProductAttribute t where productType_id = ? and name = ?",productTypeId,name);
	}
		    
    // 商品类型
  	public ProductType getProductType() {
 		return ProductType.dao.findFirst("select * from producttype where id = ?",getStr("productType_id"));
 	}
    
    public AttributeType getAttributeType(){
		return AttributeType.values()[this.getInt("attributeType")];    	
    }
    
	// 获取可选项
 	public List<String> getAttributeOptionList() {
 		String attributeOptionStore = getStr("attributeOptionStore");
 		if (StrKit.isBlank(attributeOptionStore)) {
 			return null;
 		}
 		List<String> list = new ArrayList<String>();
 		JSONArray jsonArray = JSONArray.parseArray((attributeOptionStore));		
 		for(int i = 0; i < jsonArray.size(); i++){
 			list.add(jsonArray.get(i).toString());
 		}
 		return list;
 	}
    	 	 
	/**
	 * 根据商品类型获取已启用的商品属性.
	 * 
	 * @return 已启用的商品属性集合.
	 */
 	public List<ProductAttribute> getEnabledProductAttributeList(String productType) {
		//String sql = "select attributeOptionStore,attributeType,id,isEnabled,isRequired,name,orderList from ProductAttribute  where isEnabled = ? and productType_id = ? order by orderList asc";
 		String sql = "select * from ProductAttribute  where isEnabled = ? and productType_id = ? order by orderList asc";
		productAttributeList =  dao.find(sql,true,productType);
		
		if (productAttributeList == null) {
			return null;
		}
		List<ProductAttribute> enabledProductAttributeList = new ArrayList<ProductAttribute>();
		
		for (ProductAttribute productAttribute : productAttributeList) {
			productAttribute.set("attributeType", ProductAttribute.dao.findById(productAttribute.getStr("id")).getAttributeType().ordinal());
			productAttribute.put("attributeOptionList", productAttribute.getAttributeOptionList());
			enabledProductAttributeList.add(productAttribute);
		}
		return enabledProductAttributeList;
	}
 	
 	
 	
}
