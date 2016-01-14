package com.jfinalshop.model;

import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * Bean类 - 订单项
 */
public class OrderItem extends Model<OrderItem>{

	private static final long serialVersionUID = 8891675666120549939L;
	
	public static final OrderItem dao = new OrderItem();
	
	// 产品
	public Product getProduct(){
		return Product.dao.findById(getStr("product_id"));		
	}
	
	// 产品List
	public List<Product> getProductItemList(){
		String sql = "select * from product where id = ?";
		return Product.dao.find(sql,getStr("product_id"));		
	}
	
	// 订单
	public Orders getOrder() {
		return Orders.dao.findById(getStr("order_id"));
	}
	
	//重写save
	public boolean save(OrderItem orderItem){
		orderItem.set("id", CommonUtil.getUUID());
		orderItem.set("createDate", new Date());
		return orderItem.save();
	}
}
