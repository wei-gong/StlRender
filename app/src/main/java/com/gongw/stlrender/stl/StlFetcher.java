package com.gongw.stlrender.stl;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gw on 2016/6/29.
 */

public class StlFetcher {
    private static final String TAG = StlFetcher.class.getSimpleName();

    /**
     * fetch stl file to STLObject
     * @param stlFile
     * @param callback
     */
    public static void fetchStlFile(final File stlFile, final StlFetchCallback callback){
        final STLObject stlObject = new STLObject();
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(){
            @Override
            public void run() {
                if(callback != null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onBefore();
                        }
                    });
                }
                //check stl type, text or binary
                if(isTextFile(stlFile)){
                    Log.i(TAG, "fetch text file");
                    fetchTextFile(stlObject, stlFile, handler, callback);
                }else{
                    Log.i(TAG, "fetch binary file");
                    fetchBinaryFile(stlObject, stlFile, handler, callback);
                }
            }
        }.start();
    }

    /**
     * check file type, text or binary
     * @param stlFile
     * @return
     */
    private static boolean isTextFile(File stlFile){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(stlFile));
            br.skip(80);
            int line = 0;
            String buffer;
            while((buffer = br.readLine()) != null && line < 5){
                line ++;
                if(buffer.contains("facet")||buffer.contains("outer")||buffer.contains("vertex")||buffer.contains("end")){
                    return true;
                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

//    private static boolean isTextFile(File stlFile){
//        boolean isText = true;
//        LINE_NUM = 0;
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream(stlFile);
//            int b;
//            while((b = fis.read()) != -1){
//                if (b == 0x0a || b == 0x0d || b == 0x09) {
//                    // white spaces
//                    if(b == 0x0a){
//                        //remember line num for fetching text file
//                        LINE_NUM ++;
//                    }
//                    continue;
//                }
//                if (b < 0x20 || (0xff & b) >= 0x80) {
//                    // control codes
//                    isText = false;
//                    //line num no need for fetching binary file
//                    LINE_NUM = 0;
//                    break;
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            if(fis != null){
//                try {
//                    fis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return isText;
//    }

    /**
     * fetch stl file of text type
     * konwledge about stl:
     * <url>http://baike.baidu.com/link?url=pufdhCnxTAiup4GCn5WMGGoYR5E0ygeWpgbyKDvPop7mmbfkG1qos1Lc7K379QVgOQOUYxYYdzYzZYq-4m6flq</url>
     * @param stlObject
     * @param stlFile
     */
    private static void fetchTextFile(final STLObject stlObject, final File stlFile, final Handler handler, final StlFetchCallback callback){
        List<Float> normalList = new ArrayList<>();
        List<Float> vertexList = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(stlFile));
            String str;
            long size = stlFile.length();
            long current = 0;
            long lastTime = System.currentTimeMillis();
            while ((str = br.readLine()) != null){
                current += str.length();
                str = str.trim();
                //get triangle's normal info
                if(str.startsWith("facet normal ")){
                    str = str.replaceFirst("(^facet normal)([ \\f\\r\\t\\n]+)", "");
                    for(int i=0;i<3;i++){
                        normalList.add(Float.parseFloat(str.substring(0, str.indexOf(" "))));
                        normalList.add(Float.parseFloat(str.substring(str.indexOf(" ")+1, str.lastIndexOf(" "))));
                        normalList.add(Float.parseFloat(str.substring(str.lastIndexOf(" ")+1)));
                    }
                    stlObject.triangleCount ++;
                }
                //get triangle's vertex info
                if(str.startsWith("vertex ")){
                    str = str.replaceFirst("(^vertex)([ \\f\\r\\t\\n]+)", "");
                    float x = Float.parseFloat(str.substring(0, str.indexOf(" ")));
                    float y = Float.parseFloat(str.substring(str.indexOf(" ")+1, str.lastIndexOf(" ")));
                    float z = Float.parseFloat(str.substring(str.lastIndexOf(" ")+1));
                    vertexList.add(x);
                    vertexList.add(y);
                    vertexList.add(z);
                    adjustMaxMin(stlObject, x, y, z);
                }

                if(callback != null){
                    final int progress = (int) ((1.0f * current/size) * 100);
                    long time = System.currentTimeMillis();
                    //update progress every 1 second
                    if(time - lastTime > 1000){
                        lastTime = time;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onProgress(progress);
                            }
                        });
                    }
                }
            }

            ByteBuffer normalBuffer = ByteBuffer.allocateDirect(normalList.size() * 4);
            normalBuffer.order(ByteOrder.nativeOrder());
            stlObject.normalBuffer = normalBuffer.asFloatBuffer();
            int i = 0;
            for(float f : normalList){
                stlObject.normalBuffer.position(i++);
                stlObject.normalBuffer.put(f);
            }
            stlObject.normalBuffer.position(0);

            ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(vertexList.size() * 4);
            vertexBuffer.order(ByteOrder.nativeOrder());
            stlObject.vertexBuffer = vertexBuffer.asFloatBuffer();
            i = 0;
            for(float f : vertexList){
                stlObject.vertexBuffer.position(i++);
                stlObject.vertexBuffer.put(f);
            }
            stlObject.vertexBuffer.position(0);

            if(callback != null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFinish(stlObject);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(callback != null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError();
                    }
                });
            }
        }finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * fetch stl file of binary type
     * knowledge about stl:
     * <url>http://baike.baidu.com/link?url=pufdhCnxTAiup4GCn5WMGGoYR5E0ygeWpgbyKDvPop7mmbfkG1qos1Lc7K379QVgOQOUYxYYdzYzZYq-4m6flq</url>
     * @param stlObject
     * @param stlFile
     * @param handler
     * @param callback
     */
    private static void fetchBinaryFile(final STLObject stlObject, File stlFile, Handler handler, final StlFetchCallback callback){
        FileInputStream fis = null;
        float[] normalArray;
        float[] vertexArray;
        try {
            fis = new FileInputStream(stlFile);
            fis.skip(80);
            byte[] vertex_size = new byte[4];
            fis.read(vertex_size);
            //java use BigEdian，OpenGL needs LittleEdian
            stlObject.triangleCount = getIntByLittleEndian(vertex_size, 0);
            normalArray = new float[stlObject.triangleCount * 3 * 3];
            vertexArray = new float[stlObject.triangleCount * 3 * 3];
            byte[] facet = new byte[50];
            int num = 0;
            long lastTime = System.currentTimeMillis();
            while(fis.read(facet) != -1){
                //get triangle's vertex info
                for(int i=0;i<3;i++){
                    normalArray[num ++] = Float.intBitsToFloat(getIntByLittleEndian(facet, 0));
                    normalArray[num ++] = Float.intBitsToFloat(getIntByLittleEndian(facet, 4));
                    normalArray[num ++] = Float.intBitsToFloat(getIntByLittleEndian(facet, 8));
                }

                //get triangle's vertex info
                for(int i=0;i<3;i++){
                    float x = Float.intBitsToFloat(getIntByLittleEndian(facet, (i*12)+12));
                    float y = Float.intBitsToFloat(getIntByLittleEndian(facet, (i*12)+16));
                    float z = Float.intBitsToFloat(getIntByLittleEndian(facet, (i*12)+20));
                    vertexArray[(num-9) + (i*3)] = x;
                    vertexArray[(num-9)  + (i*3) + 1] = y;
                    vertexArray[(num-9)  + (i*3) + 2] = z;
                    adjustMaxMin(stlObject, x, y, z);
                }
                if(callback != null){
                    final int progress = (int) (1.0f * num/(stlObject.triangleCount*3*3) * 100);
                    long time = System.currentTimeMillis();
                    //update progress every 1 second
                    if(time - lastTime > 1000){
                        lastTime = time;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onProgress(progress);
                            }
                        });
                    }
                }
            }
            ByteBuffer normal = ByteBuffer.allocateDirect(normalArray.length * 4);
            normal.order(ByteOrder.nativeOrder());
            stlObject.normalBuffer = normal.asFloatBuffer();
            stlObject.normalBuffer.put(normalArray);
            stlObject.normalBuffer.position(0);

            ByteBuffer vertex = ByteBuffer.allocateDirect(vertexArray.length * 4);
            vertex.order(ByteOrder.nativeOrder());
            stlObject.vertexBuffer = vertex.asFloatBuffer();
            stlObject.vertexBuffer.put(vertexArray);
            stlObject.vertexBuffer.position(0);

            if(callback != null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFinish(stlObject);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(callback != null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError();
                    }
                });
            }
        }finally {
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * update stl model's size info
     * @param stlObject
     * @param x
     * @param y
     * @param z
     */
    private static void adjustMaxMin(STLObject stlObject, float x, float y, float z){
        if (x > stlObject.maxX) {
            stlObject.maxX = x;
        }
        if (y > stlObject.maxY) {
            stlObject.maxY = y;
        }
        if (z > stlObject.maxZ) {
            stlObject.maxZ = z;
        }
        if (x < stlObject.minX) {
            stlObject.minX = x;
        }
        if (y < stlObject.minY) {
            stlObject.minY = y;
        }
        if (z < stlObject.minZ) {
            stlObject.minZ = z;
        }
    }

    /**
     * change BigEdian to LittleEdian
     * @param bytes
     * @param offset
     * @return
     */
    private static int getIntByLittleEndian(byte[] bytes, int offset){
        return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16) | ((0xff & bytes[offset + 3]) << 24);
    }
}