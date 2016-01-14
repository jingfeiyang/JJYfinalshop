package com.jfinalshop.bean;

import com.jfinalshop.bean.SystemConfig.CurrencyType;
/**
 * Bean类 - 财付通配置
 *
 */

public class TenpayConfig {
	
	public static final String PAYMENT_URL = "http://service.tenpay.com/cgi-bin/v3.0/payservice.cgi";// 财付通支付请求URL
	public static final String QUERY_URL = "http://mch.tenpay.com/cgi-bin/cfbi_query_order_v3.cgi";// 财付通查询请求URL
	public static final String RETURN_URL = "/shop/payment/tenpayReturn";// 处理返回结果的URL
	public static final String SHOW_URL = "/shop/payment/result";// 支付完成显示URL
	
	// 财付通交易类型（即时交易、担保交易-实物、担保交易-虚拟）
	public enum TenpayType {
		direct, partnerMaterial, partnerVirtual
	}
	
	// 支持货币种类
	public static final CurrencyType[] currencyType = {CurrencyType.CNY};
	
	private TenpayType tenpayType;// 财会通交易类型
	private String bargainorId;// 商户号
	private String key;// 密钥
	
	public TenpayType getTenpayType() {
		return tenpayType;
	}
	
	public void setTenpayType(String tenpayType) {
		this.tenpayType = TenpayType.valueOf(tenpayType);
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