/**
 * 
 */
package com.kparser.reasoning;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.Map;

import com.commonSense.DependencyExtractor;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import module.graph.MakeGraph;
import module.graph.SentenceToGraph;
import module.graph.helper.GraphPassingNode;
import module.graph.helper.Node;
import module.graph.helper.NodePassedToViewer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class HasReasoning {
	
	static Map<String,Integer> eventRelMap=new HashMap<String,Integer>();
	static Map<String,Integer> reverseEvent=new HashMap<String,Integer>();
	static String COP="";
	static MakeGraph mg = new MakeGraph();
	static int COUNTER;
	
	public HasReasoning(){
		//Adding possible events
		eventRelMap.put("causes",1);
		eventRelMap.put("caused_by",2);
		eventRelMap.put("enables",3);
		eventRelMap.put("enabled_by",4);
		eventRelMap.put("objective",5);
		eventRelMap.put("next_event",6);
		eventRelMap.put("previous",7);
		eventRelMap.put("event",8);
		eventRelMap.put("resulting_state",9);
		eventRelMap.put("subevent",10);
		eventRelMap.put("inibits",11);
		eventRelMap.put("inhibited_by",12);
		
		//Adding reverse causal relations
		reverseEvent.put("caused_by",1);
		reverseEvent.put("enabled_by",2);
		reverseEvent.put("inhibited_by",3);
		reverseEvent.put("previous",4);

		//Counter initialize
		COUNTER=0;
	}
	
	public static void main(String args[]){
		
//		HasReasoning hr=new HasReasoning();
//		String sent = "Sponge gathering was devastated after a fungus attacked the crop  ,  and sisal was produced more cheaply in South_America .";
////		String sent="A couple of Shehhi 's colleagues were obviously unused to travel ; according to the United ticket agent , they had trouble understanding the standard security questions , and she had to go over them slowly until they gave the routine , reassuring answers.";
//		String sent1="The article then quotes a retired banker in Amherst who said he was angry at the poor example Clinton has set for children , and that since he is making character his top priority , he 's voting for George W. Bush . ";
//		String sent2="More and more, since I quit reviewing books, I think that's the job of fiction.";
//		String sent3="If you kill your mother-in-law because you hate her , that is not a hate crime . ";
//		String sent4="I confess I read this book with some trepidation because I have just completed a book that attempts to answer just these questions . ";
//
//		List<HasObject> hoList= null;
//
//		FileIteratorForNLP fi = new FileIteratorForNLP();
//		Iterator<File> fileIter = fi.getFileIter();
//		COP=fi.getCopular();
//		System.out.println(COP);
//		hoList=hr.getRelations(sent);
//		
//		for(String s:hr.reasonOverHasObjects(hoList)){
//			System.out.println(s);
//		}

		startExtraction();
	}
	public static void startExtraction(){

		HasReasoning hr=new HasReasoning();
		try{
			List<HasObject> hoList=null; 

			FileIteratorForNLP fi = new FileIteratorForNLP();
			Iterator<File> fileIter = fi.getFileIter();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter (fi.getOutputFilePath()));
			
			//Set copular verbs
			COP=fi.getCopular();
			while(fileIter.hasNext()){
				File currentFile = fileIter.next();
				// parses the whole while and returns trees for every sentence
				System.out.println("###Current file [ " + currentFile.getAbsolutePath() + " ]###");
				for(List<HasWord> sentence : new DocumentPreprocessor(currentFile.getAbsolutePath())) {
					String currentSentence = fi.extractSentence(sentence);
//					String currentSentence = "Sponge gathering was devastated after a fungus attacked the crop  ,  and sisal was produced more cheaply in South_America .";
					try {
						hoList=hr.getRelations(currentSentence);
						for(String s : hr.reasonOverHasObjects(hoList)){
							COUNTER++;
							bw.write(COUNTER+".S: "+currentSentence);
							bw.write(System.getProperty("line.separator"));
							bw.write("  K: If " + s);
							bw.write(System.getProperty("line.separator"));
							bw.write(System.getProperty("line.separator"));
							bw.flush();
							System.out.println(COUNTER+" "+s);
							
						}
					}  catch (OutOfMemoryError e1){
						System.out.println("System overhead!!");
						System.gc();
							e1.printStackTrace();
					}catch (Exception e1){
						// do stanford parsing here
//						try {
//							DependencyExtractor.parseSentence(currentFile, sentence);
//						} catch (Exception e2) {
						System.gc();
							e1.printStackTrace();
//						}
					}
				}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Total Knowledge Instances Extracted "+COUNTER);
		
	}
	
	public HasObject getNodeDetails(Node node){
		
		if(node.isAnEvent()){
			HasObject ho=new HasObject();
			//Set event1
			ho.setEvent1(node.getValue());
			
			//Go through its edgeList
			ArrayList<String> edgeList=node.getEdgeList();
			ArrayList<Node> children=node.getChildren();
			
			for(int i=0;i<edgeList.size();i++){
				
				//Get Recepient
				if(edgeList.get(i).equals("recipient")) {
					ho.setRecipient(children.get(i).getValue());
				}
				
				//Get Agent
				else if(edgeList.get(i).equals("agent")) {
					ho.setAgent(children.get(i).getValue());
				}
				
				//Get Causal Event if its a causal relation
				else if(eventRelMap.get(edgeList.get(i))!=null){
					ho.setEvent2(children.get(i).getValue());
					
					//Set the reverse flag in case of caused by relation
					if(reverseEvent.get(edgeList.get(i))!=null){
						ho.setReverse(true);
					}
				}
				
				//Get Negative
				else if(edgeList.get(i).equals("negative")){
					ho.setNegative(children.get(i).getValue());
				}
				
				
				//Get Instance of
				else if(edgeList.get(i).equals("instance_of")){
					ho.setInstance_of(children.get(i).getValue());
				}
			
			}

			return ho;
		}
		return null;
	}
	
	
	public List<String> reasonOverHas(String sent){

		SentenceToGraph stg = new SentenceToGraph();

/*		The output of below line of code is an object of GraphPassingNode class and has many things along with the below mentioned 
		list of has strings.
		extractGraph(sentence, for appending output with user defined indices (always false, ask Arpit if you want to use 
		it),Word sense disambiguation flag (always true),coreference resolution flag (true if you want to resolve coreferences in the 
		input));
*/
		GraphPassingNode gpn2 = stg.extractGraph(sent,false,true,false);
		ArrayList<String> list = gpn2.getAspGraph();
		
		return list;

	}
	
	public void dfs(Node node,List<Node> eventNodes){
		if(node!=null){
			if(node.isAnEvent()){
				eventNodes.add(node);	
			}
			
			for(Node n:node.getChildren()){
				dfs(n,eventNodes);
			}
			
		}		
	}
	
	public List<HasObject> getRelations(String sent) throws Exception{
		List<HasObject> ho=new ArrayList<HasObject>();
		try{
			List<NodePassedToViewer> npvList=mg.createGraphUsingSentence(sent, false, false, false);
			List<Node> eventNode=new ArrayList<Node>();
			
			//Get all the events
			for(NodePassedToViewer n :npvList){
				Node startNode=n.getGraphNode();
				dfs(startNode,eventNode);
								
			}
			
			//get the details of all the events
			for(Node startNode:eventNode){
				if(startNode!=null){
					HasObject h=getNodeDetails(startNode);
					ho.add(h);
				}

			}

		} catch (java.lang.StringIndexOutOfBoundsException e){
			throw e;
		}
		catch(Exception e){
			throw e;
		}
		return ho;
		
	}
	
	public List<String> reasonOverHasObjects(List<HasObject> hoList){
		List<String> result=new ArrayList<String>();
		
		Map<String,HasObject> eventMap=new HashMap<String,HasObject>();
		for(HasObject ho:hoList){
			eventMap.put(ho.getEvent1(), ho);
		}
		
		for(HasObject ho:hoList){
			HasObject ho2=eventMap.get(ho.getEvent2());
			
			if(ho2!=null && !isCopular(ho,ho2)){
				
				//reverse if is an reverse relation
				if(ho.isReverse()){
					HasObject temp=ho2;
					ho2=ho;
					ho=temp;
				}
				
				//Agents 1 Agent 2
				if(areSame(ho.getAgent(),ho2.getAgent())){
					result.add(ho.getNegEvent1()+" causes "+ho2.getNegEvent1()+" then agent of "+ho.getNegEvent1()+" is the same as agent of "+ho2.getNegEvent1());
				}
				
				//Recipient 1 Recipient 2
				if(areSame(ho.getRecipient(),ho2.getRecipient())){
					result.add(ho.getNegEvent1()+" causes "+ho2.getNegEvent1()+" then recipient of "+ho.getNegEvent1()+" is the same as recipient of "+ho2.getNegEvent1());
				}
				
				//Recipient 1 Agents 2 
				if(areSame(ho.getRecipient(),ho2.getAgent())){
					result.add(ho.getNegEvent1()+" causes "+ho2.getNegEvent1()+" then recipient of "+ho.getNegEvent1()+" is the same as agent of "+ho2.getNegEvent1());
				}
				
				//Agent 1 Recipient 2
				if(areSame(ho.getAgent(),ho2.getRecipient())){
					result.add(ho.getNegEvent1()+" causes "+ho2.getNegEvent1()+" then agent of "+ho.getNegEvent1()+" is the same as recipient of "+ho2.getNegEvent1());
				}

			}
			//if(ho2!=null) result.add(ho.printLine()+"\n"+ho2.printLine());
		}
		
		
		return result;
	}
	
	public boolean areSame(String s1,String s2){
		if(s1!=null && s2!=null){
			return s1.split("-")[0].equalsIgnoreCase(s2.split("-")[0]);
		}
		return false;
	}
	
	public boolean isCopular(HasObject ho,HasObject ho2){

		boolean result=false;
		try {
			if(COP.contains(ho.getEvent1().split("-")[0].toLowerCase()) || COP.contains(ho2.getEvent1().split("-")[0].toLowerCase()))
				result=true;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

}
