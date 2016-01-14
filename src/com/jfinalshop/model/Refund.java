package com.jfinalshop.model;

import java.util.Date;

import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * 实体类 - 退款
 * 
 */
public class Refund extends Model<Refund>{

	private static final long serialVersionUID = -6543627055495001658L;
	
	public static final Refund dao = new Refund();
	
	// 退款类型（预存款支付、在线支付、线下支付）
	public enum RefundType {
		deposit, online, offline
	};

	// 重写save方法
	public boolean save(Refund refund){
		refund.set("id", CommonUtil.getUUID());
		refund.set("createDate", new Date());		
		return refund.save();
	}
		
	public RefundType getRefundType() {
		return RefundType.values()[getInt("refundType")];
	}
	
	// 订单
	public Orders getOrder() {
		return Orders.dao.findById(getStr("order_id"));
	}
	/**
	 * 获取最后生成的退款编号
	 * 
	 * @return 收款编号
	 */
	public String getLastRefundSn() {
		String sql = "select * from Refund  order by createDate desc";
		Refund refundList =  dao.findFirst(sql);
		if (refundList != null) {
			return refundList.getStr("refundSn");
		} else {
			return null;
		}
	}
}
