import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.language.Soundex;
import org.tartarus.snowball.ext.EnglishStemmer;

public class TextNormalization {

	boolean mDebug = false;
	Map<String, List<String>> mMappedDoubles;

	public TextNormalization() throws IOException {
		mMappedDoubles = new HashMap<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass()
				.getClassLoader().getResourceAsStream("wordsWithDoubles.txt")));
		fillDoubleWords(br);
		br.close();
	}

	public TextNormalization(String wordsWithDoublesPath) throws IOException {
		// TODO Auto-generated constructor stub
		mMappedDoubles = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(
				wordsWithDoublesPath));
		fillDoubleWords(br);
		br.close();
	}

	public void setDebug(boolean debug) {
		mDebug = debug;
	}

	private void fillDoubleWords(BufferedReader br) throws IOException {
		String line = null;
		Soundex encoder = new Soundex();
		while ((line = br.readLine()) != null) {
			// Avoid problems with BufferedReader capacity per line?
			String[] wordsBuf = line.split(", ");
			for (String word : wordsBuf) {
				String encodedWord = encoder.encode(word);
				if (mMappedDoubles.containsKey(encodedWord)) {
					if (mDebug) {
						System.out.println("Repeated key: ");
						System.out.println("\tKey: " + encodedWord);
						System.out.println("\tOld value: "
								+ mMappedDoubles.get(encodedWord));
						System.out.println("\tNew value: " + word);
					}
					mMappedDoubles.get(encodedWord).add(word);
				} else {
					List<String> words = new ArrayList<>();
					words.add(word);
					mMappedDoubles.put(encodedWord, words);
				}
			}
		}
	}

	public String normalize(String token, boolean stem) {
		EnglishStemmer stemmer = new EnglishStemmer();
		String biasedString = token.toLowerCase().replaceAll("[\\W]", "");
		i.e. barring = bar, occuring = occur, patrolling = patrol
		stemmer.setCurrent(biasedString);
		stemmer.stem();
		biasedString = stemmer.getCurrent();
		find repeated letters
		Matcher matcherString = Pattern.compile("(.)\\1+\\1").matcher(
				biasedString);
		List<String> groups = new ArrayList<>();
		while (matcherString.find()) {
			groups.add(matcherString.group());
		}
		if repeated letters exist
		if (!groups.isEmpty()) {
			String normalizedString = biasedString;
			String unbiasedString = biasedString;
			for (String group : groups) {
				normalizedString = normalizedString.replace(group,
						group.subSequence(0, 1));
				unbiasedString = unbiasedString.replace(group,
						group.subSequence(0, 2));
				biasedString = biasedString.replace(group,
						group.substring(0, 3));
			}
			Soundex filter = new Soundex();
			String encodedString = filter.encode(unbiasedString);
			if (mMappedDoubles.containsKey(encodedString)) {
				String result = "";
				int lastMatchLength = 0;
				for (String word : mMappedDoubles.get(encodedString)) {
					String res = org.apache.commons.collections4.ListUtils
							.longestCommonSubsequence(word, unbiasedString);
					if (mDebug) {
						System.out.println("word: " + word + " res: " + res
								+ " len: " + res.length());
						System.out.println("\tcurrent result: " + result);
					}
					if (res.length() > 3 && res.length() > lastMatchLength) {
						result = word;
						lastMatchLength = res.length();
					}
				}
				
				if (result.isEmpty()) {
					if (stem) {
						stemmer.setCurrent(normalizedString);
						stemmer.stem();
						result = stemmer.getCurrent();
					} else {
						result = normalizedString;
					}
				} else if (stem) {
					stemmer.setCurrent(result);
					stemmer.stem();
					result = stemmer.getCurrent();
				}
				if (mDebug) {
					System.out.println();
					System.out.println("Normalized: " + result);
					System.out.println("unBiased: " + unbiasedString);
					System.out.println("Biased: " + biasedString);
				}
				return result;
			} else if (stem) {
				stemmer.setCurrent(normalizedString);
				stemmer.stem();
				return stemmer.getCurrent();
			} else {
				return normalizedString;
			}
		} else if (stem) {
			return biasedString;
		} else {
			return token;
		}
	}

	
	public static String[] normalizeCombination(String token, boolean stem) {
		EnglishStemmer stemmer = new EnglishStemmer();
		String biasedString = token.toLowerCase().replaceAll("[\\W]", "");
		// i.e. barring = bar, occuring = occur, patrolling = patrol
		// stemmer.setCurrent(biasedString);
		// stemmer.stem();
		// biasedString = stemmer.getCurrent();
		Matcher matcherString = Pattern.compile("(.)\\1+\\1").matcher(
				biasedString);
		List<String> groups = new ArrayList<>();
		Set<String> possibilities = new HashSet<>();
		while (matcherString.find()) {
			groups.add(matcherString.group());
		}
		if (!groups.isEmpty()) {
			String unbiasedString = biasedString;
			String totwo = biasedString;
			for (String group : groups) {
				if (group.length() > 3) {
					unbiasedString = unbiasedString.replace(group,
							group.substring(0, 3));
				}
				totwo = totwo.replace(group, group.substring(0, 2));
			}
			if (stem) {
				stemmer.setCurrent(totwo);
				stemmer.stem();
				possibilities.add(stemmer.getCurrent());
			} else {
				possibilities.add(totwo);
			}
			for (int i = 0; i < groups.size(); ++i) {
				String group = groups.get(i).substring(0, 2);
				String reduction = totwo
						.replaceFirst(group, group.substring(1));
				if (stem) {
					stemmer.setCurrent(reduction);
					stemmer.stem();
					possibilities.add(stemmer.getCurrent());
				} else {
					possibilities.add(reduction);
				}
				for (String g : groups.subList(i, groups.size())) {
					g = g.substring(0, 2);
					String reduct = reduction.replaceFirst(g, g.substring(1));
					if (stem) {
						stemmer.setCurrent(reduct);
						stemmer.stem();
						possibilities.add(stemmer.getCurrent());
					} else {
						possibilities.add(reduct);
					}
				}
			}
			return possibilities.toArray(new String[] {});
		} else if (stem) {
			stemmer.setCurrent(biasedString);
			stemmer.stem();
			return new String[] { stemmer.getCurrent() };
		} else {
			return new String[] { token }; // Token doesn't need normalization
		}
	}

	public static void main(String[] args) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		TextNormalization tn = new TextNormalization();
		while (!(line = br.readLine()).equals("q")) {
			System.out.println("Normalize phonetic: "
					+ tn.normalize(line, true));
			System.out.println("Normalize combination: ");
			for (String s : TextNormalization.normalizeCombination(line, true)) {
				System.out.println("\t" + s);
			}
		}

	}
}
