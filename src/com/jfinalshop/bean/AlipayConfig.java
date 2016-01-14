package com.jfinalshop.bean;

import com.jfinalshop.bean.SystemConfig.CurrencyType;

/**
 * Bean类 - 支付宝配置
 *
 */
public class AlipayConfig {

	// 支付宝交易类型（即时交易、担保交易）
	public enum AlipayType {
		direct, partner
	}
	
	// 支持货币种类
	public static final CurrencyType[] currencyType = {CurrencyType.CNY};
	
	private AlipayType alipayType;// 支付宝交易类型
	private String bargainorId;// 商户号
	private String key;// 密钥
	
	public AlipayType getAlipayType() {
		return alipayType;
	}
	public void setAlipayType(String alipayType) {
		this.alipayType = AlipayType.valueOf(alipayType);
	}
	public String getBargainorId() {
		return bargainorId;
	}
	public void setBargainorId(String bargainorId) {
		this.bargainorId = bargainorId;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
		
}
