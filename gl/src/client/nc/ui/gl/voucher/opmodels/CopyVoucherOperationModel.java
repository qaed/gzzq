package nc.ui.gl.voucher.opmodels;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.gl.pub.IFreevaluePub;
import nc.ui.gl.datacache.AccountCache;
import nc.ui.gl.formulaexecute.GlAssVOTools;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.gl.vouchercard.VoucherModel;
import nc.ui.gl.voucherdata.VoucherDataBridge;
import nc.ui.gl.vouchermodels.AbstractOperationModel;
import nc.ui.gl.vouchertools.VoucherDataCenter;
import nc.ui.glcom.period.GlPeriodForClient;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.ToftPanel;
import nc.vo.bd.account.AccAssVO;
import nc.vo.bd.account.AccountVO;
import nc.vo.bd.currinfo.CurrinfoVO;
import nc.vo.fipub.freevalue.Module;
import nc.vo.gateway60.accountbook.AccountBookUtil;
import nc.vo.gateway60.accountbook.GlOrgUtils;
import nc.vo.gateway60.itfs.Currency;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.glcom.ass.AssVO;
import nc.vo.glcom.glperiod.GlPeriodVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;

public class CopyVoucherOperationModel extends AbstractOperationModel {
	public CopyVoucherOperationModel() {
	}

	public Object doOperation() {
		Boolean isInSum = (Boolean) getMasterModel().getParameter("isInSumMode");
		if ((isInSum != null) && (isInSum.booleanValue())) {
			return null;
		}
		Component jc = getMasterModel().getUI();
		while ((!(jc instanceof ToftPanel)) && (jc != null)) {
			jc = jc.getParent();
		}
		ToftPanel tfpanel = (ToftPanel) jc;
		getMasterModel().setParameter("stopediting", null);
		getMasterModel().doOperation("CONFIRMSAVE");
		((VoucherModel) getMasterModel()).getView().setEditable(true);
		VoucherVO voucher = (VoucherVO) getMasterModel().getParameter("vouchervo");

		voucher.clearEmptyDetail();
		VoucherVO tmp_voucher = (VoucherVO) voucher.clone();
		tmp_voucher.setPk_voucher(null);
		tmp_voucher.setPk_casher(null);
		tmp_voucher.setPk_checked(null);
		tmp_voucher.setPk_manager(null);
		tmp_voucher.setPk_prepared(GlWorkBench.getLoginUser());
		tmp_voucher.setPk_system("GL");
		tmp_voucher.setCashername(null);
		tmp_voucher.setCheckedname(null);
		tmp_voucher.setManagername(null);
		tmp_voucher.setDiscardflag(UFBoolean.FALSE);
		tmp_voucher.setErrmessage(null);

		tmp_voucher.setModifyflag("YYY");
		tmp_voucher.setDetailmodflag(UFBoolean.TRUE);
		tmp_voucher.setIsmatched(new Boolean(false));
		tmp_voucher.setErrmessageh(null);
		tmp_voucher.setHasCashflowModified(false);
		tmp_voucher.setPrepareddate(GlWorkBench.getBusiDate());
		tmp_voucher.setTempsaveflag(UFBoolean.FALSE);
		tmp_voucher.setCreator(GlWorkBench.getLoginUser());
		tmp_voucher.setCreationtime(new UFDateTime(GlWorkBench.getDefaultDate().getMillis()));
		tmp_voucher.setModifier(null);
		tmp_voucher.setModifiedtime(null);
		tmp_voucher.setDeleteclass(null);
		tmp_voucher.setIsOffer(UFBoolean.FALSE);
		tmp_voucher.setOffervoucher(null);

		try {
			if ((voucher.getVoucherkind() != null) && (voucher.getVoucherkind().intValue() == 1)) {
				tmp_voucher.setPrepareddate(voucher.getPrepareddate());
				tmp_voucher.setYear(voucher.getYear());
				tmp_voucher.setPeriod(voucher.getPeriod());
				tmp_voucher.setM_adjustperiod(voucher.getM_adjustperiod());
				tmp_voucher.setModifyflag("NYY");
			} else {
				tmp_voucher.setVoucherkind(Integer.valueOf(0));
			}
			GlPeriodVO pvo = new GlPeriodForClient().getPeriod(tmp_voucher.getPk_accountingbook(), tmp_voucher.getPrepareddate());
			if (pvo != null) {
				tmp_voucher.setYear(pvo.getYear());
				tmp_voucher.setPeriod(pvo.getMonth());
			} else {
				tmp_voucher.setYear(VoucherDataCenter.getClientYear());
				tmp_voucher.setPeriod(VoucherDataCenter.getClientPeriod());
			}
			tmp_voucher.setNo(VoucherDataBridge.getInstance().getCorrectVoucherNo(tmp_voucher));
		} catch (Exception e) {
			tmp_voucher.setYear(VoucherDataCenter.getClientYear());
			tmp_voucher.setPeriod(VoucherDataCenter.getClientPeriod());
		}
		tmp_voucher.setPk_sourcepk(null);
		tmp_voucher.setConvertflag(UFBoolean.FALSE);

		String[] pk_units = new String[tmp_voucher.getNumDetails() + 1];
		DetailVO detailTemp = null;
		int i = 0;
		for (i = 0; i < tmp_voucher.getNumDetails(); i++) {
			detailTemp = tmp_voucher.getDetail(i);
			detailTemp.setPk_detail(null);
			detailTemp.setErrmessage(null);
			detailTemp.setModifyflag("YYYYYYYYYYYYYYYYYYYY");
			// 20161227 tsy 修改：复制凭证时汇率不能直接复制，而应该是当前的实际汇率
			// 获取当前实际汇率
			UFDouble currRate = VoucherDataCenter.getCurrrateByPk_orgbook(tmp_voucher.getPk_accountingbook(), detailTemp.getPk_currtype(),
					VoucherDataCenter.getMainCurrencyPK(detailTemp.getPk_accountingbook()), new UFDate().toLocalString());
			// 设置汇率
			detailTemp.setExcrate2(currRate);
			// 获取原币金额
			// --贷方原币金额
			UFDouble creditAmount = detailTemp.getCreditamount();
			// --借方原币金额
			UFDouble debitAmount = detailTemp.getDebitamount();
			// 获取当前折算模式
			CurrinfoVO currinfo = null;
			try {
				currinfo = Currency.getCurrRateInfo(tmp_voucher.getPk_accountingbook(), detailTemp.getPk_currtype(),
						Currency.getLocalCurrPK(tmp_voucher.getPk_accountingbook()));
			} catch (BusinessException e1) {
				e1.printStackTrace();
			}
			// 设置金额
			if (currinfo == null ? true : currinfo.getConvmode() == 0) {// 折算模式为本币*汇率=目的币种（默认）
				// 设置贷方金额
				if (creditAmount != null && !creditAmount.equals(UFDouble.ZERO_DBL)) {
					detailTemp.setLocalcreditamount(creditAmount.multiply(currRate));
				}
				// 设置借方金额
				if (debitAmount != null && !debitAmount.equals(UFDouble.ZERO_DBL)) {
					detailTemp.setLocaldebitamount(debitAmount.multiply(currRate));
				}
			} else {// 折算模式为本币/汇率=目的币种
					// 设置贷方金额
				if (creditAmount != null && !creditAmount.equals(UFDouble.ZERO_DBL)) {
					detailTemp.setLocalcreditamount(creditAmount.div(currRate));
				}
				// 设置借方金额
				if (debitAmount != null && !debitAmount.equals(UFDouble.ZERO_DBL)) {
					detailTemp.setLocaldebitamount(debitAmount.div(currRate));
				}
			}
			// 20161227 end

			detailTemp.setIsmatched(UFBoolean.FALSE);
			detailTemp.setUserData(null);
			detailTemp.setOtheruserdata(null);

			detailTemp.setBusireconno(null);
			detailTemp.setInnerbusno(null);
			detailTemp.setTempsaveflag(UFBoolean.FALSE);
			detailTemp.setVerifydate(tmp_voucher.getPrepareddate().toString());
			detailTemp.setPk_sourcepk(null);
			detailTemp.setVoucherkind(Integer.valueOf(0));
			detailTemp.setPrepareddate(tmp_voucher.getPrepareddate());

			pk_units[i] = detailTemp.getPk_unit();

			AccountVO newAccountVo = AccountCache.getInstance().getAccountVOByPK(detailTemp.getPk_accountingbook(), detailTemp.getPk_accasoa(),
					tmp_voucher.getPrepareddate().toString());
			AccAssVO[] accAssVOs = newAccountVo.getAccass();
			AssVO[] newAssVOs = getNewAssVOs(accAssVOs, detailTemp.getAss());
			if (newAssVOs == null) {
				detailTemp.setAss(null);
			} else {
				if (!GlAssVOTools.isAssVOMatched(detailTemp.getAss(), newAssVOs)) {
					String assid = null;
					try {
						assid = ((IFreevaluePub) NCLocator.getInstance().lookup(IFreevaluePub.class)).getAssID(newAssVOs, Boolean.valueOf(true),
								tmp_voucher.getPk_group(), Module.GL);
					} catch (BusinessException e) {
						Logger.error(e.getMessage(), e);
					}
					detailTemp.setAssid(assid);
				}
				detailTemp.setAss(newAssVOs);
			}
		}
		if (tmp_voucher.getPk_org() == null) {
			tmp_voucher.setPk_org(AccountBookUtil.getPk_orgByAccountBookPk(tmp_voucher.getPk_accountingbook()));
		}
		pk_units[i] = tmp_voucher.getPk_org();
		if (tmp_voucher.getPk_org() != null) {
			HashMap<String, String> versionMap = null;
			try {
				versionMap = GlOrgUtils.getNewVIDSByOrgIDSAndDate(pk_units, tmp_voucher.getPrepareddate());
			} catch (BusinessException e) {
				Logger.error(e.getMessage(), e);
			}
			if (versionMap != null) {
				tmp_voucher.setPk_org_v((String) versionMap.get(tmp_voucher.getPk_org()));
				for (DetailVO detail : tmp_voucher.getDetails()) {
					detail.setPk_unit_v((String) versionMap.get(detail.getPk_unit()));
				}
			}
		}

		getMasterModel().doOperation("CLEAROTHERVOUCHERTAB");
		getMasterModel().setParameter("vouchervo", tmp_voucher);
		getMasterModel().setParameter("saveflag", new Boolean(false));
		getMasterModel().setParameter("startediting", null);

		tfpanel.showHintMessage(NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000040"));
		return null;
	}

	private AssVO[] getNewAssVOs(AccAssVO[] accAssVOs, AssVO[] oldAssVOs) {
		if ((accAssVOs == null) || (accAssVOs.length == 0)) {
			return null;
		}
		Map<String, AssVO> oldAssMap = new HashMap();

		if ((oldAssVOs != null) && (oldAssVOs.length > 0)) {
			for (AssVO assVo : oldAssVOs) {
				oldAssMap.put(assVo.getPk_Checktype(), assVo);
			}
		}

		List<AssVO> rtList = new ArrayList();
		for (AccAssVO accAssVo : accAssVOs) {
			if (oldAssMap.containsKey(accAssVo.getPk_entity())) {
				rtList.add(oldAssMap.get(accAssVo.getPk_entity()));
			} else {
				AssVO assVo = new AssVO();
				assVo.setPk_Checktype(accAssVo.getPk_entity());
				rtList.add(assVo);
			}
		}

		return (AssVO[]) rtList.toArray(new AssVO[0]);
	}
}