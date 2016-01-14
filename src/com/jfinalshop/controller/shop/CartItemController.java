package com.jfinalshop.controller.shop;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.StrKit;
import com.jfinalshop.bean.CartItemCookie;
import com.jfinalshop.bean.SystemConfig.PointType;
import com.jfinalshop.interceptor.NavigationInterceptor;
import com.jfinalshop.model.CartItem;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.Product;
import com.jfinalshop.util.CommonUtil;
import com.jfinalshop.util.SystemConfigUtil;

/**
 * 前台类 - 购物车项
 * 
 */
@Before(NavigationInterceptor.class)
@ControllerBind(controllerKey = "/shop/cartItem")
public class CartItemController extends BaseShopController<CartItem>{

	private Product product;
	private Integer quantity;// 商品数量
	private Integer totalQuantity;// 商品总数
	private Integer totalPoint;// 总积分
	private BigDecimal totalPrice;// 总计商品价格
	private List<CartItem> cartItemList = new ArrayList<CartItem>();
	
	// 购物车项列表
	public void list() {
		Member loginMember = getLoginMember();
		totalQuantity = 0;
		totalPoint = 0;
		totalPrice = new BigDecimal("0");
		if (loginMember == null) {
			Cookie[] cookies = getRequest().getCookies();
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					if (StringUtils.equalsIgnoreCase(cookie.getName(), CartItemCookie.CART_ITEM_LIST_COOKIE_NAME)) {
						if (StringUtils.isNotEmpty(cookie.getValue())) {
							JSONArray jsonArray = JSON.parseArray(cookie.getValue());
							List<CartItemCookie> cartItemCookieList = JSON.parseArray(jsonArray.toJSONString(), CartItemCookie.class);
							for (CartItemCookie cartItemCookie : cartItemCookieList) {
								Product product = Product.dao.findById(cartItemCookie.getI());
								if (product != null) {
									totalQuantity += cartItemCookie.getQ();
									if (getSystemConfig().getPointType() == PointType.productSet) {
										totalPoint = product.getInt("point") * cartItemCookie.getQ() + totalPoint;
									}
									totalPrice = product.getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(cartItemCookie.getQ().toString())).add(totalPrice);
									CartItem cartItem = new CartItem();
									cartItem.set("product_id",product.getStr("id"));
									cartItem.set("quantity",cartItemCookie.getQ());
									cartItemList.add(cartItem);
								}
							}
						}
					}
				}
			}
		} else {
			cartItemList = loginMember.getCartItemList();
			if (cartItemList != null) {
				for (CartItem cartItem : cartItemList) {
					totalQuantity += cartItem.getInt("quantity");
					if (getSystemConfig().getPointType() == PointType.productSet) {
						totalPoint = cartItem.getProduct().getInt("point") * cartItem.getInt("quantity") + totalPoint;
					}
					totalPrice = cartItem.getProduct().getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(cartItem.getInt("quantity").toString())).add(totalPrice);
				}
			}
		}
		totalPrice = SystemConfigUtil.getOrderScaleBigDecimal(totalPrice);
		if (getSystemConfig().getPointType() == PointType.orderAmount) {
			totalPoint = totalPrice.multiply(new BigDecimal(getSystemConfig().getPointScale().toString())).setScale(0, RoundingMode.DOWN).intValue();
		}
		setAttr("totalPoint", totalPoint);
		setAttr("totalPrice", totalPrice);
		setAttr("totalQuantity", totalQuantity);
		setAttr("cartItemList", cartItemList);
		render("/shop/cart_item_list.html");
	}
	
	// 添加购物车项
	public void ajaxAdd() {
		String id = getPara("id","");
		quantity = getParaToInt("quantity",0);		
		if(StrKit.isBlank(id)){
			addActionError("产品ID为空!");
			return;
		}		
		product = Product.dao.findById(id);
		if (product == null && !product.getBoolean("isMarketable")) {
			ajaxJsonErrorMessage("此商品已下架!");
		}
		if (quantity == null || quantity < 1) {
			quantity = 1;
		}
		Integer totalQuantity = 0;// 总计商品数量
		BigDecimal totalPrice = new BigDecimal("0");// 总计商品价格
		Member loginMember = getLoginMember();
		if (loginMember == null) {
			List<CartItemCookie> cartItemCookieList = new ArrayList<CartItemCookie>();
			boolean isExist = false;
			Cookie[] cookies = getRequest().getCookies();
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					if (StringUtils.equalsIgnoreCase(cookie.getName(), CartItemCookie.CART_ITEM_LIST_COOKIE_NAME)) {
						if (StringUtils.isNotEmpty(cookie.getValue())) {
							JSONArray jsonArray = JSON.parseArray(cookie.getValue());
							List<CartItemCookie> previousCartItemCookieList = JSON.parseArray(jsonArray.toJSONString(), CartItemCookie.class);
							
							for (CartItemCookie previousCartItemCookie : previousCartItemCookieList) {
								Product cartItemCookieProduct = Product.dao.findById(previousCartItemCookie.getI());
								if (StringUtils.equals(previousCartItemCookie.getI(), id)) {
									isExist = true;
									previousCartItemCookie.setQ(previousCartItemCookie.getQ() + quantity);
									if (product.getInt("store") != null && (product.getInt("freezeStore") + previousCartItemCookie.getQ()) > product.getInt("store")) {
										ajaxJsonErrorMessage("添加购物车失败，商品库存不足!");
									}
								}
								cartItemCookieList.add(previousCartItemCookie);
								totalQuantity += previousCartItemCookie.getQ();
								totalPrice =  cartItemCookieProduct.getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(previousCartItemCookie.getQ().toString())).add(totalPrice);
							}
						}
					}
				}
			}
			if (!isExist) {
				CartItemCookie cartItemCookie = new CartItemCookie();
				cartItemCookie.setI(id);
				cartItemCookie.setQ(quantity);
				cartItemCookieList.add(cartItemCookie);
				totalQuantity += quantity;
				totalPrice =  product.getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(quantity.toString())).add(totalPrice);
				if (product.getInt("store") != null && (product.getInt("freezeStore") + cartItemCookie.getQ()) > product.getInt("store")) {
					ajaxJsonErrorMessage("添加购物车失败，商品库存不足!");
				}
			}
			for (CartItemCookie cartItemCookie : cartItemCookieList) {
				if (StringUtils.equals(cartItemCookie.getI(), id)) {
					Product cartItemCookieProduct = Product.dao.findById(cartItemCookie.getI());
					if (product.getInt("store") != null && (cartItemCookieProduct.getInt("freezeStore") + cartItemCookie.getQ()) > cartItemCookieProduct.getInt("store")) {
						ajaxJsonErrorMessage("添加购物车失败，商品库存不足!");
					}
				}
			}
			String jsonText = JSON.toJSONString(cartItemCookieList,true);
			Cookie cookie = new Cookie(CartItemCookie.CART_ITEM_LIST_COOKIE_NAME, jsonText);
			cookie.setPath(getRequest().getContextPath() + "/");
			cookie.setMaxAge(CartItemCookie.CART_ITEM_LIST_COOKIE_MAX_AGE);
			getResponse().addCookie(cookie);
		} else {
			boolean isExist = false;
			List<CartItem> previousCartItemList = loginMember.getCartItemList();
			if (previousCartItemList != null) {
				for (CartItem previousCartItem : previousCartItemList) {
					if (StringUtils.equals(previousCartItem.getProduct().getStr("id"), id)) {
						isExist = true;
						previousCartItem.set("quantity",previousCartItem.getInt("quantity") + quantity);
						if (product.getInt("store") != null && (product.getInt("freezeStore") + previousCartItem.getInt("quantity")) > product.getInt("store")) {
							ajaxJsonErrorMessage("添加购物车失败，商品库存不足!");
						}
						previousCartItem.update();
					}
					totalQuantity += previousCartItem.getInt("quantity");
					totalPrice =  previousCartItem.getProduct().getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(previousCartItem.getInt("quantity").toString())).add(totalPrice);
				}
			}
			if (!isExist) {
				CartItem cartItem = new CartItem();
				cartItem.set("id", CommonUtil.getUUID());
				cartItem.set("createDate", new Date());
				cartItem.set("member_id",loginMember.getStr("id"));
				cartItem.set("product_id",product.getStr("id"));
				cartItem.set("quantity",quantity);
				if (product.getInt("store") != null && (product.getInt("freezeStore") + cartItem.getInt("quantity")) > product.getInt("store")) {
					ajaxJsonErrorMessage("添加购物车失败，商品库存不足!");
				}
				cartItem.save();
				totalQuantity += quantity;
				totalPrice =  product.getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(quantity.toString())).add(totalPrice);
			}
		}
		totalPrice = SystemConfigUtil.getOrderScaleBigDecimal(totalPrice);
		DecimalFormat decimalFormat = new DecimalFormat(getOrderUnitCurrencyFormat());
		String totalPriceString = decimalFormat.format(totalPrice);
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put(STATUS, SUCCESS);
		jsonMap.put(MESSAGE, "添加至购物车成功！");
		jsonMap.put("totalQuantity", totalQuantity.toString());
		jsonMap.put("totalPrice", totalPriceString);
		renderJson(jsonMap);
	}
		
		
	// 购物车项列表
	public void ajaxList() {
		List<Map<String, String>> jsonList = new ArrayList<Map<String, String>>();
		Member loginMember = getLoginMember();
		totalQuantity = 0;
		totalPrice = new BigDecimal("0");
		if (loginMember == null) {
			Cookie[] cookies = getRequest().getCookies();
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					if (StringUtils.equalsIgnoreCase(cookie.getName(), CartItemCookie.CART_ITEM_LIST_COOKIE_NAME)) {
						if (StringUtils.isNotEmpty(cookie.getValue())) {
							
							JSONArray jsonArray = JSON.parseArray(cookie.getValue());
							List<CartItemCookie> cartItemCookieList = JSON.parseArray(jsonArray.toJSONString(), CartItemCookie.class);
							for (CartItemCookie cartItemCookie : cartItemCookieList) {
								Product product = Product.dao.findById(cartItemCookie.getI());
								if (product != null) {
									totalQuantity += cartItemCookie.getQ();
									totalPrice = product.getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(cartItemCookie.getQ().toString())).add(totalPrice);
									DecimalFormat decimalFormat = new DecimalFormat(getPriceCurrencyFormat());
									String priceString = decimalFormat.format(product.getPreferentialPrice(getLoginMember()));
									Map<String, String> jsonMap = new HashMap<String, String>();
									jsonMap.put("name", product.getStr("Name"));
									jsonMap.put("price", priceString);
									jsonMap.put("quantity", cartItemCookie.getQ().toString());
									jsonMap.put("htmlFilePath", product.getStr("htmlFilePath"));
									jsonList.add(jsonMap);
								}
							}
						}
					}
				}
			}
		} else {
			List<CartItem> cartItemSet = loginMember.getCartItemList();
			if (cartItemSet != null) {
				for (CartItem cartItem : cartItemSet) {
					Product product = cartItem.getProduct();
					totalQuantity += cartItem.getInt("quantity");
					totalPrice = product.getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(cartItem.getInt("quantity").toString())).add(totalPrice);
					DecimalFormat decimalFormat = new DecimalFormat(getPriceCurrencyFormat());
					String priceString = decimalFormat.format(cartItem.getProduct().getPreferentialPrice(getLoginMember()));
					Map<String, String> jsonMap = new HashMap<String, String>();
					jsonMap.put("name", product.getStr("name"));
					jsonMap.put("price", priceString);
					jsonMap.put("quantity", cartItem.getInt("quantity").toString());
					jsonMap.put("htmlFilePath", cartItem.getProduct().getStr("htmlFilePath"));
					jsonList.add(jsonMap);
				}
			}
		}
		totalPrice = SystemConfigUtil.getOrderScaleBigDecimal(totalPrice);
		DecimalFormat decimalFormat = new DecimalFormat(getOrderUnitCurrencyFormat());
		String totalPriceString = decimalFormat.format(totalPrice);
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("totalQuantity", totalQuantity.toString());
		jsonMap.put("totalPrice", totalPriceString);
		jsonList.add(0, jsonMap);
		renderJson(jsonList);
	}
	
	// 修改购物车项	
	public void ajaxEdit() {
		quantity = getParaToInt("quantity",0);
		String id = getPara("id");
		if (quantity == null || quantity < 1) {
			quantity = 1;
		}
		Member loginMember = getLoginMember();
		totalQuantity = 0;
		totalPoint = 0;
		totalPrice = new BigDecimal("0");
		BigDecimal subtotalPrice = new BigDecimal("0");
		if (loginMember == null) {
			Cookie[] cookies = getRequest().getCookies();
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					if (StringUtils.equalsIgnoreCase(cookie.getName(), CartItemCookie.CART_ITEM_LIST_COOKIE_NAME)) {
						if (StringUtils.isNotEmpty(cookie.getValue())) {
							JSONArray jsonArray = JSON.parseArray(cookie.getValue());
							List<CartItemCookie> cartItemCookieList = JSON.parseArray(jsonArray.toJSONString(), CartItemCookie.class);
							Iterator<CartItemCookie> iterator = cartItemCookieList.iterator();
							while (iterator.hasNext()) {
								CartItemCookie cartItemCookie = iterator.next();
								Product product = Product.dao.findById(cartItemCookie.getI());
								if (StringUtils.equals(id, cartItemCookie.getI())) {
									cartItemCookie.setQ(quantity);
									subtotalPrice = product.getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(quantity));
									if (product.getInt("store") != null && (product.getInt("freezeStore") + cartItemCookie.getQ()) > product.getInt("store")) {
										ajaxJsonErrorMessage("商品库存不足！");
									}
								}
								totalQuantity += cartItemCookie.getQ();
								if (getSystemConfig().getPointType() == PointType.productSet) {
									totalPoint = product.getInt("point") * cartItemCookie.getQ() + totalPoint;
								}
								totalPrice = product.getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(cartItemCookie.getQ().toString())).add(totalPrice);
							}
							String jsonText = JSON.toJSONString(cartItemCookieList,true);
							Cookie newCookie = new Cookie(CartItemCookie.CART_ITEM_LIST_COOKIE_NAME, jsonText);
							newCookie.setPath(getRequest().getContextPath() + "/");
							newCookie.setMaxAge(CartItemCookie.CART_ITEM_LIST_COOKIE_MAX_AGE);
							getResponse().addCookie(newCookie);
						}
					}
				}
			}
		} else {
			cartItemList = loginMember.getCartItemList();
			if (cartItemList != null) {
				for (CartItem cartItem : cartItemList) {
					Product product = cartItem.getProduct();
					if (StringUtils.equals(id, cartItem.getProduct().getStr("id"))) {
						cartItem.set("quantity",quantity);
						if (product.getInt("store") != null && (product.getInt("freezeStore") + cartItem.getInt("quantity")) > product.getInt("store")) {
							ajaxJsonErrorMessage("商品库存不足！");
						}
						cartItem.update();
						subtotalPrice = cartItem.getSubtotalPrice();
					}
					totalQuantity += cartItem.getInt("quantity");
					if (getSystemConfig().getPointType() == PointType.productSet) {
						totalPoint = product.getInt("point") * cartItem.getInt("quantity") + totalPoint;
					}
					totalPrice = product.getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(cartItem.getInt("quantity").toString())).add(totalPrice);
				}
			}
		}
		DecimalFormat orderUnitCurrencyFormat = new DecimalFormat(getOrderUnitCurrencyFormat());
		DecimalFormat orderCurrencyFormat = new DecimalFormat(getOrderCurrencyFormat());
		totalPrice = SystemConfigUtil.getOrderScaleBigDecimal(totalPrice);
		if (getSystemConfig().getPointType() == PointType.orderAmount) {
			totalPoint = totalPrice.multiply(new BigDecimal(getSystemConfig().getPointScale().toString())).setScale(0, RoundingMode.DOWN).intValue();
		}
		String subtotalPriceString = orderCurrencyFormat.format(subtotalPrice);
		String totalPriceString = orderUnitCurrencyFormat.format(totalPrice);
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("subtotalPrice", subtotalPriceString);
		jsonMap.put("totalQuantity", totalQuantity.toString());
		jsonMap.put("totalPoint", totalPoint.toString());
		jsonMap.put("totalPrice", totalPriceString);
		jsonMap.put(STATUS, SUCCESS);
		renderJson(jsonMap);
	}
	
	// 删除购物车项
	public void ajaxDelete() {
		id = getPara("id","");
		Member loginMember = getLoginMember();
		totalQuantity = 0;
		totalPoint = 0;
		totalPrice = new BigDecimal("0");
		if (loginMember == null) {
			Cookie[] cookies = getRequest().getCookies();
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					if (StringUtils.equalsIgnoreCase(cookie.getName(), CartItemCookie.CART_ITEM_LIST_COOKIE_NAME)) {
						if (StringUtils.isNotEmpty(cookie.getValue())) {
							JSONArray previousJsonArray = JSON.parseArray(cookie.getValue());
							List<CartItemCookie> cartItemCookieList = JSON.parseArray(previousJsonArray.toJSONString(), CartItemCookie.class);
							Iterator<CartItemCookie> iterator = cartItemCookieList.iterator();
							while (iterator.hasNext()) {
								CartItemCookie cartItemCookie = iterator.next();
								if (StringUtils.equals(cartItemCookie.getI(), id)) {
									iterator.remove();
								} else {
									Product product = Product.dao.findById(cartItemCookie.getI());
									totalQuantity += cartItemCookie.getQ();
									if (getSystemConfig().getPointType() == PointType.productSet) {
										totalPoint = product.getInt("point") * cartItemCookie.getQ() + totalPoint;
									}
									totalPrice = product.getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(cartItemCookie.getQ().toString())).add(totalPrice);
								}
							}
							
							String jsonText = JSON.toJSONString(cartItemCookieList,true);
							Cookie newCookie = new Cookie(CartItemCookie.CART_ITEM_LIST_COOKIE_NAME, jsonText);
							newCookie.setPath(getRequest().getContextPath() + "/");
							newCookie.setMaxAge(CartItemCookie.CART_ITEM_LIST_COOKIE_MAX_AGE);
							getResponse().addCookie(newCookie);
						}
					}
				}
			}
		} else {
			List<CartItem> cartItemList = loginMember.getCartItemList();
			if (cartItemList != null) {
				for (CartItem cartItem : cartItemList) {
					if (StringUtils.equals(cartItem.getProduct().getStr("id"), id)) {
						cartItem.delete();
					} else {
						Product product = cartItem.getProduct();
						totalQuantity += cartItem.getInt("quantity");
						if (getSystemConfig().getPointType() == PointType.productSet) {
							totalPoint = product.getInt("point") * cartItem.getInt("quantity") + totalPoint;
						}
						totalPrice = product.getPreferentialPrice(getLoginMember()).multiply(new BigDecimal(cartItem.getInt("quantity").toString())).add(totalPrice);
					}
				}
			}
		}
		totalPrice = SystemConfigUtil.getOrderScaleBigDecimal(totalPrice);
		if (getSystemConfig().getPointType() == PointType.orderAmount) {
			totalPoint = totalPrice.multiply(new BigDecimal(getSystemConfig().getPointScale().toString())).setScale(0, RoundingMode.DOWN).intValue();
		}
		DecimalFormat decimalFormat = new DecimalFormat(getOrderUnitCurrencyFormat());
		String totalPriceString = decimalFormat.format(totalPrice);
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("totalQuantity", totalQuantity.toString());
		jsonMap.put("totalPoint", totalPoint.toString());
		jsonMap.put("totalPrice", totalPriceString);
		jsonMap.put(STATUS, SUCCESS);
		jsonMap.put(MESSAGE, "商品删除成功！");
		renderJson(jsonMap);
	}
	
	// 清空购物车项
	public void ajaxClear() {
		Member loginMember = getLoginMember();
		if (loginMember == null) {
			Cookie cookie = new Cookie(CartItemCookie.CART_ITEM_LIST_COOKIE_NAME, null);
			cookie.setPath(getRequest().getContextPath() + "/");
			cookie.setMaxAge(0);
			getResponse().addCookie(cookie);
		} else {
			List<CartItem> cartItemSet = loginMember.getCartItemList();
			if (cartItemSet != null) {
				for (CartItem cartItem : cartItemSet) {
					cartItem.delete();
				}
			}
		}
		ajaxJsonSuccessMessage("购物车清空成功！");
	}
}
