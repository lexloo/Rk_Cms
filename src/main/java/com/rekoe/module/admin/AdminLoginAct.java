package com.rekoe.module.admin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.view.ForwardView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.web.Webs;

import com.rekoe.domain.User;
import com.rekoe.exception.IncorrectCaptchaException;
import com.rekoe.filter.CaptchaFormAuthenticationFilter;


@IocBean
@At("/admin")
public class AdminLoginAct {
	private static final Log logger = Logs.get();

	@At
	@Filters(@By(type = CaptchaFormAuthenticationFilter.class))
	public View login(@Attr("loginToken") AuthenticationToken token) {
		try {
			Subject subject = SecurityUtils.getSubject();
			ThreadContext.bind(subject);
			subject.login(token);
			return new ServerRedirectView("/admin/main.rk");
		} catch (LockedAccountException e) {
			return new ViewWrapper(new ForwardView("/admin/index.rk"), e.getMessage());
		} catch (UnknownAccountException e) {
			return new ViewWrapper(new ForwardView("/admin/index.rk"), e.getMessage());
		} catch (IncorrectCaptchaException e) {
			return new ViewWrapper(new ForwardView("/admin/index.rk"), e.getMessage());
		}catch(IncorrectCredentialsException e){
			return new ViewWrapper(new ForwardView("/admin/index.rk"), "密码错误");
		} catch (AuthenticationException e) {
			return new ViewWrapper(new ForwardView("/admin/index.rk"), e.getMessage());
		} catch (Exception e) {
			return new ViewWrapper(new ForwardView("/admin/index.rk"), e.getMessage());
		}
	}

	@At
	@Ok(">>:/admin/index.rk")
	@RequiresAuthentication
	public void logout() {
		Subject currentUser = SecurityUtils.getSubject();
		try {
			currentUser.logout();
		} catch (SessionException ise) {
			logger.debug("Encountered session exception during logout.  This can generally safely be ignored.", ise);
		} catch (Exception e) {
			logger.debug("登出发生错误", e);
		}
	}

	@At
	@Ok("fm:template.login.login")
	@Filters
	public String index() {
		return null;
	}

	@At
	@Ok("fm:template.front.account.create_user")
	@RequiresAuthentication
	public Object register(@Attr(Webs.ME) User user) {
		if (Lang.isEmpty(user) || user.isUpdated()) {
			return new ForwardView("/admin/common/unauthorized.rk");
		}
		return null;
	}
}
