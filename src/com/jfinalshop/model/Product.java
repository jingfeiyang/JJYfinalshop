package com.jfinalshop.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinalshop.bean.HtmlConfig;
import com.jfinalshop.bean.ProductImage;
import com.jfinalshop.controller.shop.BaseShopController.OrderType;
import com.jfinalshop.service.ProductImageService;
import com.jfinalshop.util.CommonUtil;
import com.jfinalshop.util.SystemConfigUtil;
import com.jfinalshop.util.TemplateConfigUtil;

/**
 * 实体类 - 商品
 * 
 */
public class Product extends Model<Product>{

	private static final long serialVersionUID = -8178061660099975236L;
	
	public static final Product dao = new Product();

	public static final int MAX_BEST_PRODUCT_LIST_COUNT = 20;// 精品商品列表最大商品数
	public static final int MAX_NEW_PRODUCT_LIST_COUNT = 20;// 新品商品列表最大商品数
	public static final int MAX_HOT_PRODUCT_LIST_COUNT = 20;// 热销商品列表最大商品数
	public static final int DEFAULT_PRODUCT_LIST_PAGE_SIZE = 12;// 商品列表默认每页显示数
	
	
	// 重量单位（克、千克、吨）
	public enum WeightUnit {
		g, kg, t
	}
	
	// 商品属性存储
	private Map<String, String> productAttributeMapStore = new HashMap<String, String>();
	private ProductImageService productImageService = new ProductImageService();
	
	/**
	 * 根据分页对象搜索产品
	 * 
	 * @return 分页对象
	 */
	public Page<Product> search(int pageNumber, int pageSize, String keyword, String orderBy, OrderType orderType) {
		String select = "select * ";
		String sqlExceptSelect = " from product where isMarketable = true ";

		if (StrKit.notBlank(keyword)) {
			sqlExceptSelect += " and name like '%" + keyword + "%'";
		}
		
		if (StrKit.notBlank(orderBy) && StrKit.notBlank(orderType.name())){
			sqlExceptSelect += " order by " + orderBy +" "+ orderType;
		}
				
		Page<Product> pager = dao.paginate(pageNumber, pageSize, select, sqlExceptSelect);
		return pager;
	}
	
	/**
	 * 根据分页对象搜索产品
	 * 
	 * @return 分页对象
	 */
	public Page<Product> categorySearch(int pageNumber, int pageSize, ProductCategory productCategory, String orderBy, OrderType orderType) {
		String select = "select p.* ";
		String sqlExceptSelect ="" 
						 +"  from product p "
						 +" inner join productcategory c " 
						 +"   on p.productcategory_id = c.id " 
						 +" where (p.productcategory_id = ? or c.path like ?) " 
						 +"  and p.ismarketable = ? ";
		
		if (StrKit.notBlank(orderBy) && StrKit.notBlank(orderType.name())){
			sqlExceptSelect += " order by " + orderBy +" "+ orderType;
		}
		Page<Product> pager = dao.paginate(pageNumber, pageSize, select, sqlExceptSelect,productCategory.getStr("id"),productCategory.getStr("path") + "%",true);
		return pager;
	}
	
	/**
	 * 根据Member、Pager获取收藏商品分页对象
	 * 
	 * @param member
	 *            Member对象
	 *            
	 * @return 收藏商品分页对象
	 */
	public Page<Product> getFavoriteProductPager(Member member,int pageSize) {
		String select = "SELECT p.*,mp.favoriteMemberSet_id ";
		String sqlExceptSelect = "" 
						 +"  from product p"
						 +" inner join member_product mp" 
						 +"   on p.id = mp.favoriteproductset_id" 
						 +" inner join member m" 
						 +"   on mp.favoritememberset_id = m.id" 
						 +" where m.id = ?" 
						 +" order by p.name desc, p.createdate desc";
		
		Page<Product> list = dao.paginate(1, pageSize, select, sqlExceptSelect,member.getStr("id"));
		return list;
	}
	
	// 重写方法，保存对象的同时处理价格精度并生成HTML静态文件
	public void save(Product product) {
		HtmlConfig htmlConfig = TemplateConfigUtil.getHtmlConfig(HtmlConfig.PRODUCT_CONTENT);
		String htmlFilePath = htmlConfig.getHtmlFilePath();
		product.set("id", CommonUtil.getUUID());
		product.set("htmlFilePath",htmlFilePath);
		product.set("createDate", new Date());
		product.save();
	}
		
	// get产品分类
	public ProductCategory getProductCategory(){
		return ProductCategory.dao.findById(getStr("productCategory_id"));		
	}
	
	// get 品牌
	public Brand getBrand(){
		return Brand.dao.findById(getStr("brand_id"));		
	}
	
	// 产品类型
	public ProductType getProductType() {
		return ProductType.dao.findById(getStr("productType_id"));
	}
	
	/**
	 * 根据SN判断数据是否已存在.
	 * 
	 */
	public boolean isExist(String productSn){
		return dao.findFirst("select * from product t where t.productSn = ?",productSn) != null;		
	}
	
	/**
	 * 根据最大返回数获取最新商品(只包含isMarketable=true的对象，不限分类)
	 * 
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 最新商品集合
	 */
	public List<Product> getNewProductList(int maxResults) {
		String sql = "select * from Product  where isMarketable = ? and isNew = ? order by product.createDate desc limit 0,?";
		return dao.find(sql,true,true,maxResults);
	}

	/**
	 * 根据ProductCategory对象和最大返回数获取此分类下的最新商品(只包含isMarketable=true的对象，包含子分类商品)
	 * 
	 * @param productCategory
	 *            商品分类
	 * 
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 此分类下的最新商品集合
	 */
	public List<Product> getNewProductList(String  productCategory_id, int maxResults) {
		String sql =  "" 
			 +"select *"
			 +"  from product t" 
			 +" where t.ismarketable = ?" 
			 +"   and t.isnew = ?" 
			 +"   and t.productcategory_id = ?" 
			 +" order by t.createdate desc limit 0, ?";
		return dao.find(sql,true,true,productCategory_id,maxResults);
	}
	
	/**
	 * 根据最大返回数获取所有热卖商品(只包含isMarketable=true的对象，不限分类)
	 * 
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 所有热卖商品集合
	 */
	public List<Product> getHotProductList(int maxResults) {
		String sql = "select * from Product  where isMarketable = ? and isHot = ? order by product.createDate desc limit 0,?";
		return dao.find(sql,true,true,maxResults);
	}

	/**
	 * 根据ProductCategory对象和最大返回数获取此分类下的所有热卖商品(只包含isMarketable=true的对象，包含子分类商品)
	 * 
	 * @param productCategory
	 *            商品分类
	 * 
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 此分类下的所有热卖商品集合
	 */
	public List<Product> getHotProductList(String productCategory_id, int maxResults) {
		String sql =  "" 
				 +"select *"
				 +"  from product t" 
				 +" where t.ismarketable = ?" 
				 +"   and t.ishot = ?" 
				 +"   and t.productcategory_id = ?" 
				 +" order by t.createdate desc limit 0, ?";

		return dao.find(sql,true,true,productCategory_id,maxResults);
	}
	
	/**
	 * 根据最大返回数获取所有精品商品(只包含isMarketable=true的对象，不限分类)
	 * 
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 所有精品商品集合
	 */
	public List<Product> getBestProductList(int maxResults) {
		String sql = "select * from Product  where isMarketable = ? and isBest = ? order by product.createDate desc limit 0,?";
		return dao.find(sql,true,true,maxResults);
	}

	/**
	 * 根据ProductCategory对象和最大返回数获取此分类下的所有精品商品(只包含isMarketable=true的对象，包含子分类商品)
	 * 
	 * @param productCategory
	 *            商品分类
	 *            
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 此分类下的所有精品商品集合
	 */
	public List<Product> getBestProductList(String productCategory_id, int maxResults) {
		String sql = "" 
				 +"select *"
				 +"  from product t" 
				 +" where t.ismarketable = ?" 
				 +"   and t.isbest = ?" 
				 +"   and t.productcategory_id = ?" 
				 +" order by t.createdate desc limit 0, ?";
		return dao.find(sql,true,true,productCategory_id,maxResults);
	}
	
	/**
	 * 根据ProductCategory对象，获取此分类下的所有商品（只包含isMarketable=true的对象，包含子分类商品）
	 * 
	 * @param productCategory
	 *            商品分类
	 * 
	 * @return 此分类下的所有商品集合
	 */
	public List<Product> getProductList(ProductCategory productCategory) {
		String sql = "select * from  product where isMarketable = ? and productCategory_id =? order by createDate desc";
		return dao.find(sql,true,productCategory.getStr("id"));
	}
	
	/**
	 * 根据ProductCategory对象、起始结果数、最大结果数，获取此分类下的所有商品（只包含isMarketable=true的对象，包含子分类商品）
	 * 
	 * @param productCategory
	 *            商品分类
	 *            
	 * @param firstResult
	 *            起始结果数
	 *            
	 * @param maxResults
	 *            最大结果数
	 * 
	 * @return 此分类下的所有商品集合
	 */
	public List<Product> getProductList(ProductCategory productCategory, int firstResult, int maxResults) {
		String sql = "" 
				 +"select p.*"
				 +"  from product p, productcategory pc " 
				 +" where p.productcategory_id = pc.id " 
				 +"   and p.ismarketable = ? " 
				 +"   and (pc.path like ?) " 
				 +" order by p.createdate desc limit ?,?";

		return dao.find(sql,true,productCategory.getStr("path") + "%",firstResult,maxResults);
	}
	
	/**
	 * 根据起始结果数、最大结果数，获取所有商品（只包含isMarketable=true的对象）
	 *            
	 * @param firstResult
	 *            起始结果数
	 *            
	 * @param maxResults
	 *            最大结果数
	 * 
	 * @return 此分类下的所有商品集合
	 */
	public List<Product> getProductList(int firstResult, int maxResults) {
		String sql = "select * from Product where isMarketable = ? order by createDate desc limit ?,?";
		return dao.find(sql, true, firstResult, maxResults);
	}
	
	/**
	 * 根据起始日期、结束日期、起始结果数、最大结果数，获取商品集合（只包含isMarketable=true的对象）
	 * 
	 * @param beginDate
	 *            起始日期，为null则不限制起始日期
	 *            
	 * @param endDate
	 *            结束日期，为null则不限制结束日期
	 *            
	 * @param firstResult
	 *            起始结果数
	 *            
	 * @param maxResults
	 *            最大结果数
	 * 
	 * @return 此分类下的所有商品集合
	 */
	public List<Product> getProductList(Date beginDate, Date endDate, int firstResult, int maxResults) {
		if (beginDate != null && endDate == null) {
			String sql = "select * from Product where isMarketable = ? and createDate > ? order by createDate desc limit ?,?";
			return dao.find(sql,true,beginDate,firstResult,maxResults);
		} else if (endDate != null && beginDate == null) {
			String sql = "select * from Product  where isMarketable = ? and createDate < ? order by createDate desc limit ?,?";
			return dao.find(sql, true, endDate, firstResult, maxResults);
		} else if (endDate != null && beginDate != null) {
			String sql = "select * from Product where isMarketable = ? and createDate > ? and createDate < ? order by product.createDate desc limit ?,?";
			return dao.find(sql, true, beginDate, endDate, firstResult, maxResults);
		} else {
			String sql = "select *from Product  where isMarketable = ? order by createDate desc limit ?,?";
			return dao.find(sql, true, firstResult, maxResults);
		}
	}
	
	public WeightUnit getWeightUnit(){
		return WeightUnit.values()[getInt("weightUnit")];    	
    }
	
	/**
	 * 根据商品图片ID获取商品图片，未找到则返回null
	 * 
	 * @param ProductImage
	 *            ProductImage对象
	 */
	public ProductImage getProductImage(String productImageId) {
		List<ProductImage> productImageList = getProductImageList();
		for (ProductImage productImage : productImageList) {
			if (StringUtils.equals(productImageId, productImage.getId())) {
				return productImage;
			}
		}
		return null;
	}
	
	/**
	 * 商品是否缺货
	 */
	public boolean getIsOutOfStock() {
		if (getInt("store") != null && getInt("freezeStore") >= getInt("store")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 获取优惠价格，若member对象为null则返回原价格
	 */
	public BigDecimal getPreferentialPrice(Member member) {
		if (member != null) {
			BigDecimal preferentialPrice = getBigDecimal("price").multiply(new BigDecimal(member.getMemberRank().getDouble("preferentialScale").toString()).divide(new BigDecimal("100")));
			return SystemConfigUtil.getPriceScaleBigDecimal(preferentialPrice);
		} else {
			return getBigDecimal("price");
		}
	}
	
	/**
	 * 获取商品图片
	 * @return
	 */
	public List<ProductImage> getProductImageList() {
		String productImageListStore = getStr("productImageListStore");
		if (StringUtils.isEmpty(productImageListStore)) {
			return null;
		}		
		List<ProductImage> list = JSON.parseArray(productImageListStore, ProductImage.class); 		
 		return list;
	}
	
	/**
	 * 设置商品图片
	 * @param productImageList
	 */
	public void setProductImageList(List<ProductImage> productImageList) {
		if (productImageList == null || productImageList.size() == 0) {
			set("productImageListStore", null);
			return;
		}
		String jsonText = JSON.toJSONString(productImageList, true);
		set("productImageListStore", jsonText);
	}
		
	/**
	 * 获取属性
	 * @return
	 */
	public Map<String, String> getProductAttributeMap() {
		String sql = "select * from product_productattributemapstore where product_id = ?";
		List<ProductAttributeMapStore> productAttributeList = ProductAttributeMapStore.dao.find(sql,getStr("id"));
		
		if (productAttributeList != null && productAttributeList.size() > 0){
			for(ProductAttributeMapStore productAttribute:productAttributeList){
				productAttributeMapStore.put(productAttribute.getStr("mapkey_id"), productAttribute.getStr("element"));
			}
		}
		return productAttributeMapStore;
	}
		
	/**
	 * 设置商品属性
	 * @param productAttributeMap
	 */
	public void setProductAttributeMap(Map<String, String> productAttributeMap) {
		if (productAttributeMap == null || productAttributeMap.size() == 0) {
			return;
		}
		// 先删除已存的
		Db.deleteById("product_productattributemapstore", "product_id", getStr("id"));
		ProductAttributeMapStore productAttribute = new ProductAttributeMapStore();
		for (Entry<String, String> entry: productAttributeMap.entrySet()) {
			productAttribute.set("product_id", getStr("id"));
			productAttribute.set("mapkey_id", entry.getKey());
			productAttribute.set("element", entry.getValue());
			productAttribute.save();
		}
	}
	
	/**
	 * 收藏夹
	 * @return
	 */
	public List<MemberProduct> getFavoriteMemberList(){
		String sql ="select * from member_product where favoriteProductSet_id = ?";
		return MemberProduct.dao.find(sql,getStr("id"));		
	}
	
	/**
	 * 订单项
	 * @return
	 */
	public List<OrderItem> getOrderItemList() {
		String sql = "select * from orderitem t where t.product_id = ?";
		return OrderItem.dao.find(sql,getStr("id"));
	}
	
	/**
	 * 购物车项
	 * @return
	 */
	public List<CartItem> getCartItemList() {
		String sql = "select * from cartitem t where t.product_id = ?";
		return CartItem.dao.find(sql,getStr("id"));
	}
	
	/**
	 * 物流项
	 * @return
	 */
	public List<DeliveryItem> getDeliveryItemList() {
		String sql = "select * from deliveryitem t where t.product_id = ?";
		return DeliveryItem.dao.find(sql,getStr("id"));
	}
	
	/**
	 * 商品属性存储
	 * @return
	 */
	public List<ProductAttributeMapStore> getProductAttributeMapStore() {
		String sql = "select * from product_productattributemapstore t where t.product_id = ?";
		return ProductAttributeMapStore.dao.find(sql,getStr("id"));
	}
	
	/**
	 * 获取商品库存报警数
	 *            
	 * @return 商品库存报警数
	 */
	public Long getStoreAlertCount() {
		String sql = "select count(*) from Product  where isMarketable = ? and store - freezeStore <= ?";
		return Db.queryLong(sql, true, SystemConfigUtil.getSystemConfig().getStoreAlertCount());
	}
	
	/**
	 * 获取已上架商品数
	 *            
	 * @return 已上架商品数
	 */
	public Long getMarketableProductCount() {
		String sql = "select count(*) from Product  where isMarketable = ?";
		return Db.queryLong(sql,true);
	}
	
	/**
	 * 获取已下架商品数
	 *            
	 * @return 已下架商品数
	 */
	public Long getUnMarketableProductCount() {
		String sql = "select count(*) from Product  where isMarketable = ?";
		return Db.queryLong(sql,false);
	}
	
	/**
	 *  关联处理
	 * @param product
	 * @return
	 */
	public boolean delete(Product product) {
		List<MemberProduct> favoriteMemberList = product.getFavoriteMemberList();
		if (favoriteMemberList != null && favoriteMemberList.size() > 0) {
			for (MemberProduct favoriteMember : favoriteMemberList) {
				favoriteMember.delete();
			}
		}
		List<OrderItem> orderItemList = product.getOrderItemList();
		if (orderItemList != null && orderItemList.size() > 0) {
			for (OrderItem orderItem : orderItemList) {
				orderItem.delete();
			}
		}
		List<DeliveryItem> deliveryItemList = product.getDeliveryItemList();
		if (deliveryItemList != null && deliveryItemList.size() > 0) {
			for (DeliveryItem deliveryItem : deliveryItemList) {
				deliveryItem.delete();
			}
		}
		
		List<CartItem> cartItemList = product.getCartItemList();
		if (cartItemList != null && cartItemList.size() > 0) {
			for (CartItem cartItem : cartItemList) {
				cartItem.delete();
			}
		}
		List<ProductImage> persistentProductImageList = product.getProductImageList();
		if (persistentProductImageList != null && persistentProductImageList.size() > 0) {
			for (ProductImage persistentProductImage : persistentProductImageList) {
				productImageService.deleteFile(persistentProductImage);
			}
		}
		// 先删除已存的
		Db.deleteById("product_productattributemapstore", "product_id", product.getStr("id"));
		return product.delete();
	}
}
