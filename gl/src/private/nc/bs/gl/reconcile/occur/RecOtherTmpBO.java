package nc.bs.gl.reconcile.occur;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.naming.NamingException;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.gl.dmo.SingleTabDMO;
import nc.bs.gl.reconcile.center.RecVoucherUtilBS;
import nc.bs.gl.reconcile.notrecdetail.INotRecDetailGen;
import nc.bs.gl.reconcile.notrecdetail.NotRecDetailGenFactory;
import nc.bs.gl.voucher.DetailBO;
import nc.bs.logging.Logger;
import nc.itf.gl.contrast.data.GLArrayListProcessor;
import nc.itf.gl.pub.ICashFlowCase;
import nc.itf.gl.pub.IFreevaluePub;
import nc.itf.gl.reconcile.cfitemrelation.ICFItemRelationQueryService;
import nc.itf.gl.voucher.IVoucher;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.pubitf.accperiod.AccountCalendar;
import nc.pubitf.bd.accessor.GeneralAccessorFactory;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.pubitf.uapbd.ICustSupPubService_C;
import nc.vo.bd.accessor.IBDData;
import nc.vo.bd.account.AccAssVO;
import nc.vo.bd.account.AccountVO;
import nc.vo.bd.period.AccperiodVO;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.fipub.freevalue.Module;
import nc.vo.fipub.freevalue.account.proxy.AccAssGL;
import nc.vo.fipub.utils.StrTools;
import nc.vo.gateway60.accountbook.AccountBookUtil;
import nc.vo.gateway60.itfs.AccountUtilGL;
import nc.vo.gateway60.itfs.CalendarUtilGL;
import nc.vo.gl.cashflowcase.CashflowcaseVO;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.gl.reconcile.cfitemrelation.CFItemRelationVO;
import nc.vo.gl.reconcile.othertmp.RecOtherTmpVO;
import nc.vo.gl.reconcile.othertmp.ReconDetailVO;
import nc.vo.gl.reconcile.othertmp.ReconVoucher;
import nc.vo.glcom.ass.AssVO;
import nc.vo.glcom.exception.GLBusinessException;
import nc.vo.glcom.tools.GLPubProxy;
import nc.vo.org.AccountingBookVO;
import nc.vo.org.FinanceOrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.MultiLangText;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import org.apache.commons.lang.StringUtils;

public class RecOtherTmpBO {
	public RecOtherTmpBO() {
	}

	public boolean insertArray(RecOtherTmpVO[] vos) throws BusinessException {
		boolean result = false;

		String[] fieldnames = { "pk_reconcileoccur", "pk_voucher", "pk_selfcorp", "pk_othercorp", "pk_vouchertype", "prepareddate", "pk_prepared",
				"totalcredit", "totaldebit", "explanation", "pk_recvoucher", "pk_selfglbook", "pk_selfglorg", "pk_otherglbook", "pk_otherglorg",
				"pk_otherglorgbook", "pk_selfglorgbook", "pk_group" };

		int[] fieldtypes = { 1, 1, 1, 1, 1, 1, 1, 8, 8, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		try {
			result = new SingleTabDMO().insertArray(vos, fieldnames, fieldtypes);
		} catch (SQLException e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessException(e);
		} catch (NamingException e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessException(e);
		}
		return result;
	}

	public VoucherVO getVouchVOByPkOtherTmps(String[] pk_recotherTmps) throws GLBusinessException {
		VoucherVO result = null;
		List<String> srcVoucherpk = new ArrayList();
		try {
			ReconVoucher[] recvouchers = ReconVoucherBO.qryByrecotherTmpsPKs(pk_recotherTmps);
			if ((null == recvouchers) || (recvouchers.length == 0)) {
				return null;
			}
			Vector detailvec = new Vector();
			for (ReconVoucher recvouch : recvouchers) {
				srcVoucherpk.add(recvouch.getPk_sourcepk());
				if (null == result) {
					result = new VoucherVO();
					result.setPk_group(AccountBookUtil.getPkGruopByAccountingBookPK(recvouch.getPk_accountingbook()));
					result.setPk_accountingbook(recvouch.getPk_accountingbook());
					result.setPk_vouchertype(recvouch.getPk_vouchertype());
					String pk_accountingbook = recvouch.getPk_accountingbook();

					long busitime = InvocationInfoProxy.getInstance().getBizDateTime();
					UFDate date = new UFDate(busitime);
					AccountCalendar cal = CalendarUtilGL.getAccountCalendarByAccountBook(pk_accountingbook);
					cal.setDate(date);
					result.setPrepareddate(date);
					result.setYear(cal.getYearVO().getPeriodyear());
					result.setPeriod(cal.getMonthVO().getAccperiodmth());
					result.setM_adjustperiod(cal.getMonthVO().getAccperiodmth());
					result.setTotalcredit(recvouch.getTotalcredit());
					result.setTotaldebit(recvouch.getTotaldebit());
					result.setExplanation(recvouch.getExplanation());
					result.setErrmessage(recvouch.getErrmessage());
					result.setPk_sourcepk(recvouch.getPk_sourcepk());
					result.setPk_setofbook(recvouch.getPk_setofbook());
					result.setPk_org(recvouch.getPk_org());
					result.setDiscardflag(UFBoolean.FALSE);
					result.setVoucherkind(Integer.valueOf(0));
					result.setIsdifflag(UFBoolean.FALSE);
					result.setPk_system("CV");
				}
				HashMap<String, DetailVO> detailmap = new HashMap();
				if ((null != recvouch.getAggdetails()) && (recvouch.getAggdetails().length > 0)) {
					DetailBO detailbo = new DetailBO();
					String pk_srcAccpk = recvouch.getPk_accountingbook();
					String pk_optAccountingbookpk = getpk_sourceAccpk(recvouch.getPk_sourcepk());

					Map<String, String> accasoaMap = new HashMap();
					for (ReconDetailVO detail : recvouch.getAggdetails()) {
						accasoaMap.put(detail.getPk_accasoa(), null);
					}
					IGeneralAccessor accessor = GeneralAccessorFactory.getAccessor("23a89307-5992-460e-95dd-c628c85f7f95");
					IBDData[] bdDatas = accessor.getDocbyPks((String[]) accasoaMap.keySet().toArray(new String[0]));
					accasoaMap.clear();
					if ((bdDatas != null) && (bdDatas.length > 0)) {
						for (IBDData bdData : bdDatas) {
							accasoaMap.put(bdData.getPk(), bdData.getCode());
						}
					}
					Map<String, AccountVO> codePkMap = new HashMap();
					long busitime = InvocationInfoProxy.getInstance().getBizDateTime();
					AccountVO[] accountVOs = AccountUtilGL.queryAccountVosByCodes(pk_srcAccpk, (String[]) accasoaMap.values().toArray(new String[0]),
							new UFDate(busitime).toLocalString());
					if ((accountVOs != null) && (accountVOs.length > 0)) {
						for (AccountVO accountVo : accountVOs) {
							codePkMap.put(accountVo.getCode(), accountVo);
						}
					}
					Map<String, AccountVO> old2newMap = new HashMap();
					for (String key : accasoaMap.keySet()) {
						String value = (String) accasoaMap.get(key);
						old2newMap.put(key, codePkMap.get(value));
					}

					for (ReconDetailVO detail : recvouch.getAggdetails()) {
						DetailVO detailvo = new DetailVO();
						detailvo.setPk_sourcepk(detail.getPk_sourcepk());

						setSrcSoaAccid(detailvo, detail, pk_srcAccpk, pk_optAccountingbookpk, old2newMap);

						detailvo.setPk_otherorgbook(pk_srcAccpk);
						detailvo.setExplanation(detail.getExplanation());
						detailvo.setExcrate2(detail.getExcrate2());
						detailvo.setExcrate3(detail.getExcrate3());
						detailvo.setExcrate4(detail.getExcrate4());
						detailvo.setPrice(detail.getPrice());
						detailvo.setDebitquantity(detail.getDebitquantity());
						detailvo.setDebitamount(detail.getDebitamount());
						detailvo.setLocaldebitamount(detail.getLocaldebitamount());
						detailvo.setGroupdebitamount(detail.getGroupdebitamount());
						detailvo.setGlobaldebitamount(detail.getGlobaldebitamount());
						detailvo.setCreditquantity(detail.getCreditquantity());
						detailvo.setCreditamount(detail.getCreditamount());
						detailvo.setLocalcreditamount(detail.getLocalcreditamount());
						detailvo.setGroupcreditamount(detail.getGroupcreditamount());
						detailvo.setGlobalcreditamount(detail.getGlobalcreditamount());
						detailvo.setPk_accountingbook(detail.getPk_accountingbook());
						detailvo.setPk_currtype(detail.getPk_currtype());
						if (detailmap.containsKey(detail.getPk_sourcepk())) {
							DetailVO dvo = (DetailVO) detailmap.get(detail.getPk_sourcepk());
							detailvo.setUserData(dvo);
						} else {
							DetailVO dvo = detailbo.findByPrimaryKey(detail.getPk_sourcepk());
							detailvo.setUserData(dvo);
							detailmap.put(detail.getPk_sourcepk(), dvo);
						}
						appendCashflowInfo(detailvo, pk_optAccountingbookpk);
						detailvec.add(detailvo);
					}
				}
			}
			result.setDetail(detailvec);
			// 20161229 tsy 不进行本币的重新计算
			// RecVoucherUtilBS.convertDetailAmount(result);
			// 20161229 end
			RecVoucherUtilBS.setGroupSelfValue(result);
			RecVoucherUtilBS.setGloalSelfValue(result);
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
			throw new GLBusinessException(e);
		}

		try {
			if (pk_recotherTmps.length == 1) {
				IVoucher iv = (IVoucher) NCLocator.getInstance().lookup(IVoucher.class.getName());
				VoucherVO srcVoucher = iv.queryByPk((String) srcVoucherpk.get(0));

				result.setPk_vouchertype(srcVoucher.getPk_vouchertype());

				INotRecDetailGen nRecDetailGen = NotRecDetailGenFactory.getNotRecDetailGen(srcVoucher, srcVoucher.getPk_accountingbook());

				DetailVO[] nRecDetail = nRecDetailGen.genRecDetail(srcVoucher, result);
				if ((nRecDetail != null) && (nRecDetail.length > 0)) {
					for (DetailVO detail : nRecDetail) {
						result.getDetail().add(detail);
					}
				}
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}

		return result;
	}

	private void setSrcSoaAccid(DetailVO detailvo, ReconDetailVO detail, String pk_srcAccpk, String pk_optAccountingbook, Map<String, AccountVO> old2newMap)
			throws BusinessException {
		AccountVO accountVo = (AccountVO) old2newMap.get(detail.getPk_accasoa());

		if (accountVo == null) {
			AccountingBookVO abvo = AccountBookUtil.getAccountingBookVOByPrimaryKey(pk_srcAccpk);
			throw new BusinessException("核算账簿：" + abvo.getCode() + "+科目" + detail.getPk_accasoa() + "+ 当前日期," + "无法找到对应的科目");
		}

		detailvo.setPk_accasoa(detail.getPk_accasoa());
		FinanceOrgVO forgvo = AccountBookUtil.getOrgByPk_Accbook(pk_optAccountingbook);
		AccAssVO[] accass = accountVo.getAccass();
		if (accass != null) {
			AssVO[] ass = new AssVO[accass.length];
			boolean isfindAss = false;
			for (int i = 0; i < accass.length; i++) {
				ass[i] = new AssVO();
				ass[i].setPk_Checktype(accass[i].getPk_entity());
				String classId = AccAssGL.getClassidByCheckTypePk(accass[i].getPk_entity());

				if (((classId.equals("720dcc7c-ff19-48f4-b9c5-b90906682f45")) || (classId.equals("e4f48eaf-5567-4383-a370-a59cb3e8a451")) || (classId
						.equals("8c6510dd-3b8a-4cfc-a5c5-323d53c6006f"))) && (!isfindAss)) {

					if (classId.equals("8c6510dd-3b8a-4cfc-a5c5-323d53c6006f")) {
						String getpk_cuppk = getpk_cuppk(forgvo.getPrimaryKey(), true);

						if (StringUtils.isEmpty(getpk_cuppk)) {
							getpk_cuppk = getpk_cuppk(forgvo.getPrimaryKey(), false);
						}
						ass[i].setPk_Checkvalue(getpk_cuppk);
					} else {
						ass[i].setPk_Checkvalue(getpk_cuppk(forgvo.getPrimaryKey(), classId.equals("720dcc7c-ff19-48f4-b9c5-b90906682f45")));
					}
					isfindAss = true;
				} else {
					String srcAssid = (String) new BaseDAO().executeQuery("select assid from gl_detail where pk_detail='" + detailvo.getPk_sourcepk() + "'",
							new ColumnProcessor());

					if (!StrTools.isEmptyStr(srcAssid)) {
						AssVO[] srcAss = GLPubProxy.getRemoteFreevaluePub().queryAssvosByid(srcAssid, Module.GL);
						if (srcAss != null) {
							for (AssVO srcA : srcAss) {

								if (srcA.getPk_Checktype().equals(ass[i].getPk_Checktype())) {
									ass[i].setPk_Checkvalue(srcA.getPk_Checkvalue());
									break;
								}
							}
						}
					}
				}
			}
			String pk_group = AccountBookUtil.getPkGruopByAccountingBookPK(pk_srcAccpk);
			String newAssid = GLPubProxy.getRemoteFreevaluePub().getAssID(ass, Boolean.TRUE, pk_group, Module.GL);
			detailvo.setAssid(newAssid);
			detailvo.setAss(GLPubProxy.getRemoteFreevaluePub().queryAssvosByid(newAssid, Module.GL));
		}
	}

	private String getpk_sourceAccpk(String voucherpk) {
		try {
			List<String> accountbook = (List) new BaseDAO().executeQuery("select pk_accountingbook from gl_voucher where pk_voucher='" + voucherpk + "'",
					new GLArrayListProcessor());

			if (!accountbook.isEmpty()) {
				return (String) accountbook.get(0);
			}
		} catch (DAOException e) {
			Logger.error(e.getMessage(), e);
		}
		return null;
	}

	private String getpk_cuppk(String pkforg, boolean isSupplier) throws BusinessException {
		return ((ICustSupPubService_C) NCLocator.getInstance().lookup(ICustSupPubService_C.class)).queryCustsupPkByOrgPk(pkforg, isSupplier);
	}

	public void appendCashflowInfo(DetailVO detailvo, String pk_optbook) {
		String souceBookPk = detailvo.getPk_accountingbook();
		boolean isCrossGroup = false;
		if (!AccountBookUtil.getPkGruopByAccountingBookPK(souceBookPk).equals(AccountBookUtil.getPkGruopByAccountingBookPK(pk_optbook))) {
			isCrossGroup = true;
		}
		ICashFlowCase cashFlowCase = (ICashFlowCase) NCLocator.getInstance().lookup(ICashFlowCase.class);
		try {
			CashflowcaseVO[] cfCases = cashFlowCase.queryByPKDetails(new String[] { detailvo.getPk_sourcepk() });
			if ((cfCases != null) && (cfCases.length > 0)) {
				HashMap<String, IBDData> oppCfitemMap = new HashMap();

				Set<String> cfitem = new HashSet();
				for (CashflowcaseVO vo : cfCases) {
					cfitem.add(vo.getPk_cashflow());
				}
				IGeneralAccessor accessor = GeneralAccessorFactory.getAccessor("08d4138b-a7b5-42fd-94bc-bb6eb7ac0fdc");
				IBDData[] datas = accessor.getDocbyPks((String[]) cfitem.toArray(new String[0]));
				if (datas != null) {
					for (IBDData data : datas) {
						if (data != null) {
							oppCfitemMap.put(data.getPk(), data);
						}
					}
				}
				ICFItemRelationQueryService service = (ICFItemRelationQueryService) NCLocator.getInstance().lookup(ICFItemRelationQueryService.class);
				String pk_group = AccountBookUtil.getPkGruopByAccountingBookPK(souceBookPk);
				if (isCrossGroup) {
					pk_group = "GLOBLE00000000000000";
				}

				CFItemRelationVO[] cfItemVOs = service.queryCFItemRelationsUnTrans(pk_group);
				if (cfItemVOs == null) {
					cfItemVOs = new CFItemRelationVO[0];
				}

				CFItemRelationVO[] globeCfItemVOs = null;
				if (!isCrossGroup) {
					globeCfItemVOs = service.queryCFItemRelationsUnTrans("GLOBLE00000000000000");
					if (globeCfItemVOs == null) {
						globeCfItemVOs = new CFItemRelationVO[0];
					}
				}

				cfitem.clear();

				List<CashflowcaseVO> oppCashflow = new ArrayList();
				for (CashflowcaseVO cfvo : cfCases) {
					IBDData data = (IBDData) oppCfitemMap.get(cfvo.getPk_cashflow());
					String cfcode = null;
					for (CFItemRelationVO vo : cfItemVOs) {
						if (data.getCode().startsWith(vo.getSrccfitemcode())) {
							cfcode = vo.getDescfitemcode();
							break;
						}
					}

					if ((cfcode == null) && (!isCrossGroup)) {
						for (CFItemRelationVO vo : globeCfItemVOs) {
							if (data.getCode().startsWith(vo.getSrccfitemcode())) {
								cfcode = vo.getDescfitemcode();
								break;
							}
						}
					}
					if (cfcode != null) {
						cfvo.setPk_cashflow(cfcode);
						cfitem.add(cfcode);
						oppCashflow.add(cfvo);
					}
					cfvo.setPk_glorgbook(detailvo.getPk_accountingbook());
					cfvo.setPk_detail(null);
					cfvo.setPk_cashflowcase(null);
					cfvo.setPk_corp(AccountBookUtil.getPkGruopByAccountingBookPK(cfvo.getPk_glorgbook()));
					cfvo.setPk_unit(AccountBookUtil.getPk_org(cfvo.getPk_glorgbook()));
					cfvo.setPk_innercorp(AccountBookUtil.getPk_org(pk_optbook));
				}
				detailvo.setCashFlow((CashflowcaseVO[]) oppCashflow.toArray(new CashflowcaseVO[0]));
				oppCfitemMap.clear();
				datas = accessor.getDocByCodes(AccountBookUtil.getPk_org(souceBookPk), (String[]) cfitem.toArray(new String[0]));
				if (datas != null) {
					for (IBDData data : datas) {
						if (data != null) {
							oppCfitemMap.put(data.getCode(), data);
						}
					}
				}

				CashflowcaseVO[] cfvos = detailvo.getCashFlow();
				for (CashflowcaseVO cfvo : cfvos) {
					IBDData data = (IBDData) oppCfitemMap.get(cfvo.getPk_cashflow());
					if (data != null) {
						cfvo.setPk_cashflow(data.getPk());
						cfvo.setCashflowcode(data.getCode());
						cfvo.setCashflowName(data.getName().toString());
					}
				}
			}
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		}
	}
}