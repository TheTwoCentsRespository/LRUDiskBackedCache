package LRUDiskBackedCache;
import java.io.File;

class HelperFunctions {

	public static boolean existsOnDisk(String absolutePath) throws Exception {
		boolean rValue = false;
		File file = new File(absolutePath);
		rValue = file.isFile();
		return rValue;
	}

	public static String constructAbsolutePath(String cacheDirectoryPath,
			String key) {
		return cacheDirectoryPath + File.separator + key;
	}

}
