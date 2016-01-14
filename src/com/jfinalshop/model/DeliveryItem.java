package com.jfinalshop.model;

import java.util.Date;

import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * Bean类 - 物流项
 * 
 */
public class DeliveryItem extends Model<DeliveryItem>{

	private static final long serialVersionUID = 9117150671194966106L;

	public static final DeliveryItem dao = new DeliveryItem();
	
	// 重写save方法
	public boolean save(DeliveryItem deliveryItem){
		deliveryItem.set("id", CommonUtil.getUUID());
		deliveryItem.set("createDate", new Date());		
		return deliveryItem.save();
	}
}
