package nc.ui.gl.uicfg.voucher;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.IRefDocEdit;
import nc.ui.bd.ref.IRefMaintenanceHandler;
import nc.ui.gl.accsubjref.AccsubjRefPane;
import nc.ui.gl.ref.CheckStlyeRef;
import nc.ui.gl.voucher.ref.DetailAssistantRefPane;
import nc.ui.gl.voucher.ref.QuantityAmountRefPane;
import nc.ui.glcom.control.CurrencyComboBox;
import nc.ui.glcom.tableassrefeditor.AssRefTableEditorRenderer;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.FocusUtils;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UITextField;
import nc.vo.gateway60.pub.DefVO;
import nc.vo.gateway60.pub.GLFreeItemVO;
import nc.vo.glcom.constant.VoucherInputMaxLenConst;

public class TableCellEditorCreator {
	public TableCellEditorCreator() {
	}

	public static TableCellEditor createCellEditorByDataType(GLFreeItemVO datatype) {
		if (datatype == null)
			return null;
		TableCellEditor celleditor = null;
		if (datatype.getDetail().getType().equals(NCLangRes.getInstance().getStrByID("2002100557", "UPP2002100557-000064"))) {
			UITextField text = new UITextField();
			text.setTextType("TextDbl");
			text.setNumPoint(datatype.getDetail().getDigitnum() == null ? 0 : datatype.getDetail().getDigitnum().intValue());
			text.setMaxLength(datatype.getDetail().getLengthnum() == null ? 16 : datatype.getDetail().getLengthnum().intValue());
			celleditor = new VoucherTableCellEditor(text);
		} else if (datatype.getDetail().getType().equals(NCLangRes.getInstance().getStrByID("common", "UC000-0002313"))) {
			UIRefPane ref = new UIRefPane();
			ref.setRefNodeName("日历");
			celleditor = new VoucherTableCellEditor(ref);
		} else if (datatype.getDetail().getType().equals(NCLangRes.getInstance().getStrByID("common", "UC000-0001376"))) {
			UITextField text = new UITextField();
			text.setMaxLength(datatype.getDetail().getLengthnum() == null ? 20 : datatype.getDetail().getLengthnum().intValue());
			celleditor = new VoucherTableCellEditor(text);
		}
		return celleditor;
	}
	/**
	 * <p></p>
	 * @param iKey
	 * @return
	 */
	static TableCellEditor createDefaultCellEditor(int iKey) {
		TableCellEditor celleditor = null;
		UITextField tf = null;
		switch (iKey) {

			case 103:
				celleditor = new VoucherTableCellEditor(getAccsubjRef());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 301:
				celleditor = new VoucherTableCellEditor(getAccsubjRef());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 302:
				celleditor = new VoucherTableCellEditor(getAccsubjRef());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 606:
				celleditor = new VoucherTableCellEditor(getBusiUnitRef());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 304:
				celleditor = new VoucherTableCellEditor(getCurrencyRef());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 305:
				celleditor = new VoucherTableCellEditor(getCurrencyRef());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 108:
				celleditor = new VoucherTableCellEditor(getAssRef());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 322:
				celleditor = new VoucherTableCellEditor(new DetailAssistantRefPane());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 109:
				celleditor = new VoucherTableCellEditor(getExplanationRef());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;
//				20161223-tsy-修改自定义项目1:设置参照类型为科目参照
			case 151:
				celleditor = new VoucherTableCellEditor(getAccsubjRef());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;
//				end
			case 110:
			case 111:
			case 112:
			case 113:
			case 114:
			case 115:
			case 116:
			case 117:
			case 118:
			case 119:
			case 120:
			case 310:
			case 311:
			case 312:
			case 313:
			case 324:
			case 325:
			case 326:
			case 327:
				QuantityAmountRefPane celleditorcomp = getAmountRef();
				celleditorcomp.setVoucherKey(iKey);

				celleditor = new VoucherTableCellEditor(celleditorcomp);
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 128:
			case 132:
			case 135:
				celleditor = new VoucherTableCellEditor(getDateRef());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 126:
			case 306:
				celleditor = new VoucherTableCellEditor(getCheckStyleRef());
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 133:
				UIRefPane billref = getRefBillType();
				if (billref != null)
					celleditor = new VoucherTableCellEditor(billref);
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 136:
				UIRefPane bankaccountRef = getRefBankAccount();
				if (bankaccountRef != null)
					celleditor = new VoucherTableCellEditor(bankaccountRef);
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 152:
			case 153:
			case 154:
			case 155:
			case 156:
			case 157:
			case 158:
			case 159:
			case 160:
			case 161:
			case 162:
			case 163:
			case 164:
			case 165:
			case 166:
			case 167:
			case 168:
			case 169:
			case 170:
				tf = new UITextField();
				tf.setMaxLength(40);
				celleditor = new VoucherTableCellEditor(tf);
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 171:
			case 172:
			case 173:
			case 174:
			case 175:
				tf = new UITextField();
				tf.setMaxLength(200);
				celleditor = new VoucherTableCellEditor(tf);
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			case 176:
			case 177:
			case 178:
			case 179:
			case 180:
				tf = new UITextField();
				tf.setMaxLength(500);
				celleditor = new VoucherTableCellEditor(tf);
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
				break;

			default:
				tf = new UITextField();
				tf.setMaxLength(500);
				celleditor = new VoucherTableCellEditor(tf);
				((VoucherTableCellEditor) celleditor).setVoucherKey(iKey);
		}

		return celleditor;
	}

	private static UIRefPane getBusiUnitRef() {
		UIRefPane bupanel = new UIRefPane();

		bupanel.setRefNodeName("总账业务单元版本");
		bupanel.setDataPowerOperation_code("fi");
		return bupanel;
	}

	private static AccsubjRefPane getAccsubjRef() {
		AccsubjRefPane ivjAccsubjRefEditor = new AccsubjRefPane();
		return ivjAccsubjRefEditor;
		// if(null == accsubjRefPane)
		// accsubjRefPane = new AccsubjRefPane();
		// AccsubjRefPane ivjAccsubjRefEditor = accsubjRefPane;
	}

	private static QuantityAmountRefPane getAmountRef() {
		QuantityAmountRefPane m_AmountRefPanel1 = new QuantityAmountRefPane();

		return m_AmountRefPanel1;
	}

	private static AssRefTableEditorRenderer getAssRef() {
		AssRefTableEditorRenderer m_AssRefTableEditorRenderer1 = new AssRefTableEditorRenderer();
		m_AssRefTableEditorRenderer1.setNoshowcheckandcode(true);
		m_AssRefTableEditorRenderer1.setFreevalueMultiSelected(true);
		return m_AssRefTableEditorRenderer1;
	}

	private static CheckStlyeRef getCheckStyleRef() {
		CheckStlyeRef ivjAccsubjRefEditor = new CheckStlyeRef();
		ivjAccsubjRefEditor.setRefNodeName("结算方式");
		return ivjAccsubjRefEditor;
	}

	private static CurrencyComboBox getCurrencyRef() {
		CurrencyComboBox m_CCurrency = new CurrencyComboBox();
		m_CCurrency.refresh();
		return m_CCurrency;
	}

	private static UIRefPane getDateRef() {
		UIRefPane m_RefExplanation = new UIRefPane();
		m_RefExplanation.setRefNodeName("日历");
		return m_RefExplanation;
	}

	private static UIRefPane getExplanationRef() {
		UIRefPane m_RefExplanation = new UIRefPane();
		m_RefExplanation.setRefNodeName("常用摘要");
		m_RefExplanation.getRefModel().setRefMaintenanceHandler(new IRefMaintenanceHandler() {
			public String[] getFucCodes() {
				return new String[] { "200006", "200008", "200010" };
			}

			public IRefDocEdit getRefDocEdit() {
				return null;
			}

		});
		m_RefExplanation.setReturnCode(false);
		m_RefExplanation.setRefInputType(1);
		m_RefExplanation.setAutoCheck(false);
		m_RefExplanation.setMaxLength(VoucherInputMaxLenConst.EXPLANATION.intValue());

		InputMap map = m_RefExplanation.getUITextField().getInputMap(0);
		ActionMap am = m_RefExplanation.getUITextField().getActionMap();
		String key = "transFocus";
		map.remove(KeyStroke.getKeyStroke(10, 0, true));
		map.put(KeyStroke.getKeyStroke(10, 0, false), key);
		am.put(key, new ShortCutKeyAction(10));

		return m_RefExplanation;
	}

	static class ShortCutKeyAction extends AbstractAction {
		int keycode = -1;

		static final int VK_ENTER = 10;

		static final String KEY_FOCUS_TRANSFER = "transFocus";

		public ShortCutKeyAction(int keycode) {
			this.keycode = keycode;
		}

		public void actionPerformed(ActionEvent e) {
			if ((e.getSource() instanceof UITextField)) {
				UITextField c = (UITextField) e.getSource();
				if ((c.isEnabled()) && (c.isEditable())) {
					switch (this.keycode) {
						case 10:
							focusNextComponent(c);
					}
				}
			}
		}

		private void focusNextComponent(Component c) {
			Component parent = c.getParent();
			if (((parent instanceof JTable)) || (((parent instanceof UIRefPane)) && ((parent.getParent() instanceof JTable)))) {
				return;
			}
			FocusUtils.focusNextComponent(c);
		}
	}

	private static UIRefPane getRefBankAccount() {
		UIRefPane refpane = new UIRefPane();
		refpane.setRefNodeName("银行账户子户");
		refpane.getRefModel().setDataPowerOperation_code("fi");
		return refpane;
	}

	private static UIRefPane getRefBillType() {
		UIRefPane refpane = new UIRefPane();
		refpane.setRefNodeName("票据类型");

		return refpane;
	}
}
