package com.alibaba.otter.canal.client.adapter.rdbsharding.config;

import com.alibaba.otter.canal.client.adapter.support.URLClassExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author TimFruit
 * @date 20-2-16 下午7:41
 */
public class PluginClassLoader {
    private static Logger logger = LoggerFactory.getLogger(ShardingConfigLoader.class);

    private static final String                                      SERVICES_DIRECTORY         = "META-INF/services/";

    private static final String                                      CANAL_DIRECTORY            = "META-INF/canal/";



//    public void loadSpiClass(String className){
//        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
//
//        // 1. plugin folder，customized extension classLoader （jar_dir/plugin）
//        String dir = File.separator + this.getJarDirectoryPath() + File.separator + "plugin";
//
//        File externalLibDir = new File(dir);
//        if (!externalLibDir.exists()) {
//            externalLibDir = new File(File.separator + this.getJarDirectoryPath() + File.separator + "canal-adapter"
//                    + File.separator + "plugin");
//        }
//        logger.info("extension classpath dir: " + externalLibDir.getAbsolutePath());
//        if (externalLibDir.exists()) {
//            File[] files = externalLibDir.listFiles((dir1, name) -> name.endsWith(".jar"));
//            if (files != null) {
//                for (File f : files) {
//                    URL url;
//                    try {
//                        url = f.toURI().toURL();
//                    } catch (MalformedURLException e) {
//                        throw new RuntimeException("load extension jar failed!", e);
//                    }
//
//                    ClassLoader parent = Thread.currentThread().getContextClassLoader();
//                    URLClassLoader localClassLoader;
//                    localClassLoader = new URLClassLoader(new URL[] { url }, parent);
//
//                    loadFile(extensionClasses, CANAL_DIRECTORY, localClassLoader);
//                    loadFile(extensionClasses, SERVICES_DIRECTORY, localClassLoader);
//                }
//            }
//        }
//
//    }



    private String getJarDirectoryPath() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String dirtyPath;
        if (url != null) {
            dirtyPath = url.toString();
        } else {
            File file = new File("");
            dirtyPath = file.getAbsolutePath();
        }
        String jarPath = dirtyPath.replaceAll("^.*file:/", ""); // removes
        // file:/ and
        // everything
        // before it
        jarPath = jarPath.replaceAll("jar!.*", "jar"); // removes everything
        // after .jar, if .jar
        // exists in dirtyPath
        jarPath = jarPath.replaceAll("%20", " "); // necessary if path has
        // spaces within
        if (!jarPath.endsWith(".jar")) { // this is needed if you plan to run
            // the app using Spring Tools Suit play
            // button.
            jarPath = jarPath.replaceAll("/classes/.*", "/classes/");
        }
        Path path = Paths.get(jarPath).getParent(); // Paths - from java 8
        if (path != null) {
            return path.toString();
        }
        return null;
    }


}
