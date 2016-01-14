package com.jfinalshop.model;

import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * 实体类 - 发货
 * 
 */
public class Shipping extends Model<Shipping>{

	private static final long serialVersionUID = -7846935939597029639L;

	public static final Shipping dao = new Shipping();
	
	// 重写save方法
	public boolean save(Shipping shipping){
		shipping.set("id", CommonUtil.getUUID());
		shipping.set("createDate", new Date());		
		return shipping.save();
	}
		
	// 物流项
	public List<DeliveryItem> getDeliveryItem(){
		String sql = "select * from deliveryitem where shipping_id = ?";
		return DeliveryItem.dao.find(sql,getStr("id"));
	}
	
	// 订单
	public Orders getOrder(){
		return Orders.dao.findById(getStr("order_id"));
	}
	
	/**
	 * 获取最后生成的发货编号
	 * 
	 * @return 发货编号
	 */
	public String getLastShippingSn() {
		String sql = "select * from Shipping  order by createDate desc";
		Shipping shippingList =  dao.findFirst(sql);
		if (shippingList != null) {
			return shippingList.getStr("shippingSn");
		} else {
			return null;
		}
	}
}
