package com.opentext.apps.cc.upgradeutils.dao;

import com.cordys.cpc.bsf.busobject.BusObject;
import com.cordys.cpc.bsf.busobject.BusObjectConfig;

public class CM_CONTRACT extends BusObject {

	public String getContractsJson() {
		return contractsJson;
	}

	public void setContractsJson(String contractsJson) {
		this.contractsJson = contractsJson;
	}

	private String contractsJson;

	protected CM_CONTRACT(BusObjectConfig arg0) {
		super(arg0);
	}

	@Override
	public void onDelete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInsert() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpdate() {
		// TODO Auto-generated method stub

	}

}
