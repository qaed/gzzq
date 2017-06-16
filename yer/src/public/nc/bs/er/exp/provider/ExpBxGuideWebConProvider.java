/** 
 * <p>功能描述:</p>
 * @version v63
 * <p>文件名： ExpBxGuideWebConProvider.java</p>
 * <p>创建人及时间： hzy 2017-5-15下午5:36:24</p>
 *
 * <p>修改人：</p>
 * <p>修改时间：</p>
 * <p>修改描述：</p>
 **/
package nc.bs.er.exp.provider;

import nc.uap.lfw.core.comp.IWebPartContentFetcher;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.LfwWindow;
import nc.uap.lfw.jsp.uimeta.UIMeta;

/** 
 * <p>功能说明:</p>
 * <p>创建人及时间： hzy 2017-5-15下午5:36:24</p>
 *
 * <p>修改人：</p>
 * <p>修改时间：</p>
 * <p>修改描述：</p>
 **/
public class ExpBxGuideWebConProvider implements IWebPartContentFetcher {

	/**
	 * <p>功能描述：</p>
	 * <p>创建人及时间： hzy 2017-5-15下午5:36:24</p>
	 * (non-Javadoc)
	 * @see nc.uap.lfw.core.comp.IWebPartContentFetcher#fetchBodyScript(nc.uap.lfw.jsp.uimeta.UIMeta, nc.uap.lfw.core.page.LfwWindow, nc.uap.lfw.core.page.LfwView)
	 */
	@Override
	public String fetchBodyScript(UIMeta arg0, LfwWindow arg1, LfwView arg2) {
		// TODO 自动生成的方法存根
		return null;
	}

	/**
	 * <p>功能描述：</p>
	 * <p>创建人及时间： hzy 2017-5-15下午5:36:24</p>
	 * (non-Javadoc)
	 * @see nc.uap.lfw.core.comp.IWebPartContentFetcher#fetchHtml(nc.uap.lfw.jsp.uimeta.UIMeta, nc.uap.lfw.core.page.LfwWindow, nc.uap.lfw.core.page.LfwView)
	 */
	@Override
	public String fetchHtml(UIMeta arg0, LfwWindow arg1, LfwView arg2) {
		// TODO 自动生成的方法存根
		String rsHTML = "<div id='insertphoto_top' align='center' class='topinfo_table_div'><div id='insertphoto' align='center' class='info_table_div'></div></div>";
		return rsHTML;
	}

}
