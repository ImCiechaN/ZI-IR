import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MovieReviewStatictics
{
    private static final String DOCUMENTS_PATH = "movies/";
    private int _verbCount = 0;
    private int _nounCount = 0;
    private int _adjectiveCount = 0;
    private int _adverbCount = 0;
    private int _totalTokensCount = 0;

    private PrintStream _statisticsWriter;

    private SentenceModel _sentenceModel;
    private TokenizerModel _tokenizerModel;
    private DictionaryLemmatizer _lemmatizer;
    private PorterStemmer _stemmer;
    private POSModel _posModel;
    private TokenNameFinderModel _peopleModel;
    private TokenNameFinderModel _placesModel;
    private TokenNameFinderModel _organizationsModel;

    public static String TOKENIZER_MODEL = "models/en-token.bin";
    public static String SENTENCE_MODEL = "models/en-sent.bin";
    public static String POS_MODEL = "models/en-pos-maxent.bin";
    public static String LEMMATIZER_DICT = "models/en-lemmatizer.dict";
    public static String NAME_MODEL = "models/en-ner-person.bin";
    public static String LOCATION_MODEL = "models/en-ner-location.bin";
    public static String ORGANIZATION_MODEL = "models/en-ner-organization.bin";

    public static void main(String[] args)
    {
        MovieReviewStatictics statistics = new MovieReviewStatictics();
        statistics.run();
    }

    private void run()
    {
        try
        {
            initModelsStemmerLemmatizer();

            File dir = new File(DOCUMENTS_PATH);
            File[] reviews = dir.listFiles((d, name) -> name.endsWith(".txt"));

            _statisticsWriter = new PrintStream("statistics.txt", "UTF-8");

            Arrays.sort(reviews, Comparator.comparing(File::getName));
            for (File file : reviews)
            {
                System.out.println("Movie: " + file.getName().replace(".txt", ""));
                _statisticsWriter.println("Movie: " + file.getName().replace(".txt", ""));

                String text = new String(Files.readAllBytes(file.toPath()));
                processFile(text);

                _statisticsWriter.println();
            }

            overallStatistics();
            _statisticsWriter.close();

        } catch (IOException ex)
        {
            Logger.getLogger(MovieReviewStatictics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initModelsStemmerLemmatizer()
    {
        try
        {
            //todo: load all opennlp models (+porter stemmer + lemmatizer)
            //from files (use class variables)
            _tokenizerModel = new TokenizerModel(new File(TOKENIZER_MODEL));
            _sentenceModel = new SentenceModel(new File(SENTENCE_MODEL));
            _posModel = new POSModel(new File(POS_MODEL));
            _stemmer = new PorterStemmer();
            _lemmatizer = new DictionaryLemmatizer(new File(LEMMATIZER_DICT));
            _peopleModel = new TokenNameFinderModel(new File(NAME_MODEL));
            _placesModel = new TokenNameFinderModel(new File(LOCATION_MODEL));
            _organizationsModel = new TokenNameFinderModel(new File(ORGANIZATION_MODEL));

        }
        catch (IOException ex)
        {
            Logger.getLogger(MovieReviewStatictics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processFile(String text)
    {
        // TODO: process the text to find the following statistics:
        // For each movie derive:
        //    - number of sentences
        int noSentences = 0;
        //    - number of tokens
        int noTokens = 0;
        //    - number of (unique) stemmed forms
        int noStemmed = 0;
        //    - number of (unique) words from a dictionary (lemmatization)
        int noWords = 0;
        //    -  people
        Span people[] = new Span[] { };
        //    - locations
        Span locations[] = new Span[] { };
        //    - organisations
        Span organisations[] = new Span[] { };

        // TODO + compute the following overall (for all movies) POS tagging statistics:
        //    - percentage number of adverbs (class variable, private int _verbCount = 0)
        //    - percentage number of adjectives (class variable, private int _nounCount = 0)
        //    - percentage number of verbs (class variable, private int _adjectiveCount = 0)
        //    - percentage number of nouns (class variable, private int _adverbCount = 0)
        //    + update _totalTokensCount
        // ------------------------------------------------------------------
        ////////////////////////////////////////////////HEREEEEEEEEEEEEEEEEEEEEEEEEEEEE
        // TODO derive sentences (update noSentences variable)
        SentenceDetectorME sent_me = new SentenceDetectorME(_sentenceModel);
        noSentences = sent_me.sentDetect(text).length;

        // TODO derive tokens and POS tags from text
        // (update noTokens and _totalTokensCount)
        TokenizerME token_me = new TokenizerME(_tokenizerModel);
        String[] tokens= token_me.tokenize(text);
        noTokens = tokens.length;
        _totalTokensCount += noTokens;
        POSTaggerME pos_me =  new POSTaggerME(_posModel);
        String[] pos_tags = pos_me.tag(tokens);

        // TODO perform stemming (use derived tokens)
        // (update noStemmed)
        Set <String> stems = new HashSet <>();
        for (String token : tokens)
        {
            stems.add(_stemmer.stem(token).toLowerCase().replaceAll("[^a-z0-9]", ""));
        }
        noStemmed = stems.size();


        // TODO perform lemmatization (use derived tokens)
        // (remove "O" from results - non-dictionary forms, update noWords)
        Set <String> words = new HashSet <String>(Arrays.asList(_lemmatizer.lemmatize(tokens, pos_tags)));
        words.remove("O");
        noWords = words.size();
        // TODO derive people, locations, organisations (use tokens),
        // (update people, locations, organisations lists).
        NameFinderME name_me = new NameFinderME(_peopleModel);
        people = name_me.find(tokens);
        NameFinderME org_me = new NameFinderME(_organizationsModel);
        organisations = org_me.find(tokens);
        NameFinderME loc_me = new NameFinderME(_placesModel);
        locations = loc_me.find(tokens);
        // TODO update overall statistics - use tags and check first letters
        // (see https://www.clips.uantwerpen.be/pages/mbsp-tags; first letter = "V" = verb?)
        for (String pos : pos_tags){
            if(pos.startsWith("V") || pos.startsWith("M")){  _verbCount++; }
            else if(pos.startsWith("N")){ _nounCount++; }
            else if(pos.startsWith("J")){ _adjectiveCount++; }
            else if(pos.startsWith("R")){ _adverbCount++; }
        }
        // ------------------------------------------------------------------

        saveResults("Sentences", noSentences);
        saveResults("Tokens", noTokens);
        saveResults("Stemmed forms (unique)", noStemmed);
        saveResults("Words from a dictionary (unique)", noWords);

        saveNamedEntities("People", people, tokens);
        saveNamedEntities("Locations", locations, tokens);
        saveNamedEntities("Organizations", organisations, tokens);
    }


    private void saveResults(String feature, int count)
    {
        String s = feature + ": " + count;
        System.out.println("   " + s);
        _statisticsWriter.println(s);
    }

    private void saveNamedEntities(String entityType, Span spans[], String tokens[])
    {
        StringBuilder s = new StringBuilder(entityType + ": ");
        for (int sp = 0; sp < spans.length; sp++)
        {
            for (int i = spans[sp].getStart(); i < spans[sp].getEnd(); i++)
            {
                s.append(tokens[i]);
                if (i < spans[sp].getEnd() - 1) s.append(" ");
            }
            if (sp < spans.length - 1) s.append(", ");
        }

        System.out.println("   " + s);
        _statisticsWriter.println(s);
    }

    private void overallStatistics()
    {
        _statisticsWriter.println("---------OVERALL STATISTICS----------");
        DecimalFormat f = new DecimalFormat("#0.00");

        if (_totalTokensCount == 0) _totalTokensCount = 1;
        String verbs = f.format(((double) _verbCount * 100) / _totalTokensCount);
        String nouns = f.format(((double) _nounCount * 100) / _totalTokensCount);
        String adjectives = f.format(((double) _adjectiveCount * 100) / _totalTokensCount);
        String adverbs = f.format(((double) _adverbCount * 100) / _totalTokensCount);

        _statisticsWriter.println("Verbs: " + verbs + "%");
        _statisticsWriter.println("Nouns: " + nouns + "%");
        _statisticsWriter.println("Adjectives: " + adjectives + "%");
        _statisticsWriter.println("Adverbs: " + adverbs + "%");

        System.out.println("---------OVERALL STATISTICS----------");
        System.out.println("Adverbs: " + adverbs + "%");
        System.out.println("Adjectives: " + adjectives + "%");
        System.out.println("Verbs: " + verbs + "%");
        System.out.println("Nouns: " + nouns + "%");
    }

}
