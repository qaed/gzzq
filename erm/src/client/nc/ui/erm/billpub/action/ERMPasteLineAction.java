package nc.ui.erm.billpub.action;

import java.awt.event.ActionEvent;
import nc.ui.er.util.BXUiUtil;
import nc.ui.erm.billpub.model.ErmBillBillManageModel;
import nc.ui.erm.billpub.view.ErmBillBillForm;
import nc.ui.erm.billpub.view.eventhandler.EventHandleUtil;
import nc.ui.erm.billpub.view.eventhandler.InitBodyEventHandle;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pub.bill.BillCellEditor;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillItem;
import nc.ui.pub.bill.BillModel;
import nc.ui.pub.bill.BillScrollPane;
import nc.ui.pub.bill.BillScrollPane.BillTable;
import nc.ui.uif2.UIState;
import nc.ui.uif2.actions.PasteLineAction;
import nc.ui.uif2.editor.BillForm;
import nc.ui.uif2.model.AbstractAppModel;

public class ERMPasteLineAction extends PasteLineAction {
	private static final long serialVersionUID = 1L;

	public ERMPasteLineAction() {
	}

	protected boolean isActionEnable() {
		String tradeType = null;
		if ((getModel() instanceof ErmBillBillManageModel)) {
			ErmBillBillManageModel model = (ErmBillBillManageModel) getModel();
			tradeType = model.getSelectBillTypeCode();
			if ("2647".equals(tradeType)) {
				return false;
			}
		}

		BillItem headItem = getCardpanel().getBillCardPanel().getHeadItem(
				"pk_item");
		Object mtAppPk = null;
		if (headItem != null) {
			mtAppPk = headItem.getValueObject();
		}

		boolean isBX = tradeType != null ? tradeType.startsWith("264") : false;
		return ((getModel().getUiState() == UIState.ADD) || (getModel()
				.getUiState() == UIState.EDIT))
				&& ((mtAppPk == null) || (isBX));
	}

	public void doAction(ActionEvent e) throws Exception {
		BillCardPanel billCardPanel = getCardpanel().getBillCardPanel();

		billCardPanel.stopEditing();

		BillScrollPane bsp = billCardPanel.getBodyPanel();

		int rownum = bsp.getTable().getSelectedRow();
		if (rownum < 0) {
			return;
		}
		super.doAction(e);

		Object pk_item = getCardpanel().getBillCardPanel()
				.getHeadItem("pk_item").getValueObject();
		Object srcbilltype = getCardpanel().getBillCardPanel()
				.getHeadItem("srcbilltype").getValueObject();
		Object srctype = getCardpanel().getBillCardPanel()
				.getHeadItem("srctype").getValueObject();

		int pasteLineCont = bsp.getTableModel().getPasteLineNumer();
		for (int i = 0; i < pasteLineCont; i++) {
			if (billCardPanel.getCurrentBodyTableCode().equals(
					"er_cshare_detail")) {

				Boolean ismashare = Boolean
						.valueOf(getCardpanel().getBillCardPanel().getHeadItem(
								"ismashare") == null ? false
								: ((Boolean) getCardpanel().getBillCardPanel()
										.getHeadItem("ismashare")
										.getValueObject()).booleanValue());

				if (ismashare.booleanValue()) {
					getCardpanel().getBillCardPanel().setBodyValueAt(pk_item,
							rownum + i, "pk_item");
				}

				billCardPanel.setBodyValueAt(null, rownum + i,
						"pk_cshare_detail");
			} else {
				getCardpanel().getBillCardPanel().setBodyValueAt(pk_item,
						rownum + i, "pk_item");
				getCardpanel().getBillCardPanel().setBodyValueAt(srcbilltype,
						rownum + i, "srcbilltype");
				getCardpanel().getBillCardPanel().setBodyValueAt(srctype,
						rownum + i, "srctype");

				billCardPanel.setBodyValueAt(null, rownum + i, "pk_busitem");
				billCardPanel.setBodyValueAt(null, rownum + i, "rowno");
				billCardPanel.setBodyValueAt(null, pasteLineCont, "rowno");
			}
		}
		
		((ErmBillBillForm) getCardpanel()).getbodyEventHandle()
				.resetJeAfterModifyRow();
		((ErmBillBillForm) getCardpanel()).getbodyEventHandle().finBodyYbjeEdit();

		BillModel contrastBillModel = getCardpanel().getBillCardPanel()
				.getBillModel("er_bxcontrast");
		if (contrastBillModel != null) {
			int rows = contrastBillModel.getRowCount();
			if (rows > 0) {
				BXUiUtil.doContract((ErmBillBillForm) getCardpanel());
			}
		}

		
		
	}
}