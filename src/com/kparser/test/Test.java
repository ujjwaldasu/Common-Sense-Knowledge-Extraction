/**
 * 
 */
package com.kparser.test;

import java.util.ArrayList;
import java.util.List;

import module.graph.MakeGraph;
import module.graph.ParserHelper;
import module.graph.SentenceToGraph;
import module.graph.helper.GraphPassingNode;
import module.graph.helper.Node;
import module.graph.helper.NodePassedToViewer;
import module.graph.resources.DependencyParserResource;
import module.graph.resources.InputDependencies;

/**
 * @author arpit
 *
 */
public class Test {
	

	public static void main(String[] args){

//		Below code is used for extracting the graph for a given input sentence.
//		The sentence is: John loves Mia.
//		Output is a list of has(X,R,Y) strings, where each such string means that there is an edge labeled R between the nodes X and Y 
//		in the output graph
		SentenceToGraph stg = new SentenceToGraph();
		String sent = "Tom bullied Ron so we rescued Ron";
		
//		The output of below line of code is an object of GraphPassingNode class and has many things along with the below mentioned 
//		list of has strings.
//		extractGraph(sentence, for appending output with user defined indices (always false, ask Arpit if you want to use 
//		it),Word sense disambiguation flag (always true),coreference resolution flag (true if you want to resolve coreferences in the 
//		input));

/*
 * 
 * Can reason over this
 * */		
/*		GraphPassingNode gpn2 = stg.extractGraph(sent,false,true,false);
		ArrayList<String> list = gpn2.getAspGraph();
		
		for(String s : list){
			System.out.println(s);
		}

*/
//		Below code is a part of kparser.jar 
//		Below lines are used to get the syntactic dependency parse and POS maps of input sentence from Stanford Parser
//		DependencyParserResource dps = new DependencyParserResource();
//		InputDependencies inDeps = dps.extractDependencies(sent, false, 0);
		
		
/*	
 * Contains every relation isEvent etc	
 * 
*/
		MakeGraph mg = new MakeGraph();
		try{
			List<NodePassedToViewer> npvList=mg.createGraphUsingSentence(sent, false, false, false);
			Node startNode=npvList.get(0).getGraphNode();
			System.out.println(startNode);
		}	
		catch(Exception e){
			e.printStackTrace();
		}
		
		//Gets semantic graph in json object
		/*		ParserHelper ph =new ParserHelper();
		String jsonSent=ph.getJsonString(sent, false);
		System.out.println(jsonSent);
		System.exit(0);*/
	}

}
