package com.jfinalshop.controller.admin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinalshop.bean.AlipayConfig;
import com.jfinalshop.bean.AlipayConfig.AlipayType;
import com.jfinalshop.bean.TenpayConfig;
import com.jfinalshop.bean.TenpayConfig.TenpayType;
import com.jfinalshop.model.PaymentConfig;
import com.jfinalshop.model.PaymentConfig.PaymentConfigType;
import com.jfinalshop.model.PaymentConfig.PaymentFeeType;
import com.jfinalshop.validator.admin.PaymentConfigValidator;

/**
 * 后台类 - 支付方式
 * 
 */
public class PaymentConfigController extends BaseAdminController<PaymentConfig>{
	
	private PaymentConfig paymentConfig;
	private TenpayConfig tenpayConfig;
	private AlipayConfig alipayConfig;
	
	// 列表
	public void list() {
		findByPage();
		render("/admin/payment_config_list.html");
	}

	// 是否已存在 ajax验证
	public void checkName() {
		String value = getPara("paymentConfig.name","");
		if (isUnique("paymentconfig", "name", value)) {
			renderText("true");
		} else {
			renderText("false");
		}
	}
		
	// 添加
	public void add() {
		setAttr("allPaymentConfigType", getAllPaymentConfigType());
		setAttr("allPaymentFeeType", getAllPaymentFeeType());
		setAttr("allTenpayType", getAllTenpayType());
		setAttr("allAlipayType", getAllAlipayType());
		render("/admin/payment_config_input.html");
	}
	
	// 编辑
	public void edit() {
		String id = getPara("id","");
		if (StrKit.notBlank(id)){
			paymentConfig = PaymentConfig.dao.findById(id);
			setAttr("paymentConfig", paymentConfig);
			if (paymentConfig.getPaymentConfigType() == PaymentConfigType.tenpay) {
				setAttr("tenpayConfig", paymentConfig.getConfigObject());
			}else if(paymentConfig.getPaymentConfigType() == PaymentConfigType.alipay) {
				setAttr("alipayConfig", paymentConfig.getConfigObject());
			}
		}		
		setAttr("allPaymentConfigType", getAllPaymentConfigType());
		setAttr("allPaymentFeeType", getAllPaymentFeeType());
		setAttr("allTenpayType", getAllTenpayType());
		setAttr("allAlipayType", getAllAlipayType());
		render("/admin/payment_config_input.html");
	}
		
	
	// 保存
	@Before(PaymentConfigValidator.class)
	public void save(){
		paymentConfig = getModel(PaymentConfig.class);
		tenpayConfig = getModel(TenpayConfig.class);	
		alipayConfig = getModel(AlipayConfig.class);
		String alipayType = getPara("alipayType","");
		String tenpayType = getPara("tenpayType","");
		
		if (StrKit.notBlank(tenpayType)){
			tenpayConfig.setTenpayType(tenpayType);
		}
		if (StrKit.notBlank(alipayType)){
			alipayConfig.setAlipayType(alipayType);
		}	
		
		String paymentFeeType = getPara("paymentFeeType","");
		if (StrKit.notBlank(paymentFeeType)){
			paymentConfig.setPaymentFeeType(paymentFeeType);
		}		
		String paymentConfigType = getPara("paymentConfigType","");
		if (StrKit.notBlank(paymentConfigType)){
			paymentConfig.setPaymentConfigType(paymentConfigType);
		}
		if (paymentConfig.getBigDecimal("paymentFee").compareTo(new BigDecimal("0")) < 0) {
			addActionError("支付手续费金额不允许小于0！");
			return;
		}		
		if (paymentConfig.getPaymentConfigType() == PaymentConfigType.tenpay) {
			if (tenpayConfig == null) {
				addActionError("财付通配置不允许为空！");
				return;
			}
			if (tenpayConfig.getTenpayType() == null) {
				addActionError("财付通交易类型不允许为空！");
				return;
			}
			if (StringUtils.isEmpty(tenpayConfig.getBargainorId())) {
				addActionError("财付通商户号不允许为空！");
				return;
			}
			if (StringUtils.isEmpty(tenpayConfig.getKey())) {
				addActionError("财付通密钥不允许为空！");
				return;
			}
			paymentConfig.setConfigObject(tenpayConfig);
		} else if (paymentConfig.getPaymentConfigType() == PaymentConfigType.alipay) {
			if (alipayConfig == null) {
				addActionError("支付宝配置不允许为空！");
				return;
			}
			if (alipayConfig.getAlipayType() == null) {
				addActionError("支付宝交易类型不允许为空！");
				return;
			}
			if (StringUtils.isEmpty(alipayConfig.getBargainorId())) {
				addActionError("支付宝商户号不允许为空！");
				return;
			}
			if (StringUtils.isEmpty(alipayConfig.getKey())) {
				addActionError("支付宝密钥不允许为空！");
				return;
			}
			paymentConfig.setConfigObject(alipayConfig);
		}
		saved(paymentConfig);
		redirect("/paymentConfig/list");
	}
	
	// 更新
	@Before(PaymentConfigValidator.class)
	public void update(){
		paymentConfig = getModel(PaymentConfig.class);
		tenpayConfig = getModel(TenpayConfig.class);	
		
		String paymentFeeType = getPara("paymentFeeType","");
		if (StrKit.notBlank(paymentFeeType)){
			paymentConfig.setPaymentFeeType(paymentFeeType);
		}
		
		String paymentConfigType = getPara("paymentConfigType","");
		if (StrKit.notBlank(paymentConfigType)){
			paymentConfig.setPaymentConfigType(paymentConfigType);
		}
		
		String tenpayType = getPara("tenpayType","");
		if (StrKit.notBlank(tenpayType)){
			tenpayConfig.setTenpayType(tenpayType);
		}		
		
		if (paymentConfig.getBigDecimal("paymentFee").compareTo(new BigDecimal("0")) < 0) {
			addActionError("支付手续费金额不允许小于0！");
			return;
		}
		
		if (paymentConfig.getPaymentConfigType() == PaymentConfigType.tenpay) {
			if (tenpayConfig == null) {
				addActionError("财付通配置不允许为空！");
				return;
			}
			if (tenpayConfig.getTenpayType() == null) {
				addActionError("财付通交易类型不允许为空！");
				return;
			}
			if (StringUtils.isEmpty(tenpayConfig.getBargainorId())) {
				addActionError("财付通商户号不允许为空！");
				return;
			}
			if (StringUtils.isEmpty(tenpayConfig.getKey())) {
				addActionError("财付通密钥不允许为空！");
				return;
			}
			paymentConfig.setConfigObject(tenpayConfig);
		}
		updated(paymentConfig);
		redirect("/paymentConfig/list");
	}
	
	// 删除
	public void delete(){
		ids = getParaValues("ids");
		Long totalCount = PaymentConfig.dao.getTotalCount();
		if (ids.length >= totalCount) {
			ajaxJsonErrorMessage("删除失败，必须保留至少一个支付方式！");
		}else{
			for (String id : ids) {
				if(PaymentConfig.dao.deleteById(id)){	
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}			
		}		
		
	}
	
	// 获取所有支付配置类型
	public List<PaymentConfigType> getAllPaymentConfigType() {
		List<PaymentConfigType> allPaymentConfigType = new ArrayList<PaymentConfigType>();
		for (PaymentConfigType paymentConfigType : PaymentConfigType.values()) {
			allPaymentConfigType.add(paymentConfigType);
		}
		return allPaymentConfigType;
	}
	
	// 获取所有支付手续费类型
	public List<PaymentFeeType> getAllPaymentFeeType() {
		List<PaymentFeeType> allPaymentFeeType = new ArrayList<PaymentFeeType>();
		for (PaymentFeeType paymentFeeType : PaymentFeeType.values()) {
			allPaymentFeeType.add(paymentFeeType);
		}
		return allPaymentFeeType;
	}
	
	// 获取所有财付通交易类型
	public List<TenpayType> getAllTenpayType() {
		List<TenpayType> allTenpayType = new ArrayList<TenpayType>();
		for (TenpayType tenpayType : TenpayType.values()) {
			allTenpayType.add(tenpayType);
		}
		return allTenpayType;
	}
	
	// 获取所有支付宝交易类型
	public List<AlipayType> getAllAlipayType() {
		List<AlipayType> allAlipayType = new ArrayList<AlipayType>();
		for (AlipayType alipayType : AlipayType.values()) {
			allAlipayType.add(alipayType);
		}
		return allAlipayType;
	}
	
}
