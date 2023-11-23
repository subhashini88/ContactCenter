package com.opentext.apps.cc.custom.querybuilder.builder.data;

public enum ObligationStatus {
	Open("Open"), MetOverride("Met-Override"), MetCorrected("Open"), Met("Met"),
	MetPostEscalation("Met-PostEscalation"), InProgress("Open"), InProgressEscalated("InProgress-Escalated"),
	MetReviewInProgress("Met-ReviewInProgress"), NotmetReviewInProgress("Notmet-ReviewInProgress"), NotMet("NotMet"),
	MetInRecurrence("Met-InRecurrence"), MetPendingRecurrences("Met-PendingRecurrences"),
	NotMetPendingRecurrences("NotMet-PendingRecurrences"),
	MetOverridePendingRecurrences("Met-Override-PendingRecurrences"),
	MetCorrectedPendingRecurrences("Met-Corrected-PendingRecurrences"),
	MetPostEscalationPendingRecurrences("Met-PostEscalation-PendingRecurrences");

	private String status;

	ObligationStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

}
