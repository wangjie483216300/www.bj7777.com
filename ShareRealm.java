package com.smart.common.shiro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.Sha256CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.smart.system.entity.system.SysResource;
import com.smart.system.entity.system.SysRole;
import com.smart.system.entity.system.SysUser;
import com.smart.system.service.system.SysResourceService;
import com.smart.system.service.system.SysRoleService;
import com.smart.system.service.system.SysUserService;

import javax.json.JsonObject;

/**
* @class EducRealm 
* @description <p>权限逻辑领域</p> 
 */
@SuppressWarnings("deprecation")
public class ShareRealm extends AuthorizingRealm {

	@Autowired
	private SysUserService userService;
	
	@Autowired
	private SysRoleService roleService;
	
	@Autowired
	private SysResourceService resourceService;


	// @Autowired
	//在配置文件credentialsMatcher处配置
	/*public ShareRealm() {
		setName("ShareRealm");
		setCredentialsMatcher(new Sha256CredentialsMatcher());
	}*/

	/**
	 * 认证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) {
		final UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
		if (StringUtils.isBlank(token.getUsername())) {
			return null;
		}
		final SysUser user = userService.getUserByName(token.getUsername());
		if (null != user) {
			try {
				/**
				 * 返回AuthenticationInfo的实现类
				 * 	1、认证的实体信息（可以是对象，用户实体类）
				 *  2、密码   加密
				 *  3、当前realmName的名字：调用父类的getName()方法
						 *  AuthenticationInfo主要是用来获取认证信息的，它实现了Serializable接口，先对其解析如下：
						 * 1.获取主题的身份(或者可以理解为用户名，有一个主身份)
						 * PrincipalCollection getPrincipals();
						 * 2.获取主题的凭证(或者可以理解为密码)
						 * Object getCredentials();
				 * 4、盐值
				 */
				return new SimpleAuthenticationInfo(user, user.getPwd(), getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 授权
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		final SysUser user = (SysUser) principals.fromRealm(getName()).iterator().next();
		return getAuthorizationInfo(user);
	}

	private AuthorizationInfo getAuthorizationInfo(SysUser user) {
		//
		final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		final Session session = SecurityUtils.getSubject().getSession();
		Map<String,Object> map = new HashMap<String, Object>(); 
		// 角色集合 一个人可对应多个角色
		map.put("userId", String.valueOf(user.getId()));
		map.put("userCode", user.getUserCode());
		final List<SysRole> roles = roleService.getRoles(map);
		List<SysResource> roleResources = Lists.newArrayList();
		List<SysResource> menus = Lists.newArrayList();
		
		
		// 操作权限
		if (null != roles) {
			
			List<Integer> roleIds=new ArrayList<Integer>();
			for(SysRole role: roles){
				//获取用户的角色id集合
				roleIds.add(role.getId());
			}
			
			if(roleIds.size()!=0){
				//根据多个角色获得菜单权限
				map.put("userLevel", String.valueOf(user.getUserLevel()));
				map.put("roleIds", roleIds);
//				System.out.println("----------------------@@@@@@@@@@@@@@@@@@@@@@@@@@@-------------------------------------------------------------------------");
//				System.out.println(new JSONObject(map).toString());
//				System.out.println("----------------------@@@@@@@@@@@@@@@@@@@@@@@@@@@-------------------------------------------------------------------------");
				menus=resourceService.getMenuResources(map);
				//根据多个角色获得资源权限
				roleResources = resourceService.selResourcesByRoleIds(map);
				System.out.println("----------------------roleResourcesroleResourcesroleResourcesroleResources-------------------------------------------------------------------------");
				System.out.println(roleResources.toString());
				System.out.println("----------------------roleResourcesroleResourcesroleResourcesroleResources-------------------------------------------------------------------------");

			}
			if (null != roleResources && roleResources.size() > 0) {
				for (SysResource resource : roleResources) {
					//添加权限字符串信息
					info.addStringPermission(resource.getResFlag());
//					System.out.println("----------------------infoinfoinfoinfoinfoinfoinfoinfoinfoinfoinfoinfoinfo-------------------------------------------------------------------------");
//					System.out.println(info.toString());
//					System.out.println("----------------------infoinfoinfoinfoinfoinfoinfoinfoinfoinfoinfoinfoinfo-------------------------------------------------------------------------");
				}
			}							
		}
		
		if (roleResources.size() > 0) {
			session.setAttribute("authzation", roleResources);
		}
		// 菜单权限
		if (null != menus && menus.size() > 0) {
			
			session.setAttribute("menu", menus);
		}
		return info;
	}

	/**
	 * 更新用户授权信息缓存.
	 */
	public void clearCachedAuthorizationInfo(String principal) {
		final SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
		clearCachedAuthorizationInfo(principals);
	}
	
	public void clearCachedAuthorizationInfo(){
		clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());  
	}

	/**
	 * 清除所有用户授权信息缓存.
	 */
	public void clearAllCachedAuthorizationInfo() {
		final Cache<Object, AuthorizationInfo> cache = getAuthorizationCache();
		if (cache != null) {
			for (Object key : cache.keys()) {
				cache.remove(key);
			}
		}
	}

}
