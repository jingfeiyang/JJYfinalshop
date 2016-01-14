package com.jfinalshop.controller.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinalshop.bean.SystemConfig.PointType;
import com.jfinalshop.bean.SystemConfig.StoreFreezeTime;
import com.jfinalshop.model.Area;
import com.jfinalshop.model.DeliveryCorp;
import com.jfinalshop.model.DeliveryItem;
import com.jfinalshop.model.DeliveryType;
import com.jfinalshop.model.Deposit;
import com.jfinalshop.model.Deposit.DepositType;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.MemberRank;
import com.jfinalshop.model.OrderItem;
import com.jfinalshop.model.OrderLog;
import com.jfinalshop.model.OrderLog.OrderLogType;
import com.jfinalshop.model.Orders;
import com.jfinalshop.model.Orders.OrderStatus;
import com.jfinalshop.model.Orders.ShippingStatus;
import com.jfinalshop.model.Payment;
import com.jfinalshop.model.Payment.PaymentStatus;
import com.jfinalshop.model.Payment.PaymentType;
import com.jfinalshop.model.PaymentConfig;
import com.jfinalshop.model.Product;
import com.jfinalshop.model.Product.WeightUnit;
import com.jfinalshop.model.Refund;
import com.jfinalshop.model.Refund.RefundType;
import com.jfinalshop.model.Reship;
import com.jfinalshop.model.Shipping;
import com.jfinalshop.security.ShiroUtils;
import com.jfinalshop.service.HtmlService;
import com.jfinalshop.util.SerialNumberUtil;
import com.jfinalshop.validator.admin.OrderValidator;
import com.jfinalshop.validator.admin.RefundValidator;
import com.jfinalshop.validator.admin.ReshipValidator;
import com.jfinalshop.validator.admin.ShippingValidator;

/**
 * 后台类 - 订单
 * 
 */
public class OrderController extends BaseAdminController<Orders>{
	
	private Orders orders;
	private Payment payment;
	private Shipping shipping;
	private Refund refund;
	private Reship reship;
	
	private List<OrderItem> orderItemList = new ArrayList<OrderItem>();
	private List<DeliveryItem> deliveryItemList = new ArrayList<DeliveryItem>();
	
	// 列表
	public void list() {
		findByPage();
		render("/admin/order_list.html");
	}
		
	// 编辑
	public void edit() {		
		String orderId = getPara("order.id","");		
		if (StrKit.isBlank(orderId)){
			addActionError("订单ID不能为空！");
			return;
		}
		
		orders = Orders.dao.findById(orderId);		
		if (orders.getOrderStatus() == OrderStatus.completed || orders.getOrderStatus() == OrderStatus.invalid) {
			addActionError("此订单状态无法编辑！");
			return;
		}
		if (orders.getPaymentStatus() != Orders.PaymentStatus.unpaid) {
			addActionError("此订单付款状态无法编辑！");
			return;
		}
		if (orders.getShippingStatus() != ShippingStatus.unshipped) {
			addActionError("此订单发货状态无法编辑！");
			return;
		}
		setAttr("orders", orders);
		setAttr("allDeliveryType", DeliveryType.dao.getAllDeliveryCorp());// 获取所有配送方式
		setAttr("allPaymentConfig", PaymentConfig.dao.getAll()); // 获取所有支付方式
		setAttr("allWeightUnit", getAllWeightUnit()); // 获取所有重量单位
		render("/admin/order_input.html");
	}
		
	// 更新
	@Before(OrderValidator.class)
	public void update() {
		orders = getModel(Orders.class);
		String productWeightUnit = getPara("productWeightUnit","");
		int orderItemSize = getParaToInt("orderItemSize",0);
		orders.set("productWeightUnit", WeightUnit.valueOf(productWeightUnit).ordinal());
		
		// 统计产品行数
		for (int i = 1; i <= orderItemSize; i++) {
			orderItemList.add(getModel(OrderItem.class, "orderItem[" + i + "]"));
		}
		
		Orders persistent = Orders.dao.findById(orders.getStr("id"));
		if (persistent.getOrderStatus() == OrderStatus.completed || persistent.getOrderStatus() == OrderStatus.invalid) {
			addActionError("此订单状态无法编辑！");
			return;
		}
		if (persistent.getPaymentStatus() != Orders.PaymentStatus.unpaid) {
			addActionError("此订单付款状态无法编辑！");
			return;
		}
		if (persistent.getShippingStatus() != ShippingStatus.unshipped) {
			addActionError("此订单配送状态无法编辑！");
			return;
		}
		if (orders.getBigDecimal("deliveryFee").compareTo(new BigDecimal("0")) < 0) {
			addActionError("配送费用不允许小于0！");
			return;
		}
		if (orders.getBigDecimal("deliveryFee").compareTo(new BigDecimal("0")) < 0) {
			addActionError("支付费用不允许小于0！");
			return;
		}
		if (orders.getDouble("productWeight") < 0) {
			addActionError("商品重量不允许小于0！");
			return;
		}
		if (StringUtils.isEmpty(orders.getStr("shipPhone")) && StringUtils.isEmpty(orders.getStr("shipMobile"))) {
			addActionError("联系电话、联系手机必须填写其中一项！");
			return;
		}
		if (orderItemList == null || orderItemList.size() == 0) {
			addActionError("请保留至少一个商品！");
			return;
		}
		if (!Area.dao.isAreaPath(orders.getStr("shipAreaPath"))) {
			addActionError("地区错误！");
			return;
		}
		for (OrderItem orderItem : orderItemList) {
			if (orderItem.getInt("productQuantity") <= 0) {
				addActionError("商品数量错误！");
				return;
			}
			if (orderItem.getBigDecimal("productPrice").compareTo(new BigDecimal("0")) < 0) {
				addActionError("商品价格错误！");
				return;
			}
			Product product = Product.dao.findById(OrderItem.dao.findById(orderItem.getStr("id")).getProduct().getStr("id"));
			if (product.getInt("store") != null) {
				OrderItem orderItemPersistent = OrderItem.dao.findById(orderItem.getStr("id"));
				Integer availableStore = 0;
				if ((getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.payment && orders.getPaymentStatus() == Orders.PaymentStatus.unpaid) || 
					(getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.ship && orders.getShippingStatus() == ShippingStatus.unshipped)) {
					availableStore = product.getInt("store") - product.getInt("freezeStore");
				} else {
					availableStore = product.getInt("store") - product.getInt("freezeStore") + orderItemPersistent.getInt("productQuantity");
				}
				if (orderItem.getInt("productQuantity") > availableStore) {
					addActionError("商品[" + product.getStr("name") + "]库存不足！");
					return;
				}
				if (getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.order || (getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.payment && orders.getPaymentStatus() != Orders.PaymentStatus.unpaid) || 
					(getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.ship && orders.getShippingStatus() != ShippingStatus.unshipped)) {
					product.set("freezeStore",product.getInt("freezeStore") - orderItemPersistent.getInt("productQuantity") + orderItem.getInt("productQuantity"));
					product.update();
				}
			}
		}
		DeliveryType deliveryType = orders.getDeliveryType();
		PaymentConfig paymentConfig = orders.getPaymentConfig();
		String paymentConfigName = null;
		if (paymentConfig != null && StringUtils.isNotEmpty(paymentConfig.getStr("id"))) {
			//paymentConfig = paymentConfigService.load(paymentConfig.getId());
			paymentConfigName = paymentConfig.getStr("name");
		} else {
			paymentConfig = null;
			paymentConfigName = "货到付款";
		}
		
		Integer productTotalQuantity = 0;// 商品总数
		BigDecimal productTotalPrice = new BigDecimal("0");// 商品总价格
		BigDecimal totalAmount = new BigDecimal("0");// 订单总金额
		for (OrderItem orderItem : orderItemList) {
			OrderItem orderItemPersistent = OrderItem.dao.findById(orderItem.getStr("id"));
			productTotalQuantity += orderItem.getInt("productQuantity");
			productTotalPrice = productTotalPrice.add(orderItem.getBigDecimal("productPrice").multiply(new BigDecimal(orderItem.getInt("productQuantity").toString())));
			orderItemPersistent.update();
		}
		for (OrderItem orderItem : persistent.getOrderItemList()) {
			if (!orderItemList.contains(orderItem)) {
				orderItem.delete();
			}
		}
		totalAmount = productTotalPrice.add(orders.getBigDecimal("deliveryFee")).add(orders.getBigDecimal("paymentFee"));
		orders.set("totalAmount", totalAmount);
		orders.set("orderStatus", OrderStatus.valueOf(OrderStatus.processed.name()).ordinal());
		orders.set("deliveryTypeName", deliveryType.getStr("name"));
		orders.set("paymentConfigName", paymentConfigName);
		orders.set("paymentConfig_id",paymentConfig.getStr("id"));
		orders.set("productTotalPrice", productTotalPrice);
		orders.set("productTotalQuantity",productTotalQuantity);
		orders.set("shipArea",Area.dao.getAreaString(orders.getStr("shipAreaPath")));
		updated(orders);
		
		// 订单日志
		OrderLog orderLog = new OrderLog();
		orderLog.set("orderLogType",OrderLogType.valueOf(OrderLogType.modify.name()).ordinal());
		orderLog.set("orderSn",persistent.getStr("orderSn"));
		orderLog.set("operator",getLoginAdminName());
		orderLog.set("info",null);
		orderLog.set("order_id",persistent.getStr("id"));
		orderLog.save(orderLog);		
		redirect("/order/list");
	}
	
	// 处理
	public void process() {
		String orderId = getPara("orders.id", "");
		if (StrKit.isBlank(orderId)) {
			return;
		}
		orders = Orders.dao.findById(orderId);
		setAttr("orders", orders);
		setAttr("nonRechargePaymentTypeList", getNonRechargePaymentTypeList());
		setAttr("allPaymentConfig", getAllPaymentConfig());
		setAttr("allDeliveryType", getAllDeliveryType());
		setAttr("allDeliveryCorp", getAllDeliveryCorp());
		setAttr("refundTypeList", getRefundTypeList());
		render("/admin/order_process.html");
	}
		
	// 支付
	@Before(Tx.class)
	public void payment() {
		payment = getModel(Payment.class);
		String orderId = getPara("orders.id","");
		String paymentType = getPara("paymentType","");	
		
		payment.set("paymentType", PaymentType.valueOf(paymentType).ordinal()); // 支付类型
		
		if (StrKit.isBlank(orderId)) {
			addActionError("订单ID不能为空！");
			return;
		}
		orders = Orders.dao.findById(orderId);
		
		if (orders.getOrderStatus() == OrderStatus.completed || orders.getOrderStatus() == OrderStatus.invalid) {
			addActionError("此订单状态无法支付！");
			return;
		}
		if (orders.getPaymentStatus() == Orders.PaymentStatus.paid || orders.getPaymentStatus() == Orders.PaymentStatus.partRefund || orders.getPaymentStatus() == Orders.PaymentStatus.refunded) {
			addActionError("此订单付款状态无法支付！");
			return;
		}
		if (payment.getPaymentType() == PaymentType.recharge) {
			addActionError("支付类型错误！");
			return;
		}
		if (payment.getBigDecimal("totalAmount").compareTo(new BigDecimal("0")) < 0) {
			addActionError("支付金额不允许小于0！");
			return;
		}
		if (payment.getBigDecimal("totalAmount").compareTo(orders.getBigDecimal("totalAmount").subtract(orders.getBigDecimal("paidAmount"))) > 0) {
			addActionError("支付金额超出订单需付金额！");
			return;
		}
		Deposit deposit = null;
		if (payment.getPaymentType() == PaymentType.deposit) {
			Member member = orders.getMember();
			if (payment.getBigDecimal("totalAmount").compareTo(member.getBigDecimal("deposit")) > 0) {
				addActionError("会员余存款余额不足！");
				return;
			}
			member.set("deposit",member.getBigDecimal("deposit").subtract(payment.getBigDecimal("totalAmount")));
			member.set("modifyDate", new Date());
			member.update();
			deposit = new Deposit();
			deposit.set("depositType",DepositType.adminPayment.ordinal());
			deposit.set("credit",new BigDecimal("0"));
			deposit.set("debit",payment.getBigDecimal("totalAmount"));
			deposit.set("balance",member.getBigDecimal("deposit"));
			deposit.set("member_id",member.getStr("id"));
			deposit.save(deposit);
		}
		PaymentConfig paymentConfig = PaymentConfig.dao.findById(payment.getStr("paymentConfig_id"));
		payment.set("paymentConfigName",paymentConfig.getStr("name"));
		payment.set("paymentFee",new BigDecimal("0"));
		payment.set("operator",ShiroUtils.getLoginAdminName());
		payment.set("paymentStatus",PaymentStatus.success.ordinal());
		payment.set("deposit_id",deposit.getStr("id"));
		payment.set("paymentSn", SerialNumberUtil.buildPaymentSn());
		payment.set("order_id",orders.getStr("id"));
		payment.save(payment);
		
		// 库存处理
		if (getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.payment && orders.getPaymentStatus() == Orders.PaymentStatus.unpaid && orders.getShippingStatus() == ShippingStatus.unshipped) {
			for (OrderItem orderItem : orders.getOrderItemList()) {
				Product product = orderItem.getProduct();
				if (product.getInt("store") != null) {
					product.set("freezeStore",product.getInt("freezeStore") + orderItem.getInt("productQuantity"));
					if (product.getIsOutOfStock()) {
						orderItem.getProduct().getProductAttributeMapStore();
					}
					product.update();
					if (product.getIsOutOfStock()) {
						HtmlService.service.productContentBuildHtml(product);
					}
				}
			}
		}
		
		orders.set("orderStatus",OrderStatus.processed.ordinal());
		if (payment.getBigDecimal("totalAmount").compareTo(orders.getBigDecimal("totalAmount").subtract(orders.getBigDecimal("paidAmount"))) == 0) {
			orders.set("paymentStatus",Orders.PaymentStatus.paid.ordinal());
		} else {
			orders.set("paymentStatus",Orders.PaymentStatus.partPayment.ordinal());
		}
		orders.set("paidAmount",orders.getBigDecimal("paidAmount").add(payment.getBigDecimal("totalAmount")));
		updated(orders);
		
		// 订单日志
		OrderLog orderLog = new OrderLog();
		orderLog.set("orderLogType",OrderLogType.payment.ordinal());
		orderLog.set("orderSn",orders.getStr("orderSn"));
		orderLog.set("operator",getLoginAdminName());
		orderLog.set("info","支付金额：" + payment.getBigDecimal("totalAmount"));
		orderLog.set("order_id",orders.getStr("id"));
		orderLog.save(orderLog);
		process();
	}
	
	// 发货
	@Before({ShippingValidator.class,Tx.class})
	public void shipping() {
		String orderId = getPara("orders.id","");
		shipping = getModel(Shipping.class);
		
		if (StrKit.isBlank(orderId)){
			addActionError("订单ID不能为空！");
			return;
		}
		orders = Orders.dao.findById(orderId);
		int orderItemSize = getParaToInt("orderItemSize",0);
		
		// 统计产品行数
		for (int i = 1; i <= orderItemSize; i++) {
			deliveryItemList.add(getModel(DeliveryItem.class, "deliveryItemList[" + i + "]"));
		}
		
		if (orders.getOrderStatus() == OrderStatus.completed || orders.getOrderStatus() == OrderStatus.invalid) {
			addActionError("此订单状态无法发货！");
			return;
		}
		if (orders.getShippingStatus() == ShippingStatus.shipped) {
			addActionError("此订单配送状态无法发货！");
			return;
		}
		if (shipping.getBigDecimal("deliveryFee").compareTo(new BigDecimal("0")) < 0) {
			addActionError("物流费用不允许小于0！");
			return;
		}
		if (!Area.dao.isAreaPath(shipping.getStr("shipAreaPath"))) {
			addActionError("地区错误！");
			return;
		}
		if (StringUtils.isEmpty(orders.getStr("shipPhone")) && StringUtils.isEmpty(orders.getStr("shipMobile"))) {
			addActionError("联系电话、联系手机必须填写其中一项！");
			return;
		}
		List<OrderItem> orderItemList = orders.getOrderItemList();
		int totalDeliveryQuantity = 0;// 总发货数
		for (DeliveryItem deliveryItem : deliveryItemList) {
			Product product = Product.dao.findById(deliveryItem.getStr("product_id"));
			Integer deliveryQuantity = deliveryItem.getInt("deliveryQuantity");
			if (deliveryQuantity < 0) {
				addActionError("发货数不允许小于0！");
				return;
			}
			totalDeliveryQuantity += deliveryQuantity;
			boolean isExist = false;
			for (OrderItem orderItem : orderItemList) {
				if (product.getStr("id").equals(orderItem.getStr("product_id"))) {
					if (deliveryQuantity > (orderItem.getInt("productQuantity") - orderItem.getInt("deliveryQuantity"))) {
						addActionError("发货数超出订单购买数！");
						return;
					}
					if (product.get("store") != null) {
						if ((getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.payment && orders.getPaymentStatus() == Orders.PaymentStatus.unpaid && orders.getShippingStatus() == ShippingStatus.unshipped) || (getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.ship && orders.getShippingStatus() == ShippingStatus.unshipped)) {
							if (deliveryQuantity > (product.getInt("store") - product.getInt("freezeStore"))) {
								addActionError("商品[" + orderItem.getProduct().getStr("name") + "]库存不足！");
								return;
							}
						} else {
							if (orderItem.getInt("totalDeliveryQuantity") < orderItem.getInt("productQuantity") && deliveryQuantity > (product.getInt("store") - product.getInt("freezeStore") + orderItem.getInt("productQuantity") - orderItem.getInt("deliveryQuantity"))) {
								addActionError("商品[" + orderItem.getProduct().getStr("name") + "]库存不足！");
								return;
							} else if (orderItem.getInt("totalDeliveryQuantity") >= orderItem.getInt("productQuantity") && deliveryQuantity > (product.getInt("store") - product.getInt("freezeStore"))) {
								addActionError("商品[" + orderItem.getProduct().getStr("name") + "]库存不足！");
								return;
							}
						}
					}
					isExist = true;
					break;
				}
			}
			if (!isExist) {
				addActionError("发货商品未在订单中！");
				return;
			}
		}
		if (totalDeliveryQuantity < 1) {
			addActionError("发货总数必须大于0！");
			return;
		}
		DeliveryType deliveryType = DeliveryType.dao.findById(shipping.getStr("deliveryType_id"));
		shipping.set("shipArea",Area.dao.getAreaString(shipping.getStr("shipAreaPath")));
		shipping.set("order_id",orders.getStr("id"));
		shipping.set("deliveryTypeName",deliveryType.getStr("name"));
		shipping.set("shippingSn", SerialNumberUtil.buildShippingSn());
		shipping.save(shipping);
		
		// 库存处理
		if ((getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.payment && orders.getPaymentStatus() == Orders.PaymentStatus.unpaid && orders.getShippingStatus() == ShippingStatus.unshipped) || (getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.ship && orders.getShippingStatus() == ShippingStatus.unshipped)) {
			for (OrderItem orderItem : orders.getOrderItemList()) {
				Product product = orderItem.getProduct();
				if (product.getStr("store") != null) {
					product.set("freezeStore",product.getInt("freezeStore") + orderItem.getInt("productQuantity"));
					if (product.getIsOutOfStock()) {
						orderItem.getProduct().getProductAttributeMapStore();
					}
					product.update();
					if (product.getIsOutOfStock()) {
						HtmlService.service.productContentBuildHtml(product);
					}
				}
			}
		}
		
		ShippingStatus shippingStatus = ShippingStatus.shipped;// 发货状态
		for (DeliveryItem deliveryItem : deliveryItemList) {
			Product product = Product.dao.findById(deliveryItem.getStr("product_id"));
			for (OrderItem orderItem : orderItemList) {
				if (orderItem.getStr("product_id").equals(product.getStr("product_id"))) {
					orderItem.set("deliveryQuantity",orderItem.getInt("deliveryQuantity") + deliveryItem.getInt("deliveryQuantity"));
					orderItem.set("totalDeliveryQuantity",orderItem.getInt("totalDeliveryQuantity") + deliveryItem.getInt("deliveryQuantity"));
					orderItem.update();
					if (orderItem.getInt("productQuantity") > orderItem.getInt("deliveryQuantity")) {
						shippingStatus = ShippingStatus.partShipped;
					}
					// 库存处理
					if (product.getInt("store") != null) {
						if (orderItem.getInt("totalDeliveryQuantity") <= orderItem.getInt("productQuantity")) {
							product.set("freezeStore",product.getInt("freezeStore") - deliveryItem.getInt("deliveryQuantity"));
						}
						product.set("store",product.getInt("store") - deliveryItem.getInt("deliveryQuantity"));
						if (product.getIsOutOfStock()) {
							product.getProductAttributeMapStore();
						}
						product.update();
						if (product.getIsOutOfStock()) {
							HtmlService.service.productContentBuildHtml(product);
						}
					}
					break;
				}
			}
			deliveryItem.set("productSn", product.getStr("productSn"));
			deliveryItem.set("productName", product.getStr("name"));
			deliveryItem.set("productHtmlFilePath", product.getStr("htmlFilePath"));
			deliveryItem.set("shipping_id", shipping.getStr("id"));
			deliveryItem.save(deliveryItem);
		}
		orders.set("shippingStatus",shippingStatus.ordinal());
		updated(orders);
		
		// 订单日志
		OrderLog orderLog = new OrderLog();
		orderLog.set("orderLogType", OrderLogType.shipping.ordinal());
		orderLog.set("orderSn", orders.getStr("orderSn"));
		orderLog.set("operator", getLoginAdminName());
		orderLog.set("info", null);
		orderLog.set("order_id",orders.getStr("id"));
		orderLog.save(orderLog);
		process();
	}
		
		
	// 完成
	@Before(Tx.class)
	public void completed() {
		String orderId = getPara("orders.id", "");
		if (StrKit.isBlank(orderId)) {
			addActionError("订单ID不能空！");
			return;
		}
		orders = Orders.dao.findById(orderId);
		
		if (orders.getOrderStatus() == OrderStatus.completed) {
			ajaxJsonWarnMessage("此订单已经完成！");
		} else if (orders.getOrderStatus() == OrderStatus.invalid) {
			ajaxJsonErrorMessage("此订单已经作废！");
		} else {
			orders.set("orderStatus",OrderStatus.valueOf(OrderStatus.completed.name()).ordinal());
			updated(orders);
			
			// 积分处理
			Integer totalPoint = 0;
			if (getSystemConfig().getPointType() == PointType.orderAmount) {
				totalPoint = orders.getBigDecimal("productTotalPrice").multiply(new BigDecimal(getSystemConfig().getPointScale().toString())).setScale(0, RoundingMode.DOWN).intValue();
			} else if (getSystemConfig().getPointType() == PointType.productSet) {
				for (OrderItem orderItem : orders.getOrderItemList()) {
					totalPoint = orderItem.getProduct().getInt("point") * orderItem.getInt("productQuantity") + totalPoint;
				}
			}
			if (totalPoint > 0) {
				Member member = orders.getMember();
				member.set("point",member.getInt("point") + totalPoint);
				MemberRank upMemberRank = MemberRank.dao.getUpMemberRankByPoint(member.getInt("point"));
				if (upMemberRank != null && member.getInt("point") >= upMemberRank.getInt("point")) {
					member.set("memberRank_id",upMemberRank.getStr("id"));
				}
				member.update();
			}
			// 订单日志
			OrderLog orderLog = new OrderLog();
			orderLog.set("orderLogType", OrderLogType.valueOf(OrderLogType.completed.name()).ordinal());
			orderLog.set("orderSn", orders.getStr("orderSn"));
			orderLog.set("operator", getLoginAdminName());
			orderLog.set("info", null);
			orderLog.set("order_id",orders.getStr("id"));
			orderLog.save(orderLog);
			ajaxJsonSuccessMessage("您的操作已成功！");
		}
	}
	
	// 作废
	public void invalid() {
		String orderId = getPara("order.id", "");
		if (StrKit.isBlank(orderId)) {
			addActionError("订单ID不能空！");
			return;
		}
		orders = Orders.dao.findById(orderId);
		if (orders.getOrderStatus() == OrderStatus.completed || orders.getOrderStatus() == OrderStatus.invalid) {
			addActionError("此订单状态无法编辑！");
			return;
		}
		if (orders.getPaymentStatus() != Orders.PaymentStatus.unpaid) {
			addActionError("此订单支付状态无法编辑！");
			return;
		}
		if (orders.getShippingStatus() != ShippingStatus.unshipped) {
			addActionError("此订单发货状态无法编辑！");
			return;
		}
		
		if (orders.getOrderStatus() == OrderStatus.completed) {
			ajaxJsonWarnMessage("此订单已经完成！");
		} else if (orders.getOrderStatus() == OrderStatus.invalid) {
			ajaxJsonErrorMessage("此订单已经作废！");
		} else if (orders.getPaymentStatus() != Orders.PaymentStatus.unpaid) {
			ajaxJsonErrorMessage("此订单付款状态无法作废！");
		} else if (orders.getShippingStatus() != ShippingStatus.unshipped) {
			ajaxJsonErrorMessage("此订单配送状态无法作废！");
		} else {
			orders.set("orderStatus",OrderStatus.valueOf(OrderStatus.invalid.name()).ordinal());
			updated(orders);
			// 库存处理
			if (getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.order || (getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.payment && orders.getPaymentStatus() != Orders.PaymentStatus.unpaid) || orders.getShippingStatus() != ShippingStatus.unshipped) {
				for (OrderItem orderItem : orders.getOrderItemList()) {
					Product product = orderItem.getProduct();
					product.set("freezeStore", product.getInt("freezeStore") - orderItem.getInt("productQuantity"));
					if (!product.getIsOutOfStock()) {
						orderItem.getProduct().getProductAttributeMapStore();
					}
					product.set("modifyDate", new Date());
					product.update();
					if (!product.getIsOutOfStock()) {
						HtmlService.service.productContentBuildHtml(product);
					}
				}
			}
			// 订单日志
			OrderLog orderLog = new OrderLog();
			orderLog.set("orderLogType",OrderLogType.valueOf(OrderLogType.invalid.name()).ordinal());
			orderLog.set("orderSn", orders.getStr("orderSn"));
			orderLog.set("order_id",orders.getStr("id"));
			orderLog.save(orderLog);
			ajaxJsonSuccessMessage("您的操作已成功！");
		}
	}
		
	// 查看
	public void view() {
		String orderId = getPara("orders.id", "");
		if (StrKit.notBlank(orderId)) {
			orders = Orders.dao.findById(orderId);
		}
		setAttr("order", orders);
		render("/admin/order_view.html");
	}
		
	// 退款
	@Before({RefundValidator.class,Tx.class})
	public void refund() {
		String refundType = getPara("refundType","");
		String ordersId = getPara("orders.id","");
		refund = getModel(Refund.class);
		refund.set("refundType", RefundType.valueOf(refundType).ordinal());
		
		if (StrKit.isBlank(ordersId)){ 
			addActionError("订单ID不能为空！");
			return;		
		}
		orders = Orders.dao.findById(ordersId);
		if (orders.getOrderStatus() == OrderStatus.completed || orders.getOrderStatus() == OrderStatus.invalid) {
			addActionError("此订单状态无法退款！");
			return;
		}
		if (orders.getPaymentStatus() == Orders.PaymentStatus.unpaid || orders.getPaymentStatus() == Orders.PaymentStatus.refunded) {
			addActionError("此订单付款状态无法支付！");
			return;
		}
		if (refund.getBigDecimal("totalAmount").compareTo(new BigDecimal("0")) < 0) {
			addActionError("退款金额不允许小于0！");
			return;
		}
		if (refund.getBigDecimal("totalAmount").compareTo(orders.getBigDecimal("paidAmount")) > 0) {
			addActionError("退款金额超出订单已付金额！");
			return;
		}
		Deposit deposit = null;
		if (refund.getRefundType() == RefundType.deposit) {
			Member member = orders.getMember();
			member.set("deposit",member.getBigDecimal("deposit").add(refund.getBigDecimal("totalAmount")));
			member.update();
			deposit = new Deposit();
			deposit.set("depositType",DepositType.adminRecharge.ordinal());
			deposit.set("credit",refund.getBigDecimal("totalAmount"));
			deposit.set("debit",new BigDecimal("0"));
			deposit.set("balance",member.getBigDecimal("deposit"));
			deposit.set("member_id",member.getStr("id"));
			deposit.save(deposit);
		}
		PaymentConfig paymentConfig = PaymentConfig.dao.findById(refund.getStr("paymentConfig_id"));
		refund.set("paymentConfigName",paymentConfig.getStr("name"));
		refund.set("operator",getLoginAdminName());
		refund.set("deposit_id",deposit.getStr("id"));
		refund.set("order_id",orders.getStr("id"));
		refund.set("refundSn", SerialNumberUtil.buildRefundSn());
		refund.save(refund);
		
		orders.set("orderStatus",OrderStatus.processed.ordinal());
		if (refund.getBigDecimal("totalAmount").compareTo(orders.getBigDecimal("paidAmount")) < 0) {
			orders.set("paymentStatus",Orders.PaymentStatus.partRefund.ordinal());
		} else {
			orders.set("paymentStatus",Orders.PaymentStatus.refunded.ordinal());
		}
		orders.set("paidAmount",orders.getBigDecimal("paidAmount").subtract(refund.getBigDecimal("totalAmount")));
		orders.update();
		
		// 订单日志
		OrderLog orderLog = new OrderLog();
		orderLog.set("orderLogType",OrderLogType.refund.ordinal());
		orderLog.set("orderSn",orders.getStr("orderSn"));
		orderLog.set("operator",getLoginAdminName());
		orderLog.set("info","退款金额：" + refund.getBigDecimal("totalAmount"));
		orderLog.set("order_id",orders.getStr("id"));
		orderLog.save(orderLog);
		process();
	}
		
	// 退货
	@Before({ReshipValidator.class,Tx.class})
	public void reship() {
		String ordersId = getPara("orders.id","");
		reship = getModel(Reship.class);
		int orderItemSize = getParaToInt("orderItemSize",0);
		// 统计产品行数
		for (int i = 1; i <= orderItemSize; i++) {
			deliveryItemList.add(getModel(DeliveryItem.class, "deliveryItemList[" + i + "]"));
		}
		if (StrKit.isBlank(ordersId)) {
			addActionError("此订单ID不能为空！");
			return;
		}
		orders = Orders.dao.findById(ordersId);
		
		if (orders.getOrderStatus() == OrderStatus.completed || orders.getOrderStatus() == OrderStatus.invalid) {
			addActionError("此订单状态无法退货！");
			return;
		}
		if (orders.getShippingStatus() == ShippingStatus.unshipped || orders.getShippingStatus() == ShippingStatus.reshiped) {
			addActionError("此订单配送状态无法退货！");
			return;
		}
		if (reship.getBigDecimal("deliveryFee").compareTo(new BigDecimal("0")) < 0) {
			addActionError("物流费用不允许小于0！");
			return;
		}
		if (!Area.dao.isAreaPath(reship.getStr("shipAreaPath"))) {
			addActionError("地区错误！");
			return;
		}
		if (StringUtils.isEmpty(orders.getStr("shipPhone")) && StringUtils.isEmpty(orders.getStr("shipMobile"))) {
			addActionError("联系电话、联系手机必须填写其中一项！");
			return;
		}
		//orders = orderService.load(orders.getId());
		List<OrderItem> orderItemSet = orders.getOrderItemList();
		int totalDeliveryQuantity = 0;// 总退货数
		for (DeliveryItem deliveryItem : deliveryItemList) {
			Product product = Product.dao.findById(deliveryItem.getStr("product_id"));
			Integer deliveryQuantity = deliveryItem.getInt("deliveryQuantity");
			if (deliveryQuantity < 0) {
				addActionError("退货数不允许小于0！");
				return;
			}
			totalDeliveryQuantity += deliveryQuantity;
			boolean isExist = false;
			for (OrderItem orderItem : orderItemSet) {
				if (product.getStr("id").equals(orderItem.getStr("product_id"))) {
					if (deliveryQuantity > orderItem.getInt("deliveryQuantity")) {
						addActionError("退货数超出已发货数！");
						return;
					}
					isExist = true;
					break;
				}
			}
			if (!isExist) {
				addActionError("退货商品未在订单中！");
				return;
			}
		}
		if (totalDeliveryQuantity < 1) {
			addActionError("退货总数必须大于0！");
			return;
		}
		
		DeliveryType deliveryType = DeliveryType.dao.findById(reship.getStr("deliveryType_id"));
		reship.set("shipArea",Area.dao.getAreaString(reship.getStr("shipAreaPath")));
		reship.set("order_id",orders.getStr("id"));
		reship.set("deliveryTypeName",deliveryType.getStr("name"));
		reship.set("reshipSn",SerialNumberUtil.buildReshipSn());
		reship.save(reship);
		
		ShippingStatus shippingStatus = ShippingStatus.reshiped;// 配送状态
		for (DeliveryItem deliveryItem : deliveryItemList) {
			Product product = Product.dao.findById(deliveryItem.getStr("product_id"));
			for (OrderItem orderItem : orderItemSet) {
				if (orderItem.getStr("product_id").equals(product.getStr("id"))) {
					orderItem.set("deliveryQuantity",orderItem.getInt("deliveryQuantity") - deliveryItem.getInt("deliveryQuantity"));
					orderItem.update();
					if (orderItem.getInt("deliveryQuantity") > deliveryItem.getInt("deliveryQuantity")) {
						shippingStatus = ShippingStatus.partReshiped;
					}
				}
			}
			deliveryItem.set("productSn",product.getStr("productSn"));
			deliveryItem.set("productName",product.getStr("name"));
			deliveryItem.set("productHtmlFilePath",product.getStr("htmlFilePath"));
			deliveryItem.set("reship_id",reship.getStr("id"));
			deliveryItem.save(deliveryItem);
		}
		orders.set("shippingStatus",shippingStatus.ordinal());
		updated(orders);
		
		// 订单日志
		OrderLog orderLog = new OrderLog();
		orderLog.set("orderLogType",OrderLogType.reship.ordinal());
		orderLog.set("orderSn",orders.getStr("orderSn"));
		orderLog.set("operator",getLoginAdminName());
		orderLog.set("info",null);
		orderLog.set("order_id",orders.getStr("id"));
		orderLog.save(orderLog);
		process();
	}
		
	// 获取所有重量单位
	public List<WeightUnit> getAllWeightUnit() {
		List<WeightUnit> allWeightUnit = new ArrayList<WeightUnit>();
		for (WeightUnit weightUnit : WeightUnit.values()) {
			allWeightUnit.add(weightUnit);
		}
		return allWeightUnit;
	}
	
	// 获取支付类型（不包含在线充值）
	public List<PaymentType> getNonRechargePaymentTypeList() {
		List<PaymentType> paymentTypeList = new ArrayList<PaymentType>();
		for (PaymentType paymentType : PaymentType.values()) {
			if (paymentType != PaymentType.recharge) {
				paymentTypeList.add(paymentType);
			}
		}
		return paymentTypeList;
	}
	
	// 获取退款类型
	public List<RefundType> getRefundTypeList() {
		List<RefundType> refundTypeList = new ArrayList<RefundType>();
		for (RefundType refundType : RefundType.values()) {
			refundTypeList.add(refundType);
		}
		return refundTypeList;
	}
	
	// 获取所有支付方式
	public List<PaymentConfig> getAllPaymentConfig() {
		return PaymentConfig.dao.getAll();
	}
	
	// 获取所有配送方式
	public List<DeliveryType> getAllDeliveryType() {
		return DeliveryType.dao.getAll();
	}
	
	// 获取所有物流公司
	public List<DeliveryCorp> getAllDeliveryCorp() {
		return DeliveryCorp.dao.getAll();
	}
}
