package com.samistax.webhook.dto;

public class Shipment {
    private int shipmentid;
    private String picklistid;
    private String deliverylocation;
    private String boxtype;
    private String ownercode;

    public int getShipmentid() {
        return shipmentid;
    }

    public void setShipmentid(int shipmentid) {
        this.shipmentid = shipmentid;
    }

    public String getPicklistid() {
        return picklistid;
    }

    public void setPicklistid(String picklistid) {
        this.picklistid = picklistid;
    }

    public String getDeliverylocation() {
        return deliverylocation;
    }

    public void setDeliverylocation(String deliverylocation) {
        this.deliverylocation = deliverylocation;
    }

    public String getBoxtype() {
        return boxtype;
    }

    public void setBoxtype(String boxtype) {
        this.boxtype = boxtype;
    }

    public String getOwnercode() {
        return ownercode;
    }

    public void setOwnercode(String ownercode) {
        this.ownercode = ownercode;
    }

    @Override
    public String toString() {
        return "Shipment{" +
                "shipmentid=" + shipmentid +
                ", picklistid='" + picklistid + '\'' +
                ", deliverylocation='" + deliverylocation + '\'' +
                ", boxtype='" + boxtype + '\'' +
                ", ownercode='" + ownercode + '\'' +
                '}';
    }
}
