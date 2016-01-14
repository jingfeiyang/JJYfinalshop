package com.jfinalshop.controller.admin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.core.JFinal;
import com.jfinal.upload.UploadFile;
import com.jfinalshop.bean.SystemConfig;
import com.jfinalshop.model.Admin;
import com.jfinalshop.util.CommonUtil;

/**
 * 后台Action类 - 文件上传
 * 
 */
public class UploadController extends BaseAdminController<Admin> {
	private File upload;// 上传文件
	private String uploadFileName;// 上传文件名
	
	// 图片文件上传
	public void image() {
		UploadFile imgageFile = getFile();	
		if (imgageFile != null) {
			upload = imgageFile.getFile();
			uploadFileName = imgageFile.getFileName();
		}		
		if (upload == null) {
			ajaxJsonErrorMessage("请选择上传文件!");
			return;
		}
		String allowedUploadImageExtension = getSystemConfig().getAllowedUploadImageExtension().toLowerCase();
		if (StringUtils.isEmpty(allowedUploadImageExtension)){
			ajaxJsonErrorMessage("不允许上传图片文件!");
			return;
		}
		String imageExtension =  StringUtils.substringAfterLast(uploadFileName, ".").toLowerCase();
		String[] imageExtensionArray = allowedUploadImageExtension.split(SystemConfig.EXTENSION_SEPARATOR);
		if (!ArrayUtils.contains(imageExtensionArray, imageExtension)) {
			ajaxJsonErrorMessage("只允许上传图片文件类型: " + allowedUploadImageExtension + "!");
			return;
		}
		int uploadLimit = getSystemConfig().getUploadLimit() * 1024;
		if (uploadLimit != 0) {
			if (upload != null && upload.length() > uploadLimit) {
				ajaxJsonErrorMessage("文件大小超出限制!");
				return;
			}
		}
		File uploadImageDir = new File(JFinal.me().getServletContext().getRealPath(SystemConfig.UPLOAD_IMAGE_DIR));
		if (!uploadImageDir.exists()) {
			uploadImageDir.mkdirs();
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
		String dateString = simpleDateFormat.format(new Date());
		String uploadImagePath = SystemConfig.UPLOAD_IMAGE_DIR + dateString + "/" + CommonUtil.getUUID() + "." + imageExtension;
		File file = new File(JFinal.me().getServletContext().getRealPath(uploadImagePath));
		try {
			FileUtils.copyFile(upload, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put(STATUS, SUCCESS);
		jsonMap.put("url", JFinal.me().getServletContext().getContextPath() + uploadImagePath);
		renderJson(jsonMap);
	}
	
	// 媒体文件上传
	public void media() {
		UploadFile mediaFile = getFile();		
		if (mediaFile != null) {
			upload = mediaFile.getFile();
			uploadFileName = mediaFile.getFileName();
		}		
		if (upload == null) {
			ajaxJsonErrorMessage("请选择上传文件!");
			return;
		}
		String allowedUploadMediaExtension = getSystemConfig().getAllowedUploadMediaExtension().toLowerCase();
		if (StringUtils.isEmpty(allowedUploadMediaExtension)){
			ajaxJsonErrorMessage("不允许上传媒体文件!");
			return;
		}
		String mediaExtension =  StringUtils.substringAfterLast(uploadFileName, ".").toLowerCase();
		String[] mediaExtensionArray = allowedUploadMediaExtension.split(SystemConfig.EXTENSION_SEPARATOR);
		if (!ArrayUtils.contains(mediaExtensionArray, mediaExtension)) {
			ajaxJsonErrorMessage("只允许上传媒体文件类型: " + allowedUploadMediaExtension + "!");
			return;
		}
		int uploadLimit = getSystemConfig().getUploadLimit() * 1024;
		if (uploadLimit != 0) {
			if (upload != null && upload.length() > uploadLimit) {
				ajaxJsonErrorMessage("文件大小超出限制!");
				return;
			}
		}
		File uploadMediaDir = new File(JFinal.me().getServletContext().getRealPath(SystemConfig.UPLOAD_MEDIA_DIR));
		if (!uploadMediaDir.exists()) {
			uploadMediaDir.mkdirs();
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
		String dateString = simpleDateFormat.format(new Date());
		String uploadMediaPath = SystemConfig.UPLOAD_MEDIA_DIR + dateString + "/" + CommonUtil.getUUID() + "." + mediaExtension;
		File file = new File(JFinal.me().getServletContext().getRealPath(uploadMediaPath));
		try {
			FileUtils.copyFile(upload, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put(STATUS, SUCCESS);
		jsonMap.put("url", JFinal.me().getServletContext().getContextPath() + uploadMediaPath);
		renderJson(jsonMap);
	}
	
	// 其它文件上传
	public void file() throws Exception {
		UploadFile upfile = getFile();		
		if (upfile != null) {
			upload = upfile.getFile();
			uploadFileName = upfile.getFileName();
		}		
		if (upload == null) {
			ajaxJsonErrorMessage("请选择上传文件!");
			return;
		}
		String allowedUploadFileExtension = getSystemConfig().getAllowedUploadFileExtension().toLowerCase();
		if (StringUtils.isEmpty(allowedUploadFileExtension)){
			ajaxJsonErrorMessage("不允许上传文件!");
			return;
		}
		String fileExtension =  StringUtils.substringAfterLast(uploadFileName, ".").toLowerCase();
		String[] fileExtensionArray = allowedUploadFileExtension.split(SystemConfig.EXTENSION_SEPARATOR);
		if (!ArrayUtils.contains(fileExtensionArray, fileExtension)) {
			ajaxJsonErrorMessage("只允许上传文件类型: " + allowedUploadFileExtension + "!");
			return;
		}
		int uploadLimit = getSystemConfig().getUploadLimit() * 1024;
		if (uploadLimit != 0) {
			if (upload != null && upload.length() > uploadLimit) {
				ajaxJsonErrorMessage("文件大小超出限制!");
				return;
			}
		}
		File uploadFileDir = new File(JFinal.me().getServletContext().getRealPath(SystemConfig.UPLOAD_FILE_DIR));
		if (!uploadFileDir.exists()) {
			uploadFileDir.mkdirs();
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
		String dateString = simpleDateFormat.format(new Date());
		String uploadFilePath = SystemConfig.UPLOAD_FILE_DIR + dateString + "/" + CommonUtil.getUUID() + "." + fileExtension;
		File file = new File(JFinal.me().getServletContext().getRealPath(uploadFilePath));
		FileUtils.copyFile(upload, file);
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put(STATUS, SUCCESS);
		jsonMap.put("url", JFinal.me().getServletContext().getContextPath() + uploadFilePath);
		ajaxJsonErrorMessage("文件大小超出限制!");
	}
}
