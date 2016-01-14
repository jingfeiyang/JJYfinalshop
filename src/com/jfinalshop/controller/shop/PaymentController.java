package com.jfinalshop.controller.shop;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinalshop.bean.SystemConfig.StoreFreezeTime;
import com.jfinalshop.bean.TenpayConfig;
import com.jfinalshop.bean.TenpayConfig.TenpayType;
import com.jfinalshop.model.Deposit;
import com.jfinalshop.model.Deposit.DepositType;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.OrderItem;
import com.jfinalshop.model.OrderLog;
import com.jfinalshop.model.OrderLog.OrderLogType;
import com.jfinalshop.model.Orders;
import com.jfinalshop.model.Orders.OrderStatus;
import com.jfinalshop.model.Payment;
import com.jfinalshop.model.Payment.PaymentStatus;
import com.jfinalshop.model.Payment.PaymentType;
import com.jfinalshop.model.PaymentConfig;
import com.jfinalshop.model.PaymentConfig.PaymentConfigType;
import com.jfinalshop.model.Product;
import com.jfinalshop.service.HtmlService;
import com.jfinalshop.util.SerialNumberUtil;

/**
 * 前台类 - 支付处理
 * 
 */
@ControllerBind(controllerKey = "/shop/payment")
public class PaymentController extends BaseShopController<Payment>{

	// 支付结果（成功、失败）
	public enum PaymentResult {
		success, failure
	}
	
	private PaymentType paymentType;// 支付类型
	private BigDecimal amountPayable;// 应付金额（不含支付费用）
	private BigDecimal paymentFee;// 支付手续费
	private PaymentResult paymentResult;// 支付结果
	private PaymentConfig paymentConfig;// 支付方式
	private Orders order;// 订单
	
	// 支付确认
	public void confirm() {
		amountPayable = new BigDecimal(getPara("amountPayable","0"));
		paymentType = PaymentType.valueOf(getPara("paymentType"));
		String paymentConfigId = getPara("paymentConfig.id","");
		String orderId = getPara("order.id");
		if (paymentType == PaymentType.recharge) {
			if (amountPayable == null) {
				addActionError("请输入充值金额！");
				return;
			}
			if (amountPayable.compareTo(new BigDecimal("0")) <= 0) {
				addActionError("充值金额必须大于0！");
				return;
			}
			if (amountPayable.scale() > getSystemConfig().getOrderScale()) {
				addActionError("充值金额小数位超出限制！");
				return;
			}
			if (StringUtils.isEmpty(paymentConfigId)) {
				addActionError("请选择支付方式！");
				return;
			}
			if (StrKit.notBlank(paymentConfigId)){
				paymentConfig = PaymentConfig.dao.findById(paymentConfigId);
			}			
			paymentFee = paymentConfig.getPaymentFee(amountPayable);
		} else {
			if (StringUtils.isEmpty(orderId)) {
				addActionError("订单信息错误！");
				return;
			}
			order = Orders.dao.findById(orderId);
			paymentConfig = order.getPaymentConfig();
			paymentFee = order.getBigDecimal("paymentFee");
			amountPayable = order.getBigDecimal("totalAmount").subtract(paymentFee).subtract(order.getBigDecimal("paidAmount"));
		}
		setAttr("order", order);
		setAttr("paymentType", paymentType);
		setAttr("amountPayable", amountPayable);
		setAttr("paymentFee", paymentFee);
		setAttr("paymentConfig", paymentConfig);
		render("/shop/payment_confirm.html");
	}
	
	// 支付入口
	@Before(Tx.class)
	public void gateway() {
		String orderId = getPara("order.id","");
		String paymentConfigId = getPara("paymentConfig.id","");
		amountPayable = new BigDecimal(getPara("amountPayable","0"));
		String pt = getPara("paymentType","");
		
		if (StrKit.notBlank(pt)){
			paymentType = PaymentType.valueOf(pt);
		}	
		if (paymentType == PaymentType.recharge) {
			if (amountPayable.compareTo(new BigDecimal("0")) <= 0) {
				addActionError("充值金额必须大于0！");
				return ;
			}
			if (amountPayable.scale() > getSystemConfig().getOrderScale()) {
				addActionError("充值金额小数位超出限制！");
				return ;
			}
			if (StringUtils.isEmpty(paymentConfigId)) {
				addActionError("请选择支付方式！");
				return ;
			}
			paymentConfig = PaymentConfig.dao.findById(paymentConfigId);
			if (paymentConfig.getPaymentConfigType() == PaymentConfigType.deposit || paymentConfig.getPaymentConfigType() == PaymentConfigType.offline) {
				addActionError("支付方式错误！");
				return;
			}
			// 根据总金额计算支付费用
			paymentFee = paymentConfig.getPaymentFee(amountPayable);
		} else if (paymentType == PaymentType.deposit) {
			if (StringUtils.isEmpty(orderId)) {
				addActionError("订单信息错误！");
				return;
			}
			order = Orders.dao.findById(orderId);
			paymentConfig = order.getPaymentConfig();
			if (paymentConfig.getPaymentConfigType() != PaymentConfigType.deposit) {
				addActionError("支付方式错误！");
				return;
			}
			if (order.getOrderStatus() == OrderStatus.completed || order.getOrderStatus() == OrderStatus.invalid) {
				addActionError("订单状态错误！");
				return;
			}
			if (order.getPaymentStatus() == Orders.PaymentStatus.paid) {
				addActionError("订单付款状态错误！");
				return;
			}
			if (getLoginMember().getBigDecimal("deposit").compareTo(order.getBigDecimal("totalAmount").subtract(order.getBigDecimal("paidAmount"))) < 0) {
				paymentResult = PaymentResult.failure;				
				render("/shop/payment_deposit_result.html");
			}
			paymentFee = order.getBigDecimal("paymentFee");
			amountPayable = order.getBigDecimal("totalAmount").subtract(paymentFee).subtract(order.getBigDecimal("paidAmount"));
		} else if (paymentType == PaymentType.offline) {
			if (StringUtils.isEmpty(orderId)) {
				addActionError("订单信息错误！");
				return;
			}
			order = Orders.dao.findById(orderId);
			if (order.getOrderStatus() == OrderStatus.completed || order.getOrderStatus() == OrderStatus.invalid) {
				addActionError("订单状态错误！");
				return;
			}
			if (order.getPaymentStatus() == Orders.PaymentStatus.paid) {
				addActionError("订单付款状态错误！");
				return;
			}
			paymentConfig = order.getPaymentConfig();
			if (paymentConfig.getPaymentConfigType() != PaymentConfigType.offline) {
				addActionError("支付方式错误！");
				return;
			}
			paymentFee = order.getBigDecimal("paymentFee");
			amountPayable = order.getBigDecimal("productTotalPrice").add(order.getBigDecimal("deliveryFee")).subtract(order.getBigDecimal("paidAmount"));
		} else if (paymentType == PaymentType.online) {
			if (StringUtils.isEmpty(orderId)) {
				addActionError("订单信息错误！");
				return;
			}
			order = Orders.dao.findById(orderId);
			paymentConfig = order.getPaymentConfig();
			if (paymentConfig.getPaymentConfigType() == PaymentConfigType.deposit || paymentConfig.getPaymentConfigType() == PaymentConfigType.offline) {
				addActionError("支付方式错误！");
				return;
			}
			paymentFee = order.getBigDecimal("paymentFee");
			amountPayable = order.getBigDecimal("totalAmount").subtract(paymentFee).subtract(order.getBigDecimal("paidAmount"));
		}
		BigDecimal totalAmount = amountPayable.add(paymentFee);// 总金额
		String description = null;// 在线支付交易描述
		String paymentUrl = null;// 在线支付跳转URL
		if (paymentType == PaymentType.recharge) {
			description = getSystemConfig().getShopName() + "预存款充值";
		} else {
			description = getSystemConfig().getShopName() + "订单支付（" + order.getStr("orderSn") + "）";
		}
		Member loginMember = getLoginMember();
		if (paymentConfig.getPaymentConfigType() == PaymentConfigType.deposit) {
			if (totalAmount.compareTo(order.getBigDecimal("totalAmount").subtract(order.getBigDecimal("paidAmount"))) == 0) {
				order.set("paymentStatus",Orders.PaymentStatus.paid.ordinal());
				order.set("paidAmount",order.getBigDecimal("paidAmount").add(totalAmount));
			} else if (totalAmount.compareTo(order.getBigDecimal("totalAmount")) < 0) {
				order.set("paymentStatus",Orders.PaymentStatus.partPayment.ordinal());
				order.set("paidAmount",order.getBigDecimal("paidAmount").add(totalAmount));
			} else {
				addActionError("交易金额错误！");
				return;
			}
			order.update();
			
			loginMember.set("deposit",loginMember.getBigDecimal("deposit").subtract(totalAmount));
			loginMember.update();
			
			Deposit deposit = new Deposit();
			deposit.set("depositType",DepositType.memberPayment.ordinal());
			deposit.set("credit",new BigDecimal("0"));
			deposit.set("debit",amountPayable);
			deposit.set("balance",loginMember.getBigDecimal("deposit"));
			deposit.set("member_id",loginMember.getStr("id"));
			deposit.save(deposit);
			
			Payment payment = new Payment();
			payment.set("paymentType",paymentType.ordinal());
			payment.set("PaymentConfigName",paymentConfig.getStr("name"));
			payment.set("bankName",null);
			payment.set("bankAccount",null);
			payment.set("totalAmount",totalAmount);
			payment.set("paymentFee",paymentFee);
			payment.set("payer",getLoginMember().getStr("name"));
			payment.set("operator",null);
			payment.set("memo",null);
			payment.set("paymentStatus",PaymentStatus.success.ordinal());
			payment.set("paymentConfig_id",paymentConfigId);
			payment.set("deposit_id",deposit.getStr("id"));
			payment.set("order_id",order.getStr("id"));
			payment.set("paymentSn", SerialNumberUtil.buildPaymentSn());
			payment.save(payment);
			
			// 库存处理
			if (getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.payment) {
				for (OrderItem orderItem : order.getOrderItemList()) {
					Product product = orderItem.getProduct();
					if (product.getInt("store") != null) {
						product.set("freezeStore",product.getInt("freezeStore") + orderItem.getInt("productQuantity"));
						if (product.getIsOutOfStock()) {
							//Hibernate.initialize(orderItem.getProduct().getProductAttributeMapStore());
							orderItem.getProduct().getProductAttributeMapStore();
						}
						product.update();
						if (product.getIsOutOfStock()) {
							//flushCache();
							HtmlService.service.productContentBuildHtml(product);
						}
					}
				}
			}
			
			// 订单日志
			OrderLog orderLog = new OrderLog();
			orderLog.set("orderLogType",OrderLogType.payment.ordinal());
			orderLog.set("orderSn",order.getStr("orderSn"));
			orderLog.set("operator",null);
			orderLog.set("info","支付总金额：" + payment.getBigDecimal("TotalAmount"));
			orderLog.set("order_id",order.getStr("id"));
			orderLog.save(orderLog);
			
			paymentResult = PaymentResult.success;			
			render("/shop/payment_deposit_result.html");
		} else if (paymentConfig.getPaymentConfigType() == PaymentConfigType.offline) {
			paymentResult = PaymentResult.success;
			render("/shop/payment_offline_result.html");
		} else if (paymentConfig.getPaymentConfigType() == PaymentConfigType.tenpay) {
			TenpayConfig tenpayConfig = (TenpayConfig) paymentConfig.getConfigObject();
			Payment payment = new Payment();
			payment.set("paymentType",paymentType.ordinal());
			payment.set("paymentConfigName",paymentConfig.getStr("name"));
			//payment.set("bankName",getText("PaymentConfigType.tenpay"));
			payment.set("bankAccount",tenpayConfig.getBargainorId());
			payment.set("totalAmount",totalAmount);
			payment.set("paymentFee",paymentFee);
			payment.set("payer",getLoginMember().getStr("username"));
			payment.set("operator",null);
			payment.set("memo",null);
			payment.set("paymentStatus",PaymentStatus.ready.ordinal());
			payment.set("paymentConfig_id",paymentConfigId);
			payment.set("deposit_id",null);
			payment.set("paymentSn", SerialNumberUtil.buildPaymentSn());
			if (paymentType == PaymentType.recharge) {
				payment.set("order_id", null);
			} else {
				payment.set("order_id", order.getStr("id"));
			}
			payment.save(payment);
			
			String ip = getRequest().getRemoteAddr();
			if (tenpayConfig.getTenpayType() == TenpayType.direct) {
				paymentUrl = PaymentConfig.dao.buildTenpayDirectPaymentUrl(paymentConfig, payment.getStr("paymentSn"), totalAmount, description, ip);
			} else {
				paymentUrl = PaymentConfig.dao.buildTenpayPartnerPaymentUrl(paymentConfig, payment.getStr("paymentSn"), totalAmount, description);
			}		
			redirect(paymentUrl);
		}
		/*try {
			String urlString = "123efakiaHR0cDovL3d3dy5zaG9weHgubmV0L2NlcnRpZmljYXRlLmFjdGlvbj9zaG9wVXJsPQ";
			BASE64Decoder bASE64Decoder = new BASE64Decoder();			
			urlString = new String(bASE64Decoder.decodeBuffer(StringUtils.substring(urlString, 8) + "=="));
			URL url = new URL(urlString + SystemConfigUtil.getSystemConfig().getShopUrl());
			URLConnection urlConnection = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection)urlConnection;
			httpConnection.getResponseCode();
		} catch (IOException e) {
			
		}*/
		//return null;
	}
	
	// 财付通支付请求结果处理
	public void tenpayReturn() {
		// 获取参数
		String shopName = "sh" + "op";
		String cmdno = getPara("cmdno","");
		String pay_result = getPara("pay_result","");
		String pay_info = getPara("pay_info","");
		String date = getPara("date","");
		String bargainor_id = getPara("bargainor_id","");
		String transaction_id = getPara("transaction_id","");
		String sp_billno = getPara("sp_billno","");
		String total_fee = getPara("total_fee","");
		String fee_type = getPara("fee_type","");
		String attach = getPara("attach","");
		String version = getPara("version","");
		String retcode = getPara("retcode","");
		String status = getPara("status","");
		String seller = getPara("seller","");
		String trade_price = getPara("trade_price","");
		String transport_fee = getPara("transport_fee","");
		String buyer_id = getPara("buyer_id","");
		String chnid = getPara("chnid","");
		String cft_tid = getPara("cft_tid","");
		String mch_vno = getPara("mch_vno","");
		String sign = getPara("sign","");
		
		if (StringUtils.endsWithIgnoreCase(attach, shopName + ".n" + "et")) {
			addActionError("在线支付参数错误！");
			return;
		}
		Payment payment = null;
		// 验证签名
		if (StringUtils.equals(cmdno, "1")) {
			payment = Payment.dao.getPaymentByPaymentSn(sp_billno);
			paymentConfig = payment.getPaymentConfig();
			TenpayConfig tenpayConfig = (TenpayConfig) paymentConfig.getConfigObject();
			Map<String, String> parameterMap = new LinkedHashMap<String, String>();
			parameterMap.put("cmdno", cmdno);
			parameterMap.put("pay_result", pay_result);
			parameterMap.put("date", date);
			parameterMap.put("transaction_id", transaction_id);
			parameterMap.put("sp_billno", sp_billno);
			parameterMap.put("total_fee", total_fee);
			parameterMap.put("fee_type", fee_type);
			parameterMap.put("attach", attach);
			parameterMap.put("key", tenpayConfig.getKey());
			if (!StringUtils.equals(sign, DigestUtils.md5Hex(PaymentConfig.dao.buildParameterString(parameterMap)).toUpperCase())) {
				addActionError("即时交易支付签名错误！");
				return;
			}
		} else if (StringUtils.equals(cmdno, "12")) {
			payment = Payment.dao.getPaymentByPaymentSn(mch_vno);
			paymentConfig = payment.getPaymentConfig();
			TenpayConfig tenpayConfig = (TenpayConfig) paymentConfig.getConfigObject();
			Map<String, String> parameterMap = new LinkedHashMap<String, String>();
			parameterMap.put("attach", attach);
			parameterMap.put("buyer_id", buyer_id);
			parameterMap.put("cft_tid", cft_tid);
			parameterMap.put("chnid", chnid);
			parameterMap.put("cmdno", cmdno);
			parameterMap.put("mch_vno", mch_vno);
			parameterMap.put("retcode", retcode);
			parameterMap.put("seller", seller);
			parameterMap.put("status", status);
			parameterMap.put("trade_price", trade_price);
			parameterMap.put("transport_fee", transport_fee);
			parameterMap.put("total_fee", total_fee);
			parameterMap.put("version", version);
			parameterMap.put("key", tenpayConfig.getKey());
			if (!StringUtils.equals(sign, DigestUtils.md5Hex(PaymentConfig.dao.buildParameterString(parameterMap)).toUpperCase())) {
				addActionError("担保交易支付签名错误错误！");
				return;
			}
		} else {
			addActionError("支付请求返回参数错误！");
			return;
		}
		
		Member loginMember = getLoginMember();
		if (StringUtils.equals(pay_result, "0")) {
			if (payment == null) {
				addActionError("支付信息错误！");
				return;
			}
			if (payment.getPaymentStatus() == PaymentStatus.success) {
				addActionError("此交易已经完成支付，请勿重复提交！");
				return;
			}
			if (payment.getPaymentStatus() != PaymentStatus.ready) {
				addActionError("交易状态错误！");
				return;
			}
			BigDecimal totalAmount = new BigDecimal(total_fee).divide(new BigDecimal("100"));// 支付总金额
			if (totalAmount.compareTo(payment.getBigDecimal("totalAmount")) != 0) {
				addActionError("交易金额错误！");
				return;
			}
			amountPayable = totalAmount.subtract(payment.getBigDecimal("paymentFee"));// 应付金额（不含支付费用）
			paymentFee = payment.getBigDecimal("paymentFee");
			Deposit deposit = null;
			if (payment.getPaymentType() == PaymentType.recharge) {
				loginMember.set("deposit",loginMember.getBigDecimal("deposit").add(amountPayable));
				loginMember.update();
				
				deposit = new Deposit();
				deposit.set("depositType",DepositType.memberRecharge.ordinal());
				deposit.set("credit",amountPayable);
				deposit.set("debit",new BigDecimal("0"));
				deposit.set("Balance",loginMember.getBigDecimal("deposit"));
				deposit.set("member_id",loginMember.getStr("username"));
				//deposit.setPayment(payment);
				deposit.save(deposit);
			} else if (payment.getPaymentType() == PaymentType.online) {
				order = payment.getOrder();
				if (totalAmount.compareTo(order.getBigDecimal("totalAmount").subtract(order.getBigDecimal("paidAmount"))) == 0) {
					order.set("paymentStatus",Orders.PaymentStatus.paid);
					order.set("paidAmount",order.getBigDecimal("paidAmount").add(totalAmount));
				} else if (totalAmount.compareTo(order.getBigDecimal("totalAmount")) < 0) {
					order.set("paymentStatus",Orders.PaymentStatus.partPayment);
					order.set("paidAmount",order.getBigDecimal("paidAmount").add(totalAmount));
				} else {
					addActionError("交易金额错误！");
					return;
				}
				order.update();
				
				// 库存处理
				if (getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.payment) {
					for (OrderItem orderItem : order.getOrderItemList()) {
						Product product = orderItem.getProduct();
						if (product.getInt("store") != null) {
							product.set("freezeStore",product.getInt("freezeStore") + orderItem.getInt("productQuantity"));
							if (product.getIsOutOfStock()) {
								//Hibernate.initialize(orderItem.getProduct().getProductAttributeMapStore());
								orderItem.getProduct().getProductAttributeMapStore();
							}
							product.update();
							if (product.getIsOutOfStock()) {
								//flushCache();
								HtmlService.service.productContentBuildHtml(product);
							}
						}
					}
				}
				
				// 订单日志
				OrderLog orderLog = new OrderLog();
				orderLog.set("orderLogType",OrderLogType.payment.ordinal());
				orderLog.set("orderSn",order.getStr("orderSn"));
				orderLog.set("operator",null);
				orderLog.set("info","支付总金额：" + payment.getBigDecimal("totalAmount"));
				payment.set("deposit_id",deposit.getStr("id"));
				orderLog.set("order_id",order.getStr("id"));
				orderLog.save();
			} else {
				addActionError("交易类型错误！");
				return;
			}
			payment.set("paymentStatus",PaymentStatus.success.ordinal());
			payment.update();
			paymentResult = PaymentResult.success;
		} else {
			paymentResult = PaymentResult.failure;
		}
		//setResponseNoCache();
		setAttr("paymentResult", paymentResult);
		render("/shop/payment_tenpay_result.html");
		//return "tenpay_result";
	}
}
