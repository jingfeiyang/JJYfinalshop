package com.jfinalshop.model;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * 实体类 - 会员等级
 * 
 */
public class MemberRank extends Model<MemberRank>{

	private static final long serialVersionUID = 5042501344569904747L;
	
	public static final MemberRank dao = new MemberRank();
	
	/**
	 * 获取默认会员等级.
	 * 
	 */
	public MemberRank getDefaultMemberRank() {
		MemberRank defaultMemberRank = dao.findFirst("select * from memberrank where isDefault=? limit 1",1);
		if(defaultMemberRank == null) {
			defaultMemberRank = dao.findFirst("select * from MemberRank order by memberRank.createDate asc");
		}
		return defaultMemberRank;
	}
	
	/**
	 * 根据积分获取符合此积分条件的最高会员等级，若不存在则返回null
	 * 
	 */
	public MemberRank getUpMemberRankByPoint(Integer point) {
		String sql = "select * from MemberRank  where point <= ? order by point desc";
		return dao.findFirst(sql,point);
	}
	
	
	/**
	 * 根据积分获取会员等级，若不存在则返回null
	 * 
	 */
	public MemberRank getMemberRankByPoint(Integer point) {
		String sql = "select * from MemberRank  where memberRank.point = ?";
		return dao.findFirst(sql, point);
	}
	
	/**
	 * 重写方法，保存时若对象isDefault=true，则设置其它对象isDefault值为false
	 * 
	 */
	public void save(MemberRank memberRank) {
		if (memberRank.getBoolean("isDefault")) {
			String sql = "select * from MemberRank where memberRank.isDefault = ?";
			List<MemberRank> memberRankList = dao.find(sql,1);
			if (memberRankList != null) {
				for (MemberRank r : memberRankList) {
					r.set("isDefault", false);
					r.update();
				}
			}
		}
		memberRank.set("id", CommonUtil.getUUID());
		memberRank.save();
	}
	
	/**
	 * 重写方法，更新时若对象isDefault=true，则设置其它对象isDefault值为false
	 * 
	 */
	public void update(MemberRank memberRank) {
		if (memberRank.getBoolean("isDefault")) {
			String sql = "select * from MemberRank where memberRank.isDefault = ?";
			List<MemberRank> memberRankList = dao.find(sql,true);
			if (memberRankList != null) {
				for (MemberRank r : memberRankList) {
					r.set("isDefault", false);
					r.update();
				}
			}
		}
		memberRank.update();
	}
	
	/**
	 * 获取所有实体对象总数.
	 * 
	 * @return 实体对象总数
	 */
	public Long getTotalCount() {
		String sql = "select count(*) from memberrank ";
		return Db.queryLong(sql);
	}
	
	/* getter */
	public List<Member> getMember(){
		String sql = "select * from member where memberRank_id = ?";
		return Member.dao.find(sql,getStr("id"));
		
	}
	
	public List<MemberRank> getAll(){
		String sql = "select * from memberRank";
		return dao.find(sql);
	}
}
