package nc.ui.gl.uicfg.voucher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import nc.bs.logging.Logger;
import nc.bs.ml.NCLangResOnserver;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.ui.gl.datacache.AccountCache;
import nc.ui.gl.pubvoucher.VoucherTable;
import nc.ui.gl.vouchercard.IVoucherModel;
import nc.ui.glcom.tableassrefeditor.AssRefTableEditorRenderer;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UISlider;
import nc.ui.pub.beans.UITablePane;
import nc.ui.sm.clientsetup.ClientSetup;
import nc.vo.bd.account.AccAssVO;
import nc.vo.fipub.utils.uif2.FiUif2MsgUtil;
import nc.vo.gateway60.pub.GlBusinessException;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.voucher.ColumnmodeVO;
import nc.vo.glcom.ass.AssVO;
import nc.vo.glcom.constant.GLStringConst;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.lang.UFBoolean;

import org.apache.commons.lang.StringUtils;

public class VoucherTablePane extends UIPanel implements javax.swing.event.ListSelectionListener, ISelectionIndexListener {
	private UISlider ivjUISlider = null;

	private UITablePane ivjUITablePane = null;

	private VoucherTableModel m_tablemodel = null;

	private ColumnmodeVO[] m_columnmode = null;

	private int tableheight = 23;

	private HashMap<String, AccAssVO> assControlRuels = new HashMap();

	private boolean lastIsAdjust = true;

	IvjEventHandler ivjEventHandler = new IvjEventHandler();

	class IvjEventHandler implements java.awt.event.ComponentListener, javax.swing.event.ChangeListener {
		IvjEventHandler() {
		}

		public void componentHidden(ComponentEvent e) {
		}

		public void componentMoved(ComponentEvent e) {
		}

		public void componentResized(ComponentEvent e) {
			if (e.getSource() == VoucherTablePane.this.getUISlider()) {
				VoucherTablePane.this.connEtoC2(e);
			}
		}

		public void componentShown(ComponentEvent e) {
		}

		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == VoucherTablePane.this.getUISlider()) {
				VoucherTablePane.this.connEtoC1(e);
			}
		}
	}

	public VoucherTablePane() {
		initialize();
	}

	public VoucherTablePane(LayoutManager p0) {
		super(p0);
	}

	public VoucherTablePane(LayoutManager p0, boolean p1) {
		super(p0, p1);
	}

	public VoucherTablePane(boolean p0) {
		super(p0);
	}

	private void connEtoC1(ChangeEvent arg1) {
		try {
			uISlider_StateChanged(arg1);

		} catch (Throwable ivjExc) {

			handleException(ivjExc);
		}
	}

	private void connEtoC2(ComponentEvent arg1) {
		try {
			uISlider_ComponentResized(arg1);

		} catch (Throwable ivjExc) {

			handleException(ivjExc);
		}
	}

	public TableCellEditor getCellEditor(int row, Object key) {
		return getUITablePane().getTable().getCellEditor(row, getTableModel().getColumnIndex(((Integer) key).intValue()));
	}

	public ColumnmodeVO[] getColumnModes() {
		return this.m_columnmode;
	}

	public int getRowHeight() {
		this.tableheight = getUITablePane().getTable().getRowHeight();
		return this.tableheight;
	}

	public VoucherTableModel getTableModel() {
		if (this.m_tablemodel == null) {
			this.m_tablemodel = new VoucherTableModel();
		}
		return this.m_tablemodel;
	}

	private UISlider getUISlider() {
		if (this.ivjUISlider == null) {
			try {
				this.ivjUISlider = new UISlider() {
					protected void processComponentEvent(ComponentEvent e) {
						try {
							super.processComponentEvent(e);

						} catch (NullPointerException ex) {
						}
					}

				};
				this.ivjUISlider.setName("UISlider");
				this.ivjUISlider.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
				this.ivjUISlider.setOrientation(1);

				this.ivjUISlider.setSize(10, 10);
				this.ivjUISlider.setBackground(getBackground());

			} catch (Throwable ivjExc) {

				handleException(ivjExc);
			}
		}
		return this.ivjUISlider;
	}

	public UITablePane getUITablePane() {
		if (this.ivjUITablePane == null) {
			try {
				this.ivjUITablePane = new UITablePane();
				this.ivjUITablePane.setName("UITablePane");

				this.ivjUITablePane.getTable().getTableHeader().setRequestFocusEnabled(false);

			} catch (Throwable ivjExc) {

				handleException(ivjExc);
			}
		}
		return this.ivjUITablePane;
	}

	private void handleException(Throwable exception) {
		Logger.debug("--------- 未捕捉到的异常 ---------");
		Logger.error(exception.getMessage(), exception);
	}

	private void initConnections() throws Exception {
		getUISlider().addChangeListener(this.ivjEventHandler);
		getUISlider().addComponentListener(this.ivjEventHandler);
	}

	private void initialize() {
		try {
			setName("VoucherTablePane");
			setLayout(new java.awt.BorderLayout());
			setSize(607, 358);
			add(getUITablePane(), "Center");
			add(getUISlider(), "East");
			initConnections();
		} catch (Throwable ivjExc) {
			handleException(ivjExc);
		}

		initTable();
	}

	private void initTable() {
		getUITablePane().setTable(new VoucherTable());
		getUITablePane().getTable().setAutoResizeMode(0);
		getUITablePane().getTable().setSelectionBackground(new Color(220, 220, 255));
		getUITablePane().getTable().setRowHeight(this.tableheight);
		getUITablePane().getTable().getSelectionModel().addListSelectionListener(this);
	}

	public boolean isEditable() {
		return getTableModel().isEditable();
	}

	public void repaintCell(int index, int key) {
		int row = index;
		int column = getTableModel().getColumnIndex(key);
		if (column < 0)
			return;
		((VoucherTable) getUITablePane().getTable()).repaintCell(row, column);
	}

	public void rowSelectionIndexChanged(int[] indexs) {
		getUITablePane().getTable().getSelectionModel().clearSelection();
		if ((indexs == null) || (indexs.length == 0)) {

			return;
		}
		int rowcount = getUITablePane().getTable().getRowCount();
		for (int i = 0; i < indexs.length; i++) {
			if (indexs[i] < rowcount) {
				getUITablePane().getTable().getSelectionModel().addSelectionInterval(indexs[i], indexs[i]);
			}
		}
	}

	public void setBackground(Color color) {
		setBackground(this, color);
	}

	private void setBackground(Component c, Color color) {
		if ((c instanceof Container)) {
			Component[] cmps = ((Container) c).getComponents();
			if (cmps != null) {
				for (int i = 0; i < cmps.length; i++) {
					setBackground(cmps[i], color);
				}
			}
		}
		if (c == this) {
			super.setBackground(color);
		} else {
			c.setBackground(color);
		}
	}

	public void setColumnModes(ColumnmodeVO[] newM_columnmode) {
		if (newM_columnmode == null) {
			return;
		}
		Vector vecCol = new Vector();
		for (int i = 0; i < newM_columnmode.length; i++) {
			if (newM_columnmode[i].getColumnvisiable().booleanValue()) {
				vecCol.addElement(newM_columnmode[i]);
			}
		}
		ColumnmodeVO[] cols = null;
		if (vecCol.size() > 0) {
			cols = new ColumnmodeVO[vecCol.size()];
			vecCol.copyInto(cols);
		}
		if (cols == null) {
			throw new GlBusinessException(NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000396"));
		}
		nc.ui.gl.beans.sort.Quicksort.sort(cols, new nc.vo.gl.voucher.ColumnCompareForSort());
		getTableModel().setColumnModes(cols);
		getUITablePane().getTable().setModel(getTableModel());
		getTableModel().fireTableStructureChanged();
		int[] width = new int[cols.length];
		ClientSetup currentClientSetup = nc.ui.sm.clientsetup.ClientSetupCache.getCurrentClientSetup();

		for (int i = 0; i < cols.length; i++) {
			if (cols[i].getColumnwidth() != null) {
				width[i] = cols[i].getColumnwidth().intValue();
			} else {
				width[i] = 100;
			}
			String key = getClass().getName() + cols[i].getColumnname();
			if ((currentClientSetup != null) && (currentClientSetup.get(key) != null) && ((currentClientSetup.get(key) instanceof Integer))) {
				Integer width2 = (Integer) currentClientSetup.get(key);
				getUITablePane().getTable().getColumnModel().getColumn(i).setWidth(width2.intValue());
				getUITablePane().getTable().getColumnModel().getColumn(i).setPreferredWidth(width2.intValue());
			} else {
				getUITablePane().getTable().getColumnModel().getColumn(i).setWidth(width[i]);
				getUITablePane().getTable().getColumnModel().getColumn(i).setPreferredWidth(width[i]);
			}
		}

		String key = getClass().getName() + "getRowHeight";
		if ((currentClientSetup != null) && (currentClientSetup.get(key) != null) && ((currentClientSetup.get(key) instanceof Integer))) {
			Integer height = (Integer) currentClientSetup.get(key);

			setRowHeight(height);
		}

		Vector vecNotNullColumn = new Vector();
		for (int i = 0; i < getUITablePane().getTable().getColumnCount(); i++) {
			getUITablePane().getTable().getColumnModel().getColumn(i).setCellRenderer(new VoucherTableCellRenderer());

			((VoucherTableCellRenderer) getUITablePane().getTable().getColumnModel().getColumn(i).getCellRenderer()).setLineWrap(getTableModel()
					.getColumnModes()[i].getIslinewrap().booleanValue());
			((VoucherTableCellRenderer) getUITablePane().getTable().getColumnModel().getColumn(i).getCellRenderer()).setVoucherKey(getTableModel()
					.getColumnModes()[i].getColumnkey().intValue());
			switch (getTableModel().getColumnModes()[i].getAlignment().intValue()) {
				case 1:
					((VoucherTableCellRenderer) getUITablePane().getTable().getColumnModel().getColumn(i).getCellRenderer()).setHorizontalAlignment(2);
					break;
				case 2:
					((VoucherTableCellRenderer) getUITablePane().getTable().getColumnModel().getColumn(i).getCellRenderer()).setHorizontalAlignment(0);
					break;
				case 3:
					((VoucherTableCellRenderer) getUITablePane().getTable().getColumnModel().getColumn(i).getCellRenderer()).setHorizontalAlignment(4);
					break;
				default:
					((VoucherTableCellRenderer) getUITablePane().getTable().getColumnModel().getColumn(i).getCellRenderer()).setHorizontalAlignment(2);
			}
			if ((getTableModel().getColumnModes()[i].getColumnclass() == null) || (getTableModel().getColumnModes()[i].getColumnclass().trim().length() == 0)) {
				getUITablePane().getTable().getColumnModel().getColumn(i)
						.setCellEditor(TableCellEditorCreator.createDefaultCellEditor(getTableModel().getColumnModes()[i].getColumnkey().intValue()));
			} else {
				try {
					Class m_classce = Class.forName(getTableModel().getColumnModes()[i].getColumnclass());
					Object m_objectce = m_classce.newInstance();
					TableCellEditor ce = (TableCellEditor) m_objectce;
					getUITablePane().getTable().getColumnModel().getColumn(i).setCellEditor(ce);
					if ((ce instanceof TableCellRenderer)) {
						getUITablePane().getTable().getColumnModel().getColumn(i).setCellRenderer((TableCellRenderer) ce);
					}
				} catch (Exception e) {
					Logger.error(e.getMessage(), e);
					Logger.debug("The tablecelleditor(" + getTableModel().getColumnModes()[i].getColumnclass() + ") can't initialize correctly!Use default.");
					getUITablePane().getTable().getColumnModel().getColumn(i)
							.setCellEditor(TableCellEditorCreator.createDefaultCellEditor(getTableModel().getColumnModes()[i].getColumnkey().intValue()));
				}
			}

			switch (getTableModel().getColumnModes()[i].getColumnkey().intValue()) {
				case 103:
				case 104:
				case 109:
				case 301:
				case 302:
				case 304:
				case 305:
				case 320:
//				20161223-tsy-修改自定义项目1：添加非空限制
				case 151:
//				end
					vecNotNullColumn.addElement(new Integer(i));
			}

		}

		if (vecNotNullColumn.size() > 0) {
			int[] notNullCol = new int[vecNotNullColumn.size()];
			for (int i = 0; i < vecNotNullColumn.size(); i++) {
				notNullCol[i] = ((Integer) vecNotNullColumn.elementAt(i)).intValue();
			}
			getUITablePane().getTable().setNotNullColumnByModelColumns(notNullCol);
		}
		this.m_columnmode = newM_columnmode;
	}

	public void setEditable(boolean b) {
		getTableModel().setEditable(b);
	}

	public void setPk_corp(String strpk_corp) {
		for (int i = 0; i < getUITablePane().getTable().getColumnCount(); i++) {
			if (getColumnModes()[i].getColumnkey().intValue() == 109) {
				TableCellEditor ce = getUITablePane().getTable().getColumnModel().getColumn(i).getCellEditor();
				if (ce != null) {
					Component cell = ce.getTableCellEditorComponent(getUITablePane().getTable(), ce.getCellEditorValue(), false, 0, i);
					if (cell != null) {
						if ((cell instanceof UIRefPane)) {
							if (((UIRefPane) cell).getRefModel() != null) {
								((UIRefPane) cell).getRefModel().setPk_org(strpk_corp);
							}
						} else {
							Method setMethod = null;
							try {
								setMethod = cell.getClass().getMethod("setPk_corp", new Class[] { String.class });
							} catch (NoSuchMethodException e) {
							}

							if (setMethod != null) {

								try {

									setMethod.invoke(cell, new Object[] { strpk_corp });
								} catch (IllegalAccessException e) {
									Logger.error(e.getMessage(), e);
									throw new GlBusinessException(e.getMessage());
								} catch (InvocationTargetException e) {
									Logger.error(e.getMessage(), e);
									throw new GlBusinessException(e.getTargetException().getMessage());
								}
							}
						}
					}
				}
			}
		}
	}

	public void setRowForeground(int index, Color color) {
		for (int i = 0; i < getUITablePane().getTable().getColumnCount(); i++) {
			TableCellRenderer renderer = getUITablePane().getTable().getColumnModel().getColumn(i).getCellRenderer();
			if ((renderer instanceof VoucherTableCellRenderer))
				((VoucherTableCellRenderer) renderer).setForeground(color, index);
		}
	}

	public void setRowBackground(int index, Color color) {
		for (int i = 0; i < getUITablePane().getTable().getColumnCount(); i++) {
			TableCellRenderer renderer = getUITablePane().getTable().getColumnModel().getColumn(i).getCellRenderer();
			if ((renderer instanceof VoucherTableCellRenderer))
				((VoucherTableCellRenderer) renderer).setBackground(color, index);
		}
	}

	public void setRowHeight(int row, int rowheight) {
		getUITablePane().getTable().setRowHeight(row, rowheight);
	}

	public void setRowHeight(int rowheight) {
		getUITablePane().getTable().setRowHeight(rowheight);
		this.tableheight = rowheight;
		try {
			getUISlider().setValue(getUISlider().getMaximum() - this.tableheight);
		} catch (Exception e) {
		}
	}

	public void setRowHeight(Integer rowheight) {
		getUITablePane().getTable().setRowHeight(rowheight.intValue());
		this.tableheight = rowheight.intValue();
		try {
			getUISlider().setValue(getUISlider().getMaximum() - this.tableheight);
		} catch (Exception e) {
		}
	}

	public void setTableModel(VoucherTableModel newM_tablemodel) {
		getUITablePane().getTable().setModel(newM_tablemodel);
		this.m_tablemodel = newM_tablemodel;
	}

	public void setValue(Object value) {
		getUITablePane().getTable().updateUI();
	}

	public void setVoucherModel(IVoucherModel model) {
		if (model != null)
			model.addListener("selectionindexlistener", this);
		getTableModel().setVoucherModel(model);
		getUITablePane().getTable().updateUI();
	}

	public boolean stopEditing() {
		TableCellEditor ce = getUITablePane().getTable().getCellEditor();
		boolean b = true;
		if (ce != null) {
			b = ce.stopCellEditing();
		}
		return b;
	}

	public void uISlider_ComponentResized(ComponentEvent componentEvent) {
		getUISlider().setMaximum(getUISlider().getSize().height - 15);
		getUISlider().setValue(getUISlider().getMaximum() - this.tableheight);
		if ((getUISlider().isVisible()) && (getUISlider().isShowing())) {
			getUISlider().updateUI();
		}
	}

	public void uISlider_StateChanged(ChangeEvent stateChangeEvent) {
		if (getUISlider().getValueIsAdjusting()) {
			this.tableheight = (getUISlider().getMaximum() - getUISlider().getValue());
			getUITablePane().getTable().setRowHeight(this.tableheight);
		}
	}

	public void updateUI() {
		getUITablePane().getTable().updateUI();
		super.updateUI();
	}

	public void valueChanged(ListSelectionEvent e) {
		if (checkAssInput(e)) {
		}

		try {
			stopEditing();
		} catch (RuntimeException e1) {
			Logger.error(e1.getMessage(), e1);
		}
		if (getUITablePane().getTable().getSelectedRow() < 0)
			return;
		try {
			getTableModel().getVoucherModel().setSelectedIndex(getUITablePane().getTable().getSelectedRows(), false);
		} catch (NullPointerException eee) {
			handleException(eee);
		}

		java.awt.Rectangle cellRect = getUITablePane().getTable().getCellRect(getUITablePane().getTable().getSelectedRow(),
				getUITablePane().getTable().getSelectedColumn(), false);
		if ((cellRect != null) && (getUITablePane().getTable().getSelectedRowCount() == 1)) {
			getUITablePane().getTable().scrollRectToVisible(cellRect);
		}
		if (((e.getLastIndex() == getUITablePane().getTable().getRowCount() - 1) || (e.getFirstIndex() == getUITablePane().getTable().getRowCount() - 1))
				&& (getTableModel().isEditable())) {

			UFBoolean isCanEdit = (UFBoolean) getTableModel().getVoucherModel().getValue(-1, new Integer(26));

			if ((isCanEdit != null) && (isCanEdit.booleanValue())) {
				getTableModel().getVoucherModel().doOperation("ADDEMPTYDETAIL");
			}
			getUITablePane().getTable().updateUI();
		}
	}

	private boolean checkAssInput(ListSelectionEvent e) {
		boolean errInput = false;
		if ((e.getFirstIndex() == e.getLastIndex()) || (e.getFirstIndex() == -1) || (e.getLastIndex() == -1)) {
			return errInput;
		}
		if ((!e.getValueIsAdjusting()) && (isLastIsAdjust())) {
			setLastIsAdjust(e.getValueIsAdjusting());
			return errInput;
		}
		setLastIsAdjust(e.getValueIsAdjusting());
		int preIndex = e.getFirstIndex();
		if (getUITablePane().getTable().getSelectedRow() == preIndex) {
			preIndex = e.getLastIndex();
		}
		if (preIndex < 0) {
			return errInput;
		}
		if (!getUITablePane().getTable().getModel()
				.isCellEditable(preIndex, getUITablePane().getTable().getColumnModel().getColumnIndex(GLStringConst.getStringAss()))) {
			return errInput;
		}

		DetailVO[] details = (DetailVO[]) getTableModel().getVoucherModel().getParameter("details");
		if (details.length >= preIndex + 1) {

			DetailVO detail = details[preIndex];
			if ((!StringUtils.isEmpty(detail.getPk_accasoa())) && (detail.getAss() != null) && (detail.getAss().length > 0)) {
				AccAssVO[] assRules = AccountCache.getInstance()
						.getAccountVOByPK(detail.getPk_accountingbook(), detail.getPk_accasoa(), detail.getPrepareddate().toStdString()).getAccass();
				if (assRules != null) {
					for (AccAssVO accass : assRules) {
						getAssControlRuels().put(detail.getPk_accasoa() + accass.getPk_entity(), accass);
					}
				}

				for (AssVO ass : detail.getAss()) {
					boolean legal = true;
					String errMsg = null;
					if ((StringUtils.isEmpty(ass.getPk_Checkvalue())) && (getAssControlRuels().get(detail.getPk_accasoa() + ass.getPk_Checktype()) != null)
							&& (((AccAssVO) getAssControlRuels().get(detail.getPk_accasoa() + ass.getPk_Checktype())).getIsempty() != null)
							&& (getAssControlRuels().get(detail.getPk_accasoa() + ass.getPk_Checktype()) != null)
							&& (!((AccAssVO) getAssControlRuels().get(detail.getPk_accasoa() + ass.getPk_Checktype())).getIsempty().booleanValue())) {

						legal = false;
						errMsg = NCLangRes4VoTransl.getNCLangRes().getStrByID("glpub_0", "02002003-0263", null, new String[] { ass.getChecktypename() });

					} else if (getAssControlRuels().get(detail.getPk_accasoa() + ass.getPk_Checktype()) != null) {
						AccAssVO accass = (AccAssVO) getAssControlRuels().get(detail.getPk_accasoa() + ass.getPk_Checktype());

						if ((accass.getIsempty() != null) && (accass.getIsempty().booleanValue())
								&& ((StringUtils.isEmpty(ass.getPk_Checkvalue())) || ("~".equals(ass.getPk_Checkvalue())))) {
							continue;
						}

						IGeneralAccessor acc = nc.pubitf.bd.accessor.GeneralAccessorFactory.getAccessor(ass.getM_classid());
						if ((accass.getIsnonleafused() != null) && (!accass.getIsnonleafused().booleanValue()) && (acc != null) && (acc.isHaslevel())
								&& (!acc.isLeaf(detail.getPk_unit(), ass.getPk_Checkvalue()))) {
							legal = false;
							errMsg = NCLangResOnserver.getInstance().getStrByID("200243", "UPP200243-000006", null, new String[] { ass.getChecktypename() });
						}
					}

					if (!legal) {
						try {
							stopEditing();
						} catch (RuntimeException e1) {
							Logger.error(e1.getMessage(), e1);
						}
						int column = getUITablePane().getTable().getColumnModel().getColumnIndex(GLStringConst.getStringAss());
						if ((column >= 0) && (column <= getUITablePane().getTable().getColumnCount())) {
							FiUif2MsgUtil.showUif2DetailMessage(this, NCLangRes.getInstance().getStrByID("_Beans", "UPP_Beans-000053"), errMsg);
							getUITablePane().getTable().editCellAt(preIndex, column);
							AssRefTableEditorRenderer com = (AssRefTableEditorRenderer) getUITablePane().getTable().getCellEditor(preIndex, column)
									.getTableCellEditorComponent(getUITablePane().getTable(), detail.getAssid(), true, preIndex, column);
							com.onButtonClicked();
							errInput = true;
							return errInput;
						}
					}
				}
			} else if ((!StringUtils.isEmpty(detail.getPk_accasoa())) && ((detail.getAss() == null) || (detail.getAss().length <= 0))) {
				AccAssVO[] assRules = AccountCache.getInstance()
						.getAccountVOByPK(detail.getPk_accountingbook(), detail.getPk_accasoa(), detail.getPrepareddate().toStdString()).getAccass();
				if (assRules != null) {
					boolean needInput = false;
					for (AccAssVO accass : assRules) {
						if ((accass.getIsempty() != null) && (!accass.getIsempty().booleanValue())) {
							needInput = true;
							break;
						}
					}
					if (needInput) {
						try {
							stopEditing();
						} catch (RuntimeException e1) {
							Logger.error(e1.getMessage(), e1);
						}
						int column = getUITablePane().getTable().getColumnModel().getColumnIndex(GLStringConst.getStringAss());
						if ((column >= 0) && (column <= getUITablePane().getTable().getColumnCount())) {
							FiUif2MsgUtil.showUif2DetailMessage(this, NCLangRes.getInstance().getStrByID("_Beans", "UPP_Beans-000053"), NCLangRes4VoTransl
									.getNCLangRes().getStrByID("glpub_0", "02002003-0263", null, new String[] { "" }));

							getUITablePane().getTable().editCellAt(preIndex, column);
							AssRefTableEditorRenderer com = (AssRefTableEditorRenderer) getUITablePane().getTable().getCellEditor(preIndex, column)
									.getTableCellEditorComponent(getUITablePane().getTable(), detail.getAssid(), true, preIndex, column);
							com.onButtonClicked();
							errInput = true;
							return errInput;
						}
					}
				}
			}
		}
		return errInput;
	}

	public void setFocusCell(int index, Object okey) {
		if (okey == null)
			return;
		int row = index;
		int column = getTableModel().getColumnIndex(new Integer(okey.toString()).intValue());
		getUITablePane().getTable().clearSelection();
		if (row >= 0) {
			getUITablePane().getTable().getSelectionModel().setSelectionInterval(row, row);
			if (column >= 0) {
				getUITablePane().getTable().getColumnModel().getSelectionModel().setSelectionInterval(column, column);
				if (getUITablePane().getTable().editCellAt(row, column)) {
					TableCellEditor ce = getUITablePane().getTable().getCellEditor();
					if (ce != null) {
						Component ccmp = ce.getTableCellEditorComponent(getUITablePane().getTable(), ce.getCellEditorValue(), true, 0, 0);
						if (ccmp != null) {
							if ((ccmp instanceof UIRefPane)) {
								((UIRefPane) ccmp).getUITextField().requestFocus();
							} else {
								ccmp.requestFocus();
							}
						}
					}
				}
			}
		}
	}

	private HashMap<String, AccAssVO> getAssControlRuels() {
		return this.assControlRuels;
	}

	private void setLastIsAdjust(boolean lastIsAdjust) {
		this.lastIsAdjust = lastIsAdjust;
	}

	private boolean isLastIsAdjust() {
		return this.lastIsAdjust;
	}
}