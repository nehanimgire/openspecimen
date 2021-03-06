package com.krishagni.catissueplus.core.printer.printRule.events;

import com.krishagni.catissueplus.core.common.events.EventStatus;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;


public class PrintRuleDeletedEvent extends ResponseEvent {

	private static final String SUCCESS = "success";

	public static PrintRuleDeletedEvent ok() {
		PrintRuleDeletedEvent event = new PrintRuleDeletedEvent();
		event.setMessage(SUCCESS);
		event.setStatus(EventStatus.OK);
		return event;
	}

	public static PrintRuleDeletedEvent serverError(Throwable... t) {
		Throwable t1 = t != null && t.length > 0 ? t[0] : null;
		PrintRuleDeletedEvent resp = new PrintRuleDeletedEvent();
		resp.setStatus(EventStatus.INTERNAL_SERVER_ERROR);
		resp.setException(t1);
		resp.setMessage(t1 != null ? t1.getMessage() : null);
		return resp;
	}

	public static PrintRuleDeletedEvent notFound() {
		PrintRuleDeletedEvent resp = new PrintRuleDeletedEvent();
		resp.setStatus(EventStatus.NOT_FOUND);
		return resp;
	}
}
