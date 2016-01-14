package com.jfinalshop.ext.beetl;

import org.beetl.core.Context;
import org.beetl.core.Function;

import com.jfinalshop.model.DeliveryType;

public class DeliveryFee implements Function{

	/**
	 * 根据总重量计算配送费用（重量单位：g）
	 * 
	 * @return 配送费用
	 */
	@Override
	public Object call(Object[] obj, Context ctx) {
		if (obj[0] == null || obj[1] == null){
			return null;
		}
		DeliveryType deliveryType = DeliveryType.dao.findById(obj[1]);
		return deliveryType.getDeliveryFee(Double.parseDouble(obj[0].toString()));
	}

}
