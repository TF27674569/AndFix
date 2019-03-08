package com.sample.andfix;

import android.content.Context;

import java.io.File;
import java.util.List;

/**
 * 管理path
 */

public class PatchManager {
    private Context context;
    AndFixManager andFixManager;
    File src;
    public PatchManager(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        andFixManager=new AndFixManager(context);
    }

    public  void loadPatch(String patch) {
        src=new File(patch);
        Patch patch1=new Patch(src,context);
        loadPatch(patch1);

    }

    public void loadPatch(Patch patch) {
        //PathClassLoader
      ClassLoader classLoader=context.getClassLoader();
        List<String>  list;
        for (String name:patch.getPatchNames())
        {
            list=patch.getClasses(name);
            andFixManager.fix(src,classLoader,list);
        }
    }
}
