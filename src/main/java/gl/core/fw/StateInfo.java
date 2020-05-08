package gl.core.fw;

public class StateInfo
{
    private String serviceId;
    private int seqNumber;
    private String currentId;
    private String eventId;
    private String actionId;
    private String nextStateId;
    private String eventMsg;
    private int timeout;
    
    public StateInfo(final String serviceId, final int seqNumber, final String currentId, final String eventId, final String actionId, final String nextStateId, final int timeout, final String eventMsg) {
        this.serviceId = null;
        this.seqNumber = -1;
        this.currentId = null;
        this.eventId = null;
        this.actionId = null;
        this.nextStateId = null;
        this.eventMsg = null;
        this.timeout = 0;
        this.serviceId = serviceId;
        this.seqNumber = seqNumber;
        this.currentId = currentId;
        this.eventId = eventId;
        this.actionId = actionId;
        this.nextStateId = nextStateId;
        this.timeout = timeout;
        this.eventMsg = eventMsg;
    }
    
    public String getServiceId() {
        return this.serviceId;
    }
    
    public String getEventId() {
        return this.eventId;
    }
    
    public String getCurrentId() {
        return this.currentId;
    }
    
    public String getNextStateId() {
        return this.nextStateId;
    }
    
    public String getActionId() {
        return this.actionId;
    }
    
    public int getTimeOut() {
        return this.timeout;
    }
    
    public String getEventMsg() {
        return this.eventMsg;
    }
    
    public int getSeqNumber() {
        return this.seqNumber;
    }
    
    public void setEventMsg(final String eventMsg) {
        this.eventMsg = eventMsg;
    }
}