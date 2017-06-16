package nc.uap.wfm.commonwordmgr;

import java.util.Map;
import nc.bs.framework.common.NCLocator;
import nc.uap.cpb.log.CpLogger;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.cmd.UifDatasetLoadCmd;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.ctx.WindowContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.data.Row;
import nc.uap.lfw.core.event.DataLoadEvent;
import nc.uap.lfw.core.event.MouseEvent;
import nc.uap.lfw.core.model.plug.TranslatedRow;
import nc.uap.lfw.core.serializer.impl.Dataset2SuperVOSerializer;
import nc.uap.lfw.login.vo.LfwSessionBean;
import nc.uap.wfm.commonword.SortHelper;
import nc.uap.wfm.exception.WfmServiceException;
import nc.uap.wfm.itf.IWfmCommonWordBill;
import nc.uap.wfm.itf.IWfmCommonWordQry;
import nc.uap.wfm.vo.WfmCommonWordVO;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import uap.lfw.core.locator.ServiceLocator;

public class MainCtrl implements nc.uap.lfw.core.ctrl.IController
{
  private static final long serialVersionUID = 1L;
  
  public MainCtrl() {}
  
  public void addEvent(MouseEvent mouseEvent)
  {
    AppLifeCycleContext.current().getWindowContext().popView("edit", "502", "263", NCLangRes4VoTransl.getNCLangRes().getStrByID("commonwords", "MainCtrl-000000"));
    

    getCurrentWinCtx().addAppAttribute("edit", "add");
  }
  
  public void delEvent(MouseEvent mouseEvent) { new nc.uap.lfw.core.cmd.UifDelCmd("commonwordds", nc.uap.lfw.core.vo.LfwExAggVO.class.getName()).execute();
    nc.uap.lfw.core.cmd.CmdInvoker.invoke(new UifDatasetLoadCmd("commonwordds")
    {
      public void execute() { MainCtrl.this.loadds(); }
    });
  }
  
  public void upEvent(MouseEvent mouseEvent) {
    Dataset ds = getDataset();
    Row row = ds.getSelectedRow();
    if (row != null) {
      SortHelper helper = new SortHelper(ds);
      Row frontRow = helper.getFrontRow(row);
      if (frontRow != null) {
        helper.up(row);
        WfmCommonWordVO[] vos = (WfmCommonWordVO[])new Dataset2SuperVOSerializer().serialize(ds, new Row[] { row, frontRow });
        try
        {
          ((IWfmCommonWordBill)ServiceLocator.getService(IWfmCommonWordBill.class)).updateCommonWordVOs(vos);
        } catch (WfmServiceException e) {
          CpLogger.error(e.getMessage(), e);
        }
      }
    }
  }
  
  public void downEvent(MouseEvent mouseEvent) { Dataset ds = getDataset();
    Row row = ds.getSelectedRow();
    if (row != null) {
      SortHelper helper = new SortHelper(ds);
      Row nextRow = helper.getNextRow(row);
      if (nextRow != null) {
        helper.down(row);
        WfmCommonWordVO[] vos = (WfmCommonWordVO[])new Dataset2SuperVOSerializer().serialize(ds, new Row[] { row, nextRow });
        try
        {
          ((IWfmCommonWordBill)ServiceLocator.getService(IWfmCommonWordBill.class)).updateCommonWordVOs(vos);
        } catch (WfmServiceException e) {
          CpLogger.error(e.getMessage(), e);
        }
      }
    }
  }
  
  public void onDataLoad_commonwordds(DataLoadEvent dataLoadEvent) { 
	  Dataset ds = (Dataset)dataLoadEvent.getSource();
	  nc.uap.lfw.core.cmd.CmdInvoker.invoke(new UifDatasetLoadCmd(ds.getId()) {
      public void execute() {
        MainCtrl.this.loadds();
      }
    });
  }
  
  private Dataset getDataset() { return AppLifeCycleContext.current().getViewContext().getView().getViewModels().getDataset("commonwordds"); }
  
  private WindowContext getCurrentWinCtx()
  {
    return AppLifeCycleContext.current().getApplicationContext().getCurrentWindowContext();
  }
  
  public void pluginedit2main_plugin(Map keys) {
    Dataset ds = getDataset();
    Row emptyRow = ds.getEmptyRow();
    setValues(emptyRow, ds, keys);
    if ((emptyRow.getString(ds.nameToIndex("contents")) == null) || (emptyRow.getString(ds.nameToIndex("contents")).trim().length() == 0))
    {

      AppLifeCycleContext.current().getApplicationContext().getCurrentWindowContext().closeView("edit");
      
      return;
    }
    SortHelper helper = new SortHelper(ds);
    emptyRow.setInt(ds.nameToIndex("sort"), helper.getNewSort(ds));
    emptyRow.setString(ds.nameToIndex("scope"), "3");
    
    emptyRow.setString(ds.nameToIndex("pk_group"), LfwRuntimeEnvironment.getLfwSessionBean().getPk_unit());
    
    WfmCommonWordVO[] vos = (WfmCommonWordVO[])new Dataset2SuperVOSerializer().serialize(ds, emptyRow);
    
    if ((vos != null) && (vos.length > 0)) {
      try {
        ((IWfmCommonWordBill)ServiceLocator.getService(IWfmCommonWordBill.class)).addCommonWordVOs(vos);
      } catch (WfmServiceException e) {
        CpLogger.error(e.getMessage(), e);
      }
    }
    
    AppLifeCycleContext.current().getApplicationContext().getCurrentWindowContext().closeView("edit");
    
    UifDatasetLoadCmd cmd = new UifDatasetLoadCmd("commonwordds") {
      public void execute() {
        MainCtrl.this.loadds();
      }
    };
    cmd.execute();
  }
  
  private void setValues(Row row, Dataset ds, Map map) { TranslatedRow r = (TranslatedRow)map.get("row");
    String[] keys = r.getKeys();
    
    for (String key : keys)
      row.setValue(ds.nameToIndex(key), r.getValue(key));
  }
  
  private void loadds() {
    Dataset ds = getDataset();
    ds.clear();
    //20170419 hezy 增加提醒语设置[1109020202]节点，通过nodecode区别不同的节点进行过滤数据
    String nodecode = (String) LfwRuntimeEnvironment.getWebContext().getAppSession().getAttribute("nodecode");
    
    
    IWfmCommonWordQry qry = (IWfmCommonWordQry)NCLocator.getInstance().lookup(IWfmCommonWordQry.class);
    WfmCommonWordVO[] vos;
    try
    {
    	//20170419 hezy 注释，根据节点重写
//      vos = qry.getGroupCommonWord(LfwRuntimeEnvironment.getLfwSessionBean().getPk_unit());
    	if("1109020202".equals(nodecode)){
    		vos = qry.getGroupCommonByScope(LfwRuntimeEnvironment.getLfwSessionBean().getPk_unit(),"99");
    	}else
    		vos = qry.getGroupCommonWord(LfwRuntimeEnvironment.getLfwSessionBean().getPk_unit());
    }
    catch (WfmServiceException e) {
      throw new nc.uap.lfw.core.exception.LfwRuntimeException(e.getMessage());
    }
    if ((vos != null) && (vos.length != 0))
      new nc.uap.lfw.core.serializer.impl.SuperVO2DatasetSerializer().serialize(vos, ds);
  }
  
  public void editonclick(MouseEvent mouseEvent) { Dataset ds = getDataset();
    Row selectedRow = ds.getSelectedRow();
    if (selectedRow != null) {
      WfmCommonWordVO[] vos = (WfmCommonWordVO[])new Dataset2SuperVOSerializer().serialize(ds, selectedRow);
      

      AppLifeCycleContext.current().getApplicationContext().addAppAttribute("pk_commonword", vos[0].getPk_commonword());
      AppLifeCycleContext.current().getWindowContext().popView("edit", "502", "263", NCLangRes4VoTransl.getNCLangRes().getStrByID("commonwords", "MainCtrl-000001"));
      

      getCurrentWinCtx().addAppAttribute("edit", "edit");
    } else {
      throw new nc.uap.lfw.core.exception.LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("wfm", "MainCtrl-000025"));
    }
  }
}