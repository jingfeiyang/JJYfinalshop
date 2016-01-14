package com.jfinalshop.model;

import java.io.File;
import java.util.List;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 友情链接
 * 
 */
public class FriendLink extends Model<FriendLink>{

	private static final long serialVersionUID = 2937890826595747504L;

	public static final FriendLink dao = new FriendLink();
	
	/**
	 * 获取所有图片友情链接集合;
	 * 
	 * @return 图片友情链接集合
	 * 
	 */
	public List<FriendLink> getPictureFriendLinkList() {
		String sql = "select * from friendLink where logo is not null order by orderList asc ,createDate desc";
		return dao.find(sql);
	}
	
	/**
	 * 获取所有文字友情链接集合;
	 * 
	 * @return 图片友情链接集合
	 * 
	 */
	public List<FriendLink> getTextFriendLinkList() {
		String sql = "select * from friendLink where logo is null order by orderList asc ,createDate desc";
		return dao.find(sql);
	}
	
	// 重写方法，删除同时删除Logo文件
	public boolean delete(String id){
		FriendLink persistent = dao.findById(id); 
		if (persistent.getStr("logo") != null) {
			File logoFile = new File(PathKit.getWebRootPath() + persistent.getStr("logo"));
			if (logoFile.exists()) {
				logoFile.delete();
			}
		}		
		return dao.deleteById(id);
	}
	
	// 所有友情链接
	public List<FriendLink> getAll(){
		return dao.find("select * from friendLink order by orderList asc ,createDate desc");
	}
}
