package com.jfinalshop.controller.shop;

import java.util.List;

import com.jfinalshop.model.Deposit;
import com.jfinalshop.model.PaymentConfig;

/**
 * 前台类 - 预存款
 * 
 */
public class DepositController extends BaseShopController<Deposit>{

	// 预存款列表
	public void list() {
		findByPage();
		render("/shop/deposit_list.html");
	}
	
	// 预存款充值
	public void recharge() {
		setAttr("nonDepositOfflinePaymentConfigList", getNonDepositOfflinePaymentConfigList());
		render("/shop/deposit_recharge.html");
	}
	
	// 获取支付配置（不包含预存款、线下支付方式）
	public List<PaymentConfig> getNonDepositOfflinePaymentConfigList() {
		return PaymentConfig.dao.getNonDepositOfflinePaymentConfigList();
	}
}
