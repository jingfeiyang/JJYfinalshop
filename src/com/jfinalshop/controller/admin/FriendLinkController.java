package com.jfinalshop.controller.admin;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import com.jfinal.upload.UploadFile;
import com.jfinalshop.bean.SystemConfig;
import com.jfinalshop.model.FriendLink;
import com.jfinalshop.util.CommonUtil;
import com.jfinalshop.validator.admin.FriendLinkValidator;

/**
 * 后台类 - 友情链接
 * 
 */
public class FriendLinkController extends BaseAdminController<FriendLink>{
	
	private FriendLink friendLink;
	private File logo;
	private String logoFileName;
	
	// 添加
	public void add() {
		render("/admin/friend_link_input.html");
	}
	
	// 编辑
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("friendLink", FriendLink.dao.findById(id));
		}		
		render("/admin/friend_link_input.html");
	}

	// 列表
	public void list() {
		findByPage();
		render("/admin/friend_link_list.html");
	}
		
	
	// 保存
	@Before(FriendLinkValidator.class)
	public void save() throws IOException{
		UploadFile upLoadFile = getFile();
		friendLink = getModel(FriendLink.class);
		
		if (upLoadFile != null && upLoadFile.getFile().length() > 0){
			logo = upLoadFile.getFile(); 
			logoFileName = upLoadFile.getFileName();
		}		
		String allowedUploadImageExtension = getSystemConfig().getAllowedUploadImageExtension().toLowerCase();					
		String[] imageExtensionArray = allowedUploadImageExtension.split(SystemConfig.EXTENSION_SEPARATOR);		
		if (logo != null) {
			String logoExtension = StringUtils.substringAfterLast(logoFileName, ".").toLowerCase();
			if (!ArrayUtils.contains(imageExtensionArray, logoExtension)) {
				addActionError("只允许上传图片文件类型: " + allowedUploadImageExtension + "!");
				return;
			}
		}		
		int uploadLimit = getSystemConfig().getUploadLimit() * 1024;
		if (uploadLimit != 0) {
			if (logo != null && logo.length() > uploadLimit) {
				addActionError("Logo文件大小超出限制!");
				return;
			}
		}
		if (logo != null){
			String logoFilePath = SystemConfig.UPLOAD_IMAGE_DIR + CommonUtil.getUUID() + "." + StringUtils.substringAfterLast(logoFileName, ".").toLowerCase();
			File logoFile = new File(PathKit.getWebRootPath() + logoFilePath);
			FileUtils.copyFile(logo, logoFile);
			friendLink.set("logo", logoFilePath);
		}
		
		friendLink.set("id", CommonUtil.getUUID());
		friendLink.set("createDate", new Date());
		friendLink.save();
		redirect("/friendLink/list");
	}
	
	// 更新
	@Before(FriendLinkValidator.class)
	public void update() throws IOException{
		UploadFile upLoadFile = getFile();
		friendLink = getModel(FriendLink.class);
		
		if (upLoadFile != null && upLoadFile.getFile().length() > 0){
			logo = upLoadFile.getFile(); 
			logoFileName = upLoadFile.getFileName();
		}		
		String allowedUploadImageExtension = getSystemConfig().getAllowedUploadImageExtension().toLowerCase();					
		String[] imageExtensionArray = allowedUploadImageExtension.split(SystemConfig.EXTENSION_SEPARATOR);		
		if (logo != null) {
			String logoExtension = StringUtils.substringAfterLast(logoFileName, ".").toLowerCase();
			if (!ArrayUtils.contains(imageExtensionArray, logoExtension)) {
				addActionError("只允许上传图片文件类型: " + allowedUploadImageExtension + "!");
				return;
			}
		}		
		int uploadLimit = getSystemConfig().getUploadLimit() * 1024;
		if (uploadLimit != 0) {
			if (logo != null && logo.length() > uploadLimit) {
				addActionError("Logo文件大小超出限制!");
				return;
			}
		}
		if (logo != null) {
			String logoFilePath = null;
			if (friendLink.getStr("logo") != null) {
				logoFilePath = friendLink.getStr("logo");
			} else {
				logoFilePath = SystemConfig.UPLOAD_IMAGE_DIR + CommonUtil.getUUID() + "." + StringUtils.substringAfterLast(logoFileName, ".").toLowerCase();
			}
			File logoFile = new File(PathKit.getWebRootPath() + logoFilePath);
			FileUtils.copyFile(logo, logoFile);
			friendLink.set("logo",logoFilePath);
		}
		friendLink.set("modifyDate", new Date());
		friendLink.update();
		redirect("/friendLink/list");
	}
	
	// 删除
	public void delete(){
		String[] ids = getParaValues("ids");
		for (String id : ids) {
			if(FriendLink.dao.delete(id)){	
				ajaxJsonSuccessMessage("删除成功！");
			}else{
				ajaxJsonErrorMessage("删除失败！");
			}
		}	
	}
	
}
