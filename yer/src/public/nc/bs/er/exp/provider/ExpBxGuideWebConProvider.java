/** 
 * <p>��������:</p>
 * @version v63
 * <p>�ļ����� ExpBxGuideWebConProvider.java</p>
 * <p>�����˼�ʱ�䣺 hzy 2017-5-15����5:36:24</p>
 *
 * <p>�޸��ˣ�</p>
 * <p>�޸�ʱ�䣺</p>
 * <p>�޸�������</p>
 **/
package nc.bs.er.exp.provider;

import nc.uap.lfw.core.comp.IWebPartContentFetcher;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.LfwWindow;
import nc.uap.lfw.jsp.uimeta.UIMeta;

/** 
 * <p>����˵��:</p>
 * <p>�����˼�ʱ�䣺 hzy 2017-5-15����5:36:24</p>
 *
 * <p>�޸��ˣ�</p>
 * <p>�޸�ʱ�䣺</p>
 * <p>�޸�������</p>
 **/
public class ExpBxGuideWebConProvider implements IWebPartContentFetcher {

	/**
	 * <p>����������</p>
	 * <p>�����˼�ʱ�䣺 hzy 2017-5-15����5:36:24</p>
	 * (non-Javadoc)
	 * @see nc.uap.lfw.core.comp.IWebPartContentFetcher#fetchBodyScript(nc.uap.lfw.jsp.uimeta.UIMeta, nc.uap.lfw.core.page.LfwWindow, nc.uap.lfw.core.page.LfwView)
	 */
	@Override
	public String fetchBodyScript(UIMeta arg0, LfwWindow arg1, LfwView arg2) {
		// TODO �Զ����ɵķ������
		return null;
	}

	/**
	 * <p>����������</p>
	 * <p>�����˼�ʱ�䣺 hzy 2017-5-15����5:36:24</p>
	 * (non-Javadoc)
	 * @see nc.uap.lfw.core.comp.IWebPartContentFetcher#fetchHtml(nc.uap.lfw.jsp.uimeta.UIMeta, nc.uap.lfw.core.page.LfwWindow, nc.uap.lfw.core.page.LfwView)
	 */
	@Override
	public String fetchHtml(UIMeta arg0, LfwWindow arg1, LfwView arg2) {
		// TODO �Զ����ɵķ������
		String rsHTML = "<div id='insertphoto_top' align='center' class='topinfo_table_div'><div id='insertphoto' align='center' class='info_table_div'></div></div>";
		return rsHTML;
	}

}
