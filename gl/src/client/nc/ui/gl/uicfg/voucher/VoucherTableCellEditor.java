package nc.ui.gl.uicfg.voucher;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import nc.bs.logging.Logger;
import nc.ui.bd.manage.UIRefCellEditorNew;
import nc.ui.gl.accsubjref.AccsubjRefPane;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.gl.glref.GLBUVersionWithBookRefModel;
import nc.ui.gl.ref.CheckStlyeRef;
import nc.ui.gl.voucher.dlg.VerifyTableModel;
import nc.ui.gl.voucher.ref.DetailAssistantRefPane;
import nc.ui.gl.voucher.ref.QuantityAmountRefPane;
import nc.ui.gl.vouchercard.IVoucherModel;
import nc.ui.gl.vouchertools.VoucherDataCenter;
import nc.ui.glcom.control.CurrencyComboBox;
import nc.ui.glcom.tableassrefeditor.AssRefTableEditorRenderer;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.FocusUtils;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UITextField;
import nc.vo.bd.account.AccountVO;
import nc.vo.bd.currtype.CurrtypeVO;
import nc.vo.gateway60.itfs.Currency;
import nc.vo.gateway60.pub.IVoAccess;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.sysparam.AssRefParam;
import nc.vo.glcom.ass.AssVO;
import nc.vo.glcom.constant.VoucherInputMaxLenConst;
import nc.vo.pub.BusinessException;
import nc.vo.pub.ValueObject;
import nc.vo.pub.lang.ICalendar;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;

import org.apache.commons.lang.StringUtils;

public class VoucherTableCellEditor extends UIRefCellEditorNew
{
  private static final long serialVersionUID = 4843044670509305841L;
  int m_voucherkey;
  boolean isUIRefPane = false;

  boolean isTableIn = false;

  public VoucherTableCellEditor(JCheckBox x)
  {
    super(x);
    setClickCountToStart(1);
  }

  public VoucherTableCellEditor(JComboBox x)
  {
    super(new UITextField());
    setClickCountToStart(1);
    this.editorComponent = x;
    this.delegate = new UIRefCellEditorNew.EditorDelegate() {
      public void setValue(Object x) {
        super.setValue(x);
        ((JComboBox)VoucherTableCellEditor.this.getEditorComponent()).setSelectedItem(x);
      }

      public Object getCellEditorValue() {
        return ((JComboBox)VoucherTableCellEditor.this.getEditorComponent()).getSelectedItem();
      }

      public boolean startCellEditing(EventObject anEvent)
      {
        return (anEvent instanceof AWTEvent);
      }

      public boolean stopCellEditing()
      {
        return true;
      }
    };
    InputMap map = x.getInputMap(0);
    ActionMap am = x.getActionMap();
    String key = "transFocus";
    map.remove(KeyStroke.getKeyStroke(10, 0, true));
    map.put(KeyStroke.getKeyStroke(10, 0, false), key);
    am.put(key, new ShortCutKeyAction(10));
  }

  public VoucherTableCellEditor(JTextField x)
  {
    super(x);
    setClickCountToStart(1);
  }

  public VoucherTableCellEditor(DetailAssistantRefPane x)
  {
    super(new JTextField());
    this.editorComponent = x;
    this.clickCountToStart = 1;
    this.delegate = new UIRefCellEditorNew.EditorDelegate() {
      public void setValue(Object x) {
        ((DetailAssistantRefPane)VoucherTableCellEditor.this.getEditorComponent()).setDetail((DetailVO)x);
      }

      public Object getCellEditorValue() {
        return ((DetailAssistantRefPane)VoucherTableCellEditor.this.getEditorComponent()).getDetail();
      }

      public boolean startCellEditing(EventObject anEvent) {
        if (anEvent == null) {
          VoucherTableCellEditor.this.getEditorComponent().requestFocus();
        }

        return true;
      }

      public boolean stopCellEditing() {
        return true;
      }
    };
    ((DetailAssistantRefPane)this.editorComponent).getUITextField().addActionListener(this.delegate);
  }

  public VoucherTableCellEditor(AssRefTableEditorRenderer x)
  {
    super(new JTextField());
    this.editorComponent = x;
    this.clickCountToStart = 1;
    this.delegate = new UIRefCellEditorNew.EditorDelegate()
    {
      public void setValue(Object x)
      {
      }

      public Object getCellEditorValue()
      {
        return ((AssRefTableEditorRenderer)VoucherTableCellEditor.this.getEditorComponent()).getAssId();
      }

      public boolean startCellEditing(EventObject anEvent) {
        if (anEvent == null)
          VoucherTableCellEditor.this.getEditorComponent().requestFocus();
        return true;
      }

      public boolean stopCellEditing() {
        VoucherDataCenter.getInstance().tempSaveAssVOs(((AssRefTableEditorRenderer)VoucherTableCellEditor.this.getEditorComponent()).getAssId(), ((AssRefTableEditorRenderer)VoucherTableCellEditor.this.getEditorComponent()).getAssVOArray());
        return true;
      }
    };
    ((AssRefTableEditorRenderer)this.editorComponent).getTextField().addActionListener(this.delegate);
  }

  public VoucherTableCellEditor(UIRefPane x)
  {
    super(x);

    this.editorComponent = x;
    UITextField textField = x.getUITextField();

    this.clickCountToStart = 1;
    x.setSize(new Dimension(new Double(x.getSize().getWidth()).intValue(), 23));
    x.setPreferredSize(new Dimension(new Double(x.getPreferredSize().getWidth()).intValue(), 23));
    x.getUIButton().requestFocus();
    this.delegate = new UIRefCellEditorNew.EditorDelegate() {
      public void setValue(Object value) {
        if (((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).getRefNodeName().equals("常用摘要")) {
          String str = value == null ? "" : value.toString();
          ((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).setValue(str);
          ((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).getRefModel().setSelectedData(null);
          ((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).getUITextField().setText(str);
        } else {
          ((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).setValueObj(value);
        }
        if (!((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).getUITextField().hasFocus()) {
          ((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).getUITextField().setSelectallWhenFocus(true);
          ((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).getUITextField().selectAll();
        }
      }

      public Object getCellEditorValue()
      {
        if (((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).getRefNodeName().equals("常用摘要")) {
          if (((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).getRefName() != null)
            return ((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).getRefName();
          UIRefPane pan = (UIRefPane)VoucherTableCellEditor.this.getEditorComponent();
          if ((pan.getUITextField().getText() == null) || ("".equals(pan.getUITextField().getText())))
          {
            Object obj = pan.getValueObj();
            return obj == null ? obj : ((Object[])(Object[])obj)[0];
          }
          return pan.getUITextField().getText();
        }

        if (((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).isReturnCode())
        {
          return ((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).getText();
        }

        return ((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).getValueObj();
      }

      public void setPK(Object value) {
        ((UIRefPane)VoucherTableCellEditor.this.getEditorComponent()).setPK(value != null ? value.toString() : "");
      }

      public boolean startCellEditing(EventObject anEvent)
      {
        VoucherTableCellEditor.this.getEditorComponent().repaint();
        return true;
      }
    };
    textField.addActionListener(this.delegate);

    InputMap map = x.getUITextField().getInputMap(0);
    ActionMap am = x.getUITextField().getActionMap();
    String key = "transFocus";
    map.remove(KeyStroke.getKeyStroke(10, 0, true));
    map.put(KeyStroke.getKeyStroke(10, 0, false), key);
    am.put(key, new ShortCutKeyAction(10));
    this.isUIRefPane = true;
  }

  public Object getCellEditorValue()
  {
    switch (getVoucherKey()) {
    case 103:
    case 301:
    case 302:
//	20161223-tsy-修改自定义项目1为科目参照
    case 151:
//  end
      if (!(this.editorComponent instanceof AccsubjRefPane)) break;
      ValueObject[] vos = ((AccsubjRefPane)this.editorComponent).getVOs();
      if ((vos != null) && (vos.length > 1)) {
        return vos;
      }
      return ((AccsubjRefPane)this.editorComponent).getVO();
    case 108:
      if ((this.editorComponent instanceof AssRefTableEditorRenderer)) {
        String[] ids = ((AssRefTableEditorRenderer)this.editorComponent).getAssIDs();
        if ((ids != null) && (ids.length > 1)) {
          return ids;
        }
        return super.getCellEditorValue();
      }

    case 104:
    case 304:
    case 305:
    case 320:
      if ((this.editorComponent instanceof CurrencyComboBox))
        return ((CurrencyComboBox)this.editorComponent).getSelectedVO();
      if ((this.editorComponent instanceof UIRefPane)) {
        return VoucherDataCenter.getCurrtypeByPk_corp(((UIRefPane)this.editorComponent).getPk_corp(), ((UIRefPane)this.editorComponent).getRefPK());
      }
      return this.delegate.getCellEditorValue();
    case 322:
      return ((DetailAssistantRefPane)getEditorComponent()).getDetail();
    case 126:
      return ((UIRefPane)getEditorComponent()).getRefPK();
    case 606:
    case 608:
      HashMap orgMap = new HashMap();

      if ((this.editorComponent instanceof UIRefPane)) {
        UIRefPane refPane = (UIRefPane)getEditorComponent();
        Object pk_oid = refPane.getRefModel().getValue("pk_org");
        String pk_vid = refPane.getRefPK();

        if (pk_oid != null) {
          orgMap.put("pk_org", pk_oid == null ? null : pk_oid.toString());
        }
        if (pk_vid != null) {
          orgMap.put("pk_vid", pk_vid);
        }
      }
      return orgMap;
    case 133:
      if ((this.editorComponent instanceof UIRefPane)) {
        UIRefPane refPane = (UIRefPane)getEditorComponent();
        return refPane.getRefPK();
      }

    case 136:
      if ((this.editorComponent instanceof UIRefPane)) {
        UIRefPane refPane = (UIRefPane)getEditorComponent();
        return refPane.getRefPK();
      }

    }

    return super.getCellEditorValue();
  }

  public Component getEditorComponent()
  {
    return this.editorComponent;
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
  {
	  VoucherTableModel tm = null;
	  IVoucherModel model = null;
	  String pk_orgbook = null;
	  Object pk_unit = null;
	  UFDate date = null;
	  String pk_currtype = null;
	  Integer precision = null;
	  String pk_acc = null;
    switch (getVoucherKey())
    {
    case 103:
    case 301:
    case 302:
//	20161223-tsy-修改自定义项目1
    case 151:
//  end
      pk_acc = value == null ? null : ((AccountVO)value).getPk_accasoa();

      if (!(this.editorComponent instanceof UIRefPane)) break;
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      pk_orgbook = (String)model.getValue(row, Integer.valueOf(55));
      date = (UFDate)model.getValue(row, Integer.valueOf(17));
      if ((this.editorComponent instanceof AccsubjRefPane)) {
        UFDate preparedDate = (UFDate)model.getValue(row, Integer.valueOf(17));

        ((AccsubjRefPane)this.editorComponent).setPk_GlOrgBook("2", pk_orgbook, preparedDate.toStdString());
        ((AccsubjRefPane)this.editorComponent).setDate(date);
        UFBoolean iseditable = (UFBoolean)model.getValue(row, Integer.valueOf(26));
        if ((iseditable == null) || (iseditable.booleanValue())) {
          ((AccsubjRefPane)this.editorComponent).setMultilSelectable(true);
        }
        else {
          ((AccsubjRefPane)this.editorComponent).setMultilSelectable(false);
        }
        ((UIRefPane)this.editorComponent).setPK(pk_acc);
      }
      else {
        ((UIRefPane)this.editorComponent).getRefModel().setPk_org(pk_orgbook);
      }((UIRefPane)this.editorComponent).setPK(pk_acc);
      break;
    case 113:
    case 117:
    case 311:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      if ((this.editorComponent instanceof QuantityAmountRefPane)) {
        pk_orgbook = (String)model.getValue(-1, Integer.valueOf(55));
        if ((((QuantityAmountRefPane)this.editorComponent).getVoucherModel() == null) || (((QuantityAmountRefPane)this.editorComponent).getVoucherModel() != model))
          ((QuantityAmountRefPane)this.editorComponent).setVoucherModel(model);
        ((QuantityAmountRefPane)this.editorComponent).setIndex(row);
        ((QuantityAmountRefPane)this.editorComponent).repaint();
        precision = null;
        Object detailObj = model.getValue(row, Integer.valueOf(1));
        if ((detailObj != null) && ((detailObj instanceof DetailVO))) {
          DetailVO detailVo = (DetailVO)detailObj;
          pk_unit = VoucherDataCenter.getMaterialUnit(detailVo.getAss());
          if (StringUtils.isNotEmpty((String) pk_unit)) {
            precision = VoucherDataCenter.getQuantityDigitByUnit((String) pk_unit);
          } else {
            String pk_accasoa = (String)model.getValue(row, Integer.valueOf(103));
            date = (UFDate)model.getValue(row, Integer.valueOf(17));
            precision = VoucherDataCenter.getQuantityPrecision(pk_orgbook, pk_accasoa, date.toStdString());
          }
        }

        if (precision != null)
          ((QuantityAmountRefPane)this.editorComponent).getUITextField().setNumPoint(precision.intValue());
      }
      sleepThread();
      this.delegate.setValue(value);
      break;
    case 110:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      if ((this.editorComponent instanceof QuantityAmountRefPane)) {
        pk_orgbook = (String)model.getValue(-1, Integer.valueOf(55));
        if ((((QuantityAmountRefPane)this.editorComponent).getVoucherModel() == null) || (((QuantityAmountRefPane)this.editorComponent).getVoucherModel() != model))
          ((QuantityAmountRefPane)this.editorComponent).setVoucherModel(model);
        ((QuantityAmountRefPane)this.editorComponent).setIndex(row);
        ((QuantityAmountRefPane)this.editorComponent).getUITextField().setNumPoint(VoucherDataCenter.getPricePrecision().intValue());
      }
      this.delegate.setValue(value);
      break;
    case 114:
    case 118:
    case 310:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      if ((this.editorComponent instanceof QuantityAmountRefPane)) {
        pk_orgbook = (String)model.getValue(-1, Integer.valueOf(55));
        if ((((QuantityAmountRefPane)this.editorComponent).getVoucherModel() == null) || (((QuantityAmountRefPane)this.editorComponent).getVoucherModel() != model))
          ((QuantityAmountRefPane)this.editorComponent).setVoucherModel(model);
        ((QuantityAmountRefPane)this.editorComponent).setIndex(row);
        ((QuantityAmountRefPane)this.editorComponent).repaint();

        pk_currtype = (String)model.getValue(row, Integer.valueOf(104));

        int digit = Currency.getCurrDigit(pk_currtype).intValue();
        ((QuantityAmountRefPane)this.editorComponent).getUITextField().setNumPoint(digit);
        ((QuantityAmountRefPane)this.editorComponent).getUITextField().setMaxLength(VoucherInputMaxLenConst.AMOUNT.intValue() + digit);
      }

      sleepThread();
      if (((value instanceof UFDouble)) && (((UFDouble)value).abs().doubleValue() < 1.E-009D))
        this.delegate.setValue(null);
      else {
        this.delegate.setValue(value);
      }
      break;
    case 112:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      if ((this.editorComponent instanceof QuantityAmountRefPane)) {
        pk_orgbook = (String)model.getValue(-1, Integer.valueOf(55));
        if ((((QuantityAmountRefPane)this.editorComponent).getVoucherModel() == null) || (((QuantityAmountRefPane)this.editorComponent).getVoucherModel() != model))
          ((QuantityAmountRefPane)this.editorComponent).setVoucherModel(model);
        ((QuantityAmountRefPane)this.editorComponent).setIndex(row);
        if (VoucherDataCenter.isLocalFrac(pk_orgbook))
          ((QuantityAmountRefPane)this.editorComponent).getUITextField().setNumPoint(VoucherDataCenter.getCurrrateDigitByPk_orgbook(pk_orgbook, VoucherDataCenter.getFracCurrencyPK(pk_orgbook), VoucherDataCenter.getMainCurrencyPK(pk_orgbook)));
        else {
          ((QuantityAmountRefPane)this.editorComponent).getUITextField().setNumPoint(VoucherDataCenter.getCurrrateDigitByPk_orgbook(pk_orgbook, (String)model.getValue(row, Integer.valueOf(104)), VoucherDataCenter.getMainCurrencyPK(pk_orgbook)));
        }
      }
      sleepThread();
      this.delegate.setValue(value);
      break;
    case 116:
    case 120:
    case 313:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      if ((this.editorComponent instanceof QuantityAmountRefPane)) {
        pk_orgbook = (String)model.getValue(-1, Integer.valueOf(55));
        if ((((QuantityAmountRefPane)this.editorComponent).getVoucherModel() == null) || (((QuantityAmountRefPane)this.editorComponent).getVoucherModel() != model))
          ((QuantityAmountRefPane)this.editorComponent).setVoucherModel(model);
        ((QuantityAmountRefPane)this.editorComponent).setIndex(row);
        ((QuantityAmountRefPane)this.editorComponent).repaint();

        pk_currtype = VoucherDataCenter.getMainCurrencyPK(pk_orgbook);
        int digit = VoucherDataCenter.getCurrtypeByPk_orgbook(pk_orgbook, pk_currtype).getCurrdigit().intValue();
        ((QuantityAmountRefPane)this.editorComponent).getUITextField().setNumPoint(digit);
        ((QuantityAmountRefPane)this.editorComponent).getUITextField().setMaxLength(VoucherInputMaxLenConst.AMOUNT.intValue() + digit);
      }

      sleepThread();
      if (((value instanceof UFDouble)) && (((UFDouble)value).abs().doubleValue() < 1.E-009D))
        this.delegate.setValue(null);
      else {
        this.delegate.setValue(value);
      }

      break;
    case 328:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      pk_currtype = (String)model.getValue(row, Integer.valueOf(104));
      pk_orgbook = (String)model.getValue(-1, Integer.valueOf(55));
      precision = Currency.getCurrratePrecisionByOrg(GlWorkBench.getLoginGroup(), pk_orgbook, pk_currtype);
      if ((this.editorComponent instanceof QuantityAmountRefPane)) {
        if ((((QuantityAmountRefPane)this.editorComponent).getVoucherModel() == null) || (((QuantityAmountRefPane)this.editorComponent).getVoucherModel() != model))
          ((QuantityAmountRefPane)this.editorComponent).setVoucherModel(model);
        ((QuantityAmountRefPane)this.editorComponent).setIndex(row);
        if (precision != null)
          ((QuantityAmountRefPane)this.editorComponent).getUITextField().setNumPoint(precision.intValue());
      }
      this.delegate.setValue(value);
      break;
    case 324:
    case 325:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      if ((this.editorComponent instanceof QuantityAmountRefPane)) {
        pk_orgbook = (String)model.getValue(-1, Integer.valueOf(55));
        if ((((QuantityAmountRefPane)this.editorComponent).getVoucherModel() == null) || (((QuantityAmountRefPane)this.editorComponent).getVoucherModel() != model))
          ((QuantityAmountRefPane)this.editorComponent).setVoucherModel(model);
        ((QuantityAmountRefPane)this.editorComponent).setIndex(row);
        ((QuantityAmountRefPane)this.editorComponent).repaint();

        pk_currtype = null;
        try {
          pk_currtype = Currency.getGroupCurrpk(GlWorkBench.getLoginGroup());
        } catch (BusinessException e) {
          Logger.error(e.getMessage(), e);
          break;
        }

        precision = VoucherDataCenter.getCurrtypeByPk_orgbook(null, pk_currtype).getCurrdigit().intValue();
        ((QuantityAmountRefPane)this.editorComponent).getUITextField().setNumPoint(precision);
        ((QuantityAmountRefPane)this.editorComponent).getUITextField().setMaxLength(VoucherInputMaxLenConst.AMOUNT.intValue() + precision);
      }

      if (((value instanceof UFDouble)) && (((UFDouble)value).abs().doubleValue() < 1.E-009D))
        this.delegate.setValue(null);
      else {
        this.delegate.setValue(value);
      }

      break;
    case 329:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      pk_currtype = (String)model.getValue(row, Integer.valueOf(104));
      pk_orgbook = (String)model.getValue(-1, Integer.valueOf(55));
      precision = Currency.getCurrratePrecisionByOrg("GLOBLE00000000000000", pk_orgbook, pk_currtype);
      if ((this.editorComponent instanceof QuantityAmountRefPane)) {
        if ((((QuantityAmountRefPane)this.editorComponent).getVoucherModel() == null) || (((QuantityAmountRefPane)this.editorComponent).getVoucherModel() != model))
          ((QuantityAmountRefPane)this.editorComponent).setVoucherModel(model);
        ((QuantityAmountRefPane)this.editorComponent).setIndex(row);
        if (precision != null)
          ((QuantityAmountRefPane)this.editorComponent).getUITextField().setNumPoint(precision.intValue());
      }
      this.delegate.setValue(value);
      break;
    case 326:
    case 327:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      if ((this.editorComponent instanceof QuantityAmountRefPane)) {
        pk_orgbook = (String)model.getValue(-1, Integer.valueOf(55));
        if ((((QuantityAmountRefPane)this.editorComponent).getVoucherModel() == null) || (((QuantityAmountRefPane)this.editorComponent).getVoucherModel() != model))
          ((QuantityAmountRefPane)this.editorComponent).setVoucherModel(model); ((QuantityAmountRefPane)this.editorComponent).setIndex(row);
        ((QuantityAmountRefPane)this.editorComponent).repaint();

        pk_currtype = (String)model.getValue(row, Integer.valueOf(104));
        String globalCurrPk;
        try { globalCurrPk = Currency.getGlobalCurrPk(null);
        } catch (BusinessException e) {
          Logger.error(e.getMessage(), e);
          break;
        }
        precision = VoucherDataCenter.getCurrtypeByPk_orgbook(null, globalCurrPk).getCurrdigit().intValue();
        ((QuantityAmountRefPane)this.editorComponent).getUITextField().setNumPoint(precision);
        ((QuantityAmountRefPane)this.editorComponent).getUITextField().setMaxLength(VoucherInputMaxLenConst.AMOUNT.intValue() + precision);
      }

      if (((value instanceof UFDouble)) && (((UFDouble)value).abs().doubleValue() < 1.E-009D))
        this.delegate.setValue(null);
      else {
        this.delegate.setValue(value);
      }

      break;
    case 108:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      pk_acc = (String)model.getValue(row, Integer.valueOf(103));
      pk_orgbook = (String)model.getValue(row, Integer.valueOf(55));
      AssVO[] ass = (AssVO[])(AssVO[])model.getValue(row, Integer.valueOf(303));
      date = (UFDate)model.getValue(row, Integer.valueOf(17));
      pk_unit = (String)model.getValue(row, Integer.valueOf(607));

      ((AssRefTableEditorRenderer)getEditorComponent()).setRefParameter(new AssRefParam(pk_acc, (String)value, ass, pk_orgbook, date.toStdString(), (String) pk_unit));
      UFBoolean iseditable = (UFBoolean)model.getValue(row, Integer.valueOf(26));
      if ((iseditable == null) || (iseditable.booleanValue()))
        ((AssRefTableEditorRenderer)getEditorComponent()).setFreevalueMultiSelected(true);
      else {
        ((AssRefTableEditorRenderer)getEditorComponent()).setFreevalueMultiSelected(false);
      }
      if (VoucherDataCenter.isFreevalueDefaultDown(pk_orgbook)) {
        if (row > 0) {
          AssVO[] ass1 = (AssVO[])(AssVO[])model.getValue(row - 1, Integer.valueOf(303));
          if (ass1 != null)
            ((AssRefTableEditorRenderer)getEditorComponent()).setStandardVO(ass1);
        } else {
          ((AssRefTableEditorRenderer)getEditorComponent()).setStandardVO(null);
        }
      }
      else ((AssRefTableEditorRenderer)getEditorComponent()).setStandardVO(null);

      ((AssRefTableEditorRenderer)getEditorComponent()).setRowColumn(row, column);
      break;
    case 322:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      ((DetailAssistantRefPane)getEditorComponent()).setVoucherModel(model);
      ((DetailAssistantRefPane)getEditorComponent()).setShowflag(false);
      this.delegate.setValue(value);
      break;
    case 104:
    case 304:
    case 305:
    case 320:
      if (value == null) break;
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      pk_orgbook = (String)model.getValue(row, Integer.valueOf(55));
      ((CurrencyComboBox)this.editorComponent).setPk_corp(VoucherDataCenter.getPk_corpByPk_glorgbook(pk_orgbook));
      ((CurrencyComboBox)this.editorComponent).setSelectedPk(((CurrtypeVO)value).getPk_currtype());
      break;
    case 126:
      if (value != null) {
        tm = (VoucherTableModel)table.getModel();
        model = tm.getVoucherModel();
        pk_orgbook = (String)model.getValue(row, Integer.valueOf(55));
        ((CheckStlyeRef)this.editorComponent).setPK(value.toString());
      } else {
        ((CheckStlyeRef)this.editorComponent).setPK(null);
      }
      break;
    case 606:
    case 607:
    case 608:
      tm = (VoucherTableModel)table.getModel();
      model = tm.getVoucherModel();
      pk_orgbook = (String)model.getValue(row, Integer.valueOf(55));
      pk_unit = model.getValue(row, Integer.valueOf(608));

      GLBUVersionWithBookRefModel buModel = (GLBUVersionWithBookRefModel)((UIRefPane)this.editorComponent).getRefModel();
      buModel.setPk_accountingbook(pk_orgbook);
      UFDate preparedate = (UFDate)model.getValue(row, Integer.valueOf(17));
      buModel.setVstartdate(preparedate.asEnd());
      ((UIRefPane)this.editorComponent).setPK(pk_unit);
      break;
    case 109:
      sleepThread();
      this.delegate.setValue(value);
      break;
    case 132:
      if (value == null) {
        this.delegate.setValue(value);
      }
 else if ((table.getModel() instanceof VoucherTableModel)) {
					tm = (VoucherTableModel) table.getModel();
					model = tm.getVoucherModel();
					Object value2 = model.getValue(row, Integer.valueOf(132));
					String verifyDate = value2 == null ? null : (String) value2;
					this.delegate.setValue(verifyDate == null ? null : new UFDate(new UFDateTime(verifyDate, ICalendar.BASE_TIMEZONE).getMillis()));
				} else {
					if (!(table.getModel() instanceof VerifyTableModel))
						break;
					VerifyTableModel tm1 = (VerifyTableModel) table.getModel();
					Object[] vOs = tm1.getVOs();
					Object obj = null;
        try {
          obj = ((IVoAccess)vOs[row]).getValue(132);
        } catch (Exception e) {
          Logger.error(e.getMessage(), e);
          obj = NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000165");
        }
        this.delegate.setValue(obj == null ? null : new UFDate(new UFDateTime(obj.toString(), ICalendar.BASE_TIMEZONE).getMillis()));
      }break;
    default:
      if (((this.editorComponent instanceof UIRefPane)) && ((table.getModel() instanceof VoucherTableModel))) {
        tm = (VoucherTableModel)table.getModel();
        model = tm.getVoucherModel();

        pk_unit = model.getValue(row, Integer.valueOf(607));
        if (((UIRefPane)this.editorComponent).getRefModel() != null) {
          ((UIRefPane)this.editorComponent).getRefModel().setPk_org(pk_unit.toString());
        }

        ((UIRefPane)this.editorComponent).setPK(value);
      } else {
        this.delegate.setValue(value);
      }
    }

    return this.editorComponent;
  }

  private void sleepThread() {
    Thread th = new Thread();
    try {
      Thread.sleep(50L);
    }
    catch (InterruptedException e) {
      Logger.error(e.getMessage(), e);
    }
  }

  public int getVoucherKey()
  {
    return this.m_voucherkey;
  }

  public void setVoucherKey(int newM_voucherkey)
  {
    this.m_voucherkey = newM_voucherkey;
  }

  public boolean stopCellEditing() {
    switch (getVoucherKey()) {
    case 103:
    case 301:
    case 302:
//	20161223-tsy-修改自定义项目1：改为科目参照[停止修改]方法
    case 151:
//  end
      ((AccsubjRefPane)getEditorComponent()).onEnterKeyPress();
      break;
    case 126:
      ((CheckStlyeRef)getEditorComponent()).onEnterKeyPress();
    }

    boolean stoped = super.stopCellEditing();
    if (stoped) {
      fireEditingStopped();
    }

    return stoped;
  }

  class ShortCutKeyAction extends AbstractAction
  {
    int keycode = -1;
    static final int VK_ENTER = 10;
    static final String KEY_FOCUS_TRANSFER = "transFocus";

    public ShortCutKeyAction(int keycode)
    {
      this.keycode = keycode;
    }

    public void actionPerformed(ActionEvent e) {
      if ((e.getSource() instanceof UITextField)) {
        UITextField c = (UITextField)e.getSource();
        if ((c.isEnabled()) && (c.isEditable()))
          switch (this.keycode) {
          case 10:
            focusNextComponent(c);
          }
      }
    }

    private void focusNextComponent(Component c)
    {
      Component parent = c.getParent();
      if (((parent instanceof JTable)) || (((parent instanceof UIRefPane)) && ((parent.getParent() instanceof JTable))))
        return;
      FocusUtils.focusNextComponent(c);
    }
  }
}