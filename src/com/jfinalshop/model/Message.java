package com.jfinalshop.model;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

/**
 * 实体类 - 消息
 * 
 */
public class Message extends Model<Message>{

	private static final long serialVersionUID = -7827811492360158707L;
	
	public static final Message dao = new Message();

	public static final int DEFAULT_MESSAGE_LIST_PAGE_SIZE = 12;// 消息列表默认每页显示数

	// 删除状态（未删除、发送者删除、接收者删除）
	public enum DeleteStatus {
		nonDelete, fromDelete, toDelete 
	};
	
	/**
	 * 根据Pager获取管理员[收件箱]消息分页对象
	 * 
	 */
	public Page<Message> getAdminInboxPager(int pageNumber, int pageSize,String orderBy, String orderType, String property, String keyword) {

		String select = " select  m.*";
		String sqlExceptSelect = " from Message m inner join Member mb on m.fromMember_id = mb.id where m.tomember_id is null and m.isSaveDraftbox = ? and m.deleteStatus <> ? "; 

		if (StrKit.notBlank(property) && StrKit.notBlank(keyword)) {
			sqlExceptSelect += " and " + property + " like '%" + keyword + "%'";
		}

		if (StrKit.notBlank(orderBy) && StrKit.notBlank(orderType)) {
			sqlExceptSelect += " order by " + orderBy + " " + orderType;
		} else {
			sqlExceptSelect += " order by createDate desc ";
		}
		
		Page<Message> list = dao.paginate(pageNumber, pageSize, select, sqlExceptSelect,false,DeleteStatus.valueOf(DeleteStatus.toDelete.name()).ordinal());
		return list;
	}
	
	/**
	 * 根据Pager获取管理员[发件箱]消息分页对象
	 * 
	 */
	public Page<Message> getAdminOutboxPager(int pageNumber, int pageSize,String orderBy, String orderType, String property, String keyword) {

		String select = " select  m.* ";
		String sqlExceptSelect = " from Message m  inner join Member mb  on m.tomember_id = mb.id where m.fromMember_id is null and m.isSaveDraftbox = ? and m.deleteStatus <> ?";

		if (StrKit.notBlank(property) && StrKit.notBlank(keyword)) {
			sqlExceptSelect += "and " + property + " like '%" + keyword + "%'";
		}

		if (StrKit.notBlank(orderBy) && StrKit.notBlank(orderType)) {
			sqlExceptSelect += " order by " + orderBy + " " + orderType;
		} else {
			sqlExceptSelect += " order by createDate desc ";
		}
		
		Page<Message> list = dao.paginate(pageNumber, pageSize, select, sqlExceptSelect,false,DeleteStatus.valueOf(DeleteStatus.fromDelete.name()).ordinal());
		return list;
	}
	
	/**
	 * 根据Member、获取会员[收件箱]分页对象
	 * 
	 */
	public Page<Message> getMemberInboxPager(int pageNumber, int pageSize, Member member) {
		String select = "select * ";
		String sqlExceptSelect = " from message where toMember_id = ? and isSaveDraftbox = ? and deleteStatus <> ? ";

		sqlExceptSelect += " order by createDate desc ";
		
		Page<Message> pager = dao.paginate(pageNumber, pageSize, select, sqlExceptSelect,member.getStr("id"),false,DeleteStatus.valueOf(DeleteStatus.fromDelete.name()).ordinal());
		return pager;
	}
	
	/**
	 * 根据Member、Pager获取会员[草稿箱]分页对象
	 * 
	 */
	public Page<Message> getMemberDraftboxPager(int pageNumber, int pageSize,Member member) {
		String select = "select * ";
		String sqlExceptSelect = " from message where toMember_id = ? and isSaveDraftbox = ? and deleteStatus <> ? ";

		sqlExceptSelect += " order by createDate desc ";
		
		Page<Message> pager = dao.paginate(pageNumber, pageSize, select, sqlExceptSelect,member.getStr("id"),true,DeleteStatus.valueOf(DeleteStatus.fromDelete.name()).ordinal());
		return pager;
	}
	
	/**
	 * 根据Member、Pager获取会员[发件箱]分页对象
	 * 
	 */
	public Page<Message> getMemberOutboxPager(int pageNumber, int pageSize,Member member) {
		String select = "select * ";
		String sqlExceptSelect = " from message where fromMember_id = ? and isSaveDraftbox = ? and deleteStatus <> ? ";

		sqlExceptSelect += " order by createDate desc ";
		
		Page<Message> pager = dao.paginate(pageNumber, pageSize, select, sqlExceptSelect,member.getStr("id"),false,DeleteStatus.valueOf(DeleteStatus.fromDelete.name()).ordinal());
		return pager;
	}
	
	
	public Member getFromMember(){
		return Member.dao.findById(getStr("fromMember_id"));
	}
	
	public Member getToMember(){
		return Member.dao.findById(getStr("toMember_id"));
	}
	
	/**
	 * 根据Member获取未读消息数量
	 * 
	 * @param member
	 *            Member对象
	 * 
	 * @return 未读消息数量
	 */
	public Long getUnreadMessageCount(Member member) {
		String sql = "select count(*) from  message where toMember_id = ? and isRead = ? and isSaveDraftbox = ? and deleteStatus != ?";
		return Db.queryLong(sql,member.getStr("id"),false,false,DeleteStatus.valueOf(DeleteStatus.toDelete.name()).ordinal());
	}
	
	/**
	 * 获取管理员未读消息数
	 * 
	 * @return 未读消息数量
	 */
	public Long getUnreadMessageCount() {
		String sql = "select count(*) from message where toMember_id is null and isRead = ? and isSaveDraftbox = ? and deleteStatus != ?";
		return Db.queryLong(sql,false,false,DeleteStatus.valueOf(DeleteStatus.toDelete.name()).ordinal());
	}
	
}
