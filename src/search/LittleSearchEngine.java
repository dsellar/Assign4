package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		// check if exists
		if (docFile == null || docFile.length() == 0) {
			throw new FileNotFoundException("File not found on disk");
		}
		// map for docFile
		HashMap<String, Occurrence> docMap = new HashMap<String, Occurrence>(500, 2.0f);
		try {
			// reads the docFile in
			BufferedReader filebr = new BufferedReader(new FileReader(docFile));
			String line = filebr.readLine();
			while (line != null) {
				StringTokenizer go = new StringTokenizer(line);
				// separates and checks words
				while (go.hasMoreTokens()) {
					String word = go.nextToken();
					word = getKeyWord(word);
					if (word != null && word.length() > 0) {
						if (docMap.containsKey(word)) {
							docMap.get(word).frequency++;
						} else {
							docMap.put(word, new Occurrence(docFile, 1));
						}
					}
				}
			line = filebr.readLine();
			}
		} catch (IOException i) {
			
		}
		return docMap;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		// variables
		Iterator<String> iter = kws.keySet().iterator();
		ArrayList<Occurrence> al = new ArrayList<Occurrence>();
		// goes through kws and adds into keywordsIndex
		while (iter.hasNext()) {
			al.clear();
			String key = iter.next();
			if (keywordsIndex.containsKey(key)) {
				keywordsIndex.get(key).add(kws.get(key));
				System.out.println(key);
//				insertLastOccurrence(keywordsIndex.get(key));
			} else {
				al.add(kws.get(key));
				keywordsIndex.put(key, al);
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		// checks end of word until only letters remain
		word = word.toLowerCase();
		while (word.length() > 0 && 
				!(Character.isLetter(word.charAt(word.length()-1))) &&
				!(Character.isDigit(word.charAt(word.length()-1)))) {
			if (word.endsWith(".") ||
				word.endsWith(",") ||
				word.endsWith("?") ||
				word.endsWith(":") ||
				word.endsWith(";") ||
				word.endsWith("!")) {
				word = word.substring(0, word.length()-1);
			} else {
				return null;
			}
		}
		// checks if noise word
		if (noiseWords.containsKey(word)) {
			return null;
		}
		// checks remaining letters for other characters
		for (int i = 0; i < word.length(); i++) {
			if (!(Character.isLetter(word.charAt(i)))) {
				return null;
			}
		}
		return word;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		ArrayList<Integer> pts = new ArrayList<Integer>();
		int lo = 0, hi = occs.size()-2, spot = occs.size()-1;
		while (lo <= hi) {
			int mid = (lo+hi)/2;
			pts.add(mid);
			if (occs.get(mid).frequency == occs.get(spot).frequency) {
				occs.add(mid, occs.get(spot));
				occs.remove(occs.size()-1);
			}
			if (occs.get(mid).frequency < occs.get(spot).frequency) {
				hi = mid + 1;
			} else {
				lo = mid - 1;
			}
		}
		occs.add(lo, occs.get(spot));
		occs.remove(occs.size()-1);
		return pts;
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<String> fin = new ArrayList<String>(5);
		// check if words are in the keywords table
		if (keywordsIndex.get(kw1) == null && keywordsIndex.get(kw2) == null) {
			return null;
		} else if (keywordsIndex.get(kw1) == null && keywordsIndex.get(kw2) != null) {
			for (int i = 0;i < 5;i++) {
				fin.add(i, keywordsIndex.get(kw2).get(i).document);
			}
			return fin;
		} else if (keywordsIndex.get(kw1) != null && keywordsIndex.get(kw2) == null) {
			for (int i = 0;i < 5;i++) {
				fin.add(i, keywordsIndex.get(kw1).get(i).document);
			}
			return fin;
		}
		// get AL for words
		int i = 0, x = 0;
		while ((fin.size() < 5) && (i < keywordsIndex.get(kw1).size()) && (x < keywordsIndex.get(kw2).size())) {
			if (keywordsIndex.get(kw1).get(i).frequency >= keywordsIndex.get(kw2).get(x).frequency) {
				if (check(fin, keywordsIndex.get(kw1).get(i).document)) {
					fin.add(keywordsIndex.get(kw1).get(i).document);
				}
				i++;
			} else {
				if (check(fin, keywordsIndex.get(kw2).get(x).document)) {
					fin.add(keywordsIndex.get(kw2).get(x).document);
				}
				x++;
			}
		}
		// adds rest on end if needed
		if (fin.size() < 5) {
			if (i < keywordsIndex.get(kw1).size() && !(x < keywordsIndex.get(kw2).size())) {
				while (fin.size() < 5 && i < keywordsIndex.get(kw1).size()) {
					if (check(fin, keywordsIndex.get(kw1).get(i).document)) {
						fin.add(keywordsIndex.get(kw1).get(i).document);
					}
					i++;
				}
			} else if (!(i < keywordsIndex.get(kw1).size()) && x < keywordsIndex.get(kw2).size()) {
				while (fin.size() < 5 && x < keywordsIndex.get(kw2).size()) {
					if (check(fin, keywordsIndex.get(kw2).get(x).document)) {
						fin.add(keywordsIndex.get(kw2).get(x).document);
					}
					x++;
				}
			}
		}
		return fin;
	}
	
	private boolean check(ArrayList<String> al, String docFile) {
		// check if al is empty
		if (al.size() == 0) {
			return true;
		}
		for (int i = 0; i < al.size(); i++) {
			if (al.get(i).equals(docFile)) {
				return false;
			}
		}
		return true;
	}
}
