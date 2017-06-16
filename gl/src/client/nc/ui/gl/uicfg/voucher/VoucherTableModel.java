package nc.ui.gl.uicfg.voucher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import nc.bs.logging.Logger;
import nc.pubitf.bd.accessor.GeneralAccessorFactory;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.ui.format.NCFormater;
import nc.ui.gl.datacache.FreeValueDataCache;
import nc.ui.gl.glref.GLBUVersionWithBookRefModel;
import nc.ui.gl.vouchercard.IVoucherModel;
import nc.ui.gl.vouchertools.VoucherDataCenter;
import nc.ui.pub.beans.UIRefPane;
import nc.vo.bd.accessor.IBDData;
import nc.vo.bd.account.AccountVO;
import nc.vo.bd.currtype.CurrtypeVO;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.VoucherModflagTool;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.gl.voucher.ColumnmodeVO;
import nc.vo.glcom.ass.AssVO;
import nc.vo.pub.format.FormatResult;
import nc.vo.pub.format.exception.FormatException;
import nc.vo.pub.lang.ICalendar;
import nc.vo.pub.lang.MultiLangText;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import org.apache.commons.lang.StringUtils;

public class VoucherTableModel extends AbstractTableModel {
	protected ColumnmodeVO[] m_columnmode = null;

	boolean istableeditable = true;

	IVoucherModel m_vouchermodel = null;

	private UIRefPane buRef = null;

	public VoucherTableModel() {
	}

	public boolean editCell(int row, int column) {
		DetailVO detail = (DetailVO) getVoucherModel().getValue(row, Integer.valueOf(1));
		if ((detail != null) && (detail.getIsmatched() != null) && (detail.getIsmatched().booleanValue()) && (getColumnKey(column) != 109))
			return false;
		String strModifyFlag = (String) getVoucherModel().getValue(row, Integer.valueOf(121));
		if (StringUtils.isEmpty(strModifyFlag))
			return true;
		int iKey = getColumnKey(column);

		return VoucherModflagTool.getFieldEditable(strModifyFlag, iKey);
	}

	public int getColumnCount() {
		return getColumnModes() == null ? 0 : getColumnModes().length;
	}

	public int getColumnIndex(int iKey) {
		int index = -1;
		for (int i = 0; i < getColumnCount(); i++) {
			if (iKey == getColumnKey(i)) {
				index = i;
				break;
			}
		}
		return index;
	}

	public int getColumnKey(int col) {
		return getColumnModes()[col].getColumnkey().intValue();
	}

	public ColumnmodeVO[] getColumnModes() {
		return this.m_columnmode;
	}

	public String getColumnName(int col) {
		return getColumnModes()[col].getColumnname();
	}

	public int getRowCount() {
		if (getVoucherModel() == null)
			return 0;
		VoucherVO voucher = (VoucherVO) getVoucherModel().getValue(-1, Integer.valueOf(0));
		return voucher == null ? 0 : voucher.getNumDetails();
	}

	public Object getValueAt(int row, int col) {
		if (row == 2) {
			row = 2;
		}
		if ((row < 0) || (col < 0))
			return null;
		UFDate date = null;
		switch (getColumnKey(col)) {
			case 103:
			case 301:
			case 302:
				date = (UFDate) getVoucherModel().getValue(-1, Integer.valueOf(17));
				return VoucherDataCenter.getAccsubjByPK((String) getVoucherModel().getValue(-1, Integer.valueOf(55)),
						(String) getVoucherModel().getValue(row, Integer.valueOf(103)), date.toStdString());
//				20161223-tsy-修改自定义项目1：获取当前格子对应的VO对象
			case 151:
				date = (UFDate) getVoucherModel().getValue(-1, Integer.valueOf(17));
				return VoucherDataCenter.getAccsubjByPK((String) getVoucherModel().getValue(-1, Integer.valueOf(55)),
						(String) getVoucherModel().getValue(row, Integer.valueOf(151)), date.toStdString());
//				end

			case 104:
			case 304:
			case 305:
			case 320:
				return VoucherDataCenter.getCurrtypeByPk_orgbook((String) getVoucherModel().getValue(row, Integer.valueOf(55)), (String) getVoucherModel()
						.getValue(row, Integer.valueOf(104)));

			case 322:
				return getVoucherModel().getValue(row, Integer.valueOf(1));

			case 606:
				date = (UFDate) getVoucherModel().getValue(row, Integer.valueOf(17));

				((GLBUVersionWithBookRefModel) getBuRef().getRefModel()).setPk_accountingbook((String) getVoucherModel().getValue(row, Integer.valueOf(55)));
				((GLBUVersionWithBookRefModel) getBuRef().getRefModel()).setVstartdate(date.asEnd());
				Object pk_unit = getVoucherModel().getValue(row, Integer.valueOf(608));
				getBuRef().setPK(pk_unit);

				String refName = getBuRef().getRefName();
				if (StringUtils.isEmpty(refName)) {
					try {
						IBDData docByPk = GeneralAccessorFactory.getAccessor("31736d3d-3626-46c5-b5d0-e10b4ed33dce").getDocByPk((String) pk_unit);
						if (docByPk != null)
							refName = docByPk.getName().toString();
					} catch (Exception e) {
						Logger.error(e.getMessage(), e);
					}
				}

				return refName;

			case 132:
				Object verifydate = getVoucherModel().getValue(row, Integer.valueOf(132));
				try {
					return verifydate == null ? null : NCFormater.formatDate(
							new UFDate(new UFDateTime(verifydate.toString(), ICalendar.BASE_TIMEZONE).getMillis())).getValue();
				} catch (FormatException e) {
					Logger.error(e.getMessage(), e);
					return new UFDate(verifydate.toString()).toStdString();
				}
		}

		return getVoucherModel().getValue(row, Integer.valueOf(getColumnKey(col)));
	}

	public IVoucherModel getVoucherModel() {
		return this.m_vouchermodel;
	}

	public boolean isCellEditable(int row, int column) {
		return (this.istableeditable) && (getColumnModes()[column].getColumneditable().booleanValue()) && (editCell(row, column));
	}

	public boolean isEditable() {
		return this.istableeditable;
	}

	public void setColumnKey(int iKey, int col) {
		getColumnModes()[col].setColumnkey(Integer.valueOf(iKey));
	}

	public void setColumnModes(ColumnmodeVO[] newM_columnmode) {
		this.m_columnmode = newM_columnmode;
	}

	public void setColumnName(String name, int col) {
		getColumnModes()[col].setColumnname(name);
	}

	public void setEditable(boolean b) {
		this.istableeditable = b;
	}

	public void setValueAt(Object value, int row, int col) {
		switch (getColumnKey(col)) {
			case 103:
			case 301:
			case 302:
				if ((value instanceof AccountVO)) {
					AccountVO accvo = (AccountVO) value;
					if (accvo != null) {
						getVoucherModel().setValue(row, Integer.valueOf(301), accvo.getCode());
						getVoucherModel().setValue(row, Integer.valueOf(302), accvo.getName());
						getVoucherModel().setValue(row, Integer.valueOf(103), accvo.getPk_accasoa());
					} else {
						getVoucherModel().setValue(row, Integer.valueOf(301), null);
						getVoucherModel().setValue(row, Integer.valueOf(302), null);
						getVoucherModel().setValue(row, Integer.valueOf(103), null);
					}

				} else {
					AccountVO[] accvo = (AccountVO[]) value;

					if (accvo != null) {
						addDetails(accvo, row);
					} else {
						getVoucherModel().setValue(row, Integer.valueOf(301), null);
						getVoucherModel().setValue(row, Integer.valueOf(302), null);
						getVoucherModel().setValue(row, Integer.valueOf(103), null);
					}
				}

				break;
//				20161223-tsy-修改自定义项目1：设置值
			case 151:
				if ((value instanceof AccountVO)) {
					AccountVO accvo = (AccountVO) value;
					if (accvo != null) {
						getVoucherModel().setValue(row, Integer.valueOf(151), accvo.getPk_accasoa());
					} else {
						getVoucherModel().setValue(row, Integer.valueOf(151), null);
					}

				} else {
					AccountVO[] accvo = (AccountVO[]) value;

					if (accvo != null) {//选择的是多个科目
						//addDetails(accvo, row);//不支持选择多个科目
					} else {//
						getVoucherModel().setValue(row, Integer.valueOf(151), null);
					}
				}

				break;
//			end

			case 108:
				if ((value instanceof String[])) {
					if ((value != null) && (((String[]) value).length > 1)) {
						addDetailsForAssids((String[]) value, row);
					} else {
						getVoucherModel().setValue(row, Integer.valueOf(getColumnKey(col)), value);
					}
				} else {
					getVoucherModel().setValue(row, Integer.valueOf(getColumnKey(col)), value);
				}
				break;

			case 104:
			case 304:
			case 305:
			case 320:
				CurrtypeVO currvo = (CurrtypeVO) value;
				if (currvo != null) {
					getVoucherModel().setValue(row, Integer.valueOf(304), currvo.getCode());
					getVoucherModel().setValue(row, Integer.valueOf(305), currvo.getName());
					getVoucherModel().setValue(row, Integer.valueOf(320), currvo.getCurrtypesign());
					getVoucherModel().setValue(row, Integer.valueOf(104), currvo.getPk_currtype());
				} else {
					getVoucherModel().setValue(row, Integer.valueOf(304), null);
					getVoucherModel().setValue(row, Integer.valueOf(305), null);
					getVoucherModel().setValue(row, Integer.valueOf(320), null);
					getVoucherModel().setValue(row, Integer.valueOf(104), null);
				}
				break;

			case 322:
				getVoucherModel().setValue(row, Integer.valueOf(1), value);
				break;

			case 606:
			case 607:
			case 608:
				if ((value instanceof HashMap)) {

					HashMap orgMap = (HashMap) value;
					getVoucherModel().setValue(row, Integer.valueOf(608), orgMap.get("pk_vid"));
					getVoucherModel().setValue(row, Integer.valueOf(607), orgMap.get("pk_org"));
				}

				break;
			default:
				getVoucherModel().setValue(row, Integer.valueOf(getColumnKey(col)), value);
		}
	}
	/**
	 * <p>添加多条（科目）分录</p>
	 * @param accvo
	 * @param row 起始行
	 */ 
	private void addDetails(AccountVO[] accvo, int row) {
		DetailVO detail = (DetailVO) getVoucherModel().getValue(row, Integer.valueOf(1));
		HashMap<Integer, DetailVO> rowhm = new HashMap();
		VoucherVO voucher = (VoucherVO) getVoucherModel().getParameter("vouchervo");

		int rowcount = getRowCount() - 1;
		for (int i = rowcount - 1; i > row; i--) {
			rowhm.put(Integer.valueOf(i), (DetailVO) getVoucherModel().getValue(i, Integer.valueOf(1)));
			voucher.getDetail().remove(i);
		}
		for (int i = 0; i < accvo.length; i++) {
			if (i != 0) {
				DetailVO de = (DetailVO) detail.clone();
				de.setDetailindex(Integer.valueOf(row + i + 1));
				if (de.getPk_detail() != null) {
					de.setPk_detail(null);
				}
				de.setLocalcreditamount(new UFDouble(0));
				de.setLocaldebitamount(new UFDouble(0));
				de.setFraccreditamount(new UFDouble(0));
				de.setFracdebitamount(new UFDouble(0));
				de.setDebitamount(new UFDouble(0));
				de.setCreditamount(new UFDouble(0));
				de.setCreditquantity(new UFDouble(0));
				de.setDebitquantity(new UFDouble(0));
				de.setAss(null);
				de.setAssid(null);
				voucher.addDetail(de);
			}
			getVoucherModel().setValue(row + i, Integer.valueOf(301), accvo[i].getCode());
			getVoucherModel().setValue(row + i, Integer.valueOf(302), accvo[i].getName());
			getVoucherModel().setValue(row + i, Integer.valueOf(103), accvo[i].getPk_accasoa());

			if (i == 0) {
				voucher.clearEmptyDetail();
			}
		}
		int otherrow = rowhm.size();
		for (int i = 0; i < otherrow; i++) {
			row += 1;
			DetailVO de = (DetailVO) rowhm.get(Integer.valueOf(row));
			de.setDetailindex(Integer.valueOf(row + accvo.length));
			voucher.addDetail(de);
		}
		voucher.addDetail(new DetailVO());
	}

	private void addDetailsForAssids(String[] ids, int row) {
		DetailVO detail = (DetailVO) getVoucherModel().getValue(row, Integer.valueOf(1));
		HashMap<Integer, DetailVO> rowhm = new HashMap();
		VoucherVO voucher = (VoucherVO) getVoucherModel().getParameter("vouchervo");

		int rowcount = getRowCount() - 1;
		for (int i = rowcount - 1; i > row; i--) {
			rowhm.put(Integer.valueOf(i), (DetailVO) getVoucherModel().getValue(i, Integer.valueOf(1)));
			voucher.getDetail().remove(i);
		}
		for (int i = 0; i < ids.length; i++) {
			if (i != 0) {
				DetailVO de = (DetailVO) detail.clone();
				de.setDetailindex(Integer.valueOf(row + i + 1));
				if (de.getPk_detail() != null) {
					de.setPk_detail(null);
				}
				de.setLocalcreditamount(new UFDouble(0));
				de.setLocaldebitamount(new UFDouble(0));
				de.setFraccreditamount(new UFDouble(0));
				de.setFracdebitamount(new UFDouble(0));
				de.setDebitamount(new UFDouble(0));
				de.setCreditamount(new UFDouble(0));
				de.setCreditquantity(new UFDouble(0));
				de.setDebitquantity(new UFDouble(0));
				de.setGroupdebitamount(UFDouble.ZERO_DBL);
				de.setGroupcreditamount(UFDouble.ZERO_DBL);
				de.setGlobaldebitamount(UFDouble.ZERO_DBL);
				de.setGlobalcreditamount(UFDouble.ZERO_DBL);

				voucher.addDetail(de);
			}
			getVoucherModel().setValue(row + i, Integer.valueOf(108), ids[i]);
			List<AssVO> rtList = new ArrayList();
			AssVO[] popUpAssVOs = FreeValueDataCache.getInstance().getAssvosByID(ids[i]);
			if ((popUpAssVOs != null) && (popUpAssVOs.length > 0)) {
				for (AssVO assVo : popUpAssVOs) {
					rtList.add((AssVO) assVo.clone());
				}
			}
			getVoucherModel().setValue(row + i, Integer.valueOf(303), rtList.toArray(new AssVO[0]));

			if (i == 0) {
				voucher.clearEmptyDetail();
			}
		}
		int otherrow = rowhm.size();
		for (int i = 0; i < otherrow; i++) {
			row += 1;
			DetailVO de = (DetailVO) rowhm.get(Integer.valueOf(row));
			de.setDetailindex(Integer.valueOf(row + ids.length));
			voucher.addDetail(de);
		}
		voucher.addDetail(new DetailVO());
	}

	public void setVoucherModel(IVoucherModel newM_vouchermodel) {
		this.m_vouchermodel = newM_vouchermodel;
	}

	public void setBuRef(UIRefPane buRef) {
		this.buRef = buRef;
	}

	private UIRefPane getBuRef() {
		if (this.buRef == null) {
			this.buRef = new UIRefPane();
			this.buRef.setRefNodeName("总账业务单元版本");
			this.buRef.setDataPowerOperation_code("fi");
		}
		return this.buRef;
	}
}