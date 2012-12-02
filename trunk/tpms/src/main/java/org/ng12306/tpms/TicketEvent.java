package org.ng12306.tpms;

public class TicketEvent {
    private TicketEventType _type;
    public TicketEventType getType() { return _type; }

    private int _value;
    public int getValue() { return _value; }
    public void setValue(int value) { _value = value; }

    public TicketEvent(TicketEventType type) {
	_type = type;
    }
}
