package nc.ui.gl.vouchercard;

import java.awt.Container;
import nc.bs.logging.Logger;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.gl.uicfg.IBasicModel;
import nc.ui.gl.uicfg.IBasicView;
import nc.ui.gl.uicfg.ValueChangeEvent;
import nc.ui.gl.uicfg.voucher.VoucherCell;
import nc.ui.gl.vouchertools.VoucherDataCenter;
import nc.vo.gateway60.pub.GlBusinessException;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.pub.lang.UFBoolean;

public class VoucherControl implements IVoucherControl
{
  IVoucherModel m_VoucherModel = null;
  
  IVoucherView m_VoucherView = null;
  





  public VoucherControl() {}
  





  public void adjustViewEditable(VoucherVO voucher)
  {
    boolean editable = true;
    if (((voucher.getDiscardflag() != null) && (voucher.getDiscardflag().booleanValue())) || (voucher.getPk_casher() != null) || (voucher.getPk_checked() != null) || (voucher.getPk_manager() != null) || ((VoucherDataCenter.isVoucherSelfEditDelete(voucher.getPk_accountingbook())) && (!GlWorkBench.getLoginUser().equals(voucher.getPk_prepared())))) {
      editable = false;
    } else if (getVoucherModel().getParameter("functionname") == null) {
      editable = false;
    } else if ((!getVoucherModel().getParameter("functionname").toString().trim().equals("preparevoucher")) && (!getVoucherModel().getParameter("functionname").toString().trim().equals("voucherbridge")))
    {





      if ((getVoucherModel().getParameter("functionname").toString().trim().equals("offsetvoucher")) && (voucher.getOffervoucher() != null) && (voucher.getPk_voucher() != null)) {
        editable = false;
      }
      else if (!getVoucherModel().getParameter("functionname").toString().trim().equals("offsetvoucher"))
      {


        editable = false; } }
    if ((voucher.getIsmatched() != null) && (voucher.getIsmatched().booleanValue())) {
      editable = false;
    }
    
    IVoucherView voucherView = (IVoucherView)getVoucherModel().getUI();
    if (!voucherView.isEditable()) {
      editable = false;
    }
    
    getVoucherView().setEditable(editable);
  }
  




  private IVoucherModel getVoucherModel()
  {
    return this.m_VoucherModel;
  }
  




  private IVoucherView getVoucherView()
  {
    return this.m_VoucherView;
  }
  





  public void setBasicModel(IBasicModel model)
  {
    this.m_VoucherModel = ((IVoucherModel)model);
  }
  





  public void setBasicView(IBasicView view)
  {
    this.m_VoucherView = ((IVoucherView)view);
  }
  


  public void valueChanged(ValueChangeEvent evt)
  {
    try
    {
      int iKey = ((Integer)evt.getKey()).intValue();
      if (getVoucherView() != null)
      {
        switch (iKey)
        {

        case 0: 
          adjustViewEditable((VoucherVO)evt.getNewValue());
          getVoucherView().refresh(-1, Integer.valueOf(0), null);
 
          break;
        

        case 55: 
          getVoucherView().stopEditing();
          getVoucherView().refresh(evt.getIndex(), evt.getKey(), evt.getNewValue());
          break;
        

        case 53: 
          getVoucherModel().resetDataByField(-1, iKey);
          getVoucherView().refresh(evt.getIndex(), evt.getKey(), evt.getNewValue());
          break;
        

        case 17: 
          getVoucherModel().resetDataByField(-1, iKey);
          getVoucherView().refresh(evt.getIndex(), evt.getKey(), evt.getNewValue());
          getVoucherView().setFocus(-1, Integer.valueOf(iKey));
          break;
        

        case 11: 
          getVoucherModel().resetDataByField(-1, iKey);
          getVoucherView().refresh(evt.getIndex(), evt.getKey(), evt.getNewValue());
          break;
        

        case 32: 
          if ((evt.getNewValue() != null) && (new Integer(evt.getNewValue().toString()).intValue() == 1))
          {
            Object cell = getVoucherView().getVoucherCellEditor(evt.getIndex(), evt.getKey());
            if ((cell instanceof VoucherCell))
            {
              ((VoucherCell)cell).setEditable(false);
            }
          }
          else
          {
            Object cell = getVoucherView().getVoucherCellEditor(evt.getIndex(), evt.getKey());
            if ((cell instanceof VoucherCell))
            {
              ((VoucherCell)cell).setEditable(true);
            }
          }
          
          getVoucherView().refresh(evt.getIndex(), evt.getKey(), evt.getNewValue());
          break;
//			20161223-tsy-修改自定义项目1
        case 151:
//        	end
        case 103: 
          getVoucherModel().resetDataByField(evt.getIndex(), iKey);
          getVoucherView().refresh(evt.getIndex(), evt.getKey(), evt.getNewValue());
          break;
        

        case 16: 
          getVoucherModel().resetDataByField(evt.getIndex(), iKey);
          getVoucherView().refresh(evt.getIndex(), evt.getKey(), evt.getNewValue());
          break;
        case 700: 
          break;
        
        default: 
          getVoucherView().refresh(evt.getIndex(), evt.getKey(), evt.getNewValue());
        }
         }
      switch (iKey)
      {

      case 0: 
        getVoucherModel().setSaved(true);
        break;
      

      default: 
        getVoucherModel().setSaved(false);
      }
      
    }
    catch (GlBusinessException e)
    {
      Logger.error(e.getMessage(), e);
      nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage((Container)getVoucherView(), "", e.getMessage());
    }
  }
}