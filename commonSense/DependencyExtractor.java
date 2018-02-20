package com.commonSense;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class DependencyExtractor {

	static Logger log = Logger.getLogger(DependencyExtractor.class.getName());
	static long count = 0;
	static TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	static GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	static Properties prop = null;
	final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	static List<String> copular_verb = null;

	private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(),
			"invertible=true");
	private static LexicalizedParser PARSER = null;
	private static List<HasWord> currentSentence = null;
	
	static{
		PARSER = LexicalizedParser.loadModel(PCG_MODEL);
	}
	
	public static LexicalizedParser getPARSER(){
		return PARSER;
	}

	public Tree parse(String str) {
		List<CoreLabel> tokens = tokenize(str);
		Tree tree = PARSER.apply(tokens);
		return tree;
	}

	public void parseFromFile(File currentFile) {
		System.out.println("###Current file [ " + currentFile.getAbsolutePath() + " ]###");
		log.debug("###Current file [ " + currentFile.getAbsolutePath() + " ]###");
		for (List<HasWord> sentence : new DocumentPreprocessor(currentFile.getAbsolutePath())) {
			parseSentence(currentFile, sentence);
		}

	}

	public static void parseSentence(File currentFile, List<HasWord> sentence) {
		try {
			currentSentence = sentence;
			Tree tree = PARSER.apply(sentence);
			getKnowledgeFromTree(currentFile, tree);
		} catch (Exception ex) {
			log.error("!!! Exception caused !!!");
			log.error("!!! [ " + ex.getMessage() + " ] !!!");
			log.error("Exception caused in file [ " + currentFile + " ]");
			if (currentSentence != null)
				log.error("Line that caused exception [ " + extractSentence(sentence) + " ]");
			System.out.println("!!! Exception caused !!!");
			System.out.println("!!! [ " + ex.getMessage() + " ] !!!");
			System.out.println("Exception caused in file [ " + currentFile + " ]");
			if (currentSentence != null)
				System.out.println("Line that caused exception [ " + extractSentence(sentence) + " ]");
		}
	}

	public List<CoreLabel> tokenize(String str) {
		Tokenizer<CoreLabel> tokenizer = tokenizerFactory.getTokenizer(new StringReader(str));
		return tokenizer.tokenize();
	}

	public void startParsing() {
		try {
			loadPropertyFile("config.properties");
			Iterator<File> iter = getCorpusFiles();
			getKnowledgeFromFiles(iter);
			log.info("##### knowledge count: " + count + "#####");
			log.info("###### END ######");
			System.out.println("##### knowledge count: " + count + "#####");
			System.out.println("###### END ######");
		} catch (FileNotFoundException f) {
			log.error(f.toString());
		} catch (IOException i) {
			log.error(i.toString());
		}
	}

	public void loadPropertyFile(String fileName) throws IOException, FileNotFoundException {
		prop = new Properties();
		FileInputStream input = new FileInputStream(fileName);
		prop.load(input);
		copular_verb = Arrays.asList(prop.getProperty("COPULAR_VERBS").split(","));
		log.debug("###Properties file loaded successfully### ");
	}

	public Iterator<File> getCorpusFiles() {
		String accepted_file_format = prop.getProperty("FILE_FORMAT");
		String corpus_directory = prop.getProperty("CORPORA_DIRECTORY");
		log.debug("###Corpus Directory [ " + corpus_directory + " ]###");
		log.debug("###Accepted File Format [ " + accepted_file_format + " ]###");
		String[] patron = accepted_file_format.split(",");
		Iterator<File> iter = FileUtils.iterateFiles(new File(corpus_directory), patron, true);
		log.debug("###Returning Corpus File List###");
		return iter;
	}

	public void getKnowledgeFromFiles(Iterator<File> iter) throws FileNotFoundException, IOException {
		while (iter.hasNext()) {
			File currentFile = iter.next();
			// TODO: Set an exception here parses the whole while and returns trees for every sentence
			try{
				parseFromFile(currentFile);
			} catch (Exception e) {
				log.error("Error Reading file" +e.getMessage());
				System.out.println("Error Reading file" +e.getMessage());
			}
			
		}

	}

	/**
	 * @param currentFile
	 * @param t
	 */
	private static void getKnowledgeFromTree(File currentFile, Tree t) {
		List<TypedDependency> dependencyList;
		if (t != null) {
			dependencyList = parseSingleTree(t);
			IndexedWord verb1 = null;
			IndexedWord verb2 = null;
			boolean mark = false;
			for (TypedDependency dependency1 : dependencyList) {
				if (dependency1.reln().getShortName().equals("advcl")) {
					if (!copular_verb.contains(dependency1.gov().word())
							&& !copular_verb.contains(dependency1.dep().word())
							&& dependency1.gov().tag().startsWith("VB") && dependency1.dep().tag().startsWith("VB")) {
						verb1 = dependency1.gov();
						verb2 = dependency1.dep();
						break;
					}
				}
			}

			if (verb1 != null && verb2 != null) {
				for (TypedDependency dependency : dependencyList) {
					if (dependency.reln().getShortName().equals("mark") && (dependency.gov().compareTo(verb2) == 0)) {
						String discourseConnector = dependency.dep().word();
						List<String> discourseConnectorList = Arrays
								.asList(prop.getProperty("DISCOURSE_CONNECTORS").split(","));
						if (discourseConnectorList.contains(discourseConnector)) {
							mark = true;
							break;
						}
					}
				}
			}

			if (verb1 != null && verb2 != null && mark) {
				// find nsubj and dobj of verb1 and verb2
				IndexedWord nsubjverb1 = null;
				IndexedWord dobjverb1 = null;
				IndexedWord nsubjverb2 = null;
				IndexedWord dobjverb2 = null;
				for (TypedDependency dependency : dependencyList) {
					// get nsubj, dobj of verb1 and verb2
					if (dependency.reln().getShortName().equals("nsubj")) {
						if (dependency.gov().compareTo(verb1) == 0)
							nsubjverb1 = dependency.dep();
						else if (dependency.gov().compareTo(verb2) == 0)
							nsubjverb2 = dependency.dep();
					}
					if (dependency.reln().getShortName().equals("dobj")) {
						if (dependency.gov().compareTo(verb1) == 0)
							dobjverb1 = dependency.dep();
						else if (dependency.gov().compareTo(verb2) == 0)
							dobjverb2 = dependency.dep();
					}
				}
				// if nsubj(verb1) == nsubj(verb2), then print it
				if (nsubjverb1 != null && nsubjverb2 != null && nsubjverb1.word().equals(nsubjverb2.word())) {
					printOutput(currentFile, dependencyList, verb1, verb2, nsubjverb1, nsubjverb2, "nsubj", "nsubj");
				}
				// if nsubj(verb1) == dobj(verb2), then print it
				if (nsubjverb1 != null && dobjverb2 != null && nsubjverb1.word().equals(dobjverb2.word())) {
					printOutput(currentFile, dependencyList, verb1, verb2, nsubjverb1, dobjverb2, "nsubj", "dobj");
				}
				// if dobj(verb1) == dobj(verb2), then print it
				if (dobjverb1 != null && dobjverb2 != null && dobjverb1.word().equals((dobjverb2.word()))) {
					printOutput(currentFile, dependencyList, verb1, verb2, dobjverb1, dobjverb2, "dobj", "dobj");
				}
				// if dobj(verb1) == nsubj(verb2), then print it
				if (dobjverb1 != null && nsubjverb2 != null && dobjverb1.word().equals(nsubjverb2.word())) {
					printOutput(currentFile, dependencyList, verb1, verb2, dobjverb1, nsubjverb2, "dobj", "nsubj");
				}
			}

		}
	}

	/**
	 * @param currentFile
	 * @param t
	 * @param dependencyList
	 * @param verb1
	 * @param verb2
	 * @param nsubjverb1
	 * @param nsubjverb2
	 */
	private static void printOutput(File currentFile, List<TypedDependency> dependencyList, IndexedWord verb1,
			IndexedWord verb2, IndexedWord verb1Dep, IndexedWord verb2Dep, String verb1GramRel, String verb2GramRel) {
		// found 1 knowledge, increment count;
		count++;
		String knowledge = null;
		IndexedWord neg1 = null;
		IndexedWord neg2 = null;
		// get negations for verb1 and verb2 if any
		for (TypedDependency dependency : dependencyList) {
			if (dependency.reln().getShortName().equals("neg") && dependency.gov().compareTo(verb1) == 0)
				neg1 = dependency.dep();
			if (dependency.reln().getShortName().equals("neg") && dependency.gov().compareTo(verb2) == 0)
				neg2 = dependency.dep();
		}
		// compare subject objects of both verbs and print output
		log.debug("#########################");
		log.debug("###Found Knowledge###");
		log.debug("#File Name [ " + currentFile.getName() + " ] #");
		String sentenceinString = extractSentence(currentSentence);
		log.debug(count + ". Sentence that gave knowledge [ " + sentenceinString + " ]#");
		System.out.println(count + ". Sentence that gave knowledge [ " + sentenceinString + " ]#");
		if (neg1 != null && neg2 != null) {
			knowledge = count + ": If " + neg1.word() + " " + verb1.word() + " causes " + neg2.word() + " "
					+ verb2.word() + " then " + verb1GramRel + " of " + neg1.word() + " " + verb1.word()
					+ " is same as " + verb2GramRel + " of " + neg2.word() + " " + verb2.word();
			log.info(knowledge);
			System.out.println(knowledge);
			// System.out.println("not verb 1 " + verb1GramRel + " = not verb 2
			// " + verb2GramRel);
			// log.info("not verb 1 " + verb1GramRel + " = not verb 2 " +
			// verb2GramRel);
		} else if (neg1 == null && neg2 == null) {
			knowledge = count + ": If " + verb1.word() + " causes " + verb2.word() + " then " + verb1GramRel + " of "
					+ verb1.word() + " is same as " + verb2GramRel + " of " + verb2.word();
			log.info(knowledge);
			System.out.println(knowledge);
			// System.out.println("verb 1 " + verb1GramRel + " = verb 2 " +
			// verb2GramRel);
			// log.info("verb 1 " + verb1GramRel + " = verb 2 " + verb2GramRel);
		} else if (neg1 != null && neg2 == null) {
			knowledge = count + ": If " + neg1.word() + " " + verb1.word() + " causes " + verb2.word() + " then "
					+ verb1GramRel + " of " + neg1.word() + " " + verb1.word() + " is same as " + verb2GramRel + " of "
					+ verb2.word();
			log.info(knowledge);
			System.out.println(knowledge);
			// System.out.println("not verb 1 " + verb1GramRel + " = verb 2 " +
			// verb2GramRel);
			// log.info("not verb 1 " + verb1GramRel + " = verb 2 " +
			// verb2GramRel);
		} else if (neg1 == null && neg2 != null) {
			knowledge = count + ": If " + verb1.word() + " causes " + neg2.word() + " " + verb2.word() + " then "
					+ verb1GramRel + " of " + verb1.word() + " is same as " + verb2GramRel + " of " + neg2.word() + " "
					+ verb2.word();
			log.info(knowledge);
			System.out.println(knowledge);
			// System.out.println("verb 1 " + verb1GramRel + " = not verb 2 " +
			// verb2GramRel);
			// log.info("verb 1 " + verb1GramRel + " = not verb 2 " +
			// verb2GramRel);
		}
		knowledge+="\n"+sentenceinString;
		writeKnowledgeInFile(knowledge);
	}

	/**
	 * @return
	 */
	private static String extractSentence(List<HasWord> sentence) {
		String sentenceinString = "";
		for (HasWord w : sentence) {
			sentenceinString = sentenceinString + w.word() + " ";
		}
		return sentenceinString;
	}

	public List<TypedDependency> parseSentence(String sentence) {
		// sentence = "Tom bullied Rom so we rescued Ron";
		log.info("###Given sentence to be parsed [ " + sentence + " ]###");
		Tree parse = parse(sentence);
		// parse.pennPrint();

		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		log.info("###Returned Typed Dependency List###");
		return tdl;
		// TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
		// tp.printTree(parse);

	}

	public static List<TypedDependency> parseSingleTree(Tree tree) {
		GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		// TODO: use debug for logging
		// log.debug("###Returned Typed Dependency List###");
		return tdl;
		// TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
		// tp.printTree(parse);

	}

	public static void writeKnowledgeInFile(String knowledge) {
		FileWriter fw = null;
		String dirString = prop.getProperty("OUTPUT_FILE_LOCATION");
		File dir = new File(dirString);
		String fileString = prop.getProperty("OUTPUT_FILE_LOCATION") + System.getProperty("file.separator")
				+ prop.getProperty("OUTPUT_FILE_NAME");
		try {
			if (dir.exists()) {
				File outputFile = new File(fileString);
				if (!outputFile.exists()) {
					log.debug("Output file doesn't exists.Creating a output file with name [ "
							+ fileString.substring(fileString.lastIndexOf(System.getProperty("file.separator")))
							+ " ] at location [ " + dirString + " ]");
					outputFile.createNewFile();

				}
				if (outputFile.exists()) {
					fw = new FileWriter(fileString, true);
					fw.write(knowledge + "\n");
					fw.close();
				}
			} else {
				log.error("Output File directory [ " + dir
						+ " ] doesn't exists. Unable to create output file. Refer log file for Information");
			}
		} catch (IOException e) {
			log.error("Unable to create file. " + e.getMessage());
		}
	}
}