package com.jfinalshop.controller.admin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.core.JFinal;
import com.jfinal.kit.StrKit;
import com.jfinal.upload.UploadFile;
import com.jfinalshop.bean.SystemConfig;
import com.jfinalshop.model.Brand;
import com.jfinalshop.model.Product;
import com.jfinalshop.util.CommonUtil;
import com.jfinalshop.validator.admin.BrandValidator;

/**
 * 后台类 - 品牌
 * 
 */
public class BrandController extends BaseAdminController<Brand>{
	
	private Brand brand;
	private UploadFile logo;
	// 列表
	public void list() {
		findByPage();
		render("/admin/brand_list.html");
	}
		
	// 添加
	public void add() {
		render("/admin/brand_input.html");
	}
	
	// 编辑
	public void edit() {
		String id = getPara("id","");
		if (StrKit.notBlank(id)){
			setAttr("brand", Brand.dao.findById(id));
		}
		render("/admin/brand_input.html");
	}
	
	// 保存	
	@Before(BrandValidator.class)
	public void save() {
		logo = getFile();
		brand = getModel(Brand.class);
		if (logo != null) {
			String allowedUploadImageExtension = getSystemConfig().getAllowedUploadImageExtension().toLowerCase();
			if (StringUtils.isEmpty(allowedUploadImageExtension)){
				addActionError("不允许上传图片文件!");
				return;
			}
			String[] imageExtensionArray = allowedUploadImageExtension.split(SystemConfig.EXTENSION_SEPARATOR);
			String logoExtension = StringUtils.substringAfterLast(logo.getFileName(), ".").toLowerCase();
			if (!ArrayUtils.contains(imageExtensionArray, logoExtension)) {
				addActionError("只允许上传图片文件类型: " + allowedUploadImageExtension + "!");
				return;
			}
			int uploadLimit = getSystemConfig().getUploadLimit() * 1024;
			if (uploadLimit != 0 && logo.getFile().length() > uploadLimit) {
				addActionError("Logo文件大小超出限制!");
				return;
			}
			File uploadImageDir = new File(JFinal.me().getServletContext().getRealPath(SystemConfig.UPLOAD_IMAGE_DIR));
			if (!uploadImageDir.exists()) {
				uploadImageDir.mkdirs();
			}
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
			String dateString = simpleDateFormat.format(new Date());
			String uploadImagePath = SystemConfig.UPLOAD_IMAGE_DIR + dateString + "/" + CommonUtil.getUUID() + "." + logoExtension;
			File file = new File(JFinal.me().getServletContext().getRealPath(uploadImagePath));
			try {
				FileUtils.copyFile(logo.getFile(), file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			brand.set("logo", uploadImagePath);
		}
		saved(brand);
		redirect("/brand/list");
	}
	
	// 更新
	@Before(BrandValidator.class)
	public void update() {
		logo = getFile();
		brand = getModel(Brand.class);
		if (logo != null) {
			String allowedUploadImageExtension = getSystemConfig().getAllowedUploadImageExtension().toLowerCase();
			if (StringUtils.isEmpty(allowedUploadImageExtension)){
				addActionError("不允许上传图片文件!");
				return;
			}
			String[] imageExtensionArray = allowedUploadImageExtension.split(SystemConfig.EXTENSION_SEPARATOR);
			String logoExtension = StringUtils.substringAfterLast(logo.getFileName(), ".").toLowerCase();
			if (!ArrayUtils.contains(imageExtensionArray, logoExtension)) {
				addActionError("只允许上传图片文件类型: " + allowedUploadImageExtension + "!");
				return;
			}
			int uploadLimit = getSystemConfig().getUploadLimit() * 1024;
			if (uploadLimit != 0 && logo.getFile().length() > uploadLimit) {
				addActionError("Logo文件大小超出限制!");
				return;
			}
			File uploadImageDir = new File(JFinal.me().getServletContext().getRealPath(SystemConfig.UPLOAD_IMAGE_DIR));
			if (!uploadImageDir.exists()) {
				uploadImageDir.mkdirs();
			}
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
			String dateString = simpleDateFormat.format(new Date());
			String uploadImagePath = SystemConfig.UPLOAD_IMAGE_DIR + dateString + "/" + CommonUtil.getUUID() + "." + logoExtension;
			File file = new File(JFinal.me().getServletContext().getRealPath(uploadImagePath));
			try {
				FileUtils.copyFile(logo.getFile(), file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			brand.set("logo", uploadImagePath);
		}
		updated(brand);
		redirect("/brand/list");
	}
		
	// 删除
	public void delete() {
		String[] ids = getParaValues("ids");
		if (ids != null && ids.length > 0) {
			for (String id : ids) {
				// 充值记录
				List<Product> productList = Brand.dao.findById(id).getProductList();
				if (productList != null && productList.size() > 0){
					ajaxJsonErrorMessage("品牌在产品列表中存在，删除失败！");
					return;
				}		
				if (Brand.dao.delete(id)) {
					ajaxJsonSuccessMessage("删除成功！");
				} else {
					ajaxJsonErrorMessage("删除失败！");
				}
			} 
		} else {
			ajaxJsonErrorMessage("id为空未选中，删除失败！");
		}
	}
}
