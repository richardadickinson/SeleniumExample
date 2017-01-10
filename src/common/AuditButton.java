/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common;

import java.util.*;

/**
 *
 * @author smaho01
 */
public class AuditButton {

    public final int WGN_AUDIT_BTN_ET_DISABLE = 0;
    public final int WGN_AUDIT_BTN_ET_EVENT = 1;
    public final int WGN_AUDIT_BTN_ET_BULK = 2;
    public final int WGN_AUDIT_BTN_ET_EVENTANDBULK = 3;
    public final int WGN_AUDIT_BTN_ET_MAX = 4;

    private List<AuditStatusValue> AuditStatusValues;

    private int version;
    private int enableType;
    private int affectedField;
    private int indexFrom;
    private int indexTo1;
    private int indexTo2;
    private int indexTo3;
    private int invalidAction;
    private String comment;
    private String issueName;
    private String tooltip;

    private String bulkAuditButtonId;
    private String singleAuditButtonId;
    private String Field1Value;

    public String GetField1Text() {
        return Field1Value;
    }

    public boolean isDisabled() {
        return enableType == WGN_AUDIT_BTN_ET_DISABLE;
    }

    public boolean isBulk() {
        return enableType == WGN_AUDIT_BTN_ET_BULK;
    }

    public boolean isSingleAndBulk() {
        return enableType == WGN_AUDIT_BTN_ET_EVENTANDBULK;
    }

    public boolean isSingleAuditButton() {
        return enableType == WGN_AUDIT_BTN_ET_EVENT;
    }

    public String getBulkAuditButtonId() {
        return bulkAuditButtonId;
    }

    public String getSingleAuditButtonId() {
        return singleAuditButtonId;
    }

    public AuditButton(String buttonConfig, String bulkAuditButtonId, String singleAuditButtonId, List<AuditStatusValue> asv) {

        AuditStatusValues = asv;
        Field1Value = "";

        ParseButtonConfig(buttonConfig);
        this.bulkAuditButtonId = bulkAuditButtonId;
        this.singleAuditButtonId = singleAuditButtonId;
    }

    private void ParseButtonConfig(String buttonConfig) {
        String[] s = buttonConfig.split(",");

        setVersion(Integer.parseInt(s[0]));
        enableType = Integer.parseInt(s[1]);
        setAffectedField(Integer.parseInt(s[2]));
        setIndexFrom(Integer.parseInt(s[3]));
        indexTo1 = Integer.parseInt(s[4]);
        setIndexTo2(Integer.parseInt(s[5]));
        setIndexTo3(Integer.parseInt(s[6]));
        setInvalidAction(Integer.parseInt(s[7]));
        setComment(s[8]);
        setIssueName(s[9]);
        setTooltip(s[10]);

        if (indexTo1 != -1)
        {
            AuditStatusValue v = AuditStatusValues.get(indexTo1);
            Field1Value = v.GetAuditStatusText();
        }
    }

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public String getIssueName() {
		return issueName;
	}

	public void setIssueName(String issueName) {
		this.issueName = issueName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getInvalidAction() {
		return invalidAction;
	}

	public void setInvalidAction(int invalidAction) {
		this.invalidAction = invalidAction;
	}

	public int getIndexTo3() {
		return indexTo3;
	}

	public void setIndexTo3(int indexTo3) {
		this.indexTo3 = indexTo3;
	}

	public int getIndexTo2() {
		return indexTo2;
	}

	public void setIndexTo2(int indexTo2) {
		this.indexTo2 = indexTo2;
	}

	public int getIndexFrom() {
		return indexFrom;
	}

	public void setIndexFrom(int indexFrom) {
		this.indexFrom = indexFrom;
	}

	public int getAffectedField() {
		return affectedField;
	}

	public void setAffectedField(int affectedField) {
		this.affectedField = affectedField;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
