package com.samistax.webhook.dto;

import java.util.List;

public class ShipmentEvent extends BaseEvent{
    private String equipmentId;
    private String printerName;
    private List<Shipment> shipments;

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

    public List<Shipment> getShipments() {
        return shipments;
    }

    public void setShipments(List<Shipment> shipments) {
        this.shipments = shipments;
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
                ", printerName='" + printerName + '\'' +
                ", shipments=" + shipments +
                '}';
    }
}

