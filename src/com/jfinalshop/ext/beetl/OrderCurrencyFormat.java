package com.jfinalshop.ext.beetl;

import java.text.DecimalFormat;

import org.beetl.core.Format;

import com.jfinalshop.util.SystemConfigUtil;

public class OrderCurrencyFormat implements Format {

	/**
	 * 获取订单价格货币格式字符串
	 * 
	 */
	@Override
	public Object format(Object data, String pattern) {
		if (data == null) {
			return null;
		}
		DecimalFormat df = null;
		pattern = SystemConfigUtil.getOrderCurrencyFormat();
		if (pattern == null) {
			df = new DecimalFormat();
		} else {
			df = new DecimalFormat(pattern);
		}
		return df.format(data);
	}

}
