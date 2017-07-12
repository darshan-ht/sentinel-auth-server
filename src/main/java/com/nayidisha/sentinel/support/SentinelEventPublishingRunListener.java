package com.nayidisha.sentinel.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.EventPublishingRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pankaj on 7/12/17.
 */
public class SentinelEventPublishingRunListener extends EventPublishingRunListener {

    Logger log = LoggerFactory.getLogger(SentinelEventPublishingRunListener.class);

    public SentinelEventPublishingRunListener(SpringApplication application, String[] args) {
        super(application, args);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext configurableApplicationContext) {

        String jarsDirectory = configurableApplicationContext.getEnvironment().getProperty("SENTINEL_JAR_DIR");

        if (jarsDirectory == null) {
            log.info("No SENTINEL_JAR_DIR specified. No custom Jars will be loaded!");
            return;
        }

        File[] jarFiles = new File(jarsDirectory).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (jarFiles == null) {
            log.info("No jar files found in SENTINAL_JAR_DIR. Not loading custom Jars");
            return;
        }
        log.info("Jar files found at " + jarsDirectory);

        List<URL> urlList = new ArrayList();

        Arrays.asList(jarFiles).forEach(j -> {
            try {
                urlList.add(j.toURI().toURL());
            } catch (Exception e){
                log.warn(String.format("Could not convert %s to URL", j.toString()));
            }
        });

        // create a new classloader which contains the jars...
        ClassLoader extendedClassloader = new URLClassLoader(((URL[])urlList.toArray(new URL[urlList.size()])), configurableApplicationContext.getClassLoader());

        // and replace the context's classloader
        ((DefaultResourceLoader) configurableApplicationContext).setClassLoader(extendedClassloader);
    }
}
