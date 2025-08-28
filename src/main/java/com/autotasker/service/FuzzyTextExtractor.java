package com.autotasker.service;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.UserDAO;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuzzyTextExtractor {
    private static final StringMetric METRIC = StringMetrics.jaroWinkler();
    private static final Set<String> NOISE_PHRASES = Set.of(
            "thank you", "thanks", "best regards", "regards", "cheers"
    );
    // regex for all formats
    private static final String DATE_REGEX =
            "(\\d{4}-\\d{2}-\\d{2})" +               // YYYY-MM-DD
                    "|(\\d{2}-\\d{2}-\\d{4})" +      // DD-MM-YYYY
                    "|(\\d{2}/\\d{2}/\\d{4})" +      // DD/MM/YYYY
                    "|(\\d{4}/\\d{2}/\\d{2})" +      // YYYY/MM/DD
                    "|(\\d{2}\\.\\d{2}\\.\\d{4})" +  // DD.MM.YYYY
                    "|(\\d{4}\\.\\d{2}\\.\\d{2})";   // YYYY.MM.DD
    private static final Map<String, List<String>> KEYWORD_GROUPS = new LinkedHashMap<>();
    public static final UserDAO USER_DAO = new UserDAO();
    public static final DepartmentDAO DEPARTMENT_DAO = new DepartmentDAO();

    static {
        KEYWORD_GROUPS.put("name", Arrays.asList("task", "aufgabe", "name", "taskname"));
        KEYWORD_GROUPS.put("description", Arrays.asList("description", "descr", "beschreibung"));
        KEYWORD_GROUPS.put("dueDate", Arrays.asList("due date", "due", "duedate", "deadline", "bis"));
        KEYWORD_GROUPS.put("user", Arrays.asList("assigned user", "user", "benutzer", "usr"));
        KEYWORD_GROUPS.put("department", Arrays.asList("assigned department", "department", "abteilung"));
    }

    public static Map<String, String> extractSections(String text) {
        Map<String, String> result = new LinkedHashMap<>();

        // split to words
        String[] tokens = text.split("\\s+");
        List<KeywordMatch> matches = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            // remove everything that is not a letter ot number from the current word
            String word = tokens[i].toLowerCase().replaceAll("[^a-z0-9]", "");
            // getting all groups of KEYWORD_GROUPS
            for (Map.Entry<String, List<String>> entry : KEYWORD_GROUPS.entrySet()) {
                String canonical = entry.getKey();
                // check for fuzzy-match with all values in group
                int idx = findBestMatchIndexInList(entry.getValue(), word);
                if (idx != -1) {
                    // adding a KeyWordMatch-object to matches-list
                    matches.add(new KeywordMatch(canonical, i));
                    break;
                }
            }
        }

        // sort matches-list by index
        matches.sort(Comparator.comparingInt(m -> m.index));

        // looping through the results list
        for (int i = 0; i < matches.size(); i++) {
            // set current word
            KeywordMatch current = matches.get(i);
            // set the index after the current word
            int start = current.index + 1;
            // set the end index of section
            int end = (
                    // if current index is not the last index in matches list
                    i + 1 < matches.size()) ?
                    // get the next match-word-index
                    matches.get(i + 1).index :
                    // else take the size of all tokens (reading until the end of text)
                    tokens.length;
            // taking words from start-index to end-index and join them with space
            String segment = String.join(" ",
                    Arrays.copyOfRange(tokens, start, end)
            ).trim();

            // clean noisy phrases
            for (String noise : NOISE_PHRASES) {
                // check if segment ends with noise
                if (segment.toLowerCase().endsWith(noise)) {
                    // if ends with noise = cut the noise out
                    segment = segment.substring(0, segment.length() - noise.length()).trim();
                }
            }

            // validation
            if (isValidSegment(current.canonical, segment)) {
                switch (current.canonical) {
                    case "dueDate" ->
                        // cut only date
                            segment = getOnlyDateFromSegment(segment);
                    case "user" -> {
                        List<String> allUsernames = USER_DAO.findAllUsernames();
                        int index = findBestMatchIndexInList(allUsernames, segment);
                        segment = allUsernames.get(index);
                    }
                    case "department" -> {
                        List<String> allDepartmentNames = DEPARTMENT_DAO.findAllDepartmentNames();
                        int index = findBestMatchIndexInList(allDepartmentNames, segment);
                        segment = allDepartmentNames.get(index);
                    }
                }
                result.put(current.canonical, segment);
            }
        }
        return result;
    }

    private static String getOnlyDateFromSegment(String segment) {
        // compile regex
        Pattern DATE_PATTERN = Pattern.compile(DATE_REGEX);
        Matcher matcher = DATE_PATTERN.matcher(segment);
        if (matcher.find()) {
            // returns only the date
            return matcher.group();
        }
        return null;
    }


    private static int findBestIndex(String text, String keyword, float bestScore, int bestIndex) {
        for (int i = 0; i <= text.length() - keyword.length(); i++) {
            int end = Math.min(i + keyword.length() + 3, text.length());
            String sub = text.substring(i, end);
            float score = METRIC.compare(sub, keyword);
            if (score > 0.85 && score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    // validation
    private static boolean isValidSegment(String canonical, String segment) {
        if (segment.isEmpty()) return false;

        return switch (canonical.toLowerCase()) {
            case "name" -> segment.length() > 5 && !segment.equals(canonical) && !segment.contains("task-name");
            case "description" -> segment.length() > 5 && !segment.equals(canonical) && !segment.contains("task-description");
            case "duedate" ->
                // YYYY-MM-DD, DD-MM-YYYY, DD/MM/YYYY, YYYY/MM/DD, DD.MM.YYYY, YYYY.MM.DD
                    segment.matches(DATE_REGEX);
            case "user" ->
                // check for username in DB
                    findBestMatchIndexInList(USER_DAO.findAllUsernames(), segment) != -1;
            case "department" ->
                // check for department name in DB
                    findBestMatchIndexInList(DEPARTMENT_DAO.findAllDepartmentNames(), segment) != -1;
            default -> true;
        };
    }

    // helper class
    private static class KeywordMatch {
        String canonical; // "description"
        int index;

        KeywordMatch(String canonical, int index) {
            this.canonical = canonical;
            this.index = index;
        }
    }

    public static int findBestMatchIndexInList(List<String> list, String word) {
        if (list == null || list.isEmpty() || word == null || word.isEmpty()) {
            return -1;
        }

        int bestIndex = -1;
        float bestScore = 0f;

        for (int i = 0; i < list.size(); i++) {
            String candidate = list.get(i);
            // check for direct match
            if (word.equals(candidate)) {
                return i;
            }
            // check for substring match
            if (word.contains(candidate)) {
                return i;
            }
            float score = METRIC.compare(word, candidate.toLowerCase());
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        // matching limit
        return bestScore >= 0.80f ? bestIndex : -1;
    }

    public static boolean checkIfContainsKeyword(String text, String[] variants) {
        String lower = text.toLowerCase();

        int bestIndex = -1;
        float bestScore = 0f;

        for (String variant : variants) {
            String v = variant.toLowerCase();

            // direct find
            int idx = lower.indexOf(v);
            if (idx != -1) {
                return true;
            }

            // fuzzy sliding window
            bestIndex = findBestIndex(lower, v, bestScore, bestIndex);
        }

        return bestIndex != -1; // -1 if not find
    }

    // test
    public static void main(String[] args) {
        String text = "Task Report Bug Description: Fix login issue task ds,fjheriurh DueDate: 2025-08-30-54  User: Saby Department: IT";

        Map<String, String> extracted = extractSections(text);
        extracted.forEach((k, v) -> System.out.println(k + " -> " + v));

    }
}


