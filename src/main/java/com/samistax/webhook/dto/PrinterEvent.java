package com.samistax.webhook.dto;

public class PrinterEvent extends BaseEvent{
    private String equipmentId;
    private String printerName;

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }


    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + getEventId() +
                ", activityId=" + getActivityId() +
                ", taskTypeCode='" + getTaskTypeCode() + '\'' +
                ", activated='" + getActivated() + '\'' +
                ", userCode='" + getUserCode() + '\'' +
                ", equipmentId='" + equipmentId + '\'' +
                ", printerName='" + printerName +
                '}';
    }
}
