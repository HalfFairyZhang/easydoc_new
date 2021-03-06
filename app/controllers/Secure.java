package controllers;


import java.util.Date;

import models.User;
import play.libs.Crypto;
import play.mvc.Controller;
import play.mvc.Http;
import utils.MD5;

public class Secure extends Controller {

	public static void login() {
		//记住操作
		Http.Cookie remember = request.cookies.get("rememberme");
        if(remember != null && remember.value.indexOf("-") > 0) {
            String sign = remember.value.substring(0, remember.value.indexOf("-"));
            String username = remember.value.substring(remember.value.indexOf("-") + 1);
            if(Crypto.sign(username).equals(sign)) {
            	User user = User.find("username =?",username).first();
            	if(user!=null){
            		if(user.status==2){
        				flash.error("Account is locked.");
        				login();
        			}
            		user.login = user.login +1;
                	user.save();
	            	session.put("userid",user.id);
	                session.put("username", username);
	                session.put("usermenu", user.role.menu);
	                Application.index();
            	}
            }
        }
		render();
	}

	public static void authenticate(String username, String password,boolean remember) {
		validation.required(username);
		validation.required(password);
		if (validation.hasErrors()) {
			params.flash();
			flash.error("用户名或者密码不能为空.");
			login();
		}
		User user = User.find("username =? and password =?",username,MD5.hash(password)).first();
		if (user!=null) {
			if(user.status==2){
				flash.error("用户名被锁，不能登录.");
				login();
			}
			user.login = user.login +1;
        	user.save();
			session.put("userid", user.id);
			session.put("username", user.truename);
			session.put("usermenu", user.role.menu); 
			if (remember) {
				response.setCookie("rememberme", Crypto.sign(username) + "-"+ username, "30d");
			}
			Application.index();
		} else {
			params.flash();
			flash.error("登录失败，用户名或者密码错误.");
			login();
		}
	}

	public static void logout() {
		session.clear();
		response.setCookie("rememberme", "", null);
		flash.success("secure.logout");
		login();
	}

}
