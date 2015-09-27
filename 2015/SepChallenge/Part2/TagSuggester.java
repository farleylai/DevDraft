import java.io.*;
import java.util.*;

class TagSuggester {
    public static void main(String args[]) throws IOException {
        BufferedReader input = new BufferedReader (new InputStreamReader (System.in));
        TagSuggester suggester = new TagSuggester();

        final int noteCount = Integer.parseInt(input.readLine());
        for (int i = 0; i < noteCount; i++) {
            suggester.addNote(new Note("", input.readLine()));
        }
        final int queryCount = Integer.parseInt(input.readLine());
        for (int q = 0; q < queryCount; q++) {
            List<String> suggestions = suggester.getSuggestions(input.readLine());
            String output = suggestions.isEmpty() ? "" : suggestions.get(0);
            for (int i = 1; i < suggestions.size(); i++) {
                output += "," + suggestions.get(i);
            }
            System.out.println(output);
        }
        input.close();
    }

    private Map<String, List<Note>> items;

    public TagSuggester() {
        items = new HashMap<String, List<Note>>();
    }

    public void addNote(Note note) {
        for (String tag : note.getTags()) {
            tag = tag.toLowerCase();
            List<Note> list;
            if (items.containsKey(tag)) {
                list = items.get(tag);
            } else {
                list = new ArrayList<Note>();
                items.put(tag, list);
            }
            list.add(note);
        }
    }

    public List<String> getSuggestions(String prefix) {
        prefix = prefix.toLowerCase();
        List<Suggestion> suggestions = new ArrayList<Suggestion>();
        for (String tag : items.keySet()) {
            //System.out.println("Check " + tag);
            int edits;
            if (tag.startsWith(prefix)) {
                edits = 0;
            } else if (equalsIgnoreTypo(prefix, tag.substring(0, Math.min(tag.length(), prefix.length())))) {
                edits = 1;
            } else {
                edits = 2;
            }
            if (edits < 2) {
                //System.out.printf("%s => %s: %d, %d\n", prefix, tag, edits, items.get(tag).size());
                suggestions.add(new Suggestion(tag, edits, items.get(tag).size()));
            }
        }
        System.out.println("Before: " + suggestions);
        Collections.sort(suggestions);
        System.out.println("After: " + suggestions);
        List<String> tags = new ArrayList<String>(suggestions.size());
        for (Suggestion s : suggestions) {
            tags.add(s.tag);
        }
        return tags;
    }

    public static boolean equalsIgnoreTypo(String suspect, String word) {
        boolean typo = suspect.equals(word);
        
        // letter deletion
        // try deleting letter from suspect to see if it equals word
        typo = typo || equalIfDeleteLetter(suspect, word);

        // letter addition
        // adding to suspect is the same as deleting from word
        typo = typo || equalIfDeleteLetter(word, suspect);

        // letter substitution
        // count how many letters are different between the two words.
        if (!typo && suspect.length() == word.length()) {
            int diff = 0;
            for (int i = 0; i < suspect.length() && diff < 2; i++) {
                if (suspect.charAt(i) != word.charAt(i)) {
                    diff++;
                }
            }
            typo = diff == 1;
        }
        return typo;
    }

    private static boolean equalIfDeleteLetter(String deletable, String word) {
        if (deletable.length() - word.length() != 1) {
            return false;
        }

        int i = 0;
        while (i < word.length() && deletable.charAt(i) == word.charAt(i)) {
            i++;
        }
        while (i + 1 < deletable.length() && i < word.length() && deletable.charAt(i + 1) == word.charAt(i)) {
            i++;
        }
        return i == word.length();
    }

}

class Note {
    private List<String> tags;
    private String content;

    public Note(String content, String tags) {
        List<String> list = new ArrayList<String>();
        for (String s : tags.split(",")) {
            list.add(s);
        }
        this.content = content;
        this.tags = list;
    }

    public String getContent() {
        return content;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }
}

class Suggestion implements Comparable<Suggestion> {
    public final int edits;
    public final int count;
    public final String tag;

    public Suggestion(String tag, int edits, int count) {
        this.tag = tag;
        this.edits = edits;
        this.count = count;
    }

    @Override
    public int compareTo(Suggestion o) {
        int compare = edits - o.edits;
        if (compare == 0) {
            compare = o.count - count;
        }
        if (compare == 0) {
            compare = o.tag.compareTo(tag);
        }
        return compare;
    }

    @Override
    public String toString() {
        return tag;
    }
}
