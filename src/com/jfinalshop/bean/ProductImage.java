package com.jfinalshop.bean;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.jfinal.core.JFinal;
import com.jfinalshop.util.CommonUtil;
import com.jfinalshop.util.ImageUtil;
import com.jfinalshop.util.SystemConfigUtil;


/**
 * Bean类 - 商品图片
 * 
 */

public class ProductImage {
	
	public static final String PRODUCT_IMAGE_FILE_EXTENSION = "jpg";// 商品图片文件名扩展名
	public static final String BIG_PRODUCT_IMAGE_FILE_NAME_SUFFIX = "_big";// 商品图片（大）文件名后缀
	public static final String SMALL_PRODUCT_IMAGE_FILE_NAME_SUFFIX = "_small";// 商品图片（小）文件名后缀
	public static final String THUMBNAIL_PRODUCT_IMAGE_FILE_NAME_SUFFIX = "_thumbnail";// 商品缩略图文件名后缀
	
	private String id;// ID
	private String sourceProductImagePath;// 商品图片（原）路径
	private String bigProductImagePath;// 商品图片（大）路径
	private String smallProductImagePath;// 商品图片（小）路径
	private String thumbnailProductImagePath;// 商品图片（缩略图）路径
	
	public ProductImage buildProductImage(File uploadProductImageFile) {
		SystemConfig systemConfig = SystemConfigUtil.getSystemConfig();
		String sourceProductImageFormatName = ImageUtil.getImageFormatName(uploadProductImageFile);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
		String dateString = simpleDateFormat.format(new Date());
		String uuid = CommonUtil.getUUID();
		
		String sourceProductImagePath = SystemConfig.UPLOAD_IMAGE_DIR + dateString + "/" + uuid + "." + sourceProductImageFormatName;
		String bigProductImagePath = SystemConfig.UPLOAD_IMAGE_DIR + dateString + "/" + uuid + ProductImage.BIG_PRODUCT_IMAGE_FILE_NAME_SUFFIX + "." + ProductImage.PRODUCT_IMAGE_FILE_EXTENSION;
		String smallProductImagePath = SystemConfig.UPLOAD_IMAGE_DIR + dateString + "/" + uuid + ProductImage.SMALL_PRODUCT_IMAGE_FILE_NAME_SUFFIX + "." + ProductImage.PRODUCT_IMAGE_FILE_EXTENSION;
		String thumbnailProductImagePath = SystemConfig.UPLOAD_IMAGE_DIR + dateString + "/" + uuid + ProductImage.THUMBNAIL_PRODUCT_IMAGE_FILE_NAME_SUFFIX + "." + ProductImage.PRODUCT_IMAGE_FILE_EXTENSION;

		File sourceProductImageFile = new File(JFinal.me().getServletContext().getRealPath(sourceProductImagePath));
		File bigProductImageFile = new File(JFinal.me().getServletContext().getRealPath(bigProductImagePath));
		File smallProductImageFile = new File(JFinal.me().getServletContext().getRealPath(smallProductImagePath));
		File thumbnailProductImageFile = new File(JFinal.me().getServletContext().getRealPath(thumbnailProductImagePath));
		File watermarkImageFile = new File(JFinal.me().getServletContext().getRealPath(systemConfig.getWatermarkImagePath()));

		File sourceProductImageParentFile = sourceProductImageFile.getParentFile();
		File bigProductImageParentFile = bigProductImageFile.getParentFile();
		File smallProductImageParentFile = smallProductImageFile.getParentFile();
		File thumbnailProductImageParentFile = thumbnailProductImageFile.getParentFile();

		if (!sourceProductImageParentFile.exists()) {
			sourceProductImageParentFile.mkdirs();
		}
		if (!bigProductImageParentFile.exists()) {
			bigProductImageParentFile.mkdirs();
		}
		if (!smallProductImageParentFile.exists()) {
			smallProductImageParentFile.mkdirs();
		}
		if (!thumbnailProductImageParentFile.exists()) {
			thumbnailProductImageParentFile.mkdirs();
		}

		try {
			BufferedImage srcBufferedImage = ImageIO.read(uploadProductImageFile);
			// 将上传图片复制到原图片目录
			FileUtils.copyFile(uploadProductImageFile, sourceProductImageFile);
			// 商品图片（大）缩放、水印处理
			ImageUtil.zoomAndWatermark(srcBufferedImage, bigProductImageFile, systemConfig.getBigProductImageHeight(), systemConfig.getBigProductImageWidth(), watermarkImageFile, systemConfig.getWatermarkPosition(), systemConfig.getWatermarkAlpha().intValue());
			// 商品图片（小）缩放、水印处理
			ImageUtil.zoomAndWatermark(srcBufferedImage, smallProductImageFile, systemConfig.getSmallProductImageHeight(), systemConfig.getSmallProductImageWidth(), watermarkImageFile, systemConfig.getWatermarkPosition(), systemConfig.getWatermarkAlpha().intValue());
			// 商品图片缩略图处理
			ImageUtil.zoom(srcBufferedImage, thumbnailProductImageFile, systemConfig.getThumbnailProductImageHeight(), systemConfig.getThumbnailProductImageWidth());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ProductImage productImage = new ProductImage();
		productImage.setId(uuid);
		productImage.setSourceProductImagePath(sourceProductImagePath);
		productImage.setBigProductImagePath(bigProductImagePath);
		productImage.setSmallProductImagePath(smallProductImagePath);
		productImage.setThumbnailProductImagePath(thumbnailProductImagePath);
		return productImage;
	}
	
	public void deleteFile(ProductImage productImage) {
		File sourceProductImageFile = new File(JFinal.me().getServletContext().getRealPath(productImage.getSourceProductImagePath()));
		if (sourceProductImageFile.exists()) {
			sourceProductImageFile.delete();
		}
		File bigProductImageFile = new File(JFinal.me().getServletContext().getRealPath(productImage.getBigProductImagePath()));
		if (bigProductImageFile.exists()) {
			bigProductImageFile.delete();
		}
		File smallProductImageFile = new File(JFinal.me().getServletContext().getRealPath(productImage.getSmallProductImagePath()));
		if (smallProductImageFile.exists()) {
			smallProductImageFile.delete();
		}
		File thumbnailProductImageFile = new File(JFinal.me().getServletContext().getRealPath(productImage.getThumbnailProductImagePath()));
		if (thumbnailProductImageFile.exists()) {
			thumbnailProductImageFile.delete();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getSourceProductImagePath() {
		return sourceProductImagePath;
	}
	
	public void setSourceProductImagePath(String sourceProductImagePath) {
		this.sourceProductImagePath = sourceProductImagePath;
	}

	public String getBigProductImagePath() {
		return bigProductImagePath;
	}
	
	public void setBigProductImagePath(String bigProductImagePath) {
		this.bigProductImagePath = bigProductImagePath;
	}
	
	public String getSmallProductImagePath() {
		return smallProductImagePath;
	}
	
	public void setSmallProductImagePath(String smallProductImagePath) {
		this.smallProductImagePath = smallProductImagePath;
	}
	
	public String getThumbnailProductImagePath() {
		return thumbnailProductImagePath;
	}
	
	public void setThumbnailProductImagePath(String thumbnailProductImagePath) {
		this.thumbnailProductImagePath = thumbnailProductImagePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ProductImage other = (ProductImage) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}