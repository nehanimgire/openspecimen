package com.krishagni.openspecimen.custom.sgh.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.krishagni.catissueplus.core.biospecimen.ConfigParams;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.SpecimenErrorCode;
import com.krishagni.catissueplus.core.biospecimen.services.SpecimenLabelPrinter;
import com.krishagni.catissueplus.core.common.OpenSpecimenAppCtxProvider;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.service.ConfigurationService;
import com.krishagni.openspecimen.custom.sgh.SghErrorCode;
import com.krishagni.openspecimen.custom.sgh.TridGenerator;
import com.krishagni.openspecimen.custom.sgh.events.BulkTridPrintSummary;
import com.krishagni.openspecimen.custom.sgh.services.TridPrintSvc;

public class TridPrintSvcImpl implements TridPrintSvc {

	private static final String SGH_MODULE = "sgh";
	
	private ConfigurationService cfgSvc;
	
	public void setCfgSvc(ConfigurationService cfgSvc) {
		this.cfgSvc = cfgSvc;
	}

	@Override
	@PlusTransactional
	public ResponseEvent<Boolean> generateAndPrintTrids(RequestEvent<BulkTridPrintSummary> req) {
		BulkTridPrintSummary printReq = req.getPayload();
		int tridsCount = printReq.getTridCount();
		if (tridsCount < 1) {
			return ResponseEvent.userError(SghErrorCode.INVALID_TRID_COUNT);
		}
		
		SpecimenLabelPrinter printer = getLabelPrinter();
		if (printer == null) {
			throw OpenSpecimenException.serverError(SpecimenErrorCode.NO_PRINTER_CONFIGURED);
		}
		
		List<String> labels = new ArrayList<String>();
		for(int i = 0; i < printReq.getTridCount(); i++){
			String trid = TridGenerator.getNextTrid(); 
			labels.addAll(getSpecimenLabels(trid));
		}
		
		int copiesToPrint = cfgSvc.getIntSetting(SGH_MODULE, "copies_to_print", 4);
//		SpecimenLabelPrintJob job = printer.print(null, copiesToPrint);
//		if (job == null) {
//			throw OpenSpecimenException.userError(SpecimenErrorCode.PRINT_ERROR);
//		}
		return ResponseEvent.response(true);
	}
	
	private Collection<String> getSpecimenLabels(String parentLabel) {
		List<String> labels = new ArrayList<String>();
		labels.add(parentLabel);
		
		String malignantAliqPrefix = parentLabel + "_" + getMalignantAliqSuffix() + "_"; 
		for (int i = 0; i < getMalignantAliqCnt(); i++) {
			labels.add(malignantAliqPrefix + i);
		}
		
		String nonMalignantAliqPrefix = parentLabel + "_" + getNonMalignantAliqSuffix() + "_";
		for (int i = 0; i < getNonMalignantAliqCnt(); i++) {
			labels.add(nonMalignantAliqPrefix + i);
		}
		return labels;
	}
	
	private SpecimenLabelPrinter getLabelPrinter() {
		String labelPrinterBean = cfgSvc.getStrSetting(
				ConfigParams.MODULE, 
				ConfigParams.LABEL_PRINTER, 
				"defaultTridPrinter");
		return (SpecimenLabelPrinter)OpenSpecimenAppCtxProvider
				.getAppCtx()
				.getBean(labelPrinterBean);
	}

	private int getMalignantAliqCnt() {
		return cfgSvc.getIntSetting(SGH_MODULE, "malignant_aliq_cnt", 3);
	}
	
	private int getNonMalignantAliqCnt() {
		return cfgSvc.getIntSetting(SGH_MODULE, "non_malignant_aliq_cnt", 2);
	}

	private String getMalignantAliqSuffix() {
		return cfgSvc.getStrSetting(SGH_MODULE, "malignantAliqSuffix", "FZ-T");
	}
	
	private String getNonMalignantAliqSuffix() {
		return cfgSvc.getStrSetting(SGH_MODULE, "nonMalignantAliqSuffix", "FZ-N");
	}
	
}
