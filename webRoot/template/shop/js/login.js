/***
 *	JFinalShop Login JavaScript
 *
 *	http://www.jfinalshop.com
 *
 *	Copyright (c) 2014 JFinalShop. All rights reserved.
 **/


$().ready( function() {

	// 登录窗口显示
	$.loginWindowShow = function () {
		$("#loginWindow").jqmShow();
	}

	// 必须会员登录才允许的操作，若未登录则显示登录窗口
	$("a.mustMemberLogin").click( function(event) {
		if ($.cookie("loginMemberUsername") == null) {
			$.cookie("redirectionUrl", $(this).attr("href"), {path: "/"});
			$("#loginWindow").jqmShow();
			return false; 
		}
	});
	
	// 登录窗口
	var loginWindowHtml = '<div id="loginWindow" class="loginWindow"><div class="windowTop"><div class="windowTitle">会员登录</div><a class="windowClose loginWindowClose" href="#" hidefocus="true"></a></div><div class="windowMiddle"><form id="loginWindowForm" action="' + JFinalshop.base + '/shop/member/ajaxLogin" method="post"><table><tr><th>用户名: </th><td><input type="text" name="member.username" class="formText {required: true, messages: {required: \'请填写用户名!\'}}" /></td></tr><tr><th>密&nbsp;&nbsp;&nbsp;码: </th><td><input type="password" name="member.password" class="formText {required: true, messages: {required: \'请填写密码!\'}}" /></td></tr><tr><th>验证码: </th><td><input type="text" id="loginWindowCaptcha" name="captchaToken" class="formTextS {required: true, messages: {required: \'请填写验证码!\'}}" /><img id="loginWindowCaptchaImage" src="" alt="换一张" /></td></tr><tr><th>&nbsp;</th><td><span class="warnIcon">&nbsp;</span><a href="' + JFinalshop.base + '/shop/member/passwordRecover">忘记了密码? 点击找回!</a></td></tr><tr><th>&nbsp;</th><td><input type="submit" id="loginWindowSubmit" class="loginSubmit" value="登 录" hidefocus="true" /></td></tr></table></form></div><div class="windowBottom"></div></div>';

	$("body").prepend(loginWindowHtml);
	
	// 登录悬浮窗口
    $("#loginWindow").jqm({
		modal: true,// 是否开启模态窗口
		overlay: 30,// 屏蔽层透明度
		trigger: ".showLoginWindow",// 激活元素
		closeClass: "loginWindowClose",// 关闭按钮
		onHide: function(hash) {
			$("#loginWindowForm").resetForm();
			$.cookie("redirectionUrl", null, {path: "/"});
    		hash.o.remove();
    		hash.w.fadeOut();
    	},
    	onShow: function(hash){
    		hash.w.fadeIn();
    		loginWindowCaptchaImageRefresh();
	    }
	}).jqDrag(".windowTop");
	
	// 表单验证
	$("#loginWindowForm").validate({
		invalidHandler: function(form, validator) {
			$.each(validator.invalid, function(key, value){
				$.tip(value);
				return false;
			});
		},
		errorPlacement:function(error, element) {},
		submitHandler: function(form) {
			$("#loginWindowSubmit").attr("disabled", true);
			$("#loginWindowForm").ajaxSubmit({
				dataType: "json",
				success: function(data) {
					if (data.status == "success") {
						$.tip(data.status, data.message);
						$.flushHeaderInfo();
						$.flushCartItemList();
						var redirectionUrl = $.cookie("redirectionUrl");
						$("#loginWindow").jqmHide();
						if(redirectionUrl != null && redirectionUrl != "") {
							location.href = redirectionUrl;
						}
					} else {
						loginWindowCaptchaImageRefresh();
						$("#loginWindowCaptcha").val("");
						$.tip(data.status, data.message);
					}
					$("#loginWindowSubmit").attr("disabled", false);
				}
			});
		}
	});
	
	// 刷新验证码图片
	function loginWindowCaptchaImageRefresh() {
		$("#loginWindowCaptchaImage").attr("src", JFinalshop.base + "/captcha?timestamp=" + (new Date()).valueOf() + "&width=100&height=45&fontsize=35");
	}
	
	// 点击刷新验证码图片
	$("#loginWindowCaptchaImage").click( function() {
		loginWindowCaptchaImageRefresh();
	});
	
	// 刷新验证码图片
	function loginCaptchaImageRefresh() {
		$("#loginCaptchaImage").attr("src", JFinalshop.base + "/captcha?timestamp=" + (new Date()).valueOf() + "&width=100&height=45&fontsize=35");
	}
	
	// 点击刷新验证码图片
	$("#loginCaptchaImage").click( function() {
		loginCaptchaImageRefresh();
	});
	
	// 表单验证
	$("#loginForm").submit(function() {
		if ($("#loginUsername").val() == "") {
			$.message("请输入您的用户名!");
			return false;
		}
		if ($("#loginPassword").val() == "") {
			$.message("请输入您的密码!");
			return false;
		}
		if ($("#loginCaptcha").val() == "") {
			alert($("#loginCaptcha").val());
			$.message("请输入您的验证码!");
			return false;
		}
	})

});