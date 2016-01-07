package com.ludei.share.cordova;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import org.apache.cordova.*;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SharePlugin extends CordovaPlugin {


    private CallbackContext _pendingCallback;
    private static int SHARE_REQUEST_CODE = 2312531;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if ("share".equals(action)) {


            JSONObject dic = args.getJSONObject(0);
            final String message = dic.optString("message");
            final String subject = dic.optString("subject");
            final String url = dic.optString("url");
            Object image = dic.get("image");
            String[] images = null;
            if (image != null && image instanceof JSONArray) {

                JSONArray array = (JSONArray) image;
                images = new String[array.length()];
                for (int i = 0; i < array.length(); ++i) {
                    images[i] = array.getString(i);
                }
            }
            else if (image != null) {
                images = new String[]{image.toString()};
            }
            final String[] files = images;

            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    SharePlugin.this.sendIntent(callbackContext, message, subject, files, url);
                }
            });
            return true;
        }
        else {
            JSONArray array = new JSONArray();
            array.put(""); //activity name
            array.put(false); //completed
            array.put("SharePlugin: " + action + " action not found");
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, array));
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (_pendingCallback != null && requestCode == SHARE_REQUEST_CODE) {
            JSONArray array = new JSONArray();
            array.put(""); //activity name
            array.put(true); //completed
            array.put(null);
            _pendingCallback.sendPluginResult(new PluginResult(PluginResult.Status.OK, array));
            _pendingCallback = null;
        }
    }


    private void sendIntent(final CallbackContext callbackContext, final String message, final String subject, final String[] files, final String url) {

        final boolean hasMultipleAttachments = files.length > 1;
        final Intent intent = new Intent(hasMultipleAttachments ? Intent.ACTION_SEND_MULTIPLE : Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);


        String msg = message;

        if (files.length > 0) {
            ArrayList<Uri> fileUris = new ArrayList<Uri>();
            try {
                final String dir = getDownloadDir();
                Uri fileUri = null;
                for (int i = 0; i < files.length; i++) {
                    fileUri = getFileUriAndSetType(intent, dir, files[i], subject, i);
                    if (fileUri != null) {
                        fileUris.add(fileUri);
                    }
                }
                if (!fileUris.isEmpty()) {
                    if (files.length > 1) {
                        intent.putExtra(Intent.EXTRA_STREAM, fileUris);
                    } else {
                        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    }
                }
            } catch (Exception e) {
                JSONArray array = new JSONArray();
                array.put(""); //activity name
                array.put(false); //completed
                array.put(e.getMessage());
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, array));
                return;
            }
        } else {
            intent.setType("text/plain");
        }

        if (notEmpty(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        // add the URL to the message, as there seems to be no separate field
        if (notEmpty(url)) {
            if (notEmpty(message)) {
                msg += " " + url;
            } else {
                msg = url;
            }
        }
        if (notEmpty(message)) {
            intent.putExtra(android.content.Intent.EXTRA_TEXT, msg);
            intent.putExtra("sms_body", msg); // sometimes required when the user picks share via sms
        }

        _pendingCallback = callbackContext;
        this.cordova.setActivityResultCallback(this);
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                SharePlugin.this.cordova.startActivityForResult(SharePlugin.this, Intent.createChooser(intent, null), SHARE_REQUEST_CODE);
            }
        });

    }



    //Helper utils taken from https://github.com/EddyVerbruggen/SocialSharing-PhoneGap-Plugin

    private Uri getFileUriAndSetType(Intent sendIntent, String dir, String image, String subject, int nthFile) throws IOException {

        // we're assuming an image, but this can be any filetype you like
        String localImage = image;
        sendIntent.setType("image/*");
        if (image.startsWith("http") || image.startsWith("www/")) {
            String filename = getFileName(image);
            localImage = "file://" + dir + "/" + filename;
            if (image.startsWith("http")) {
                // filename optimisation taken from https://github.com/EddyVerbruggen/SocialSharing-PhoneGap-Plugin/pull/56
                URLConnection connection = new URL(image).openConnection();
                String disposition = connection.getHeaderField("Content-Disposition");
                if (disposition != null) {
                    final Pattern dispositionPattern = Pattern.compile("filename=([^;]+)");
                    Matcher matcher = dispositionPattern.matcher(disposition);
                    if (matcher.find()) {
                        filename = matcher.group(1).replaceAll("[^a-zA-Z0-9._-]", "");
                        localImage = "file://" + dir + "/" + filename;
                    }
                }
                saveFile(getBytes(connection.getInputStream()), dir, filename);
            } else {
                saveFile(getBytes(this.cordova.getActivity().getAssets().open(image)), dir, filename);
            }
        } else if (image.startsWith("data:")) {
            // safeguard for https://code.google.com/p/android/issues/detail?id=7901#c43
            if (!image.contains(";base64,")) {
                sendIntent.setType("text/plain");
                return null;
            }
            // image looks like this: data:image/png;base64,R0lGODlhDAA...
            final String encodedImg = image.substring(image.indexOf(";base64,") + 8);
            // correct the intent type if anything else was passed, like a pdf: data:application/pdf;base64,..
            if (!image.contains("data:image/")) {
                sendIntent.setType(image.substring(image.indexOf("data:") + 5, image.indexOf(";base64")));
            }
            // the filename needs a valid extension, so it renders correctly in target apps
            final String imgExtension = image.substring(image.indexOf("/") + 1, image.indexOf(";base64"));
            String fileName;
            // if a subject was passed, use it as the filename
            // filenames must be unique when passing in multiple files [#158]
            if (notEmpty(subject)) {
                fileName = sanitizeFilename(subject) + (nthFile == 0 ? "" : "_" + nthFile) + "." + imgExtension;
            } else {
                fileName = "file" + (nthFile == 0 ? "" : "_" + nthFile) + "." + imgExtension;
            }
            saveFile(Base64.decode(encodedImg, Base64.DEFAULT), dir, fileName);
            localImage = "file://" + dir + "/" + fileName;
        } else if (!image.startsWith("file://")) {
            throw new IllegalArgumentException("URL_NOT_SUPPORTED");
        }
        return Uri.parse(localImage);
    }

    private void createOrCleanDir(final String downloadDir) throws IOException {
        final File dir = new File(downloadDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("CREATE_DIRS_FAILED");
            }
        } else {
            cleanupOldFiles(dir);
        }
    }

    private String getDownloadDir() throws IOException {
        final String dir = cordova.getActivity().getExternalFilesDir(null) + "/socialsharing-downloads"; // external
        createOrCleanDir(dir);
        return dir;
    }

    private String getFileName(String url) {
        final int lastIndexOfSlash = url.lastIndexOf('/');
        if (lastIndexOfSlash == -1) {
            return url;
        } else {
            return url.substring(lastIndexOfSlash + 1);
        }
    }

    private byte[] getBytes(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayBuffer baf = new ByteArrayBuffer(5000);
        int current;
        while ((current = bis.read()) != -1) {
            baf.append((byte) current);
        }
        return baf.toByteArray();
    }

    private void saveFile(byte[] bytes, String dirName, String fileName) throws IOException {
        final File dir = new File(dirName);
        final FileOutputStream fos = new FileOutputStream(new File(dir, fileName));
        fos.write(bytes);
        fos.flush();
        fos.close();
    }

    private static boolean notEmpty(String what) {
        return what != null &&
                !"".equals(what) &&
                !"null".equalsIgnoreCase(what);
    }

    private void cleanupOldFiles(File dir) {
        for (File f : dir.listFiles()) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    public static String sanitizeFilename(String name) {
        return name.replaceAll("[:\\\\/*?|<> ]", "_");
    }


}
