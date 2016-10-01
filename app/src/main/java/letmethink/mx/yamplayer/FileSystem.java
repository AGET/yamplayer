/**
 * Created by Rafael Rendon Pablo on 30/09/2016.
 */
package letmethink.mx.yamplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

class FileSystem {
    File[] findMediaFiles(String path) throws IOException {
        if (path == null) {
            return new File[0];
        }

        File root = new File(path);
        if (!root.isDirectory()) {
            throw new IOException("Path should be a directory");
        }

        List<File> fileList = new ArrayList<>();
        Queue<File> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            File nextDir = queue.poll();
            if (nextDir == null) {
                continue;
            }
            File[] files = nextDir.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    if (isMediaFile(file.getName())) {
                        fileList.add(file);
                    }
                }
                if (!file.isDirectory()) {
                    continue;
                }
                queue.add(file);
            }
        }
        return fileList.toArray(new File[fileList.size()]);
    }

    private boolean isMediaFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        fileName = fileName.toLowerCase();
        return fileName.endsWith(".mp3") || fileName.endsWith(".m4a");
    }

    String[] getFileNames(File[] files) {
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }
        return names;
    }
}
