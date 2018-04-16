package ren.helloworld.upload2pgyer.helper;

import org.apache.tools.ant.DirectoryScanner;
import ren.helloworld.upload2pgyer.impl.Message;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
    /**
     *
     */
    private static long last_time = -1L;

    /**
     * Header
     *
     * @param listener listener
     */
    public static void printHeaderInfo(Message listener) {
        printMessage(listener, false, "");
        printMessage(listener, false, "**************************************************************************************************");
        printMessage(listener, false, "**************************************************************************************************");
        printMessage(listener, false, "********************************          UPLOAD TO PGYER         ********************************");
        printMessage(listener, false, "**************************************************************************************************");
        printMessage(listener, false, "**************************************************************************************************\n");
    }

    /**
     * print message
     *
     * @param listener listener
     * @param needTag  needTag
     * @param message  message
     */
    public static void printMessage(Message listener, boolean needTag, String message) {
        if (listener == null) return;
        listener.message(needTag, message);
    }

    /**
     * find file
     *
     * @param scandir  scandir
     * @param wildcard wildcard
     * @param listener listener
     * @return file path
     */
    public static String findFile(String scandir, String wildcard, Message listener) {
        File dir = new File(scandir);
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            CommonUtil.printMessage(listener, true, "scan dir:" + dir.getAbsolutePath());
            CommonUtil.printMessage(listener, true, "scan dir isn't exist or it's not a directory!");
            return null;
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(scandir);
        scanner.setIncludes(new String[]{wildcard});
        scanner.setCaseSensitive(true);
        scanner.scan();
        String[] uploadFiles = scanner.getIncludedFiles();

        if (uploadFiles == null || uploadFiles.length == 0)
            return null;
        if (uploadFiles.length == 1)
            return new File(dir, uploadFiles[0]).getAbsolutePath();

        List<String> strings = Arrays.asList(uploadFiles);
        Collections.sort(strings, new CommonUtil.FileComparator(dir));
        String uploadFiltPath = new File(dir, strings.get(0)).getAbsolutePath();
        CommonUtil.printMessage(listener, true, "Found " + uploadFiles.length + " files, the default choice of the latest modified file!");
        CommonUtil.printMessage(listener, true, "The latest modified file is " + uploadFiltPath + "\n");
        return uploadFiltPath;
    }

    /**
     * write
     *
     * @param path     path
     * @param content  content
     * @param encoding encoding
     * @return file file
     */
    public static File write(String path, String content, String encoding) {
        try {
            File file = new File(path);
            if (!file.delete() && !file.createNewFile()) return null;
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), encoding));
            writer.write(content);
            writer.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * download
     *
     * @param urlString urlString
     * @param savePath  savePath
     * @param fileName  fileName
     * @return file file
     */
    public static File download(String urlString, String savePath, String fileName) {
        InputStream is = null;
        OutputStream os = null;
        try {
            File dir = new File(savePath);
            if (!dir.exists() && !dir.mkdirs()) return null;
            String filePath = savePath + File.separator + fileName;

            URL url = new URL(urlString);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(60 * 1000);
            is = con.getInputStream();

            byte[] bs = new byte[1024 * 8];
            int len;

            os = new FileOutputStream(filePath);
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
            return new File(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * replaceBlank
     *
     * @param str str
     * @return string
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * string is blank
     *
     * @param str string
     * @return isblank
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * size convert
     *
     * @param size file size
     * @return convert file size
     */
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    /**
     *
     */
    public static class FileUploadProgressListener implements ProgressRequestBody.Listener {
        private Message listener;

        public FileUploadProgressListener(Message listener) {
            this.listener = listener;
        }

        @Override
        public void onRequestProgress(long bytesWritten, long contentLength) {
            final int progress = (int) (100F * bytesWritten / contentLength);
            if (progress == 100) {
                last_time = -1L;
                listener.message(true, "upload progress: " + progress + " %");
                return;
            }

            if (last_time == -1) {
                last_time = System.currentTimeMillis();
                listener.message(true, "upload progress: " + progress + " %");
                return;
            }

            if (System.currentTimeMillis() - last_time > 1000) {
                last_time = System.currentTimeMillis();
                listener.message(true, "upload progress: " + progress + " %");
            }
        }
    }

    /**
     *
     */
    public static class FileComparator implements Comparator<String>, Serializable {
        File dir;

        public FileComparator(File dir) {
            this.dir = dir;
        }

        @Override
        public int compare(String o1, String o2) {
            File file1 = new File(dir, o1);
            File file2 = new File(dir, o2);
            if (file1.lastModified() < file2.lastModified())
                return 1;
            if (file1.lastModified() > file2.lastModified())
                return -1;
            return 0;
        }
    }
}
