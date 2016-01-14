package com.jfinalshop.controller.admin;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.MemberRank;
import com.jfinalshop.validator.admin.MemberRankValidator;

/**
 * 后台类 - 会员分类
 * 
 */

public class MemberRankController extends BaseAdminController<MemberRank>{
	
	private MemberRank memberRank;

	// 列表
	public void list() {
		findByPage();
		render("/admin/member_rank_list.html");
	}
	
	// 添加
	public void add() {
		render("/admin/member_rank_input.html");
	}

	// 编辑
	public void edit() {
		id = getPara("id","");
		memberRank = MemberRank.dao.findById(id);
		setAttr("memberRank", memberRank);
		render("/admin/member_rank_input.html");
	}
		
	// 是否已存在 ajax验证	
	public void checkName(){
		String newValue = getPara("memberRank.name","");	
		if (isUnique("memberRank","name",newValue)) {
			renderText("true");
		} else {
			renderText("false");
		}
	}
	
	// 添加
	@Before(MemberRankValidator.class)
	public void save(){
		memberRank = getModel(MemberRank.class);
		if (memberRank.getDouble("preferentialScale") < 0) {
			addActionError("优惠百分比必须大于或等于零!");
			return;
		}
		if (MemberRank.dao.getMemberRankByPoint(memberRank.getInt("point")) != null) {
			addActionError("已存在相同积分的会员等级!");
			return;
		}
		memberRank.save(memberRank);	
		redirect("/memberRank/list");
	}
	
	// 更新
	@Before(MemberRankValidator.class)
	public void update(){
		memberRank = getModel(MemberRank.class);
		if (memberRank.getDouble("preferentialScale") < 0) {
			addActionError("优惠百分比必须大于或等于零!");
			return;
		}
		MemberRank persistent = MemberRank.dao.findById(memberRank.getStr("id"));
		MemberRank equalPointMemberRank = MemberRank.dao.getMemberRankByPoint(memberRank.getInt("point"));
		if (equalPointMemberRank != null && equalPointMemberRank != persistent) {
			addActionError("已存在相同积分的会员等级!");
			return;
		}
		memberRank.update(memberRank);		
		redirect("/memberRank/list");
	}
	
	// 删除
	public void delete() {
		String[] ids = getParaValues("ids");
		for (String id : ids) {			
			MemberRank memberRank = MemberRank.dao.findById(id);
			// 等级下存在会员
			List<Member> member = memberRank.getMember();
			
			// 获取所有实体对象总数.
			long totalCount = memberRank.getTotalCount();
			
			if(member != null && member.size() > 0){
				ajaxJsonErrorMessage("会员等级[" + memberRank.getStr("name") + "]下存在会员,删除失败!");
			}else if(ids.length >= totalCount){
				ajaxJsonErrorMessage("删除失败!必须至少保留一个会员等级");
			}else{
				MemberRank.dao.deleteById(id);
				ajaxJsonSuccessMessage("删除成功！");
			}
		}
	}
}
