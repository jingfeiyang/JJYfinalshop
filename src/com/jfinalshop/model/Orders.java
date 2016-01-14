package com.jfinalshop.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinalshop.model.Product.WeightUnit;

/**
 * 实体类 - 订单
 * 
 */

public class Orders extends Model<Orders> {

	private static final long serialVersionUID = 2186711390821790352L;
	
	public static final Orders dao = new Orders();

	public static final int DEFAULT_ORDER_LIST_PAGE_SIZE = 15;// 订单列表默认每页显示数

	// 订单状态（未处理、已处理、已完成、已作废）
	public enum OrderStatus {
		unprocessed, processed, completed, invalid
	};

	// 付款状态（未支付、部分支付、已支付、部分退款、全额退款）
	public enum PaymentStatus {
		unpaid, partPayment, paid, partRefund, refunded
	};

	// 配送状态（未发货、部分发货、已发货、部分退货、已退货）
	public enum ShippingStatus {
		unshipped, partShipped, shipped, partReshiped, reshiped
	};
	
	
	/**
	 * 根据Member、Pager获取订单分页对象
	 * 
	 * @return 订单分页对象
	 */
	public Page<Orders> getOrderPager (int pageNumber, int pageSize,String memberId) {
		String select = "select * ";
		String sqlExceptSelect = " from orders where member_id = ? ";
		Page<Orders> pager = dao.paginate(pageNumber, pageSize, select, sqlExceptSelect,memberId);
		return pager;
	}
	
	public OrderStatus getOrderStatus() {
		return OrderStatus.values()[getInt("orderStatus")];
	}
	
	public PaymentStatus getPaymentStatus() {
		return PaymentStatus.values()[getInt("paymentStatus")];
	}
	
	public ShippingStatus getShippingStatus() {
		return ShippingStatus.values()[getInt("shippingStatus")];
	}
	
	public WeightUnit getProductWeightUnit(){
		return WeightUnit.values()[getInt("productWeightUnit")];
	}
	
	// 获取会员
	public Member getMember(){
		return Member.dao.findById(getStr("member_id"));
	}
		
	// 产品项
	public Product getProduct(){
		return OrderItem.dao.getProduct();
	}
		
	public List<Product> getProductItemList(){
		return OrderItem.dao.getProductItemList();
		
	}
	
	public PaymentConfig getPaymentConfig() {
		return PaymentConfig.dao.findById(getStr("paymentConfig_id"));
	}
	
	public DeliveryType getDeliveryType() {
		return  DeliveryType.dao.findById(getStr("deliveryType_id"));
	}
	
	/**
	 * 获取未处理订单数
	 *            
	 * @return 未处理订单数
	 */
	public Long getUnprocessedOrderCount() {
		String sql = "select count(*) from Orders where orderStatus = ?";
		return Db.queryLong(sql,OrderStatus.valueOf(OrderStatus.unprocessed.name()).ordinal());
	}
	
	/**
	 * 获取已支付未发货订单数（不包含已完成或已作废订单）
	 *            
	 * @return 已支付未发货订单数
	 */
	public Long getPaidUnshippedOrderCount() {
		String sql = "select count(*) from Orders  where paymentStatus = ? and shippingStatus = ? and orderStatus != ? and orderStatus != ?";
		return Db.queryLong(sql,PaymentStatus.valueOf(PaymentStatus.paid.name()).ordinal(),ShippingStatus.valueOf(ShippingStatus.unshipped.name()).ordinal(),OrderStatus.valueOf(OrderStatus.completed.name()).ordinal(),OrderStatus.valueOf(OrderStatus.invalid.name()).ordinal());
	}
	
	
	
	/**
	 * 获取最后生成的订单编号
	 * 
	 * @return 订单编号
	 */
	public String getLastOrderSn() {
		String sql = "select * from Orders  order by createDate desc";
		Orders order =  dao.findFirst(sql);
		if (order != null) {
			return order.getStr("orderSn");
		} else {
			return null;
		}
	}
	
	// 收款
	public List<Payment> getPaymentList() {
		String sql = "select * from payment t where t.order_id = ? order by createDate desc";
		return Payment.dao.find(sql,getStr("id"));
	}
	
	// 退款
	public List<Refund> getRefundList() {
		String sql = "select * from refund t where t.order_id = ? order by createDate desc";
		return Refund.dao.find(sql,getStr("id"));
	}
	
	// 订单项
	public List<OrderItem> getOrderItemList() {
		String sql = "select * from orderitem where order_id = ? order by createDate desc";
		return OrderItem.dao.find(sql,getStr("id"));
	}
	
	// 订单日志
	public List<OrderLog> getOrderLogList() {
		String sql = "select * from orderlog where order_id = ? order by createDate desc";
		return OrderLog.dao.find(sql,getStr("id"));
	}
	
	// 发货
	public List<Shipping> getShippingList() {
		String sql = "select * from shipping t where t.order_id = ? order by createDate desc";
		return Shipping.dao.find(sql,getStr("id"));
	}
	
	// 退货
	public List<Reship> getReshipList() {
		String sql = "select * from reship t where t.order_id = ? order by createDate desc";
		return Reship.dao.find(sql,getStr("id"));
	}
}
