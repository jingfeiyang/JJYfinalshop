package com.jfinalshop.controller.shop;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Page;
import com.jfinalshop.bean.SystemConfig.PointType;
import com.jfinalshop.bean.SystemConfig.StoreFreezeTime;
import com.jfinalshop.model.Area;
import com.jfinalshop.model.CartItem;
import com.jfinalshop.model.DeliveryType;
import com.jfinalshop.model.DeliveryType.DeliveryMethod;
import com.jfinalshop.model.OrderItem;
import com.jfinalshop.model.OrderLog;
import com.jfinalshop.model.OrderLog.OrderLogType;
import com.jfinalshop.model.Orders;
import com.jfinalshop.model.Orders.OrderStatus;
import com.jfinalshop.model.Orders.PaymentStatus;
import com.jfinalshop.model.Orders.ShippingStatus;
import com.jfinalshop.model.PaymentConfig;
import com.jfinalshop.model.Product;
import com.jfinalshop.model.Product.WeightUnit;
import com.jfinalshop.model.Receiver;
import com.jfinalshop.service.HtmlService;
import com.jfinalshop.util.ArithUtil;
import com.jfinalshop.util.CommonUtil;
import com.jfinalshop.util.SerialNumberUtil;
import com.jfinalshop.util.SystemConfigUtil;
import com.jfinalshop.validator.shop.OrderValidator;

/**
 * 前台类 - 订单处理
 * 
 */
@ControllerBind(controllerKey = "/shop/order")
public class OrderController extends BaseShopController<Orders>{
	
	private Boolean isSaveReceiver;// 是否保存收货地址
	private Integer totalQuantity;// 商品总数
	private Integer totalPoint;// 总积分
	private Double totalWeightGram;// 商品总重量（单位：g）
	private BigDecimal productTotalPrice;// 总计商品价格
	private String memo;// 附言
	
	private List<CartItem> cartItemList;// 购物车项
	private DeliveryType deliveryType;// 配送方式
	private PaymentConfig paymentConfig;// 支付方式
	private Orders order;// 订单
	private Receiver receiver;
	
	// 订单列表
	public void list(){
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",Orders.DEFAULT_ORDER_LIST_PAGE_SIZE);
		
		Page<Orders> pager = Orders.dao.getOrderPager(pageNumber,pageSize,getLoginMember().getStr("id"));
		setAttr("pager", pager);
		render("/shop/order_list.html");
	}

	// 订单信息
	public void info() {
		cartItemList = getLoginMember().getCartItemList();
		if (cartItemList == null || cartItemList.size() == 0) {
			addActionError("购物车目前没有加入任何商品！");
			return;
		}
		for (CartItem cartItem : cartItemList) {
			Product product = cartItem.getProduct();
			if (product.getInt("store") != null && (cartItem.getInt("quantity") + product.getInt("freezeStore")) > product.getInt("store")) {
				addActionError("商品库存不足，请返回修改！");
				return;
			}
		}
		totalQuantity = 0;
		totalPoint = 0;
		totalWeightGram = 0D;
		productTotalPrice = new BigDecimal("0");
		for (CartItem cartItem : cartItemList) {
			totalQuantity += cartItem.getInt("quantity");
			if (getSystemConfig().getPointType() == PointType.productSet) {
				totalPoint = cartItem.getProduct().getInt("point") * cartItem.getInt("quantity") + totalPoint;
			}
			productTotalPrice = cartItem.getProduct().getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(cartItem.getInt("quantity").toString())).add(productTotalPrice);
			Product product = cartItem.getProduct();
			Double weightGram = DeliveryType.toWeightGram(product.getDouble("weight"), product.getWeightUnit());
			totalWeightGram = ArithUtil.add(totalWeightGram, ArithUtil.mul(weightGram, cartItem.getInt("quantity")));
		}
		productTotalPrice = SystemConfigUtil.getOrderScaleBigDecimal(productTotalPrice);
		if (getSystemConfig().getPointType() == PointType.orderAmount) {
			totalPoint = productTotalPrice.multiply(new BigDecimal(getSystemConfig().getPointScale().toString())).setScale(0, RoundingMode.DOWN).intValue();
		}
		setAttr("loginMember", getLoginMember());
		setAttr("totalQuantity", totalQuantity);
		setAttr("totalPoint", totalPoint);
		setAttr("totalWeightGram", totalWeightGram);
		setAttr("productTotalPrice", productTotalPrice);
		setAttr("allDeliveryType", DeliveryType.dao.getAll());
		setAttr("allPaymentConfig", PaymentConfig.dao.getAll());
		setAttr("cartItemList", cartItemList);
		render("/shop/order_info.html");
	}
	
	// 订单保存
	@Before(OrderValidator.class)
	public void save() {
		isSaveReceiver = getParaToBoolean("isSaveReceiver");
		String paymentConfig_id = getPara("paymentConfig_id","");
		String deliveryType_id = getPara("deliveryType_id","");
		 
		memo = getPara("memo");
		receiver = getModel(Receiver.class);
		
		cartItemList = getLoginMember().getCartItemList();
		if (cartItemList == null || cartItemList.size() == 0) {
			addActionError("购物车目前没有加入任何商品！");
			return;
		}
		if (StringUtils.isNotEmpty(receiver.getStr("id"))) {
			receiver = Receiver.dao.findById(receiver.getStr("id"));
			if (Area.dao.getAreaString(receiver.getStr("areaPath")) == null) {
				addActionError("收货地址信息不完整，请补充收货地址信息！");
				redirectionUrl = "receiver/edit?id=" + receiver.getStr("id");
				return;
			}
		} else {
			if (StringUtils.isEmpty(receiver.getStr("name"))) {
				addActionError("收货人不允许为空！");
				return;
			}
			if (StringUtils.isEmpty(receiver.getStr("areaPath"))) {
				addActionError("地区不允许为空！");
				return;
			}
			if (StringUtils.isEmpty(receiver.getStr("address"))) {
				addActionError("联系地址不允许为空！");
				return;
			}
			if (StringUtils.isEmpty(receiver.getStr("zipCode"))) {
				addActionError("邮编不允许为空！");
				return;
			}
			if (StringUtils.isEmpty(receiver.getStr("phone")) && StringUtils.isEmpty(receiver.getStr("mobile"))) {
				addActionError("联系电话、联系手机必须填写其中一项！");
				return;
			}
			if (!Area.dao.isAreaPath(receiver.getStr("areaPath"))) {
				addActionError("地区错误！");
				return;
			}
			if (isSaveReceiver == null) {
				addActionError("是否保存不允许为空！");
				return;
			}
			if (isSaveReceiver) {
				receiver.set("isDefault",false);
				receiver.set("member_id",getLoginMember().getStr("id"));
				receiver.set("id", CommonUtil.getUUID());
				receiver.set("createDate", new Date());
				receiver.save();
			}
		}
		for (CartItem cartItem : cartItemList) {
			Product product = cartItem.getProduct();
			if (product.getInt("store") != null && (cartItem.getInt("quantity") + product.getInt("freezeStore") > product.getInt("store"))) {
				addActionError("商品[" + product.getStr("name") + "]库存不足！");
				return;
			}
		}
		deliveryType = DeliveryType.dao.findById(deliveryType_id);
		if (deliveryType.getDeliveryMethod() == DeliveryMethod.deliveryAgainstPayment && (StringUtils.isEmpty(paymentConfig_id))) {
			addActionError("请选择支付方式！");
			return;
		}
		totalQuantity = 0;
		productTotalPrice = new BigDecimal("0");
		totalWeightGram = 0D;
		for (CartItem cartItem : cartItemList) {
			Product product = cartItem.getProduct();
			totalQuantity += cartItem.getInt("quantity");
			productTotalPrice = cartItem.getProduct().getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(cartItem.getInt("quantity").toString())).add(productTotalPrice);
			Double weightGram = DeliveryType.toWeightGram(product.getDouble("weight"), product.getWeightUnit());
			totalWeightGram = ArithUtil.add(totalWeightGram, ArithUtil.mul(weightGram, cartItem.getInt("quantity")));
			cartItem.delete();
		}
		productTotalPrice = SystemConfigUtil.getOrderScaleBigDecimal(productTotalPrice);
		BigDecimal deliveryFee = deliveryType.getDeliveryFee(totalWeightGram);
		
		String paymentConfigName = null;
		BigDecimal paymentFee = null;
		if (deliveryType.getDeliveryMethod() == DeliveryMethod.deliveryAgainstPayment) {
			paymentConfig = PaymentConfig.dao.findById(paymentConfig_id);
			paymentConfigName = paymentConfig.getStr("name");
			paymentFee = paymentConfig.getPaymentFee(productTotalPrice.add(deliveryFee));
		} else {
			paymentConfig = null;
			paymentConfigName = "货到付款";
			paymentFee = new BigDecimal("0");
		}
		
		BigDecimal totalAmount = productTotalPrice.add(deliveryFee).add(paymentFee);
		String orderSn = SerialNumberUtil.buildOrderSn();
		order = new Orders();
		order.set("orderSn", orderSn);
		order.set("orderStatus",OrderStatus.valueOf(OrderStatus.unprocessed.name()).ordinal());
		order.set("paymentStatus",PaymentStatus.valueOf(PaymentStatus.unpaid.name()).ordinal());
		order.set("shippingStatus",ShippingStatus.valueOf(ShippingStatus.unshipped.name()).ordinal());
		order.set("deliveryTypeName",deliveryType.getStr("name"));
		order.set("paymentConfigName",paymentConfigName);
		order.set("productTotalPrice",productTotalPrice);
		order.set("deliveryFee",deliveryFee);
		order.set("paymentFee",paymentFee);
		order.set("totalAmount",totalAmount);
		order.set("paidAmount",new BigDecimal("0"));
		if (totalWeightGram < 1000) {
			order.set("productWeight",totalWeightGram);
			order.set("productWeightUnit",WeightUnit.valueOf(WeightUnit.g.name()).ordinal());
		} else if(totalWeightGram >= 1000 && totalWeightGram < 1000000) {
			order.set("productWeight",totalWeightGram / 1000);
			order.set("productWeightUnit",WeightUnit.valueOf(WeightUnit.kg.name()).ordinal());
		} else if(totalWeightGram >= 1000000) {
			order.set("productWeight",totalWeightGram / 1000000);
			order.set("productWeightUnit",WeightUnit.valueOf(WeightUnit.t.name()).ordinal());
		}
		order.set("productTotalQuantity",totalQuantity);
		order.set("shipName",receiver.getStr("name"));
		order.set("shipArea",Area.dao.getAreaString(receiver.getStr("areaPath")));
		order.set("shipAreaPath",receiver.getStr("areaPath"));
		order.set("shipAddress",receiver.getStr("address"));
		order.set("shipZipCode",receiver.getStr("zipCode"));
		order.set("shipPhone",receiver.getStr("phone"));
		order.set("shipMobile",receiver.getStr("mobile"));
		order.set("memo",memo);
		order.set("member_id",getLoginMember().getStr("id"));
		order.set("deliveryType_id",deliveryType_id);
		order.set("paymentConfig_id",paymentConfig_id);
		saved(order);
		
		// 商品项
		for (CartItem cartItem : cartItemList) {
			Product product = cartItem.getProduct();
			OrderItem orderItem = new OrderItem();
			orderItem.set("productSn",product.getStr("productSn"));
			orderItem.set("productName",product.getStr("name"));
			orderItem.set("productPrice",product.getPreferentialPrice(getLoginMember()));
			orderItem.set("productQuantity",cartItem.getInt("quantity"));
			orderItem.set("deliveryQuantity",0);
			orderItem.set("totalDeliveryQuantity",0);
			orderItem.set("productHtmlFilePath",product.getStr("htmlFilePath"));
			orderItem.set("order_id",order.getStr("id"));
			orderItem.set("product_id",product.getStr("id"));
			orderItem.save(orderItem);
		}
		
		// 库存处理
		if (getSystemConfig().getStoreFreezeTime() == StoreFreezeTime.order) {
			for (CartItem cartItem : cartItemList) {
				Product product = cartItem.getProduct();
				if (product.getInt("store") != null) {
					product.set("freezeStore",product.getInt("freezeStore") + cartItem.getInt("quantity"));
					if (product.getIsOutOfStock()) {
						cartItem.getProduct().getProductAttributeMapStore();
					}
					product.update();
					if (product.getIsOutOfStock()) {
						HtmlService.service.productContentBuildHtml(product);
					}
				}
			}
		}
		
		// 订单日志
		OrderLog orderLog = new OrderLog();
		orderLog.set("orderLogType",OrderLogType.valueOf(OrderLogType.create.name()).ordinal());
		orderLog.set("orderSn",order.getStr("orderSn"));
		orderLog.set("operator",null);
		orderLog.set("info",null);
		orderLog.set("order_id",order.getStr("id"));
		orderLog.save(orderLog);		
		setAttr("order", order);
		render("/shop/order_result.html");
	}
	
	// 订单详情
	public void view() {
		id = getPara("id","");
		order = Orders.dao.findById(id);
		totalPoint = 0;
		if (getSystemConfig().getPointType() == PointType.productSet) {
			for (OrderItem orderItem : order.getOrderItemList()) {
				totalPoint = orderItem.getProduct().getInt("point") * orderItem.getInt("productQuantity") + totalPoint;
			}
		} else if (getSystemConfig().getPointType() == PointType.orderAmount) {
			totalPoint = order.getBigDecimal("productTotalPrice").multiply(new BigDecimal(getSystemConfig().getPointScale().toString())).setScale(0, RoundingMode.DOWN).intValue();
		}
		setAttr("order", order);
		render("/shop/order_view.html");
	}
	
}
