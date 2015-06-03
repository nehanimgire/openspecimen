package com.krishagni.openspecimen.custom.sgh.services;

import java.util.List;

import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenLabelPrintJob;


public interface TridPrinter {

	public SpecimenLabelPrintJob print(List<String> trids);
	
}
