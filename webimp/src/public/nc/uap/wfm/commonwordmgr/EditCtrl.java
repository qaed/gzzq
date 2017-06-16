package nc.uap.wfm.commonwordmgr;

import java.util.Random;
import nc.bs.framework.common.NCLocator;
import nc.uap.cpb.log.CpLogger;
import nc.uap.cpb.persist.dao.PtBaseDAO;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.cmd.UifDatasetLoadCmd;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.ctx.ViewContext;
import nc.uap.lfw.core.ctx.WindowContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.data.Row;
import nc.uap.lfw.core.event.DialogEvent;
import nc.uap.lfw.core.event.MouseEvent;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.ViewModels;
import nc.uap.lfw.core.serializer.impl.Dataset2SuperVOSerializer;
import nc.uap.lfw.core.serializer.impl.SuperVO2DatasetSerializer;
import nc.uap.lfw.login.vo.LfwSessionBean;
import nc.uap.wfm.commonword.SortHelper;
import nc.uap.wfm.exception.WfmServiceException;
import nc.uap.wfm.itf.IWfmCommonWordBill;
import nc.uap.wfm.itf.IWfmCommonWordQry;
import nc.uap.wfm.vo.WfmCommonWordVO;

public class EditCtrl implements nc.uap.lfw.core.ctrl.IController
{
  private static final long serialVersionUID = 1L;
  
  public EditCtrl() {}
  
  public void okEvent(MouseEvent mouseEvent)
  {
    LfwView edit = AppLifeCycleContext.current().getWindowContext().getCurrentViewContext().getView();
    LfwView main = AppLifeCycleContext.current().getWindowContext().getViewContext("main").getView();
    String edittype = (String)getCurrentWinCtx().getAppAttribute("edit");
    Dataset mainDs = main.getViewModels().getDataset("commonwordds");
    Dataset ds = edit.getViewModels().getDataset("commonwordds");
    Row curRow = ds.getCurrentRowData().getSelectedRow();
    if (edittype.equals("add")) {
      UifDatasetLoadCmd cmd = new UifDatasetLoadCmd("commonwordds") {
        public void execute() {
          EditCtrl.this.loadds();
        }
        
      };
      cmd.execute();
      Row emptyRow = mainDs.getEmptyRow();
      emptyRow = (Row)curRow.clone();
      Random random = new Random();
      emptyRow.setRowId(String.valueOf(random.nextLong()));
      if ((emptyRow.getString(mainDs.nameToIndex("contents")) == null) || (emptyRow.getString(mainDs.nameToIndex("contents")).trim().length() == 0))
      {
        AppLifeCycleContext.current().getApplicationContext().getCurrentWindowContext().closeView("edit");
        return;
      }
      
      SortHelper helper = new SortHelper(ds);
      emptyRow.setInt(ds.nameToIndex("sort"), helper.getNewSort(ds));
      
      //20170419 hezy 增加提醒语设置[1109020202]节点，通过nodecode区别不同的节点进行过滤数据
      String nodecode = (String) LfwRuntimeEnvironment.getWebContext().getAppSession().getAttribute("nodecode");
      if("1109020202".equals(nodecode))
    	  emptyRow.setString(ds.nameToIndex("scope"), "99");
      else
    	  emptyRow.setString(ds.nameToIndex("scope"), "3");
      //注释原代码行
//      emptyRow.setString(ds.nameToIndex("scope"), "3");
      
      emptyRow.setString(ds.nameToIndex("pk_group"), nc.uap.lfw.core.LfwRuntimeEnvironment.getLfwSessionBean().getPk_unit());
      
      WfmCommonWordVO[] vos = (WfmCommonWordVO[])new Dataset2SuperVOSerializer().serialize(mainDs, emptyRow);
      new PtBaseDAO();vos[0].setPk_commonword(PtBaseDAO.generatePK(null));
      emptyRow.setString(ds.nameToIndex("pk_commonword"), vos[0].getPk_commonword());
      mainDs.addRow(emptyRow);
      if ((vos != null) && (vos.length > 0)) {
        try {
          ((IWfmCommonWordBill)uap.lfw.core.locator.ServiceLocator.getService(IWfmCommonWordBill.class)).addCommonWordVOs(vos);
        } catch (WfmServiceException e) {
          CpLogger.error(e.getMessage(), e);
        }
      }
    } else if (edittype.equals("edit")) {
      WfmCommonWordVO[] vos = (WfmCommonWordVO[])new Dataset2SuperVOSerializer().serialize(ds, curRow);
      //20170419 hezy 增加提醒语设置[1109020202]节点，通过nodecode区别不同的节点进行过滤数据
      String nodecode = (String) LfwRuntimeEnvironment.getWebContext().getAppSession().getAttribute("nodecode");
      if("1109020202".equals(nodecode)){
    	  for(WfmCommonWordVO vo : vos)
    		  vo.setScope("99");
      }
      //end
      if ((vos != null) && (vos.length > 0)) {
        try {
          ((IWfmCommonWordBill)uap.lfw.core.locator.ServiceLocator.getService(IWfmCommonWordBill.class)).updateCommonWordVOs(vos);
        } catch (WfmServiceException e) {
          CpLogger.error(e.getMessage(), e);
        }
      }
      Row row = mainDs.getSelectedRow();
      row.setValue(mainDs.nameToIndex("contents"), vos[0].getContents());
    }
    
    AppLifeCycleContext.current().getViewContext().getView().getWindow().setHasChanged(Boolean.valueOf(false));
    
    AppLifeCycleContext.current().getApplicationContext().getCurrentWindowContext().closeView("edit");
  }
  
  private void loadds() {
    Dataset ds = getDataset();
    ds.clear();
    IWfmCommonWordQry qry = (IWfmCommonWordQry)NCLocator.getInstance().lookup(IWfmCommonWordQry.class);
    WfmCommonWordVO[] vos;
    try {
      vos = qry.getGroupCommonWord(nc.uap.lfw.core.LfwRuntimeEnvironment.getLfwSessionBean().getPk_unit());
    } catch (WfmServiceException e) {
      throw new nc.uap.lfw.core.exception.LfwRuntimeException(e.getMessage());
    }
    if ((vos != null) && (vos.length != 0))
      new SuperVO2DatasetSerializer().serialize(vos, ds);
  }
  
  public void cancelEvent(MouseEvent mouseEvent) {
    AppLifeCycleContext.current().getWindowContext().closeView("edit");
  }
  
  public void beforeShow(DialogEvent dialogEvent) {
    String edit = (String)getCurrentWinCtx().getAppAttribute("edit");
    if (edit == null)
      edit = "";
    if (edit.equals("add")) {
      Dataset ds = getDataset();
      ds.clear();
      Row emptyRow = ds.getEmptyRow();
      ds.addRow(emptyRow);
      ds.setSelectedIndex(ds.getRowIndex(emptyRow));
      ds.setEnabled(true);
    } else if (edit.equals("edit")) {
      Dataset ds = getDataset();
      ds.clear();
      String pk_commonword = (String)AppLifeCycleContext.current().getApplicationContext().getAppAttribute("pk_commonword");
      try
      {
        WfmCommonWordVO vo = (WfmCommonWordVO)new PtBaseDAO().retrieveByPK(WfmCommonWordVO.class, pk_commonword);
        
        new SuperVO2DatasetSerializer().serialize(new WfmCommonWordVO[] { vo }, ds);
        ds.setRowSelectIndex(Integer.valueOf(0));
      }
      catch (nc.bs.dao.DAOException e) {
        throw new nc.uap.lfw.core.exception.LfwRuntimeException(e);
      }
      
      ds.setEnabled(true);
    }
  }
  
  private Dataset getDataset() {
    return AppLifeCycleContext.current().getViewContext().getView().getViewModels().getDataset("commonwordds");
  }
  
  private WindowContext getCurrentWinCtx() {
    return AppLifeCycleContext.current().getApplicationContext().getCurrentWindowContext();
  }
}