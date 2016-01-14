package com.jfinalshop.model;

import java.util.Date;

import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * 实体类 - 订单日志
 * 
 */

public class OrderLog extends Model<OrderLog>{

	private static final long serialVersionUID = 573118204870766554L;
	
	public static final OrderLog dao = new OrderLog();
	
	// 订单日志类型（订单创建、订单修改、订单支付、订单退款、订单发货、订单退货、订单完成、订单作废）
	public enum OrderLogType {
		create, modify, payment, refund, shipping, reship, completed, invalid
	};

	// 重写save方法
	public boolean save(OrderLog orderLog){
		orderLog.set("id", CommonUtil.getUUID());
		orderLog.set("createDate", new Date());		
		return orderLog.save();
	}
	
	// 订单日志类型
	public OrderLogType getOrderLogType() {
		return OrderLogType.values()[getInt("orderLogType")];
	}
}
