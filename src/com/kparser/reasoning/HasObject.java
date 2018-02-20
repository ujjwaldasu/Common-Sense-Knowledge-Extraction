package com.kparser.reasoning;

public class HasObject {
	private String event1;
	private String recipient;
	private String agent;
	private String event2;
	private String instance_of;
	private String negative;
	private boolean isReverse;
	
	
	public boolean isReverse() {
		return isReverse;
	}
	public HasObject() {
		super();
		this.event1 = null;
		this.recipient = null;
		this.agent = null;
		this.event2 = null;
		this.instance_of = null;
		this.negative = null;
		this.isReverse = false;
	}
	public void setReverse(boolean isReverse) {
		this.isReverse = isReverse;
	}
	public String getNegative() {
		return negative;
	}
	public void setNegative(String negative) {
		this.negative = negative;
	}
	public String getEvent1() {
		return event1;
	}
	public void setEvent1(String event1) {
		this.event1 = event1;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	public String getEvent2() {
		return event2;
	}
	public void setEvent2(String event2) {
		this.event2 = event2;
	}
	public String getInstance_of() {
		return instance_of;
	}
	public void setInstance_of(String instance_of) {
		this.instance_of = instance_of;
	}
	
	
	public String getNegEvent1(){
		
		String s=" ";
		
		//If Instance is present
		if(this.instance_of!=null) s=this.instance_of;		
		else s=this.event1;
		
		s = s.split("-")[0];
		if(this.negative!=null) {
			String s1=this.negative;
			s1 = s1.split("-")[0];
			s = s1 + " " + s; 
			}
		return s;
	}
	
	
	public void printHasObject(){
		System.out.println("Negative "+this.negative);
		System.out.println("Event 1 "+this.event1);
		System.out.println("Event 2 "+this.event2);
		System.out.println("Agent "+this.agent);
		System.out.println("Recipient "+this.recipient);
		System.out.println();
	}
	
	public String printLine(){

		String result=this.getEvent1()+" "+this.getAgent()+" "+this.getRecipient();
		
		if(this.negative!=null) result=this.negative+" "+result;
		return result;
	}
	
}
