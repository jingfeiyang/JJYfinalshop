package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class MemberRankValidator extends Validator{

	@Override
	protected void validate(Controller c) {
		validateRequiredString("memberRank.name", "errorMessages", "等级名称不允许为空!");
		validateRequiredString("memberRank.preferentialScale", "errorMessages", "优惠百分比不允许为空!");
		validateRequiredString("memberRank.point", "errorMessages", "所需积分不允许为空!");
		validateRequiredString("memberRank.isDefault", "errorMessages", "是否为默认等级不允许为空!");
		
		validateInteger("memberRank.point", 0, 100000, "errorMessages", "所需积分只允许为正整数或零!");		
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");		
	}
}
