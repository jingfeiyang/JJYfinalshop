package com.jfinalshop.model;


import java.util.Date;

import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * 实体类 - 支付
 * 
 */
public class Payment extends Model<Payment>{

	private static final long serialVersionUID = -3979725664238105210L;
	
	public static final Payment dao = new Payment();

	// 支付类型（在线充值、预存款支付、在线支付、线下支付）
	public enum PaymentType {
		recharge, deposit, online, offline
	};
	
	// 支付状态（准备、超时、作废、成功、失败）
	public enum PaymentStatus {
		ready, timeout, invalid, success, failure
	};
	
	// 重写save方法
	public boolean save(Payment payment){
		payment.set("id", CommonUtil.getUUID());
		payment.set("createDate", new Date());		
		return payment.save();
	}
		
	public PaymentType getPaymentType() {
		return PaymentType.values()[getInt("paymentType")];
	}
	
	public PaymentStatus getPaymentStatus() {
		return PaymentStatus.values()[getInt("paymentStatus")];
	}
	
	// 订单
	public Orders getOrder() {
		return Orders.dao.findById(getStr("order_id"));
	}
	
	// 支付配置
	public PaymentConfig getPaymentConfig() {
		return PaymentConfig.dao.findById(getStr("paymentConfig_id"));
	}
	
	/**
	 * 获取最后生成的支付编号
	 * 
	 * @return 支付编号
	 */
	public String getLastPaymentSn() {
		String sql = "select * from Payment order by createDate desc";
		Payment paymentList =  dao.findFirst(sql);
		if (paymentList != null) {
			return paymentList.getStr("paymentSn");
		} else {
			return null;
		}
	}
	
	/**
	 * 根据支付编号获取对象（若对象不存在，则返回null）
	 * 
	 * @return 支付对象
	 */
	public Payment getPaymentByPaymentSn(String paymentSn) {
		String sql = "select * from Payment  where paymentSn = ?";
		return dao.findFirst(sql,paymentSn);
	}
}
