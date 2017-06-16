package nc.vo.gl.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import nc.bs.logging.Log;
import nc.bs.logging.Logger;
import nc.gl.account.glconst.CurrTypeConst;
import nc.gl.utils.GLMultiLangUtil;
import nc.itf.gl.pub.IFreevaluePub;
import nc.pubitf.accperiod.AccountCalendar;
import nc.ui.fi.uforeport.UFOFunNameTransfer;
import nc.vo.bank_cvp.compile.Compiler;
import nc.vo.bank_cvp.compile.FunctionStruct;
import nc.vo.bank_cvp.compile.word.Word;
import nc.vo.bd.account.AccAssVO;
import nc.vo.bd.account.AccountVO;
import nc.vo.bd.currtype.CurrtypeVO;
import nc.vo.bd.period.AccperiodVO;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.fipub.freevalue.Module;
import nc.vo.fipub.freevalue.account.proxy.AccAssGL;
import nc.vo.gateway60.accountbook.AccountBookUtil;
import nc.vo.gateway60.accountbook.GlOrgUtils;
import nc.vo.gateway60.itfs.AccountThreadCache;
import nc.vo.gateway60.itfs.CalendarUtilGL;
import nc.vo.gateway60.itfs.Currency;
import nc.vo.gateway60.pub.GlBusinessException;
import nc.vo.gl.amortize.setting.AmorCycleVO;
import nc.vo.gl.amortize.setting.AmorStrategy;
import nc.vo.gl.amortize.setting.AmortizeRateEnum;
import nc.vo.gl.amortize.setting.AmortizeVO;
import nc.vo.gl.exception.NoAccsubjException;
import nc.vo.gl.exception.TransferDetailException;
import nc.vo.gl.exception.TransferHeadException;
import nc.vo.gl.formulaexecute.FormulaExpression;
import nc.vo.gl.formulaexecute.GLAssVOContainer;
import nc.vo.gl.formulaexecute.GlStringParse;
import nc.vo.gl.formulaexecute.GroupFunctionTool;
import nc.vo.gl.formulaexecute.QueryVOGenerator;
import nc.vo.gl.formulaexecute.ResultSetGetter;
import nc.vo.gl.glreport.GroupFunctionVO;
import nc.vo.gl.glreport.publictool.PrepareAssParse;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.glcom.ass.AssVO;
import nc.vo.glcom.balance.GlBalanceVO;
import nc.vo.glcom.balance.GlQueryVO;
import nc.vo.glcom.inteltool.CDataSource;
import nc.vo.glcom.inteltool.CGenTool;
import nc.vo.glcom.inteltool.COutputTool;
import nc.vo.glcom.inteltool.CSumTool;
import nc.vo.glcom.inteltool.IGenToolElementProvider;
import nc.vo.glcom.intelvo.CIntelVO;
import nc.vo.glcom.shellsort.CShellSort;
import nc.vo.glcom.sorttool.CVoSortTool;
import nc.vo.glcom.tools.GLPubProxy;
import nc.vo.glpub.IVoAccess;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.org.AccountingBookVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;

public class VoucherGenerator implements IGenToolElementProvider, nc.vo.glcom.sorttool.ISortToolProvider {
	private static final Log log = Log.getInstance(VoucherGenerator.class);

	private static final UFDouble ZERO = new UFDouble(0);

	private VoucherVO[] voucherHead;

	private TransferEnvVO envVO;

	private TransferVO[] transfervo;

	final boolean $DEBUG = true;

	/**
	 * @deprecated
	 */
	private Boolean isLocalOnly;

	private Hashtable htTransDetails = new Hashtable();

	private String[] funnames = { UFOFunNameTransfer.getFunNameByLang("QC"), UFOFunNameTransfer.getFunNameByLang("QM"),
			UFOFunNameTransfer.getFunNameByLang("FS"), UFOFunNameTransfer.getFunNameByLang("LFS"), UFOFunNameTransfer.getFunNameByLang("RFS"),
			UFOFunNameTransfer.getFunNameByLang("JFS"), UFOFunNameTransfer.getFunNameByLang("RJFS"), UFOFunNameTransfer.getFunNameByLang("RQM"),
			UFOFunNameTransfer.getFunNameByLang("SQC"), UFOFunNameTransfer.getFunNameByLang("SQM"), UFOFunNameTransfer.getFunNameByLang("SFS"),
			UFOFunNameTransfer.getFunNameByLang("SLFS"), UFOFunNameTransfer.getFunNameByLang("SRFS"), UFOFunNameTransfer.getFunNameByLang("SJFS"),
			UFOFunNameTransfer.getFunNameByLang("SRJFS"), UFOFunNameTransfer.getFunNameByLang("SRQM"), "QC", "QM", "FS", "LFS", "JFS", "SQC", "SQM", "SFS",
			"SLFS", "SJFS", "Openbal", "Closebal", "AmtOccr", "NetAmtOccr", "CumulAmtOccr", "OpenQtyBal", "CloseQtyBal", "QtyOccr", "NetQtyOccr",
			"CumulQtyOccr" };

	private PrepareAssParse pap;

	Vector vFormulas = new Vector();

	Vector<String> vResultAss = new Vector();

	Vector vTransferDetails = new Vector();

	QueryVOContainer qryContainer = null;

	private boolean LocalCurrtypeOnly = true;

	private String fracCurrtype;

	private String localCurrtype;

	private String groupCurrtype;

	private String globalCurrtype;

	private FormulaExpression formulaExpression;

	private boolean blTransOneByOne = false;

	private HashMap<String, Integer> htCurAndRate = new HashMap();

	private QueryVOGenerator generator = new QueryVOGenerator();

	private String[] selfDefFunnames = null;

	private HashMap h_NameToVOs = null;

	private String pk_corp = null;

	private String pk_orgbook = null;

	private String pk_accountingbook;

	public static String AssFlag = "*";

	public Vector getTheGroupAssIDs(GlQueryVO[] queryvos, Vector<GlQueryVO> vNoThisAss) throws Exception {
		if ((queryvos == null) || (queryvos.length == 0))
			return null;
		Vector<GlQueryVO> vLegelQueryvos = new Vector();
		GLAssVOContainer assvoContainer = new GLAssVOContainer();

		for (int i = 0; i < queryvos.length; i++) {
			if ((queryvos[i].getAssVos() == null) || (queryvos[i].getAssVos().length == 0)) {
				vLegelQueryvos.addElement(queryvos[i]);
			} else {
				AssVO[] assvos = queryvos[i].getAssVos();

				String[] assids = null;
				if (assvoContainer.isContainsAssVO(assvos) >= 0) {
					assids = assvoContainer.getAssIDsByAssvos(assvos);
					if ((assids == null) || (assids.length == 0)) {
						queryvos[i].setAssIDs(null);
						vNoThisAss.addElement(queryvos[i]);
					} else {
						queryvos[i].setAssIDs(assids);
						vLegelQueryvos.addElement(queryvos[i]);
					}
				} else {
					boolean hasOnlyCheckType = false;
					for (int j = 0; j < assvos.length; j++) {
						String checkValuePk = assvos[j].getPk_Checkvalue();
						if ((checkValuePk == null) || ("".equals(checkValuePk))) {
							hasOnlyCheckType = true;
							break;
						}
					}
					if (hasOnlyCheckType) {
						queryvos[i].setAssIDs(null);
						queryvos[i].setQueryByAssVo(hasOnlyCheckType);
						vLegelQueryvos.addElement(queryvos[i]);
					} else {
						assids = GLPubProxy.getRemoteFreevaluePub().getAssIDs(assvos, getEnvVO().getPk_group(), Module.GL);
						boolean isValid = false;
						String strErromsg = "";
						if ((assvos != null) && (assids != null)) {
							for (int m = 0; m < assvos.length; m++) {
								if (((assvos[m].getCheckvaluecode() != null) && (assvos[m].getPk_Checkvalue() == null))
										|| ((assvos[m].getCheckvaluename() != null) && (assvos[m].getPk_Checkvalue() == null))) {
									for (int n = 0; n < assids.length; n++) {
										if ((assvos[m].getPk_Checkvalue() != null) && (assids[n].equals(assvos[m].getPk_Checkvalue()))) {
											isValid = true;
											break;
										}
									}
									if (!isValid) {
										String str1 = assvos[m].getChecktypename() == null ? assvos[m].getChecktypecode() : assvos[m].getChecktypename();
										String str2 = assvos[m].getCheckvaluename() == null ? assvos[m].getCheckvaluecode() : assvos[m].getCheckvaluename();

										strErromsg = str1 + str2 + NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000146");

										throw new BusinessException(strErromsg);
									}
								}
							}
						}
						queryvos[i].setAssIDs(assids);
						assvoContainer.addElements(assvos, assids);
						if ((assids == null) || (assids.length == 0)) {
							vNoThisAss.addElement(queryvos[i]);
						} else {
							vLegelQueryvos.addElement(queryvos[i]);
						}
					}
				}
			}
		}
		return vLegelQueryvos;
	}

	public QueryVOContainer genTheGroupRstForQueryVOs(GlQueryVO[] qryvos) throws Exception {
		if ((qryvos == null) || (qryvos.length == 0))
			return null;
		QueryVOContainer qryVOCont = new QueryVOContainer();
		Vector<GlQueryVO> vfinalQueryvos = new Vector();
		ArrayList<GlBalanceVO[]> finalResult = new ArrayList(qryvos.length);

		GlQueryVO qryvo = qryvos[0];
		GlQueryVO newQueryVO = (GlQueryVO) qryvo.clone();

		Hashtable<String, String> subPkcode = new Hashtable();
		Vector<String> vec2Pk = new Vector();
		Vector<String> vec2Cd = new Vector();

		for (int i = 0; i < qryvos.length; i++) {
			GlQueryVO tempqryvo = qryvos[i];

			String[] pks = tempqryvo.getPk_account();
			for (int j = 0; j < pks.length; j++) {
				String pk = tempqryvo.getPk_account()[j];
				String code = AccountThreadCache.getInstance(getEnvVO().getPk_orgbook(), getEnvVO().getDate().toStdString()).getAccountVO(pk).getCode();
				if (!subPkcode.containsKey(pk)) {
					vec2Pk.add(pk);
					vec2Cd.add(code);
					subPkcode.put(pk, code);
				}
			}
		}

		String[] pk_accsubjs = new String[vec2Pk.size()];
		String[] accsubjcodes = new String[vec2Cd.size()];

		if (vec2Pk.size() > 0) {
			vec2Pk.copyInto(pk_accsubjs);
		}
		if (vec2Cd.size() > 0) {
			vec2Cd.copyInto(accsubjcodes);
		}

		newQueryVO.setPk_account(pk_accsubjs);
		newQueryVO.setAccountCodes(accsubjcodes);

		if (!newQueryVO.isQueryByAssVo()) {
			String[] assids = ResultSetGetter.getAllAssIDs(qryvos);
			newQueryVO.setAssIDs(assids);
		}
		try {
			long t1 = System.currentTimeMillis();
			GlBalanceVO[] tempvos = nc.vo.gl.formula.FunctionProcessor.execQueryBatch(newQueryVO.getFunname(), newQueryVO);
			if ((tempvos != null) && (tempvos.length > 0)) {
				for (int i = 0; i < qryvos.length; i++) {
					GlQueryVO thisQueryvo = qryvos[i];
					GlBalanceVO[] vos = ResultSetGetter.getBalanceVOsByQueryVO(thisQueryvo, tempvos);
					vfinalQueryvos.addElement(thisQueryvo);
					finalResult.add(vos);
				}
			} else {
				for (int i = 0; i < qryvos.length; i++) {
					GlQueryVO thisQueryvo = qryvos[i];

					vfinalQueryvos.addElement(thisQueryvo);
					finalResult.add(null);
				}
			}
			GlQueryVO[] finaleQryVOs = new GlQueryVO[vfinalQueryvos.size()];
			vfinalQueryvos.copyInto(finaleQryVOs);
			qryVOCont.setQueryvos(finaleQryVOs);
			qryVOCont.setAryList(finalResult);

			long t2 = System.currentTimeMillis();
			UFDouble differ = new UFDouble(t2).sub(new UFDouble(t1));
			differ = differ.div(1000.0D);

			Log.getInstance(getClass()).info("**************************************************");
			Log.getInstance(getClass()).info(
					NCLangRes4VoTransl.getNCLangRes().getStrByID("public20111017_0", "02002001-0014") + differ
							+ NCLangRes4VoTransl.getNCLangRes().getStrByID("public20111017_0", "02002001-0015"));

			Log.getInstance(getClass()).info("**************************************************");
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new Exception(NCLangRes4VoTransl.getNCLangRes().getStrByID("2002GL502", "UPP2002GL502-000050"));
		}

		return qryVOCont;
	}

	public VoucherGenerator() {
		try {
			initialize();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new GlBusinessException(e.getMessage());
		}
	}

	public VoucherGenerator(String pk_accountingbook) {
		setPk_accountingbook(pk_accountingbook);
		try {
			initialize();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new GlBusinessException(e.getMessage());
		}
	}

	private boolean isNeedDiscard(DetailVO vo, boolean isProcessed) {
		boolean isAmountEmpty = (vo.getCreditamount().equals(new UFDouble(0))) && (vo.getDebitamount().equals(ZERO))
				&& (vo.getLocalcreditamount().equals(ZERO)) && (vo.getLocaldebitamount() != null) && (vo.getLocaldebitamount().equals(ZERO))
				&& (vo.getFraccreditamount().equals(ZERO)) && (vo.getFracdebitamount().equals(ZERO));

		boolean isQuantityEmpty = ((vo.getCreditquantity() == null) || (vo.getCreditquantity().equals(ZERO)))
				&& ((vo.getDebitquantity() == null) || (vo.getDebitquantity().equals(ZERO)));
		if (!isProcessed) {
			return (!isCEFunction(vo)) && (isAmountEmpty) && (isQuantityEmpty);
		}
		return (isAmountEmpty) && (isQuantityEmpty);
	}

	private DetailVO[] afterFillDetail(Vector<DetailVO> detail, TransferVO transfervo) throws TransferDetailException, Exception {
		for (int i = detail.size() - 1; i >= 0; i--) {
			DetailVO vo = (DetailVO) detail.elementAt(i);
			setNullToZero(vo);

			if (isNeedDiscard(vo, false)) {
				detail.remove(i);
				display(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000227"));
			}
		}
		// 20161227_1 tsy 由后面 20161227_2 移动到这里，先进行汇率计算，再进行金额四舍五入
		calculateCurrRate(detail, transfervo);
		// 20161227_1 end
		setRatePrecision(detail);

		detail = getCEDetail(detail);
		setRatePrecision(detail);

		for (int i = detail.size() - 1; i >= 0; i--) {
			DetailVO vo = (DetailVO) detail.elementAt(i);
			if (isNeedDiscard(vo, true)) {
				detail.remove(i);
				display(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000227"));
			}

			if (vo.getAssid() == null)
				vo.setAssid(null);
		}
		// 20161227_2 tsy 先进行汇率计算，再进行金额四舍五入，移动下行代码至四舍五入前即20161227_1 处
		// calculateCurrRate(detail, transfervo);
		// 20161227_2 end
		calculatePrice(detail);
		DetailVO[] vos = new DetailVO[0];
		setDetailQuantity((DetailVO[]) detail.toArray(vos));

		for (int i = detail.size() - 1; i >= 0; i--) {
			DetailVO vo = (DetailVO) detail.elementAt(i);
			if (isNeedDiscard(vo, true)) {
				detail.remove(i);
				display(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000227"));
			}

			if (vo.getAssid() == null)
				vo.setAssid(null);
		}
		vos = (DetailVO[]) detail.toArray(vos);
		setDetailQuantity(vos);
		orderDetail(vos);

		return vos;
	}

	public void reCalculateLastDetail(DetailVO[] vos) {
		int balanceIndex = -1;
		UFDouble debitTotal = UFDouble.ZERO_DBL;
		UFDouble groupdebitTotal = UFDouble.ZERO_DBL;
		UFDouble globaldebitTotal = UFDouble.ZERO_DBL;
		UFDouble creditTotal = UFDouble.ZERO_DBL;
		UFDouble groupcreditTotal = UFDouble.ZERO_DBL;
		UFDouble globalcreditTotal = UFDouble.ZERO_DBL;
		for (int i = 0; i < vos.length; i++) {
			DetailVO detailVO = vos[i];
			debitTotal = debitTotal.add(detailVO.getLocaldebitamount());
			groupdebitTotal = groupdebitTotal.add(detailVO.getGroupdebitamount() == null ? UFDouble.ZERO_DBL : detailVO.getGroupdebitamount());
			globaldebitTotal = globaldebitTotal.add(detailVO.getGlobaldebitamount() == null ? UFDouble.ZERO_DBL : detailVO.getGlobaldebitamount());
			creditTotal = creditTotal.add(detailVO.getLocalcreditamount());
			groupcreditTotal = groupcreditTotal.add(detailVO.getGroupcreditamount() == null ? UFDouble.ZERO_DBL : detailVO.getGroupcreditamount());
			globalcreditTotal = globalcreditTotal.add(detailVO.getGlobalcreditamount() == null ? UFDouble.ZERO_DBL : detailVO.getGlobalcreditamount());

			if (i == vos.length - 1) {
				balanceIndex = i;
			}
		}

		UFDouble remainder = debitTotal.sub(creditTotal);
		UFDouble groupremainder = groupdebitTotal.sub(groupcreditTotal);
		UFDouble globleremainder = globaldebitTotal.sub(globalcreditTotal);
		if ((!remainder.equals(UFDouble.ZERO_DBL)) && (balanceIndex > 0)) {
			DetailVO balanceVo = vos[balanceIndex];

			if (balanceVo.getDirection()) {
				balanceVo.setLocaldebitamount(balanceVo.getLocaldebitamount().sub(remainder));
			} else {
				balanceVo.setLocalcreditamount(balanceVo.getLocalcreditamount().add(remainder));
			}
		}
		if ((!groupremainder.equals(UFDouble.ZERO_DBL)) && (balanceIndex > 0)) {
			if (balanceIndex >= 0) {
				DetailVO balanceVo = vos[balanceIndex];

				if (balanceVo.getDirection()) {
					balanceVo.setGroupdebitamount((balanceVo.getGroupdebitamount() == null ? UFDouble.ZERO_DBL : balanceVo.getGroupdebitamount())
							.sub(remainder));
				} else {
					balanceVo.setGroupcreditamount((balanceVo.getGroupcreditamount() == null ? UFDouble.ZERO_DBL : balanceVo.getGroupcreditamount())
							.add(remainder));
				}
			}
		}
		if ((!globleremainder.equals(UFDouble.ZERO_DBL)) && (balanceIndex > 0)) {
			if (balanceIndex >= 0) {
				DetailVO balanceVo = vos[balanceIndex];

				if (balanceVo.getDirection()) {
					balanceVo.setGlobaldebitamount((balanceVo.getGlobaldebitamount() == null ? UFDouble.ZERO_DBL : balanceVo.getGlobaldebitamount())
							.sub(remainder));
				} else {
					balanceVo.setGlobalcreditamount((balanceVo.getGlobalcreditamount() == null ? UFDouble.ZERO_DBL : balanceVo.getGlobalcreditamount())
							.add(remainder));
				}
			}
		}
	}

	private Vector calculateCurrRate(Vector vecDetailVO, TransferVO transfervo) throws Exception {
		try {
			String pk_corp = "";
			String pk_group = null;
			for (int i = 0; i < vecDetailVO.size(); i++) {
				DetailVO vo = (DetailVO) vecDetailVO.elementAt(i);

				if ((transfervo.getAmortizeVO() == null)
						|| (!AmortizeRateEnum.INSTANTRATE.toStringValue().equals(transfervo.getAmortizeVO().getAmortizerate())) || (isCEFunction(vo))) {

					UFDouble localAmount = vo.getLocaldebitamount().equals(new UFDouble(0)) ? vo.getLocalcreditamount() : vo.getLocaldebitamount();

					UFDouble groupAmount = vo.getGroupdebitamount().equals(new UFDouble(0)) ? vo.getGroupcreditamount() : vo.getGroupdebitamount();
					UFDouble globalAmount = vo.getGlobaldebitamount().equals(new UFDouble(0)) ? vo.getGlobalcreditamount() : vo.getGlobaldebitamount();
					UFDouble Amount = vo.getDebitamount().equals(new UFDouble(0)) ? vo.getCreditamount() : vo.getDebitamount();
					if (pk_corp.equals("")) {
						pk_corp = vo.getPk_accountingbook();
					}
					nc.vo.bd.currinfo.CurrinfoVO currinfo = null;
					if ((this.localCurrtype != null) && (!this.localCurrtype.equals(vo.getPk_currtype()))) {
						currinfo = Currency.getCurrRateInfo(pk_corp, vo.getPk_currtype(), this.localCurrtype);
					}
					// 20161227_3 tsy 反编译出来后，下面一行的代码逻辑与实际运行逻辑不符
					// 反编译代码->执行currinfo == null,但currinfo不为null，返回false
					// 实际->似乎不执行currinfo == null,且
					// 按F5进入currinfo.getConvmode()方法,最后,localCurrConvmode值为true
					// ---------
					// 原代码
					// boolean localCurrConvmode = currinfo == null;
					// ---------
					// 修改后代码
					/**
					 * 当前本地的折算模式
					 * <p>
					 * 本币与外币的折算方式，有2种值：0或1
					 * <ol>
					 * <li>0 - 源币种 * 汇率 = 目的币种</li>
					 * <li>1 - 源币种 / 汇率 = 目的币种</li>
					 * </ol>
					 */
					boolean localCurrConvmode = (currinfo == null ? true : currinfo.getConvmode() == 0);
					// 20161227_3 end
					if (localCurrConvmode) {
						if (Amount.equals(UFDouble.ZERO_DBL)) {
							vo.setExcrate2(UFDouble.ZERO_DBL);
						} else {
							// 20161227_4 tsy 汇率进行四舍五入
							// vo.setExcrate2(localAmount.div(Amount));
							vo.setExcrate2(format(localAmount.div(Amount), 4));
							// 20161227_4 end
						}

					} else if (localAmount.equals(UFDouble.ZERO_DBL)) {
						vo.setExcrate2(UFDouble.ZERO_DBL);
					} else {
						// 20161227_5 tsy 同20161227_4汇率进行四舍五入
						// vo.setExcrate2(Amount.div(localAmount));
						vo.setExcrate2(format(Amount.div(localAmount), 4));
						// 20161227_5 end
					}

					pk_group = vo.getPk_group();
					if (pk_group == null) {
						pk_group = AccountBookUtil.getAccountingBookVOByPrimaryKey(vo.getPk_accountingbook()).getPk_group();
					}
					if (Currency.isStartGroupCurr(pk_group)) {
						currinfo = null;
						if (!Currency.isGlobalRawConvertModel(pk_group)) {
							if ((this.localCurrtype != null) && (!this.localCurrtype.equals(this.groupCurrtype)))
								currinfo = Currency.getCurrRateInfo(pk_corp, this.localCurrtype, this.groupCurrtype);
							// 20161227_6 tsy 同20161227_3修改获取折算模式的判断逻辑
							// boolean groupCurrConvmode = currinfo == null;
							boolean groupCurrConvmode = (currinfo == null ? true : currinfo.getConvmode() == 0);
							// 20161227_6 end
							if (groupCurrConvmode) {
								if (localAmount.doubleValue() == 0.0D) {
									vo.setExcrate3(UFDouble.ZERO_DBL);
								} else {
									// 20161227_7 tsy 同20161227_4汇率进行四舍五入
									// vo.setExcrate3(groupAmount.div(localAmount));
									vo.setExcrate3(format(groupAmount.div(localAmount), 4));
									// 20161227_7 end
								}
							} else if (groupAmount.doubleValue() == 0.0D) {
								vo.setExcrate3(UFDouble.ZERO_DBL);
							} else
								vo.setExcrate3(localAmount.div(groupAmount));
						} else {
							if ((this.localCurrtype != null) && (!this.localCurrtype.equals(this.groupCurrtype)))
								currinfo = Currency.getCurrRateInfo(pk_corp, this.localCurrtype, this.groupCurrtype);
							// 20161227_8 tsy 同20161227_3修改获取折算模式的判断逻辑
							// boolean groupCurrConvmode = currinfo == null;
							boolean groupCurrConvmode = (currinfo == null ? true : currinfo.getConvmode() == 0);
							// 20161227_8 end
							if (groupCurrConvmode) {
								if (Amount.doubleValue() == 0.0D) {
									vo.setExcrate3(UFDouble.ZERO_DBL);
								} else {
									// 20161227_9 tsy 同20161227_4汇率进行四舍五入
									// vo.setExcrate3(groupAmount.div(Amount));
									vo.setExcrate3(format(groupAmount.div(Amount), 4));
									// 20161227_9 end
								}
							} else if (groupAmount.doubleValue() == 0.0D) {
								vo.setExcrate3(UFDouble.ZERO_DBL);
							} else {
								// 20161227_10 tsy 同20161227_4汇率进行四舍五入
								// vo.setExcrate3(Amount.div(groupAmount));
								vo.setExcrate3(format(Amount.div(groupAmount), 4));
								// 20161227_10 end
							}
						}
					}
					if (Currency.isStartGlobalCurr()) {
						currinfo = null;
						if (!Currency.isGlobalRawConvertModel(null)) {
							if ((this.localCurrtype != null) && (!this.localCurrtype.equals(this.globalCurrtype)))
								currinfo = Currency.getCurrRateInfo(pk_corp, this.localCurrtype, this.globalCurrtype);
							// 20161227_11 tsy 同20161227_3修改获取折算模式的判断逻辑
							// boolean globalCurrConvmode = currinfo == null;
							boolean globalCurrConvmode = (currinfo == null ? true : currinfo.getConvmode() == 0);
							// 20161227_11 end
							if (globalCurrConvmode) {
								if (localAmount.doubleValue() == 0.0D) {
									vo.setExcrate4(UFDouble.ZERO_DBL);
								} else {
									// 20161227_12 tsy 同20161227_4汇率进行四舍五入
									// vo.setExcrate4(globalAmount.div(localAmount));
									vo.setExcrate4(format(globalAmount.div(localAmount), 4));
									// 20161227_12 end
								}
							} else if (globalAmount.doubleValue() == 0.0D) {
								vo.setExcrate4(UFDouble.ZERO_DBL);
							} else {
								// 20161227_13 tsy 同20161227_4汇率进行四舍五入
								// vo.setExcrate4(localAmount.div(globalAmount));
								vo.setExcrate4(format(localAmount.div(globalAmount), 4));
								// 20161227_13 end
							}
						} else {
							if ((this.localCurrtype != null) && (!this.localCurrtype.equals(this.globalCurrtype)))
								currinfo = Currency.getCurrRateInfo(pk_corp, this.localCurrtype, this.globalCurrtype);
							// 20161227_14 tsy 同20161227_3修改获取折算模式的判断逻辑
							// boolean globalCurrConvmode = currinfo == null;
							boolean globalCurrConvmode = (currinfo == null ? true : currinfo.getConvmode() == 0);
							// 20161227_14 end
							if (globalCurrConvmode) {
								if (Amount.doubleValue() == 0.0D) {
									vo.setExcrate4(UFDouble.ZERO_DBL);
								} else {
									// 20161227_15 tsy 同20161227_4汇率进行四舍五入
									// vo.setExcrate4(globalAmount.div(Amount));
									vo.setExcrate4(format(globalAmount.div(Amount), 4));
									// 20161227_15 end
								}
							} else if (globalAmount.doubleValue() == 0.0D) {
								vo.setExcrate4(UFDouble.ZERO_DBL);
							} else {
								// 20161227_16 tsy 同20161227_4汇率进行四舍五入
								// vo.setExcrate4(Amount.div(globalAmount));
								vo.setExcrate4(format(Amount.div(globalAmount), 4));
								// 20161227_16 end
							}
						}
					}

					if ((vo.getExcrate2() != null) && (vo.getExcrate2().compareTo(new UFDouble(0)) < 0))
						vo.setExcrate2(vo.getExcrate2().multiply(-1.0D));
					if ((vo.getExcrate3() != null) && (vo.getExcrate3().compareTo(new UFDouble(0)) < 0))
						vo.setExcrate3(vo.getExcrate3().multiply(-1.0D));
					if ((vo.getExcrate4() != null) && (vo.getExcrate4().compareTo(new UFDouble(0)) < 0)) {
						vo.setExcrate4(vo.getExcrate4().multiply(-1.0D));
					}
				}
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new GlBusinessException(e.getMessage());
		}
		return vecDetailVO;
	}

	private Vector calculatePrice(Vector vecDetailVO) {
		for (int i = 0; i < vecDetailVO.size(); i++) {
			DetailVO vo = (DetailVO) vecDetailVO.elementAt(i);

			UFDouble Amount = vo.getDebitamount().equals(new UFDouble(0)) ? vo.getCreditamount() : vo.getDebitamount();
			UFDouble quantity = vo.getDebitquantity().equals(new UFDouble(0)) ? vo.getCreditquantity() : vo.getDebitquantity();
			if ((Amount.compareTo(new UFDouble(0)) != 0) && (quantity.compareTo(new UFDouble(0)) != 0)) {
				UFDouble price = Amount.div(quantity);
				if (price.compareTo(new UFDouble(0)) < 0) {
					vo.setPrice(price.multiply(-1.0D));
				} else {
					vo.setPrice(price);
				}
			}
		}

		return vecDetailVO;
	}

	private void changeCEToResult(DetailVO[] detailvos, DetailVO vo) {
		try {
			for (int i = 0; i < detailvos.length; i++) {
				DetailVO voCE = detailvos[i];
				voCE.setPk_detail(null);
				voCE.setAccsubjcode(null);
				voCE.setAssid(vo.getAssid());
				voCE.setExplanation(vo.getExplanation());
				voCE.setPk_accasoa(vo.getPk_accasoa());
				voCE.setDetailindex(vo.getDetailindex());

				if ((vo.getPk_currtype() != null) && (vo.getPk_currtype().equals("本币"))) {
					voCE.setDebitamount(voCE.getLocaldebitamount());
					voCE.setCreditamount(voCE.getLocalcreditamount());
				}

			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	private void changeCEToResult(Vector<DetailVO> detail, Vector partdetailvo, int location) {
		try {
			DetailVO vo1 = (DetailVO) detail.elementAt(location);
			for (int i = 0; i < partdetailvo.size(); i++) {
				DetailVO voCE = (DetailVO) partdetailvo.elementAt(i);
				voCE.setPk_detail(null);
				voCE.setAccsubjcode(null);
				voCE.setAssid(vo1.getAssid());
				voCE.setExplanation(vo1.getExplanation());
				voCE.setPk_accasoa(vo1.getPk_accasoa());

				voCE.setDetailindex(((DetailVO) detail.elementAt(location)).getDetailindex());
				if ((vo1.getPk_currtype() != null) && (vo1.getPk_currtype().equals("本币"))) {
					voCE.setDebitamount(voCE.getLocaldebitamount());
					voCE.setCreditamount(voCE.getLocalcreditamount());
				}
			}

			detail.remove(location);
			detail.addAll(location, partdetailvo);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	private void checkCEDefine(Vector detail) throws Exception {
		if ((detail == null) || (detail.size() == 0)) {
			return;
		}

		DetailVO ceExpress = null;
		for (int i = 0; i < detail.size(); i++) {
			DetailVO vo = (DetailVO) detail.elementAt(i);
			if (isCEFunction(vo)) {
				if (ceExpress == null) {
					ceExpress = vo;
				} else if ((getCEProp(ceExpress) == null) || (getCEProp(vo) == null)) {
					throw new Exception(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000228"));
				}
			}
		}
	}

	private Vector compute(Vector vecDetail, DetailVO cevo, int location) throws Exception {
		if ((vecDetail == null) || (vecDetail.size() == 0) || (vecDetail.size() == 1))
			return null;
		if (cevo == null) {
			return null;
		}
		Vector<DetailVO> vecDetails = new Vector();
		for (int i = 0; i < vecDetail.size(); i++) {
			vecDetails.addElement((DetailVO) ((DetailVO) vecDetail.elementAt(i)).clone());
		}

		vecDetails.removeElementAt(location);
		for (int i = 0; i < vecDetails.size(); i++) {
			((IVoAccess) vecDetails.elementAt(i)).setUserData(null);
		}

		CGenTool genTool = new CGenTool();
		int[] intSortIndex = null;

		if (!cevo.getPk_currtype().equals("本币")) {
			if (getEnvVO().isSupportUnit()) {
				intSortIndex = new int[] { 104, 607 };
			} else {
				intSortIndex = new int[] { 104 };
			}
		} else if (getEnvVO().isSupportUnit()) {
			intSortIndex = new int[] { 607 };
		}

		genTool.setSortIndex(intSortIndex);
		if (intSortIndex != null)
			genTool.setLimitSumGen(intSortIndex.length - 1);
		CSumTool sumTool = new CSumTool();
		int[] sumIndex = { 114, 113, 115, 116, 118, 117, 119, 120, 324, 325, 326, 327 };

		sumTool.setSumIndex(sumIndex);

		COutputTool outputTool = new COutputTool();
		String[] strSummary = { NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000229") };

		String[] strInitSummary = { NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000230") };

		outputTool.setSimpleSummary(true);
		outputTool.setInitSummary(strInitSummary);
		outputTool.setSummary(strSummary);
		outputTool.setSummaryCol(109);

		outputTool.setRequireOutputDetail(false);

		CDataSource datasource = new CDataSource();
		if (intSortIndex == null) {
			datasource.setSumVector(vecDetails);
		} else {
			datasource.setSumVector(CDataSource.sortVector(vecDetails, genTool, false));
		}
		CIntelVO tt = new CIntelVO();
		if ((!cevo.getPk_currtype().equals("本币")) || (getEnvVO().isSupportUnit())) {
			tt.setSumTool(sumTool);
		} else {
			tt.setTotalSumTool(sumTool);
		}
		try {
			if ((!cevo.getPk_currtype().equals("本币")) || (getEnvVO().isSupportUnit())) {
				tt.setGenTool(genTool);
			}
			tt.setDatasource(datasource);
			tt.setOutputTool(outputTool);

			Vector recVector = tt.getResultVector();

			for (int i = 0; i < recVector.size(); i++) {
				DetailVO dvo = (DetailVO) recVector.elementAt(i);
				dvo.interChangeDC();

				if (cevo.getUserData() == null) {
					if (dvo.getDebitamount().compareTo(dvo.getCreditamount()) >= 0) {
						dvo.setLocaldebitamount(dvo.getLocaldebitamount().sub(dvo.getLocalcreditamount()));
						dvo.setGroupdebitamount(dvo.getGroupdebitamount().sub(dvo.getGroupcreditamount()));
						dvo.setGlobaldebitamount(dvo.getGlobaldebitamount().sub(dvo.getGlobalcreditamount()));
						dvo.setDebitquantity(dvo.getDebitquantity().sub(dvo.getCreditquantity()));
						dvo.setLocalcreditamount(new UFDouble(0));
						dvo.setGroupcreditamount(new UFDouble(0));
						dvo.setGlobalcreditamount(new UFDouble(0));
						if (cevo.getPk_currtype().equals(CurrTypeConst.QUERY_LOC_CURRTYPE())) {

							dvo.setPk_currtype(this.localCurrtype);
							dvo.setDebitamount(dvo.getLocaldebitamount());
							dvo.setCreditamount(dvo.getLocaldebitamount());
						} else if (cevo.getPk_currtype().equals(CurrTypeConst.ALL_CURRTYPE())) {
							dvo.setDebitamount(dvo.getDebitamount().sub(dvo.getCreditamount()));
							dvo.setCreditamount(new UFDouble(0));
						} else if (dvo.getPk_currtype().equals(cevo.getPk_currtype())) {
							dvo.setPk_currtype(cevo.getPk_currtype());
							dvo.setDebitamount(dvo.getDebitamount().sub(dvo.getCreditamount()));
							dvo.setCreditamount(new UFDouble(0));

						}

					} else {

						dvo.setLocalcreditamount(dvo.getLocalcreditamount().sub(dvo.getLocaldebitamount()));
						dvo.setGroupcreditamount(dvo.getGroupcreditamount().sub(dvo.getGroupdebitamount()));
						dvo.setGlobalcreditamount(dvo.getGlobalcreditamount().sub(dvo.getGlobaldebitamount()));
						dvo.setCreditquantity(dvo.getCreditquantity().sub(dvo.getDebitquantity()));
						dvo.setLocaldebitamount(new UFDouble(0));
						dvo.setGroupdebitamount(new UFDouble(0));
						dvo.setGlobaldebitamount(new UFDouble(0));
						if (cevo.getPk_currtype().equals(CurrTypeConst.QUERY_LOC_CURRTYPE())) {
							dvo.setPk_currtype(this.localCurrtype);
							dvo.setCreditamount(dvo.getLocalcreditamount());
							dvo.setDebitamount(new UFDouble(0));
						} else if (cevo.getPk_currtype().equals(CurrTypeConst.ALL_CURRTYPE())) {
							dvo.setCreditamount(dvo.getCreditamount().sub(dvo.getDebitamount()));
							dvo.setDebitamount(new UFDouble(0));
						} else if (dvo.getPk_currtype().equals(cevo.getPk_currtype())) {
							dvo.setPk_currtype(cevo.getPk_currtype());
							dvo.setCreditamount(dvo.getCreditamount().sub(dvo.getDebitamount()));
							dvo.setDebitamount(new UFDouble(0));

						}

					}

				} else if (((UFBoolean) cevo.getUserData()).booleanValue()) {
					dvo.setLocaldebitamount(dvo.getLocaldebitamount().sub(dvo.getLocalcreditamount()));
					dvo.setGroupdebitamount(dvo.getGroupdebitamount().sub(dvo.getGroupcreditamount()));
					dvo.setGlobaldebitamount(dvo.getGlobaldebitamount().sub(dvo.getGlobalcreditamount()));
					dvo.setDebitquantity(dvo.getDebitquantity().sub(dvo.getCreditquantity()));
					dvo.setLocalcreditamount(new UFDouble(0));
					dvo.setGroupcreditamount(new UFDouble(0));
					dvo.setGlobalcreditamount(new UFDouble(0));

					dvo.setCreditquantity(new UFDouble(0));
					if (cevo.getPk_currtype().equals(CurrTypeConst.QUERY_LOC_CURRTYPE())) {
						dvo.setPk_currtype(this.localCurrtype);
						dvo.setDebitamount(dvo.getLocaldebitamount());
						dvo.setCreditamount(new UFDouble(0));
					} else if (cevo.getPk_currtype().equals(CurrTypeConst.ALL_CURRTYPE())) {
						dvo.setDebitamount(dvo.getDebitamount().sub(dvo.getCreditamount()));
						dvo.setCreditamount(new UFDouble(0));
					} else if (dvo.getPk_currtype().equals(cevo.getPk_currtype())) {
						dvo.setPk_currtype(cevo.getPk_currtype());
						dvo.setDebitamount(dvo.getDebitamount().sub(dvo.getCreditamount()));
						dvo.setCreditamount(new UFDouble(0));
					}

				} else {
					dvo.setLocalcreditamount(dvo.getLocalcreditamount().sub(dvo.getLocaldebitamount()));
					dvo.setGroupcreditamount(dvo.getGroupcreditamount().sub(dvo.getGroupdebitamount()));
					dvo.setGlobalcreditamount(dvo.getGlobalcreditamount().sub(dvo.getGlobaldebitamount()));
					dvo.setCreditquantity(dvo.getCreditquantity().sub(dvo.getDebitquantity()));
					dvo.setLocaldebitamount(new UFDouble(0));
					dvo.setGroupdebitamount(new UFDouble(0));
					dvo.setGlobaldebitamount(new UFDouble(0));
					if (cevo.getPk_currtype().equals(CurrTypeConst.QUERY_LOC_CURRTYPE())) {

						dvo.setPk_currtype(this.localCurrtype);
						dvo.setCreditamount(dvo.getLocalcreditamount());
						dvo.setDebitamount(new UFDouble(0));
					} else if (cevo.getPk_currtype().equals(CurrTypeConst.ALL_CURRTYPE())) {
						dvo.setCreditamount(dvo.getCreditamount().sub(dvo.getDebitamount()));
						dvo.setDebitamount(new UFDouble(0));
					} else if (dvo.getPk_currtype().equals(cevo.getPk_currtype())) {
						dvo.setPk_currtype(cevo.getPk_currtype());
						dvo.setCreditamount(dvo.getCreditamount().sub(dvo.getDebitamount()));
						dvo.setDebitamount(new UFDouble(0));
					}
				}
			}

			return recVector;
		} catch (Throwable e) {
			Logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
	}

	private void display(String message) {
	}

	public void excuteTransferDetail(TransferDetailVO vo, Vector<DetailVO> allDetail, int iIndex) throws Exception {
		getEnvVO().setPk_accasoa(vo.getPk_accasoa());
		getEnvVO().setPk_currtype(vo.getPk_currtype());
		getEnvVO().setOrientation(vo.getOrientation());
		getEnvVO().setNote(vo.getNote());
		try {
			getEnvVO().setAssvoTrans(in_subjAss(vo.getPk_accasoa(), vo.getAss(), getEnvVO().getPk_orgbook()));

		} catch (Exception e) {

			Logger.error(e.getMessage(), e);
			throw new GlBusinessException(e.getMessage());
		}

		getFormulaExpression().setEnvVO(this.envVO);
		getFormulaExpression().setTransferdetailvo(vo);
		getFormulaExpression().setSelfDefNames(getSelfDefFunnames());
		getFormulaExpression().setH_NameToVo(getH_NameToVOs());

		if (getFormulaExpression().getVecDetailVOs() != null) {
			getFormulaExpression().getVecDetailVOs().removeAllElements();
		}
		try {
			getFormulaExpression().genDetail();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new GlBusinessException(e.getMessage());
		}
		Vector vecVOs = getFormulaExpression().getVecDetailVOs();
		int details = vecVOs == null ? 0 : vecVOs.size();

		for (int i = 0; i < details; i++) {
			((DetailVO) vecVOs.elementAt(i)).setDetailindex(Integer.valueOf(iIndex));
		}
		display(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000234"));

		if (details > 0) {
			allDetail.addAll(vecVOs);
		}
	}

	private DetailVO[] fillDetailByTransfer(TransferVO transfervo) throws TransferDetailException, Exception {
		output(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000235"));

		display(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000236"));

		TransferDetailVO[] TDvos = null;

		TransferDetailVO[] finalDetails = null;

		Vector allDetail = new Vector();
		try {
			TransferDetailVO vo = new TransferDetailVO();
			vo.setPk_transfer(transfervo.getPk_transfer());

			TDvos = (TransferDetailVO[]) this.htTransDetails.get(transfervo.getPk_transfer());

			DetailSpread detailspread = new DetailSpread();

			detailspread.setPk_orgbook(transfervo.getPk_glorgbook());
			detailspread.setYear(this.envVO.getAccYear());
			detailspread.setPeriod(this.envVO.getAccMouth());
			detailspread.setDate(this.envVO.getDate());

			if (TDvos.length == 0) {
				display(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000237"));
			}

			finalDetails = detailspread.spreadDetails(TDvos);

		} catch (Exception e) {

			output("fillDetailByTransfer:" + e);
			Logger.error(e.getMessage(), e);
			throw new TransferDetailException();
		}
		if ((finalDetails == null) || (finalDetails.length == 0)) {
			throw new TransferDetailException(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000238"));
		}

		for (int i = 0; i < finalDetails.length; i++) {

			display(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000239", null, new String[] { String.valueOf(i + 1) }));

			try {
				finalDetails[i].setPk_busiunit(transfervo.getPk_busiunit());
				excuteTransferDetail(finalDetails[i], allDetail, i);
			} catch (Exception e) {
				throw new TransferDetailException(e.getMessage());
			}
		}

		getDetailAssIDsByAssVOs(allDetail);

		if (transfervo.getAmortizeVO() != null) {

			if (AmortizeRateEnum.ORIENTRATE.toStringValue().equals(transfervo.getAmortizeVO().getAmortizerate())) {
				calculateCurrRate(allDetail, transfervo);
			}

			AmorCycleVO amorCycleVO = transfervo.getAmortizeVO().getAmorCycleVO();
			AmorStrategy strategy = AmorStrategy.getStrategy(amorCycleVO.getCycletype());
			setRatePrecision(allDetail);
			strategy.setHtCurAndRate(this.htCurAndRate);
			strategy.generateQuantity(transfervo.getAmortizeVO(), (DetailVO[]) allDetail.toArray(new DetailVO[0]), this.envVO);
			strategy.generateAmout(transfervo.getAmortizeVO(), (DetailVO[]) allDetail.toArray(new DetailVO[0]), this.envVO);
		}

		if (allDetail != null) {
			return afterFillDetail(allDetail, transfervo);
		}
		return null;
	}

	private VoucherVO fillHeadByTransfer(TransferVO transfervo) throws TransferHeadException, Exception {
		getEnvVO().setPk_system(transfervo.getPk_system());

		TransferHeadException the = null;
		VoucherVO vouchervo = new VoucherVO();
		vouchervo.setNo(Integer.valueOf(0));

		vouchervo.setPk_accountingbook(this.envVO.getPk_orgbook());

		vouchervo.setPk_prepared(this.envVO.getPk_operator());

		if (("GLRC".equals(transfervo.getPk_system())) && ("00".equals(this.envVO.getAccMouth()))) {
			AccountCalendar calendar = CalendarUtilGL.getAccountCalendarByAccountBook(this.envVO.getPk_orgbook());
			calendar.setDate(this.envVO.getDate());
			vouchervo.setPeriod("00");
			vouchervo.setYear(this.envVO.getAccYear());
			vouchervo.setPrepareddate(new UFDate("0000-01-01", false));
			vouchervo.setModifyflag("YYY");
		} else if ((this.envVO.getAccMouth() != null) && (this.envVO.getAccMouth().length() == 3)) {
			AccountCalendar calendar = CalendarUtilGL.getAccountCalendarByAccountBook(this.envVO.getPk_orgbook());
			calendar.set(calendar.getYearVO().getPeriodyear(), this.envVO.getAccMouth().substring(0, 2));

			vouchervo.setM_adjustperiod(this.envVO.getAccMouth());
			vouchervo.setYear(calendar.getYearVO().getPeriodyear());
			vouchervo.setPeriod(this.envVO.getAccMouth().substring(0, 2));
			vouchervo.setPrepareddate(calendar.getMonthVO().getEnddate());
			vouchervo.setModifyflag("NYY");
		} else {
			AccountCalendar calendar = CalendarUtilGL.getAccountCalendarByAccountBook(this.envVO.getPk_orgbook());
			calendar.setDate(this.envVO.getDate());
			vouchervo.setPrepareddate(this.envVO.getDate());
			vouchervo.setPeriod(calendar.getMonthVO().getAccperiodmth());
			vouchervo.setYear(calendar.getYearVO().getPeriodyear());
			vouchervo.setModifyflag("YYY");
		}

		vouchervo.setPk_vouchertype(transfervo.getPk_vouchertype());
		vouchervo.setAttachment(transfervo.getBillnum() == null ? Integer.valueOf(0) : transfervo.getBillnum());
		vouchervo.setDetailmodflag(UFBoolean.TRUE);
		vouchervo.setUserData(transfervo.getPk_transfer());

		if ("GLRC".equals(transfervo.getPk_system())) {
			vouchervo.setVoucherkind(Integer.valueOf(5));
		} else if ("PLCF".equals(transfervo.getPk_system())) {
			vouchervo.setVoucherkind(Integer.valueOf(4));
		} else {
			vouchervo.setVoucherkind(Integer.valueOf(0));
		}
		vouchervo.setTotalcredit(new UFDouble(0));
		vouchervo.setTotaldebit(new UFDouble(0));

		vouchervo.setDiscardflag(UFBoolean.FALSE);

		vouchervo.setPk_system(transfervo.getPk_system());

		if (vouchervo.getPk_accountingbook() != null) {
			String pk_org = AccountBookUtil.getPk_orgByAccountBookPk(vouchervo.getPk_accountingbook());
			vouchervo.setPk_org(pk_org);
			transfervo.setPk_busiunit(pk_org);
			HashMap<String, String> versionMap = null;
			try {
				versionMap = GlOrgUtils.getNewVIDSByOrgIDSAndDate(new String[] { pk_org }, vouchervo.getPrepareddate());
			} catch (BusinessException e) {
				Logger.error(e.getMessage(), e);
			}
			if (versionMap != null) {
				vouchervo.setPk_org_v((String) versionMap.get(pk_org));
			}
		}
		try {
			vouchervo.setDetails(fillDetailByTransfer(transfervo));
			if ((vouchervo.getDetails() == null) || (vouchervo.getDetails().length == 0)) {
				return null;
			}
			vouchervo.setExplanation(vouchervo.getDetail(0).getExplanation());
			UFDouble dblDebit = new UFDouble(0);
			UFDouble dblCredit = new UFDouble(0);

			for (int i = 0; i < vouchervo.getDetails().length; i++) {
				if (vouchervo.getDetails()[i].getLocaldebitamount() == null)
					vouchervo.getDetails()[i].setLocaldebitamount(new UFDouble(0));
				dblDebit = dblDebit.add(vouchervo.getDetails()[i].getLocaldebitamount());
				dblCredit = dblCredit.add(vouchervo.getDetails()[i].getLocalcreditamount());
			}
			vouchervo.setTotaldebit(dblDebit);
			vouchervo.setTotalcredit(dblCredit);

			TransferHistoryVO aHistoryVO = new TransferHistoryVO();

			aHistoryVO.setPk_transfer(transfervo.getPk_transfer());
			aHistoryVO.setTransferType(NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55", "UPP2002gl55-000389"));

			aHistoryVO.setYear(getEnvVO().getAccYear());
			aHistoryVO.setPeriod(getEnvVO().getAccMouth());
			aHistoryVO.setPk_glorgbook(getEnvVO().getPk_orgbook());

			vouchervo.setAddclass("nc.bs.gl.transrate.TransferVoucherCallBack");
			vouchervo.setDeleteclass("nc.bs.gl.transrate.TransferVoucherCallBack");
			vouchervo.setUserData(aHistoryVO);
		} catch (TransferDetailException te) {
			output(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000241") + te.toString());

			the = new TransferHeadException(te.getLocalizedMessage());

			Logger.error(the.getMessage(), the);
			throw the;
		}

		return vouchervo;
	}

	private UFDouble format(UFDouble ufdbl, int digits) {
		if (ufdbl == null)
			return null;
		if (ufdbl.compareTo(new UFDouble(0.0D)) == 0) {
			return new UFDouble(0);
		}
		return ufdbl.setScale(digits, 4);
	}

	public void generateVoucher() throws nc.vo.gl.exception.TransferException, Exception {
		if (getVTransferDetails().size() == 0) {
			return;
		}

		Vector<VoucherVO> vos = new Vector();
		long t0 = System.currentTimeMillis();
		TransferDetailVO[] detailvos = new TransferDetailVO[getVTransferDetails().size()];
		getVTransferDetails().copyInto(detailvos);
		getEnvVO().setPap(getPap());

		if (!isBlTransOneByOne()) {
			try {
				prepareTransact(detailvos);
				long tt = System.currentTimeMillis();
				outPutTimes(t0, tt, NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000242"));

			} catch (Exception e) {

				display(e.getMessage());
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getMessage());
			}
		}

		display(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000243"));

		for (int i = 0; i < getTransfervo().length; i++) {
			VoucherVO vo = null;
			try {
				display("");
				display(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000244") + this.transfervo[i].getTransferno()
						+ NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000245") + this.transfervo[i].getNote());

				vo = fillHeadByTransfer(this.transfervo[i]);
				if (getIsLocalOnly().booleanValue()) {

					for (int j = 0; (vo != null) && (j < vo.getDetails().length); j++) {
						vo.getDetails()[j].setFraccreditamount(new UFDouble(0));
						vo.getDetails()[j].setFracdebitamount(new UFDouble(0));
					}
				}
			} catch (TransferHeadException e) {
				Logger.error(e.getMessage(), e);
				throw new GlBusinessException(e.getMessage());
			} catch (Exception ex) {
				display(ex.getLocalizedMessage());
				display(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000246"));

				Logger.error(ex.getMessage(), ex);
				throw new GlBusinessException(ex.getMessage());
			}
			if (vo != null) {
				vos.addElement(vo);
			}
		}
		long t1 = System.currentTimeMillis();
		outPutTimes(t0, t1, NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000247"));

		if (vos.size() > 0) {
			this.voucherHead = new VoucherVO[0];
			setVoucherHead((VoucherVO[]) vos.toArray(this.voucherHead));
		} else {
			display(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000195"));

			setVoucherHead(null);
		}

		long t2 = System.currentTimeMillis();
		outPutTimes(t1, t2, NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000248"));
	}

	public void genRstForQueryVOs(GlQueryVO[] qryvos) throws Exception {
		if ((qryvos == null) || (qryvos.length == 0))
			return;
		QueryVOContainer qryVOCont = new QueryVOContainer();
		Vector<GlQueryVO> vfinalQueryvos = new Vector();
		ArrayList<GlBalanceVO[]> finalResult = new ArrayList(qryvos.length);

		Vector vNothisAss = new Vector();
		Vector vLegelAss = null;
		vLegelAss = getTheGroupAssIDs(qryvos, vNothisAss);

		if (vNothisAss.size() > 0) {
			for (int i = 0; i < vNothisAss.size(); i++) {
				vfinalQueryvos.addElement((GlQueryVO) vNothisAss.elementAt(i));
				finalResult.add(null);
			}
		}
		GlQueryVO[] legelQueryVOs = null;
		if (vLegelAss.size() > 0) {
			legelQueryVOs = new GlQueryVO[vLegelAss.size()];
			vLegelAss.copyInto(legelQueryVOs);

			GroupFunctionVO[] groupvos = new GroupFunctionTool().getQryVOGrpsByAccsubj(legelQueryVOs);
			if ((groupvos != null) && (groupvos.length > 0)) {
				for (int i = 0; i < groupvos.length; i++) {
					GlQueryVO[] tempqueryvos = null;
					if ((groupvos[i].getVFunctions() != null) && (groupvos[i].getVFunctions().size() > 0)) {
						tempqueryvos = new GlQueryVO[groupvos[i].getVFunctions().size()];
						groupvos[i].getVFunctions().copyInto(tempqueryvos);
						QueryVOContainer tempContainer = genTheGroupRstForQueryVOs(tempqueryvos);
						for (int j = 0; j < tempContainer.getQueryvos().length; j++) {
							vfinalQueryvos.addElement(tempContainer.getQueryvos()[j]);
							finalResult.add(tempContainer.getBalaceVOsAt(j));
						}
					}
				}
			}
		}
		GlQueryVO[] finaleQryVOs = new GlQueryVO[vfinalQueryvos.size()];
		vfinalQueryvos.copyInto(finaleQryVOs);
		qryVOCont.setQueryvos(finaleQryVOs);
		qryVOCont.setAryList(finalResult);
		getEnvVO().setQryVOsContainer(qryVOCont);
	}

	public Vector getAllDetails(TransferDetailVO[] vos) {
		if ((vos == null) || (vos.length == 0)) {
			return null;
		}
		Vector<String> v = new Vector();
		for (int i = 0; i < vos.length; i++) {
			v.addElement(vos[i].getAmountFormula());

			if ((vos[i].getAss() != null) && (vos[i].getAss().trim().length() != 0)) {
				this.vResultAss.addElement(vos[i].getAss());
			}
		}

		return v;
	}

	private int getCEAmount(Vector v) {
		if (v.size() == 0) {
			return 0;
		}
		int iAmount = 0;
		for (int i = 0; i < v.size(); i++) {
			DetailVO vo = (DetailVO) v.elementAt(i);
			if ((vo.getAccsubjcode() != null) && (isCEFunction(vo))) {
				iAmount++;
			}
		}
		return iAmount;
	}

	private Vector<DetailVO> getCEDetail(Vector detail) throws Exception {
		if (detail == null)
			return null;
		removeUselessCEs(detail);
		int location = locateCEPosition(detail);
		if (location < 0) {
			return detail;
		}
		try {
			checkCEDefine(detail);

			if (getCEProp((DetailVO) detail.elementAt(location)) == null) {
				Vector partdetailvo = null;
				try {
					partdetailvo = compute(detail, (DetailVO) detail.elementAt(location), location);
				} catch (Exception e) {
					display(e.getLocalizedMessage());

					Logger.error(e.getMessage(), e);
				}

				if ((partdetailvo != null) && (partdetailvo.size() != 0)) {
					changeCEToResult(detail, partdetailvo, location);
				}
				return detail;
			}

			int iDetailCounts = getCEDetailCounts(detail);
			ArrayList<DetailVO[]> aryList = new ArrayList(iDetailCounts);
			for (int i = 0; i < iDetailCounts; i++) {
				aryList.add(null);
			}

			int iCECounter = -1;
			int i = 0;
			Vector<DetailVO> vTempDetail = new Vector();
			DetailVO vo = null;
			while (i < detail.size()) {
				DetailVO thisDetailvo = (DetailVO) detail.elementAt(i);
				if (!isCEFunction(thisDetailvo)) {
					vTempDetail.addElement(thisDetailvo);
					i++;
				} else {
					if (vo != null) {
						if (vTempDetail.size() > 0) {
							Vector<DetailVO> v = (Vector) vTempDetail.clone();
							v.insertElementAt(vo, 0);
							DetailVO[] aryDetails = getThisCEDetail(v, 0);
							iCECounter += 1;
							aryList.set(iCECounter, aryDetails);
						} else {
							DetailVO tmpvo = new DetailVO();
							DetailVO[] aryDetails = new DetailVO[1];
							aryDetails[0] = tmpvo;
							iCECounter += 1;
							aryList.set(iCECounter, aryDetails);
						}
						vo = null;
					}

					if (getCEProp(thisDetailvo).trim().toUpperCase().equals("UP")) {
						if (vTempDetail.size() > 0) {
							Vector<DetailVO> v = (Vector) vTempDetail.clone();
							v.addElement(thisDetailvo);

							DetailVO[] aryDetails = getThisCEDetail(v, v.size() - 1);
							iCECounter += 1;
							aryList.set(iCECounter, aryDetails);
						} else {
							DetailVO tmpvo = new DetailVO();
							DetailVO[] aryDetails = new DetailVO[1];
							aryDetails[0] = tmpvo;
							iCECounter += 1;
							aryList.set(iCECounter, aryDetails);
						}
					} else {
						vo = (DetailVO) thisDetailvo.clone();
					}

					vTempDetail.removeAllElements();
					i++;
				}
			}

			if ((vo != null) && (vTempDetail != null) && (vTempDetail.size() > 0)) {
				Vector<DetailVO> v = (Vector) vTempDetail.clone();
				v.insertElementAt(vo, 0);
				DetailVO[] aryDetails = getThisCEDetail(v, 0);
				iCECounter += 1;
				aryList.set(iCECounter, aryDetails);
			}

			return getCEDetailFromAryList(aryList, detail);

		} catch (Exception e) {

			Logger.error(e.getMessage(), e);
			throw new GlBusinessException(e.getMessage());
		}
	}

	private int getCEDetailCounts(Vector detail) {
		int iCount = 0;
		for (int i = 0; i < detail.size(); i++) {
			if (isCEFunction((DetailVO) detail.elementAt(i))) {
				iCount += 1;
			}
		}
		return iCount;
	}

	private Vector<DetailVO> getCEDetailFromAryList(ArrayList aryList, Vector detail) throws Exception {
		Vector<DetailVO> v = (Vector) detail.clone();

		int iCount = getCEDetailCounts(detail);

		if (iCount > 0) {
			int[] positions = new int[iCount];
			int pos = 0;
			for (int i = 0; i < detail.size(); i++) {
				if (isCEFunction((DetailVO) detail.elementAt(i))) {
					positions[pos] = i;
					pos += 1;
				}
			}

			for (int i = positions.length - 1; i >= 0; i--) {
				if (aryList.get(i) != null) {
					DetailVO[] vos = (DetailVO[]) aryList.get(i);
					if ((vos != null) && (vos.length > 0)) {

						changeCEToResult(vos, (DetailVO) v.elementAt(positions[i]));
						v.removeElementAt(positions[i]);
						for (int k = vos.length - 1; k >= 0; k--) {
							v.insertElementAt(vos[k], positions[i]);
						}
					}
				}
			}
		}

		return v;
	}

	private String getCEProp(DetailVO vo) {
		String retvalue = null;
		if ((vo.getAccsubjcode() != null) && (vo.getAccsubjcode().toUpperCase().indexOf("CE") == 0)) {
			int bpos = vo.getAccsubjcode().indexOf("(");
			int epos = vo.getAccsubjcode().indexOf(")");
			retvalue = vo.getAccsubjcode().substring(bpos + 1, epos);
			if ((retvalue == null) || (retvalue.trim().length() == 0))
				return null;
			String mark = "\"\"";
			if (retvalue.equals(mark))
				return null;
		}
		return retvalue;
	}

	public void getCurrInfo() {
		try {
			CurrtypeVO[] vo = Currency.queryAll(getPk_accountingbook());
			if ((vo != null) && (vo.length > 0)) {
				for (int i = 0; i < vo.length; i++) {
					String key = vo[i].getPk_currtype().trim();
					this.htCurAndRate.put(key, vo[i].getCurrdigit());
				}
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	public void getDetailAssIDsByAssVOs(Vector vDetails) {
		long t1 = System.currentTimeMillis();
		if ((vDetails == null) || (vDetails.size() == 0))
			return;
		ArrayList aryListAssVOs = new ArrayList(vDetails.size());
		for (int i = 0; i < vDetails.size(); i++) {
			if (vDetails.elementAt(i) != null) {
				if ((((DetailVO) vDetails.elementAt(i)).getAss() != null) && (((DetailVO) vDetails.elementAt(i)).getAss().length != 0)) {
					aryListAssVOs.add(i, ((DetailVO) vDetails.elementAt(i)).getAss());
				} else {
					aryListAssVOs.add(i, null);
				}
			} else {
				aryListAssVOs.add(null);
			}
		}
		String[] retids = null;
		try {
			retids = GLPubProxy.getRemoteFreevaluePub().getAssSigleIDs(aryListAssVOs, getEnvVO().getPk_group(), Module.GL);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		for (int i = 0; i < vDetails.size(); i++) {
			if (vDetails.elementAt(i) != null) {
				((DetailVO) vDetails.elementAt(i)).setAssid(retids[i]);
				((DetailVO) vDetails.elementAt(i)).setAss(null);
			}
		}
		long t2 = System.currentTimeMillis();
		UFDouble differ = new UFDouble(t2).sub(new UFDouble(t1));
		differ = differ.div(1000.0D);

		log.info("**************************************************");
		log.info(NCLangRes4VoTransl.getNCLangRes().getStrByID("public20111017_0", "02002001-0016") + differ
				+ NCLangRes4VoTransl.getNCLangRes().getStrByID("public20111017_0", "02002001-0015"));

		log.info("**************************************************");
	}

	public TransferEnvVO getEnvVO() {
		return this.envVO;
	}

	public FormulaExpression getFormulaExpression() {
		if (this.formulaExpression == null)
			this.formulaExpression = new FormulaExpression();
		return this.formulaExpression;
	}

	public nc.vo.glcom.inteltool.IGenToolElement getGenToolElement(Object objKey) {
		return new nc.vo.gl.formula.CAssGenTool();
	}

	public Hashtable getHtTransDetails() {
		return this.htTransDetails;
	}

	public Boolean getIsLocalOnly() {
		return this.isLocalOnly;
	}

	public PrepareAssParse getPap() {
		if (this.pap == null) {
			this.pap = new PrepareAssParse();
		}
		return this.pap;
	}

	public void getQueryVOAllIDs(GlQueryVO[] qryvos) {
	}

	public GlQueryVO[] getQueryVOsFromDetail(TransferDetailVO vo) throws Exception {
		GlQueryVO[] queryvos = null;
		GlQueryVO[] queryvos1 = null;
		GlQueryVO[] queryvos2 = null;

		TransferEnvVO envvo = (TransferEnvVO) getEnvVO().clone();

		if ((getTransfervo() != null) && (getTransfervo().length > 0)) {
			for (TransferVO transferVO : getTransfervo()) {
				if (transferVO.getPk_transfer().equals(vo.getPk_transfer())) {
					envvo.setPk_system(transferVO.getPk_system());
					break;
				}
			}
		}

		envvo.setPk_accasoa(vo.getPk_accasoa());
		envvo.setPk_currtype(vo.getPk_currtype());
		envvo.setOrientation(vo.getOrientation());
		envvo.setNote(vo.getNote());

		envvo.setAssvoTrans(in_subjAss(vo.getPk_accasoa(), vo.getAss(), envvo.getPk_orgbook()));

		envvo.setPap(getPap());

		if ((vo.getAmountFormula() != null) && (!GlStringParse.isFunCE(vo.getAmountFormula()))) {
			queryvos1 = getQueryVOsFromFormula(vo.getAmountFormula(), envvo);
		}
		if ((vo.getQuantityFormula() != null) && (!GlStringParse.isFunCE(vo.getQuantityFormula()))) {
			queryvos2 = getQueryVOsFromFormula(vo.getQuantityFormula(), envvo);
		}
		int iLenth = 0;
		if (queryvos1 != null) {
			if (queryvos2 != null) {
				iLenth = queryvos1.length + queryvos2.length;
			} else {
				iLenth = queryvos1.length;
			}
			queryvos = new GlQueryVO[iLenth];
			for (int i = 0; i < queryvos.length; i++) {
				if (i < queryvos1.length) {
					queryvos[i] = queryvos1[i];
				} else {
					queryvos[i] = queryvos2[(i - queryvos1.length)];
				}
			}
		}
		return queryvos;
	}

	public GlQueryVO[] getQueryVOsFromFormula(String Formula, TransferEnvVO envvo) throws Exception {
		GlQueryVO[] queryvos = null;
		Vector vResult = new Vector();

		Vector vWords = new Compiler(Formula, null, null).getWords();

		String funname = null;
		String accsubj = null;
		String year = null;
		String period = null;
		String ass = null;
		String orientation = null;

		FunctionStruct[] funcs = Compiler.getFunctions(this.funnames, vWords);

		for (int j = 0; (funcs != null) && (j < funcs.length); j++) {
			funname = funcs[j].getFunctionName();

			funname = UFOFunNameTransfer.getCNNameByEng(funname);

			if ((funname != null) && (funname.trim().toUpperCase().charAt(0) == 'S'))
				funname = funname.substring(1, funname.length());
			accsubj = (funcs[j].getParams()[0].getValue() == null) || (funcs[j].getParams()[0].getValue().toString().trim().length() == 0) ? null : funcs[j]
					.getParams()[0].getValue().toString();
			if ((accsubj == null) || (accsubj.indexOf("*") <= -1)) {

				year = (funcs[j].getParams()[2].getValue() == null) || (funcs[j].getParams()[2].getValue().toString().trim().length() == 0) ? null : funcs[j]
						.getParams()[2].getValue().toString();
				period = (funcs[j].getParams()[4].getValue() == null) || (funcs[j].getParams()[4].getValue().toString().trim().length() == 0) ? null : funcs[j]
						.getParams()[4].getValue().toString();
				ass = (funcs[j].getParams()[6].getValue() == null) || (funcs[j].getParams()[6].getValue().toString().trim().length() == 0) ? null : funcs[j]
						.getParams()[6].getValue().toString();

				if ((ass != null) && (ass.indexOf(AssFlag) > 0)) {
					ass = ass.substring(0, ass.indexOf(AssFlag)) + "]";
				}
				orientation = (funcs[j].getParams()[8].getValue() == null) || (funcs[j].getParams()[8].getValue().toString().trim().length() == 0) ? null
						: funcs[j].getParams()[8].getValue().toString();
				GlQueryVO qryvo = null;
				try {
					qryvo = this.generator.getQueryVO(funname, accsubj, year, period, ass, orientation, envvo);
				} catch (NoAccsubjException e) {
					Logger.error(e.getMessage(), e);
					continue;
				} catch (Exception e) {
					Logger.error(e.getMessage(), e);
					throw e;
				}

				vResult.addElement(qryvo);
			}
		}
		if (vResult.size() > 0) {

			queryvos = new GlQueryVO[vResult.size()];
			vResult.copyInto(queryvos);
		}
		return queryvos;
	}

	public nc.vo.glcom.sorttool.ISortTool getSortTool(Object objCompared) {
		try {
			return new nc.ui.glcom.balance.CAssSortTool1();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		return null;
	}

	private DetailVO[] getThisCEDetail(Vector vTempCEDetal, int location) {
		DetailVO[] aryRetDetails = null;
		Vector partdetailvo = null;
		try {
			partdetailvo = compute(vTempCEDetal, (DetailVO) vTempCEDetal.elementAt(location), location);
		} catch (Exception e) {
			display(e.getLocalizedMessage());

			Logger.error(e.getMessage(), e);
		}
		if ((partdetailvo != null) && (partdetailvo.size() > 0)) {
			aryRetDetails = new DetailVO[partdetailvo.size()];
			partdetailvo.copyInto(aryRetDetails);
		}
		return aryRetDetails;
	}

	public TransferVO[] getTransfervo() {
		return this.transfervo;
	}

	private Vector getVectorBeforeCE(Vector v, int pos) {
		Vector subV = new Vector();
		for (int i = 0; i < pos; i++) {
			DetailVO vo = (DetailVO) ((DetailVO) v.elementAt(i)).clone();
			vo.setPk_currtype(((DetailVO) v.elementAt(i)).getPk_currtype());
			subV.addElement(vo);
		}
		return subV;
	}

	public VoucherVO[] getVoucherHead() {
		return this.voucherHead;
	}

	public Vector getVTransferDetails() {
		return this.vTransferDetails;
	}

	private AssVO[] in_subjAss(String pk_subj, String assDesc, String pk_orgbook) throws Exception {
		AccountVO should = AccountThreadCache.getInstance(getPk_accountingbook(), this.envVO.getDate().toStdString()).getAccountVO(pk_subj);
		if ((should == null) || (should.getAccass() == null) || (should.getAccass().length == 0)) {
			return null;
		}
		AssVO[] assvoshould = new AssVO[should.getAccass().length];
		for (int i = 0; i < assvoshould.length; i++) {
			assvoshould[i] = new AssVO();
			AccAssVO subjassvo = should.getAccass()[i];
			nc.vo.bd.accassitem.AccAssItemVO accAssItem = AccAssGL.getAccAssItemVOByPk(subjassvo.getPk_entity());
			assvoshould[i].setPk_Checktype(subjassvo.getPk_entity());
			assvoshould[i].setChecktypename(GLMultiLangUtil.getMultiName(accAssItem));
		}

		try {
			if ((assDesc != null) && (assDesc.trim().length() != 0)) {
				Vector v = null;
				String pk_corp = AccountBookUtil.getPk_org(getEnvVO().getPk_orgbook());
				getPap().setPk_corp(pk_corp);
				getPap().setPk_accountingbook(getEnvVO().getPk_orgbook());
				v = getPap().getAssPropertys(assDesc);

				if (v == null) {
					throw new Exception(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000044") + assDesc
							+ NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000250"));
				}

				AssVO[] assvo = new AssVO[v.size()];

				for (int i = 0; i < assvo.length; i++) {
					assvo[i] = new AssVO();
					assvo[i].setPk_Checktype(((FormulaAssVO) v.elementAt(i)).getPk_type());
					assvo[i].setChecktypename(((FormulaAssVO) v.elementAt(i)).getTypeName());
					assvo[i].setPk_Checkvalue(((FormulaAssVO) v.elementAt(i)).getPk_value());
					assvo[i].setCheckvaluename(((FormulaAssVO) v.elementAt(i)).getValueCode());
					assvo[i].setCheckvaluecode(((FormulaAssVO) v.elementAt(i)).getValueCode());
					boolean found = false;
					for (int j = 0; j < assvoshould.length; j++) {
						if (assvoshould[j].getPk_Checktype().equals(assvo[i].getPk_Checktype())) {
							assvoshould[j].setPk_Checkvalue(assvo[i].getPk_Checkvalue());
							found = true;
							break;
						}
					}

					if (!found) {
						throw new Exception(NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000251", null,
								new String[] { should.getName() })
								+ assvo[i].getChecktypename());
					}

				}
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new GlBusinessException(e.getMessage());
		}

		return assvoshould;
	}

	public void initialize() throws Exception {
		try {
			AccountingBookVO accountbook = AccountBookUtil.getAccountingBookVOByPrimaryKey(getPk_accountingbook());
			this.LocalCurrtypeOnly = true;
			this.localCurrtype = Currency.getLocalCurrPK(getPk_accountingbook());
			this.groupCurrtype = Currency.getGroupCurrpk(accountbook.getPk_group());
			this.globalCurrtype = Currency.getGlobalCurrPk(getPk_accountingbook());
			Boolean blLocalOnly = new Boolean(this.LocalCurrtypeOnly);
			setIsLocalOnly(blLocalOnly);
			getCurrInfo();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new GlBusinessException(e.getMessage());
		}
	}

	public boolean isBlTransOneByOne() {
		return this.blTransOneByOne;
	}

	private boolean isCEFunction(DetailVO vo) {
		if ((vo.getAccsubjcode() != null) && (vo.getAccsubjcode().toUpperCase().indexOf("CE") == 0))
			return true;
		return false;
	}

	private int locateCEPosition(Vector v) {
		if (v.size() == 0) {
			return -1;
		}
		for (int i = 0; i < v.size(); i++) {
			DetailVO vo = (DetailVO) v.elementAt(i);
			if ((vo.getAccsubjcode() != null) && (isCEFunction(vo))) {
				return i;
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		try {
			Vector a = new Vector();

			Vector b = new Vector();
			b.add("B");
			b.add("C");
			for (int i = 0; i < 8; i++)
				a.add("" + i);
			Iterator iterator = a.iterator();
			log.info(iterator.next());
			log.info(iterator.next());
			a.addAll(2, b);

		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	private void orderDetail(DetailVO[] vos) {
		try {
			if ((vos != null) && (vos.length > 0)) {
				getAssType(vos);

				CShellSort objShellSort = new CShellSort();
				CVoSortTool objVoSortTool = new CVoSortTool();

				objVoSortTool.setSortIndex(new int[] { 107, 301, 303 });
				objVoSortTool.setGetSortTool(this);

				objShellSort.sort(vos, objVoSortTool, false);
			}

		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		for (int i = 0; i < vos.length; i++) {
			vos[i].setDetailindex(Integer.valueOf(i + 1));
		}
	}

	private String[] getAssType(DetailVO[] vos) throws Exception {
		if ((vos == null) || (vos.length == 0)) {
			return null;
		}
		HashMap<String, AccountVO> accMap = new HashMap();
		Vector vPK_bdinfo = new Vector();
		AssVO[] assVos = null;
		List<String> assids = new ArrayList();

		for (int i = 0; i < vos.length; i++) {
			if ((vos[i].getAssid() != null) && (!"".equals(vos[i].getAssid()))) {
				assids.add(vos[i].getAssid());
			}

			accMap.put(vos[i].getPk_accasoa(),
					AccountThreadCache.getInstance(getPk_accountingbook(), this.envVO.getDate().toStdString()).getAccountVO(vos[i].getPk_accasoa()));
		}

		ConcurrentHashMap<String, AssVO[]> assMap = null;
		if (assids.size() > 0) {
			assMap = GLPubProxy.getRemoteFreevaluePub().queryAssvosByAssids((String[]) assids.toArray(new String[0]), Module.GL);
		}

		for (int j = 0; j < vos.length; j++) {
			String strAssid = vos[j].getAssid();
			AccountVO Subvo = (AccountVO) accMap.get(vos[j].getPk_accasoa());
			vos[j].setAccsubjcode(Subvo.getCode());
			if ((strAssid != null) && (!"".equals(strAssid))) {
				assVos = (AssVO[]) assMap.get(strAssid);
				Vector<AssVO> vec = new Vector();
				AccAssVO[] vecAss = Subvo.getAccass();
				if (vecAss != null) {
					for (int i = 0; i < vecAss.length; i++) {
						AccAssVO assvo = vecAss[i];
						for (int m = 0; m < assVos.length; m++) {
							if ((assvo.getPk_entity() != null) && (assVos[m].getPk_Checktype() != null)
									&& (assvo.getPk_entity().equals(assVos[m].getPk_Checktype()))) {
								vec.add(assVos[m]);
							}
						}
					}
				}

				if (vec.size() > 0) {
					assVos = new AssVO[vec.size()];
					vec.copyInto(assVos);
				}

				vos[j].setAss(assVos);
			}
		}

		if (vPK_bdinfo.size() == 0)
			return null;
		String[] sRet = new String[vPK_bdinfo.size()];
		vPK_bdinfo.copyInto(sRet);

		return sRet;
	}

	public void output(String msg) {
		display(msg);
	}

	public void outPutTimes(long tStart, long tEnd, String message) {
		UFDouble differ = null;
		differ = new UFDouble(tEnd).sub(new UFDouble(tStart));
		differ = differ.div(1000.0D);
		Logger.debug(message + differ);
	}

	public void prepareAss(PrepareAssParse pap, Vector v) throws Exception {
		try {
			Vector<Object> vResult = new Vector();

			for (int i = 0; i < v.size(); i++) {
				Vector vWords = new Compiler((String) v.elementAt(i), null, null).getWords();

				FunctionStruct[] funcs = Compiler.getFunctions(this.funnames, vWords);
				for (int j = 0; (funcs != null) && (j < funcs.length); j++) {
					if ((funcs[j].getParams()[6] != null) && (funcs[j].getParams()[6].getValue() != null)
							&& (funcs[j].getParams()[6].getValue().toString().trim().length() != 0)) {
						if (funcs[j].getParams()[6].getValue().toString().indexOf(AssFlag) > 0) {
							String obj = funcs[j].getParams()[6].getValue().toString();
							obj = obj.substring(0, obj.indexOf(AssFlag)) + "]";
							vResult.addElement(obj);
						} else {
							vResult.addElement(funcs[j].getParams()[6].getValue());
						}
					}
				}
			}
			String[] strResults = null;
			if (this.vResultAss.size() > 0) {
				for (int i = 0; i < this.vResultAss.size(); i++) {
					vResult.addElement(this.vResultAss.elementAt(i));
				}
			}
			if (vResult.size() > 0) {
				strResults = new String[vResult.size()];
				vResult.copyInto(strResults);
				pap.getAssVOsByAssInit(strResults);
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	public GlQueryVO[] prepareQueryVOs(TransferDetailVO[] detailvos) throws Exception {
		GlQueryVO[] qryvos = null;

		DetailSpread detailspread = new DetailSpread();
		detailspread.setPk_orgbook(getPk_accountingbook());
		detailspread.setYear(this.envVO.getAccYear());
		detailspread.setPeriod(this.envVO.getAccMouth());
		detailspread.setDate(this.envVO.getDate());

		if ((detailvos != null) && (detailvos.length != 0)) {
			detailvos = detailspread.spreadDetails(detailvos);
		} else {
			return null;
		}
		try {
			Vector<GlQueryVO> vResult = new Vector();

			for (int i = 0; i < detailvos.length; i++) {
				GlQueryVO[] tempvos = getQueryVOsFromDetail(detailvos[i]);
				if ((tempvos != null) && (tempvos.length > 0)) {
					for (int j = 0; j < tempvos.length; j++) {
						vResult.addElement(tempvos[j]);
					}
				}
			}
			if (vResult.size() > 0) {
				qryvos = new GlQueryVO[vResult.size()];
				vResult.copyInto(qryvos);
			}

		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new GlBusinessException(e.getMessage());
		}

		return qryvos;
	}

	public void prepareTransact(TransferDetailVO[] detailvos) throws Exception {
		try {
			Vector vResult = getAllDetails(detailvos);

			if (getPap() != null) {
				String pk_corp = AccountBookUtil.getPk_org(getPk_accountingbook());
				getPap().setPk_corp(pk_corp);
				getPap().setPk_accountingbook(getPk_accountingbook());
			}
			prepareAss(getPap(), vResult);
			GlQueryVO[] qryvos = prepareQueryVOs(detailvos);
			if ((qryvos != null) && (qryvos.length > 0))
				genRstForQueryVOs(qryvos);
		} catch (Exception e) {
			String errmsg = NCLangRes4VoTransl.getNCLangRes().getStrByID("20021505", "UPP20021505-000253") + e.getMessage();

			Logger.error(errmsg, e);
			throw new GlBusinessException(errmsg);
		}
	}

	private void removeUselessCEs(Vector detail) throws Exception {
		if ((detail == null) || (detail.size() == 0)) {
			return;
		}
		for (int i = detail.size() - 1; i >= 0; i--) {
			if (i == detail.size() - 1) {
				if ((getCEProp((DetailVO) detail.elementAt(detail.size() - 1)) == null)
						|| (!getCEProp((DetailVO) detail.elementAt(detail.size() - 1)).trim().toUpperCase().equals("DOWN")))
					break;
				detail.removeElementAt(detail.size() - 1);
				if (detail.size() == 0) {
					return;
				}
			}
		}

		boolean isHeadCEUP = true;
		while (isHeadCEUP) {
			if ((getCEProp((DetailVO) detail.elementAt(0)) != null) && (getCEProp((DetailVO) detail.elementAt(0)).trim().toUpperCase().equals("UP"))) {
				detail.removeElementAt(0);
				if (detail.size() != 0) {
				}
			} else {
				isHeadCEUP = false;
			}
		}
	}

	public void setBlTransOneByOne(boolean newBlTransOneByOne) {
		this.blTransOneByOne = newBlTransOneByOne;
	}

	private void setDetailQuantity(DetailVO[] vos) throws BusinessException {
		AccountVO accvo = null;
		for (int i = 0; i < vos.length; i++) {
			accvo = AccountThreadCache.getInstance(getPk_accountingbook(), this.envVO.getDate().toStdString()).getAccountVO(vos[i].getPk_accasoa());
			if ((accvo == null) || (accvo.getUnit() == null) || (accvo.getUnit().trim().length() == 0)) {
				vos[i].setCreditquantity(new UFDouble(0));
				vos[i].setDebitquantity(new UFDouble(0));
			}
		}
	}

	public void setEnvVO(TransferEnvVO newEnvVO) {
		this.envVO = newEnvVO;
	}

	public void setHtTransDetails(Hashtable newHtTransDetails) {
		this.htTransDetails = newHtTransDetails;
	}

	/**
	 * @deprecated
	 */
	public void setIsLocalOnly(Boolean newIsLocalOnly) {
		this.isLocalOnly = newIsLocalOnly;
	}

	private void setNullToZero(DetailVO vo) {
		if (vo.getDebitamount() == null)
			vo.setDebitamount(new UFDouble(0));
		if (vo.getCreditamount() == null)
			vo.setCreditamount(new UFDouble(0));
		if (vo.getLocaldebitamount() == null)
			vo.setLocaldebitamount(new UFDouble(0));
		if (vo.getLocalcreditamount() == null)
			vo.setLocalcreditamount(new UFDouble(0));
		if (vo.getFracdebitamount() == null)
			vo.setFracdebitamount(new UFDouble(0));
		if (vo.getFraccreditamount() == null)
			vo.setFraccreditamount(new UFDouble(0));
		if (vo.getDebitquantity() == null)
			vo.setDebitquantity(new UFDouble(0));
		if (vo.getCreditquantity() == null) {
			vo.setCreditquantity(new UFDouble(0));
		}
	}

	public void setPap(PrepareAssParse newPap) {
		this.pap = newPap;
	}

	private void setRatePrecision(Vector vParam) throws Exception {
		if ((vParam == null) || (vParam.size() == 0))
			return;
		for (int i = 0; i < vParam.size(); i++) {
			DetailVO detailvo = (DetailVO) vParam.elementAt(i);
			Integer rateDigit = null;

			if (this.htCurAndRate.get(detailvo.getPk_currtype()) != null) {
				rateDigit = (Integer) this.htCurAndRate.get(detailvo.getPk_currtype());
				detailvo.setDebitamount(format(detailvo.getDebitamount(), rateDigit.intValue()));
				detailvo.setCreditamount(format(detailvo.getCreditamount(), rateDigit.intValue()));
			}

			if (this.htCurAndRate.get(this.localCurrtype) != null) {
				rateDigit = (Integer) this.htCurAndRate.get(this.localCurrtype);
				detailvo.setLocaldebitamount(format(detailvo.getLocaldebitamount(), rateDigit.intValue()));
				detailvo.setLocalcreditamount(format(detailvo.getLocalcreditamount(), rateDigit.intValue()));
			}

			if ((Currency.isStartGroupCurr(AccountBookUtil.getPkGruopByAccountingBookPK(getPk_accountingbook())))
					&& (this.htCurAndRate.get(this.localCurrtype) != null)) {
				rateDigit = (Integer) this.htCurAndRate.get(this.groupCurrtype);
				detailvo.setGroupdebitamount(format(detailvo.getGroupdebitamount(), rateDigit.intValue()));
				detailvo.setGroupcreditamount(format(detailvo.getGroupcreditamount(), rateDigit.intValue()));
			}

			if ((Currency.isStartGlobalCurr()) && (this.htCurAndRate.get(this.localCurrtype) != null)) {
				rateDigit = (Integer) this.htCurAndRate.get(this.globalCurrtype);
				detailvo.setGlobaldebitamount(format(detailvo.getGlobaldebitamount(), rateDigit.intValue()));
				detailvo.setGlobalcreditamount(format(detailvo.getGlobalcreditamount(), rateDigit.intValue()));
			}
		}
	}

	public void setTransfervo(TransferVO[] newTransfervo) {
		this.transfervo = newTransfervo;
	}

	private void setVoucherHead(VoucherVO[] newVoucherHead) {
		this.voucherHead = newVoucherHead;
	}

	public void setVTransferDetails(Vector newVTransferDetails) {
		this.vTransferDetails = newVTransferDetails;
	}

	public HashMap getH_NameToVOs() {
		return this.h_NameToVOs;
	}

	public void setH_NameToVOs(HashMap nameToVOs) {
		this.h_NameToVOs = nameToVOs;
	}

	public String[] getSelfDefFunnames() {
		return this.selfDefFunnames;
	}

	public void setSelfDefFunnames(String[] selfDefFunnames) {
		this.selfDefFunnames = selfDefFunnames;
	}

	public String getPk_corp() {
		if (this.pk_corp == null) {
			try {

				AccountBookUtil.getPk_org(getPk_orgbook());
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
			}
		}
		return this.pk_corp;
	}

	public final synchronized void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	/**
	 * @deprecated
	 */
	public final synchronized String getPk_orgbook() {
		return getPk_accountingbook();
	}

	public final synchronized void setPk_orgbook(String pk_orgbook) {
		setPk_accountingbook(pk_orgbook);
	}

	public String getPk_accountingbook() {
		return this.pk_accountingbook;
	}

	public void setPk_accountingbook(String pkAccountingbook) {
		this.pk_accountingbook = pkAccountingbook;
	}
}