package com.jfinalshop.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 商品分类
 * 
 */
public class ProductCategory extends Model<ProductCategory>{

	private static final long serialVersionUID = -9170872348235959429L;
	
	public static final ProductCategory dao = new ProductCategory();

	public static final String PATH_SEPARATOR = ",";// 树路径分隔符
	
	public List<ProductCategory> getAll() {
		String sql = "select * from productcategory t  order by t.orderList asc ";
		List<ProductCategory> allProductCategoryList = dao.find(sql);
		return recursivProductCategoryTreeList(allProductCategoryList, null, null);
	}
		
	public List<ProductCategory> getChildren() {
		String sql = "select * from productcategory t where t.parent_id = ? order by orderList asc";		
		return dao.find(sql,getStr("id"));
	}
	
	public List<Product> getProductList(){
		String sql = "select * from product t where t.productCategory_id = ?";
		return Product.dao.find(sql,getStr("id"));
	}
	
	public Integer getLevel() {
		return getStr("path").split(PATH_SEPARATOR).length - 1;
	}
	
	public ProductCategory getParent() {
		return dao.findById(getStr("parent_id"));
	}
	
	/**
	 * 获取商品分类树集合;
	 * 
	 * @return 商品分类树集合
	 * 
	 */
	public List<ProductCategory> getProductCategoryTreeList() {
		List<ProductCategory> allProductCategoryList = this.getAll();
		return recursivProductCategoryTreeList(allProductCategoryList, null, null);
	}
	
	// 递归父类排序分类树
	private List<ProductCategory> recursivProductCategoryTreeList(List<ProductCategory> allProductCategoryList, ProductCategory p, List<ProductCategory> temp) {
		if (temp == null) {
			temp = new ArrayList<ProductCategory>();
		}
		for (ProductCategory productCategory : allProductCategoryList) {
			ProductCategory parent = productCategory.getParent();
			
			if ((p == null && parent == null) || (productCategory != null && parent == p || (parent != null && parent.equals(p)))) {
				temp.add(productCategory);
				List<ProductCategory> children = productCategory.getChildren();
				if (children != null && children.size() > 0) {
					recursivProductCategoryTreeList(allProductCategoryList, productCategory, temp);
				}
			}
		}
		return temp;
	}
	
	/**
	 * 获取所有顶级商品分类集合;
	 * 
	 * @return 所有顶级商品分类集合
	 * 
	 */
	public List<ProductCategory> getRootProductCategoryList() {
		String sql = "select * from ProductCategory  where parent_id is null order by orderList asc";
		return dao.find(sql);
	}
	
	/**
	 * 根据ProductCategory对象获取所有父类集合，若无父类则返回null;
	 * 
	 * @return 父类集合
	 * 
	 */
	public List<ProductCategory> getParentProductCategoryList(ProductCategory productCategory) {
		String[] ids = productCategory.getStr("path").split(ProductCategory.PATH_SEPARATOR);
		String sql = "select * from ProductCategory  where id != ? and id in(?) order by orderList asc";		
		return dao.find(sql,productCategory.getStr("id"),ids);
	}
	
	/**
	 * 根据ProductCategory对象获取路径集合;
	 * 
	 * @return ProductCategory集合
	 * 
	 */
	public List<ProductCategory> getProductCategoryPathList(ProductCategory productCategory) {
		List<ProductCategory> productCategoryPathList = new ArrayList<ProductCategory>();
		productCategoryPathList.addAll(this.getParentProductCategoryList(productCategory));
		productCategoryPathList.add(productCategory);
		return productCategoryPathList;
	}
	
	/**
	 * 根据Product对象获取路径集合;
	 * 
	 * @return ProductCategory集合
	 * 
	 */
	public List<ProductCategory> getProductCategoryPathList(Product product) {
		ProductCategory productCategory = product.getProductCategory();
		List<ProductCategory> productCategoryList = new ArrayList<ProductCategory>();
		productCategoryList.addAll(this.getParentProductCategoryList(productCategory));
		productCategoryList.add(productCategory);
		return productCategoryList;
	}
	
	/**
	 * 根据ProductCategory对象获取所有子类集合，若无子类则返回null;
	 * 
	 * @return 子类集合
	 * 
	 */
	public List<ProductCategory> getChildrenProductCategoryList(ProductCategory productCategory) {
		String sql = "select * from  ProductCategory  where id != ? and path like ? order by orderList asc";
		return dao.find(sql,productCategory.getStr("id"),productCategory.getStr("id") + "%");
	}
	
	public boolean equals(ProductCategory obj) {
		if (obj == null)
			return false;
		return getStr("id").equals(obj.getStr("id"));
	}
}
