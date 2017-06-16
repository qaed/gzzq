package nc.uap.portal.action;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nc.bs.framework.common.NCLocator;
import nc.itf.uap.IUAPQueryBS;
import nc.uap.lfw.core.BrowserSniffer;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.WebContext;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.uap.lfw.servletplus.annotation.Action;
import nc.uap.lfw.servletplus.annotation.Param;
import nc.uap.lfw.servletplus.annotation.Servlet;
import nc.uap.lfw.servletplus.core.impl.BaseAction;
import nc.uap.lfw.util.LfwClassUtil;
import nc.uap.portal.cache.PortalCacheManager;
import nc.uap.portal.deploy.vo.PtSessionBean;
import nc.uap.portal.exception.PortalServiceException;
import nc.uap.portal.exception.PortletDeleteException;
import nc.uap.portal.exception.UserAccessException;
import nc.uap.portal.log.PortalLogger;
import nc.uap.portal.login.util.HeartBreatRunner;
import nc.uap.portal.om.Page;
import nc.uap.portal.om.PageMenu;
import nc.uap.portal.om.Portlet;
import nc.uap.portal.om.PortletDisplayCategory;
import nc.uap.portal.plugins.PluginManager;
import nc.uap.portal.portlet.itf.IPortletDeletePlugin;
import nc.uap.portal.service.PortalServiceUtil;
import nc.uap.portal.service.itf.IPtPageService;
import nc.uap.portal.service.itf.IPtPortalPageRegistryService;
import nc.uap.portal.service.itf.IPtPortletRegistryService;
import nc.uap.portal.servlet.HeartBreatKeeper;
import nc.uap.portal.user.entity.IUserVO;
import nc.uap.portal.util.PortalPageDataWrap;
import nc.uap.portal.util.PortalRenderEnv;
import nc.uap.portal.util.PortletDataWrap;
import nc.uap.portal.util.ToolKit;
import nc.uap.portal.util.freemarker.FreeMarkerTools;
import nc.uap.portal.vo.PtPageVO;
import nc.uap.wfm.itf.IWfmCommonWordQry;
import nc.uap.wfm.vo.WfmCommonWordVO;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pubapp.AppContext;
import nc.vo.sm.UserVO;

import org.apache.commons.beanutils.MethodUtils;
import uap.json.JSONArray;
import uap.json.JSONObject;
import uap.lfw.core.locator.ServiceLocator;
import uap.lfw.core.ml.LfwResBundle;
import uap.lfw.portal.cache.itf.IPortalFrontCacheService;
import uap.lfw.portal.page.RenderResult;
import uap.lfw.portal.page.itf.IPageService;
import uap.web.bd.pub.AppUtil;

@Servlet(path = "/home")
public class PortalHomeAction extends BaseAction {
	private static final String BEFORE = "before";
	private static final String AFTER = "after";

	public PortalHomeAction() {
	}

	@Action
	public void index() throws IOException {
		try {
			Page[] myPages = PortalPageDataWrap.getUserPages();

			Page page = PortalPageDataWrap.getUserDefaultPage(myPages);

			PageMenu menu = PortalPageDataWrap.getUserMenu(myPages);

			showPage(page, myPages, menu);

			// 20170509 hezy 登陆后弹出提示框提示用户注意报销事项 begin
			IWfmCommonWordQry qry = (IWfmCommonWordQry) NCLocator.getInstance().lookup(IWfmCommonWordQry.class);
			WfmCommonWordVO[] vos = qry.getGroupCommonByScope(LfwRuntimeEnvironment.getLfwSessionBean().getPk_unit(), "99");
			// 20170615 tsy 解决空指针异常，存在单点登录的情况下，这个方法只有在刷新和登录的时候触发
			// Boolean showBXTip = (Boolean) (null ==
			// LfwRuntimeEnvironment.getWebContext().getAppSession().getAttribute("showBXTip")
			// ?
			// false :
			// LfwRuntimeEnvironment.getWebContext().getAppSession().getAttribute("showBXTip"));
			Boolean showBXTip =
					(Boolean) (LfwRuntimeEnvironment.getWebContext().getAppSession() == null ? false : null == LfwRuntimeEnvironment.getWebContext().getAppSession().getAttribute("showBXTip") ? false : LfwRuntimeEnvironment.getWebContext().getAppSession().getAttribute("showBXTip"));
			// 20170615 end
			IUAPQueryBS bs = NCLocator.getInstance().lookup(IUAPQueryBS.class);
			Collection col =
					bs.retrieveByClause(UserVO.class, " nvl(dr,0)=0 and cuserid='" + AppContext.getInstance().getPkUser() + "' and user_type=1 ");
			if (!showBXTip && !col.isEmpty()) {
				String msg = "";
				if (vos == null || vos.length == 0)
					msg = "未设置提示语！请用管理员登录，【系统管理 > 客户化配置 > 模板管理 > 提醒语设置 】维护信息！";
				else {
					msg = vos[0].getContents();
					if (msg == null || msg.length() == 0)
						msg = "未设置提示语！请用管理员登录，【系统管理 > 客户化配置 > 模板管理 > 提醒语设置 】维护信息！";
				}
				addExecScript("parent.showDefMessageDialog('" + msg + "',null,'温馨提示',null,false)");
				// 20170615 tsy 解决空指针异常
				// LfwRuntimeEnvironment.getWebContext().getAppSession().setAttribute("showBXTip",
				// true);
				if (LfwRuntimeEnvironment.getWebContext().getAppSession() != null) {
					LfwRuntimeEnvironment.getWebContext().getAppSession().setAttribute("showBXTip", true);
				}
				// 20170615 end
			}
			// end

		} catch (Throwable e) {
			PortalLogger.error(e.getMessage(), e);
			throw new LfwRuntimeException(e.getMessage(), e.getCause());
		}
	}

	private void showPage(Page page, Page[] myPages, PageMenu menu) throws IOException {
		doBreat();
		String reqETag = this.request.getHeader("If-None-Match");
		PtSessionBean bean = (PtSessionBean) LfwRuntimeEnvironment.getLfwSessionBean();
		IUserVO user = bean.getUser();
		setRenderEnv(page, myPages, user.getPk_user(), reqETag);
		RenderResult rsl = null;
		try {
			rsl = ((IPageService) ServiceLocator.getService(IPageService.class)).render(page, menu, user);
			if (rsl.getEtag() != null) {
				try {
					this.response.setHeader("ETag", rsl.getEtag());
				} catch (Exception e) {
					PortalLogger.info(rsl.getEtag());
					PortalLogger.error(e.getMessage(), e);
				}
				if ((reqETag != null) && (rsl.getEtag().equals(reqETag))) {
					this.response.setStatus(304);
					return;
				}
			}
			print(rsl.getHtml());
		} catch (PortalServiceException e) {
			PortalLogger.error(e.getMessage(), e);
			if (PortalCacheManager.getUserPageCache().size() > 1) {
				reView(page);
			} else {
				throw new LfwRuntimeException(LfwResBundle.getInstance().getStrByID("pserver", "PortalHomeAction-000000") + e.getMessage(), e);
			}
		}
	}

	private void setRenderEnv(Page page, Page[] myPages, String pk_user, String reqETag) {
		IPortalFrontCacheService frontCacheSrv = PortalServiceUtil.getFrontCacheService();
		if (reqETag != null) {
			Map<String, String> requestPortletEtag = frontCacheSrv.transPortletEtag(reqETag);
			PortalRenderEnv.setRequestPortletEtag(requestPortletEtag);
		}
		PortalRenderEnv.setLight(LfwRuntimeEnvironment.getBrowserInfo().isIE());
		PortalRenderEnv.setHtml5Model(!LfwRuntimeEnvironment.getBrowserInfo().isIE7());
	}

	public void doBreat() {
		HttpSession session = LfwRuntimeEnvironment.getWebContext().getRequest().getSession();
		Integer i = (Integer) session.getAttribute("KEEP_HEART_BEAT");
		if (i == null) {
			i = Integer.valueOf(0);
		}
		Integer localInteger1 = i;
		Integer localInteger2 = i = Integer.valueOf(i.intValue() + 1);
		session.setAttribute("KEEP_HEART_BEAT", i);
	}

	public void reView(Page page) throws IOException {
		String cacheKey = PortalPageDataWrap.modModuleName(page.getModule(), page.getPagename());
		PortalCacheManager.getUserPageCache().remove(cacheKey);
		PortalLogger.error("Remove Error page:" + cacheKey);
		gun(LfwRuntimeEnvironment.getRootPath());
	}

	@Action
	public void insertNewPortlet(@Param(name = "pageName") String pageName, @Param(name = "pageModule") String pageModule) {
		Map<String, PortletDisplayCategory> cates = PortalServiceUtil.getPortletRegistryService().getPortletDisplayCache();

		if ((cates == null) || (cates.isEmpty()))
			return;
		String ftlName = "portlets/insertPortlet.ftl";
		Map<String, Object> root = new HashMap();
		root.put("pageName", pageName);
		root.put("pageModule", pageModule);
		root.put("PortletDisplayCategory", cates.values());
		try {
			print(FreeMarkerTools.render(ftlName, root));
		} catch (PortalServiceException e) {
			PortalLogger.error(e.getMessage());
		}
	}

	@Action
	public void view(@Param(name = "pageModule") String model, @Param(name = "pageName") String pageName) throws IOException {
		try {
			Page[] myPages = PortalPageDataWrap.getUserPages();

			Page page = PortalPageDataWrap.getUserPage(myPages, model, pageName);

			PageMenu menu = PortalPageDataWrap.getUserMenu(myPages);

			showPage(page, myPages, menu);
		} catch (Throwable e) {
			throw new LfwRuntimeException(e.getMessage(), e);
		}
	}

	@Action
	public void layout(@Param(name = "pageName") String pageName, @Param(name = "pageModule") String pageModule, @Param(name = "portletId") String portletId, @Param(name = "destinationId") String destinationId, @Param(name = "isAfter") Boolean isAfter)
			throws IOException {
		try {
			Page[] myPages = PortalPageDataWrap.getUserPages();

			PtSessionBean sbean = (PtSessionBean) LfwRuntimeEnvironment.getLfwSessionBean();
			String userId = sbean.getPk_user();

			Page page = PortalPageDataWrap.getUserPage(myPages, pageModule, pageName);

			if ((page.getReadonly() == null) || (page.getReadonly().booleanValue())) {
				alert(NCLangRes4VoTransl.getNCLangRes().getStrByID("portal", "PortalSettingAction-000003"));

				return;
			}

			if (isAfter == null) {

				page.addPortletToBlankLayout(portletId, destinationId);

			} else {

				page.movePortlet(portletId, destinationId, isAfter);
			}

			page.setExtendAttribute("_TS", new UFDateTime());
			PortalServiceUtil.getRegistryService().registryUserPageCache(page);

			PtPageVO portalPageVO = new PtPageVO();
			portalPageVO.setFk_pageuser(userId);
			portalPageVO = PortalPageDataWrap.copyPage2PageVO(page, portalPageVO);
			PortalServiceUtil.getPageService().updateLayout(portalPageVO);
		} catch (PortalServiceException e) {
			throw new LfwRuntimeException(e.getMessage(), NCLangRes4VoTransl.getNCLangRes().getStrByID("portal", "PortalSettingAction-000004"), e);
		} catch (UserAccessException e) {
			logout();
		}
	}

	@Action
	public void doDelPortlet(@Param(name = "PageName") String pageName, @Param(name = "PageModule") String pageModule, @Param(name = "PortletModule") String portletModule, @Param(name = "PortletName") String portletId, @Param(name = "pid") String pid)
			throws IOException {
		JSONArray returnData = new JSONArray();
		try {
			portletDeletePluginExecutor(pageName, pageModule, portletModule, portletId, "before");

			JSONObject node = new JSONObject();
			node.put("err", 1);

			Page[] myPages = PortalPageDataWrap.getUserPages();

			PtSessionBean sbean = (PtSessionBean) LfwRuntimeEnvironment.getLfwSessionBean();
			String userId = sbean.getPk_user();

			Page page = PortalPageDataWrap.getUserPage(myPages, pageModule, pageName);

			if ((page.getReadonly() == null) || (page.getReadonly().booleanValue())) {
				node.put("msg", NCLangRes4VoTransl.getNCLangRes().getStrByID("portal", "PortalHomeAction-000000"));

				returnData.put(node);

			} else {

				boolean b = PortletDataWrap.hasPortlet(page, portletModule, portletId);

				if (!b) {
					node.put("msg", NCLangRes4VoTransl.getNCLangRes().getStrByID("portal", "PortalHomeAction-000001"));

					returnData.put(node);

				} else {

					page.removePortletElement(pid);

					page.setExtendAttribute("_TS", new UFDateTime());
					PortalServiceUtil.getRegistryService().registryUserPageCache(page);

					PtPageVO portalPageVO = new PtPageVO();
					portalPageVO.setFk_pageuser(userId);
					portalPageVO = PortalPageDataWrap.copyPage2PageVO(page, portalPageVO);
					PortalServiceUtil.getPageService().updateLayout(portalPageVO);
					node.put("msg", NCLangRes4VoTransl.getNCLangRes().getStrByID("portal", "PortalHomeAction-000002"));

					node.put("err", 0);
					returnData.put(node);

					portletDeletePluginExecutor(pageName, pageModule, portletModule, portletId, "after");
				}
			}
		} catch (UserAccessException e) {
			logout();
		} catch (PortalServiceException e) {
			PortalLogger.error(e.getMessage(), e);
		} finally {
			print(returnData);
		}
	}

	private void portletDeletePluginExecutor(String pageName, String pageModule, String portletModule, String portletId, String cmd) {
		List<IPortletDeletePlugin> plugines = PluginManager.newIns().getExtInstances("PORTLET_DELETE_PLUGIN", IPortletDeletePlugin.class);
		if (ToolKit.notNull(plugines)) {
			for (IPortletDeletePlugin ex : plugines) {
				try {
					if ("before".equals(cmd)) {
						ex.beforeDelete(pageName, pageModule, portletModule, portletId);
					} else if ("after".equals(cmd)) {
						ex.afterDelete(pageName, pageModule, portletModule, portletId);
					}
				} catch (PortletDeleteException e) {
					PortalLogger.error(e.getMessage(), e);
					throw new LfwRuntimeException(e.getHint());
				} catch (Throwable th) {
					PortalLogger.error(LfwResBundle.getInstance().getStrByID("pserver", "PortalHomeAction-000001") + th.getMessage(), th);
				}
			}
		}
	}

	@Action
	public void doInsertNewPortlet(@Param(name = "pageName") String pageName, @Param(name = "pageModule") String pageModule, @Param(name = "portletModule") String portletModule, @Param(name = "portletId") String portletId, @Param(name = "skin") String skin)
			throws IOException {
		try {
			Page[] myPages = PortalPageDataWrap.getUserPages();

			PtSessionBean sbean = (PtSessionBean) LfwRuntimeEnvironment.getLfwSessionBean();
			String userId = sbean.getPk_user();

			Page page = PortalPageDataWrap.getUserPage(myPages, pageModule, pageName);

			if ((page.getReadonly() == null) || (page.getReadonly().booleanValue())) {
				alert(NCLangRes4VoTransl.getNCLangRes().getStrByID("portal", "PortalHomeAction-000003"));

				return;
			}

			boolean b = PortletDataWrap.hasPortlet(page, portletModule, portletId);

			if (b) {
				alert(NCLangRes4VoTransl.getNCLangRes().getStrByID("portal", "PortalHomeAction-000004"));

				return;
			}

			Portlet portlet = PortalPageDataWrap.creatPortlet(portletId, portletModule, skin);
			page.insertNewPortlet(portlet);

			String key = PortalPageDataWrap.modModuleName(pageModule, pageName);
			PortalCacheManager.getUserPageCache().put(key, page);

			PtPageVO portalPageVO = new PtPageVO();
			portalPageVO.setFk_pageuser(userId);
			portalPageVO = PortalPageDataWrap.copyPage2PageVO(page, portalPageVO);
			PortalServiceUtil.getPageService().updateLayout(portalPageVO);
			alert(NCLangRes4VoTransl.getNCLangRes().getStrByID("portal", "PortalHomeAction-000005"));

			addExecScript("parent.insertOK();");
		} catch (UserAccessException e) {
			logout();
		} catch (PortalServiceException e) {
			PortalLogger.error(e.getMessage(), e);
		}
	}

	@Action
	public void logout() throws IOException {
		String logoutScript = "";
		try {
			Object osl = LfwClassUtil.newInstance("nc.uap.portal.integrate.system.OtherSystemLogout");
			logoutScript = (String) MethodUtils.invokeMethod(osl, "getOtherSysLogoutScript", null);
		} catch (Throwable e) {
			PortalLogger.error(NCLangRes4VoTransl.getNCLangRes().getStrByID("portal", "PortalHomeAction-000006", null, new String[] { e.getMessage() }), e);
		}

		StringBuffer output = new StringBuffer(logoutScript);
		String loginUrl = this.request.getContextPath() + "/?logout=1";
		output.append("<script>window.location='" + loginUrl + "';</script>");

		Cookie p_auth = new Cookie("p_logoutflag", "y");
		p_auth.setMaxAge(604800000);
		p_auth.setPath("/portal");
		this.response.addCookie(p_auth);
		print(output);

		this.request.getSession().invalidate();
	}

	@Action
	public void stopBreat() throws IOException {
		HttpSession session = this.request.getSession(false);
		if ((session != null) && (session.getAttribute("KEEP_HEART_BEAT") != null)) {
			Integer i = (Integer) session.getAttribute("KEEP_HEART_BEAT");
			Integer localInteger1 = i;
			Integer localInteger2 = i = Integer.valueOf(i.intValue() - 1);
			if (i.intValue() == 0) {
				session.removeAttribute("KEEP_HEART_BEAT");
			} else {
				session.setAttribute("KEEP_HEART_BEAT", i);
			}
			HeartBreatRunner.addKeeper(new HeartBreatKeeper(session));
		}
	}
}