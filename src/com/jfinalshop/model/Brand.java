package com.jfinalshop.model;

import java.io.File;
import java.util.List;

import com.jfinal.core.JFinal;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 品牌
 * 
 */
public class Brand extends Model<Brand>{

	private static final long serialVersionUID = 4675418211373785326L;
	
	public static final Brand dao = new Brand();

	public List<Brand> getAll(){
		return dao.find("select * from brand");
	}
	
	// 品牌关连到的产品
	public List<Product> getProductList(){
		String sql = "select * from product where brand_id = ?";
		return Product.dao.find(sql,getStr("id"));
	}
	
	// 重写delete
	public boolean delete(String id){
		Brand brand = dao.findById(id);
		//删除logo文件
		if (StrKit.notBlank(brand.getStr("logo"))){
			File logoFile = new File(JFinal.me().getServletContext().getRealPath(brand.getStr("logo")));
			if (logoFile.exists()) {
				logoFile.delete();
			}
		}		
		return brand.delete();
	}
}
