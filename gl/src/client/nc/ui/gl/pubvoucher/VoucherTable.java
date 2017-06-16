package nc.ui.gl.pubvoucher;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import nc.bs.logging.Logger;
import nc.ui.gl.uicfg.voucher.VoucherTableModel;
import nc.ui.gl.vouchercard.IVoucherModel;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.UIMenuItem;
import nc.ui.pub.beans.UIPopupMenu;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UITable;
import nc.ui.pub.beans.UITextField;
import nc.ui.pub.beans.util.NCOptionPane;
import nc.vo.bd.account.AccountVO;
import nc.vo.gl.sysparam.SystemParamConfig;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;




public class VoucherTable
  extends UITable
  implements ActionListener
{
  public static final String Action_SelectAndEditAtNextColumnCell = "nc.ui.gl.uicfg.voucher.VoucherTable_SelectAndEditAtNextColumnCell";
  public static final String Action_SelectAndEditAtPreviousColumnCell = "nc.ui.gl.uicfg.voucher.VoucherTable_SelectAndEditAtPreviousColumnCell";
  public static final String Action_CancelCellEditing = "nc.ui.gl.uicfg.voucher.VoucherTable_CancelCellEditing";
  public VoucherPopupMenu menu = new VoucherPopupMenu();
  


  public VoucherTable()
  {
    setAutoscrolls(true);
    InputMap map = getInputMap(1);
    InputMap parentmap = map.getParent();
    ActionMap am = getActionMap();
    KeyStroke ks = KeyStroke.getKeyStroke(10, 0, true);
    if (parentmap != null)
      parentmap.remove(ks);
    map.remove(ks);
    map.put(ks, "nc.ui.gl.uicfg.voucher.VoucherTable_SelectAndEditAtNextColumnCell");
    am.put("nc.ui.gl.uicfg.voucher.VoucherTable_SelectAndEditAtNextColumnCell", new TableSelectAndEditAtNextColumnCellAction(this));
    ks = KeyStroke.getKeyStroke(10, 65, false);
    if (parentmap != null)
      parentmap.remove(ks);
    map.remove(ks);
    ks = KeyStroke.getKeyStroke(10, 1, true);
    if (parentmap != null)
      parentmap.remove(ks);
    map.remove(ks);
    map.put(ks, "nc.ui.gl.uicfg.voucher.VoucherTable_SelectAndEditAtPreviousColumnCell");
    am.put("nc.ui.gl.uicfg.voucher.VoucherTable_SelectAndEditAtPreviousColumnCell", new TableSelectAndEditAtPreviousColumnCellAction(this));
    ks = KeyStroke.getKeyStroke(27, 0, true);
    if (parentmap != null)
      parentmap.remove(ks);
    map.remove(ks);
    map.put(ks, "nc.ui.gl.uicfg.voucher.VoucherTable_CancelCellEditing");
    am.put("nc.ui.gl.uicfg.voucher.VoucherTable_CancelCellEditing", new TableCancelCellEditingAction(this));
    
    addListerners();
  }
  
  private void addListerners() {
    addMouseListener(new MouseAdapter()
    {
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          VoucherTable.this.onPopupMenuShow(e);
        }
      }
    });
    List<UIMenuItem> allmenu = this.menu.getMenus();
    if (null != allmenu) {
      for (UIMenuItem menu : allmenu)
        menu.addActionListener(this);
    }
  }
  
  private void onPopupMenuShow(MouseEvent e) {
    int y = e.getY() - getPopMenuHeight(this.menu);
    this.menu.show((Component)e.getSource(), e.getX(), y);
  }
  
  private int getPopMenuHeight(UIPopupMenu popmenu) {
    if (popmenu == null)
      return 0;
    Component[] coms = popmenu.getComponents();
    int h = 0;
    if ((coms != null) && (coms.length > 0)) {
      for (int i = 0; i < coms.length; i++) {
        h += new Double(coms[i].getHeight()).intValue();
        if ((coms[i] instanceof JPopupMenu.Separator))
          h += 4;
      }
    }
    return h + 4;
  }
  
  public void grabFocus() { super.grabFocus();
    if (getSelectedRow() < 0)
    {
      getSelectionModel().setSelectionInterval(0, 0);
      getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
      if (editCellAt(0, 0))
      {
        TableCellEditor ce = getCellEditor();
        if (ce != null)
        {
          Component ccmp = ce.getTableCellEditorComponent(this, ce.getCellEditorValue(), true, 0, 0);
          if (ccmp != null)
          {
            if ((ccmp instanceof UIRefPane))
            {
              ((UIRefPane)ccmp).getUITextField().requestFocus();
            }
            else
            {
              ccmp.requestFocus();
            }
          }
        }
      }
    }
  }
  





















  public void repaintCell(int row, int column)
  {
    paintImmediately(getCellRect(row, column, true));
  }
  



  protected static class TableSelectAndEditAtNextColumnCellAction
    extends AbstractAction
  {
    JTable table = null;
    
    protected TableSelectAndEditAtNextColumnCellAction(JTable jtable) {
      this.table = jtable;
    }
    
    public void actionPerformed(ActionEvent e) {
      ListSelectionModel rsm = this.table.getSelectionModel();
      int anchorRow = rsm.getAnchorSelectionIndex();
      ListSelectionModel csm = this.table.getColumnModel().getSelectionModel();
      int anchorColumn = csm.getAnchorSelectionIndex();
      int oldRow = anchorRow;
      int oldcolumn = anchorColumn;
      if (anchorRow >= this.table.getRowCount())
      {
        anchorRow = 0;
        anchorColumn = 0;
      }
      
      if ((this.table.isEditing()) || (this.table.getCellEditor() != null))
      {
        this.table.getCellEditor().stopCellEditing();
      }
      

      if ((anchorColumn >= 0) && (anchorColumn == this.table.getColumnCount() - 1) && (anchorRow >= 0) && (anchorRow == this.table.getRowCount() - 1))
      {
        this.table.transferFocus();
        return;
      }
      
      anchorColumn++;
      if (anchorColumn >= this.table.getColumnCount())
      {
        anchorColumn = 0;
        anchorRow++;
        if (anchorRow >= this.table.getRowCount())
        {
          anchorRow = 0;
        }
      }
      
      while ((anchorRow < this.table.getRowCount()) && (!this.table.isCellEditable(anchorRow, anchorColumn)))
      {
        anchorColumn++;
        if (anchorColumn >= this.table.getColumnCount())
        {
          anchorColumn = 0;
          anchorRow++;
        }
      }
      if (anchorRow >= this.table.getRowCount())
      {
        anchorColumn = oldcolumn;
        anchorRow = oldRow;
      }
      
      rsm.setSelectionInterval(anchorRow, anchorRow);
      csm.setSelectionInterval(anchorColumn, anchorColumn);
      this.table.editCellAt(anchorRow, anchorColumn);
      this.table.scrollRectToVisible(this.table.getCellRect(anchorRow, anchorColumn, false));
      
      Component editorComp = this.table.getEditorComponent();
      if (editorComp != null)
      {
        if ((editorComp instanceof JTextField))
        {
          JTextField textField = (JTextField)editorComp;
          textField.requestFocus();
          textField.selectAll();

        }
        else if ((editorComp instanceof UIRefPane))
        {
          JTextField textField = ((UIRefPane)editorComp).getUITextField();
          textField.requestFocus();
          textField.selectAll();


        }
        else if ((editorComp instanceof JTable))
        {
          this.table.requestFocus();
          this.table.setColumnSelectionInterval(anchorColumn, anchorColumn);
          this.table.setRowSelectionInterval(anchorRow, anchorRow);


        }
        else if (!editorComp.hasFocus()) {
          editorComp.requestFocus();
        }
        try
        {
          Method method = editorComp.getClass().getMethod("showDialogIfNeed", new Class[0]);
          if (method != null) {
            method.invoke(editorComp, new Object[0]);
          }
        }
        catch (Exception ex) {}
      }
    }
  }
  
  protected static class TableSelectAndEditAtPreviousColumnCellAction
    extends AbstractAction
  {
    JTable table = null;
    
    protected TableSelectAndEditAtPreviousColumnCellAction(JTable jtable) {
      this.table = jtable;
    }
    
    public void actionPerformed(ActionEvent e)
    {
      ListSelectionModel rsm = this.table.getSelectionModel();
      int anchorRow = rsm.getAnchorSelectionIndex();
      int oldRow = anchorRow;
      ListSelectionModel csm = this.table.getColumnModel().getSelectionModel();
      int anchorColumn = csm.getAnchorSelectionIndex();
      int oldcolumn = anchorColumn;
      
      if ((this.table.isEditing()) && (this.table.getCellEditor() != null))
      {
        this.table.getCellEditor().stopCellEditing();
      }
      
      anchorColumn--;
      if (anchorColumn < 0)
      {
        if (anchorRow > 0)
        {
          anchorColumn = this.table.getColumnCount();
          anchorRow -= 1;
        }
        else
        {
          anchorColumn = 0;
          anchorRow = 0;
        }
      }
      
      while ((anchorRow >= 0) && (!this.table.isCellEditable(anchorRow, anchorColumn)))
      {
        anchorColumn--;
        if (anchorColumn < 0)
        {
          anchorColumn = this.table.getColumnCount() - 1;
          anchorRow--;
        }
      }
      if (anchorRow < 0)
      {
        anchorColumn = oldcolumn;
        anchorRow = oldRow;
      }
      
      rsm.setSelectionInterval(anchorRow, anchorRow);
      csm.setSelectionInterval(anchorColumn, anchorColumn);
      this.table.editCellAt(anchorRow, anchorColumn);
      this.table.scrollRectToVisible(this.table.getCellRect(anchorRow, anchorColumn, false));
      
      Component editorComp = this.table.getEditorComponent();
      if (editorComp != null)
      {
        if ((editorComp instanceof JTextField))
        {
          JTextField textField = (JTextField)editorComp;
          textField.requestFocus();
          textField.selectAll();

        }
        else if ((editorComp instanceof UIRefPane))
        {
          JTextField textField = ((UIRefPane)editorComp).getUITextField();
          textField.requestFocus();
          textField.selectAll();
        }
        else if ((editorComp instanceof JTable))
        {
          this.table.requestFocus();
          this.table.setColumnSelectionInterval(anchorColumn, anchorColumn);
          this.table.setRowSelectionInterval(anchorRow, anchorRow);


        }
        else if (!editorComp.hasFocus()) {
          editorComp.requestFocus();
        }
      }
    }
  }
  
  protected static class TableCancelCellEditingAction extends AbstractAction
  {
    JTable table = null;
    
    protected TableCancelCellEditingAction(JTable jtable) {
      this.table = jtable;
    }
    
    public void actionPerformed(ActionEvent e) {
      if (this.table.isEditing())
      {
        TableCellEditor ce = this.table.getCellEditor();
        if (ce != null)
          ce.cancelCellEditing();
      }
    }
  }
  
  public void requestFocus() {
    super.requestFocus();
    if (getSelectedRow() < 0)
    {
      int row = 0;
      int column = 0;
      while (!editCellAt(row, column))
      {
        column++;
        if (column >= getColumnCount())
        {
          row++;
          if (row >= getRowCount())
          {
            row = 0;
            column = 0;
            break;
          }
          column = 0;
        }
      }
      getSelectionModel().setSelectionInterval(row, row);
      getColumnModel().getSelectionModel().setSelectionInterval(column, column);
      editCellAt(row, column);
      TableCellEditor ce = getCellEditor();
      if (ce != null)
      {
        Component ccmp = ce.getTableCellEditorComponent(this, ce.getCellEditorValue(), true, row, column);
        if (ccmp != null)
        {
          if ((ccmp instanceof UIRefPane))
          {
            ((UIRefPane)ccmp).getUITextField().requestFocus();
          }
          else
          {
            ccmp.requestFocus();
          }
          
        }
        
      }
    }
    else if (editCellAt(getSelectedRow(), getSelectedColumn()))
    {
      TableCellEditor ce = getCellEditor();
      if (ce != null)
      {
        Component ccmp = ce.getTableCellEditorComponent(this, ce.getCellEditorValue(), true, getSelectedRow(), getSelectedColumn());
        if (ccmp != null)
        {
          if ((ccmp instanceof UIRefPane))
          {
            ((UIRefPane)ccmp).getUITextField().requestFocus();
          }
          else
          {
            ccmp.requestFocus();
          }
        }
      }
    }
  }
  

  public void actionPerformed(ActionEvent e)
  {
    Object o = e.getSource();
    String actionCommand;
    if ((o instanceof JMenuItem))
      actionCommand = ((JMenuItem)o).getActionCommand(); else
      return;
    if (actionCommand.equals("COPYDETAIL"))
    {
      ((VoucherTableModel)getModel()).getVoucherModel().doOperation(actionCommand);
    } else if (actionCommand.equals("PASTEDETAIL"))
    {
      ((VoucherTableModel)getModel()).getVoucherModel().doOperation(actionCommand);
    }
  }
  


  private UITableSearchDialog searchDialog = null;
  
  private void showSearchDialog() {
    this.searchDialog = new UITableSearchDialog(this, this);
    this.searchDialog.showModal();
  }
  



  public void locateCell()
  {
    if (getSelectedColumn() == -1)
      return;
    if ((getModel() == null) || (getModel().getRowCount() == 0)) {
      NCOptionPane.showMessageDialog(this, NCLangRes.getInstance().getStrByID("_Beans", "UPP_Beans-000082") + ".", NCLangRes.getInstance().getStrByID("_Beans", "UPP_Beans-000053") + ":", 2);
      





      return;
    }
    showSearchDialog();
  }
  






















  public String getToolTipText(MouseEvent event)
  {
    UFBoolean showTip = UFBoolean.FALSE;
    try {
      showTip = SystemParamConfig.getInstance().getSubShowTipFlag();
    } catch (BusinessException e) {
      Logger.error(e);
    }
    
    if (showTip.booleanValue()) {
      Point p = event.getPoint();
      int hitColumnIndex = columnAtPoint(p);
      int hitRowIndex = rowAtPoint(p);
      
      Object valueAt = getValueAt(hitRowIndex, hitColumnIndex);
      
      if ((valueAt != null) && ((valueAt instanceof AccountVO))) {
        AccountVO accountVo = (AccountVO)valueAt;
        
        String tip = accountVo.getDispname();
        if (accountVo.getDef5() != null) {
          tip = tip + ":" + accountVo.getDef5();
        }
        return tip;
      }
    }
    return super.getToolTipText(event);
  }
  








  protected void processMouseEvent(MouseEvent e)
  {
    try
    {
      super.processMouseEvent(e);
    } catch (RuntimeException e1) {
      Logger.error(e1.getMessage(), e1);
    }
  }
}
