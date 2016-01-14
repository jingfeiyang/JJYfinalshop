package com.jfinalshop.controller.shop;

import java.util.List;

import com.jfinal.aop.Clear;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.MemberProduct;
import com.jfinalshop.model.Product;

/**
 * 前台类 - 收藏夹
 * 
 */
@ControllerBind(controllerKey = "/shop/favorite")
public class FavoriteController extends BaseShopController<Product>{
	
	private static final Integer pageSize = 10;// 商品收藏每页显示数
	private Product product;
	private MemberProduct memberProduct;
	
	// 商品收藏列表
	public void list(){
		Member loginMember = getLoginMember();
		setAttr("pager", Product.dao.getFavoriteProductPager(loginMember,pageSize));
		render("/shop/favorite_list.html");
	}

	// 添加收藏商品
	@Clear
	public void ajaxAdd() {
		id = getPara("id","");		
		if(StrKit.isBlank(id)){
			addActionError("此商品ID不能为空!");
			return;
		}			
		product = Product.dao.findById(id);
		if (!product.getBoolean("isMarketable")) {
			ajaxJsonErrorMessage("此商品已下架!");
			return;
		}
		Member loginMember = getLoginMember();
		
		List<Product> favoriteProductList = loginMember.getFavoriteProductList();
		
		if (favoriteProductList.size() > 0 && favoriteProductList.contains(product)) {
			ajaxJsonWarnMessage("您已经收藏过此商品!");
		} else {
			memberProduct = new MemberProduct();
			memberProduct.set("favoriteMemberSet_id", loginMember.getStr("id"));
			memberProduct.set("favoriteProductSet_id", product.getStr("id"));
			memberProduct.save();
			ajaxJsonSuccessMessage("商品收藏成功!");
		}
	}
		
	// 删除收藏商品
	public void delete () {
		id = getPara("id","");
		String loginMember = getLoginMember().getStr("id");
		String sql = "delete from member_product where favoriteMemberSet_id = ? and favoriteProductSet_id = ?";
		if (StrKit.notBlank(id)){
			int row = Db.update(sql,loginMember,id);
			if (0 < row){
				redirect("/shop/favorite/list");
			} else{
				ajaxJsonErrorMessage("删除失败!");
			}
		}
		
	}
}
