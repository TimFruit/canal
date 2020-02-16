package com.alibaba.otter.canal.client.adapter.rdbsharding;

import com.alibaba.otter.canal.client.adapter.rdb.RdbAdapter;
import com.alibaba.otter.canal.client.adapter.rdbsharding.config.AdapterShardingDataSourceFactory;
import com.alibaba.otter.canal.client.adapter.rdbsharding.config.ShardingConfigLoader;
import com.alibaba.otter.canal.client.adapter.support.OuterAdapterConfig;
import com.alibaba.otter.canal.client.adapter.support.SPI;
import com.alibaba.otter.canal.client.adapter.support.URLClassExtensionLoader;
import org.apache.shardingsphere.core.parse.spi.SQLParserEntry;
import org.apache.shardingsphere.core.spi.NewInstanceServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * RDB适配器实现类 使用了sharding-jdbc
 *
 * @author TimFruit
 * @date 20-2-14 下午8:28
 */
@SPI("rdb-sharding")
public class RdbShardingAdapter extends RdbAdapter {

    private static Logger logger = LoggerFactory.getLogger(ShardingConfigLoader.class);

    private static final String                                      SERVICES_DIRECTORY         = "META-INF/services/";

    private static final String                                      CANAL_DIRECTORY            = "META-INF/canal/";

    @Override
    protected DataSource createDatasource(OuterAdapterConfig configuration, Properties envProperties) {

        // 如果去掉这段代码, 如要将 sharding-core-parse-mysql-4.0.0-RC2.jar 放到client-adapter/lib包下加载
        // 如果需要同步其他类型的数据库, 如oracle, 则需要使用sharding-core-parse-oracle-4.0.0-RC2.jar

        URL[] extendUrls=findExtensionUrls();
        if(extendUrls!=null && extendUrls.length>0){
            logger.info("===== set extendLoader for shardingshpere spi to load org.apache.shardingsphere.core.parse.spi.SQLParserEntry");
            ClassLoader parent=Thread.currentThread().getContextClassLoader();
            ClassLoader extendLoader=new URLClassLoader(extendUrls, parent);
            Thread.currentThread().setContextClassLoader(extendLoader);
            NewInstanceServiceLoader.register(SQLParserEntry.class);
        }



        try {
            return AdapterShardingDataSourceFactory.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("create sharding datasource error", e);
        }
    }




    private URL[] findExtensionUrls(){
        // 1. plugin folder，customized extension classLoader （jar_dir/plugin）
        String dir = File.separator + this.getJarDirectoryPath() + File.separator + "plugin";

        File externalLibDir = new File(dir);
        if (!externalLibDir.exists()) {
            externalLibDir = new File(File.separator + this.getJarDirectoryPath() + File.separator + "canal-adapter"
                    + File.separator + "plugin");
        }
        logger.info("extension classpath dir: " + externalLibDir.getAbsolutePath());
        if (externalLibDir.exists()) {
            File[] files = externalLibDir.listFiles((dir1, name) -> name.endsWith(".jar"));
            if (files != null) {
                List<URL> urlList=new ArrayList<>(files.length);
                for (File f : files) {
                    URL url;
                    try {
                        url = f.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("load extension jar failed!", e);
                    }
                    urlList.add(url);
                }
                URL[] urls=new URL[urlList.size()];
                return urlList.toArray(urls);
            }
        }
        return null;
    }


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
