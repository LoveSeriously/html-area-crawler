package exec;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 创建新文件和目录
 *
 * @author lw
 * @date 2019-08-14
 */
public class FileUtil {
    /**
     * 验证字符串是否为正确路径名的正则表达式
     */
    private static String matches = "[A-Za-z]:\\\\[^:?\"><*]*";

    /**
     * 通过 sPath.matches(matches) 方法的返回值判断是否正确
     * sPath 为上传的文件路径字符串
     */
    static boolean flag = false;

    /**
     * 文件
     */
    static File file;

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param deletePath
     * @return
     */
    public static boolean deleteFolder(String deletePath) {
        flag = false;
        if (deletePath.matches(matches)) {
            file = new File(deletePath);
            // 判断目录或文件是否存在
            if (!file.exists()) {
                // 不存在返回 false
                return flag;
            } else {
                // 判断是否为文件
                if (file.isFile()) {
                    // 为文件时调用删除文件方法
                    return deleteFile(deletePath);
                } else {
                    // 为目录时调用删除目录方法
                    return deleteDirectory(deletePath);
                }
            }
        } else {
            System.out.println("要传入正确路径！");
            return false;
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath 文件路径
     * @return
     */
    public static boolean deleteFile(String filePath) {
        flag = false;
        file = new File(filePath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();// 文件删除
            flag = true;
        }
        return flag;
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param dirPath
     * @return
     */
    public static boolean deleteDirectory(String dirPath) {
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }
        File dirFile = new File(dirPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        // 获得传入路径下的所有文件
        File[] files = dirFile.listFiles();
        // 循环遍历删除文件夹下的所有文件(包括子目录)
        if (files != null) {
            for (File file1 : files) {
                if (file1.isFile()) {
                    // 删除子文件
                    flag = deleteFile(file1.getAbsolutePath());
                    System.out.println(file1.getAbsolutePath() + " 删除成功");
                    if (!flag) {
                        break;// 如果删除失败，则跳出
                    }
                } else {// 运用递归，删除子目录
                    flag = deleteDirectory(file1.getAbsolutePath());
                    if (!flag) {
                        break;// 如果删除失败，则跳出
                    }
                }
            }
        }

        if (!flag) {
            return false;
        }
        // 删除当前目录
        return dirFile.delete();
    }

    /**
     * 创建单个文件
     *
     * @param filePath 文件所存放的路径
     * @return
     */
    public static boolean createFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {// 判断文件是否存在
            System.out.println("目标文件已存在" + filePath);
            return false;
        }
        if (filePath.endsWith(File.separator)) {// 判断文件是否为目录
            System.out.println("目标文件不能为目录！");
            return false;
        }
        if (!file.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
            // 如果目标文件所在的文件夹不存在，则创建父文件夹
            System.out.println("目标文件所在目录不存在，准备创建它！");
            if (!file.getParentFile().mkdirs()) {// 判断创建目录是否成功
                System.out.println("创建目标文件所在的目录失败！");
                return false;
            }
        }
        try {
            if (file.createNewFile()) {// 创建目标文件
                System.out.println("创建文件成功:" + filePath);
                return true;
            } else {
                System.out.println("创建文件失败！");
                return false;
            }
        } catch (IOException e) {// 捕获异常
            e.printStackTrace();
            System.out.println("创建文件失败！" + e.getMessage());
            return false;
        }
    }

    /**
     * 创建目录(如果目录存在就删掉目录)
     *
     * @param destDirName 目标目录路径
     * @return
     */
    public static boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {// 判断目录是否存在
            System.out.println("目标目录已存在!");
            //return false;
            return FileUtil.deleteDirectory(destDirName);
        }
        System.out.println("已删除原目录并重新创建!");
        if (!destDirName.endsWith(File.separator)) {// 结尾是否以"/"结束
            destDirName = destDirName + File.separator;
        }
        if (dir.mkdirs()) {// 创建目标目录
            System.out.println("创建目录成功！" + destDirName);
            return true;
        } else {
            System.out.println("创建目录失败！");
            return false;
        }
    }

    /**
     * 创建临时文件
     *
     * @param prefix  前缀字符串定义的文件名;必须至少有三个字符长
     * @param suffix  后缀字符串定义文件的扩展名;如果为null后缀".tmp" 将被使用
     * @param dirName 该目录中的文件被创建。对于默认的临时文件目录nullis来传递
     * @return 一个抽象路径名新创建的空文件。
     * @throws IllegalArgumentException -- 如果前缀参数包含少于三个字符
     * @throws IOException              -- 如果文件创建失败
     * @throws SecurityException        -- 如果SecurityManager.checkWrite(java.lang.String)方法不允许创建一个文件
     */
    public static String createTempFile(String prefix, String suffix, String dirName) {
        File tempFile = null;
        if (dirName == null) {// 目录如果为空
            try {
                tempFile = File.createTempFile(prefix, suffix);// 在默认文件夹下创建临时文件
                return tempFile.getCanonicalPath();// 返回临时文件的路径
            } catch (IOException e) {// 捕获异常
                e.printStackTrace();
                System.out.println("创建临时文件失败：" + e.getMessage());
                return null;
            }
        } else {
            // 指定目录存在
            File dir = new File(dirName);// 创建目录
            if (!dir.exists()) {
                // 如果目录不存在则创建目录
                if (FileUtil.createDir(dirName)) {
                    System.out.println("创建临时文件失败，不能创建临时文件所在的目录！");
                    return null;
                }
            }
            try {
                tempFile = File.createTempFile(prefix, suffix, dir);// 在指定目录下创建临时文件
                return tempFile.getCanonicalPath();// 返回临时文件的路径
            } catch (IOException e) {// 捕获异常
                e.printStackTrace();
                System.out.println("创建临时文件失败!" + e.getMessage());
                return null;
            }
        }
    }

    /**
     * 获取路径目录下的全部文件
     * @param path
     * @return
     */
    public static File[] getKeywordFiles(String path) {
        File dir = new File(path);
        if (!dir.exists())
            return null;
        File[] fs = dir.listFiles();
        return fs;
    }

    /**
     * 读取一个文件
     * @param filePathAndName
     * @return
     * @throws IOException
     */
    public static List<String> readFile(String filePathAndName)
            throws IOException {
        FileInputStream fis = new FileInputStream(filePathAndName);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        LineNumberReader lnr = new LineNumberReader(br);

        List<String> returnValue = new ArrayList<String>();
        int cnt = 0;
        while (true) {
            cnt++;
            String tempStr = lnr.readLine();
            if (tempStr == null)
                break;
            if (tempStr.length() < 2)
                continue;
            returnValue.add(tempStr);
        }
        lnr.close();
        br.close();
        isr.close();
        fis.close();
        return returnValue;
    }

    /**
     * 读取一个文件,并排重后返回

     */
    public static List<String> readFileNoDup(String filePathAndName)
            throws IOException {
        FileInputStream fis = new FileInputStream(filePathAndName);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        LineNumberReader lnr = new LineNumberReader(br);

        Set<String> set = new HashSet<String>();
        while (true) {
            String tempStr = lnr.readLine();
            if (tempStr == null)
                break;
            if (tempStr.length() < 2)
                continue;
            set.add(tempStr.trim());
        }
        lnr.close();
        br.close();
        isr.close();
        fis.close();
        List<String> returnValue = new ArrayList<String>(set.size());
        returnValue.addAll(set);
        return returnValue;
    }

    /**
     * 加入内容到指定文件 假设该文件不存在，则创建并加入内容 假设该文件已存在，则加入内容到已有内容最后
     * flag为true，则向现有文件里加入内容，否则覆盖原有内容

     */
    public static void writeFile(String filePathAndName, String fileContent,
                                 boolean flag) throws IOException {
        if (null == fileContent || fileContent.length() < 1)
            return;
        File file = new File(filePathAndName);

        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(filePathAndName, flag);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
        osw.write(fileContent + "\r\n");
        osw.flush();
        osw.close();
    }

    /**
     * 加入内容到指定文件 假设该文件不存在，则创建并加入内容 假设该文件已存在，则加入内容到已有内容最后
     * flag为true，则向现有文件里加入内容，否则覆盖原有内容

     */
    public static void writeFile(String filePathAndName,
                                 List<String> fileContent, boolean flag) throws IOException {
        File file = new File(filePathAndName);

        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(filePathAndName, flag);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
        for (String temp : fileContent)
            osw.write(temp + "\r\n");
        osw.flush();
        osw.close();
    }

    /**
     * 加入内容到指定文件 假设该文件不存在，则创建并加入内容 假设该文件已存在，则加入内容到已有内容最后
     * flag为true，则向现有文件里加入内容，否则覆盖原有内容

     */
    public static void writeFile(String filePath,String filename,
                                 List<String> fileContent, boolean flag) throws IOException {
        File file = new File(filePath);

        if(!file.exists()){
            boolean tempFlag = file.mkdirs();
            if(!tempFlag){
//                log.error("目录"+filePath+"创建失败");
                return;
            }
        }

        file = new File(filePath,filename);

        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(filePath+filename, flag);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
        for (String temp : fileContent)
            osw.write(temp + "\r\n");
        osw.flush();
        osw.close();
    }
}
