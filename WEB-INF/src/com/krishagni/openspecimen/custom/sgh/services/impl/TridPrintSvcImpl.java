package com.krishagni.openspecimen.custom.sgh.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.service.ConfigurationService;
import com.krishagni.openspecimen.custom.sgh.SghErrorCode;
import com.krishagni.openspecimen.custom.sgh.TridGenerator;
import com.krishagni.openspecimen.custom.sgh.events.BulkTridPrintSummary;
import com.krishagni.openspecimen.custom.sgh.services.TridPrintSvc;

public class TridPrintSvcImpl implements TridPrintSvc{

	private ConfigurationService cfgSvc;
	
	public void setCfgSvc(ConfigurationService cfgSvc) {
		this.cfgSvc = cfgSvc;
	}

	@Override
	@PlusTransactional
	public ResponseEvent<BulkTridPrintSummary> generateAndPrintTrids(RequestEvent<BulkTridPrintSummary> req) {
		BulkTridPrintSummary printReq = req.getPayload();
		int tridsCount = printReq.getTridsCount();
		if (tridsCount < 1) {
			return ResponseEvent.userError(SghErrorCode.INVALID_TRID_COUNT);
		}
//		TridPrinter printer = getLabelPrinter();
//		if (printer == null) {
//			throw OpenSpecimenException.serverError(SpecimenErrorCode.NO_PRINTER_CONFIGURED);
//		}
		
		List<String> labels = new ArrayList<String>();
		for(int i=0; i < printReq.getTridsCount(); i++){
			String trid = TridGenerator.getNextTrid(); 
			labels.addAll(getSpecimenLabels(trid));
		}
		
		int copiesToPrint = cfgSvc.getIntSetting("SGH", "copies_to_print", 1);
//	TridPrintJob job = printer.print(null, copiesToPrint);
//	if (job == null) {
//		throw OpenSpecimenException.userError(SpecimenErrorCode.PRINT_ERROR);
//	}
		return ResponseEvent.response(printReq);
	}
	
	private Collection<String> getSpecimenLabels(String parentLabel) {
		List<String> labels = new ArrayList<String>();
		labels.add(parentLabel);
		for (int i=0; i < malignantAliqCnt; i++) {
			labels.add(parentLabel+"_"+malignantAliqSuffix+"_"+i);
		}
		for (int i=0; i < nonMalignantAliqCnt; i++) {
			labels.add(parentLabel+"_"+nonMalignantAliqSuffix+"_"+i);
		}
		return labels;
	}

//	private TridPrinter getLabelPrinter() {
//		String labelPrinterBean = cfgSvc.getStrSetting(
//				ConfigParams.MODULE, 
//				ConfigParams.LABEL_PRINTER, 
//				"defaultSpecimenLabelPrinter");
//		return (TridPrinter)OpenSpecimenAppCtxProvider
//				.getAppCtx()
//				.getBean(labelPrinterBean);
//	}
	
	private final String malignantAliqSuffix = "FZ-T";
	
	private final String nonMalignantAliqSuffix = "FZ-N";
	
	private final int malignantAliqCnt = 3;
	
	private final int nonMalignantAliqCnt = 2;

}
