import java.io.*;
import java.util.*;

class TagSuggesterFix {
    public static void main(String args[]) throws IOException {
        BufferedReader input = new BufferedReader (new InputStreamReader (System.in));
        TagSuggesterFix suggester = new TagSuggesterFix();

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

    public TagSuggesterFix() {
        items = new HashMap<String, List<Note>>();
    }

    public void addNote(Note note) {
        // The note tag should already be trimed when added
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
            int edits;
            if (tag.startsWith(prefix)) {
                edits = 0;
            } else if (equalsIgnoreTypo(prefix, tag.substring(0, Math.min(tag.length(), prefix.length())))) {
                edits = 1;
            } else {
                edits = 2;
            }
            if (edits < 2) {
                suggestions.add(new Suggestion(tag, edits, items.get(tag).size()));
            }
        }
        Collections.sort(suggestions);
        List<String> tags = new ArrayList<String>(suggestions.size());
        for (Suggestion s : suggestions) {
            // Fix: find the most recent version for its capitalization and display
            List<Note> notes = items.get(s.tag);
            Note note = notes.get(notes.size()-1);
            for(String tag : note.getTags()) {
                if(tag.toLowerCase().equals(s.tag)) {
                    tags.add(tag);
                    break;
                }
            }
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
        // i == word.length() || deletable.charAt(i) != word.charAt(i)
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
            // Fix: tags should be trimed and saved for comparison.
            list.add(s.trim());
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
            // Fix: in lecicographical order
            compare = tag.compareTo(o.tag);
        }
        return compare;
    }

}
