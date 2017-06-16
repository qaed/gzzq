package nc.uap.wfm.itf;

import nc.uap.wfm.exception.WfmServiceException;
import nc.uap.wfm.vo.WfmCommonWordVO;

public abstract interface IWfmCommonWordQry
{
  public abstract WfmCommonWordVO[] getUserCommonWord(String paramString)
    throws WfmServiceException;
  
  public abstract WfmCommonWordVO[] getGroupCommonWord(String paramString)
    throws WfmServiceException;
  
  public abstract WfmCommonWordVO[] getCommonWord(String paramString1, String paramString2)
    throws WfmServiceException;
  
  public abstract WfmCommonWordVO[] getCommonWordWithCount(String paramString1, String paramString2, int paramInt)
    throws WfmServiceException;
  
  public abstract String getCommonContentsByPk(String paramString)
    throws WfmServiceException;
  
  /**
   * <p>功能描述:提醒语设置   查询入口</p>
   * <p>创建人及时间： hzy 2017-4-19下午5:27:00</p>
   * @param paramString
   * @param scope
   * @return
   * @throws WfmServiceException
   */
  public abstract WfmCommonWordVO[] getGroupCommonByScope(String paramString , String scope)
		    throws WfmServiceException;
}