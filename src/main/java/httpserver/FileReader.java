package httpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class FileReader {

    static byte[] readFile(String path) throws IOException {

        File file = new File("html" + path);
        FileInputStream fileInputStream;
        byte[] bFile = new byte[(int) file.length()];

        fileInputStream = new FileInputStream(file);
        fileInputStream.read(bFile);
        fileInputStream.close();
        return bFile;
    }


}

