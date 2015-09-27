== What did you change to fix the code?

Problems::
1. Tags used before are not shown => leading or trailing spaces are not trimed
2. The most recent capitalization is not displayed => the most recent version is not used
3. Tags are not shown in lexicographical order => the string comparison order is reversed

The following test case shows the expected output and the actual output of the original code.

.TestCase
[source]
--
INPUT
5
 bake ,apple pie
  bake,bread
Bake , cherry pie
Buy,apples,Gold delicious
buy, Apples,Red Delicious
3
aa
baker
bu
--

.Expected Output
[source]
--
Bake,Apples,apple pie
Bake
buy,Bake,bread
--

.Actual Output
[source]
--
bake ,apples,apple pie, apples
bake 
buy,bread,bake 
--

Fixes::
- [*] The tages should be trimed to remove leading or trailing spaces for comparsion. Otherwise, they are viewed as distinct tags and cause confusion

[source,java]
--
public Note(String content, String tags) {
  List<String> list = new ArrayList<String>();
  for (String s : tags.split(",")) {
    // Fix: tags should be trimed and saved for comparison.
    list.add(s.trim());
  }
  this.content = content;
  this.tags = list;
}
--

- [*] The suggestion capitalization should use the most recent version should be displayed as the suggestion in a reverse
  order of the associated note list. In `TagSuggester.getSuggestions(prefix)`, the fix finds the most recent version.
[source,java]
--
    public List<String> getSuggestions(String prefix) {
    ...
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
--

- [*] The string comparison order should inverted to reflect the required lexicographical order

[source,java]
--
class Suggestion implements Comparable<Suggestion> {
...
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
--

== Did you see any bad coding practices in the original code?

1. The requirements are precise but the test case is trivial. It is likely the client does not participating in writing test cases for validation. This is a bad development practice regardless of how good the coding practices are.
2. Lack of essential documentation, especially for the complex business logic like the tag suggestion. Coding while cross referencing separate documents is error-prone. The following maintainer may not figure out where the business logic the code is based on.
3. Complex procedures such as equalsIgnoreTypo() and equalIfDeleteLetter() lack documentation on loop invariants, pre and post conditions for ease of debugging
4. Use the high level Scanner class instead of BufferedReader to clean the code. Lengthy code may complicate the logic behind.. 
5. class fields may deserve a prefix in case of confusion with arguments
