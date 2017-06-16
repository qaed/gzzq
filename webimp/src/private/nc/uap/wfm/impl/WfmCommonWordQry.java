package nc.uap.wfm.impl;

import java.util.ArrayList;
import java.util.List;
import nc.bs.dao.DAOException;
import nc.uap.cpb.persist.dao.PtBaseDAO;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.uap.wfm.exception.WfmServiceException;
import nc.uap.wfm.itf.IWfmCommonWordQry;
import nc.uap.wfm.logger.WfmLogger;
import nc.uap.wfm.vo.WfmCommonWordVO;



public class WfmCommonWordQry
  implements IWfmCommonWordQry
{
  public WfmCommonWordQry() {}
  
  public WfmCommonWordVO[] getUserCommonWord(String userPK)
    throws WfmServiceException
  {
    PtBaseDAO dao = new PtBaseDAO();
    try {
      StringBuffer buf = new StringBuffer();
      buf.append("pk_obj='").append(userPK).append("' and scope = '").append(0).append("'");
      buf.append("order by cast(sort AS INT)");
      return (WfmCommonWordVO[])dao.queryByCondition(WfmCommonWordVO.class, buf.toString());
    }
    catch (DAOException e) {
      WfmLogger.error(e.getMessage(), e);
      throw new LfwRuntimeException(e.getMessage());
    }
  }
  
  public WfmCommonWordVO[] getCommonWord(String userPK, String pk_group) throws WfmServiceException {
    try {
      WfmCommonWordVO[] userVOs = getUserCommonWord(userPK);
      WfmCommonWordVO[] GroupVOs = getGroupCommonWord(pk_group);
      List<WfmCommonWordVO> list = new ArrayList();
      for (WfmCommonWordVO vo : userVOs) {
        list.add(vo);
      }
      for (WfmCommonWordVO vo : GroupVOs) {
        list.add(vo);
      }
      return (WfmCommonWordVO[])list.toArray(new WfmCommonWordVO[0]);
    } catch (WfmServiceException e) {
      WfmLogger.error(e.getMessage(), e);
      throw new LfwRuntimeException(e.getMessage());
    }
  }
  
  public WfmCommonWordVO[] getCommonWordWithCount(String userPK, String pk_group, int count) throws WfmServiceException { if (count <= 0)
      return null;
    try {
      WfmCommonWordVO[] allVOs = getCommonWord(userPK, pk_group);
      WfmCommonWordVO[] vos = new WfmCommonWordVO[count];
      for (int i = 0; i < count; i++) {
        if ((allVOs == null) || (i >= allVOs.length)) {
          vos[i] = null;
        } else
          vos[i] = allVOs[i];
      }
      return vos;
    } catch (WfmServiceException e) {
      WfmLogger.error(e.getMessage(), e);
      throw new LfwRuntimeException(e.getMessage());
    }
  }
  
  public WfmCommonWordVO[] getGroupCommonWord(String pk_group) throws WfmServiceException { PtBaseDAO dao = new PtBaseDAO();
    try {
      StringBuffer buf = new StringBuffer();
      buf.append("pk_group='").append(pk_group).append("' and scope = '").append(3).append("'");
      buf.append("order by cast(sort AS INT)");
      return (WfmCommonWordVO[])dao.queryByCondition(WfmCommonWordVO.class, buf.toString());
    }
    catch (DAOException e) {
      WfmLogger.error(e.getMessage(), e);
      throw new LfwRuntimeException(e.getMessage());
    }
  }
  
  public String getCommonContentsByPk(String pk_commonword) throws WfmServiceException {
    String contents = "";
    PtBaseDAO dao = new PtBaseDAO();
    StringBuffer buf = new StringBuffer();
    buf.append("pk_commonword='").append(pk_commonword).append("'");
    try {
      WfmCommonWordVO[] vos = (WfmCommonWordVO[])dao.queryByCondition(WfmCommonWordVO.class, buf.toString());
      if ((vos != null) && (vos.length > 0)) {
        contents = vos[0].getContents();
      }
    } catch (DAOException e) {
      WfmLogger.error(e.getMessage(), e);
      throw new LfwRuntimeException(e.getMessage());
    }
    return contents;
  }

/**
 * <p>功能描述：</p>
 * <p>创建人及时间： hzy 2017-4-19下午5:28:08</p>
 * (non-Javadoc)
 * @see nc.uap.wfm.itf.IWfmCommonWordQry#getGroupCommonByScope(java.lang.String, java.lang.String)
 */
	@Override
	public WfmCommonWordVO[] getGroupCommonByScope(String pk_group, String scope)
			throws WfmServiceException {
		// TODO 自动生成的方法存根
		PtBaseDAO dao = new PtBaseDAO();
		try {
	      StringBuffer buf = new StringBuffer();
	      buf.append("pk_group='").append(pk_group).append("' and scope = '").append(scope).append("'");
	      buf.append("order by cast(sort AS INT)");
	      return (WfmCommonWordVO[])dao.queryByCondition(WfmCommonWordVO.class, buf.toString());
	    }
	    catch (DAOException e) {
	      WfmLogger.error(e.getMessage(), e);
	      throw new LfwRuntimeException(e.getMessage());
	    }
	}
}