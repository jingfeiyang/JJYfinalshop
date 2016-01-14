/***
 *	JFinalShop Register JavaScript
 *
 *	http://www.jfinalshop.com
 *
 *	Copyright (c) 2014 JFinalShop. All rights reserved.
 **/

$().ready( function() {

	// 注册窗口显示
	$.registerWindowShow = function () {
		$("#registerWindow").jqmShow();
	}
	
	// 协议窗口显示
	$.agreementWindowShow = function () {
		$("#agreementWindow").jqmShow();
	}

	// 注册窗口
	var registerWindowHtml = '<div id="registerWindow" class="registerWindow"><div class="windowTop"><div class="windowTitle">会员注册</div><a class="windowClose registerWindowClose" href="#" hidefocus="true"></a></div><div class="windowMiddle"><form id="registerWindowForm" action="' + JFinalshop.base + '/shop/member/ajaxRegister" method="post"><table><tr><th>用户名: </th><td><input type="text" name="member.username" class="formText {required: true, username: true, remote: \'' + JFinalshop.base + '/shop/member/checkUsername\', minlength: 2, maxlength: 20, messages: {required: \'请输入用户名!\', username: \'不允许包含特殊字符!\', remote: \'用户名已存在,请重新输入!\'}}" title="用户名只允许包含中文、英文、数字和下划线!" /></td></tr><tr><th>密&nbsp;&nbsp;&nbsp;码: </th><td><input type="password" id="registerWindowPassword" name="member.password" class="formText {required: true, minlength: 4, maxlength: 20, messages: {required: \'请输入密码!\', minlength: \'密码长度不能小于4\', maxlength: \'密码长度不能大于20\'}}" /></td></tr><tr><th>重复密码: </th><td><input type="password" name="rePassword" class="formText {equalTo: \'#registerWindowPassword\', messages: {equalTo: \'两次密码输入不一致!\'}}" title="密码长度只允许在4-20之间!" /></td></tr><tr><th>E-mail: </th><td><input type="text" name="member.email" class="formText {required: true, email: true, messages: {required: \'请输入E-mail!\', email: \'E-mail格式错误!\'}}" /></td></tr><tr><th>验证码: </th><td><input type="text" id="registerWindowCaptcha" name="captchaToken" class="formTextS {required: true, messages: {required: \'请输入验证码!\'}}" messagePosition="#registerWindowCaptchaMessagePosition" /><img id="registerWindowCaptchaImage" src="" alt="换一张" /><span id="registerWindowCaptchaMessagePosition"></span></td></tr><tr><th>&nbsp;</th><td><label><input type="checkbox" id="isAgreeAgreement" name="isAgreeAgreement" class="{required: true, messages: {required: \'必须同意注册协议，才可进行注册操作！\'}}" value="true" checked messagePosition="#isAgreeAgreementMessagePosition" /><a id="showAgreementWindow" class="showAgreementWindow" href="#">已阅读并同意《注册协议》</a></label><span id="isAgreeAgreementMessagePosition"></span></td></tr><tr><th>&nbsp;</th><td><input type="submit" id="registerWindowSubmit" class="registerWindowSubmit" value="" hidefocus="true" /></td></tr></table></form></div><div class="windowBottom"></div></div>';

	// 协议窗口
	var agreementWindowHtml = '<div id="agreementWindow" class="agreementWindow"><div class="windowTop"><div class="windowTitle">注册协议</div><a class="windowClose agreementWindowClose" href="#" hidefocus="true"></a></div><div class="windowMiddle"><div id="agreementContent"></div><input type="button" id="agreementButton" class="agreementButton agreementWindowClose" value="" hidefocus="true" /></div><div class="windowBottom"></div></div>';
	
	$("body").prepend(registerWindowHtml).append(agreementWindowHtml);
	
	// 注册悬浮窗口
    $("#registerWindow").jqm({
		modal: true,// 是否开启模态窗口
		overlay: 30,// 屏蔽层透明度
		trigger: ".showRegisterWindow",// 激活元素
		closeClass: "registerWindowClose",// 关闭按钮
		onHide: function(hash) {
			$("#registerWindowForm").resetForm();
    		hash.o.remove();
    		hash.w.fadeOut();
    	},
    	onShow: function(hash){
    		hash.w.fadeIn();
    		registerWindowCaptchaImageRefresh();
	    }
	}).jqDrag(".windowTop");
	
	// 协议悬浮窗口
    $("#agreementWindow").jqm({
		modal: true,// 是否开启模态窗口
		overlay: 0,// 屏蔽层透明度
		trigger: ".showAgreementWindow",// 激活元素
		closeClass: "agreementWindowClose",// 关闭按钮
		onShow: function(hash){
			if ($.trim($("#agreementContent").html()) == "") {
				$.ajax({
					beforeSend: function(data) {
						$("#agreementContent").html('<span class="loadingIcon">&nbsp;</span> 加载中...');
					},
					url: JFinalshop.base + "/shop/member/agreement",
					success: function(data){
						$("#agreementContent").html(data);
					}
				});
			}
			hash.w.fadeIn();
	    }
	}).jqDrag(".windowTop");
	
	$("#agreementButton").click( function() {
    	$("#isAgreeAgreement").attr("checked", true);
    });
	
	// 表单验证
	$("#registerWindowForm").validate({
		invalidHandler: function(form, validator) {
			$.each(validator.invalid, function(key, value){
				$.tip(value);
				return false;
			});
		},
		errorPlacement:function(error, element) {},
		submitHandler: function(form) {
			$("#registerWindowSubmit").attr("disabled", true);
			$("#registerWindowForm").ajaxSubmit({
				dataType: "json",
				success: function(data) {
					if (data.status == "success") {
						$.tip(data.status, data.message);
						$.flushHeaderInfo();
						$("#registerWindow").jqmHide();
					} else {
						registerWindowCaptchaImageRefresh();
						$("#registerWindowCaptcha").val("");
						$.tip(data.status, data.message);
					}
					$("#registerWindowSubmit").attr("disabled", false);
				}
			});
		}
	});
	
	// 刷新验证码图片
	function registerWindowCaptchaImageRefresh() {
		$("#registerWindowCaptchaImage").attr("src", JFinalshop.base + "/captcha?timestamp=" + (new Date()).valueOf() + "&width=100&height=45&fontsize=35");
	}
	
	// 点击刷新验证码图片
	$("#registerWindowCaptchaImage").click( function() {
		registerWindowCaptchaImageRefresh();
	});

});