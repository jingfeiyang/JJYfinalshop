package com.jfinalshop.model;

import java.math.BigDecimal;
import java.util.Date;

import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;
import com.jfinalshop.util.SystemConfigUtil;

/**
 * 实体类 - 购物车项
 * 
 */
public class CartItem extends Model<CartItem>{

	private static final long serialVersionUID = -2752867902533206652L;
	public static final CartItem dao = new CartItem();

	// 商品
	public Product getProduct() {
		return Product.dao.findById(getStr("product_id"));
	}
	
	// 用户
	public Member getMember() {
		return Member.dao.findById(getStr("member_id"));
	}
	
	//重写save
	public boolean save(CartItem cartItem){
		cartItem.set("id", CommonUtil.getUUID());
		cartItem.set("createDate", new Date());
		return cartItem.save();
	}
		
	// 获取优惠价格，若member对象为null则返回原价格
	public BigDecimal getPreferentialPrice() {
		Member member = getMember();
		Product product = getProduct();
		if (member != null) {
			BigDecimal preferentialPrice = product.getBigDecimal("price").multiply(new BigDecimal(member.getMemberRank().getDouble("preferentialScale").toString()).divide(new BigDecimal("100")));
			return SystemConfigUtil.getPriceScaleBigDecimal(preferentialPrice);
		} else {
			return product.getBigDecimal("price");
		}
	}
	
	// 获取小计价格
	public BigDecimal getSubtotalPrice() {
		BigDecimal subtotalPrice = getPreferentialPrice().multiply(new BigDecimal(getInt("quantity").toString()));
		return SystemConfigUtil.getOrderScaleBigDecimal(subtotalPrice);
	}
}
