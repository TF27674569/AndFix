package com.sample.andfix;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 用于解析patch包
 */
public class Patch {

    // patch包里面的key
    private static final String PATCH_CLASSES = "Patch-Classes";
    // patch包里面的value
    private static final String ENTRY_NAME = "META-INF/PATCH.MF";
    private File mFile;
    private Map<String, List<String>> mClassMap;

    private Context context;

    public Patch(File mFile, Context context) {
        this.mFile = mFile;
        this.context = context;
        init();
    }


    public Set<String> getPatchNames() {
        return mClassMap.keySet();
    }

    public List<String> getClasses(String name) {
        return mClassMap.get(name);
    }

    public File getFile() {
        return mFile;
    }

    private void init() {
        JarFile jarFile = null;
        InputStream inputStream = null;
        mClassMap = new HashMap<>();
        List<String> list;
        try {
            // file 加载到jarfile
            jarFile = new JarFile(mFile);
            // 拿到信息
            JarEntry jarEntry = jarFile.getJarEntry(ENTRY_NAME);
            // 用流接收
            inputStream = jarFile.getInputStream(jarEntry);
            // 解析成Manifest
            Manifest manifest = new Manifest(inputStream);
            // 获取信息
            Attributes main = manifest.getMainAttributes();
            // 遍历信息
            Attributes.Name attrName;
            for (Iterator<?> ite = main.keySet().iterator(); ite.hasNext(); ) {
                attrName = (Attributes.Name) ite.next();
                if (attrName != null) {
                    String name = attrName.toString();
                    // 获取需要修复的class全类名
                    if (name.endsWith("Classes")) {
                        list = Arrays.asList(main.getValue(name).split(","));
                        if (name.equalsIgnoreCase(PATCH_CLASSES)) {
                            mClassMap.put(name, list);
                        } else {
                            mClassMap.put(name.trim().substring(0, name.length() - 8), list);

                        }
                    }
                }
            }

        } catch (Exception ex) {
            Log.i("Patch", ex.toString());

        } finally {
            try {
                jarFile.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
