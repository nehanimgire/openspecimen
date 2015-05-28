package com.krishagni.openspecimen.custom.sgh;

import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;


public class TridGenerator {

	private static DaoFactory daoFactory;

	public static void setDaoFactory(DaoFactory daoFactory) {
		TridGenerator.daoFactory = daoFactory;
	}

	public static String getNextTrid(){
			return TRID_PREFIX + daoFactory.getUniqueIdGenerator().getUniqueId("TRID", "TRID");
	}
	
	private static final String TRID_PREFIX = "z";
	
}
