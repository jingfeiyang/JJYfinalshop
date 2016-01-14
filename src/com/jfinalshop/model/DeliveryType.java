package com.jfinalshop.model;

import java.math.BigDecimal;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.model.Product.WeightUnit;
import com.jfinalshop.util.ArithUtil;
import com.jfinalshop.util.SystemConfigUtil;

/**
 * 实体类 - 配送方式 
 * 
 */
public class DeliveryType extends Model<DeliveryType> {

	private static final long serialVersionUID = -2644719338741132970L;

	public static final DeliveryType dao = new DeliveryType();

	// 配送类型：先付款后发货、货到付款
	public enum DeliveryMethod {
		deliveryAgainstPayment, cashOnDelivery
	};
	
	/**
	 * 获取所有实体对象总数.
	 * 
	 * @return 实体对象总数
	 */
	public Long getTotalCount() {
		return Db.queryLong("select count(*) from deliverytype");
	}

	/**
	 * 根据重量、重量单位转换为单位为g的重量值
	 * 
	 * @return 重量值（单位：g）
	 */
	public static double toWeightGram(double weight, WeightUnit weightUnit) {
		double weightGram = 0D;// 重量（单位：g）
		if (weightUnit == WeightUnit.g) {
			weightGram = weight;
		} else if (weightUnit == WeightUnit.kg) {
			weightGram = ArithUtil.mul(weight, 1000);
		} else {
			weightGram = ArithUtil.mul(weight, 1000000);
		}
		return weightGram;
	}

	/**
	 * 根据总重量、重量单位计算配送费用
	 * 
	 * @return 配送费用
	 */
	public BigDecimal getDeliveryFee(double totalWeight, WeightUnit totalWeightUnit) {
		double totalWeightGram = toWeightGram(totalWeight, totalWeightUnit);// 首重量（单位：g）
		double firstWeightGram = toWeightGram(getDouble("firstWeight"), getFirstWeightUnit());// 首重量（单位：g）
		double contiuneWeightGram = toWeightGram(getDouble("continueWeight"), getContinueWeightUnit());// 续重量（单位：g）
		BigDecimal deliveryFee = new BigDecimal("0");// 配送费用
		if (totalWeightGram <= firstWeightGram) {
			deliveryFee = getBigDecimal("firstWeightPrice");
		} else {
			Double contiuneWeightCount = Math.ceil(ArithUtil.div(ArithUtil.sub(totalWeightGram, firstWeightGram), contiuneWeightGram));
			deliveryFee = getBigDecimal("firstWeightPrice").add(getBigDecimal("continueWeightPrice").multiply(new BigDecimal(contiuneWeightCount.toString())));
		}
		return SystemConfigUtil.getOrderScaleBigDecimal(deliveryFee);
	}

	/**
	 * 根据总重量计算配送费用（重量单位：g）
	 * 
	 * @return 配送费用
	 */
	public BigDecimal getDeliveryFee(double totalWeight) {
		return getDeliveryFee(totalWeight, WeightUnit.g);
	}

	// 获取所有物流公司
	public List<DeliveryCorp> getAllDeliveryCorp() {
		return DeliveryCorp.dao.getAll();
	}

	// 获取所有配送方式
	public List<DeliveryType> getAll() {
		return dao.find("select * from deliverytype");
	}

	public DeliveryMethod getDeliveryMethod() {
		return DeliveryMethod.values()[getInt("deliveryMethod")];
	}

	public void setDeliveryMethod(String deliveryMethod) {
		set("deliveryMethod", DeliveryMethod.valueOf(deliveryMethod).ordinal());
	}

	public WeightUnit getFirstWeightUnit() {
		return WeightUnit.values()[getInt("firstWeightUnit")];
	}

	public void setFirstWeightUnit(String firstWeightUnit) {
		set("firstWeightUnit", WeightUnit.valueOf(firstWeightUnit).ordinal());
	}

	public WeightUnit getContinueWeightUnit() {
		return WeightUnit.values()[getInt("continueWeightUnit")];
	}

	public void setContinueWeightUnit(String continueWeightUnit) {
		set("continueWeightUnit", WeightUnit.valueOf(continueWeightUnit).ordinal());
	}

}
