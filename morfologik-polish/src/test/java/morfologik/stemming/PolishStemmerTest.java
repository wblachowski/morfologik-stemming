package morfologik.stemming;

import static morfologik.stemming.DictionaryLookupTest.assertNoStemFor;
import static morfologik.stemming.DictionaryLookupTest.stem;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import org.junit.Ignore;
import org.junit.Test;

/*
 * 
 */
public class PolishStemmerTest {
	/* */
	@Test
	public void testLexemes() throws IOException {
		PolishStemmer s = new PolishStemmer();

		assertEquals("żywotopisarstwo", stem(s, "żywotopisarstwie")[0]);
		assertEquals("abradować", stem(s, "abradowałoby")[0]);

		assertArrayEquals(new String[] { "żywotopisarstwo", "subst:sg:loc:n" },
		        stem(s, "żywotopisarstwie"));
		assertArrayEquals(new String[] { "bazia", "subst:pl:inst:f" }, stem(s,
		        "baziami"));

		// This word is not in the dictionary.
		assertNoStemFor(s, "martygalski");
	}

	/* */
	@Test
	@Ignore
	public void listUniqueTags() throws IOException {
		HashSet<String> forms = new HashSet<String>();
		for (WordData wd : new PolishStemmer()) {
			final CharSequence chs = wd.getTag();
			if (chs == null) {
				System.err.println("Missing tag for: " + wd.getWord());
				continue;
			}
			forms.add(chs.toString());
		}

		for (String s : new TreeSet<String>(forms)) {
			System.out.println(s);
		}
	}
	
    /* */
    @Test
    public void testWordDataFields() throws IOException {
        final IStemmer s = new PolishStemmer();

        final String word = "liga";
        final List<WordData> response = s.lookup(word);
        assertEquals(2, response.size());

        final HashSet<String> stems = new HashSet<String>();
        final HashSet<String> tags = new HashSet<String>();
        for (WordData wd : response) {
            stems.add(wd.getStem().toString());
            tags.add(wd.getTag().toString());
            assertSame(word, wd.getWord());
        }
        assertTrue(stems.contains("ligać"));
        assertTrue(stems.contains("liga"));
        assertTrue(tags.contains("subst:sg:nom:f"));
        assertTrue(tags.contains("verb:fin:sg:ter:imperf"));

        // Repeat to make sure we get the same values consistently.
        for (WordData wd : response) {
            stems.contains(wd.getStem().toString());
            tags.contains(wd.getTag().toString());
        }

        // Run the same consistency check for the returned buffers.
        final ByteBuffer temp = ByteBuffer.allocate(100);
        for (WordData wd : response) {
            // Buffer should be copied.
            final ByteBuffer copy = wd.getStemBytes(null);
            final String stem = new String(copy.array(), copy.arrayOffset()
                    + copy.position(), copy.remaining(), "iso-8859-2");
            // The buffer should be present in stems set.
            assertTrue(stem, stems.contains(stem));
            // Buffer large enough to hold the contents.
            temp.clear();
            assertSame(temp, wd.getStemBytes(temp));
            // The copy and the clone should be identical.
            assertEquals(0, copy.compareTo(temp));
        }

        for (WordData wd : response) {
            // Buffer should be copied.
            final ByteBuffer copy = wd.getTagBytes(null);
            final String tag = new String(copy.array(), copy.arrayOffset()
                    + copy.position(), copy.remaining(), "iso-8859-2");
            // The buffer should be present in tags set.
            assertTrue(tag, tags.contains(tag));
            // Buffer large enough to hold the contents.
            temp.clear();
            assertSame(temp, wd.getTagBytes(temp));
            // The copy and the clone should be identical.
            assertEquals(0, copy.compareTo(temp));
        }

        for (WordData wd : response) {
            // Buffer should be copied.
            final ByteBuffer copy = wd.getWordBytes(null);
            assertNotNull(copy);
            assertEquals(0, copy.compareTo(ByteBuffer.wrap(word
                    .getBytes("iso-8859-2"))));
        }
    }
}
