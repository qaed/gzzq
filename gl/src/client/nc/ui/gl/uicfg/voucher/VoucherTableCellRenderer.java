package nc.ui.gl.uicfg.voucher;

import java.awt.Color;
import java.awt.Component;
import java.util.Hashtable;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import nc.bs.logging.Logger;
import nc.pubitf.bd.accessor.GeneralAccessorFactory;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.gl.vouchercard.IVoucherModel;
import nc.ui.gl.vouchertools.VoucherDataCenter;
import nc.ui.glcom.displayformattool.ShowContentCenter;
import nc.ui.glcom.numbertool.GlNumberFormat;
import nc.vo.bd.accessor.IBDData;
import nc.vo.bd.account.AccountVO;
import nc.vo.bd.balatype.BalaTypeVO;
import nc.vo.bd.currtype.CurrtypeVO;
import nc.vo.gateway60.itfs.Currency;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.glcom.ass.AssVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import org.apache.commons.lang.StringUtils;

public class VoucherTableCellRenderer extends DefaultTableCellRenderer {
	private Hashtable m_foreground = new Hashtable();

	private Hashtable m_background = new Hashtable();

	private Color m_tebleforegroundcolor;

	private boolean m_islinewrap = false;

	private JCheckBox checkboxComponent;

	private JTextArea textareaComponent;

	private int m_voucherkey;

	public VoucherTableCellRenderer() {
	}

	public void clear() {
		this.m_background.clear();
		this.m_foreground.clear();
	}

	public Color getBackground(int row) {
		Object color = this.m_background.get(Integer.valueOf(row));
		return color == null ? Color.WHITE : (Color) color;
	}

	private JCheckBox getCheckBoxComponent() {
		if (this.checkboxComponent == null)
			this.checkboxComponent = new JCheckBox();
		return this.checkboxComponent;
	}

	private Object getConvertedValue(JTable table, Object value, int row, int column) {
		Object r = null;
		switch (getVoucherKey()) {

			case 108: {
				if (value == null)
					return null;
				VoucherTableModel tm = (VoucherTableModel) table.getModel();
				IVoucherModel model = tm.getVoucherModel();
				AssVO[] ass = (AssVO[]) model.getValue(row, Integer.valueOf(303));

				String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));
				UFDate date = (UFDate) model.getValue(row, Integer.valueOf(17));
				String pk_accsubj = (String) model.getValue(row, Integer.valueOf(103));

				if (ass != null) {

					r = ShowContentCenter.getShowAss(pk_orgbook, pk_accsubj, date.toStdString(), ass);
				}
				break;
			}
			case 322:
				r = null;
				break;

			case 103:{
				if (value == null)
					return null;
				String accname = "";

				accname = ((AccountVO) value).getDispname();
				String acccode = ((AccountVO) value).getCode();
				if (accname != null) {

					r = acccode + " " + accname;
				}
				break;
			}
			case 302: {
				if (value == null)
					return null;
				String accname = "";

				accname = ((AccountVO) value).getDispname();
				if (accname != null) {

					r = accname;
				}
				break;
			}
//			20161223-tsy-修改自定义项目1：确认后，显示的值
			case 151: {
				if (value == null)
					return null;
				String accname = "";

				accname = ((AccountVO) value).getDispname();
				if (accname != null) {

					r = "[自定义： ]" + accname;
				}
				break;
			}
//			end
			case 301: {
				if (value == null)
					return null;
				String acccode = ((AccountVO) value).getCode();
				if (acccode != null) {

					r = acccode;
				}
				break;
			}
			case 104:
			case 304:
				if (value == null)
					return null;
				r = ((CurrtypeVO) value).getCode();
				break;

			case 305:
				if (value == null)
					return null;
				r = ((CurrtypeVO) value).getName();
				break;

			case 320:
				if (value == null)
					return null;
				r = ((CurrtypeVO) value).getCurrtypesign();
				break;

			case 110: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (dbValue.abs().compareTo(new UFDouble(1.0E-11D)) > 0) {
					VoucherTableModel tm = (VoucherTableModel) table.getModel();
					IVoucherModel model = tm.getVoucherModel();
					String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));
					int digit = VoucherDataCenter.getPricePrecision().intValue();
					dbValue = dbValue.setScale(digit, 4);
					r = GlNumberFormat.formatUFDouble(dbValue);
				}
				break;
			}
			case 111: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (dbValue.abs().compareTo(new UFDouble(1.0E-11D)) > 0) {
					VoucherTableModel tm = (VoucherTableModel) table.getModel();
					IVoucherModel model = tm.getVoucherModel();
					String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));
					if (VoucherDataCenter.isLocalFrac(pk_orgbook)) {
						String pk_curr = (String) model.getValue(row, Integer.valueOf(104));
						int digit = VoucherDataCenter.getCurrrateDigitByPk_orgbook(pk_orgbook, pk_curr, VoucherDataCenter.getFracCurrencyPK(pk_orgbook));
						dbValue = dbValue.setScale(digit, 4);
						r = GlNumberFormat.formatUFDouble(dbValue);
					}
				}
				break;
			}
			case 112: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (dbValue.abs().compareTo(new UFDouble(1.0E-11D)) > 0) {
					VoucherTableModel tm = (VoucherTableModel) table.getModel();
					IVoucherModel model = tm.getVoucherModel();
					String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));
					if (VoucherDataCenter.isLocalFrac(pk_orgbook)) {
						String pk_curr = VoucherDataCenter.getFracCurrencyPK(pk_orgbook);
						int digit = VoucherDataCenter.getCurrrateDigitByPk_orgbook(pk_orgbook, pk_curr, VoucherDataCenter.getMainCurrencyPK(pk_orgbook));
						dbValue = dbValue.setScale(digit, 4);
						r = GlNumberFormat.formatUFDouble(dbValue);
					} else {
						String pk_curr = (String) model.getValue(row, Integer.valueOf(104));
						int digit = VoucherDataCenter.getCurrrateDigitByPk_orgbook(pk_orgbook, pk_curr, VoucherDataCenter.getMainCurrencyPK(pk_orgbook));
						dbValue = dbValue.setScale(digit, 4);
						r = GlNumberFormat.formatUFDouble(dbValue);
					}
				}
				break;
			}
			case 113:
			case 117:
			case 311: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (dbValue.abs().compareTo(new UFDouble(1.0E-11D)) > 0) {
					VoucherTableModel tm = (VoucherTableModel) table.getModel();
					IVoucherModel model = tm.getVoucherModel();
					Integer precision = null;
					Object detailObj = model.getValue(row, Integer.valueOf(1));
					if ((detailObj != null) && ((detailObj instanceof DetailVO))) {
						DetailVO detailVo = (DetailVO) detailObj;
						String pk_unit = VoucherDataCenter.getMaterialUnit(detailVo.getAss());
						if (StringUtils.isNotEmpty(pk_unit)) {
							precision = VoucherDataCenter.getQuantityDigitByUnit(pk_unit);
						} else {
							String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));

							String pk_accasoa = (String) model.getValue(row, Integer.valueOf(103));
							UFDate date = (UFDate) model.getValue(row, Integer.valueOf(17));
							precision = VoucherDataCenter.getQuantityPrecision(pk_orgbook, pk_accasoa, date.toStdString());
						}
					}
					if (precision != null) {
						dbValue = dbValue.setScale(precision.intValue(), 4);
						r = GlNumberFormat.formatUFDouble(dbValue);
					}
				}
				break;
			}
			case 114:
			case 118:
			case 310: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (dbValue.abs().compareTo(new UFDouble(1.0E-11D)) > 0) {
					VoucherTableModel tm = (VoucherTableModel) table.getModel();
					IVoucherModel model = tm.getVoucherModel();
					String pk_curr = (String) model.getValue(row, Integer.valueOf(104));
					String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));
					if ((StringUtils.isNotEmpty(pk_curr)) && (StringUtils.isNotEmpty(pk_orgbook))) {
						CurrtypeVO vo = VoucherDataCenter.getCurrtypeByPk_orgbook(pk_orgbook, pk_curr);
						int digit = vo == null ? 2 : vo.getCurrdigit().intValue();
						dbValue = dbValue.setScale(digit, 4);
					}
					r = GlNumberFormat.formatUFDouble(dbValue);
				}
				break;
			}
			case 115:
			case 119:
			case 312: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (dbValue.abs().compareTo(new UFDouble(1.0E-11D)) > 0) {
					VoucherTableModel tm = (VoucherTableModel) table.getModel();
					IVoucherModel model = tm.getVoucherModel();
					String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));
					String pk_curr = VoucherDataCenter.getFracCurrencyPK(pk_orgbook);
					int digit = 0;
					if (pk_curr != null)
						digit = VoucherDataCenter.getCurrtypeByPk_orgbook(pk_orgbook, pk_curr).getCurrdigit().intValue();
					dbValue = dbValue.setScale(digit, 4);
					r = GlNumberFormat.formatUFDouble(dbValue);
				}
				break;
			}
			case 116:
			case 313: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (dbValue.abs().compareTo(new UFDouble(1.0E-11D)) > 0) {
					VoucherTableModel tm = (VoucherTableModel) table.getModel();
					IVoucherModel model = tm.getVoucherModel();
					String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));

					int digit = Currency.getCurrDigit(Currency.getMainCurrencyPK(pk_orgbook)).intValue();
					dbValue = dbValue.setScale(digit, 4);
					r = GlNumberFormat.formatUFDouble(dbValue);
				}
				break;
			}
			case 120: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (dbValue.abs().compareTo(new UFDouble(1.0E-11D)) > 0) {
					VoucherTableModel tm = (VoucherTableModel) table.getModel();
					IVoucherModel model = tm.getVoucherModel();
					String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));
					String pk_curr = VoucherDataCenter.getMainCurrencyPK(pk_orgbook);
					int digit = VoucherDataCenter.getCurrtypeByPk_orgbook(pk_orgbook, pk_curr).getCurrdigit().intValue();
					dbValue = dbValue.setScale(digit, 4);
					r = GlNumberFormat.formatUFDouble(dbValue);
				}
				break;
			}
			case 328: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (dbValue.abs().compareTo(new UFDouble(1.0E-11D)) > 0) {
					VoucherTableModel tm = (VoucherTableModel) table.getModel();
					IVoucherModel model = tm.getVoucherModel();
					String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));
					String pk_curr = (String) model.getValue(row, Integer.valueOf(104));
					Integer precision = Currency.getCurrratePrecisionByOrg(GlWorkBench.getLoginGroup(), pk_orgbook, pk_curr);

					dbValue = dbValue.setScale(precision.intValue(), 4);
					r = GlNumberFormat.formatUFDouble(dbValue);
				}
				break;
			}
			case 324:
			case 325: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (!dbValue.equals(UFDouble.ZERO_DBL)) {
					try {
						String pk_curr = Currency.getGroupCurrpk(GlWorkBench.getLoginGroup());

						int digit = Currency.getCurrDigit(pk_curr).intValue();
						dbValue = dbValue.setScale(digit, 4);
						r = GlNumberFormat.formatUFDouble(dbValue);
					} catch (BusinessException e) {
						Logger.error(e.getMessage(), e);
					}
				}
				break;
			}
			case 329: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (dbValue.abs().compareTo(new UFDouble(1.0E-11D)) > 0) {
					VoucherTableModel tm = (VoucherTableModel) table.getModel();
					IVoucherModel model = tm.getVoucherModel();
					String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));
					String pk_curr = (String) model.getValue(row, Integer.valueOf(104));
					Integer precision = Currency.getCurrratePrecisionByOrg("GLOBLE00000000000000", pk_orgbook, pk_curr);

					dbValue = dbValue.setScale(precision.intValue(), 4);
					r = GlNumberFormat.formatUFDouble(dbValue);
				}
				break;
			}
			case 326:
			case 327: {
				if (value == null)
					return null;
				UFDouble dbValue = (UFDouble) value;
				if (!dbValue.equals(UFDouble.ZERO_DBL)) {
					try {
						String pk_curr = Currency.getGlobalCurrPk(null);
						int digit = VoucherDataCenter.getCurrtypeByPk_orgbook(null, pk_curr).getCurrdigit().intValue();
						dbValue = dbValue.setScale(digit, 4);
						r = GlNumberFormat.formatUFDouble(dbValue);
					} catch (BusinessException e) {
						Logger.error(e.getMessage(), e);
					}
				}
				break;
			}
			case 126: {
				if (value == null)
					return null;
				VoucherTableModel tm = (VoucherTableModel) table.getModel();
				IVoucherModel model = tm.getVoucherModel();
				String pk_orgbook = (String) model.getValue(row, Integer.valueOf(55));
				BalaTypeVO balavo = VoucherDataCenter.getCheckstyleByPk_orgbook(pk_orgbook, value.toString().trim());
				if (balavo != null)
					r = balavo.getName();
				break;
			}

			case 133:
				if (value != null) {
					IGeneralAccessor billTyepAccessor = GeneralAccessorFactory.getAccessor("74c98540-4879-4584-a4c9-0f8b6e20b96a");
					IBDData billType = billTyepAccessor.getDocByPk(value.toString());
					if (billType == null) {
						r = null;
					} else
						r = billType.getName();
				}
				break;

			case 136:
				if (value != null) {
					IGeneralAccessor billTyepAccessor = GeneralAccessorFactory.getAccessor("ada13c7f-b854-46cf-95a3-9f59d121c0e7");
					IBDData billType = billTyepAccessor.getDocByPk(value.toString());
					if (billType == null) {
						r = null;
					} else
						r = billType.getName();
				}
				break;
			default:
				r = value;
		}

		return r;
	}

	private Color getDetaultForeground(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color foreground = Color.black;
		try {
			VoucherTableModel tm = (VoucherTableModel) table.getModel();
			IVoucherModel model = tm.getVoucherModel();
			UFBoolean discard = (UFBoolean) model.getValue(-1, Integer.valueOf(27));
			if ((discard != null) && (discard.booleanValue())) {
				foreground = Color.gray;
			} else {
				String errmsg = (String) model.getValue(row, Integer.valueOf(125));
				String errmsg2 = (String) model.getValue(row, Integer.valueOf(37));
				if ((errmsg2 != null) && (errmsg2.length() > 0) && (errmsg != null) && (errmsg.trim().length() > 0)) {
					foreground = Color.red;
				}
				String errmsg1 = (String) model.getValue(row, Integer.valueOf(403));
				if ((errmsg != null) && (errmsg.trim().length() > 0) && (errmsg1 != null) && (errmsg1.trim().length() > 0)) {
					foreground = Color.magenta;
				}
			}
		} catch (Exception e) {
		}

		return foreground;
	}

	public Color getForeground(int row) {
		Object color = this.m_foreground.get(Integer.valueOf(row));
		return color == null ? this.m_tebleforegroundcolor : (Color) color;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component component = null;
		if (isLineWrap()) {
			getTextAreaComponent().setLineWrap(true);
			if (isSelected) {
				getTextAreaComponent().setForeground(
						getForeground(row) == null ? getDetaultForeground(table, value, isSelected, hasFocus, row, column) : getForeground(row));

				getTextAreaComponent().setBackground(new Color(table.getSelectionBackground().getRGB() & getBackground(row).getRGB()));
			} else {
				getTextAreaComponent().setForeground(
						getForeground(row) == null ? getDetaultForeground(table, value, isSelected, hasFocus, row, column) : getForeground(row));

				getTextAreaComponent().setBackground(getBackground(row));
			}

			getTextAreaComponent().setFont(table.getFont());

			if (hasFocus) {
				getTextAreaComponent().setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				if (table.isCellEditable(row, column)) {
					getTextAreaComponent().setForeground(UIManager.getColor("Table.focusCellForeground"));
					getTextAreaComponent().setBackground(UIManager.getColor("Table.focusCellBackground"));
				}
			} else {
				getTextAreaComponent().setBorder(noFocusBorder);
			}

			Object vv = getConvertedValue(table, value, row, column);
			getTextAreaComponent().setText(vv == null ? "" : vv.toString().trim());
			getTextAreaComponent().setToolTipText(ShowContentCenter.changeAssShowWithHtml(vv == null ? "" : vv.toString().trim()));

			component = getTextAreaComponent();
		} else {
			if (((value instanceof Boolean)) || ((value instanceof UFBoolean))) {
				component = getCheckBoxComponent();
				((JCheckBox) component).setSelected(((Boolean) value).booleanValue());
				((JCheckBox) component).setHorizontalAlignment(0);
				if (hasFocus) {
					((JCheckBox) component).setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				} else {
					((JCheckBox) component).setBorder(noFocusBorder);
				}
			} else {
				setValue(getConvertedValue(table, value, row, column));
				component = this;
			}

			if (isSelected) {
				component
						.setForeground(getForeground(row) == null ? getDetaultForeground(table, value, isSelected, hasFocus, row, column) : getForeground(row));

				component.setBackground(new Color(table.getSelectionBackground().getRGB() & getBackground(row).getRGB()));
			} else {
				component
						.setForeground(getForeground(row) == null ? getDetaultForeground(table, value, isSelected, hasFocus, row, column) : getForeground(row));

				component.setBackground(getBackground(row));
			}

			setFont(table.getFont());

			if (hasFocus) {
				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				if (table.isCellEditable(row, column)) {
					component.setForeground(UIManager.getColor("Table.focusCellForeground"));
					component.setBackground(UIManager.getColor("Table.focusCellBackground"));
				}
			} else {
				setBorder(noFocusBorder);
			}
		}
		return component;
	}

	private JTextArea getTextAreaComponent() {
		if (this.textareaComponent == null)
			this.textareaComponent = new JTextArea();
		return this.textareaComponent;
	}

	public int getVoucherKey() {
		return this.m_voucherkey;
	}

	public boolean isLineWrap() {
		return this.m_islinewrap;
	}

	public void setBackground(Color c, int row) {
		this.m_background.put(Integer.valueOf(row), c);
	}

	public void setForeground(Color c, int row) {
		this.m_foreground.put(Integer.valueOf(row), c);
	}

	public void setLineWrap(boolean newM_islinewrap) {
		this.m_islinewrap = newM_islinewrap;
	}

	public void setVoucherKey(int newM_voucherkey) {
		this.m_voucherkey = newM_voucherkey;
	}
}