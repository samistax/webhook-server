package com.samistax.webhook.dto;

public class BaseEvent {
    private int eventId;
    private int activityId;
    private String taskTypeCode;
    private String activated;
    private String userCode;

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public String getTaskTypeCode() {
        return taskTypeCode;
    }

    public void setTaskTypeCode(String taskTypeCode) {
        this.taskTypeCode = taskTypeCode;
    }

    public String getActivated() {
        return activated;
    }

    public void setActivated(String activated) {
        this.activated = activated;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", activityId=" + activityId +
                ", taskTypeCode='" + taskTypeCode + '\'' +
                ", activated='" + activated + '\'' +
                ", userCode='" + userCode +
                '}';
    }
}

