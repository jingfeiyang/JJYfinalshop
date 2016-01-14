package com.jfinalshop.controller.admin;


import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.upload.UploadFile;
import com.jfinalshop.bean.ProductImage;
import com.jfinalshop.bean.SystemConfig;
import com.jfinalshop.bean.SystemConfig.PointType;
import com.jfinalshop.model.Brand;
import com.jfinalshop.model.OrderItem;
import com.jfinalshop.model.Orders.OrderStatus;
import com.jfinalshop.model.Product;
import com.jfinalshop.model.Product.WeightUnit;
import com.jfinalshop.model.ProductAttribute;
import com.jfinalshop.model.ProductAttribute.AttributeType;
import com.jfinalshop.model.ProductCategory;
import com.jfinalshop.model.ProductType;
import com.jfinalshop.service.HtmlService;
import com.jfinalshop.service.ProductImageService;
import com.jfinalshop.util.SerialNumberUtil;
import com.jfinalshop.validator.admin.ProductValidator;

/**
 * 后台类 - 商品 
 * 
 */

public class ProductController extends BaseAdminController<Product>{
	private List<UploadFile> productImages;
	private String[] productImageParameterTypes;
	private String[] productImageIds;
	
	private Product product = new Product();
	
	Map<String, String> productAttributeMap = new HashMap<String, String>();
	
	// 列表
	public void list() {
		findByPage();
		render("/admin/product_list.html");
	}
	
	// 添加
	public void add() {
		setAttr("productCategoryTreeList", getProductCategoryTreeList());
		setAttr("allBrand", getAllBrand());
		setAttr("allWeightUnit", getAllWeightUnit());
		setAttr("allProductType", getAllProductType());
		render("/admin/product_input.html");
	}
		
	public void getProduct() {
		String select = "select * ";
		String sqlExceptSelect = " from product ";
		
		Page<Product> product = Product.dao.paginate(1, 20, select, sqlExceptSelect);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("page", product.getPageNumber());
		map.put("total", product.getTotalPage()); 
		map.put("records", product.getTotalRow());
		map.put("rows", product.getList());		
		renderJson(map);
	}
	
	public void findProduct() {
		// 获取请求次数
		int draw = getParaToInt("draw", 0);
		// 数据起始位置
		int start = getParaToInt("start", 0);
		// 数据长度
		int pageSize = getParaToInt("length", 10);
		// 页码
		int pageNumber = 1;

		if (0 < start) {
			pageNumber = (start / pageSize) + 1;
		}
		//定义列名
	    String[] cols = {"productSn", "name", "price", "isMarketable", "isBest", "isNew", "isHot", "store", "modifyDate"};
	    
	    //获取客户端需要那一列排序
	    int orderColumn = getParaToInt("order[0][column]", 0);
	    String orderName = cols[orderColumn];
	    
        //获取排序方式 默认为asc
		String orderDir = getPara("order[0][dir]", "asc");
        
		//获取用户过滤框里的字符
		String searchValue = getPara("search[value]", "");
		
		String select = "select * ";
		String sqlExceptSelect = " from product ";
		
		// 全局搜索
		if (StrKit.notBlank(searchValue)) {
			sqlExceptSelect += " where ";
			List<String> sArray = new ArrayList<String>();
			for (String col : cols) {
				sArray.add(col + " like '%" + searchValue + "%'");
			}
			
			for (int i = 0; i < sArray.size() - 1; i++) {
				sqlExceptSelect += sArray.get(i) + " or ";
	        }
			sqlExceptSelect += sArray.get(sArray.size() - 1);
		}		
		
		// 排序
		if (StrKit.notBlank(orderName) && StrKit.notBlank(orderDir)) {
			sqlExceptSelect += " order by " + orderName + " " + orderDir; // 排序
		} else {
			sqlExceptSelect += " order by createDate desc "; // 默认排序
		}
	        
		Page<Product> product = Product.dao.paginate(pageNumber, pageSize, select, sqlExceptSelect);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("draw", draw);
		map.put("data", product.getList());
		map.put("recordsFiltered", product.getTotalRow());  //过滤后记录数
		map.put("recordsTotal", product.getTotalRow()); //总记录数
		renderJson(map);
	}
	
	// 编辑
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("product", Product.dao.findById(id));
		}
		setAttr("productCategoryTreeList", getProductCategoryTreeList());
		setAttr("allBrand", getAllBrand());
		setAttr("allWeightUnit", getAllWeightUnit());
		setAttr("allProductType", getAllProductType());
		render("/admin/product_input.html");
	}
			
	// 保存
	@Before(ProductValidator.class)
	public void save() {
		productImages = getFiles();
		product = getModel(Product.class);
		String weightUnit = getPara("weightUnit","");
		
		if (StrKit.notBlank(weightUnit)){
			product.set("weightUnit", WeightUnit.valueOf(weightUnit).ordinal());
		}		
		if (product.getBigDecimal("price").compareTo(new BigDecimal("0")) < 0) {
			addActionError("销售价不允许小于0");
			return;
		}		
		if (product.getBigDecimal("marketPrice").compareTo(new BigDecimal("0")) < 0) {
			addActionError("市场价不允许小于0");
			return;
		}		
		if (product.getDouble("weight") < 0) {
			addActionError("商品重量只允许为正数或零!");
			return;
		}		
		if (StringUtils.isNotEmpty(product.getStr("productSn"))) {
			if (Product.dao.isExist(product.getStr("productSn"))) {
				addActionError("货号重复,请重新输入!");
				return;
			}
		} else {
			String productSn = SerialNumberUtil.buildProductSn();
			product.set("productSn",productSn);
		}		
			
		if (getSystemConfig().getPointType() == PointType.productSet) {
			if (product.get("point") == null) {
				addActionError("积分不允许为空!");
				return;
			}
		} else {
			product.set("point",0);
		}
		
		// 处理上传的图片
		if (productImages != null && productImages.size() > 0) {
			String allowedUploadImageExtension = getSystemConfig().getAllowedUploadImageExtension().toLowerCase();
			if (StringUtils.isEmpty(allowedUploadImageExtension)) {
				addActionError("不允许上传图片文件!");
				return;
			}
			for(int i = 0; i < productImages.size(); i ++) {
				File images = productImages.get(i).getFile();
				String productImageExtension =  StringUtils.substringAfterLast(images.getName(), ".").toLowerCase();
				String[] imageExtensionArray = allowedUploadImageExtension.split(SystemConfig.EXTENSION_SEPARATOR);
				if (!ArrayUtils.contains(imageExtensionArray, productImageExtension)) {
					addActionError("只允许上传图片文件类型: " + allowedUploadImageExtension + "!");
					return;
				}
				if (getSystemConfig().getUploadLimit() != 0 && images.length() > getSystemConfig().getUploadLimit() * 1024) {
					addActionError("此上传文件大小超出限制!");
					return;
				}
			}
		}
				
		List<ProductImage> productImageList = new ArrayList<ProductImage>();
		if (productImages != null && productImages.size() > 0) {
			for(int i = 0; i < productImages.size(); i ++) {
				ProductImage productImage = ProductImageService.service.buildProductImage(productImages.get(i).getFile());
				productImageList.add(productImage);
			}
		}		
		product.setProductImageList(productImageList);
		product.set("freezeStore",0);
		
		if (StrKit.notBlank(product.getStr("productType_id"))) {			
			List<ProductAttribute> enabledProductAttributeList = ProductAttribute.dao.getEnabledProductAttributeList(product.getStr("productType_id"));
			for (ProductAttribute productAttribute : enabledProductAttributeList) {
				String parameterValues = getPara(productAttribute.getStr("id"));				
				if (productAttribute.getBoolean("isRequired") && (parameterValues == null || parameterValues.isEmpty())) {
					addActionError(productAttribute.getStr("name") + "不允许为空!");
					return;
				}				
				AttributeType attributeType = ProductAttribute.dao.findById(productAttribute.getStr("id")).getAttributeType();				
				if (StrKit.notBlank(parameterValues)) {
					if (attributeType == AttributeType.number) {
						Pattern pattern = Pattern.compile("^-?(?:\\d+|\\d{1,3}(?:,\\d{3})+)(?:\\.\\d+)?");
						Matcher matcher = pattern.matcher(parameterValues);
						if (!matcher.matches()) {
							addActionError(productAttribute.getStr("name") + "只允许输入数字!");
							return;
						}
					}
					if (attributeType == AttributeType.alphaint) {
						Pattern pattern = Pattern.compile("[a-zA-Z]+");
						Matcher matcher = pattern.matcher(parameterValues);
						if (!matcher.matches()) {
							addActionError(productAttribute.getStr("name") + "只允许输入字母!");
							return;
						}
					}
					if (attributeType == AttributeType.date) {
						Pattern pattern = Pattern.compile("\\d{4}[\\/-]\\d{1,2}[\\/-]\\d{1,2}");
						Matcher matcher = pattern.matcher(parameterValues);
						if (!matcher.matches()) {
							addActionError(productAttribute.getStr("name") + "日期格式错误!");
							return;
						}
					}
					if (attributeType == AttributeType.select || attributeType == AttributeType.checkbox) {
						List<String> attributeOptionList = productAttribute.getAttributeOptionList();
						if (!attributeOptionList.contains(parameterValues)) {
							addActionError("参数错误!");
							return;
						}
					}
					productAttributeMap.put(productAttribute.getStr("id"), parameterValues);
				}
			}
		} 		
		product.save(product);		
		
		// 保存产品属性
		if (productAttributeMap != null && productAttributeMap.size() > 0) {
			product.setProductAttributeMap(productAttributeMap);
		}		
		// 生成静态页面
		if (product.getBoolean("isMarketable")) {// 是否上架
			HtmlService.service.productContentBuildHtml(product);
		}
		redirect("/product/list");
	}
	
	// 更新
	@SuppressWarnings("unused")
	@Before(ProductValidator.class)
	public void update() {
		productImages = getFiles();
		product = getModel(Product.class);
		productImageIds = getParaValues("productImageIds");
		productImageParameterTypes = getParaValues("productImageParameterTypes");
		
		String weightUnit = getPara("weightUnit","");
		if (StrKit.notBlank(weightUnit)){
			product.set("weightUnit", WeightUnit.valueOf(weightUnit).ordinal());
		}			
		if (product.getBigDecimal("price").compareTo(new BigDecimal("0")) < 0) {
			addActionError("销售价不允许小于0");
			return;
		}		
		if (product.getBigDecimal("marketPrice").compareTo(new BigDecimal("0")) < 0) {
			addActionError("市场价不允许小于0");
			return;
		}		
		if (product.getDouble("weight") < 0) {
			addActionError("商品重量只允许为正数或零!");
			return;
		}
		
		Product persistent = Product.dao.findById(product.getStr("id"));		
		if (StringUtils.isNotEmpty(product.getStr("productSn"))) {
			if (!Product.dao.isExist(product.getStr("productSn"))) {
				addActionError("货号重复,请重新输入!");
				return;
			}
		} else {
			String productSn = SerialNumberUtil.buildProductSn();
			product.set("productSn",productSn);
		}		
		
		if (StrKit.notBlank(product.getStr("productType_id"))) {
			List<ProductAttribute> enabledProductAttributeList = ProductAttribute.dao.getEnabledProductAttributeList(product.getStr("productType_id"));
			for (ProductAttribute productAttribute : enabledProductAttributeList) {
				String parameterValues = getPara(productAttribute.getStr("id"));
				if (productAttribute.getBoolean("isRequired") && (parameterValues == null || parameterValues.isEmpty())) {
					addActionError(productAttribute.getStr("name") + "不允许为空!");
					return;
				}
				
				AttributeType attributeType = ProductAttribute.dao.findById(productAttribute.getStr("id")).getAttributeType();
				if (StrKit.notBlank(parameterValues)) {
					if (attributeType == AttributeType.number) {
						Pattern pattern = Pattern.compile("^-?(?:\\d+|\\d{1,3}(?:,\\d{3})+)(?:\\.\\d+)?");
						Matcher matcher = pattern.matcher(parameterValues);
						if (!matcher.matches()) {
							addActionError(productAttribute.getStr("name") + "只允许输入数字!");
							return;
						}
					}
					if (attributeType == AttributeType.alphaint) {
						Pattern pattern = Pattern.compile("[a-zA-Z]+");
						Matcher matcher = pattern.matcher(parameterValues);
						if (!matcher.matches()) {
							addActionError(productAttribute.getStr("name") + "只允许输入字母!");
							return;
						}
					}
					if (productAttribute.getAttributeType() == AttributeType.date) {
						Pattern pattern = Pattern.compile("\\d{4}[\\/-]\\d{1,2}[\\/-]\\d{1,2}");
						Matcher matcher = pattern.matcher(parameterValues);
						if (!matcher.matches()) {
							addActionError(productAttribute.getStr("name") + "日期格式错误!");
							return;
						}
					}
					if (productAttribute.getAttributeType() == AttributeType.select || productAttribute.getAttributeType() == AttributeType.checkbox) {
						List<String> attributeOptionList = productAttribute.getAttributeOptionList();
						if (!attributeOptionList.contains(parameterValues)) {
							addActionError("参数错误!");
							return;
						}
					}
					productAttributeMap.put(productAttribute.getStr("id"), parameterValues);
				}
			}
		} 

		if (productImages != null && productImages.size() > 0) {
			String allowedUploadImageExtension = getSystemConfig().getAllowedUploadImageExtension().toLowerCase();
			if (StringUtils.isEmpty(allowedUploadImageExtension)) {
				addActionError("不允许上传图片文件!");
				return;
			}
			for(int i = 0; i < productImages.size(); i ++) {
				File images = productImages.get(i).getFile();
				String productImageExtension =  StringUtils.substringAfterLast(images.getName(), ".").toLowerCase();
				String[] imageExtensionArray = allowedUploadImageExtension.split(SystemConfig.EXTENSION_SEPARATOR);
				if (!ArrayUtils.contains(imageExtensionArray, productImageExtension)) {
					addActionError("只允许上传图片文件类型: " + allowedUploadImageExtension + "!");
					return;
				}
				if (getSystemConfig().getUploadLimit() != 0 && images.length() > getSystemConfig().getUploadLimit() * 1024) {
					addActionError("此上传文件大小超出限制!");
					return;
				}
			}
		}
		
		List<ProductImage> productImageList = new ArrayList<ProductImage>();
		if (productImageParameterTypes != null && productImageParameterTypes.length > 0) {
			int index = 0;
			int productImageFileIndex = 0;
			int productImageIdIndex = 0;
			for (String parameterType : productImageParameterTypes) {
				if (StringUtils.equalsIgnoreCase(parameterType, "productImageFile")) {
					ProductImage destProductImage = ProductImageService.service.buildProductImage(productImages.get(productImageFileIndex).getFile());
					productImageList.add(destProductImage);
					productImageFileIndex ++;
					index ++;
				} else if (StringUtils.equalsIgnoreCase(parameterType, "productImageId")) {
					ProductImage destProductImage = persistent.getProductImage(productImageIds[productImageIdIndex]);
					productImageList.add(destProductImage);
					productImageIdIndex ++;
					index ++;
				}
			}
		}
		
		if (StringUtils.isEmpty(product.getBrand().getStr("id"))) {
			product.set("brand_id",null);
		}
		if (getSystemConfig().getPointType() == PointType.productSet) {
			if (product.get("point") == null) {
				addActionError("积分不允许为空!");
				return;
			}
		} else {
			product.set("point",0);
		}
		if (product.get("store") == null) {
			product.set("freezeStore",0);
		} else {
			product.set("freezeStore",persistent.getInt("freezeStore"));
			
		}
		
		List<ProductImage> persistentProductImageList = persistent.getProductImageList();
		if (persistentProductImageList != null && persistentProductImageList.size() > 0) {
			for (ProductImage persistentProductImage : persistentProductImageList) {
				if (!productImageList.contains(persistentProductImage)) {
					ProductImageService.service.deleteFile(persistentProductImage);
				}
			}
		}
		
		// 产品图片地址
		if (productImageList != null && productImageList.size() > 0) {
			product.setProductImageList(productImageList);
		}
		// 保存产品属性
		if (productAttributeMap != null && productAttributeMap.size() > 0) {
			product.setProductAttributeMap(productAttributeMap);
		}			
		updated(product);
		// 生成静态页面
		if (product.getBoolean("isMarketable")) {// 是否上架
			HtmlService.service.productContentBuildHtml(persistent);
		}		
		redirect("/product/list");
	}
	
	// 删除
	public void delete() {
		ids = getParaValues("ids");
		if (ids != null && ids.length > 0) {
			for (String id : ids) {
				Product product = Product.dao.findById(id);
				List<OrderItem> orderItemList = product.getOrderItemList();
				if (orderItemList != null && orderItemList.size() > 0) {
					for (OrderItem orderItem : orderItemList) {
						if (orderItem.getOrder().getOrderStatus() != OrderStatus.completed && orderItem.getOrder().getOrderStatus() != OrderStatus.invalid) {
							ajaxJsonErrorMessage("商品[" + product.getStr("name") + "]订单处理未完成，删除失败！");
							return;
						}
					}
				}
				if (Product.dao.delete(product)) {
					ajaxJsonSuccessMessage("删除成功！");
				} else {
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}

	}
		
	// 获取所有商品类型
	public List<ProductType> getAllProductType() {
		return ProductType.dao.getAll();
	}
	
	// 获取所有品牌
	public List<Brand> getAllBrand() {
		return Brand.dao.getAll();
	}
		
	// 获取所有重量单位
	public List<WeightUnit> getAllWeightUnit() {
		List<WeightUnit> allWeightUnit = new ArrayList<WeightUnit>();
		for (WeightUnit weightUnit : WeightUnit.values()) {
			allWeightUnit.add(weightUnit);
		}
		return allWeightUnit;
	}
	
	// 获取商品分类树
	public List<ProductCategory> getProductCategoryTreeList() {
		return ProductCategory.dao.getProductCategoryTreeList();
	}
}
