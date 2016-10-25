package letmethink.mx.yamplayer;

import java.io.IOException;
import java.io.File;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class FileSystemTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(expected=IOException.class)  
    public void findMediaFilesRaisesIfPathIsNotADirectory() throws Exception {
        (new FileSystem()).findMediaFiles("heyjude.mp3");
    }

    @Test
    public void findMediaFilesReturnsEmptyFileArrayIfPathIsNull() throws Exception {
        File[] files = (new FileSystem()).findMediaFiles(null);
        assertArrayEquals(files, new File[0]);
    }

    @Test
    public void findMediaFilesReturnsOnlyMediaFiles() throws Exception {
        File musicf = folder.newFolder("music");

        folder.newFile("music/lyrics.txt");
        folder.newFile("music/a.mp3");
        folder.newFile("music/b.mp3");
        folder.newFile("music/c.m4a");
        folder.newFile("music/d.txt");

        FileSystem fs = new FileSystem();
        File[] files = fs.findMediaFiles(musicf.getPath());

        assertEquals(3, files.length);
    }
}
