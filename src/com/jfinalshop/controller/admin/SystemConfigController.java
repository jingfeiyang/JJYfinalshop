package com.jfinalshop.controller.admin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinalshop.bean.SystemConfig;
import com.jfinalshop.bean.SystemConfig.CurrencyType;
import com.jfinalshop.bean.SystemConfig.PointType;
import com.jfinalshop.bean.SystemConfig.RoundType;
import com.jfinalshop.bean.SystemConfig.StoreFreezeTime;
import com.jfinalshop.bean.SystemConfig.WatermarkPosition;
import com.jfinalshop.interceptor.AdminInterceptor;
import com.jfinalshop.util.CommonUtil;
import com.jfinalshop.util.SystemConfigUtil;

/**
 * 后台类 - 系统设置
 * 
 */
@Before(AdminInterceptor.class)
public class SystemConfigController extends Controller {

	private SystemConfig systemConfig;
	private File shopLogo;
	private String shopLogoFileName;
	private File defaultBigProductImage;
	private String defaultBigProductImageFileName;
	private File defaultSmallProductImage;
	private String defaultSmallProductImageFileName;
	private File defaultThumbnailProductImage;
	private String defaultThumbnailProductImageFileName;
	private File watermarkImage;
	private String watermarkImageFileName;
	
	// 编辑
	public void edit() {
		setAttr("systemConfig", SystemConfigUtil.getSystemConfig());
		setAttr("allWatermarkPosition", getAllWatermarkPosition());
		setAttr("allCurrencyType", getAllCurrencyType());
		setAttr("allRoundType", getAllRoundType());
		setAttr("allStoreFreezeTime", getAllStoreFreezeTime());
		setAttr("allPointType", getAllPointType());
		render("/admin/system_config_input.html");
	}

	// 更新
	public void update() throws IOException{
		// 网店Logo
		if (getFile("shopLogo") != null) {
			shopLogo = getFile("shopLogo").getFile();
			shopLogoFileName = shopLogo.getName();
		}			
		// 默认商品图片（大）
		if (getFile("defaultBigProductImage") != null) {
			defaultBigProductImage = getFile("defaultBigProductImage").getFile();
			defaultBigProductImageFileName = defaultBigProductImage.getName();
		}		
		// 默认商品图片（小）
		if (getFile("defaultSmallProductImage") != null) {
			defaultSmallProductImage = getFile("defaultSmallProductImage").getFile();
			defaultSmallProductImageFileName = defaultSmallProductImage.getName();
		}		
		// 默认缩略图
		if (getFile("defaultThumbnailProductImage") != null) {
			defaultThumbnailProductImage = getFile("defaultThumbnailProductImage").getFile();
			defaultThumbnailProductImageFileName = defaultThumbnailProductImage.getName();
		}
		// 水印图片
		if (getFile("watermarkImage") != null) {
			watermarkImage = getFile("watermarkImage").getFile();
			watermarkImageFileName = watermarkImage.getName();
		}
		systemConfig = getModel(SystemConfig.class);
		
		if (systemConfig.getPointType() == PointType.orderAmount) {
			if (systemConfig.getPointScale() < 0) {
				addActionError("积分换算比率不允许小于0!");
				return;
			}
		} else {
			systemConfig.setPointScale(0D);
		}
	
		// 验证图片
		if (shopLogo != null || defaultBigProductImage != null || defaultSmallProductImage != null || defaultThumbnailProductImage != null || watermarkImage != null) {
			String allowedUploadImageExtension = getSystemConfig().getAllowedUploadImageExtension().toLowerCase();
			if (StringUtils.isEmpty(allowedUploadImageExtension)){
				addActionError("不允许上传图片文件!");
				return;
			}
			String[] imageExtensionArray = allowedUploadImageExtension.split(SystemConfig.EXTENSION_SEPARATOR);
			if (defaultBigProductImage != null) {
				String defaultBigProductImageExtension =  StringUtils.substringAfterLast(defaultBigProductImageFileName, ".").toLowerCase();
				if (!ArrayUtils.contains(imageExtensionArray, defaultBigProductImageExtension)) {
					addActionError("只允许上传图片文件类型: " + allowedUploadImageExtension + "!");
					return;
				}
			}			
			if (defaultSmallProductImage != null) {
				String defaultSmallProductImageExtension =  StringUtils.substringAfterLast(defaultSmallProductImageFileName, ".").toLowerCase();
				if (!ArrayUtils.contains(imageExtensionArray, defaultSmallProductImageExtension)) {
					addActionError("只允许上传图片文件类型: " + allowedUploadImageExtension + "!");
					return;
				}
			}			
			if (defaultThumbnailProductImage != null) {
				String defaultThumbnailProductImageExtension =  StringUtils.substringAfterLast(defaultThumbnailProductImageFileName, ".").toLowerCase();
				if (!ArrayUtils.contains(imageExtensionArray, defaultThumbnailProductImageExtension)) {
					addActionError("只允许上传图片文件类型: " + allowedUploadImageExtension + "!");
					return;
				}
			}			
			if (watermarkImage != null) {
				String watermarkImageExtension =  StringUtils.substringAfterLast(watermarkImageFileName, ".").toLowerCase();
				if (!ArrayUtils.contains(imageExtensionArray, watermarkImageExtension)) {
					addActionError("只允许上传图片文件类型: " + allowedUploadImageExtension + "!");
					return;
				}
			}			
		}	
		
		int uploadLimit = systemConfig.getUploadLimit() * 1024;
		if (uploadLimit != 0) {
			if (shopLogo != null && shopLogo.length() > uploadLimit) {
				addActionError("网店Logo文件大小超出限制!");
				return;
			}
			if (defaultBigProductImage != null && defaultBigProductImage.length() > uploadLimit) {
				addActionError("默认商品图片（大）文件大小超出限制!");
				return;
			}
			if (defaultSmallProductImage != null && defaultSmallProductImage.length() > uploadLimit) {
				addActionError("默认商品图片（小）文件大小超出限制!");
				return;
			}
			if (defaultThumbnailProductImage != null && defaultThumbnailProductImage.length() > uploadLimit) {
				addActionError("默认缩略图文件大小超出限制!");
				return;
			}
			if (watermarkImage != null && watermarkImage.length() > uploadLimit) {
				addActionError("水印图片文件大小超出限制!");
				return;
			}
		}
		
		SystemConfig persistent = SystemConfigUtil.getSystemConfig();
		
		if (StringUtils.isEmpty(systemConfig.getSmtpPassword())) {
			systemConfig.setSmtpPassword(persistent.getSmtpPassword());
		}
		if (systemConfig.getIsLoginFailureLock() == false) {
			systemConfig.setLoginFailureLockCount(3);
			systemConfig.setLoginFailureLockTime(10);
		}
		// 网店Logo
		if (shopLogo != null) {
			File oldShopLogoFile = new File(PathKit.getWebRootPath() + persistent.getShopLogo());
			if (oldShopLogoFile.isFile()) {
				oldShopLogoFile.delete();
			}
			String shopLogoFilePath = SystemConfig.UPLOAD_IMAGE_DIR + SystemConfig.LOGO_UPLOAD_NAME + "." +  StringUtils.substringAfterLast(shopLogoFileName, ".").toLowerCase();
			File shopLogoFile = new File(PathKit.getWebRootPath() + shopLogoFilePath);
			FileUtils.copyFile(shopLogo, shopLogoFile);
			systemConfig.setShopLogo(shopLogoFilePath);
		}		
		// 处理默认商品图片（大）
		if (defaultBigProductImage != null) {
			File oldDefaultBigProductImageFile = new File(PathKit.getWebRootPath() + persistent.getDefaultBigProductImagePath());
			if (oldDefaultBigProductImageFile.exists()) {
				oldDefaultBigProductImageFile.delete();
			}
			String defaultBigProductImagePath = SystemConfig.UPLOAD_IMAGE_DIR + SystemConfig.DEFAULT_BIG_PRODUCT_IMAGE_FILE_NAME + "." +  StringUtils.substringAfterLast(defaultBigProductImageFileName, ".").toLowerCase();
			File defaultBigProductImageFile = new File(PathKit.getWebRootPath() + defaultBigProductImagePath);
			FileUtils.copyFile(defaultBigProductImage, defaultBigProductImageFile);
			systemConfig.setDefaultBigProductImagePath(defaultBigProductImagePath);
		}		
		// 处理默认商品图片（小）
		if (defaultSmallProductImage != null) {
			File oldDefaultSmallProductImageFile = new File(PathKit.getWebRootPath() + persistent.getDefaultSmallProductImagePath());
			if (oldDefaultSmallProductImageFile.exists()) {
				oldDefaultSmallProductImageFile.delete();
			}
			String defaultSmallProductImagePath = SystemConfig.UPLOAD_IMAGE_DIR + SystemConfig.DEFAULT_SMALL_PRODUCT_IMAGE_FILE_NAME + "." +  StringUtils.substringAfterLast(defaultSmallProductImageFileName, ".").toLowerCase();
			File defaultSmallProductImageFile = new File(PathKit.getWebRootPath() + defaultSmallProductImagePath);
			FileUtils.copyFile(defaultSmallProductImage, defaultSmallProductImageFile);
			systemConfig.setDefaultSmallProductImagePath(defaultSmallProductImagePath);
		}		
		// 处理默认商品缩略图
		if (defaultThumbnailProductImage != null) {
			File oldDefaultThumbnailProductImageFile = new File(PathKit.getWebRootPath() + persistent.getDefaultThumbnailProductImagePath());
			if (oldDefaultThumbnailProductImageFile.exists()) {
				oldDefaultThumbnailProductImageFile.delete();
			}
			String defaultThumbnailProductImagePath = SystemConfig.UPLOAD_IMAGE_DIR + SystemConfig.DEFAULT_THUMBNAIL_PRODUCT_IMAGE_FILE_NAME + "." +  StringUtils.substringAfterLast(defaultThumbnailProductImageFileName, ".").toLowerCase();
			File defaultThumbnailProductImageFile = new File(PathKit.getWebRootPath() + defaultThumbnailProductImagePath);
			FileUtils.copyFile(defaultThumbnailProductImage, defaultThumbnailProductImageFile);
			systemConfig.setDefaultThumbnailProductImagePath(defaultThumbnailProductImagePath);
		}		
		// 处理水印图片
		if (watermarkImage != null) {
			File oldWatermarkImageFile = new File(PathKit.getWebRootPath() + persistent.getWatermarkImagePath());
			if (oldWatermarkImageFile.exists()) {
				oldWatermarkImageFile.delete();
			}
			String watermarkImagePath = SystemConfig.UPLOAD_IMAGE_DIR + SystemConfig.WATERMARK_IMAGE_FILE_NAME + "." +  StringUtils.substringAfterLast(watermarkImageFileName, ".").toLowerCase();
			File watermarkImageFile = new File(PathKit.getWebRootPath() + watermarkImagePath);
			FileUtils.copyFile(watermarkImage, watermarkImageFile);
			systemConfig.setWatermarkImagePath(watermarkImagePath);
		}
		// 如是这个对象的某一个属性不为空，把他copy到另一个有这个属性的bean中
		CommonUtil.copyProperties(persistent, systemConfig);
		SystemConfigUtil.update(persistent);
		renderSuccessMessage("修改成功!", " /systemConfig/edit");
		
	}
	
	// 获取所有WatermarkPosition值
	public List<WatermarkPosition> getAllWatermarkPosition() {
		List<WatermarkPosition> allWatermarkPosition = new ArrayList<WatermarkPosition>();
		for (WatermarkPosition watermarkPosition : WatermarkPosition.values()) {
			allWatermarkPosition.add(watermarkPosition);
		}
		return allWatermarkPosition;
	}

	// 获取所有货币种类
	public List<CurrencyType> getAllCurrencyType() {
		List<CurrencyType> allCurrencyType = new ArrayList<CurrencyType>();
		for (CurrencyType currencyType : CurrencyType.values()) {
			allCurrencyType.add(currencyType);
		}
		return allCurrencyType;
	}

	// 获取所有小数位精确方式
	public List<RoundType> getAllRoundType() {
		List<RoundType> allRoundType = new ArrayList<RoundType>();
		for (RoundType roundType : RoundType.values()) {
			allRoundType.add(roundType);
		}
		return allRoundType;
	}

	// 获取所有库存预占时间点
	public List<StoreFreezeTime> getAllStoreFreezeTime() {
		List<StoreFreezeTime> allStoreFreezeTime = new ArrayList<StoreFreezeTime>();
		for (StoreFreezeTime storeFreezeTime : StoreFreezeTime.values()) {
			allStoreFreezeTime.add(storeFreezeTime);
		}
		return allStoreFreezeTime;
	}

	// 获取所有积分获取方式
	public List<PointType> getAllPointType() {
		List<PointType> allPointType = new ArrayList<PointType>();
		for (PointType pointType : PointType.values()) {
			allPointType.add(pointType);
		}
		return allPointType;
	}

	public void addActionError(String error){
		setAttr("errorMessages", error);
		render("/admin/error.html");	
	}
	
	public void renderSuccessMessage(String message,String url){
		setAttr("message", message);
		setAttr("redirectionUrl", url);
		render("/admin/success.html");
	}
	
	// 获取系统配置信息
	public SystemConfig getSystemConfig() {
		return SystemConfigUtil.getSystemConfig();
	}
}
