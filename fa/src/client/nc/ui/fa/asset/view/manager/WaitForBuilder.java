package nc.ui.fa.asset.view.manager;

import java.util.HashMap;
import java.util.Map;
import nc.pub.fa.card.CardTabConst;
import nc.ui.am.model.BillManageModel;
import nc.ui.fa.asset.event.WaitDataChangeEvent;
import nc.ui.fa.asset.manager.AssetCodeManager;
import nc.ui.fa.asset.manager.AssetDataModel;
import nc.ui.fa.asset.manager.WaitForBuildDataManage;
import nc.ui.fa.asset.relative.RelativeClientTool;
import nc.ui.fa.asset.view.AssetBillForm;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pub.bill.BillData;
import nc.ui.pub.bill.BillItem;
import nc.ui.pub.bill.BillModel;
import nc.ui.pub.bill.BillTabbedPane;
import nc.ui.uif2.AppEvent;
import nc.ui.uif2.AppEventListener;
import nc.ui.uif2.UIState;
import nc.ui.uif2.model.AbstractAppModel;
import nc.vo.am.common.util.ArrayUtils;
import nc.vo.am.common.util.StringUtils;
import nc.vo.fa.asset.AggAssetVO;
import nc.vo.fa.asset.AssetVO;
import nc.vo.logging.Debug;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.uif2.LoginContext;

public class WaitForBuilder implements AppEventListener {
	private AssetBillForm billForm;
	private WaitForBuildDataManage dataManager;

	public WaitForBuilder() {
	}

	private void setPreBillCode() {
		try {
			AssetCodeManager.setPreBillCode(getBillForm(), (BillManageModel) getBillForm().getModel());
		} catch (BusinessException e) {
			Debug.error(e);
			getBillForm().showHintMessage(e.getMessage());
		}
	}

	private void synchronizeBillFormData() {
		BillItem[] items = getBillForm().getBillCardPanel().getHeadItems();
		if (ArrayUtils.isNotEmpty(items)) {
			for (BillItem item : items) {
				if (!item.isIsDef()) {

					if ((item.getDataType() == 18) || (item.getDataType() == 2)) {
						item.setDecimalDigits(8);
					}
				}
			}
		}
		getBillForm().setValue(this.dataManager.getSelectedWaitForData());

		setDefaultBillItemEdit();
	}

	private void setDefaultBillItemEdit() {
		getBillForm().getBillCardPanel().getHeadItem("card_code").setEdit(false);

		getBillForm().getBillCardPanel().setHeadItem("pk_card", null);
		getBillForm().getBillCardPanel().setHeadItem("pk_cardhistory", null);
		getBillForm().getBillCardPanel().setHeadItem("pk_cardsub", null);
	}

	private void fillDefualtValueToAggVO() {
		AssetVO vo = (AssetVO) this.dataManager.getSelectedWaitForData().getParentVO();
		String card_code = getBillForm().getHeadItemStringValue("card_code");

		String setCardCode = vo.getCard_code();

		if ((StringUtils.isEmpty(card_code)) && (StringUtils.isEmpty(setCardCode))) {
			setPreBillCode();
			card_code = getBillForm().getHeadItemStringValue("card_code");
		} else if ((StringUtils.isEmpty(card_code)) && (StringUtils.isNotEmpty(setCardCode))) {
			card_code = setCardCode;
		}
		card_code = "cym";
		vo.setCard_code(card_code);
		getBillForm().getBillCardPanel().setHeadItem("card_code", card_code);

		if (vo.getBill_source().equals("handin")) {
			String asset_code = getBillForm().getHeadItemStringValue("asset_code");
			vo.setAsset_code(asset_code);
		}
	}

	private void initMutilUsedept(AggAssetVO aggVO) {
		BillCardPanel cardPanel = getBillForm().getBillCardPanel();

		SuperVO[] usedeptVOs = aggVO.getTableVO(CardTabConst.useDept);
		if ((usedeptVOs != null) && (usedeptVOs.length > 1)) {
			cardPanel.getHeadItem("usedep_flag").setValue(UFBoolean.TRUE);
			cardPanel.getHeadItem("pk_usedept").setEdit(false);
			cardPanel.getHeadItem("pk_usedept").setNull(false);

			cardPanel.setBodyMenuShow(CardTabConst.useDept, true);

			int index = cardPanel.getBillData().getBodyTableCodeIndex(CardTabConst.useDept);
			cardPanel.getBodyTabbedPane().setSelectedIndex(index);

			cardPanel.getBillModel(CardTabConst.useDept).setBodyDataVO(usedeptVOs);

			cardPanel.getBillModel(CardTabConst.useDept).loadLoadRelationItemValue();
		} else {
			cardPanel.getHeadItem("usedep_flag").setValue(UFBoolean.FALSE);
			String pk_org = getBillForm().getModel().getContext().getPk_org();
			if (StringUtils.isNotEmpty(pk_org)) {
				cardPanel.getHeadItem("pk_usedept").setEdit(true);
			}
		}
	}

	public void handleEvent(AppEvent event) {
		if (event.getType().equals("waitForData_changeEvent")) {
			AggAssetVO aggVO = this.dataManager.getSelectedWaitForData();
			if (aggVO != null) {
				AssetVO assetVO = (AssetVO) aggVO.getParentVO();
				String newTransType = assetVO.getTransi_type();

				changeTemplate(newTransType);
				getBillForm().getModel().setUiState(UIState.ADD);

				String pk_org = assetVO.getPk_org();

				if (assetVO.getBill_source() != "handin") {
					((AssetDataModel) this.billForm.getModel()).setPkOrg(pk_org, null, false);
				} else {
					((AssetDataModel) this.billForm.getModel()).setPkOrg(pk_org, null, true);
				}

				fillDefualtValueToAggVO();

				synchronizeBillFormData();
				try {
					WaitDataChangeEvent waitEvent = (WaitDataChangeEvent) event;
					RelativeClientTool.doDefaultRelativeWhenAdd(getBillForm(), waitEvent.getAddType());
				} catch (BusinessException be) {
					Debug.error("资产卡片新增时初始化事件关系网异常（WaitForBuilder #handleEvent）:" + be.getMessage());

					getBillForm().showErrorMessage(be.getMessage());
					return;
				}
				initMutilUsedept(aggVO);
			}
		}
	}

	private void changeTemplate(String newTransType) {
		this.billForm.changeTemlate(newTransType);

		BillItem[] headtailitems = this.billForm.getBillCardPanel().getHeadItems();

		Map<String, String> notNullFields = new HashMap();
		if (headtailitems != null) {
			for (int i = 0; i < headtailitems.length; i++) {
				String fileName = headtailitems[i].getKey();
				if (headtailitems[i].isNull()) {
					notNullFields.put(fileName, headtailitems[i].getName());
				}
			}
		}
		getDataManager().setNotNullFields(notNullFields);
	}

	public WaitForBuildDataManage getDataManager() {
		return this.dataManager;
	}

	public void setDataManager(WaitForBuildDataManage dataManager) {
		this.dataManager = dataManager;
	}

	public AssetBillForm getBillForm() {
		return this.billForm;
	}

	public void setBillForm(AssetBillForm billForm) {
		this.billForm = billForm;

		if (billForm.getModel() != null) {
			billForm.getModel().addAppEventListener(this);
		}
	}
}