package com.example.masqu.myapplication;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = getContext();

        assertEquals("com.example.masqu.myapplication", appContext.getPackageName());
    }

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void GetFilesTest() throws Exception {
        Context appContext = getContext();

        ArrayList<String> fileList = new ArrayList<String>();

        File root = new File(Environment.getRootDirectory().getAbsolutePath());
        fileList = getFile(root, fileList);
    }

    private ArrayList<String> getFile(File dir,ArrayList<String> fileList) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    getFile(file , fileList);
                } else {
                    if (file.getName().endsWith(".png")
                            || file.getName().endsWith(".jpg")
                            || file.getName().endsWith(".jpeg")
                            || file.getName().endsWith(".gif")
                            || file.getName().endsWith(".bmp")
                            || file.getName().endsWith(".webp")) {
                        String temp = file.getPath().substring(0, file.getPath().lastIndexOf('/'));
                        if (!fileList.contains(temp))
                            fileList.add(temp);
                    }
                }
            }
        }
        return fileList;
    }
}

