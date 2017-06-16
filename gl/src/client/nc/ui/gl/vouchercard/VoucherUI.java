package nc.ui.gl.vouchercard;

import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.table.TableCellEditor;
import nc.bs.logging.Log;
import nc.bs.logging.Logger;
import nc.itf.fi.pub.MeasdocUtils;
import nc.ui.gl.datacache.GLParaDataCache;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.gl.uicfg.DefaultBasicView;
import nc.ui.gl.uicfg.IBasicModel;
import nc.ui.gl.uicfg.voucher.VoucherCell;
import nc.ui.gl.uicfg.voucher.VoucherTableCellEditor;
import nc.ui.gl.uicfg.voucher.VoucherTablePane;
import nc.ui.gl.vouchertools.VoucherDataCenter;
import nc.ui.glcom.displayformattool.ShowContentCenter;
import nc.ui.glcom.numbertool.GlNumberFormat;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UITable;
import nc.ui.pub.beans.UITablePane;
import nc.vo.bd.account.AccountVO;
import nc.vo.gateway60.pub.GlBusinessException;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.gl.voucher.ColumnmodeVO;
import nc.vo.glcom.ass.AssVO;
import nc.vo.glcom.constant.GLStringConst;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import org.apache.commons.lang.StringUtils;

public class VoucherUI extends DefaultBasicView implements IVoucherView {
	public VoucherUI() {
	}

	public void afterUILoad() {
		Vector c = (Vector) getKey_Cell_Map().get(Integer.valueOf(2));
		if (c != null) {
			for (int i = 0; i < c.size(); i++) {
				if ((c.elementAt(i) instanceof VoucherTablePane))
					((VoucherTablePane) c.elementAt(i)).setVoucherModel((IVoucherModel) getModel());
			}
		}
	}

	public VoucherTablePane getVoucherTablePane() {
		Vector c = (Vector) getKey_Cell_Map().get(Integer.valueOf(2));
		if (c != null) {
			for (int i = 0; i < c.size(); i++) {
				if ((c.elementAt(i) instanceof VoucherTablePane))
					return (VoucherTablePane) c.elementAt(i);
			}
		}
		return null;
	}

	public void convertCode_Name(Object param) {
		if (param == null) {
			return;
		}
		if ((param instanceof Integer)) {
			switch (((Integer) param).intValue()) {
				case 103:
				case 301:
				case 302:
					Vector vc = (Vector) getKey_Cell_Map().get(Integer.valueOf(2));
					if (vc == null) {
						return;
					}
					for (int i = 0; i < vc.size(); i++) {
						if ((vc.elementAt(i) instanceof VoucherTablePane)) {
							VoucherTablePane vchp = (VoucherTablePane) vc.elementAt(i);
							ColumnmodeVO[] cms = vchp.getColumnModes();
							for (int j = 0; j < cms.length; j++) {
								if (cms[j].getColumnkey().intValue() == 301) {
									cms[j].setColumnkey(Integer.valueOf(302));
								} else if (cms[j].getColumnkey().intValue() == 302)
									cms[j].setColumnkey(Integer.valueOf(301));
							}
							vchp.setColumnModes(cms);
						}
					}
					break;
			}

		}
	}

	public Object getCellEditor(int index, Object oKey) {
		Object detailCell = getKey_Cell_Map().get(Integer.valueOf(2));
		if (detailCell != null) {
			return ((VoucherTablePane) ((Vector) detailCell).elementAt(0)).getCellEditor(index, oKey);
		}
		return null;
	}

	public VoucherTablePane getVoucherTable() {
		Object detailCell = getKey_Cell_Map().get(Integer.valueOf(2));
		if (detailCell != null) {
			return (VoucherTablePane) ((Vector) detailCell).elementAt(0);
		}
		return null;
	}

	public Object getCellRenderer(int index, Object oKey) {
		return getCellEditor(index, oKey);
	}

	public Object getVoucherCellEditor(int index, Object oKey) {
		Object detailCell = getKey_Cell_Map().get(oKey);
		if (index < 0)
			index = 0;
		if (detailCell != null) {
			return ((Vector) detailCell).elementAt(index);
		}
		return null;
	}

	public IVoucherControl getVoucherControl() {
		return (IVoucherControl) getControl();
	}

	public IVoucherModel getVoucherModel() {
		return (IVoucherModel) getModel();
	}

	public void refresh(int index, Object okey, Object value) {
		if (okey == null)
			return;
		int key = Integer.valueOf(okey.toString()).intValue();
		Object detailCell = null;
		switch (key) {

			case 0:
				refreshAll();
				break;

			case 1:
				detailCell = getKey_Cell_Map().get(okey);
				VoucherVO voucher = (VoucherVO) getModel().getValue(index, Integer.valueOf(0));
				if (detailCell != null) {
					setCellValue(((Vector) detailCell).elementAt(0), index + 1 + "/" + voucher.getNumDetails());
				}
				DetailVO detail = (DetailVO) getModel().getValue(index, Integer.valueOf(1));
				refreshDetailAssistant(detail, voucher);

				break;

			case 2:
				Log.getInstance(getClass()).info("okey=" + okey);
				Log.getInstance(getClass()).info("getKey_Cell_Map()" + getKey_Cell_Map());
				detailCell = getKey_Cell_Map().get(okey);
				if (detailCell != null) {
					((JComponent) ((Vector) detailCell).elementAt(0)).updateUI();
				}

				break;
			case 55:
				refreshAllOrgBook(value == null ? null : value.toString().trim());

				break;

			case 53:
				refreshAllCorp(value == null ? null : value.toString().trim());
				break;

			case 37:
				if (value != null) {

					super.refresh(index, Integer.valueOf(27), value);
				}
				break;
			case 702:
				if (value != null) {

					super.refresh(index, Integer.valueOf(27), value);
				}
				break;

			case 401:
				super.refresh(index, Integer.valueOf(401), value);
				break;

			case 125:
				if (value != null) {
					detailCell = getKey_Cell_Map().get(Integer.valueOf(2));
					if (detailCell != null) {
						((VoucherTablePane) ((Vector) detailCell).elementAt(0)).setRowForeground(index, Color.red);
					}
				}
				break;
			case 27:
				if (value == null)
					return;
				if ((value instanceof String)) {
					super.refresh(index, Integer.valueOf(27), value);
				} else {
					Color c = Color.blue;
					if (((UFBoolean) value).booleanValue()) {
						c = Color.gray;
						String str = GLStringConst.DISCARD;
						super.refresh(index, Integer.valueOf(27), str);
					}
					detailCell = getKey_Cell_Map().get(Integer.valueOf(2));
					if (detailCell != null) {
						((JComponent) ((Vector) detailCell).elementAt(0)).setForeground(c);
					}
				}
				break;
			case 303:
				String pk_corp = (String) getModel().getValue(index, Integer.valueOf(53));
				String pk_glorgbook = (String) getModel().getValue(index, Integer.valueOf(55));
				UFDate date = (UFDate) getModel().getValue(index, Integer.valueOf(17));
				String pk_accsubj = (String) getModel().getValue(index, Integer.valueOf(103));

				super.refresh(index, Integer.valueOf(108), ShowContentCenter.getShowAss(pk_glorgbook, pk_accsubj, date.toStdString(), (AssVO[]) value));
				break;

			case 405:
				if (value != null) {
					if (((value instanceof UFBoolean)) && (((UFBoolean) value).booleanValue())) {
						value = GLStringConst.TEMPSAVE;
					}
					super.refresh(index, Integer.valueOf(27), value);
				}
				break;

			case 606:
			case 607:
			case 608:
				setBusiUnit(index, Integer.valueOf(606), value);
				break;
			default:
				super.refresh(index, okey, value);
		}

		updateUI();
	}

	private void setBusiUnit(int row, Object okey, Object value) {
		VoucherTablePane table = null;
		Object detailCell = getKey_Cell_Map().get(Integer.valueOf(2));
		if (detailCell != null) {
			table = (VoucherTablePane) ((Vector) detailCell).elementAt(0);
		}
		TableCellEditor cellEditor = table.getCellEditor(row, okey);
		if ((cellEditor == null) || (!(cellEditor instanceof VoucherTableCellEditor))) {
			return;
		}
		VoucherTableCellEditor voucherEditor = (VoucherTableCellEditor) cellEditor;
		Component component = voucherEditor.getComponent();
		if (!(component instanceof UIRefPane)) {
			return;
		}
		UIRefPane buRefPane = (UIRefPane) component;
		buRefPane.setPK(value);
	}

	public void refreshAll() {
		VoucherVO voucher = (VoucherVO) getModel().getValue(-1, Integer.valueOf(0));
		if (voucher != null) {
			refreshAllOrgBook(voucher.getPk_accountingbook());
			refreshAllCorp(VoucherDataCenter.getPk_corpByPk_glorgbook(voucher.getPk_accountingbook()));
		}
		super.refreshAll();
		refreshDetailAssistant(new DetailVO(), voucher);

		refresh(-1, Integer.valueOf(37), voucher.getErrmessage());

		if (voucher.getIsOffer().booleanValue()) {
			refresh(-1, Integer.valueOf(702), GLStringConst.OFFERVOUCHER);
		}

		refresh(-1, Integer.valueOf(405), voucher.getTempsaveflag());
		refresh(-1, Integer.valueOf(27), voucher.getDiscardflag());
		if (((voucher.getDiscardflag() == null) || (!voucher.getDiscardflag().booleanValue())) && (voucher.getErrmessage() == null)
				&& ((voucher.getTempsaveflag() == null) || (!voucher.getTempsaveflag().booleanValue())) && (!voucher.getIsOffer().booleanValue())) {

			refresh(-1, Integer.valueOf(27), GLStringConst.NORMAL);
		}
		refresh(-1, Integer.valueOf(401), voucher.getIsdifflag());
		if ((voucher.getVoucherkind() != null) && (voucher.getVoucherkind().intValue() == 1)) {
			Object cell = null;
			cell = getVoucherCellEditor(-1, Integer.valueOf(55));
			if ((cell instanceof VoucherCell)) {
				((VoucherCell) cell).setEditable(false);
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(false);
			}
			cell = getVoucherCellEditor(-1, Integer.valueOf(17));
			if ((cell instanceof VoucherCell)) {
				((VoucherCell) cell).setEditable(false);
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(false);
			}
			cell = getVoucherCellEditor(-1, Integer.valueOf(32));
			if ((cell instanceof VoucherCell)) {
				((VoucherCell) cell).setEditable(false);
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(false);
			}
		}
		if (voucher.getPk_voucher() != null) {
			Object cell = getVoucherCellEditor(-1, Integer.valueOf(53));
			if ((cell instanceof VoucherCell)) {
				((VoucherCell) cell).setEditable(false);
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(false);
			}
			cell = getVoucherCellEditor(-1, Integer.valueOf(55));
			if ((cell instanceof VoucherCell)) {
				((VoucherCell) cell).setEditable(false);
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(false);
			}

			if ((voucher.getConvertflag() != null) && (voucher.getConvertflag().booleanValue()) && (voucher.getIsdifflag() != null)
					&& (voucher.getIsdifflag().booleanValue())) {
				cell = getVoucherCellEditor(-1, Integer.valueOf(401));
				if ((cell instanceof VoucherCell)) {
					((VoucherCell) cell).setEditable(false);
				} else if ((cell instanceof Component)) {
					((Component) cell).setEnabled(false);
				}
			}

			cell = getVoucherCellEditor(-1, Integer.valueOf(16));
			if (((cell instanceof VoucherCell)) && (isEditable())) {
				((VoucherCell) cell).setEditable(GLParaDataCache.getInstance().isEditVoucherNO(voucher.getPk_accountingbook()).booleanValue());
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(isEditable());
			}

			cell = getVoucherCellEditor(-1, Integer.valueOf(11));
			if (((cell instanceof VoucherCell)) && (isEditable())) {
				((VoucherCell) cell).setEditable(GLParaDataCache.getInstance().isEditVoucherNO(voucher.getPk_accountingbook()).booleanValue());
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(isEditable());
			}

		} else {
			Object cell = getVoucherCellEditor(-1, Integer.valueOf(53));
			if ((cell instanceof VoucherCell)) {
				((VoucherCell) cell).setEditable(true);
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(true);
			}
			cell = getVoucherCellEditor(-1, Integer.valueOf(55));
			if ((cell instanceof VoucherCell)) {
				((VoucherCell) cell).setEditable(isEditable());
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(isEditable());
			}
			cell = getVoucherCellEditor(-1, Integer.valueOf(16));
			if ((cell instanceof VoucherCell)) {
				((VoucherCell) cell).setEditable((isEditable())
						&& (GLParaDataCache.getInstance().isEditVoucherNO(voucher.getPk_accountingbook()).booleanValue()));
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(isEditable());
			}
			cell = getVoucherCellEditor(-1, Integer.valueOf(11));
			if ((cell instanceof VoucherCell)) {
				((VoucherCell) cell).setEditable((isEditable())
						&& (GLParaDataCache.getInstance().isEditVoucherNO(voucher.getPk_accountingbook()).booleanValue()));
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(isEditable());
			}
		}
		if (voucher.getModifyflag() != null) {
			String modstr = voucher.getModifyflag();

			boolean vouchereditable = !getVoucherHeadEditable(voucher);
			if (modstr.trim().length() < 3)
				modstr = modstr + "YYYYYYYYY";
			if ((modstr.charAt(0) == 'N') || (modstr.charAt(0) == 'n') || (vouchereditable)) {
				Object cell = getVoucherCellEditor(-1, Integer.valueOf(17));
				if ((cell instanceof VoucherCell)) {
					((VoucherCell) cell).setEditable(false);
				} else if ((cell instanceof Component)) {
					((Component) cell).setEnabled(false);
				}
			} else {
				Object cell = getVoucherCellEditor(-1, Integer.valueOf(17));
				if ((cell instanceof VoucherCell)) {
					((VoucherCell) cell).setEditable(isEditable());
				} else if ((cell instanceof Component)) {
					((Component) cell).setEnabled(true);
				}
			}
			if ((modstr.charAt(1) == 'N') || (modstr.charAt(1) == 'n') || (vouchereditable)) {
				Object cell = getVoucherCellEditor(-1, Integer.valueOf(11));
				if ((cell instanceof VoucherCell)) {
					((VoucherCell) cell).setEditable(false);
				} else if ((cell instanceof Component)) {
					((Component) cell).setEnabled(false);
				}
			} else {
				Object cell = getVoucherCellEditor(-1, Integer.valueOf(11));
				if ((cell instanceof VoucherCell)) {
					if (voucher.getPk_voucher() == null) {
						((VoucherCell) cell).setEditable(isEditable());
					} else {
						((VoucherCell) cell).setEditable((isEditable())
								&& (GLParaDataCache.getInstance().isEditVoucherNO(voucher.getPk_accountingbook()).booleanValue()));
					}
					UIRefPane vtb = (UIRefPane) ((VoucherCell) cell).getEditor();
					if ((vtb.getRefPK() == null) || (!vtb.getRefPK().equals(voucher.getPk_vouchertype()))) {
						vtb.setPK(voucher.getPk_vouchertype());
					}
				} else if ((cell instanceof Component)) {
					((Component) cell).setEnabled(isEditable());
				}
			}
			if ((modstr.charAt(2) == 'N') || (modstr.charAt(2) == 'n') || (vouchereditable)) {
				Object cell = getVoucherCellEditor(-1, Integer.valueOf(19));
				if ((cell instanceof VoucherCell)) {
					((VoucherCell) cell).setEditable(false);
				} else if ((cell instanceof Component)) {
					((Component) cell).setEnabled(false);
				}
			} else {
				Object cell = getVoucherCellEditor(-1, Integer.valueOf(19));
				if ((cell instanceof VoucherCell)) {
					((VoucherCell) cell).setEditable(isEditable());
				} else if ((cell instanceof Component)) {
					((Component) cell).setEnabled(isEditable());
				}
			}
		}

		boolean isMatched = false;
		if (voucher.getOffervoucher() == null) {
			for (DetailVO detail : voucher.getDetails()) {
				if ((detail.getIsmatched() != null) && (detail.getIsmatched().booleanValue())) {
					isMatched = true;
				}
			}
		}
		if (isMatched) {
			Object cell = getVoucherCellEditor(-1, Integer.valueOf(17));
			if ((cell instanceof VoucherCell)) {
				((VoucherCell) cell).setEditable(false);
			} else if ((cell instanceof Component)) {
				((Component) cell).setEnabled(false);
			}
		}
	}

	public boolean getVoucherHeadEditable(VoucherVO voucher) {
		if (((voucher.getDiscardflag() != null) && (voucher.getDiscardflag().booleanValue()))
				|| (voucher.getPk_casher() != null)
				|| (voucher.getPk_checked() != null)
				|| (voucher.getPk_manager() != null)
				|| ((VoucherDataCenter.isVoucherSelfEditDelete(voucher.getPk_accountingbook())) && (!GlWorkBench.getLoginUser()
						.equals(voucher.getPk_prepared())))) {
			return false;
		}
		if (getVoucherModel().getParameter("functionname") == null)
			return false;
		if ((getVoucherModel().getParameter("functionname").toString().trim().equals("preparevoucher"))
				|| (getVoucherModel().getParameter("functionname").toString().trim().equals("voucherbridge"))
				|| (getVoucherModel().getParameter("functionname").toString().trim().equals("offsetvoucher"))) {
			return true;
		}

		return false;
	}

	public void refreshAllCorp(String pk_corp) {
		if ((getKey_Cell_Map() != null) && (getKey_Cell_Map().size() > 0)) {
			Iterator keyiter = getKey_Cell_Map().keySet().iterator();
			Vector vecKeys = new Vector();
			while (keyiter.hasNext()) {
				vecKeys.addElement(keyiter.next());
			}
			for (int i = 0; i < vecKeys.size(); i++) {
				Vector vv = (Vector) getKey_Cell_Map().get(vecKeys.elementAt(i));
				if (vv != null) {
					for (int j = 0; j < vv.size(); j++) {
						Object o = vv.elementAt(j);
						setCellCorp(o, pk_corp);
					}
				}
			}
		}
	}

	public void refreshAllOrgBook(String pk_orgbook) {
		VoucherTablePane table = null;
		Object detailCell = getKey_Cell_Map().get(Integer.valueOf(2));
		if (detailCell != null) {
			table = (VoucherTablePane) ((Vector) detailCell).elementAt(0);
		}

		if ((getKey_Cell_Map() != null) && (getKey_Cell_Map().size() > 0)) {
			Iterator keyiter = getKey_Cell_Map().keySet().iterator();
			Vector vecKeys = new Vector();
			while (keyiter.hasNext()) {
				vecKeys.addElement(keyiter.next());
			}
			for (int i = 0; i < vecKeys.size(); i++) {
				Vector vv = (Vector) getKey_Cell_Map().get(vecKeys.elementAt(i));
				if (vv != null) {
					for (int j = 0; j < vv.size(); j++) {
						Object o = vv.elementAt(j);
						setCellOrgBook(o, pk_orgbook);
					}
				}
			}
		}
	}

	private ColumnmodeVO[] filterColums(ColumnmodeVO[] columnModes) {
		ColumnmodeVO[] cols = new ColumnmodeVO[columnModes.length];
		for (int i = 0; i < columnModes.length; i++) {
			if (columnModes[i].getColumnkey().intValue() == 606) {
				columnModes[i].setColumnvisiable(UFBoolean.FALSE);
			}
			cols[i] = columnModes[i];
		}
		return cols;
	}

	public void refreshDetailAssistant() {
	}

	private void refreshDetailAssistant(Vector cells, Object value) {
		if ((cells != null) && (cells.size() > 0)) {
			for (int i = 0; i < cells.size(); i++) {
				setCellValue(cells.elementAt(i), value);
			}
		}
	}

	public void refreshDetailAssistant(DetailVO detail, VoucherVO voucher) {
		Vector vv = null;
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(101));
		refreshDetailAssistant(vv, detail.getPk_detail());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(102));
		refreshDetailAssistant(vv, detail.getPk_voucher());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(103));
		refreshDetailAssistant(vv, detail.getPk_accasoa());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(104));
		refreshDetailAssistant(vv, detail.getPk_currtype());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(105));
		refreshDetailAssistant(vv, detail.getPk_sob());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(106));
		refreshDetailAssistant(vv, detail.getPk_org());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(107));
		refreshDetailAssistant(vv, detail.getDetailindex());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(108));

		refreshDetailAssistant(vv,
				ShowContentCenter.getShowAss(detail.getPk_glorgbook(), detail.getPk_accasoa(), voucher.getPrepareddate().toStdString(), detail.getAss()));

		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(109));
		refreshDetailAssistant(vv, detail.getExplanation());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(110));

		String pk_unit = VoucherDataCenter.getMaterialUnit(detail.getAss());
		if (StringUtils.isNotEmpty(pk_unit)) {
			refreshDetailAssistant(vv, GlNumberFormat.formatUFDouble(detail.getPrice()) + "/" + MeasdocUtils.getNameBypk(pk_unit));
		} else {
			AccountVO acc = VoucherDataCenter.getAccsubjByPK(voucher.getPk_accountingbook(), detail.getPk_accasoa(), voucher.getPrepareddate().toStdString());
			if (acc != null) {
				if (acc.getUnit() != null) {
					refreshDetailAssistant(vv, GlNumberFormat.formatUFDouble(detail.getPrice()) + "/" + MeasdocUtils.getNameBypk(acc.getUnit()));
				} else
					refreshDetailAssistant(vv, detail.getPrice());
			} else
				refreshDetailAssistant(vv, new UFDouble(0));
		}
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(111));
		refreshDetailAssistant(vv, detail.getExcrate1());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(112));
		refreshDetailAssistant(vv, detail.getExcrate2());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(328));
		refreshDetailAssistant(vv, detail.getExcrate3());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(329));
		refreshDetailAssistant(vv, detail.getExcrate4());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(113));
		refreshDetailAssistant(vv, detail.getDebitquantity());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(114));
		refreshDetailAssistant(vv, detail.getDebitamount());

		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(116));
		refreshDetailAssistant(vv, detail.getLocaldebitamount());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(117));
		refreshDetailAssistant(vv, detail.getCreditquantity());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(118));
		refreshDetailAssistant(vv, detail.getCreditamount());

		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(120));
		refreshDetailAssistant(vv, detail.getLocalcreditamount());

		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(325));
		refreshDetailAssistant(vv, detail.getGroupcreditamount());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(324));
		refreshDetailAssistant(vv, detail.getGroupdebitamount());

		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(327));
		refreshDetailAssistant(vv, detail.getGlobalcreditamount());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(326));
		refreshDetailAssistant(vv, detail.getGlobaldebitamount());

		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(121));
		refreshDetailAssistant(vv, detail.getModifyflag());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(122));
		refreshDetailAssistant(vv, detail.getRecieptclass());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(123));
		refreshDetailAssistant(vv, detail.getOppositesubj());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(124));
		refreshDetailAssistant(vv, detail.getContrastflag());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(125));
		refreshDetailAssistant(vv, detail.getErrmessage());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(126));
		refreshDetailAssistant(vv, detail.getCheckstyle());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(127));
		refreshDetailAssistant(vv, detail.getCheckno());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(128));
		if (detail.getCheckdate() != null) {
			refreshDetailAssistant(vv, detail.getCheckdate().toLocalString());
		} else {
			refreshDetailAssistant(vv, null);
		}
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(129));
		refreshDetailAssistant(vv, detail.getPk_innersob());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(130));
		refreshDetailAssistant(vv, detail.getPk_innercorp());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(131));

		refreshDetailAssistant(vv, detail.getVerifyno());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(132));

		refreshDetailAssistant(vv, detail.getVerifydate());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(133));
		refreshDetailAssistant(vv, detail.getBilltype());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(134));
		refreshDetailAssistant(vv, detail.getInnerbusno());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(135));
		refreshDetailAssistant(vv, detail.getInnerbusdate());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(151));
		refreshDetailAssistant(vv, detail.getFreevalue1());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(152));
		refreshDetailAssistant(vv, detail.getFreevalue2());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(153));
		refreshDetailAssistant(vv, detail.getFreevalue3());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(154));
		refreshDetailAssistant(vv, detail.getFreevalue4());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(155));
		refreshDetailAssistant(vv, detail.getFreevalue5());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(156));
		refreshDetailAssistant(vv, detail.getFreevalue6());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(157));
		refreshDetailAssistant(vv, detail.getFreevalue7());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(158));
		refreshDetailAssistant(vv, detail.getFreevalue8());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(159));
		refreshDetailAssistant(vv, detail.getFreevalue9());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(160));
		refreshDetailAssistant(vv, detail.getFreevalue10());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(161));
		refreshDetailAssistant(vv, detail.getFreevalue11());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(162));
		refreshDetailAssistant(vv, detail.getFreevalue12());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(163));
		refreshDetailAssistant(vv, detail.getFreevalue13());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(164));
		refreshDetailAssistant(vv, detail.getFreevalue14());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(165));
		refreshDetailAssistant(vv, detail.getFreevalue15());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(166));
		refreshDetailAssistant(vv, detail.getFreevalue16());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(167));
		refreshDetailAssistant(vv, detail.getFreevalue17());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(168));
		refreshDetailAssistant(vv, detail.getFreevalue18());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(169));
		refreshDetailAssistant(vv, detail.getFreevalue19());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(170));
		refreshDetailAssistant(vv, detail.getFreevalue20());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(171));
		refreshDetailAssistant(vv, detail.getFreevalue21());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(172));
		refreshDetailAssistant(vv, detail.getFreevalue22());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(173));
		refreshDetailAssistant(vv, detail.getFreevalue23());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(174));
		refreshDetailAssistant(vv, detail.getFreevalue24());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(175));
		refreshDetailAssistant(vv, detail.getFreevalue25());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(176));
		refreshDetailAssistant(vv, detail.getFreevalue26());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(177));
		refreshDetailAssistant(vv, detail.getFreevalue27());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(178));
		refreshDetailAssistant(vv, detail.getFreevalue28());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(179));
		refreshDetailAssistant(vv, detail.getFreevalue29());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(180));
		refreshDetailAssistant(vv, detail.getFreevalue30());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(301));
		refreshDetailAssistant(vv, detail.getAccsubjcode());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(302));
		refreshDetailAssistant(vv, detail.getAccsubjname());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(303));
		refreshDetailAssistant(vv, detail.getAss());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(304));
		refreshDetailAssistant(vv, detail.getCurrtypecode());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(305));
		refreshDetailAssistant(vv, detail.getCurrtypecode());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(306));
		refreshDetailAssistant(vv, detail.getCheckstylename());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(307));
		refreshDetailAssistant(vv, detail.getIsmatched());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(308));
		refreshDetailAssistant(vv, detail.getUserData());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(309));
		refreshDetailAssistant(vv, detail.getOtheruserdata());

		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(400));
		refreshDetailAssistant(vv, detail.getCashFlow());
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(310));
		refreshDetailAssistant(vv, detail.getValue(310));
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(311));
		refreshDetailAssistant(vv, detail.getValue(311));
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(312));
		refreshDetailAssistant(vv, detail.getValue(312));
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(313));
		refreshDetailAssistant(vv, detail.getValue(313));
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(501));
		refreshDetailAssistant(vv, detail.getValue(501));
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(502));
		refreshDetailAssistant(vv, detail.getValue(502));
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(330));
		refreshDetailAssistant(vv, detail.getValue(330));
		vv = (Vector) getKey_Cell_Map().get(Integer.valueOf(331));
		refreshDetailAssistant(vv, detail.getValue(331));
	}

	protected void setCellCorp(Object cell, String value) {
		Method setMethod = null;
		try {
			setMethod = cell.getClass().getMethod("setPk_corp", new Class[] { String.class });
		} catch (NoSuchMethodException e) {
		}
		if (setMethod != null) {
			try {
				setMethod.invoke(cell, new Object[] { value });
			} catch (IllegalAccessException e) {
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getMessage());
			} catch (InvocationTargetException e) {
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getTargetException().getMessage());
			}
		}
	}

	protected void setCellOrgBook(Object cell, String value) {
		Method setMethod = null;
		try {
			setMethod = cell.getClass().getMethod("setPk_glorgbook", new Class[] { String.class });
		} catch (NoSuchMethodException e) {
		}
		if (setMethod != null) {
			try {
				setMethod.invoke(cell, new Object[] { value });
			} catch (IllegalAccessException e) {
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getMessage());
			} catch (InvocationTargetException e) {
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getTargetException().getMessage());
			}
		}
	}

	protected void setCellValue(Object cell, Object value) {
		Method setMethod = null;
		try {
			setMethod = cell.getClass().getMethod("setValue", new Class[] { Object.class });
		} catch (NoSuchMethodException e) {
		}
		if (setMethod == null) {
			try {
				setMethod = cell.getClass().getMethod("setText", new Class[] { String.class });
			} catch (NoSuchMethodException e) {
			}
			if (setMethod != null) {
				try {
					setMethod.invoke(cell, new Object[] { value == null ? null : value.toString() });
				} catch (IllegalAccessException e) {
					Logger.error(e.getMessage(), e);
					throw new GlBusinessException(e.getMessage());
				} catch (InvocationTargetException e) {
					Logger.error(e.getMessage(), e);
					throw new GlBusinessException(e.getTargetException().getMessage());
				}

			}

		} else {
			try {
				setMethod.invoke(cell, new Object[] { value });
			} catch (IllegalAccessException e) {
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getMessage());
			} catch (InvocationTargetException e) {
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getTargetException().getMessage());
			}
		}
	}

	public void setFocus(int index, Object okey) {
		VoucherTablePane table = null;
		Object detailCell = getKey_Cell_Map().get(Integer.valueOf(2));
		if (detailCell != null) {
			table = (VoucherTablePane) ((Vector) detailCell).elementAt(0);
		}
		if (table != null) {
			table.getUITablePane().getTable().clearSelection();
		}
		switch (((Integer) okey).intValue()) {
			case 1:
			case 109:
				table.setFocusCell(index, okey);
				break;

			default:
				super.setFocus(index, okey);
		}

	}

	public void stopCellEditing(Object cell) {
		Method setMethod = null;
		try {
			setMethod = cell.getClass().getMethod("stopEditing", new Class[0]);
		} catch (NoSuchMethodException e) {
		}
		if (setMethod != null) {
			try {
				setMethod.invoke(cell, new Object[0]);
			} catch (IllegalAccessException e) {
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getMessage());
			} catch (InvocationTargetException e) {
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getTargetException().getMessage());
			}
		}
	}

	protected void setCellEditable(Object cell, boolean value) {
		Method setMethod = null;
		try {
			setMethod = cell.getClass().getMethod("setEditable", new Class[] { Boolean.TYPE });
		} catch (NoSuchMethodException e) {
		}
		if (setMethod != null) {
			try {
				setMethod.invoke(cell, new Object[] { new Boolean(value) });
			} catch (IllegalAccessException e) {
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getMessage());
			} catch (InvocationTargetException e) {
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getTargetException().getMessage());
			}
		}
	}

	public boolean isEditing() {
		return super.isEditing();
	}

	public void startEditing() {
		super.startEditing();
	}

	public void stopEditing() {
		try {
			super.stopEditing();
		} catch (RuntimeException e1) {
			Logger.error(e1.getMessage(), e1);
		}
	}
}