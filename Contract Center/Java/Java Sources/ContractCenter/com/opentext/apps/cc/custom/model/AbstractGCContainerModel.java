package com.opentext.apps.cc.custom.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractGCContainerModel implements Comparable<AbstractGCContainerModel> {
	private int id;
	private int parentId;
	private int order;
	private int level = 0;
	private String numberingStyle;
	private String cascadingInfo;
	private String contentCascadingInfo;
	private String styleingAttributes;
	private List<AbstractGCContainerModel> childs;

	// Conditional clause content fields;
	private int targetContainerId;
	private int sourceContainerId;
	private String conditionAction;
	private int ruleId;
	private boolean eligibleForDocument = true;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void addChildModel(AbstractGCContainerModel childContainerModel) {
		if (Objects.isNull(childs)) {
			childs = new ArrayList<AbstractGCContainerModel>();
		}
		childContainerModel.setLevel(this.level + 1);
		updateNumberingStyle(childContainerModel);
		updateContainerCascadeInfo(childContainerModel, this.cascadingInfo);
		updateContentCascadeInfo(childContainerModel, this.contentCascadingInfo);
		childs.add(childContainerModel);
	}

	private void updateNumberingStyle(AbstractGCContainerModel childContainerModel) {
		String numberingStyle = DocGenModelUtil.getTokenValue(childContainerModel.getStyleingAttributes(),
				DocGenModelUtil.NUMBERING_STYLE);
		if (!numberingStyle.isBlank()) {
			childContainerModel.setNumberingStyle(DocGenModelUtil.getAgumentedStyle(numberingStyle));
		}
	}

	private void updateContainerCascadeInfo(AbstractGCContainerModel childContainerModel, String parentCascadeInfo) {
		if (Objects.nonNull(childContainerModel)) {
			String cascadingInfo = DocGenModelUtil.getTokenValue(childContainerModel.getStyleingAttributes(),
					DocGenModelUtil.CASCADING_INFO);
			String noNumberingInfo = DocGenModelUtil.getTokenValue(childContainerModel.getStyleingAttributes(),
					DocGenModelUtil.NO_NUMBERING_INFO);
			if ("TRUE".equalsIgnoreCase(noNumberingInfo)) {
				cascadingInfo = DocGenModelUtil.CASCADE_OFF;
			} else if (DocGenModelUtil.CASCADE_INHERIT.equalsIgnoreCase(cascadingInfo)
					&& Objects.nonNull(parentCascadeInfo)) {
				// Applying parent style.
				if (DocGenModelUtil.CASCADE_OFF.equalsIgnoreCase(parentCascadeInfo)) {
					cascadingInfo = DocGenModelUtil.CASCADE_NEW;
				} else {
					cascadingInfo = parentCascadeInfo;
				}
			} else {
				cascadingInfo = DocGenModelUtil.CASCADE_CONTINUE;
			}
			childContainerModel.setCascadingInfo(cascadingInfo);
		}
	}

	private void updateContentCascadeInfo(AbstractGCContainerModel childContainerModel, String parentCascadeInfo) {
		if (Objects.nonNull(childContainerModel)) {
			String cascadingInfo = DocGenModelUtil.getTokenValue(childContainerModel.getStyleingAttributes(),
					DocGenModelUtil.CASCADING_INFO);
			String noNumberingInfo = DocGenModelUtil.getTokenValue(childContainerModel.getStyleingAttributes(),
					DocGenModelUtil.NO_NUMBERING_INFO);
			if ("TRUE".equalsIgnoreCase(noNumberingInfo)) {
				cascadingInfo = DocGenModelUtil.CASCADE_OFF;
			} else if (DocGenModelUtil.CASCADE_INHERIT.equalsIgnoreCase(cascadingInfo)
					&& Objects.nonNull(parentCascadeInfo)) {
				cascadingInfo = parentCascadeInfo;
			}
			childContainerModel.setContentCascadingInfo(cascadingInfo);
		}
	}

	public List<AbstractGCContainerModel> getChildrenModels() {
		if (Objects.isNull(childs)) {
			childs = new ArrayList<AbstractGCContainerModel>();
		}
		return childs;
	}

	public String getStyleingAttributes() {
		return styleingAttributes;
	}

	public void setStyleingAttributes(String styleingAttributes) {
		this.styleingAttributes = styleingAttributes;
	}

	public String getNumberingStyle() {
		return numberingStyle;
	}

	public void setNumberingStyle(String numberingStyle) {
		this.numberingStyle = numberingStyle;
	}

	public String getCascadingInfo() {
		return cascadingInfo;
	}

	// Update cascade information and it's children cascading info.
	public void setCascadingInfo(String cascadingInfo) {
		if (Objects.nonNull(cascadingInfo)) {
			if (!cascadingInfo.equals(this.cascadingInfo)) {
				this.cascadingInfo = DocGenModelUtil.getAgumentedCascadingInfo(cascadingInfo);
				if (Objects.nonNull(this.childs) && !this.childs.isEmpty()) {
					this.childs.forEach(child -> {
						updateContainerCascadeInfo(child, cascadingInfo);
					});
				}
			}
		}
	}

	public String getContentCascadingInfo() {
		return contentCascadingInfo;
	}

	public void setContentCascadingInfo(String contentCascadingInfo) {
		if (Objects.nonNull(contentCascadingInfo)) {
			if (!contentCascadingInfo.equals(this.contentCascadingInfo)) {
				this.contentCascadingInfo = DocGenModelUtil.getAgumentedContentCascadingInfo(contentCascadingInfo);
				if (Objects.nonNull(this.childs) && !this.childs.isEmpty()) {
					this.childs.forEach(child -> {
						updateContentCascadeInfo(child, contentCascadingInfo);
					});
				}
			}
		}
	}

	@Override
	public int compareTo(AbstractGCContainerModel o) {
		return this.order - o.order;
	}

	public void sortChildrenContainers() {
		if (Objects.nonNull(this.childs) && this.childs.size() > 0) {
			Collections.sort(this.childs);
		}
	}

	public int getTargetContainerId() {
		return targetContainerId;
	}

	public void setTargetContainerId(int targetContainerId) {
		this.targetContainerId = targetContainerId;
	}

	public int getSourceContainerId() {
		return sourceContainerId;
	}

	public void setSourceContainerId(int sourceContainerId) {
		this.sourceContainerId = sourceContainerId;
	}

	public String getConditionAction() {
		return conditionAction;
	}

	public void setConditionAction(String conditionAction) {
		this.conditionAction = conditionAction;
	}

	public int getRuleId() {
		return ruleId;
	}

	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}

	public boolean isEligibleForDocument() {
		return eligibleForDocument;
	}

	public void setEligibleForDocument(boolean eligibleForDocument) {
		this.eligibleForDocument = eligibleForDocument;
	}

}
