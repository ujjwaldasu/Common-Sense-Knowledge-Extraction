package com.kparser.reasoning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;


public class FileIteratorForNLP implements java.util.Iterator{
	private Properties prop = null;
	private static Logger log = Logger.getLogger(FileIteratorForNLP.class.getName());
	public String currentSentence;
	private File currentFile;
	
	public Iterator<File> getFileIter(){
		Iterator<File> iter = null;
		try{
			loadPropertyFile("config.properties");
			iter = getCorpusFiles();
			//sentenceIterator(iter);
		} catch(FileNotFoundException f){
			log.error(f.toString());
		} catch(IOException i){
			log.error(i.toString());
		}
		return iter;
	}
	
	public void sentenceIterator(Iterator<File> fileIter){
		while(fileIter.hasNext()){
			currentFile = fileIter.next();
			// parses the whole while and returns trees for every sentence

			for(List<HasWord> sentence : new DocumentPreprocessor(currentFile.getAbsolutePath())) {
				currentSentence = extractSentence(sentence);
				
			}
		}
	}
	/**
	 * @return
	 */
	public  String extractSentence(List<HasWord> currentSentence) {
		String sentenceinString = "";
		for(HasWord w: currentSentence){
			sentenceinString = sentenceinString+w.word() + " ";
		}
		return sentenceinString;
	}
	
	private void loadPropertyFile(String fileName) throws IOException,FileNotFoundException{
		prop = new Properties();
		FileInputStream input = new FileInputStream(fileName);
		prop.load(input);
		log.info("###Properties file loaded successfully### ");
	}
	
	private Iterator<File> getCorpusFiles(){
		String accepted_file_format = prop.getProperty("FILE_FORMAT");
		String corpus_directory = prop.getProperty("CORPORA_DIRECTORY");
		log.info("###Corpus Directory [ "+corpus_directory+" ]###");
		log.info("###Accepted File Format [ "+accepted_file_format+" ]###");
		String[] patron = accepted_file_format.split(",");
		Iterator<File> iter =  FileUtils.iterateFiles(new File(corpus_directory), patron, true);
		log.info("###Returning Corpus File List###");
		return iter;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object next() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getOutputFilePath(){
		String fileString = prop.getProperty("OUTPUT_FILE_LOCATION") + System.getProperty("file.separator")
				+ prop.getProperty("OUTPUT_FILE_NAME");
		return fileString;
	}
	public String getCopular(){
		String cop=prop.getProperty("COPULAR_VERBS");
		return cop;
	}
}
