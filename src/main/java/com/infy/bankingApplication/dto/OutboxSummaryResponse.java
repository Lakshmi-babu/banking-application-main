package com.infy.bankingApplication.dto;

public class OutboxSummaryResponse {
	
	    private long totalEvents;
	    private long pendingEvents;
	    private long publishedEvents;
	    private long failedEvents;

	    public OutboxSummaryResponse(
	            long totalEvents,
	            long pendingEvents,
	            long publishedEvents,
	            long failedEvents) {

	        this.totalEvents = totalEvents;
	        this.pendingEvents = pendingEvents;
	        this.publishedEvents = publishedEvents;
	        this.failedEvents = failedEvents;
	    }

	    public long getTotalEvents() {
	        return totalEvents;
	    }

	    public long getPendingEvents() {
	        return pendingEvents;
	    }

	    public long getPublishedEvents() {
	        return publishedEvents;
	    }

	    public long getFailedEvents() {
	        return failedEvents;
	    }

}
